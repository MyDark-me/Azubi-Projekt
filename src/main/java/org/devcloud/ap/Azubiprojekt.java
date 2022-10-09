package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.utils.SQLPostgres;
import java.io.IOException;

import org.devcloud.ap.utils.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;
    private static final Logger logger = LoggerFactory.getLogger(Azubiprojekt.class);
    
    public static void main(String[] args) throws IOException {
        logger.info("Starting Azubiprojekt Server");
        HTTPServer.startServer();
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");

    }
}
