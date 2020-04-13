package com.airobotics.commandcenter.resources;

import org.junit.Test;

public class RobotRouteWithOffTrackTest extends RobotTest {
	@Test
	public void runRobot_withRouteWithOffTrack_iterateAllRoutePoints() {
		routePoints = getRouteWithOffTrack();
		robot.init();
		robot.run();
	}
}
