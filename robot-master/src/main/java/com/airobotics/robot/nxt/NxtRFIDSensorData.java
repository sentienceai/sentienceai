package com.airobotics.robot.nxt;

import com.airobotics.robot.api.IRFIDSensorData;

public class NxtRFIDSensorData implements IRFIDSensorData {
	private String rfid;
	private double angle;
	private long distance;

	public NxtRFIDSensorData(String rfid, double angle) {
		this.rfid = rfid;
		this.angle = angle;
	}

	public NxtRFIDSensorData(String rfid, long distance) {
		this.rfid = rfid;
		this.distance = distance;
	}
	
	public String getRfidTag() {
		return rfid;
	};

	public double getAngle() {
		return angle;
	}
	
	public long getDistance() {
		return distance;
	}
}
