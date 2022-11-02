package org.devcloud.ap;

import io.sentry.Sentry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.devcloud.ap.utils.SQLPostgres;

import java.io.IOException;

import org.devcloud.ap.utils.HTTPServer;
import org.devcloud.ap.utils.apihelper.databsehelper.RoleDatabaseHelper;
import org.devcloud.ap.utils.SentryLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Azubiprojekt extends Application  {

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

        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Azubiprojekt.class.getResource("/frames/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 900);
        stage.setTitle("Azubiprojekt");
        stage.setScene(scene);
        stage.show();
    }
}
