package com.airobotics.robot.core;

import com.airobotics.api.entities.Instruction;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;

public class DefaultInstructionHandler implements IInstructionHandler {
	public void process(Instruction instruction, RobotProperty robotProperty, RobotState robotState) {
		if (instruction.getRouteIdx() >= 0)
			robotState.routeIdx = instruction.getRouteIdx();
		robotState.intervalToGetNextInstrunction = instruction.getInterval();
	}
}
