package com.airobotics.commandcenter.resources;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.airobotics.api.entities.Schedule;
import com.airobotics.api.entities.ScheduleType;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.db.ResourceHelper;
import com.codahale.metrics.annotation.Timed;

import net.vz.mongodb.jackson.DBUpdate;

@Path("/api/v1/schedules")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class ScheduleResource {
	private ResourceData resourceData;

	public ScheduleResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Path("/{id}")
	@Timed
	public Schedule getSchedule(@PathParam("id") String id) {
		Schedule result = resourceData.scheduleCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@POST
	@Timed
	public Response createSchedule(/* @Auth User user, */
			@Valid Schedule schedule) {
		net.vz.mongodb.jackson.WriteResult<Schedule, String> result = resourceData.scheduleCollection.insert(schedule);
		return Response.status(Response.Status.CREATED).entity(result.getSavedObject()).build();
	}

	@PUT
	@Timed
	public Response updateSchedule(/* @Auth User user, */
			@Valid Schedule upated) {
		net.vz.mongodb.jackson.WriteResult<Schedule, String> result = resourceData.scheduleCollection
				.updateById(upated.getId(), upated);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}")
	@Timed
	public Response deleteSchedule(/* @Auth User user, */
			@PathParam("id") String id) {
		net.vz.mongodb.jackson.WriteResult<Schedule, String> result = resourceData.scheduleCollection.removeById(id);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@PUT
	@Path("/{id}/active")
	@Timed
	public Response activateSchedule(@PathParam("id") String id) {
		resourceData.scheduleCollection.updateById(id, DBUpdate.set("type", ScheduleType.ACTIVE.ordinal()));
		return Response.status(Response.Status.OK).entity(resourceData.scheduleCollection.findOneById(id)).build();
	}

	@PUT
	@Path("/{id}/inactive")
	@Timed
	public Response deactivateSchedule(@PathParam("id") String id) {
		resourceData.scheduleCollection.updateById(id, DBUpdate.set("type", ScheduleType.INACTIVE.ordinal()));
		return Response.status(Response.Status.OK).entity(resourceData.scheduleCollection.findOneById(id)).build();
	}

	@GET
	@Path("/{id}/average-runtime")
	@Timed
	public int getScheduleAverageRuntime(@PathParam("id") String id) {
		Schedule schedule = resourceData.scheduleCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(schedule);

		int retVal = 43200;
		// TODO: Calculate average runtime
		// retVal = scheduleInstanceCollection.find("avg completed - started",
		// schedule.getInstances())

		return retVal;
	}
}
