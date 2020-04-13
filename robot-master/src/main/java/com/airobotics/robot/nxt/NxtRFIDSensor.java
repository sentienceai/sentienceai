package com.airobotics.robot.nxt;

import java.util.LinkedList;
import java.util.List;

import com.airobotics.robot.api.IRFIDSensor;
import com.airobotics.robot.api.IRFIDSensorData;
import com.airobotics.robot.api.RFIDListener;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

public class NxtRFIDSensor extends I2CSensor implements IRFIDSensor, Runnable {
	private static final String HEXES = "0123456789abcdef";
	// address #8 on arduino
	private static final int I2C_SLAVE_ADDR = 8 << 1;
	private static final int RFID_LEN = 16;
	private static final int UNSIGNED_LONG_LEN = 4;
	private static final short RESET = 254;
	private static final short GET = 253;
	private static final short HALF = 180;
	private static byte[] buffReadResponse = new byte[20];
	private RFIDListener listener;
	private Thread thread;
	private boolean activated;

	private static class SensorHeader {
		long startTime, endTime, size;

		SensorHeader(long startTime, long endTime, long size) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.size = size;
		}
	}

	private static class SensorData {
		String uuid;
		long angle;

		SensorData(String uuid, long angle) {
			this.uuid = uuid;
			this.angle = angle;
		}
	}

	public NxtRFIDSensor(I2CPort s) {
		super(s, I2C_SLAVE_ADDR, I2CPort.LEGO_MODE, TYPE_HISPEED);
		thread = new Thread(this);
	}

	public void addRFIDListener(RFIDListener rfidListener) {
		listener = rfidListener;
	}

	public void activate() {
		if (!activated) {
			activated = true;
			thread.start();
		}
	}

	public void deactivate() {
		activated = false;
	}

	public void run() {
		while (activated && listener != null) {
			List<IRFIDSensorData> rfids = readRfid();
			for (IRFIDSensorData rfid : rfids)
				listener.onRfidDetected(rfid.getRfidTag(), rfid.getAngle());
		}
	}

	public void reset() {
		int res = sendData(RESET, null, 0);
		if (res < 0)
			System.out.print(" reset:" + res);
	}

	public void setAngle(double angle) {
		int res = sendData((int) getAngleInsideByteRange(angle), null, 0);
		if (res < 0)
			System.out.print(" setAngle:" + res);
	}

	private double getAngleInsideByteRange(double angle) {
		return (angle > HALF)? angle - HALF: angle;
	}

	public List<IRFIDSensorData> readRfid() {
		SensorHeader header = getSensorHeader();
		if (header == null) {
			System.out.println("header is null");
			return null;
		}
		// System.out.println("size: " + header.size + ", " + header.startTime +
		// "->" + header.endTime);
		// while (!lejos.nxt.Button.ESCAPE.isPressed()) {
		// }
		return getRfidDataList(header.size, header.startTime, header.endTime);
	}

	private SensorHeader getSensorHeader() {
		byte[] header = readData();
		if (header == null)
			return null;
		// printByteArray(header);
		// while (!lejos.nxt.Button.ESCAPE.isPressed()) {
		// }
		return new SensorHeader(convertToLong(header, 0), convertToLong(header, UNSIGNED_LONG_LEN),
				convertToLong(header, RFID_LEN));
	}

	private byte[] readData() {
		int res = getData(GET, buffReadResponse, buffReadResponse.length);
		if (res < 0)
			System.out.print(" get:" + res);
		if (res < 0 || !isValidSensorData(buffReadResponse))
			return null;
		return buffReadResponse;
	}

	private boolean isValidSensorData(byte[] bytes) {
		for (byte b : bytes)
			if (b != 0)
				return true;
		return false;
	}

	private long convertToLong(byte[] bytes, int startIndex) {
		long result = 0;
		int pos = 0;
		result += unsignedByteToInt(bytes[startIndex + pos++]) << 24;
		result += unsignedByteToInt(bytes[startIndex + pos++]) << 16;
		result += unsignedByteToInt(bytes[startIndex + pos++]) << 8;
		result += unsignedByteToInt(bytes[startIndex + pos++]) << 0;
		return result;
	}

	private int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	private List<IRFIDSensorData> getRfidDataList(long headerSize, long startTime, long endTime) {
		List<IRFIDSensorData> rfidDataList = new LinkedList<IRFIDSensorData>();
		populateRfidDataList(rfidDataList, headerSize, startTime, endTime);
		printf(rfidDataList);
		// while (!lejos.nxt.Button.ESCAPE.isPressed()) {
		// }
		return rfidDataList;
	}

	protected void populateRfidDataList(List<IRFIDSensorData> rfidDataList, long headerSize, long startTime,
			long endTime) {
		for (int i = 0; i < headerSize; i++) {
			SensorData sensorData = getSensorData();
			if (sensorData == null)
				continue;
			// System.out.println(sensorData.uuid.substring(0, 4) + ": " +
			// sensorData.time);
			// while (!lejos.nxt.Button.ESCAPE.isPressed()) {
			// }
			rfidDataList.add(new NxtRFIDSensorData(sensorData.uuid, (double) sensorData.angle / 100d));
		}
	}

	private SensorData getSensorData() {
		byte[] data = readData();
		if (data == null)
			return null;
		// printByteArray(data);
		// while (!lejos.nxt.Button.ESCAPE.isPressed()) {
		// }
		return new SensorData(convertToUuid(data), convertToLong(data, RFID_LEN));
	}

	@SuppressWarnings("unused")
	private long convertMicroToNano(long micro) {
		return micro * 1000;
	}

	@SuppressWarnings("unused")
	private double getAngle(long time, long startTime, long endTime) {
		return round(360d * (time - startTime) / (endTime - startTime));
	}

	private double round(double value) {
		return ((int) (value * 100d)) / 100d;
	}

	private String convertToUuid(byte[] bytes) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < RFID_LEN; i++) {
			buffer.append(HEXES.charAt((bytes[i] & 0xF0) >> 4)).append(HEXES.charAt((bytes[i] & 0x0F)));
			if (i == 3 || i == 5 || i == 7 || i == 9)
				buffer.append("-");
		}
		return buffer.toString();
	}

	private void printf(List<IRFIDSensorData> rfidDataList) {
		for (IRFIDSensorData rfidData : rfidDataList)
			System.out.print(rfidData.getRfidTag().substring(0, 2) + ":" + rfidData.getAngle() + " ");
		System.out.println();
	}

	@SuppressWarnings("unused")
	private void printByteArray(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++)
			System.out.print(bytes[i] + " ");
		System.out.println();
	}

	@SuppressWarnings("unused")
	private void printByteArrayToHex(byte[] bytes) {
		char[] hex = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int j = bytes[i] & 0xFF;
			hex[i * 2] = HEXES.charAt(j >>> 4);
			hex[i * 2 + 1] = HEXES.charAt(j & 0x0F);
		}
		System.out.println(new String(hex));
	}
}
