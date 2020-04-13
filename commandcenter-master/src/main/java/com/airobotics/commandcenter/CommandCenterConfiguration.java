package com.airobotics.commandcenter;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CommandCenterConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty @NotEmpty
    public String mongohost = System.getenv("OPENSHIFT_MONGODB_DB_HOST") == null ? "localhost" : System.getenv("OPENSHIFT_MONGODB_DB_HOST");
    
    @JsonProperty @Min(1) @Max(65535)
    public int mongoport = System.getenv("OPENSHIFT_MONGODB_DB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
    
    @JsonProperty @NotEmpty
    public String mongodb = System.getenv("OPENSHIFT_APP_NAME") == null ? "airobotics" : System.getenv("OPENSHIFT_APP_NAME");

    @JsonProperty @NotEmpty
    public String memcachedhosts = System.getenv("OPENSHIFT_MEMCACHED_HOSTS") == null ? "localhost:11211" : System.getenv("OPENSHIFT_MEMCACHED_HOSTS");

    @JsonProperty @Min(1) @Max(65535)
    public int memcachedcons = System.getenv("OPENSHIFT_MEMCACHED_CONN_POOL_SIZE") == null ? 2 : Integer.parseInt(System.getenv("OPENSHIFT_MEMCACHED_CONN_POOL_SIZE"));
}
