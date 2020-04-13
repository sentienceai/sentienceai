package com.airobotics.commandcenter.resources.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Footprint;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.ScheduleType;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.core.InstructionService;
import com.airobotics.commandcenter.resources.RobotResource;
import com.airobotics.commandcenter.views.FootPrintView;
import com.mongodb.BasicDBObject;
import com.vividsolutions.jts.geom.Coordinate;

import net.rubyeye.xmemcached.exception.MemcachedException;
import net.vz.mongodb.jackson.DBCursor;

@Path("/app/v1/footprints")
@Produces(MediaType.TEXT_HTML)
public class FootprintResource {
	private ResourceData resourceData;

	public FootprintResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Path("/{id}")
	public FootPrintView getFootPrint(@PathParam("id") String id) {
		return new FootPrintView("footprint.ftl", id, getBoundaryPoints(id, false), getBeacons(id), getRoute(id));
	}

	@GET
	@Path("/{id}/live")
	public FootPrintView getLiveFootPrint(@PathParam("id") String id) {
		return new FootPrintView("live.ftl", id, getBoundaryPoints(id, false), getBeacons(id), getRoute(id));
	}

	@GET
	@Path("/{id}/canvas")
	public FootPrintView getCanvas(@PathParam("id") String id) {
		return new FootPrintView("canvas.ftl", id, getBoundaryPoints(id, true), getBeacons(id), getRoute(id));
	}

	protected List<Coordinate> getBeacons(String id) {
		List<Coordinate> beacons = new ArrayList<Coordinate>();
		RobotResource robotResource = new RobotResource(resourceData);
		List<Beacon> beaconList = robotResource.getBeacons(id);
		for (Beacon beacon : beaconList)
			beacons.add(beacon.getLocation());
		return beacons;
	}

	protected List<Coordinate> getRoute(String id) {
		List<Coordinate> route = new ArrayList<Coordinate>();
		RobotResource robotResource = new RobotResource(resourceData);
		Robot robot = robotResource.getRobot(id);
		List<Schedule> schedules = robotResource.getSchedules(robot);
		for (Schedule schedule : schedules)
			if (schedule.getType() == ScheduleType.ACTIVE.ordinal())
				route = schedule.getRoute();
		return route;
	}

	private List<Coordinate> getBoundaryPoints(String id, boolean isInternal) {
		List<Coordinate> boundaryPoints = new ArrayList<Coordinate>();
		List<Boundary> boundaries = new RobotResource(resourceData).getBoundaries(id);
		for (Boundary boundary : boundaries)
			if (isInternal)
				boundaryPoints = boundary.getBoundaryPoints();
			else
				boundaryPoints = boundary.getExternalBoundaryPoints();
		return boundaryPoints;
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/rawdata")
	public List<Footprint> getFootPrint(@PathParam("id") String id, @QueryParam("sort") int sort,
			@QueryParam("limit") int limit) {
		List<Footprint> footprints = new ArrayList<Footprint>();
		DBCursor<Footprint> dbCursor = resourceData.footprints.find().sort(new BasicDBObject("timestamp", sort))
				.limit(limit);
		while (dbCursor.hasNext())
			footprints.add(dbCursor.next());

		if (sort < 0)
			Collections.reverse(footprints);
		return footprints;
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/locations")
	public List<Coordinate> getFootPrint2(@PathParam("id") String id)
			throws TimeoutException, InterruptedException, MemcachedException {
		List<Coordinate> footprints = new ArrayList<Coordinate>();
		DBCursor<Footprint> dbCursor = resourceData.footprints.find().sort(new BasicDBObject("timestamp", 1));
		while (dbCursor.hasNext())
			footprints.add(InstructionService.getExternalLocation(id, resourceData, dbCursor.next().getLocation()));

		return footprints;
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/locations/current")
	public List<Coordinate> getCurrentFootPrint(@PathParam("id") String id)
			throws TimeoutException, InterruptedException, MemcachedException {
		List<Coordinate> footprints = new ArrayList<Coordinate>();
		DBCursor<Footprint> dbCursor = resourceData.footprints.find().sort(new BasicDBObject("timestamp", -1)).limit(1);
		while (dbCursor.hasNext())
			footprints.add(InstructionService.getExternalLocation(id, resourceData, dbCursor.next().getLocation()));

		return footprints;
	}

	@Produces(value = MediaType.APPLICATION_JSON)
	@GET
	@Path("/{id}/{index}")
	public List<Coordinate> getFootPrint(@PathParam("id") String id, @PathParam("index") int index)
			throws TimeoutException, InterruptedException, MemcachedException {
		List<Coordinate> footprints = new ArrayList<Coordinate>();
		Coordinate previousLocation = null;
		DBCursor<Footprint> dbCursor = resourceData.footprints.find().sort(new BasicDBObject("timestamp", 1));
		while (dbCursor.hasNext()) {
			Footprint footprint = dbCursor.next();
			Coordinate currentLocation = footprint.getLocation();
			if (footprints.isEmpty()) {
				footprints.add(InstructionService.getExternalLocation(id, resourceData, currentLocation));
			} else {
				if (previousLocation == null) {
					footprints.add(InstructionService.getExternalLocation(id, resourceData, currentLocation));
				}
				Coordinate nextLocation = getNextPoint(previousLocation, currentLocation,
						footprint.getNextInstruction().getDistance(), footprint.getNextInstruction().getDirection());
				footprints.add(InstructionService.getExternalLocation(id, resourceData, nextLocation));
				previousLocation = currentLocation;
			}
		}
		return footprints;
	}

	private Coordinate getNextPoint(Coordinate previousLocation, Coordinate currentLocation, double distance,
			double angle) {
		double x = currentLocation.x + distance * Math.sin(Math.toRadians(angle));
		double y = currentLocation.y + distance * Math.cos(Math.toRadians(angle));
		return new Coordinate(x, y);
	}
}
