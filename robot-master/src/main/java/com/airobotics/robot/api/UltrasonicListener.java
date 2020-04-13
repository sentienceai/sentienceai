package com.airobotics.robot.api;

public interface UltrasonicListener {
	public void onDetectedFar(int port, int level);
	public void onDetectedNear(int port, int level);
}
