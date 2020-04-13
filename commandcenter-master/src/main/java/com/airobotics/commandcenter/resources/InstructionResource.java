package com.airobotics.commandcenter.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Instruction;
import com.airobotics.api.entities.Request;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.ScheduleInstance;
import com.airobotics.api.entities.ScheduleType;
import com.airobotics.commandcenter.api.Key;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.commandcenter.api.cache.RobotVal;
import com.airobotics.commandcenter.core.AngleOutsideBeaconsRangeException;
import com.airobotics.commandcenter.core.InstructionService;
import com.airobotics.commandcenter.core.RobotService;
import com.airobotics.core.LaGps;
import com.airobotics.core.geom.CircleCannotBeFormedWithLineException;
import com.airobotics.core.geom.CirclesIntersectedOutsideMaxToleranceDistanceException;
import com.airobotics.core.geom.CirclesNotIntersectedException;
import com.airobotics.core.geom.TriangleWithPoint;
import com.airobotics.core.util.AngleUtil;
import com.airobotics.core.util.GeometryUtil;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import net.rubyeye.xmemcached.exception.MemcachedException;

@Path("/api/v1/robots/{id}/instructions")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class InstructionResource {
	final static Logger logger = LoggerFactory.getLogger(InstructionResource.class);
	private ResourceData resourceData;
	RobotService robotService;

	private RobotVal robotVal = null;
	private static int cacheDuration = 43200; // 12 hours

	public static int getCacheDuration() {
		return cacheDuration;
	}

	public InstructionResource(ResourceData resourceData) {
		this.resourceData = resourceData;
		this.robotService = new RobotService(resourceData);
	}

	@GET
	@Timed
	public Instruction getInstruction(@PathParam("id") String id, @QueryParam("req") String reqJson)
			throws TimeoutException, InterruptedException, MemcachedException {
		Request req = readRequest(id, reqJson);

		if (isActive(req))
			return getActiveInstruction(id, req);

		return getInactiveInstruction(id);
	}

	@DELETE
	@Timed
	@Path("/cache")
	public Response clearCache(/* @Auth User user, */
			@PathParam("id") String id, @QueryParam("scheduleId") String scheduleId)
			throws TimeoutException, InterruptedException, MemcachedException {
		RobotVal currentVal = robotVal;
		resourceData.memcachedClient.delete(Key.robot_val_key + id);
		resourceData.memcachedClient.delete(Key.min_external_locationn_key + id);
		resourceData.memcachedClient.delete(Key.min_external_locationn_key + scheduleId);
		return Response.status(Response.Status.OK).entity(currentVal).build();
	}

	private Request readRequest(String id, String reqJson) {
		Request req = null;
		try {
			req = new ObjectMapper().readValue(reqJson, Request.class);
			robotVal = resourceData.memcachedClient.get(Key.robot_val_key + id);
			cacheDuration = (robotVal == null) ? cacheDuration : robotVal.getScheduleAverageRunningTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return req;
	}

	public boolean isActive(Request req) {
		return robotVal != null && (req.getRouteIdx() > -1 || robotVal.getRouteIdx() > -1);
	}

	public Instruction getActiveInstruction(String id, Request req)
			throws TimeoutException, InterruptedException, MemcachedException {
		// TODO: maybe robot lost power and just got back???
		if (req.getRouteIdx() != robotVal.getRouteIdx()) {

		}
		// TODO: Check touch/ultrasonic sensor first to see if something is
		// detected in the path
		if (req.getSensors().size() > 0) {

		}

		req.getBeaconSensors().removeAll(getBeaconSensorsToRemove(req));
		// Make sure that beacons are more than 3
		if (req.getBeaconSensors().size() < 3)
			return new Instruction(Instruction.Type.Resend);

		List<BeaconLoc> beaconLocs = this.loadBeaconLocs(req.getBeaconSensors());
		Coordinate currentLoc;
		try {
			currentLoc = LaGps.getLocation(getBoundary(id), getBeaconsCoordinates(beaconLocs));
		} catch (Exception e) {
			return new Instruction(Instruction.Type.Resend);
		}

		return new InstructionService(id, req, currentLoc, beaconLocs, resourceData).generateInstruction();
	}

	@SuppressWarnings("serial")
	private List<com.airobotics.core.geom.Coordinate> getBeaconsCoordinates(final List<BeaconLoc> beaconLocs) {
		return new ArrayList<com.airobotics.core.geom.Coordinate>() {
			{
				for (BeaconLoc beaconLoc : beaconLocs)
					add(new com.airobotics.core.geom.Coordinate(beaconLoc.getLocation(),
							(long) (beaconLoc.getAngle() * AngleUtil.anglePrecision)));
			}
		};
	}

	private Envelope getBoundary(String id) {
		Envelope boundary = null;
		try {
			boundary = resourceData.memcachedClient.get(Key.boundary_envelope_key + id);
			if (boundary == null) {
				boundary = loadBoundary(id);
				resourceData.memcachedClient.set(Key.boundary_envelope_key + id, cacheDuration, boundary);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boundary;
	}

	@SuppressWarnings("serial")
	protected Envelope loadBoundary(final String id) {
		List<Coordinate> envelopeCoordinates = new ArrayList<Coordinate>() {
			{
				for (Boundary boundary : new RobotResource(resourceData).getBoundaries(id))
					for (Coordinate extLoc : boundary.getBoundaryPoints())
						add(extLoc);
			}
		};
		return new GeometryFactory()
				.createLineString(envelopeCoordinates.toArray(new Coordinate[envelopeCoordinates.size()]))
				.getEnvelopeInternal();
	}

	private List<BeaconSensor> getBeaconSensorsToRemove(Request req) {
		List<BeaconSensor> beaconSensorsToRemove = new ArrayList<BeaconSensor>();
		for (BeaconSensor beaconSensor : req.getBeaconSensors())
			if (beaconSensor.getAngle() == 0)
				beaconSensorsToRemove.add(beaconSensor);
		return beaconSensorsToRemove;
	}

	public Instruction getInactiveInstruction(String id) {
		RobotResource robotResource = new RobotResource(resourceData);
		Robot robot = robotResource.getRobot(id);

		// Check if this robot needs to start working
		List<Schedule> schedules = robotResource.getSchedules(robot);
		Schedule activeSchedule = this.getActiveSchedule(schedules);
		if (activeSchedule == null)
			// Check again after 1 min
			return new Instruction(Instruction.Type.None, 1000 * 60);
		this.loadScheduleInstance(id, activeSchedule, robot);

		// Robot needs to start working
		// Populate memcache (takes too long time)
		// List<BeaconLoc[]> threeBeaconsSet =
		// RobotService.getBeaconsSetFromBeacons(robotResource.getBeacons(robot));
		// for (BeaconLoc[] beacons : threeBeaconsSet)
		// robotService.cacheLocationMaps(beacons[0].getLocation(),
		// beacons[1].getLocation(), beacons[2].getLocation());

		return new Instruction(Instruction.Type.New, 0, 0, 0, 1000, 0); // Get
																		// new
		// instruction
		// in 1 sec
	}

	@GET
	@Timed
	@Path("/current_location")
	public Coordinate getCurrentLocation(@PathParam("id") String id,
			@QueryParam("threeBeaconsWithAngle") String threeBeaconsWithAngleJson)
			throws JsonParseException, JsonMappingException, IOException {
		Coordinate currentLocation = null;

		List<BeaconLoc> beacons = new ObjectMapper().readValue(threeBeaconsWithAngleJson,
				new TypeReference<List<BeaconLoc>>() {
				});
		for (TriangleWithPoint triangleWithPoint : RobotService.getTriangleWithPoints(beacons)) {
			try {
				currentLocation = robotService.getCurrentLocation(triangleWithPoint);
			} catch (CircleCannotBeFormedWithLineException | CirclesNotIntersectedException
					| CirclesIntersectedOutsideMaxToleranceDistanceException | AngleOutsideBeaconsRangeException e) {
				throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.toString()).build());
			}
		}

		return currentLocation;
	}

	@GET
	@Timed
	@Path("/location")
	public Coordinate getLocationWithBeaconsScanningAngle(@PathParam("id") String id) throws JsonParseException,
			JsonMappingException, IOException, TimeoutException, InterruptedException, MemcachedException {
		List<String> beaconIds = resourceData.memcachedClient.get(Key.beacon_list_key);
		Coordinate internalLocation = LaGps.getLocation(getCoordinatesOfBeacons(beaconIds, id));
		return getExternalLocation(internalLocation, getMinExternalLocation(beaconIds));
	}

	@SuppressWarnings("serial")
	private List<com.airobotics.core.geom.Coordinate> getCoordinatesOfBeacons(final List<String> beaconIds,
			final String id) throws TimeoutException, InterruptedException, MemcachedException {
		return new ArrayList<com.airobotics.core.geom.Coordinate>() {
			{
				for (String beaconId : getBeaconIdsWithScanOrderFromRobotToUseExistingMethod(
						beaconIds)) {
					Integer angle = resourceData.memcachedClient.get(id + ":" + beaconId);
					if (angle != null)
						add(new com.airobotics.core.geom.Coordinate(
								(Coordinate) resourceData.memcachedClient
										.get(Key.beacon_internal_location_key + beaconId),
								convertAngleCollectedByBeaconToAngleCollectedByRobotToUseExistingMethod(angle)));
				}
			}
		};
	}

	private List<String> getBeaconIdsWithScanOrderFromRobotToUseExistingMethod(
			List<String> beaconIds) {
		Collections.reverse(beaconIds);
		return beaconIds;
	}

	private int convertAngleCollectedByBeaconToAngleCollectedByRobotToUseExistingMethod(Integer angle) {
		return 360 * AngleUtil.anglePrecision - angle.intValue();
	}

	private Coordinate getMinExternalLocation(List<String> beaconIds)
			throws TimeoutException, InterruptedException, MemcachedException {
		List<Coordinate> envelopeCoordinates = getEnvelopeExternalCoordinates(beaconIds);
		Envelope envelope = new GeometryFactory()
				.createLineString(envelopeCoordinates.toArray(new Coordinate[envelopeCoordinates.size()]))
				.getEnvelopeInternal();
		return new Coordinate(envelope.getMinX(), envelope.getMinY());
	}

	private Coordinate getExternalLocation(Coordinate internalLocation, Coordinate minExternalLocation) {
		internalLocation.x /= GeometryUtil.coordinateConversion;
		internalLocation.y /= GeometryUtil.coordinateConversion;
		internalLocation.x += minExternalLocation.x;
		internalLocation.y += minExternalLocation.y;
		return internalLocation;
	}

	@SuppressWarnings("serial")
	private ArrayList<Coordinate> getEnvelopeExternalCoordinates(final List<String> beaconIds)
			throws TimeoutException, InterruptedException, MemcachedException {
		return new ArrayList<Coordinate>() {
			{
				for (String beaconId : beaconIds)
					add((Coordinate) resourceData.memcachedClient.get(Key.beacon_external_location_key + beaconId));
			}
		};
	}

	private Schedule getActiveSchedule(List<Schedule> schedules) {
		Schedule activeSchedule = null;
		for (Schedule schedule : schedules)
			if (schedule.getType() == ScheduleType.ACTIVE.ordinal())
				activeSchedule = schedule;

		return activeSchedule;
	}

	private void loadScheduleInstance(String id, Schedule schedule, Robot robot) {
		// Load route
		List<Coordinate> route = schedule.getRoute();
		ScheduleResource scheduleResource = new ScheduleResource(resourceData);
		if (route == null || route.size() == 0) {
			new RobotResource(resourceData).updateInternalLocations(id);
			schedule = scheduleResource.getSchedule(schedule.getId());
			route = schedule.getRoute();
		}

		try {
			for (int i = 0; i < route.size(); i++) {
				resourceData.memcachedClient.set(Key.schedule_working_route_key + id + "." + i, cacheDuration,
						route.get(i));
			}
			RobotVal robotVal = new RobotVal(id, robot.getWheelDiameter(), robot.getCoverageDiameter(), 0,
					scheduleResource.getScheduleAverageRuntime(schedule.getId()));
			resourceData.memcachedClient.set(Key.robot_val_key + id, cacheDuration, robotVal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		@SuppressWarnings("unused")
		ScheduleInstance instance = new ScheduleInstance(schedule.getType());
		// Save schedule instance
	}

	@GET
	@Timed
	@Path("/beaconLocs")
	public List<BeaconLoc> getBeaconLocs(@QueryParam("beaconSensors") String beaconSensorsJson)
			throws JsonParseException, JsonMappingException, IOException {
		List<BeaconSensor> beaconSensors = new ObjectMapper().readValue(beaconSensorsJson,
				new TypeReference<List<BeaconSensor>>() {
				});
		return loadBeaconLocs(beaconSensors);
	}

	private List<BeaconLoc> loadBeaconLocs(List<BeaconSensor> beacons) {
		List<BeaconLoc> beaconLocs = new ArrayList<BeaconLoc>();
		for (BeaconSensor beaconSensor : beacons) {
			try {
				String key = Key.beacon_loc_key + beaconSensor.getId();
				BeaconLoc beaconLoc = resourceData.memcachedClient.get(key);
				if (beaconLoc != null)
					beaconLoc.setAngle(beaconSensor.getAngle());
				else
					beaconLoc = loadBeaconLoc(beaconSensor, key);
				beaconLocs.add(beaconLoc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return beaconLocs;
	}

	public BeaconLoc loadBeaconLoc(BeaconSensor beaconSensor, String key)
			throws TimeoutException, InterruptedException, MemcachedException {
		Coordinate beaconLocation = new BeaconResource(resourceData).getBeacon(beaconSensor.getId()).getLocation();
		BeaconLoc beaconLoc = new BeaconLoc(beaconSensor, beaconLocation);
		resourceData.memcachedClient.set(key, cacheDuration, beaconLoc);
		return beaconLoc;
	}
}
