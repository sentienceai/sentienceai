package com.airobotics.commandcenter.api.cache;

import java.io.Serializable;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconSensor;
import com.vividsolutions.jts.geom.Coordinate;

public class BeaconLoc implements Serializable, Comparable<BeaconLoc> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 83918829641967405L;
	private String id;
	protected double angle;
	protected long distance;
	public Coordinate location;
    
	public BeaconLoc() {
		this("", 0, null);
	}
    
	public BeaconLoc(String id) {
		this(id, 0, null);
	}

	public BeaconLoc(String id, double angle) {
		this(id, angle, null);
	}

	public BeaconLoc(String id, long distance) {
		this(id, distance, null);
	}

    public BeaconLoc(BeaconSensor beaconSensor, Coordinate location) {
    	this(beaconSensor.getId(), beaconSensor.getAngle(), location);
    }

    public BeaconLoc(BeaconSensor beaconSensor, Beacon beacon) {
    	this(beaconSensor.getId(), beaconSensor.getAngle(), beacon.getLocation());
    }

	public BeaconLoc(String id, double angle, Coordinate location) {
		this.id = id;
		this.angle = angle;
		this.location = location;
	}

	public BeaconLoc(String id, long distance, Coordinate location) {
		this.id = id;
		this.distance = distance;
		this.location = location;
	}
	
	public String getId() {
		return this.id;
	}
	
	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double angle) {
		this.angle = angle;
	}

    public long getDistance() {
		return distance;
	}

	public void setDistance(long distance) {
		this.distance = distance;
	}

	public Coordinate getLocation() {
		return location;
	}

	public void setLocation(Coordinate location) {
		this.location = location;
	}

	@Override
	public int compareTo(BeaconLoc compareBeaconLoc) {
		return (int) (this.getAngle() - compareBeaconLoc.getAngle());
	}
	
	@Override
	public String toString() {
		return id + ":" + angle + ":" + distance;
	}
}
