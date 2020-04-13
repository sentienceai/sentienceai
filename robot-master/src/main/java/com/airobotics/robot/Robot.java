package com.airobotics.robot;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.api.entities.Instruction;
import com.airobotics.api.entities.Request;
import com.airobotics.api.entities.Sensor;
import com.airobotics.robot.api.IRFIDSensorData;
import com.airobotics.robot.api.RFIDListener;
import com.airobotics.robot.api.TouchListener;
import com.airobotics.robot.api.UltrasonicListener;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;
import com.airobotics.robot.core.InstructionProcessor;
import com.airobotics.robot.util.URLEncoder;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class Robot implements RFIDListener, TouchListener, UltrasonicListener {
	private String robotId;
	private RobotProperty robotProperty;
	private RobotState robotState;

	public Robot(RobotProperty robotProperty) {
		this.robotProperty = robotProperty;
		this.robotState = new RobotState();
	}

	public void init() {
		try {
			robotId = getRobotId();
			populateBeaconSerialIdMap();

			// robotProperty.rfidSensor.addRFIDListener(this);
			robotProperty.touchSensor.addTouchListener(this);
			robotProperty.sonarSensor.addUltrasonicListener(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void postPowerOffSignal() {
		robotState.isPowerOffSignalPosted = true;
	}

	private String getRobotId() throws IOException, JSONException {
		String response = robotProperty.httpClient.get(robotProperty.serverHost, robotProperty.serverPort,
				"/api/v1/robots?serialNo=" + robotProperty.serialNo);
		JSONObject json = new JSONObject(response);
		return (String) json.get("id");
	}

	private void populateBeaconSerialIdMap() throws IOException, JSONException {
		String response = robotProperty.httpClient.get(robotProperty.serverHost, robotProperty.serverPort,
				"/api/v1/robots/" + robotId + "/beacons");
		JSONArray beacons = new JSONArray(response);
		robotState.beaconCount = beacons.length();
		for (int i = 0; i < robotState.beaconCount; i++) {
			JSONObject beacon = beacons.getJSONObject(i);
			robotState.beaconSerialIdMap.put((String) beacon.get("serialNumber"), (String) beacon.get("id"));
		}
	}

	public void onRfidDetected(String serialNo, double angle) {
		String beaconId = robotState.beaconSerialIdMap.get(serialNo);
		// Only read the beacons that belong to this robot
		if (doesBeaconBelongToRobot(beaconId))
			this.addToBeaconSensorQueue(beaconId, (long) robotProperty.beaconMotor.getCurrentPosition());
	}

	public void onDetectedFar(int port, int level) {
		robotProperty.moverMotor.stop();
		addToSensorData("ultrasonic", Integer.toString(level));
	}

	public void onDetectedNear(int port, int level) {
		robotProperty.moverMotor.stop();
		addToSensorData("ultrasonic", Integer.toString(level));
	}

	public void onPressed(int port) {
		robotProperty.moverMotor.stop();
		addToSensorData("touch", "pressed");
	}

	public void onReleased(int port) {
		robotProperty.moverMotor.stop();
		addToSensorData("touch", "released");
	}

	public void run() {
		while (true) {
			try {
				if (robotState.isPowerOffSignalPosted)
					break;
				resetRobotStateBeforeGettingNextInstruction();
				collectBeaconSensorData();
				InstructionProcessor.process(getInstruction(), robotProperty, robotState);
				waitForIntervalToGetNextInstrunction();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(60 * 1000);
				} catch (Exception e1) {
				}
			}
		}
	}

	public void resetRobotStateBeforeGettingNextInstruction() {
		robotState.intervalToGetNextInstrunction = RobotState.defaultIntervalToGetNextInstrunction;
	}

	private void collectBeaconSensorData() throws InterruptedException {
		if (!robotState.isActive)
			return;
		robotProperty.beaconSensorQueue.clear();
		rotateBeaconMotorToCollectBeaconSensorData();
	}

	private void rotateBeaconMotorToCollectBeaconSensorData() {
		robotProperty.rfidSensor.reset();
		waitForCompletingRotationOfBeaconMotor();
		List<IRFIDSensorData> rfidDataList = robotProperty.rfidSensor.readRfid();
		populateBeaconSensorQueue(rfidDataList);
	}

	private void waitForCompletingRotationOfBeaconMotor() {
		try {
			Thread.sleep(robotState.intervalToCompleteRotationOfBeaconMotor);
		} catch (InterruptedException e) {
		}
	}

	private void populateBeaconSensorQueue(List<IRFIDSensorData> rfidDataList) {
		if (rfidDataList == null || rfidDataList.size() < 3)
			return;
		for (IRFIDSensorData rfidData : rfidDataList) {
			String beaconId = robotState.beaconSerialIdMap.get(rfidData.getRfidTag());
			if (doesBeaconBelongToRobot(beaconId))
				this.addToBeaconSensorQueue(beaconId, rfidData.getAngle());
		}
	}

	protected boolean doesBeaconBelongToRobot(String beaconId) {
		return beaconId != null;
	}

	private void waitForIntervalToGetNextInstrunction() throws InterruptedException {
		if (hasSensorData())
			return;

		for (int i = 0; i < robotState.intervalToGetNextInstrunction; i = i + 100)
			waitForInterval();
	}

	private boolean hasSensorData() {
		Enumeration<String> keys = robotState.sensors.keys();
		while (keys.hasMoreElements()) {
			String type = (String) keys.nextElement();
			if (robotState.sensors.get(type) != null)
				return true;
		}
		return false;
	}

	public void waitForInterval() throws InterruptedException {
		if (!isSimulationModeThatUsesLinkedListWhileNxtUsesQueue(robotProperty))
			Thread.sleep((long) 100);
	}

	private boolean isSimulationModeThatUsesLinkedListWhileNxtUsesQueue(RobotProperty robotProperty) {
		return robotProperty.beaconSensorQueue instanceof LinkedList;
	}

	private Instruction getInstruction() throws IOException, JSONException {
		Request request = getRequest();
		String response = robotProperty.httpClient.get(robotProperty.serverHost, robotProperty.serverPort,
				"/api/v1/robots/" + robotId + "/instructions?req=" + URLEncoder.encode(request.toJSONString()));
		return new Instruction(new JSONObject(response));
	}

	private Request getRequest() {
		Request request = new Request();
		setBeaconSensors(request);
		setSensors(request);
		request.setDirectionAngle(robotProperty.directionMotor.getCurrentPosition());
		request.setRouteIdx(robotState.routeIdx);
		return request;
	}

	private void addToBeaconSensorQueue(String beaconId, double angle) {
		synchronized (robotProperty.beaconSensorQueue) {
			robotProperty.beaconSensorQueue.offer(new BeaconSensor(beaconId, angle));
		}
	}

	private void addToSensorData(String sensor, String data) {
		synchronized (robotState.sensors) {
			robotState.sensors.put(sensor, data);
		}
	}

	private void setBeaconSensors(Request request) {
		synchronized (robotProperty.beaconSensorQueue) {
			while (!robotProperty.beaconSensorQueue.isEmpty()) {
				BeaconSensor beaconSensor = (BeaconSensor) robotProperty.beaconSensorQueue.poll();
				if (beaconSensor == null)
					break;
				request.addBeaconSensor(beaconSensor);
			}
		}
	}

	private void setSensors(Request request) {
		synchronized (robotState.sensors) {
			Enumeration<String> keys = robotState.sensors.keys();
			while (keys.hasMoreElements()) {
				String type = (String) keys.nextElement();
				String data = robotState.sensors.get(type);
				robotState.sensors.put(type, null);
				request.addSensor(new Sensor(type, data));
			}
		}
	}
}