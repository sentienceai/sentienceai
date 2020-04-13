package com.airobotics.commandcenter.resources;

import com.airobotics.core.geom.Circle;
import com.airobotics.core.geom.CircleCannotBeFormedWithLineException;
import com.airobotics.core.geom.Triangle;
import com.airobotics.core.util.AngleUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;

public class InstructionResourceCurrentLocationTest extends ResourceTest {
	private static int skippedCntWithCircleCannotBeFormedWithLineException;

	private void withTestBeaconsSet(int beaconSetIndex, int toleranceDistanceBetweenActualPointAndCalculatedPoint) {
		threeBeacons = threeBeaconsSet.get(beaconSetIndex);
		printDebugMessage(beaconSetIndex);
		
		if (Triangle.isValid(threeBeacons[0].getLocation(), threeBeacons[1].getLocation(), threeBeacons[2].getLocation()))
			super.withMiliMeterScale(toleranceDistanceBetweenActualPointAndCalculatedPoint);
		else {
			skippedCntWithInvalidTriangle++;
			printSkippedTest(null);
		}
	}

	private void printDebugMessage(int beaconSetIndex) {
		System.out.println("withTestBeaconsSet" + beaconSetIndex);
		System.out.println("tip1: " + threeBeacons[0].getLocation());
		System.out.println("tip2: " + threeBeacons[1].getLocation());
		System.out.println("tip3: " + threeBeacons[2].getLocation());
		System.out.println("tip1: " + AngleUtil.getAngleBetween(threeBeacons[2].getLocation(),
				threeBeacons[0].getLocation(), threeBeacons[1].getLocation()));
		System.out.println("tip2: " + AngleUtil.getAngleBetween(threeBeacons[0].getLocation(),
				threeBeacons[1].getLocation(), threeBeacons[2].getLocation()));
		System.out.println("tip3: " + AngleUtil.getAngleBetween(threeBeacons[1].getLocation(),
				threeBeacons[2].getLocation(), threeBeacons[0].getLocation()));
	}

	@Override
	protected void withTriangleSpecialPoints_insideToleranceDistanceFromActualPoint(double toleranceDistance) {
		withCentroidPoint_insideToleranceDistanceFromActualPoint(toleranceDistance);
		withCircumcentrePoint_insideToleranceDistanceFromActualPoint(toleranceDistance);
		withInCentrePoint_insideToleranceDistanceFromActualPoint(toleranceDistance);
		withAngleBisectorPoint_insideToleranceDistanceFromActualPoint(toleranceDistance);
		withCloseToTips_insideToleranceDistanceFromActualPoint(toleranceDistance);
		try {
			withMidpoints_insideToleranceDistanceFromActualPoint(toleranceDistance);
		} catch (CircleCannotBeFormedWithLineException e) {
			skippedCntWithCircleCannotBeFormedWithLineException++;
			throw e;
		}
	}

	@Override
	protected void withCloseToTips_insideToleranceDistanceFromActualPoint(double toleranceDistance) {
		toleranceDistance = getToleranceDistanceForCloseToTips(toleranceDistance);
		Coordinate actualPoint = getPointCloseToTip(tip1);
		assertCaculatedPointInsideToleranceDistanceFromActualPoint(actualPoint, toleranceDistance);
		actualPoint = getPointCloseToTip(tip2);
		assertCaculatedPointInsideToleranceDistanceFromActualPoint(actualPoint, toleranceDistance);
		actualPoint = getPointCloseToTip(tip3);
		assertCaculatedPointInsideToleranceDistanceFromActualPoint(actualPoint, toleranceDistance);
	}

