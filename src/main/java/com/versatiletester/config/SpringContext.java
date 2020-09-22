package com.versatiletester.config;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * '@PostConstruct' and '@PreDestroy' run in the setup and teardown of each thread which is reused by tests,
 * so cannot be used as 'BeforeSuite'/'AfterSuite' functionality.
 */
@ComponentScan("com.versatiletester")
@PropertySource({"classpath:config/application.properties"})
public class SpringContext {
    private static final Logger log = Logger.getLogger(SpringContext.class);

    public static final String PROJECT_NAME_PROPERTY_NAME = "project.name";
    public static final String MAVEN_PROFILE_PROPERTY_NAME = "maven.profile";
    public static final String LOCAL_ID_PROPERTY_NAME = "local.id";
    public static final String BUILD_NUM_PROPERTY_NAME = "build";
    public static final String BROWSER_PROPERTY_NAME = "browser";
    public static final String ENVIRONMENT_PROPERTY_NAME = "environment";
    public static final String GRID_URL_PROPERTY_NAME = "grid.url";
    public static final String BSTACK_URL_PROPERTY_NAME = "browserstack.url";
    public static final String BSTACK_LOCAL_BOOL_PROPERTY_NAME = "browserstack.local";
    public static final String RESOURCES_DIR_PROPERTY_NAME = "resources.dir";

    @Autowired
    public Environment env;
    private String propertyPrefix;

    @PostConstruct
    private void postConstruct() {
        Preconditions.checkArgument(env.getProperty(ENVIRONMENT_PROPERTY_NAME) != null);
        this.propertyPrefix = env.getProperty(ENVIRONMENT_PROPERTY_NAME) + ".";
    }

    public String getEnvProperty(String propertyName) {
        if (env.getProperty(propertyName) != null) {
            return env.getProperty(propertyName);
        } else if (env.getProperty(propertyPrefix + propertyName) != null) {
            return env.getProperty(propertyPrefix + propertyName);
        } else if (System.getProperty(propertyName) != null) {
            return System.getProperty(propertyName);
        }
        return null;
    }


}
