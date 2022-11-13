package org.devcloud.ap;

import io.sentry.Sentry;
import lombok.Getter;
import org.devcloud.ap.ui.PanelLoad;
import org.devcloud.ap.utils.HTTPServer;
import org.devcloud.ap.utils.SQLPostgres;
import org.devcloud.ap.utils.SentryLogger;

import org.devcloud.ap.utils.apihelper.databsehelper.RoleDatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;
    private static final Logger logger = LoggerFactory.getLogger(Azubiprojekt.class);
    
    public static void main(String[] args) {
        SentryLogger.startSentry();
        logger.info("Starting Azubiprojekt Server");
        try {
            HTTPServer.startServer();
        } catch (IOException e) {
            Sentry.captureException(e);
        }
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");
        RoleDatabaseHelper.autoCreate(logger);

        PanelLoad.main(args);
    }
}
