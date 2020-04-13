package com.airobotics.robot.nxt;

import ch.aplu.nxt.SensorPort;
import ch.aplu.nxt.TouchSensor;

import com.airobotics.robot.api.ITouchSensor;
import com.airobotics.robot.api.TouchListener;

public class NxtTouchSensor extends TouchSensor implements ITouchSensor, ch.aplu.nxt.TouchListener {
	private TouchListener listener;
	
	public NxtTouchSensor(SensorPort s) {
		super(s);
	}

	public void addTouchListener(TouchListener touchListener) {
		this.listener = touchListener;
		super.addTouchListener(this);
	}

	public void pressed(SensorPort port) {
		listener.onPressed(port.getId());
	}

	public void released(SensorPort port) {
		listener.onReleased(port.getId());
	}
}
