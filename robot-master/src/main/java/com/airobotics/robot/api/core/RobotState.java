package com.airobotics.robot.api.core;

import java.util.Hashtable;

public class RobotState {
	public static final double defaultIntervalToGetNextInstrunction = 5000;
	public static final double defaultBeaconMotorSpeedThatRotates360DegreeIn6Sec = 60;
	
	public boolean isPowerOffSignalPosted = false;
	public boolean isActive = false;
	public int routeIdx = -1;
	public int beaconCount = 0;
	public int intervalToCompleteRotationOfBeaconMotor = 10000;
	public double intervalToGetNextInstrunction = defaultIntervalToGetNextInstrunction;
	public Hashtable<String, String> sensors = new Hashtable<String, String>();
	public Hashtable<String, String> beaconSerialIdMap = new Hashtable<String, String>();
}