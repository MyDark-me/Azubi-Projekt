package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.utils.SQLPostgres;
import java.io.IOException;

import org.devcloud.ap.utils.HTTPServer;
import org.devcloud.ap.utils.SentryLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;
    private static final Logger logger = LoggerFactory.getLogger(Azubiprojekt.class);
    
    public static void main(String[] args) {
        SentryLogger.startSentry();
        logger.info("Starting Azubiprojekt Server");
        try {
            HTTPServer.startServer();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");
    }
}
