package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.User;
import com.airobotics.commandcenter.CommandCenterApplication;
import com.airobotics.commandcenter.CommandCenterConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class RobotResourceTest {
	@ClassRule
	public static final DropwizardAppRule<CommandCenterConfiguration> RULE = new DropwizardAppRule<CommandCenterConfiguration>(
			CommandCenterApplication.class, ResourceHelpers.resourceFilePath("airobotics.yml"));
	protected static User testUser = ResourceTestUtil.testUser;
	protected static Robot testRobot = ResourceTestUtil.testRobot;
	protected static List<Beacon> testBeacons = ResourceTestUtil.testBeacons;
	protected static Schedule testSchedule = ResourceTestUtil.testSchedule;
	protected static Boundary testBoundary = ResourceTestUtil.testBoundary;
	protected static List<Coordinate> testBoundaryPoints = ResourceTestUtil.testBoundaryPoints;

	@BeforeClass
	public static void setUpClass() throws Exception {
		ResourceTestUtil.port = RULE.getLocalPort();
		ResourceTestUtil.client = ClientBuilder.newClient();
		setUpTestUser();
		setUpTestRobot();
		setUpTestBeacons();
		setUpTestSchedule();
		setUpTestBoundaries();
		updateInternalLocations();
	}

	@Before
	public void setUp() throws Exception {
		ResourceTestUtil.client = ClientBuilder.newClient();
	}

	protected static void setUpTestUser() {
		Response response = ResourceTestUtil.getResourceGetResponse("users", "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<User> users = response.readEntity(new GenericType<List<User>>() {
		});
		setTestUserWithId(response, users);
	}

	private static void setTestUserWithId(Response response, List<User> users) {
		if (!hasTestUser(users)) {
			response = ResourceTestUtil.getResourcePostResponse("users", testUser);
			assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
			testUser = response.readEntity(User.class);
		} else
			for (User user : users)
				testUser = user;
	}

	private static boolean hasTestUser(List<User> users) {
		for (User user : users)
			if (user.getUserName().equals(testUser.getUserName()))
				return true;
		return false;
	}

	protected static void setUpTestRobot() {
		Response response = ResourceTestUtil.getResourceGetResponse("robots", "serialNo", testRobot.getSerialNumber());
		if (response.getStatus() == Status.OK.getStatusCode())
			testRobot = response.readEntity(Robot.class);
		else
			addRobotToUser();
	}

	private static void addRobotToUser() {
		Response response = ResourceTestUtil.getResourcePostResponse("robots", testRobot);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
		testRobot = response.readEntity(Robot.class);
		response = ResourceTestUtil.getResourcePostResponse(
				"users/" + testRobot.getId() + "/" + ResourceTestUtil.getRobotResourceUri(testRobot, ""), testRobot);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
	}

	protected static void setUpTestBeacons() throws UnsupportedEncodingException, JsonProcessingException {
		List<Beacon> beaconsWithId = getBeaconsWithId();

		if (testBeacons.size() == beaconsWithId.size())
			testBeacons = beaconsWithId;

		addBeaconsToRobot();
	}

	private static List<Beacon> getBeaconsWithId() {
		List<Beacon> beacons = new ArrayList<Beacon>();
		for (Beacon testBeacon : testBeacons) {
			Response response = ResourceTestUtil.getResourceGetResponse("beacons", "serialNo",
					testBeacon.getSerialNumber());
			if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
				response = ResourceTestUtil.getResourcePostResponse("beacons", testBeacon);
				assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
			}
			beacons.add(response.readEntity(Beacon.class));
		}
		return beacons;
	}

	private static void addBeaconsToRobot() throws UnsupportedEncodingException, JsonProcessingException {
		List<Beacon> beaconsOfRobot = ResourceTestUtil.getBeaconsOfTestRobot(testRobot);

		if (testBeacons.size() != beaconsOfRobot.size())
			addBeaconIdsToRobot();
	}

	private static void addBeaconIdsToRobot() throws UnsupportedEncodingException, JsonProcessingException {
		List<String> beaconIds = new ArrayList<String>();
		for (Beacon testBeacon : testBeacons)
			beaconIds.add(testBeacon.getId());
		Response response = ResourceTestUtil.getResourcePostResponse(
				ResourceTestUtil.getRobotResourceUri(testRobot, "beacons/") + getBeaconIdsJson(beaconIds), testBeacons);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
	}

	private static String getBeaconIdsJson(List<String> beaconIds)
			throws UnsupportedEncodingException, JsonProcessingException {
		return URLEncoder.encode(new ObjectMapper().writeValueAsString(beaconIds), StandardCharsets.UTF_8.toString());
	}

	protected static void setUpTestSchedule() {
		Response response = ResourceTestUtil
				.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(testRobot, "schedules"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Schedule> schedulesWithId = response.readEntity(new GenericType<List<Schedule>>() {
		});
		setTestScheduleWithId(schedulesWithId);
	}

	private static void setTestScheduleWithId(List<Schedule> schedulesWithId) {
		if (schedulesWithId.isEmpty())
			addScheduleToRobot();
		else
			for (Schedule scheduleWithId : schedulesWithId)
				testSchedule = scheduleWithId;
	}

	private static void addScheduleToRobot() {
		Response response = ResourceTestUtil.getResourcePostResponse("schedules", testSchedule);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
		testSchedule = response.readEntity(Schedule.class);
		response = ResourceTestUtil.getResourcePostResponse(
				ResourceTestUtil.getRobotResourceUri(testRobot, "schedules/") + testSchedule.getId(), testSchedule);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
	}

	protected static void setUpTestBoundaries() {
		Response response = ResourceTestUtil
				.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(testRobot, "boundaries"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Boundary> boundariesWithIds = response.readEntity(new GenericType<List<Boundary>>() {
		});
		setBoundaryWithId(boundariesWithIds);
	}

	private static void setBoundaryWithId(List<Boundary> boundariesWithIds) {
		if (boundariesWithIds.isEmpty())
			addBoundaryToRobot();
		else
			for (Boundary boundaryWithId : boundariesWithIds)
				testBoundary = boundaryWithId;
	}

	private static void addBoundaryToRobot() {
		testBoundary.setExternalBoundaryPoints(testBoundaryPoints);
		Response response = ResourceTestUtil.getResourcePostResponse("boundaries", testBoundary);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
		testBoundary = response.readEntity(Boundary.class);
		response = ResourceTestUtil
				.getResourcePostResponse(ResourceTestUtil.getRobotResourceUri(testRobot, "schedules/")
						+ testSchedule.getId() + "/boundary/" + testBoundary.getId(), testBoundary);
		assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
	}

	protected static void updateInternalLocations() {
		Response response = ResourceTestUtil.getResourcePutResponse(
				ResourceTestUtil.getRobotResourceUri(testRobot, "internal_locations"), "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
	}

	@After
	public void tearDown() throws Exception {
		ResourceTestUtil.client.close();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
//		ResourceTestUtil.client = ClientBuilder.newClient();
//		Response response = ResourceTestUtil.getResourceDeleteResponse("users/" + testUser.getId(), testUser);
//		assertTrue(response.getStatus() == Status.OK.getStatusCode());
//		ResourceTestUtil.client.close();
	}

	@Test
	public void withSerialNumber_getSameRobot() {
		Robot robot = ResourceTestUtil.getResourceGetResponse("robots", "serialNo", testRobot.getSerialNumber())
				.readEntity(Robot.class);
		assertTrue(robot.getWheelDiameter() == robot.getWheelDiameter());

		Response res = ResourceTestUtil.getResourceGetResponse("robots", "serialNo", "2");
		assertTrue(res.getStatus() == Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void afterUpdatingInternalLocations_NoNegativeBoundaryPoints() {
		Response response = ResourceTestUtil
				.getResourceGetResponse(ResourceTestUtil.getRobotResourceUri(testRobot, "boundaries"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Boundary> boundaries = response.readEntity(new GenericType<List<Boundary>>() {
		});
		for (Boundary boundary : boundaries) {
			assertTrue(boundary.getBoundaryPoints().size() == testBoundaryPoints.size());
			for (Coordinate coordinate : boundary.getBoundaryPoints())
				assertTrue(coordinate.x >= 0 && coordinate.y >= 0);
		}
	}

	@Test
	public void afterUpdatingInternalLocations_NoNegativeBeaconInternalLocations() {
		List<Beacon> beacons = ResourceTestUtil.getBeaconsOfTestRobot(testRobot);
		assertTrue(beacons.size() == testBeacons.size());
		for (Beacon beacon : beacons)
			assertTrue(beacon.getLocation().x >= 0 && beacon.getLocation().y >= 0);
	}

}
