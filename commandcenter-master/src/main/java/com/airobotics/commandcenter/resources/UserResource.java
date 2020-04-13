package com.airobotics.commandcenter.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.User;
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.db.ResourceHelper;
import com.codahale.metrics.annotation.Timed;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBUpdate;

@Path("/api/v1/users")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class UserResource {
	private ResourceData resourceData;

	public UserResource(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	@GET
	@Timed
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		DBCursor<User> dbCursor = resourceData.userCollection.find();
		while (dbCursor.hasNext()) {
			User user = dbCursor.next();
			users.add(user);
		}
		ResourceHelper.notFoundIfNull(users);

		return users;
	}

	@GET
	@Path("/{id}")
	@Timed
	public User getUser(@PathParam("id") String id) {
		User result = resourceData.userCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(result);

		return result;
	}

	@POST
	@Timed
	public Response createUser(/* @Auth User user, */
			@Valid User user) {
		net.vz.mongodb.jackson.WriteResult<User, String> result = resourceData.userCollection.insert(user);
		return Response.status(Response.Status.CREATED).entity(result.getSavedObject()).build();
	}

	@PUT
	@Timed
	public Response updateUser(/* @Auth User user, */
			@Valid User upated) {
		net.vz.mongodb.jackson.WriteResult<User, String> result = resourceData.userCollection.updateById(upated.getId(),
				upated);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}")
	@Timed
	public Response deleteUser(/* @Auth User user, */
			@PathParam("id") String id) throws IOException {
		for (Robot robot : getRobots(id))
			new RobotResource(resourceData).deleteRobot(robot.getId());

		net.vz.mongodb.jackson.WriteResult<User, String> result = resourceData.userCollection.removeById(id);
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@GET
	@Path("/{id}/robots")
	@Timed
	public List<Robot> getRobots(@PathParam("id") String id) {
		List<Robot> robots = new ArrayList<Robot>();

		User user = resourceData.userCollection.findOneById(id);
		ResourceHelper.notFoundIfNull(user);

		for (String robotId : user.getRobots()) {
			Robot robot = resourceData.robotCollection.findOneById(robotId);
			if (robot != null)
				robots.add(robot);
		}

		return robots;
	}

	@POST
	@Path("/{id}/robots/{robot_id}")
	@Timed
	public Response addRobot(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("robot_id") String robotId) {
		resourceData.userCollection.updateById(id, DBUpdate.addToSet("robots", robotId));
		return Response.status(Response.Status.CREATED).entity(resourceData.userCollection.findOneById(id)).build();
	}

	@POST
	@Path("/{id}/robots")
	@Timed
	public Response addRobot(/* @Auth User user, */
			@PathParam("id") String id, @Valid Robot robot) {
		net.vz.mongodb.jackson.WriteResult<Robot, String> result = resourceData.robotCollection.insert(robot);
		resourceData.userCollection.updateById(id, DBUpdate.addToSet("robots", result.getSavedId()));
		return Response.status(Response.Status.OK).entity(result.getSavedObject()).build();
	}

	@DELETE
	@Path("/{id}/robots/{robot_id}")
	@Timed
	public Response removeRobot(/* @Auth User user, */
			@PathParam("id") String id, @PathParam("robot_id") String robotId) {
		resourceData.userCollection.updateById(id, DBUpdate.pull("robots", robotId));
		return Response.status(Response.Status.OK).entity(resourceData.userCollection.findOneById(id)).build();
	}
}
