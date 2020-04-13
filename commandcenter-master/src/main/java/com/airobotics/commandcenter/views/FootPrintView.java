package com.airobotics.commandcenter.views;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import io.dropwizard.views.View;

public class FootPrintView extends View {
	private final String id;
	private final Point centroid;
	private final List<Point> boundaryPoints;
	private final List<Point> beacons;
	private final List<Point> route;

	@SuppressWarnings("serial")
	public FootPrintView(String view, String id, final List<Coordinate> boundaryPoints, final List<Coordinate> beacons,
			final List<Coordinate> route) {
		super(view);
		this.id = id;
		this.centroid = new GeometryFactory()
				.createLineString(boundaryPoints.toArray(new Coordinate[boundaryPoints.size()])).getCentroid();
		this.boundaryPoints = new ArrayList<Point>() {
			{
				for (Coordinate boundary : boundaryPoints)
					add(new GeometryFactory().createPoint(boundary));
				add(new GeometryFactory().createPoint(boundaryPoints.get(0)));
			}
		};
		this.beacons = new ArrayList<Point>() {
			{
				for (Coordinate beacon : beacons)
					add(new GeometryFactory().createPoint(beacon));
				add(new GeometryFactory().createPoint(beacons.get(0)));
			}
		};
		this.route = new ArrayList<Point>() {
			{
				for (Coordinate routePoint : route)
					add(new GeometryFactory().createPoint(routePoint));
			}
		};
	}

	public String getId() {
		return id;
	}

	public List<Point> getBoundaryPoints() {
		return boundaryPoints;
	}

	public Point getCentroid() {
		return centroid;
	}

	public List<Point> getBeacons() {
		return beacons;
	}

	public List<Point> getRoute() {
		return route;
	}
}