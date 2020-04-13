package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.BeforeClass;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.api.entities.Instruction;
import com.airobotics.api.entities.Request;
import com.airobotics.commandcenter.core.BeaconService;
import com.airobotics.commandcenter.core.InstructionService;
import com.airobotics.commandcenter.core.RobotService;
import com.airobotics.core.util.GeometryUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class InstructionResourceTest extends ResourceTest {
	protected static List<Beacon> testBeacons;
	protected static List<Coordinate> route;
	private static List<Coordinate> boundaryPoints;
	protected int routeIdx = -1;
	private Coordinate lastOnTrackPoint;

	@BeforeClass
	public static void setUpClass() throws Exception {
		loadSchedule();
		loadBoundary();
		testBeacons = ResourceTestUtil.getBeaconsOfTestRobot(ResourceTestUtil.testRobot);
		route = ResourceTestUtil.testSchedule.getRoute();
		boundaryPoints = ResourceTestUtil.testBoundary.getBoundaryPoints();
	}

	@Before
	public void setUp() throws Exception {
		routeIdx = getInitialRouteIdx();
		lastOnTrackPoint = route.get(0);
	}

	private int getInitialRouteIdx() throws JsonProcessingException, UnsupportedEncodingException {
		Instruction instruction = getInitialInstruction();
		if (instruction.getType() == Instruction.Type.New)
			return instruction.getRouteIdx();
		// Memcache is not refreshed
		if (instruction.getType() == Instruction.Type.Resend || instruction.getType() == Instruction.Type.None)
			return 0;
		return -1;
	}

	private Instruction getInitialInstruction() throws JsonProcessingException, UnsupportedEncodingException {
		Response response = ResourceTestUtil.getResourceDeleteResponse(
				ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "instructions") + "/cache");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		response = ResourceTestUtil.getResourceGetResponse(
				ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "instructions"), "req",
				ResourceTestUtil.getJsonEntity(new Request()));
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		Instruction instruction = response.readEntity(Instruction.class);
		assertTrue(instruction.getType() == Instruction.Type.New || instruction.getType() == Instruction.Type.Resend
				|| instruction.getType() == Instruction.Type.None);
		return instruction;
	}

	public void assertIterateAllRoutePoints(List<Coordinate> routePoints) throws JsonProcessingException,
			UnsupportedEncodingException, CloneNotSupportedException, InterruptedException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		for (int i = 0; i < routePoints.size(); i++) {
			Coordinate currentLocation = routePoints.get(i);
			Coordinate nextRoutePoint = getNextRoutePoint(currentLocation);
			Instruction instruction = getInstruction(routeIdx, currentLocation, nextRoutePoint);
			if (instruction.getType() == Instruction.Type.Done)
				break;

			assertTrue(
					String.format("route idx: %d, currentLoc: %s, nextLoc: %s, instruction: %s", i, currentLocation,
							nextRoutePoint, instruction),
					Math.abs(getDistance(currentLocation, nextRoutePoint)
							- instruction.getDistance()) <= ResourceTestUtil.coverageDiameter);
			routeIdx = instruction.getRouteIdx();
		}
	}

	protected Coordinate getNextRoutePoint(Coordinate currentLocation) {
		Coordinate nextRoutePoint = null;
		if (routeIdx < route.size() - 1) {
			Coordinate prevRoutePoint = route.get(routeIdx);
			nextRoutePoint = route.get(routeIdx + 1);
			if (routeIdx < route.size() - 2)
				nextRoutePoint = getNextInstructionFromInstructionService(currentLocation, nextRoutePoint,
						prevRoutePoint);
		}
		return nextRoutePoint;
	}

	public Coordinate getNextInstructionFromInstructionService(Coordinate currentLocation, Coordinate nextRoutePoint,
			Coordinate prevRoutePoint) {
		if (InstructionService.isCurrentLocationOnNextRoutePoint(currentLocation, nextRoutePoint,
				ResourceTestUtil.coverageDiameter))
			nextRoutePoint = route.get(routeIdx + 2);
		else {
			if (InstructionService.isCurrentLocationOnTrack(currentLocation, prevRoutePoint, nextRoutePoint,
					ResourceTestUtil.coverageDiameter))
				lastOnTrackPoint = currentLocation;
			else
				nextRoutePoint = lastOnTrackPoint;
		}
		return nextRoutePoint;
	}

	private Instruction getInstruction(int routeIdx, Coordinate currentLocation, Coordinate nextRoutePoint)
			throws JsonProcessingException, UnsupportedEncodingException, CloneNotSupportedException {
		List<Beacon> beacons = getSortedBeacons(currentLocation);
		List<BeaconSensor> beaconSensors = BeaconService.getBeaconSensors(beacons, currentLocation);
		double directionAngle = InstructionService.getRelativeDirectionAngleFromCurrentLocationToNextLocation(
				currentLocation, nextRoutePoint, beacons.get(0).getLocation(), beaconSensors.get(0).getAngle(), 0);
		double direction = RobotService.getDirection(ResourceTestUtil.testRobot.getWheelDiameter(),
				ResourceTestUtil.testRobot.getCoverageDiameter(), directionAngle);
		return getAssertedInstruction(routeIdx, direction, beaconSensors);
	}

	public List<Beacon> getSortedBeacons(Coordinate currentLocation) throws CloneNotSupportedException {
		List<Beacon> beacons = new LinkedList<Beacon>();
		for (Beacon beacon : testBeacons)
			beacons.add((Beacon) beacon.clone());
		ResourceTestUtil.sort(beacons, currentLocation);
		return beacons;
	}

	public Instruction getAssertedInstruction(int routeIdx, double direction, List<BeaconSensor> beaconSensors)
			throws JsonProcessingException, UnsupportedEncodingException {
		Response response = getResponse(routeIdx, beaconSensors);
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		Instruction instruction = response.readEntity(Instruction.class);
		assertTrue(instruction.getType() == Instruction.Type.Update || instruction.getType() == Instruction.Type.Done);
		if (instruction.getType() != Instruction.Type.Done)
			assertTrue(
					String.format("routeIdx: %d, direction: %s, instruction: %s", routeIdx, direction,
							instruction.getDirection()),
					Math.abs(Math.abs(direction) - Math.abs(instruction.getDirection())) < 10);
		return instruction;
	}

	private Response getResponse(int routeIdx, List<BeaconSensor> beaconSensors)
			throws JsonProcessingException, UnsupportedEncodingException {
		Request req = new Request();
		req.setBeaconSensors((List<BeaconSensor>) beaconSensors);
		req.setRouteIdx(routeIdx);
		return ResourceTestUtil.getResourceGetResponse(
				ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "instructions"), "req",
				URLEncoder.encode(req.toJSONString()));
	}

	protected double getDistance(Coordinate currentLocation, Coordinate nextRoutePoint) {
		return RobotService.getDistance(ResourceTestUtil.testRobot.getWheelDiameter(),
				currentLocation.distance(nextRoutePoint));
	}

	protected List<Coordinate> getRouteWithMidPoint() {
		List<Coordinate> routeWithMidPoint = new ArrayList<Coordinate>();
		Coordinate prevRoutePoint = null;
		for (Coordinate routePoint : route) {
			addToRouteWithMidPoint(routeWithMidPoint, prevRoutePoint, routePoint);
			prevRoutePoint = routePoint;
		}
		return routeWithMidPoint;
	}

	private void addToRouteWithMidPoint(List<Coordinate> routeWithMidPoint, Coordinate prevRoutePoint,
			Coordinate routePoint) {
		if (prevRoutePoint != null && getDistance(prevRoutePoint, routePoint) > ResourceTestUtil.coverageDiameter)
			routeWithMidPoint.add(new LineSegment(prevRoutePoint, routePoint).midPoint());
		routeWithMidPoint.add(routePoint);
	}

	protected List<Coordinate> getRouteWithOffTrack() {
		List<Coordinate> routeWithOffTrack = new ArrayList<Coordinate>();
		Coordinate prevRoutePoint = null;
		for (Coordinate routePoint : route) {
			addToRouteWithOffTrack(routeWithOffTrack, prevRoutePoint, routePoint);
			prevRoutePoint = routePoint;
		}
		return routeWithOffTrack;
	}

	private void addToRouteWithOffTrack(List<Coordinate> routeWithOffTrack, Coordinate prevRoutePoint,
			Coordinate routePoint) {
		if (prevRoutePoint != null && getDistance(prevRoutePoint, routePoint) > ResourceTestUtil.coverageDiameter) {
			Coordinate midPoint = new LineSegment(prevRoutePoint, routePoint).midPoint();
			Coordinate offTrackPoint = getOffTrackPoint(midPoint, routePoint);
			routeWithOffTrack.add(midPoint);
			routeWithOffTrack.add(offTrackPoint);
			if (getDistance(midPoint, offTrackPoint) > ResourceTestUtil.coverageDiameter)
				routeWithOffTrack.add(midPoint);
		}
		routeWithOffTrack.add(routePoint);
	}

	private Coordinate getOffTrackPoint(Coordinate midPoint, Coordinate routePoint) {
		Coordinate offTrackPoint = getRegularTrianglePoint(midPoint, routePoint);
		if (!isInsideBoundary(offTrackPoint)) {
			Coordinate pointOnLine = new LineSegment(midPoint, routePoint).closestPoint(offTrackPoint);
			offTrackPoint = getRegularTrianglePointInsideBoundary(offTrackPoint, pointOnLine);
			while (!isInsideBoundary(offTrackPoint))
				offTrackPoint = new LineSegment(pointOnLine, offTrackPoint).midPoint();
		}
		return offTrackPoint;
	}

	private Coordinate getRegularTrianglePoint(Coordinate midPoint, Coordinate routePoint) {
		double translationVectorX = -midPoint.x + routePoint.x;
		double translationVectorY = -midPoint.y + routePoint.y;

		double rotatedVector60DegreesX = (translationVectorX * Math.cos(60 * Math.PI / 180))
				- (translationVectorY * Math.sin(60 * Math.PI / 180));
		double rotatedVector60DegreesY = (translationVectorX * Math.sin(60 * Math.PI / 180))
				+ (translationVectorY * Math.cos(60 * Math.PI / 180));

		return new Coordinate(rotatedVector60DegreesX + midPoint.x, rotatedVector60DegreesY + midPoint.y);
	}

	private boolean isInsideBoundary(Coordinate point) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = boundaryPoints.size() - 1; i < boundaryPoints.size(); j = i++) {
			if ((boundaryPoints.get(i).y > point.y) != (boundaryPoints.get(j).y > point.y)
					&& (point.x < (boundaryPoints.get(j).x - boundaryPoints.get(i).x)
							* (point.y - boundaryPoints.get(i).y) / (boundaryPoints.get(j).y - boundaryPoints.get(i).y)
							+ boundaryPoints.get(i).x)) {
				result = !result;
			}
		}
		return result;
	}

	private Coordinate getRegularTrianglePointInsideBoundary(Coordinate offTrackPoint, Coordinate pointOnLine) {
		return GeometryUtil.extendLine(new LineSegment(offTrackPoint, pointOnLine)).p1;
	}
}