package com.airobotics.commandcenter.core;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Footprint;
import com.airobotics.api.entities.Instruction;
import com.airobotics.api.entities.Request;
import com.airobotics.commandcenter.api.Key;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.commandcenter.api.cache.RobotVal;
import com.airobotics.commandcenter.resources.InstructionResource;
import com.airobotics.commandcenter.resources.RobotResource;
import com.airobotics.core.util.GeometryUtil;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;

import net.rubyeye.xmemcached.exception.MemcachedException;

public class InstructionService {
	private static final Logger logger = LoggerFactory.getLogger(InstructionService.class);
	private int cacheDuration = 43200; // 12 hours
	private double robotSpeed = 120; // degrees per second

	private String id;
	private ResourceData resourceData;
	private BeaconLoc firstBeacon;
	private Request request;
	private double currentDirectionAngle;
	private Coordinate currentLocation;
	private int routeIdx;

	public InstructionService(String id, Request request, Coordinate currentLocation, List<BeaconLoc> beaconLocs,
			ResourceData resourceData) {
		this.id = id;
		this.resourceData = resourceData;
		this.firstBeacon = beaconLocs.get(0);
		this.request = request;
		this.currentDirectionAngle = request.getDirectionAngle();
		this.currentLocation = currentLocation;
		this.routeIdx = request.getRouteIdx();
	}

	public static boolean isCurrentLocationOnNextRoutePoint(Coordinate currentLocation, Coordinate nextRoutePoint,
			double coverageDiameter) {
		return nextRoutePoint.distance(currentLocation) <= coverageDiameter;
	}

	public static boolean isCurrentLocationOnTrack(Coordinate currentLocation, Coordinate prevRoutePoint,
			Coordinate nextRoutePoint, double coverageDiameter) {
		LineSegment routeLine = new LineSegment(prevRoutePoint, nextRoutePoint);
		return routeLine.distance(currentLocation) <= coverageDiameter;
	}

	public static double getRelativeDirectionAngleFromCurrentLocationToNextLocation(Coordinate currentLocation,
			Coordinate nextRoutePoint, Coordinate beaconLocation, double angleFromInitialBeaconAngleWhichIs0ToBeacon,
			double currentDirectionAngle) {
		// direction angle 0 == beacon angle 0
		// e.g. tacho count 0 for both direction motor and beacon motor should
		// be same
		// e.g. starting point of the direction motor and beacon motor is always
		// same
		return getAngleBetweenClockwise(beaconLocation, currentLocation, nextRoutePoint)
				+ angleFromInitialBeaconAngleWhichIs0ToBeacon - currentDirectionAngle;
	}

	protected static double getAngleBetweenClockwise(Coordinate tip1, Coordinate tail, Coordinate tip2) {
		return -Angle.toDegrees(Angle.angleBetweenOriented(tip1, tail, tip2));
	}

	public static Coordinate getMinExternalLocation(String id, String scheduleId, ResourceData resourceData)
			throws TimeoutException, InterruptedException, MemcachedException {
		String key = (scheduleId == null) ? Key.min_external_locationn_key + id
				: Key.min_external_locationn_key + scheduleId;
		Coordinate minExternalLocation = resourceData.memcachedClient.get(key);
		if (minExternalLocation == null) {
			minExternalLocation = getMinExternalLocationUncached(id, scheduleId, resourceData);
			resourceData.memcachedClient.set(key, InstructionResource.getCacheDuration(), minExternalLocation);
		}
		return minExternalLocation;
	}

	public static Coordinate getMinExternalLocationUncached(String id, String scheduleId, ResourceData resourceData) {
		List<Boundary> boundaries = (scheduleId == null) ? new RobotResource(resourceData).getBoundaries(id)
				: new RobotResource(resourceData).getBoundaries(id, scheduleId);
		List<Beacon> beacons = new RobotResource(resourceData).getBeacons(id);
		List<Coordinate> envelopeCoordinates = RobotService.getEnvelopeCoordinates(beacons, boundaries);

		Envelope envelope = new GeometryFactory()
				.createLineString(envelopeCoordinates.toArray(new Coordinate[envelopeCoordinates.size()]))
				.getEnvelopeInternal();
		return new Coordinate(envelope.getMinX(), envelope.getMinY());
	}

