package com.airobotics.commandcenter.resources;

import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.core.geom.Circle;
import com.vividsolutions.jts.geom.Coordinate;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TreeMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScheduleResourceTest extends ResourceTest {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		loadSchedule();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		assertTrue("skippedCntWithInvalidTriangle should be 352 but "+skippedCntWithInvalidTriangle, skippedCntWithInvalidTriangle == 352);
		assertTrue("skippedCntOutsideTriangle should be 3684 but "+skippedCntOutsideTriangle, skippedCntOutsideTriangle == 3684);
	}

	@Test
	public void withAllRoutePoints_insideToleranceDistance() {
		List<Coordinate> route = ResourceTestUtil.testSchedule.getRoute();
		for (Coordinate actualPoint : route) {
			threeBeacons = null;
			evaluateWithEachThreeeBeaconsSet(actualPoint, new TreeMap<Integer, String>());
			assertTrue("Each route point should be evaluated: " + actualPoint, threeBeacons != null);
		}
	}

	private void evaluateWithEachThreeeBeaconsSet(Coordinate actualPoint, TreeMap<Integer, String> distanceAnglesMap) {
		for (BeaconLoc[] beaconLoc : threeBeaconsSet) {
			if (isBeaconLocUsable(beaconLoc, actualPoint)) {
				threeBeacons = beaconLoc;
				setTipsWithScale(1);
				assertCaculatedPointInsideToleranceDistanceFromActualPoint(actualPoint, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
			}
		}
	}
}