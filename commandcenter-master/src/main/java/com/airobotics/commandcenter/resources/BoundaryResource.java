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

import com.airobotics.api.entities.Boundary;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.db.ResourceHelper;
import com.codahale.metrics.annotation.Timed;

@Path("/api/v1/boundaries")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class BoundaryResource {
	private ResourceData resourceData;

	public BoundaryResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Path("/{id}")
	@Timed
	public Boundary getBoundary(@PathParam("id") String id) {
		Boundary result = resourceData.boundaryCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@POST
	@Timed
	public Response createBoundary(/* @Auth User user, */
			@Valid Boundary boundary) {
		net.vz.mongodb.jackson.WriteResult<Boundary, String> result = resourceData.boundaryCollection.insert(boundary);
		return Response.status(Response.Status.CREATED).entity(result.getSavedObject()).build();
	}

	@PUT
	@Timed
	public Response updateBoundary(/* @Auth User user, */
			@Valid Boundary upated) {
		return Response.status(Response.Status.MOVED_PERMANENTLY)
				.header("Location", "/api/v1/robots/{id}/schedules/{schedule_id}/boundaries").build();
	}

	@DELETE
	@Path("/{id}")
	@Timed
	public Response deleteBoundary(/* @Auth User user, */
			@PathParam("id") String id) {
		net.vz.mongodb.jackson.WriteResult<Boundary, String> result = resourceData.boundaryCollection.removeById(id);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}
}
