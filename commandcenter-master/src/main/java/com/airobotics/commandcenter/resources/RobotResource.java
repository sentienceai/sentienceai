package com.airobotics.commandcenter.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.core.InstructionService;
import com.airobotics.commandcenter.core.RobotService;
import com.airobotics.commandcenter.db.ResourceHelper;
import com.airobotics.core.util.GeometryUtil;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;

import net.rubyeye.xmemcached.exception.MemcachedException;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBUpdate;

@Path("/api/v1/robots")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class RobotResource {
	private ResourceData resourceData;

	public RobotResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Path("/{id}")
	@Timed
	public Robot getRobot(@PathParam("id") String id) {
		Robot result = resourceData.robotCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@GET
	@Timed
	public Robot getRobotBySerialNumber(@QueryParam("serialNo") String serialNumber) {
		Robot result = resourceData.robotCollection.findOne(DBQuery.is("serialNumber", serialNumber));
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@POST
	@Timed
	public Response createRobot(/* @Auth User user, */
			@Valid Robot robot) {
		net.vz.mongodb.jackson.WriteResult<Robot, String> result = resourceData.robotCollection.insert(robot);
		return Response.status(Response.Status.CREATED).entity(result.getSavedObject()).build();
	}

	@PUT
	@Timed
	public Response updateRobot(/* @Auth User user, */
			@Valid Robot upated) {
		net.vz.mongodb.jackson.WriteResult<Robot, String> result = resourceData.robotCollection
				.updateById(upated.getId(), upated);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}")
	@Timed
	public Response deleteRobot(/* @Auth User user, */
			@PathParam("id") String id) throws IOException {
		deleteBeacons(id);
		deleteBoundaries(id);
		deleteSchedules(id);

		net.vz.mongodb.jackson.WriteResult<Robot, String> result = resourceData.robotCollection.removeById(id);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	private void deleteBeacons(String id) throws IOException {
		List<String> beaconIds = new ArrayList<String>();
		for (Beacon beacon : getBeacons(id))
			beaconIds.add(beacon.getId());
		removeBeacons(id, new ObjectMapper().writeValueAsString(beaconIds));
		for (String beaconId : beaconIds)
			new BeaconResource(resourceData).deleteBeacon(beaconId);
	}

	private void deleteBoundaries(String id) {
		for (Boundary boundary : getBoundaries(id))
			new BoundaryResource(resourceData).deleteBoundary(boundary.getId());
	}

	private void deleteSchedules(String id) {
		for (Schedule schedule : getSchedules(id)) {
			removeSchedule(id, schedule.getId());
			new ScheduleResource(resourceData).deleteSchedule(schedule.getId());
		}
	}

	@PUT
	@Path("/{id}/internal_locations")
	@Timed
	public Response updateInternalLocations(/* @Auth User user, */
			@PathParam("id") String id) {
		Robot robot = resourceData.robotCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(robot);

		new RobotService(resourceData).updateInternalLocations(robot, getBeacons(robot), getBoundaries(robot),
				getSchedules(robot));
		return Response.status(Response.Status.OK).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@GET
	@Path("/{id}/beacons")
	@Timed
	public List<Beacon> getBeacons(@PathParam("id") String id) {
		Robot robot = resourceData.robotCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(robot);

		return this.getBeacons(robot);
	}

	@POST
	@Path("/{id}/beacons/{beacon_ids}")
	@Timed
	public Response addBeacons(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("beacon_ids") String beaconIds)
			throws JsonParseException, JsonMappingException, IOException {
		List<String> beaconIdList = new ObjectMapper().readValue(beaconIds, new TypeReference<List<String>>() {
		});
		resourceData.robotCollection.updateById(id, DBUpdate.addToSet("beacons", beaconIdList));
		// updateInternalLocations(id);

		return Response.status(Response.Status.CREATED).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@DELETE
	@Path("/{id}/beacons/{beacon_ids}")
	@Timed
	public Response removeBeacons(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("beacon_ids") String beaconIds)
			throws JsonParseException, JsonMappingException, IOException {
		List<String> beaconIdList = new ObjectMapper().readValue(beaconIds, new TypeReference<List<String>>() {
		});
		resourceData.robotCollection.updateById(id, DBUpdate.pull("beacons", beaconIdList));
		// updateInternalLocations(id);

		return Response.status(Response.Status.OK).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/schedules")
	@Timed
	public List<Schedule> getSchedules(@PathParam("id") String id) {
		Robot robot = resourceData.robotCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(robot);

		return this.getSchedules(robot);
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/boundaries")
	@Timed
	public List<Boundary> getBoundaries(@PathParam("id") String id) {
		Robot robot = resourceData.robotCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(robot);

		return this.getBoundaries(robot);
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/schedules/{schedule_id}/boundaries")
	@Timed
	public List<Boundary> getBoundaries(@PathParam("id") String id, @PathParam("schedule_id") String scheduleId) {
		Schedule schedule = resourceData.scheduleCollection.findOneById(scheduleId);
		ResourceHelper.notFoundIfNull(schedule);

		return this.getBoundaries(schedule);
	}

	@POST
	@Path("/{id}/schedules/{schedule_id}")
	@Timed
	public Response addSchedule(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId) {
		resourceData.robotCollection.updateById(id, DBUpdate.addToSet("schedules", scheduleId));
		// updateInternalLocations(id);

		return Response.status(Response.Status.CREATED).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@DELETE
	@Path("/{id}/schedules/{schedule_id}")
	@Timed
	public Response removeSchedule(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId) {
		resourceData.robotCollection.updateById(id, DBUpdate.pull("schedules", scheduleId));
		// updateInternalLocations(id);

		return Response.status(Response.Status.OK).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@POST
	@Path("/{id}/schedules/{schedule_id}/boundary/{boundary_id}")
	@Timed
	public Response setBoundary(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId,
			@PathParam("boundary_id") String boundaryId) {
		resourceData.scheduleCollection.updateById(scheduleId, DBUpdate.set("boundary", boundaryId));
		// updateInternalLocations(id);

		return Response.status(Response.Status.CREATED).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@PUT
	@Path("/{id}/schedules/{schedule_id}/boundary")
	@Timed
	public Response updateBoundary(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId, @Valid Boundary upated) {
		net.vz.mongodb.jackson.WriteResult<Boundary, String> result = resourceData.boundaryCollection
				.updateById(upated.getId(), upated);
		// updateInternalLocations(id);

		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}/schedules/{schedule_id}/boundary")
	@Timed
	public Response removeBoundary(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId) {
		resourceData.scheduleCollection.updateById(scheduleId, DBUpdate.unset("boundary"));
		// updateInternalLocations(id);

		return Response.status(Response.Status.OK).entity(resourceData.robotCollection.findOneById(id)).build();
	}

	@PUT
	@Path("/{id}/schedules/{schedule_id}/routes")
	@Timed
	public Response updateRoute(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("schedule_id") String scheduleId, @Valid List<Coordinate> route)
			throws TimeoutException, InterruptedException, MemcachedException {
		Coordinate minExternalLocation = InstructionService.getMinExternalLocation(id, scheduleId, resourceData);
		List<Coordinate> internalRoute = new ArrayList<Coordinate>();
		for (Coordinate coordinate : route)
			internalRoute.add(new Coordinate(
					(long) Math.round(coordinate.x * GeometryUtil.coordinateConversion - minExternalLocation.x),
					(long) Math.round(coordinate.y * GeometryUtil.coordinateConversion - minExternalLocation.y)));
		resourceData.scheduleCollection.updateById(scheduleId, DBUpdate.set("route", internalRoute));
		return Response.status(Response.Status.OK).entity(resourceData.scheduleCollection.findOneById(scheduleId))
				.build();
	}

	List<Beacon> getBeacons(Robot robot) {
		List<Beacon> beacons = new ArrayList<Beacon>();
		for (String beaconId : robot.getBeacons()) {
			Beacon beacon = resourceData.beaconCollection.findOneById(beaconId);
			if (beacon != null)
				beacons.add(beacon);
		}

		return beacons;
	}

	public List<Schedule> getSchedules(Robot robot) {
		List<Schedule> schedules = new ArrayList<Schedule>();
		for (String scheduleId : robot.getSchedules()) {
			Schedule schedule = resourceData.scheduleCollection.findOneById(scheduleId);
			if (schedule != null)
				schedules.add(schedule);
		}

		return schedules;
	}

	List<Boundary> getBoundaries(Robot robot) {
		List<Boundary> boundaries = new ArrayList<Boundary>();
		for (Schedule schedule : this.getSchedules(robot)) {
			Boundary boundary = resourceData.boundaryCollection.findOneById(schedule.getBoundary());
			if (boundary != null)
				boundaries.add(boundary);
		}

		return boundaries;
	}

	List<Boundary> getBoundaries(Schedule schedule) {
		List<Boundary> boundaries = new ArrayList<Boundary>();
		Boundary boundary = resourceData.boundaryCollection.findOneById(schedule.getBoundary());
		if (boundary != null)
			boundaries.add(boundary);

		return boundaries;
	}
}
