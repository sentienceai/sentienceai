package com.airobotics.commandcenter.api.cache;

import java.io.Serializable;

public class RobotVal implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1177328737347912126L;
	private String id;
    private int wheelDiameter;
    private int coverageDiameter;

	private int routeIdx;
	private int scheduleAverageRunningTime;
	
	public RobotVal(String id, int wheelDiameter, int coverageDiameter, int routeIdx, int scheduleAverageRunningTime) {
		this.id = id;
		this.wheelDiameter = wheelDiameter;
		this.coverageDiameter = coverageDiameter;
		this.routeIdx = routeIdx;
		this.scheduleAverageRunningTime = scheduleAverageRunningTime;
	}

    public String getId() {
		return id;
	}
	
	public int getWheelDiameter() {
		return wheelDiameter;
	}
	
	public void setWheelDiameter(int wheelDiameter) {
		this.wheelDiameter = wheelDiameter;
	}
    
	public int getCoverageDiameter() {
		return coverageDiameter;
	}

	public void setCoverageDiameter(int coverageDiameter) {
		this.coverageDiameter = coverageDiameter;
	}
	
	public int getRouteIdx() {
		return routeIdx;
	}
	
	public void setRouteIdx(int routeIdx) {
		this.routeIdx = routeIdx;
	}
	
	public int getScheduleAverageRunningTime() {
		return scheduleAverageRunningTime;
	}
	
	public void setScheduleAverageRunningTime(int scheduleAverageRunningTime) {
		this.scheduleAverageRunningTime = scheduleAverageRunningTime;
	}
}
