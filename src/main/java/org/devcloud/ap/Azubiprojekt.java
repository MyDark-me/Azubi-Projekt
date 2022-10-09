package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.utils.SQLPostgres;
import java.io.IOException;

import org.devcloud.ap.utils.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Azubiprojekt {

    private static final Logger logger = LoggerFactory.getLogger(Azubiprojekt.class);
    
    public static void main(String[] args) throws IOException {
        logger.info("Starting Azubiprojekt");
        HTTPServer.startServer();
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");

    }
}
