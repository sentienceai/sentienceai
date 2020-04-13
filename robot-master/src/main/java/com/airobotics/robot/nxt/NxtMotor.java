package com.airobotics.robot.nxt;

import com.airobotics.robot.api.IMotor;

import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.TachoMotorPort;

public class NxtMotor extends NXTRegulatedMotor implements IMotor {
	public NxtMotor(TachoMotorPort port) {
		super(port);
	}

	public void setSpeed(double speed) {
		super.setSpeed((int)speed);
	}

	public void rotate(double angle) {
		super.rotate((int)angle);
	}

	public void rotate(double angle, boolean immediateReturn) {
		super.rotate((int)angle, immediateReturn);
	}

	public double getCurrentPosition() {
		return super.getPosition();
	}
}
