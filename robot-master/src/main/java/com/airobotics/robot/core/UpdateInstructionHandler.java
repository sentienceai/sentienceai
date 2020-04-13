package com.airobotics.robot.core;

import com.airobotics.api.entities.Instruction;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;

public class UpdateInstructionHandler extends ActiveInstructionHandler {
	public void process(Instruction instruction, RobotProperty robotProperty, RobotState robotState) {
		super.process(instruction, robotProperty, robotState);
		System.out.print(",spd:" + instruction.getSpeed());
		if (instruction.getSpeed() != 0) {
			robotProperty.directionMotor.setSpeed(instruction.getSpeed());
			robotProperty.moverMotor.setSpeed(instruction.getSpeed());
		}
		System.out.print("dir:" + instruction.getDirection());
		if (instruction.getDirection() != 0)
			robotProperty.directionMotor.rotate(instruction.getDirection());
		System.out.print(",dis:" + instruction.getDistance());
		if (instruction.getDistance() != 0)
			robotProperty.moverMotor.rotate(instruction.getDistance());
		System.out.println(",done");
	}
}
