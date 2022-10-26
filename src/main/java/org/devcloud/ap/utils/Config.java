package org.devcloud.ap.utils;

import io.sentry.Sentry;
import org.devcloud.ap.Azubiprojekt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class Config {
    static Logger logger = LoggerFactory.getLogger(Config.class);
    Properties props = new Properties();

    public void initConfig() {
        try {
            props.load(Azubiprojekt.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    public void getProperties(){
        logger.info("Config is imported!");

    }
}
