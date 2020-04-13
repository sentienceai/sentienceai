package com.airobotics.robot.core;

import com.airobotics.api.entities.Instruction;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;

public class StopInstructionHandler extends ActiveInstructionHandler {
	public void process(Instruction instruction, RobotProperty robotProperty, RobotState robotState) {
		super.process(instruction, robotProperty, robotState);
		robotProperty.moverMotor.stop();
	}
}
