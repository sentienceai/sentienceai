package com.airobotics.commandcenter.db;

import io.dropwizard.lifecycle.Managed;

import com.mongodb.Mongo;

public class MongoManaged implements Managed {
    
    private Mongo mongo;
    
    public MongoManaged(Mongo mongo) {
        this.mongo = mongo;
    }

    //@Override
    public void start() throws Exception {

    }

    //@Override
    public void stop() throws Exception {
        mongo.close();
    }

}
