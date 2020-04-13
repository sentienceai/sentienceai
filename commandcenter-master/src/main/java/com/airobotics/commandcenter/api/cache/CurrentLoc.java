package com.airobotics.commandcenter.api.cache;

import com.airobotics.core.geom.TriangleWithDistance;
import com.airobotics.core.geom.TriangleWithPoint;
import com.vividsolutions.jts.geom.Coordinate;

public class CurrentLoc {
	private Coordinate currentLocation;
	private TriangleWithPoint triangleWithPoint;
	private TriangleWithDistance triangleWithDistance;

	public CurrentLoc(Coordinate currentLocation, TriangleWithPoint triangleWithPoint) {
		this.currentLocation = currentLocation;
		this.triangleWithPoint = triangleWithPoint;
	}

	public CurrentLoc(Coordinate currentLocation, TriangleWithDistance triangleWithDistance) {
		this.currentLocation = currentLocation;
		this.triangleWithDistance = triangleWithDistance;
	}
	
	public Coordinate getCurrentLocation() {
		return currentLocation;
	}
	
	public void setCurrentLocation(Coordinate currentLocation) {
		this.currentLocation = currentLocation;
	}
	
	public TriangleWithPoint getTriangleWithPoint() {
		return triangleWithPoint;
	}
	
	public void setTriangleWithPoint(TriangleWithPoint triangleWithPoint) {
		this.triangleWithPoint = triangleWithPoint;
	}

	public TriangleWithDistance getTriangleWithDistance() {
		return triangleWithDistance;
	}

	public void setTriangleWithDistance(TriangleWithDistance triangleWithDistance) {
		this.triangleWithDistance = triangleWithDistance;
	}
}
