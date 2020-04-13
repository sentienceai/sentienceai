package com.airobotics.robot;

import java.net.NXTSocketUtils;

import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.robot.api.IMotor;
import com.airobotics.robot.api.IQueue;
import com.airobotics.robot.api.IRFIDSensor;
import com.airobotics.robot.api.ITouchSensor;
import com.airobotics.robot.api.IUltrasonicSensor;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.nxt.NxtLinkedList;
import com.airobotics.robot.util.ComponentFactory;
import com.airobotics.robot.util.IHttpClient;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class Main {
	private static Robot robot;

	public static void main(String[] args) {
		String serialNo = ComponentFactory.getSerialNo();
		String serverHost = ComponentFactory.getServerHost();
		int serverPort = ComponentFactory.getServerPort();
		IMotor moverMotor = ComponentFactory.getMotor(RobotProperty.MotorPort.mover.ordinal());
		IMotor directionMotor = ComponentFactory.getMotor(RobotProperty.MotorPort.direction.ordinal());
		IMotor beaconMotor = ComponentFactory.getMotor(RobotProperty.MotorPort.beacon.ordinal());
		double beaconMotorRotationPrecisionFactor = 100d;
		IRFIDSensor rfidSensor = ComponentFactory.getRFIDSensor(0);
		ITouchSensor touchSensor = ComponentFactory.getTouchSensor(1);
		IUltrasonicSensor sonarSensor = ComponentFactory.getUltrasonicSensor(2);
		IQueue<BeaconSensor> beaconSensorQueue = null;
		IHttpClient httpClient = ComponentFactory.getHttpClient();

		if (isNxt()) {
			beaconMotorRotationPrecisionFactor = 1d;
			beaconSensorQueue = new NxtLinkedList<BeaconSensor>();
			initBluetoothSocket();
		}

		robot = new Robot(RobotProperty.getInstance(serialNo, serverHost, serverPort, moverMotor, directionMotor,
				beaconMotor, rfidSensor, touchSensor, sonarSensor, beaconSensorQueue, httpClient,
				beaconMotorRotationPrecisionFactor));

		if (isNxt())
			catchExitSignal();
		robot.init();
		robot.run();
		System.exit(0);
	}

	private static boolean isNxt() {
		return ComponentFactory.getRobotModel() == ComponentFactory.ROBOT_MODEL.NXT;
	}

	private static void initBluetoothSocket() {
		System.out.println("Waiting for SocketProxy");
		BTConnection btc = Bluetooth.waitForConnection();
		LCD.clear();
		NXTSocketUtils.setNXTConnection(btc);
	}

	protected static void catchExitSignal() {
		Thread thread = new Thread() {
			public void run() {
				Button.ESCAPE.waitForPressAndRelease();
				robot.postPowerOffSignal();
			}
		};
		thread.start();
	}
}