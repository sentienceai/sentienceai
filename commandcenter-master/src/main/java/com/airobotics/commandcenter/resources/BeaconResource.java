package com.airobotics.commandcenter.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconScan;
import com.airobotics.api.entities.BeaconState;
import com.airobotics.commandcenter.api.Key;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.db.ResourceHelper;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.MongoException;

import net.rubyeye.xmemcached.exception.MemcachedException;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;

@Path("/api/v1/beacons")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class BeaconResource {
	private ResourceData resourceData;
	private static int cacheDuration = 43200; // 12 hours
	private static int cacheDurationForAnglesCollectedFromBeacons = 60; // 1 min

	public BeaconResource(ResourceData resourceData) {
		this.resourceData = resourceData;
		cacheBeacons();
	}

	private void cacheBeacons() {
		try {
			DBCursor<Beacon> dbCursor = resourceData.beaconCollection.find();
			while (dbCursor.hasNext()) {
				Beacon beacon = dbCursor.next();
				resourceData.memcachedClient.set(Key.beacon_internal_location_key + beacon.getId(), 0,
						beacon.getLocation());
				resourceData.memcachedClient.set(Key.beacon_external_location_key + beacon.getId(), 0,
						beacon.getExternalLocation());
			}
		} catch (MongoException | TimeoutException | InterruptedException | MemcachedException e) {
			// TODO Auto-generated catch block
		}
	}

	@GET
	@Path("/{id}")
	@Timed
	public Beacon getBeacon(@PathParam("id") String id) {
		Beacon result = resourceData.beaconCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@GET
	@Timed
	public Beacon getBeaconBySerialNumber(@QueryParam("serialNo") String serialNumber) {
		Beacon result = resourceData.beaconCollection.findOne(DBQuery.is("serialNumber", serialNumber));
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@POST
	@Timed
	public Response createBeacon(/* @Auth User user, */
			@Valid Beacon beacon) {
		net.vz.mongodb.jackson.WriteResult<Beacon, String> result = resourceData.beaconCollection.insert(beacon);
		return Response.status(Response.Status.CREATED).entity(result.getSavedObject()).build();
	}

	@PUT
	@Timed
	public Response updateBeacon(/* @Auth User user, */
			@Valid Beacon upated) {
		net.vz.mongodb.jackson.WriteResult<Beacon, String> result = resourceData.beaconCollection
				.updateById(upated.getId(), upated);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}")
	@Timed
	public Response deleteBeacon(/* @Auth User user, */
			@PathParam("id") String id) {
		net.vz.mongodb.jackson.WriteResult<Beacon, String> result = resourceData.beaconCollection.removeById(id);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@POST
	@Path("/scan-order/serial-no")
	@Timed
	public Response setBeaconScanOrderBySerialNumbers(/* @Auth User user, */
			@Valid final List<String> serialNumbers) throws JsonParseException, JsonMappingException, IOException,
			TimeoutException, InterruptedException, MemcachedException {
		@SuppressWarnings("serial")
		List<String> beaconIds = new ArrayList<String>() {
			{
				for (String serialNo : serialNumbers)
					add(getBeaconBySerialNumber(serialNo).getId());
			}
		};
		return setBeaconScanOrder(beaconIds);
	}

	@SuppressWarnings("unused")
	@POST
	@Path("/scan-order")
	@Timed
	public Response setBeaconScanOrder(/* @Auth User user, */
			@Valid List<String> beaconIds) throws JsonParseException, JsonMappingException, IOException,
			TimeoutException, InterruptedException, MemcachedException {
		BeaconScan beaconScan = getBeaconScan(beaconIds);
		net.vz.mongodb.jackson.WriteResult<BeaconScan, String> result = (beaconScan == null)
				? resourceData.beaconScanCollection.insert(new BeaconScan(beaconIds))
				: resourceData.beaconScanCollection.updateById(beaconScan.getId(), beaconScan);
		cacheScanOrder(beaconScan.getOrder());
		resourceData.memcachedClient.set(Key.beacon_list_key, 0, beaconIds);
		return Response.status(Response.Status.OK).entity(beaconScan).build();
	}

	private void cacheScanOrder(List<String> scanOrder)
			throws TimeoutException, InterruptedException, MemcachedException {
		if (scanOrder.size() > 1)
			for (int i = 0; i < scanOrder.size(); i++) {
				String nextBeacon = (i == scanOrder.size() - 1) ? scanOrder.get(0) : scanOrder.get(i + 1);
				resourceData.memcachedClient.set(Key.beacon_scan_order_key + scanOrder.get(i), cacheDuration,
						nextBeacon);
			}
		if (scanOrder.size() > 0)
			resourceData.memcachedClient.set(Key.beacon_current_scan_key, cacheDuration, scanOrder.get(0));
	}

	private BeaconScan getBeaconScan(List<String> beaconIds) {
		DBCursor<BeaconScan> dbCursor = resourceData.beaconScanCollection.find();
		BeaconScan beaconScan = null;
		try {
			while (dbCursor.hasNext()) {
				beaconScan = dbCursor.next();
				beaconScan.setOrder(beaconIds);
			}
		} catch (MongoException e) {
			// TODO Auto-generated catch block
		}
		return beaconScan;
	}

	@GET
	@Path("/{id}/state")
	@Timed
	public BeaconState isTimeToStartScan(@PathParam("id") String id)
			throws TimeoutException, InterruptedException, MemcachedException {
		String beaconIdToStartScan = resourceData.memcachedClient.get(Key.beacon_current_scan_key);
		BeaconState beaconState = new BeaconState();
		if (id.equals(beaconIdToStartScan))
			beaconState.isRunning = true;
		return beaconState;
	}

	@PUT
	@Path("/{id}/state")
	@Timed
	public Response setScanCompleted(/* @Auth User user, */
			@PathParam("id") String id, @Valid BeaconState beaconState)
			throws TimeoutException, InterruptedException, MemcachedException {
		String beaconIdToStartScan = resourceData.memcachedClient.get(Key.beacon_scan_order_key + id);
		resourceData.memcachedClient.set(Key.beacon_current_scan_key, cacheDuration, beaconIdToStartScan);
		return Response.status(Response.Status.OK).entity(beaconState).build();
	}

	@POST
	@Path("/{id}/state")
	@Timed
	public Response setDeviceAngle(/* @Auth User user, */
			@PathParam("id") String id, @Valid BeaconState beaconState)
			throws TimeoutException, InterruptedException, MemcachedException {
		resourceData.memcachedClient.set(beaconState.deviceId + ":" + id, cacheDurationForAnglesCollectedFromBeacons,
				beaconState.angle);
		return Response.status(Response.Status.OK).entity(beaconState).build();
	}
}
