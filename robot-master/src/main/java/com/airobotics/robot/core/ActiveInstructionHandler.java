package com.airobotics.robot.core;

import com.airobotics.api.entities.Instruction;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;

public class ActiveInstructionHandler extends DefaultInstructionHandler {
	public void process(Instruction instruction, RobotProperty robotProperty, RobotState robotState) {
		super.process(instruction, robotProperty, robotState);
		robotState.isActive = true;
	}
}