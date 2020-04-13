package com.airobotics.robot.nxt;

import com.airobotics.robot.api.core.RobotProperty;

import lejos.nxt.MotorPort;

public class NxtTankMotor extends NxtMotor {
	private int motorId;

	public NxtTankMotor(MotorPort port) {
		super(port);
		this.motorId = port.getId();
	}

	public void rotate(double angle) {
		if (motorId == RobotProperty.MotorPort.mover.ordinal())
			moveForward(angle);
		else if (motorId == RobotProperty.MotorPort.direction.ordinal())
			changeDirection(angle);
		else
			super.rotate(angle);
	}

	public double getCurrentPosition() {
		if (motorId == RobotProperty.MotorPort.direction.ordinal())
			return 0;
		return super.getCurrentPosition();
	}

	private void moveForward(double angle) {
		RobotProperty.getInstance().directionMotor.rotate(angle, true);
		super.rotate(-angle);
	}

	private void changeDirection(double angle) {
		RobotProperty.getInstance().moverMotor.rotate(angle, true);
		super.rotate(angle);
	}
}
