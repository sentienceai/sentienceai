package com.airobotics.commandcenter.auth;

import com.airobotics.api.entities.User;
import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
//import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;

public class SimpleAuthenticator {//implements Authenticator<BasicCredentials, User> {
    private JacksonDBCollection<User, String> collection;
    
	public SimpleAuthenticator(JacksonDBCollection<User, String> users) {
        this.collection = users;
    }
	
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
    	User user = collection.findOne(DBQuery.is("userName", credentials.getUsername()));
        if (user != null && credentials.getPassword().equals(user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.absent();
    }
}