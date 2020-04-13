package com.airobotics.commandcenter.resources;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.api.entities.Request;
import com.airobotics.commandcenter.api.Key;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.api.cache.RobotVal;
import com.airobotics.commandcenter.core.BeaconService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;

@Path("/api/v1/robots/{id}/simulations")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class SimulationResource {
	final static Logger logger = LoggerFactory.getLogger(SimulationResource.class);
	private ResourceData resourceData;

	public SimulationResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Timed
	public Request getRequest(@PathParam("id") String id,
			@QueryParam("beacons") String beaconsJson) {
		Request req = new Request();
		List<Beacon> beacons = null;
		try {
			beacons = new ObjectMapper().readValue(beaconsJson, new TypeReference<List<Beacon>>(){});
			RobotVal robotVal = resourceData.memcachedClient.get(Key.robot_val_key + id);
			int locationToBeIdx = robotVal.getRouteIdx() + 1;
			Coordinate locationToBe = resourceData.memcachedClient.get(Key.schedule_working_route_key + id + "." + locationToBeIdx);
			logger.debug("locationToBe: " + locationToBe);
			LinkedList<BeaconSensor> beaconSensors = BeaconService.getBeaconSensors(beacons, locationToBe);
			req.setBeaconSensors(beaconSensors);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return req;
	}
}
