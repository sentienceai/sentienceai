package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.commandcenter.CommandCenterApplication;
import com.airobotics.commandcenter.CommandCenterConfiguration;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.commandcenter.core.AngleOutsideBeaconsRangeException;
import com.airobotics.commandcenter.core.BeaconService;
import com.airobotics.core.GeometryUtilTest;
import com.airobotics.core.geom.CircleCannotBeFormedWithLineException;
import com.airobotics.core.geom.CirclesIntersectedOutsideMaxToleranceDistanceException;
import com.airobotics.core.geom.CirclesNotIntersectedException;
import com.airobotics.core.geom.Triangle;
import com.airobotics.core.util.AngleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vividsolutions.jts.geom.Coordinate;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public abstract class ResourceTest extends GeometryUtilTest {
	protected static List<BeaconLoc[]> threeBeaconsSet;
	protected BeaconLoc[] threeBeacons;
	protected static int skippedCntWithInvalidTriangle;
	protected static int skippedCntOutsideTriangle;
	
	@ClassRule
	public static final DropwizardAppRule<CommandCenterConfiguration> RULE = new DropwizardAppRule<CommandCenterConfiguration>(CommandCenterApplication.class, ResourceHelpers.resourceFilePath("airobotics.yml"));

	@BeforeClass
	public static void setUpResourceClass() throws Exception {
		ResourceTestUtil.port = RULE.getLocalPort();
		ResourceTestUtil.client = ClientBuilder.newClient();
	
		loadBeacons();
	}

	public static void loadBeacons() {
		ResourceTestUtil.testRobot = ResourceTestUtil.getResourceGetResponse("robots", "serialNo",
				ResourceTestUtil.testRobot.getSerialNumber()).readEntity(Robot.class);
		List<Beacon> beacons = ResourceTestUtil.getBeaconsOfTestRobot(ResourceTestUtil.testRobot);
		threeBeaconsSet = BeaconService.getBeaconsSetFromBeacons(beacons);
	}

	public static void loadSchedule() {
		Response response = ResourceTestUtil.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "schedules"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Schedule> schedulesWithId = response.readEntity(new GenericType<List<Schedule>>(){});
		for (Schedule scheduleWithId : schedulesWithId) {
			response = ResourceTestUtil.getResourceGetResponse("schedules/" + scheduleWithId.getId(), "", "");
			assertTrue(response.getStatus() == Status.OK.getStatusCode());
			ResourceTestUtil.testSchedule = response.readEntity(Schedule.class);
		}
	}

	public static void loadBoundary() {
		Response response = ResourceTestUtil.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "boundaries"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Boundary> boundariesWithId = response.readEntity(new GenericType<List<Boundary>>(){});
		for (Boundary boundaryWithId : boundariesWithId) {
			response = ResourceTestUtil.getResourceGetResponse("boundaries/" + boundaryWithId.getId(), "", "");
			assertTrue(response.getStatus() == Status.OK.getStatusCode());
			ResourceTestUtil.testBoundary = response.readEntity(Boundary.class);
		}
	}

	@Before
	public void setUpResource() throws Exception {
		ResourceTestUtil.client = ClientBuilder.newClient();
	}

	@After
	public void tearDownResource() throws Exception {
		ResourceTestUtil.client.close();
	}
	
	@Override
	protected void setTip2(int scale) {
		tip1 = threeBeacons[0].getLocation();
		tip2 = threeBeacons[1].getLocation();
		tip3 = threeBeacons[2].getLocation();
	}

	@Override
	protected Coordinate getCalculatedPoint(Coordinate expectedPoint) {
		updateAngleOfThreeBeacons(expectedPoint);
		Response response = getResponse();
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		calculatedPoint = response.readEntity(Coordinate.class);
		populateDistanceAnglesMap(expectedPoint);
		
		return calculatedPoint;
	}

	public Response getResponse() {
		Response response = null;
		try {
			response = ResourceTestUtil.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(ResourceTestUtil.testRobot, "instructions/current_location"),
					"threeBeaconsWithAngle", ResourceTestUtil.getJsonEntity(threeBeacons));
			if (response.getStatus() == Status.BAD_REQUEST.getStatusCode())
				checkErrorMessage(response);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void checkErrorMessage(Response response) {
		String errMsg = response.readEntity(String.class);
		if (errMsg.contains("CircleCannotBeFormedWithLineException"))
			throw new CircleCannotBeFormedWithLineException(errMsg);
		if (errMsg.contains("CirclesNotIntersectedException"))
			throw new CirclesNotIntersectedException();
		if (errMsg.contains("CirclesIntersectedOutsideMaxToleranceDistanceException"))
			throw new CirclesIntersectedOutsideMaxToleranceDistanceException();
		if (errMsg.contains("AngleOutsideBeaconsRangeException"))
			throw new AngleOutsideBeaconsRangeException();
	}

	private void updateAngleOfThreeBeacons(Coordinate expectedPoint) {
		double angle0 = AngleUtil.getAngleBetween(threeBeacons[2].getLocation(), expectedPoint, threeBeacons[0].getLocation());
		double angle1 = AngleUtil.getAngleBetween(threeBeacons[0].getLocation(), expectedPoint, threeBeacons[1].getLocation());
		double angle2 = AngleUtil.getAngleBetween(threeBeacons[1].getLocation(), expectedPoint, threeBeacons[2].getLocation());
		threeBeacons[0].setAngle(angle0);
		threeBeacons[1].setAngle(angle0 + angle1);
		threeBeacons[2].setAngle(angle0 + angle1 + angle2);
		printDebugMessage(angle0, angle1, angle2);
	}

	private void printDebugMessage(double angle0, double angle1, double angle2) {
		System.out.println("angle0: " + angle0);
		System.out.println("angle1: " + angle1);
		System.out.println("angle2: " + angle2);
		System.out.println("angle0 + angle1: " + (angle0 + angle1));
		System.out.println("angle0 + angle1 + angle2: " + (angle0 + angle1 + angle2));
	}

	protected boolean isBeaconLocUsable(BeaconLoc[] beaconLoc, Coordinate actualPoint) {
		Triangle beaconTriangle = new Triangle(beaconLoc[0].getLocation(), beaconLoc[1].getLocation(), beaconLoc[2].getLocation());
		if (beaconTriangle.isValid()) {
			if (beaconTriangle.isInside(actualPoint))
				return true;
			else {
				skippedCntOutsideTriangle++;
				printSkippedTest(actualPoint);
			}
		} else {
			skippedCntWithInvalidTriangle++;
			printSkippedTest(null);
		}
		return false;
	}
}