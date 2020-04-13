package com.airobotics.robot.api.core;

import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.robot.api.IMotor;
import com.airobotics.robot.api.IQueue;
import com.airobotics.robot.api.IRFIDSensor;
import com.airobotics.robot.api.ITouchSensor;
import com.airobotics.robot.api.IUltrasonicSensor;
import com.airobotics.robot.util.IHttpClient;

public class RobotProperty {
	public static enum MotorPort {
		mover, direction, beacon
	}

	private static RobotProperty robotProperty;
	public String serialNo;
	public String serverHost;
	public int serverPort;
	public IMotor moverMotor;
	public IMotor directionMotor;
	public IMotor beaconMotor;
	public double beaconMotorRotationPrecisionFactor;
	public IRFIDSensor rfidSensor;
	public ITouchSensor touchSensor;
	public IUltrasonicSensor sonarSensor;
	public IQueue<BeaconSensor> beaconSensorQueue;
	public IHttpClient httpClient;

	public static RobotProperty getInstance(String serialNo, String serverHost, int serverPort, IMotor moverMotor,
			IMotor directionMotor, IMotor beaconMotor, IRFIDSensor rfidSensor, ITouchSensor touchSensor,
			IUltrasonicSensor sonarSensor, IQueue<BeaconSensor> beaconSensorQueue, IHttpClient httpClient,
			double beaconMotorRotationPrecisionFactor) {
		if (robotProperty == null) {
			robotProperty = new RobotProperty();
			robotProperty.serialNo = serialNo;
			robotProperty.serverHost = serverHost;
			robotProperty.serverPort = serverPort;
			robotProperty.moverMotor = moverMotor;
			robotProperty.directionMotor = directionMotor;
			robotProperty.beaconMotor = beaconMotor;
			robotProperty.beaconMotorRotationPrecisionFactor = beaconMotorRotationPrecisionFactor;
			robotProperty.rfidSensor = rfidSensor;
			robotProperty.touchSensor = touchSensor;
			robotProperty.sonarSensor = sonarSensor;
			robotProperty.beaconSensorQueue = beaconSensorQueue;
			robotProperty.httpClient = httpClient;
		}
		return robotProperty;
	}

	public static RobotProperty getInstance() {
		return robotProperty;
	}

	private RobotProperty() {
	}
}