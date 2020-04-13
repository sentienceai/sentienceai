package com.airobotics.robot.api;

public interface RFIDListener {
	public void onRfidDetected(String serialNo, double angle);
}
