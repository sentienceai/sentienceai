package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.airobotics.api.entities.Footprint;
import com.airobotics.api.entities.Instruction;
import com.airobotics.core.geom.Triangle;
import com.vividsolutions.jts.geom.Coordinate;

public class FootprintResourceTest extends ResourceTest {
	private static double degPerDistance;
	private static List<Coordinate> route;
	private static List<Footprint> footprints;

	@BeforeClass
	public static void setUpClass() throws Exception {
		degPerDistance = 360 / (Math.PI * ResourceTestUtil.testRobot.getWheelDiameter());
		loadSchedule();
		route = ResourceTestUtil.testSchedule.getRoute();
		footprints = getFootPrints();
	}

	private static List<Footprint> getFootPrints() {
		Response response = ResourceTestUtil
				.getAppResourceGetResponse("footprints/" + ResourceTestUtil.testRobot.getId() + "/rawdata", "", "");
		return response.readEntity(new GenericType<List<Footprint>>() {
		});
	}

	@Test
	// Can pass only after running db.footprints.drop(); and
	// RobotTest.runRobot_withRoutePoints_iterateAllRoutePoints();
	public void withRoutePointsOnly_footprintsAreSameWithRoute() {
		for (int i = 0; i < footprints.size(); i++) {
			Footprint footprint = footprints.get(i);
			Coordinate footprintPoint = footprint.getLocation();
			Coordinate routePoint = route.get(i);
			assertTrue(routePoint.distance(footprintPoint) <= ResourceTestUtil.coverageDiameter);
		}
	}

	@Ignore
	@Test
	// Can pass only after running db.footprints.drop(); and
	// RobotTest.runRobot_withRoutePoints_iterateAllRoutePoints();
	public void withRoutePointsOnly_currentLocationIsSameWithExecutingPreviousInstruction() {
		for (int i = 2; i < footprints.size(); i++) {
			Footprint currentFootprint = footprints.get(i - 1);
			Coordinate previousLocation = footprints.get(i - 2).getLocation();
			Coordinate currentLocation = currentFootprint.getLocation();
			Coordinate nextLocation = footprints.get(i).getLocation();
			if (previousLocation.equals2D(currentLocation) || currentLocation.equals2D(nextLocation))
				continue;
			
			Instruction nextInstuction = currentFootprint.getNextInstruction();
			Coordinate nextLocationByAngleAndDistance = getNextLocationByAngleAndDistance(previousLocation,
					currentLocation, nextInstuction.getDistance() / degPerDistance, nextInstuction.getDirection(),
					nextLocation);
			assertTrue(nextLocation.distance(nextLocationByAngleAndDistance) <= ResourceTestUtil.coverageDiameter);
		}
	}

	private Coordinate getNextLocationByAngleAndDistance(Coordinate previousLocation, Coordinate currentLocation,
			double distance, double angle, Coordinate nextLocation) {
		Triangle triangle = new Triangle(previousLocation, currentLocation, nextLocation);
		Triangle triangle1 = Triangle.getTriangle(previousLocation, currentLocation, distance, angle);
		triangle.getLegP2();
		triangle1.getLegP2();
		return triangle1.p2;
	}
}
