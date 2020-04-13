package com.airobotics.commandcenter.resources;

import org.junit.Test;

public class RobotRouteWithMidPointTest extends RobotTest {
	@Test
	public void runRobot_withRouteWithMidPoint_iterateAllRoutePoints() {
		routePoints = getRouteWithMidPoint();
		robot.init();
		robot.run();
	}
}
