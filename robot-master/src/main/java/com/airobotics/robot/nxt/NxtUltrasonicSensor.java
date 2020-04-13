package com.airobotics.robot.nxt;

import ch.aplu.nxt.SensorPort;
import ch.aplu.nxt.UltrasonicSensor;

import com.airobotics.robot.api.IUltrasonicSensor;
import com.airobotics.robot.api.UltrasonicListener;

public class NxtUltrasonicSensor extends UltrasonicSensor implements IUltrasonicSensor, ch.aplu.nxt.UltrasonicListener {
	private UltrasonicListener listener;
	
	public NxtUltrasonicSensor(SensorPort s) {
		super(s);
	}

	public void addUltrasonicListener(UltrasonicListener ultrasonicListener) {
		this.listener = ultrasonicListener;
		super.addUltrasonicListener(this);
	}

	public void far(SensorPort port, int level) {
		listener.onDetectedFar(port.getId(), level);
	}

	public void near(SensorPort port, int level) {
		listener.onDetectedNear(port.getId(), level);
	}
}
