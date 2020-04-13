package com.airobotics.robot.api;

import java.util.List;

public interface IRFIDSensor {
	public void addRFIDListener(RFIDListener rfidListener);

	public void activate();

	public void deactivate();

	public List<IRFIDSensorData> readRfid();
	
	public void reset();
	
	public void setAngle(double angle);
}