	private Coordinate getPointCloseToTip(Coordinate tip) {
		Coordinate centroid = Triangle.centroid(tip1, tip2, tip3);
		LineSegment lineFromTipToCentroid = new LineSegment(tip, centroid);
		double segmentLengthFractionOf10mmFromTip = 10 / tip.distance(centroid);
		return lineFromTipToCentroid.pointAlong(segmentLengthFractionOf10mmFromTip);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		assertTrue("skippedCntWithCentroidPoint should be 0 but "+skippedCntWithCentroidPoint, skippedCntWithCentroidPoint == 0);
		assertTrue("skippedCntWithCircumcentrePoint should be 36 but "+skippedCntWithCircumcentrePoint, skippedCntWithCircumcentrePoint == 36);
		assertTrue("skippedCntWithInCentrePoint should be 0 but "+skippedCntWithInCentrePoint, skippedCntWithInCentrePoint == 0);
		assertTrue("skippedCntWithAngleBisectorPoint should be 26 but "+skippedCntWithAngleBisectorPoint, skippedCntWithAngleBisectorPoint == 26);
		assertTrue("skippedCntWithInvalidTriangle should be 4 but "+skippedCntWithInvalidTriangle, skippedCntWithInvalidTriangle == 4);
		assertTrue("skippedCntWithCircleCannotBeFormedWithLineException should be 12 but "+skippedCntWithCircleCannotBeFormedWithLineException, skippedCntWithCircleCannotBeFormedWithLineException == 12);
	}

	@Test
	public void withTestBeaconsSet0() {
		withTestBeaconsSet(0, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet1() {
		withTestBeaconsSet(1, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet2() {
		withTestBeaconsSet(2, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet3() {
		withTestBeaconsSet(3, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet4() {
		withTestBeaconsSet(4, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet5() {
		withTestBeaconsSet(5, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet6() {
		withTestBeaconsSet(6, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet7() {
		withTestBeaconsSet(7, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet8() {
		withTestBeaconsSet(8, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet9() {
		withTestBeaconsSet(9, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet10() {
		withTestBeaconsSet(10, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet11() {
		withTestBeaconsSet(11, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet12() {
		withTestBeaconsSet(12, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet13() {
		withTestBeaconsSet(13, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet14() {
		withTestBeaconsSet(14, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet15() {
		withTestBeaconsSet(15, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet16() {
		withTestBeaconsSet(16, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet17() {
		withTestBeaconsSet(17, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet18() {
		withTestBeaconsSet(18, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet19() {
		withTestBeaconsSet(19, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet20() {
		withTestBeaconsSet(20, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet21() {
		withTestBeaconsSet(21, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet22() {
		withTestBeaconsSet(22, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet23() {
		withTestBeaconsSet(23, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet24() {
		withTestBeaconsSet(24, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet25() {
		withTestBeaconsSet(25, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet26() {
		withTestBeaconsSet(26, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet27() {
		withTestBeaconsSet(27, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet28() {
		withTestBeaconsSet(28, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet29() {
		withTestBeaconsSet(29, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet30() {
		withTestBeaconsSet(30, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet31() {
		withTestBeaconsSet(31, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet32() {
		withTestBeaconsSet(32, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet33() {
		withTestBeaconsSet(33, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet34() {
		withTestBeaconsSet(34, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet35() {
		withTestBeaconsSet(35, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet36() {
		withTestBeaconsSet(36, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet37() {
		withTestBeaconsSet(37, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet38() {
		withTestBeaconsSet(38, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet39() {
		withTestBeaconsSet(39, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet40() {
		withTestBeaconsSet(40, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet41() {
		withTestBeaconsSet(41, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet42() {
		withTestBeaconsSet(42, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet43() {
		withTestBeaconsSet(43, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet44() {
		withTestBeaconsSet(44, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet45() {
		withTestBeaconsSet(45, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet46() {
		withTestBeaconsSet(46, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet47() {
		withTestBeaconsSet(47, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet48() {
		withTestBeaconsSet(48, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet49() {
		withTestBeaconsSet(49, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet50() {
		withTestBeaconsSet(50, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test(expected = CircleCannotBeFormedWithLineException.class)
	public void withTestBeaconsSet51() {
		withTestBeaconsSet(51, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet52() {
		withTestBeaconsSet(52, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet53() {
		withTestBeaconsSet(53, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet54() {
		withTestBeaconsSet(54, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}

	@Test
	public void withTestBeaconsSet55() {
		withTestBeaconsSet(55, Circle.maxToleranceDistanceBetweenCircleIntersectionsInMiliMeter);
	}
}