	public static Coordinate getExternalLocation(String id, ResourceData resourceData, Coordinate internalLocation)
			throws TimeoutException, InterruptedException, MemcachedException {
		return getExternalLocation(internalLocation, getMinExternalLocation(id, null, resourceData));
	}

	public static Coordinate getExternalLocation(Coordinate internalLocation, Coordinate minExternalLocation) {
		internalLocation.x += minExternalLocation.x;
		internalLocation.y += minExternalLocation.y;
		internalLocation.x /= GeometryUtil.coordinateConversion;
		internalLocation.y /= GeometryUtil.coordinateConversion;
		return internalLocation;
	}

	public Instruction generateInstruction() throws TimeoutException, InterruptedException, MemcachedException {
		RobotVal robotVal = resourceData.memcachedClient.get(Key.robot_val_key + id);
		Coordinate prevRoutePoint = resourceData.memcachedClient
				.get(Key.schedule_working_route_key + id + "." + routeIdx);
		Coordinate nextRoutePoint = resourceData.memcachedClient
				.get(Key.schedule_working_route_key + id + "." + (routeIdx + 1));
		if (nextRoutePoint == null)
			return new Instruction(Instruction.Type.Done);

		return getNextInstruction(robotVal, prevRoutePoint, nextRoutePoint);
	}

	public Instruction getNextInstruction(RobotVal robotVal, Coordinate prevRoutePoint, Coordinate nextRoutePoint)
			throws TimeoutException, InterruptedException, MemcachedException {
		if (isCurrentLocationOnNextRoutePoint(currentLocation, nextRoutePoint, robotVal.getCoverageDiameter())) {
			routeIdx++;
			nextRoutePoint = moveToNextRoutePoint(robotVal);
			if (nextRoutePoint == null)
				return new Instruction(Instruction.Type.Done);
		} else {
			if (isCurrentLocationOnTrack(currentLocation, prevRoutePoint, nextRoutePoint,
					robotVal.getCoverageDiameter()))
				resourceData.memcachedClient.set(Key.last_on_track_point_key + id, cacheDuration, currentLocation);
			else
				nextRoutePoint = moveBackToLastOnTrackPoint(prevRoutePoint);
		}

		final Instruction nextInstruction = getUpdateInstruction(nextRoutePoint, robotVal);
		logFootprintAsync(nextInstruction);
		return nextInstruction;
	}

	private Coordinate moveToNextRoutePoint(RobotVal robotVal)
			throws TimeoutException, InterruptedException, MemcachedException {
		robotVal.setRouteIdx(routeIdx);
		resourceData.memcachedClient.set(Key.robot_val_key + id, cacheDuration, robotVal);
		resourceData.memcachedClient.set(Key.last_on_track_point_key + id, cacheDuration, currentLocation);
		return resourceData.memcachedClient.get(Key.schedule_working_route_key + id + "." + (routeIdx + 1));
	}

	private Coordinate moveBackToLastOnTrackPoint(Coordinate prevRoutePoint)
			throws TimeoutException, InterruptedException, MemcachedException {
		Coordinate pointToMoveBack = resourceData.memcachedClient.get(Key.last_on_track_point_key + id);
		if (pointToMoveBack == null)
			pointToMoveBack = prevRoutePoint;
		return pointToMoveBack;
	}

	private Instruction getUpdateInstruction(Coordinate nextRoutePoint, RobotVal robotVal) {
		double directionAngle = getRelativeDirectionAngleFromCurrentLocationToNextLocation(currentLocation, nextRoutePoint,
				firstBeacon.getLocation(), firstBeacon.getAngle(), currentDirectionAngle);
		double direction = RobotService.getDirection(robotVal.getWheelDiameter(),
				robotVal.getCoverageDiameter(), directionAngle);
		double distance = RobotService.getDistance(robotVal.getWheelDiameter(),
				currentLocation.distance(nextRoutePoint));
		double interval = distance / RobotService.getDistance(robotVal.getWheelDiameter(), robotSpeed);

		return new Instruction(Instruction.Type.Update, direction, distance, robotSpeed, interval, routeIdx);
	}

	public void logFootprintAsync(final Instruction nextInstruction) {
		final Date date = new Date();
		new Thread() {
			public void run() {
				resourceData.footprints.insert(new Footprint(id, date, request, currentLocation, nextInstruction));
			}
		}.start();
	}
}
