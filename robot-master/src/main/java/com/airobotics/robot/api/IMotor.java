package com.airobotics.robot.api;

public interface IMotor {
	public double getCurrentPosition();
	public void forward();
	public void backward();
	public void stop();
	public void setSpeed(double speed);
	public void rotate(double angle);
	public void rotate(double angle, boolean immediateReturn);
	public boolean isMoving();
	public void resetTachoCount();
}
