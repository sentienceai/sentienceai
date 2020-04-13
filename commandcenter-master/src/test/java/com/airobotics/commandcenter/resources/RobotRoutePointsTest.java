package com.airobotics.commandcenter.resources;

import org.junit.Test;

public class RobotRoutePointsTest extends RobotTest {
	@Test
	public void runRobot_withRoutePoints_iterateAllRoutePoints() {
		routePoints = route;
		robot.init();
		robot.run();
	}
}
