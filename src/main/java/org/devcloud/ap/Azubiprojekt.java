package org.devcloud.ap;

import org.devcloud.ap.utils.HTTPServer;
import org.slf4j.Logger;

import java.io.IOException;

public class Azubiprojekt {
    public static Logger logger;
    public static void main(String[] args) throws IOException {
        logger = org.slf4j.LoggerFactory.getLogger(Azubiprojekt.class);
        logger.info("Starting Azubiprojekt");
        HTTPServer.startServer();
    }
}
