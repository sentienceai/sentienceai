package com.airobotics.robot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.airobotics.robot.api.IMotor;
import com.airobotics.robot.api.IRFIDSensor;
import com.airobotics.robot.api.ITouchSensor;
import com.airobotics.robot.api.IUltrasonicSensor;
import com.airobotics.robot.nxt.NxtRFIDSensor;
import com.airobotics.robot.nxt.NxtTankMotor;
import com.airobotics.robot.nxt.NxtTouchSensor;
import com.airobotics.robot.nxt.NxtUltrasonicSensor;

import ch.aplu.nxt.SensorPort;
import lejos.nxt.MotorPort;
import lejos.nxt.comm.Bluetooth;

public class ComponentFactory {
	private static Properties properties;
	private static ROBOT_MODEL robotModel = ROBOT_MODEL.NXT;

	public static enum ROBOT_MODEL {
		NXT, AIROBOT, SIMULATOR
	}

	static {
		init();
	}

	static void init() {
		try {
			properties = new Properties();
			properties.load(new FileInputStream(new File("robot.properties")));
			String model = properties.getProperty("robot.model");
			if (model.equals("NXT"))
				robotModel = ROBOT_MODEL.NXT;
			else if (model.equals("SIMULATOR"))
				robotModel = ROBOT_MODEL.SIMULATOR;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ROBOT_MODEL getRobotModel() {
		if (properties == null)
			init();
		return robotModel;
	}

	public static IMotor getMotor(int port) {
		if (robotModel == ROBOT_MODEL.NXT) {
			switch (port) {
			case 0:
				return new NxtTankMotor(MotorPort.A);
			case 1:
				return new NxtTankMotor(MotorPort.B);
			case 2:
				return new NxtTankMotor(MotorPort.C);
			default:
				throw new IllegalArgumentException("no such motor");
			}
		}

		return null;
	}

	public static IRFIDSensor getRFIDSensor(int port) {
		if (robotModel == ROBOT_MODEL.NXT) {
			switch (port) {
			case 0:
				return new NxtRFIDSensor(lejos.nxt.SensorPort.S1);
			case 1:
				return new NxtRFIDSensor(lejos.nxt.SensorPort.S2);
			case 2:
				return new NxtRFIDSensor(lejos.nxt.SensorPort.S3);
			case 3:
				return new NxtRFIDSensor(lejos.nxt.SensorPort.S4);
			default:
				throw new IllegalArgumentException("no such port");
			}
		}

		return null;
	}

	public static ITouchSensor getTouchSensor(int port) {
		if (robotModel == ROBOT_MODEL.NXT) {
			switch (port) {
			case 0:
				return new NxtTouchSensor(SensorPort.S1);
			case 1:
				return new NxtTouchSensor(SensorPort.S2);
			case 2:
				return new NxtTouchSensor(SensorPort.S3);
			case 3:
				return new NxtTouchSensor(SensorPort.S4);
			default:
				throw new IllegalArgumentException("no such port");
			}
		}

		return null;
	}

	public static IUltrasonicSensor getUltrasonicSensor(int port) {
		if (robotModel == ROBOT_MODEL.NXT) {
			switch (port) {
			case 0:
				return new NxtUltrasonicSensor(SensorPort.S1);
			case 1:
				return new NxtUltrasonicSensor(SensorPort.S2);
			case 2:
				return new NxtUltrasonicSensor(SensorPort.S3);
			case 3:
				return new NxtUltrasonicSensor(SensorPort.S4);
			default:
				throw new IllegalArgumentException("no such port");
			}
		}

		return null;
	}

	public static String getSerialNo() {
		if (properties == null)
			init();

		if (robotModel == ROBOT_MODEL.NXT)
			return Bluetooth.getLocalAddress();
		else if (robotModel == ROBOT_MODEL.SIMULATOR)
			return properties.getProperty("robot.serialNo");

		return null;
	}

	public static String getServerHost() {
		if (properties == null)
			init();
		return properties.getProperty("server.host");
	}

	public static int getServerPort() {
		if (properties == null)
			init();
		return Integer.parseInt(properties.getProperty("server.port"));
	}

	public static IHttpClient getHttpClient() {
		if (properties == null)
			init();

		if (robotModel == ROBOT_MODEL.NXT)
			return new NxtHttpClient();
		else
			return new HttpClient();
	}
}
