package com.airobotics.commandcenter.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.Mongo;

public class MongoHealthCheck extends HealthCheck {

    private Mongo mongo;

    public MongoHealthCheck(Mongo mongo) {
        super();
        this.mongo = mongo;
    }

    @Override
    protected Result check() throws Exception {
        mongo.getDatabaseNames();
        return Result.healthy("MongoDB is running... :)");
    }

}
