package com.airobotics.commandcenter;

import java.io.IOException;
import java.net.UnknownHostException;

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
import com.airobotics.commandcenter.api.ResourceData;
import com.airobotics.commandcenter.db.MongoManaged;
import com.airobotics.commandcenter.health.MongoHealthCheck;
import com.airobotics.commandcenter.resources.BeaconResource;
import com.airobotics.commandcenter.resources.BoundaryResource;
import com.airobotics.commandcenter.resources.InstructionResource;
import com.airobotics.commandcenter.resources.RobotResource;
import com.airobotics.commandcenter.resources.ScheduleResource;
import com.airobotics.commandcenter.resources.SimulationResource;
import com.airobotics.commandcenter.resources.UserResource;
import com.airobotics.commandcenter.resources.views.FootprintResource;
import com.mongodb.DB;
import com.mongodb.Mongo;
//import com.yunspace.dropwizard.xml.XmlBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
//import io.dropwizard.auth.basic.BasicAuthProvider;
//import io.dropwizard.auth.AuthFactory;
//import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import net.vz.mongodb.jackson.JacksonDBCollection;

public class CommandCenterApplication extends Application<CommandCenterConfiguration> {
	public static void main(String[] args) throws Exception {
		new CommandCenterApplication().run(args);
	}

	/*
	 * private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
	 * new HibernateBundle<HelloWorldConfiguration>(Person.class) {
	 * 
	 * @Override public DataSourceFactory
	 * getDataSourceFactory(HelloWorldConfiguration configuration) { return
	 * configuration.getDataSourceFactory(); } };
	 */
	@Override
	public String getName() {
		return "AI Robotics Command Center";
	}

	@Override
	public void initialize(Bootstrap<CommandCenterConfiguration> bootstrap) {
		// final XmlBundle xmlBundle = new XmlBundle();
		// xmlBundle.getXmlMapper()
		// .enable(SerializationFeature.INDENT_OUTPUT)
		// .setSerializationInclusion(Include.NON_NULL)
		// .registerModule(new Hibernate4Module());
		// bootstrap.addBundle(xmlBundle);

		bootstrap.addBundle(new MigrationsBundle<CommandCenterConfiguration>() {
			public DataSourceFactory getDataSourceFactory(CommandCenterConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		// bootstrap.addBundle(hibernateBundle);
		bootstrap.addBundle(new AssetsBundle("/webapp"));
		bootstrap.addBundle(new ViewBundle<CommandCenterConfiguration>());
	}

	@Override
	public void run(CommandCenterConfiguration configuration, Environment environment)
			throws UnknownHostException, IOException {
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(configuration.memcachedhosts));
		builder.setCommandFactory(new BinaryCommandFactory());
		builder.setConnectionPoolSize(configuration.memcachedcons);
		MemcachedClient memcachedClient = builder.build();

		Mongo mongo = new Mongo(configuration.mongohost, configuration.mongoport);
		DB db = mongo.getDB(configuration.mongodb);

		MongoManaged mongoManaged = new MongoManaged(mongo);
		environment.lifecycle().manage(mongoManaged);
		environment.healthChecks().register("mongo", new MongoHealthCheck(mongo));

		JacksonDBCollection<User, String> users = JacksonDBCollection.wrap(db.getCollection("users"), User.class,
				String.class);
		JacksonDBCollection<Robot, String> robots = JacksonDBCollection.wrap(db.getCollection("robots"), Robot.class,
				String.class);
		JacksonDBCollection<Beacon, String> beacons = JacksonDBCollection.wrap(db.getCollection("beacons"),
				Beacon.class, String.class);
		JacksonDBCollection<BeaconScan, String> beaconScans = JacksonDBCollection.wrap(db.getCollection("beaconScans"),
				BeaconScan.class, String.class);
		JacksonDBCollection<Schedule, String> schedules = JacksonDBCollection.wrap(db.getCollection("schedules"),
				Schedule.class, String.class);
		JacksonDBCollection<ScheduleInstance, String> scheduleInstances = JacksonDBCollection
				.wrap(db.getCollection("scheduleInstances"), ScheduleInstance.class, String.class);
		JacksonDBCollection<LocationMap, String> locationMaps = JacksonDBCollection
				.wrap(db.getCollection("locationMaps"), LocationMap.class, String.class);
		JacksonDBCollection<Boundary, String> boundaries = JacksonDBCollection.wrap(db.getCollection("boundaries"),
				Boundary.class, String.class);
		JacksonDBCollection<AngleMap, String> angleMaps = JacksonDBCollection.wrap(db.getCollection("angleMaps"),
				AngleMap.class, String.class);
		JacksonDBCollection<Footprint, String> footprints = JacksonDBCollection.wrap(db.getCollection("footprints"),
				Footprint.class, String.class);
		JacksonDBCollection<DistanceMap, String> distanceMaps = JacksonDBCollection
				.wrap(db.getCollection("distanceMaps"), DistanceMap.class, String.class);

		// environment.jersey().register(new BasicAuthProvider<User>(new
		// SimpleAuthenticator(users),
		// "SUPER SECRET STUFF"));
		ResourceData resourceData = new ResourceData(memcachedClient, users, robots, beacons, beaconScans, locationMaps,
				schedules, scheduleInstances, boundaries, angleMaps, footprints, distanceMaps);
		environment.jersey().register(new UserResource(resourceData));
		environment.jersey().register(new RobotResource(resourceData));
		environment.jersey().register(new BeaconResource(resourceData));
		environment.jersey().register(new ScheduleResource(resourceData));
		environment.jersey().register(new InstructionResource(resourceData));
		environment.jersey().register(new BoundaryResource(resourceData));
		environment.jersey().register(new SimulationResource(resourceData));
		environment.jersey().register(new FootprintResource(resourceData));
	}
}
