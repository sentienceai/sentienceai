package com.airobotics.commandcenter.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.core.util.AngleUtil;
import com.vividsolutions.jts.geom.Coordinate;

public class BeaconService {
	private static final int BEACONS_NEEDED = 3;
	
	public static List<BeaconLoc[]> getBeaconsSetFromBeacons(List<Beacon> beacons) {
		List<BeaconLoc> beaconLocs = new ArrayList<BeaconLoc>();
		for (Beacon beacon : beacons)
			beaconLocs.add(new BeaconLoc(beacon.getId(), 0, beacon.getLocation()));

		return getBeaconsSet(beaconLocs);
	}

	public static List<BeaconLoc[]> getBeaconsSet(List<BeaconLoc> beacons) {
		List<BeaconLoc[]> beaconsSet = new ArrayList<BeaconLoc[]>();
		BeaconLoc[] threeBeacons = new BeaconLoc[BEACONS_NEEDED];
		populateBeaconSet(beaconsSet, beacons, threeBeacons, 0, beacons.size()-1, 0);
		
		return beaconsSet;
	}

	public static void populateBeaconSet(List<BeaconLoc[]> beaconSet, List<BeaconLoc> beacons, BeaconLoc[] threeBeacons,
			int start, int end, int index) {
		if (index == BEACONS_NEEDED) {
		    BeaconLoc[] tmp = new BeaconLoc[BEACONS_NEEDED];
		    System.arraycopy(threeBeacons, 0, tmp, 0, threeBeacons.length);
			beaconSet.add(tmp);
			return;
		}

		for (int i=start; i<=end && end-i+1 >= BEACONS_NEEDED-index; i++) {
			threeBeacons[index] = beacons.get(i);
			populateBeaconSet(beaconSet, beacons, threeBeacons, i+1, end, index+1);
		}
	}
	
	public static LinkedList<BeaconSensor> getBeaconSensors(List<Beacon> beacons, Coordinate location) {
		LinkedList<BeaconSensor> beaconSensors = new LinkedList<BeaconSensor>();
		double angle = 0;
		for (int i=0; i<beacons.size(); i++)
			angle = addBeaconSensor(beacons.get(i),
					(i==0)? beacons.get(beacons.size()-1): beacons.get(i-1),
							location, beaconSensors, angle);
		return beaconSensors;
	}

	private static double addBeaconSensor(Beacon currBeacon, Beacon prevBeacon, Coordinate location,
			LinkedList<BeaconSensor> beaconSensors, double angle) {
		angle += AngleUtil.getAngleBetween(prevBeacon.getLocation(), location, currBeacon.getLocation());
		beaconSensors.add(new BeaconSensor(currBeacon.getId(), angle, currBeacon.getSerialNumber()));
		return angle;
	}
}
