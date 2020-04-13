package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.ScheduleType;
import com.airobotics.api.entities.User;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.core.util.AngleUtil;
import com.airobotics.core.util.CoordinateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

@SuppressWarnings("serial")
public class ResourceTestUtil {
	static {
		initTestSchedule();
	}
	public static int port = 8080;
	public static Client client;

	public static final int coverageDiameter = 100;
	public static User testUser = new User("feel@alumni.cmu.edu", "test", "Phil", "kim");
	public static Robot testRobot = new Robot("1", 30, coverageDiameter);
	public static Boundary testBoundary = new Boundary();
	public static Schedule testSchedule;
	private static void initTestSchedule() {
		testSchedule = new Schedule();
		testSchedule.setType(ScheduleType.ACTIVE.ordinal());
	}
	// 0.00001 1.11 m
	// 0.0000001 1.11 cm
	// 0.00000001 1.11 mm
	public static List<Beacon> testBeacons = new ArrayList<Beacon>() {
		{
			add(new Beacon("1", new Coordinate(-71.35262500, 42.32382250)));
			add(new Beacon("2", new Coordinate(-71.35257250, 42.32380000)));
			add(new Beacon("3", new Coordinate(-71.35263000, 42.32378250)));
			add(new Beacon("4", new Coordinate(-71.35268250, 42.32380500)));
			add(new Beacon("5", new Coordinate(-71.35268000, 42.32382500)));
			add(new Beacon("6", new Coordinate(-71.35257000, 42.32382000)));
			add(new Beacon("7", new Coordinate(-71.35257500, 42.32378000)));
			add(new Beacon("8", new Coordinate(-71.35268500, 42.32378500)));
		}
	};
	public static List<Coordinate> testBoundaryPoints = new ArrayList<Coordinate>() {
		{
			add(new Coordinate(-71.35268000 + (double) coverageDiameter / CoordinateUtil.coordinatePrecision,
					42.32382500 - (double) coverageDiameter / CoordinateUtil.coordinatePrecision)); // NW
			add(new Coordinate(-71.35257000 - (double) coverageDiameter / CoordinateUtil.coordinatePrecision,
					42.32382000 - (double) coverageDiameter / CoordinateUtil.coordinatePrecision)); // NE
			add(new Coordinate(-71.35257500 - (double) coverageDiameter / CoordinateUtil.coordinatePrecision,
					42.32378000 + (double) coverageDiameter / CoordinateUtil.coordinatePrecision)); // SE
			add(new Coordinate(-71.35268500 + (double) coverageDiameter / CoordinateUtil.coordinatePrecision,
					42.32378500 + (double) coverageDiameter / CoordinateUtil.coordinatePrecision)); // SW
		}
	};

	public static String getResourceUri(String resource) {
		return "http://localhost:" + port + "/api/v1/" + resource;
	}

	public static String getAppResourceUri(String resource) {
		return "http://localhost:" + port + "/app/v1/" + resource;
	}

	public static String getRobotResourceUri(Robot testRobot, String resource) {
		return (resource == null || resource.isEmpty()) ? "robots/" + testRobot.getId()
				: "robots/" + testRobot.getId() + "/" + resource;
	}

	public static Response getResourcePostResponse(String resource, Object entity) {
		return client.target(getResourceUri(resource)).request().post(Entity.json(entity));
	}

	public static Response getResourcePutResponse(String resource, Object entity) {
		return client.target(getResourceUri(resource)).request().put(Entity.json(entity));
	}

	public static Response getResourceDeleteResponse(String resource) {
		return client.target(getResourceUri(resource)).request().delete();
	}

	public static Response getResourceGetResponse(String resource, String paramName, String paramValue) {
		return client.target(getResourceUri(resource)).queryParam(paramName, paramValue).request().get();
	}

	public static Response getAppResourceGetResponse(String resource, String paramName, String paramValue) {
		return client.target(getAppResourceUri(resource)).queryParam(paramName, paramValue).request().get();
	}

	public static List<Beacon> getBeaconsOfTestRobot(Robot testRobot) {
		Response response = getResourceGetResponse(getRobotResourceUri(testRobot, "beacons"), "", "");
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
		List<Beacon> beaconsOfRobot = response.readEntity(new GenericType<List<Beacon>>() {
		});
		return beaconsOfRobot;
	}

	public static String getJsonEntity(Object entity) throws JsonProcessingException, UnsupportedEncodingException {
		String json = new ObjectMapper().writeValueAsString(entity);
		return URLEncoder.encode(json, StandardCharsets.UTF_8.toString());
	}

	public static void sort(List<Beacon> beacons) {
		CoordinateList coordinates = new CoordinateList();
		for (Beacon beacon : beacons)
			coordinates.add(beacon.getLocation(), true);
		coordinates.closeRing();
		Envelope envelope = new GeometryFactory().createLineString(coordinates.toCoordinateArray())
				.getEnvelopeInternal();
		sort(beacons, envelope.centre());
	}

	public static void sort(List<Beacon> beacons, final Coordinate pointToCompare) {
		for (int i = 0; i < beacons.size() - 1; i++) {
			double minAngle = Double.MAX_VALUE;
			int minIdx = 0;
			for (int j = i + 1; j < beacons.size(); j++) {
				double angle = AngleUtil.getAngleBetweenOriented(beacons.get(i).getLocation(), pointToCompare,
						beacons.get(j).getLocation());
				if (minAngle > angle) {
					minAngle = angle;
					minIdx = j;
				}
			}
			if (minIdx != 0) {
				Beacon tmp = beacons.get(i + 1);
				beacons.set(i + 1, beacons.get(minIdx));
				beacons.set(minIdx, tmp);
			}
		}
	}

	public static void sort(BeaconLoc[] beaconLocs, Coordinate pointToCompare) {
		Map<String, BeaconLoc> beaconLocMap = new Hashtable<String, BeaconLoc>();
		List<Beacon> beacons = toBeacons(beaconLocs, beaconLocMap);
		sort(beacons, pointToCompare);
		for (int i = 0; i < beacons.size(); i++)
			beaconLocs[i] = beaconLocMap.get(beacons.get(i).getSerialNumber());
	}

	private static List<Beacon> toBeacons(BeaconLoc[] beaconLocs, Map<String, BeaconLoc> beaconLocMap) {
		List<Beacon> beacons = new ArrayList<Beacon>();
		for (BeaconLoc beaconLoc : beaconLocs) {
			beaconLocMap.put(beaconLoc.getId(), beaconLoc);
			Beacon beacon = new Beacon();
			beacon.setSerialNumber(beaconLoc.getId());
			beacon.setLocation(beaconLoc.getLocation());
			beacons.add(beacon);
		}
		return beacons;
	}
}
