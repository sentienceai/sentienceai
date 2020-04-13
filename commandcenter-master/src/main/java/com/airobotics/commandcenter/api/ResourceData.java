package com.airobotics.commandcenter.api;

import com.airobotics.api.entities.AngleMap;
import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconScan;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.DistanceMap;
import com.airobotics.api.entities.Footprint;
import com.airobotics.api.entities.LocationMap;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.ScheduleInstance;
import com.airobotics.api.entities.User;

import net.rubyeye.xmemcached.MemcachedClient;
import net.vz.mongodb.jackson.JacksonDBCollection;

public class ResourceData {
	public MemcachedClient memcachedClient;
	public JacksonDBCollection<User, String> userCollection;
	public JacksonDBCollection<Robot, String> robotCollection;
	public JacksonDBCollection<Beacon, String> beaconCollection;
	public JacksonDBCollection<BeaconScan, String> beaconScanCollection;
	public JacksonDBCollection<LocationMap, String> locationMapCollection;
	public JacksonDBCollection<Schedule, String> scheduleCollection;
	public JacksonDBCollection<ScheduleInstance, String> scheduleInstanceCollection;
	public JacksonDBCollection<Boundary, String> boundaryCollection;
	public JacksonDBCollection<AngleMap, String> angleMapCollection;
	public JacksonDBCollection<Footprint, String> footprints;
	public JacksonDBCollection<DistanceMap, String> distanceMapCollection;

	public ResourceData(MemcachedClient memcachedClient, JacksonDBCollection<User, String> userCollection, 
			JacksonDBCollection<Robot, String> robotCollection,
			JacksonDBCollection<Beacon, String> beaconCollection,
			JacksonDBCollection<BeaconScan, String> beaconScanCollection,
			JacksonDBCollection<LocationMap, String> locationMapCollection,
			JacksonDBCollection<Schedule, String> scheduleCollection,
			JacksonDBCollection<ScheduleInstance, String> scheduleInstanceCollection,
			JacksonDBCollection<Boundary, String> boundaryCollection,
			JacksonDBCollection<AngleMap, String> angleMapCollection,
			JacksonDBCollection<Footprint, String> footprints,
			JacksonDBCollection<DistanceMap, String> distanceMapCollection) {
		this.memcachedClient = memcachedClient;
		this.userCollection = userCollection;
		this.robotCollection = robotCollection;
		this.beaconCollection = beaconCollection;
		this.beaconScanCollection = beaconScanCollection;
		this.locationMapCollection = locationMapCollection;
		this.scheduleCollection = scheduleCollection;
		this.scheduleInstanceCollection = scheduleInstanceCollection;
		this.boundaryCollection = boundaryCollection;
		this.angleMapCollection = angleMapCollection;
		this.footprints = footprints;
		this.distanceMapCollection = distanceMapCollection;
	}
}