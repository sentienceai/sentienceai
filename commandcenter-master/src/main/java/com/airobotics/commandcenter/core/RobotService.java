package com.airobotics.commandcenter.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airobotics.api.entities.AngleMap;
import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.DistanceMap;
import com.airobotics.api.entities.LocationMap;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.api.cache.BeaconLoc;
import com.airobotics.commandcenter.api.cache.CurrentLoc;
import com.airobotics.core.geom.AngleCoordinateMap;
import com.airobotics.core.geom.DistanceCoordinateMap;
import com.airobotics.core.geom.TriangleWithDistance;
import com.airobotics.core.geom.TriangleWithPoint;
import com.airobotics.core.util.AngleUtil;
import com.airobotics.core.util.GeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;

import net.vz.mongodb.jackson.DBUpdate;

public class RobotService {
	private static final Logger logger = LoggerFactory.getLogger(RobotService.class);
	private ResourceData resourceData;
	private int cacheDuration = 43200; // 12 hours

	public RobotService(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	public static double getDistance(double wheelDiameter, double distance) {
		// circumference = 2πr
		// circumference = 2 * Math.PI * (wheelDiameter / 2)
		double circumference = Math.PI * wheelDiameter;
		double digreeForDistance1mm = 360 / circumference;
		return distance * digreeForDistance1mm;
	}

	public static double getDirection(double wheelDiameter, double coverageDiameter, double angle) {
		// circumference = 2πr
		// circumference = 2 * Math.PI * (coverageDiameter / 2)
		double circumference = Math.PI * coverageDiameter;
		// circumference : 360 = X : angle
		double distance = (circumference * angle) / 360;
		return getDistance(wheelDiameter, distance);
	}

	public static List<TriangleWithPoint> getTriangleWithPoints(List<BeaconLoc> beacons) {
		List<TriangleWithPoint> triangleWithPointList = new ArrayList<TriangleWithPoint>();
		List<BeaconLoc[]> beaconLocSet = BeaconService.getBeaconsSet(beacons);
		for (BeaconLoc[] beaconLocs : beaconLocSet)
			triangleWithPointList.add(new TriangleWithPoint(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
					beaconLocs[2].getLocation(), beaconLocs[0].getAngle(), beaconLocs[1].getAngle(),
					beaconLocs[2].getAngle()));

		return triangleWithPointList;
	}

	public CurrentLoc getCurrentLocation(List<BeaconLoc> beacons) {
		if (hasDistance(beacons))
			return getCurrentLocationByDistance(beacons);

		for (TriangleWithPoint triangleWithPoint : getTriangleWithPoints(beacons)) {
			Coordinate currentLocation = null;
			try {
				currentLocation = this.getCurrentLocation(triangleWithPoint);
			} catch (Exception e) {
				continue;
			} finally {
				if (currentLocation != null)
					return new CurrentLoc(currentLocation, triangleWithPoint);
			}
		}

		throw new LocationNotFoundException();
	}

	private boolean hasDistance(List<BeaconLoc> beacons) {
		for (BeaconLoc beacon : beacons)
			if (beacon.getDistance() != 0)
				return true;
		return false;
	}

	private CurrentLoc getCurrentLocationByDistance(List<BeaconLoc> beacons) {
		for (TriangleWithDistance triangleWithDistance : getTriangleWithDistance(beacons)) {
			Coordinate currentLocation = null;
			try {
				currentLocation = this.getCurrentLocationByDistance(triangleWithDistance);
			} catch (Exception e) {
				continue;
			} finally {
				if (currentLocation != null)
					return new CurrentLoc(currentLocation, triangleWithDistance);
			}
		}

		throw new LocationNotFoundException();
	}

	public static List<TriangleWithDistance> getTriangleWithDistance(List<BeaconLoc> beacons) {
		List<TriangleWithDistance> triangleWithDistanceList = new ArrayList<TriangleWithDistance>();
		List<BeaconLoc[]> beaconLocSet = BeaconService.getBeaconsSet(beacons);
		for (BeaconLoc[] beaconLocs : beaconLocSet)
			triangleWithDistanceList.add(new TriangleWithDistance(beaconLocs[0].getLocation(),
					beaconLocs[1].getLocation(), beaconLocs[2].getLocation(), beaconLocs[0].getDistance(),
					beaconLocs[1].getDistance(), beaconLocs[2].getDistance()));

		return triangleWithDistanceList;
	}

	public Coordinate getCurrentLocationByDistance(TriangleWithDistance triangleWithDistance) {
		Coordinate loc = GeometryUtil.findLocation(triangleWithDistance.p0, triangleWithDistance.p1,
				triangleWithDistance.p2,
				getCoordinateByDistance(triangleWithDistance.p1, triangleWithDistance.p0, triangleWithDistance.p2,
						triangleWithDistance.getDistanceFromP0()),
				getCoordinateByDistance(triangleWithDistance.p0, triangleWithDistance.p1, triangleWithDistance.p2,
						triangleWithDistance.getDistanceFromP1()),
				getCoordinateByDistance(triangleWithDistance.p0, triangleWithDistance.p2, triangleWithDistance.p1,
						triangleWithDistance.getDistanceFromP2()));

		logger.debug(triangleWithDistance.p0 + "" + triangleWithDistance.getDistanceFromP0());
		logger.debug(triangleWithDistance.p1 + "" + triangleWithDistance.getDistanceFromP1());
		logger.debug(triangleWithDistance.p2 + "" + triangleWithDistance.getDistanceFromP2());
		logger.debug("LOC:" + loc);

		return loc;
	}

	protected Coordinate getCoordinateByDistance(Coordinate left, Coordinate center, Coordinate right, long distance) {
		Coordinate mid = new LineSegment(left, right).midPoint();
		LineSegment centerLine = new LineSegment(center, mid);
		double fraction = distance / centerLine.getLength();
		return centerLine.pointAlong(fraction);
	}

	public Coordinate getCurrentLocation(TriangleWithPoint triangleWithPoint) {
		String key1 = this.generateKey(triangleWithPoint.p0, triangleWithPoint.p1, triangleWithPoint.p2,
				AngleUtil.toIntAngle(triangleWithPoint.getAngleBetweenP0P1()), -1, -1);
		String key2 = this.generateKey(triangleWithPoint.p0, triangleWithPoint.p1, triangleWithPoint.p2, -1,
				AngleUtil.toIntAngle(triangleWithPoint.getAngleBetweenP1P2()), -1);
		String key3 = this.generateKey(triangleWithPoint.p0, triangleWithPoint.p1, triangleWithPoint.p2, -1, -1,
				AngleUtil.toIntAngle(triangleWithPoint.getAngleBetweenP2P0()));

		AngleMap locationByAngleBetweenBeacon1Beacon2 = resourceData.angleMapCollection.findOneById(key1);
		AngleMap locationByAngleBetweenBeacon2Beacon3 = resourceData.angleMapCollection.findOneById(key2);
		AngleMap locationByAngleBetweenBeacon3Beacon1 = resourceData.angleMapCollection.findOneById(key3);

		if (locationByAngleBetweenBeacon1Beacon2 == null || locationByAngleBetweenBeacon2Beacon3 == null
				|| locationByAngleBetweenBeacon3Beacon1 == null)
			throw new AngleOutsideBeaconsRangeException();

		Coordinate loc = GeometryUtil.findLocation(triangleWithPoint.p0, triangleWithPoint.p1, triangleWithPoint.p2,
				locationByAngleBetweenBeacon1Beacon2.getCoordinate(),
				locationByAngleBetweenBeacon2Beacon3.getCoordinate(),
				locationByAngleBetweenBeacon3Beacon1.getCoordinate());

		logger.debug(triangleWithPoint.p0 + "" + triangleWithPoint.getAngleBetweenP0P1());
		logger.debug(triangleWithPoint.p1 + "" + triangleWithPoint.getAngleBetweenP1P2());
		logger.debug(triangleWithPoint.p2 + "" + triangleWithPoint.getAngleBetweenP2P0());
		logger.debug("LOC:" + loc);

		return loc;
	}

	public Coordinate getCurrentLocationByMemcache(BeaconLoc[] beaconLocs) {
		double angleBetweenBeacon1Beacon2 = beaconLocs[1].getAngle() - beaconLocs[0].getAngle();
		double angleBetweenBeacon2Beacon3 = beaconLocs[2].getAngle() - beaconLocs[1].getAngle();
		double angleBetweenBeacon3Beacon1 = 360 + beaconLocs[0].getAngle() - beaconLocs[2].getAngle();

		// Takes too long time to put all of them to memcache
		String key1 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), AngleUtil.toIntAngle(angleBetweenBeacon1Beacon2), -1, -1);
		String key2 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), -1, AngleUtil.toIntAngle(angleBetweenBeacon2Beacon3), -1);
		String key3 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), -1, -1, AngleUtil.toIntAngle(angleBetweenBeacon3Beacon1));

		Coordinate locationByAngleBetweenBeacon1Beacon2 = null;
		Coordinate locationByAngleBetweenBeacon2Beacon3 = null;
		Coordinate locationByAngleBetweenBeacon3Beacon1 = null;
		try {
			locationByAngleBetweenBeacon1Beacon2 = resourceData.memcachedClient.get(key1);
			locationByAngleBetweenBeacon2Beacon3 = resourceData.memcachedClient.get(key2);
			locationByAngleBetweenBeacon3Beacon1 = resourceData.memcachedClient.get(key3);

			if (locationByAngleBetweenBeacon1Beacon2 == null || locationByAngleBetweenBeacon2Beacon3 == null
					|| locationByAngleBetweenBeacon3Beacon1 == null) {
				this.cacheLocationMaps(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
						beaconLocs[2].getLocation());
				locationByAngleBetweenBeacon1Beacon2 = resourceData.memcachedClient.get(key1);
				locationByAngleBetweenBeacon2Beacon3 = resourceData.memcachedClient.get(key2);
				locationByAngleBetweenBeacon3Beacon1 = resourceData.memcachedClient.get(key3);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Load from DB
		// LocationMap[] locationMaps =
		// this.loadLocationMaps(beaconLocs[0].getLocation(),
		// beaconLocs[1].getLocation(), beaconLocs[2].getLocation());
		// Coordinate locationByAngleBetweenBeacon1Beacon2 =
		// locationMaps[0].getCoordinateByAngle().get(angleBetweenBeacon1Beacon2);
		// Coordinate locationByAngleBetweenBeacon2Beacon3 =
		// locationMaps[1].getCoordinateByAngle().get(angleBetweenBeacon2Beacon3);
		// Coordinate locationByAngleBetweenBeacon3Beacon1 =
		// locationMaps[2].getCoordinateByAngle().get(angleBetweenBeacon3Beacon1);

		if (locationByAngleBetweenBeacon1Beacon2 == null || locationByAngleBetweenBeacon2Beacon3 == null
				|| locationByAngleBetweenBeacon3Beacon1 == null)
			return null;

		return GeometryUtil.findLocation(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), locationByAngleBetweenBeacon1Beacon2, locationByAngleBetweenBeacon2Beacon3,
				locationByAngleBetweenBeacon3Beacon1);
	}

	public Coordinate getCurrentLocation(BeaconLoc[] beaconLocs) {
		double angleBetweenBeacon1Beacon2 = beaconLocs[1].getAngle() - beaconLocs[0].getAngle();
		double angleBetweenBeacon2Beacon3 = beaconLocs[2].getAngle() - beaconLocs[1].getAngle();
		double angleBetweenBeacon3Beacon1 = 360 + beaconLocs[0].getAngle() - beaconLocs[2].getAngle();

		if (GeometryUtil.isAngleOnTip(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), angleBetweenBeacon1Beacon2, angleBetweenBeacon2Beacon3,
				angleBetweenBeacon3Beacon1))
			return beaconLocs[0].getLocation();
		if (GeometryUtil.isAngleOnTip(beaconLocs[1].getLocation(), beaconLocs[2].getLocation(),
				beaconLocs[0].getLocation(), angleBetweenBeacon2Beacon3, angleBetweenBeacon3Beacon1,
				angleBetweenBeacon1Beacon2))
			return beaconLocs[1].getLocation();
		if (GeometryUtil.isAngleOnTip(beaconLocs[2].getLocation(), beaconLocs[0].getLocation(),
				beaconLocs[1].getLocation(), angleBetweenBeacon3Beacon1, angleBetweenBeacon1Beacon2,
				angleBetweenBeacon2Beacon3))
			return beaconLocs[2].getLocation();

		String key1 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), AngleUtil.toIntAngle(angleBetweenBeacon1Beacon2), -1, -1);
		String key2 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), -1, AngleUtil.toIntAngle(angleBetweenBeacon2Beacon3), -1);
		String key3 = this.generateKey(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), -1, -1, AngleUtil.toIntAngle(angleBetweenBeacon3Beacon1));

		AngleMap locationByAngleBetweenBeacon1Beacon2 = resourceData.angleMapCollection.findOneById(key1);
		AngleMap locationByAngleBetweenBeacon2Beacon3 = resourceData.angleMapCollection.findOneById(key2);
		AngleMap locationByAngleBetweenBeacon3Beacon1 = resourceData.angleMapCollection.findOneById(key3);

		if (locationByAngleBetweenBeacon1Beacon2 == null || locationByAngleBetweenBeacon2Beacon3 == null
				|| locationByAngleBetweenBeacon3Beacon1 == null)
			throw new AngleOutsideBeaconsRangeException();

		Coordinate loc = GeometryUtil.findLocation(beaconLocs[0].getLocation(), beaconLocs[1].getLocation(),
				beaconLocs[2].getLocation(), locationByAngleBetweenBeacon1Beacon2.getCoordinate(),
				locationByAngleBetweenBeacon2Beacon3.getCoordinate(),
				locationByAngleBetweenBeacon3Beacon1.getCoordinate());

		logger.debug(beaconLocs[0].getLocation() + "" + angleBetweenBeacon1Beacon2);
		logger.debug(beaconLocs[1].getLocation() + "" + angleBetweenBeacon2Beacon3);
		logger.debug(beaconLocs[2].getLocation() + "" + angleBetweenBeacon3Beacon1);
		logger.debug("LOC:" + loc);

		return loc;
	}

	public void cacheLocationMaps(Coordinate beacon1Location, Coordinate beacon2Location, Coordinate beacon3Location) {
		try {
			LocationMap[] locationMaps = this.loadLocationMaps(beacon1Location, beacon2Location, beacon3Location);

			for (int angle : locationMaps[0].getCoordinateByAngle().keySet()) {
				String key = this.generateKey(beacon1Location, beacon2Location, beacon3Location, angle, -1, -1);
				if (resourceData.memcachedClient.get(key) == null)
					resourceData.memcachedClient.set(key, cacheDuration,
							locationMaps[0].getCoordinateByAngle().get(angle));
			}
			for (int angle : locationMaps[1].getCoordinateByAngle().keySet()) {
				String key = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, angle, -1);
				if (resourceData.memcachedClient.get(key) == null)
					resourceData.memcachedClient.set(key, cacheDuration,
							locationMaps[1].getCoordinateByAngle().get(angle));
			}
			for (int angle : locationMaps[2].getCoordinateByAngle().keySet()) {
				String key = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, -1, angle);
				if (resourceData.memcachedClient.get(key) == null)
					resourceData.memcachedClient.set(key, cacheDuration,
							locationMaps[2].getCoordinateByAngle().get(angle));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void populateLocationMaps(List<Beacon> beacons) {
		List<BeaconLoc[]> threeBeaconsSet = BeaconService.getBeaconsSetFromBeacons(beacons);
		for (BeaconLoc[] threeBeacons : threeBeaconsSet)
			loadLocationMaps(threeBeacons[0].getLocation(), threeBeacons[1].getLocation(),
					threeBeacons[2].getLocation());
	}

	public LocationMap[] loadLocationMaps(Coordinate beacon1Location, Coordinate beacon2Location,
			Coordinate beacon3Location) {
		String key1 = this.generateKey(beacon1Location, beacon2Location, beacon3Location, 0, -1, -1);
		String key2 = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, 0, -1);
		String key3 = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, -1, 0);

		LocationMap locationMap1 = resourceData.locationMapCollection.findOneById(key1);
		LocationMap locationMap2 = resourceData.locationMapCollection.findOneById(key2);
		LocationMap locationMap3 = resourceData.locationMapCollection.findOneById(key3);

		if (locationMap1 == null || locationMap2 == null || locationMap3 == null) {
			AngleCoordinateMap angleCoordinateMap = new AngleCoordinateMap(beacon1Location, beacon2Location,
					beacon3Location);
			if (locationMap1 == null) {
				locationMap1 = new LocationMap(key1, angleCoordinateMap.getCoordinateByAngleBetweenTip1TailTip2());
				resourceData.locationMapCollection.insert(locationMap1);
			}
			if (locationMap2 == null) {
				locationMap2 = new LocationMap(key2, angleCoordinateMap.getCoordinateByAngleBetweenTip2TailTip3());
				resourceData.locationMapCollection.insert(locationMap2);
			}
			if (locationMap3 == null) {
				locationMap3 = new LocationMap(key3, angleCoordinateMap.getCoordinateByAngleBetweenTip3TailTip1());
				resourceData.locationMapCollection.insert(locationMap3);
			}
		}

		return new LocationMap[] { locationMap1, locationMap2, locationMap3 };
	}

	public String generateKey(Coordinate beacon1Location, Coordinate beacon2Location, Coordinate beacon3Location,
			double angleBetweenBeacon1Beacon2, double angleBetweenBeacon2Beacon3, double angleBetweenBeacon3Beacon1) {
		double[] key = new double[] { beacon1Location.x, beacon1Location.y, beacon2Location.x, beacon2Location.y,
				beacon3Location.x, beacon3Location.y, angleBetweenBeacon1Beacon2, angleBetweenBeacon2Beacon3,
				angleBetweenBeacon3Beacon1 };

		return new JSONArray(key).toString();
	}

	public void updateInternalLocations(Robot robot, List<Beacon> beacons, List<Boundary> boundaries,
			List<Schedule> schedules) {
		List<Coordinate> envelopeCoordinates = getEnvelopeCoordinates(beacons, boundaries);

		Envelope envelope = new GeometryFactory()
				.createLineString(envelopeCoordinates.toArray(new Coordinate[envelopeCoordinates.size()]))
				.getEnvelopeInternal();
		double minX = envelope.getMinX();
		double minY = envelope.getMinY();

		populateInternalLocations(robot, beacons, boundaries, schedules, minX, minY);
	}

	public static List<Coordinate> getEnvelopeCoordinates(List<Beacon> beacons, List<Boundary> boundaries) {
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		addBeaconsToEnvelopeCoordinates(beacons, coordinates);
		addBoundariesToEnvelopeCoordinates(boundaries, coordinates);
		return coordinates;
	}

	private static void addBoundariesToEnvelopeCoordinates(List<Boundary> boundaries, List<Coordinate> coordinates) {
		for (Boundary boundary : boundaries) {
			List<Coordinate> extLocs = boundary.getExternalBoundaryPoints();
			for (Coordinate extLoc : extLocs) {
				// Make the internal location as long (just in case Coordinate
				// is implemented in house for a better performance)
				extLoc.x *= GeometryUtil.coordinateConversion;
				extLoc.y *= GeometryUtil.coordinateConversion;
				coordinates.add(extLoc);
			}
		}
	}

	private static void addBeaconsToEnvelopeCoordinates(List<Beacon> beacons, List<Coordinate> coordinates) {
		for (Beacon beacon : beacons) {
			Coordinate extLoc = beacon.getExternalLocation();
			// Make the internal location as long (just in case Coordinate is
			// implemented in house for a better performance)
			extLoc.x *= GeometryUtil.coordinateConversion;
			extLoc.y *= GeometryUtil.coordinateConversion;
			coordinates.add(extLoc);
		}
	}

	private void populateInternalLocations(Robot robot, List<Beacon> beacons, List<Boundary> boundaries,
			List<Schedule> schedules, double minX, double minY) {
		updateBeaconsInternalLocations(beacons, minX, minY);
		updateBoundariesInternalLocations(boundaries, minX, minY);
		updateScheduleRoute(robot, schedules);
		populateAngleMaps(beacons);
		// populateDistanceMaps(beacons);
	}

	private void updateBeaconsInternalLocations(List<Beacon> beacons, double minX, double minY) {
		for (Beacon beacon : beacons) {
			resourceData.beaconCollection.updateById(beacon.getId(),
					DBUpdate.set("location", new Coordinate((long) Math.round(beacon.getExternalLocation().x - minX),
							(long) Math.round(beacon.getExternalLocation().y - minY))));
		}
	}

	private void updateBoundariesInternalLocations(List<Boundary> boundaries, double minX, double minY) {
		for (Boundary boundary : boundaries) {
			List<Coordinate> extLocs = boundary.getExternalBoundaryPoints();
			List<Coordinate> intLocs = new ArrayList<Coordinate>();
			for (Coordinate extLoc : extLocs)
				intLocs.add(new Coordinate((long) Math.round(extLoc.x - minX), (long) Math.round(extLoc.y - minY)));

			resourceData.boundaryCollection.updateById(boundary.getId(), DBUpdate.set("boundaryPoints", intLocs));
		}
	}

	private void updateScheduleRoute(Robot robot, List<Schedule> schedules) {
		for (Schedule schedule : schedules) {
			Boundary boundary = resourceData.boundaryCollection.findOneById(schedule.getBoundary());
			if (boundary != null) {
				List<Coordinate> boundaryPoints = boundary.getBoundaryPoints();
				if (boundaryPoints != null && !boundaryPoints.isEmpty()) {
					// Points of LinearRing needs form a closed linestring
					boundaryPoints.add(boundaryPoints.get(0));
					List<Coordinate> route = GeometryUtil.getRouteToFill(
							boundaryPoints.toArray(new Coordinate[boundaryPoints.size()]), robot.getCoverageDiameter());
					resourceData.scheduleCollection.updateById(schedule.getId(), DBUpdate.set("route", route));
				}
			}
		}
	}

	private void populateAngleMaps(List<Beacon> beacons) {
		List<BeaconLoc[]> threeBeaconsSet = BeaconService.getBeaconsSetFromBeacons(beacons);
		for (BeaconLoc[] threeBeacons : threeBeaconsSet) {
			populateAngleMaps(threeBeacons[0].getLocation(), threeBeacons[1].getLocation(),
					threeBeacons[2].getLocation());
			populateAngleMaps(threeBeacons[1].getLocation(), threeBeacons[2].getLocation(),
					threeBeacons[0].getLocation());
			populateAngleMaps(threeBeacons[2].getLocation(), threeBeacons[0].getLocation(),
					threeBeacons[1].getLocation());
		}
	}

	private void populateAngleMaps(Coordinate beacon1Location, Coordinate beacon2Location, Coordinate beacon3Location) {
		AngleCoordinateMap angleCoordinateMap = new AngleCoordinateMap(beacon1Location, beacon2Location,
				beacon3Location);

		for (Entry<Integer, Coordinate> entry : angleCoordinateMap.getCoordinateByAngleBetweenTip1TailTip2()
				.entrySet()) {
			int angle = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, angle, -1, -1);
			resourceData.angleMapCollection.insert(new AngleMap(id, coordinate));
		}

		for (Entry<Integer, Coordinate> entry : angleCoordinateMap.getCoordinateByAngleBetweenTip2TailTip3()
				.entrySet()) {
			int angle = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, angle, -1);
			resourceData.angleMapCollection.insert(new AngleMap(id, coordinate));
		}

		for (Entry<Integer, Coordinate> entry : angleCoordinateMap.getCoordinateByAngleBetweenTip3TailTip1()
				.entrySet()) {
			int angle = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, -1, angle);
			resourceData.angleMapCollection.insert(new AngleMap(id, coordinate));
		}
	}

	private void populateDistanceMaps(List<Beacon> beacons) {
		List<BeaconLoc[]> threeBeaconsSet = BeaconService.getBeaconsSetFromBeacons(beacons);
		for (BeaconLoc[] threeBeacons : threeBeaconsSet)
			populateDistanceMaps(threeBeacons[0].getLocation(), threeBeacons[1].getLocation(),
					threeBeacons[2].getLocation());
	}

	private void populateDistanceMaps(Coordinate beacon1Location, Coordinate beacon2Location,
			Coordinate beacon3Location) {
		DistanceCoordinateMap distanceCoordinateMap = new DistanceCoordinateMap(beacon1Location, beacon2Location,
				beacon3Location);

		for (Entry<Long, Coordinate> entry : distanceCoordinateMap.getCoordinateByDistanceBetweenTip1TailTip2()
				.entrySet()) {
			long distance = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, distance, -1, -1);
			resourceData.distanceMapCollection.insert(new DistanceMap(id, coordinate));
		}

		for (Entry<Long, Coordinate> entry : distanceCoordinateMap.getCoordinateByDistanceBetweenTip2TailTip3()
				.entrySet()) {
			long distance = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, distance, -1);
			resourceData.distanceMapCollection.insert(new DistanceMap(id, coordinate));
		}

		for (Entry<Long, Coordinate> entry : distanceCoordinateMap.getCoordinateByDistanceBetweenTip3TailTip1()
				.entrySet()) {
			long distance = entry.getKey();
			Coordinate coordinate = entry.getValue();

			String id = this.generateKey(beacon1Location, beacon2Location, beacon3Location, -1, -1, distance);
			resourceData.distanceMapCollection.insert(new DistanceMap(id, coordinate));
		}
	}
}
