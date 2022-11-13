package org.devcloud.ap.ui;

import io.sentry.Sentry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.devcloud.ap.Azubiprojekt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class PanelLoad extends Application {

    @Getter @Setter private Scene scene;
    @Getter private FXMLLoader fxmlLoader;
    @Getter private PanelController panelController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        fxmlLoader = new FXMLLoader(Azubiprojekt.class.getResource("/frames/main.fxml"));
        scene = new Scene(fxmlLoader.load(), 1000, 900);
        stage.setTitle("Azubiprojekt");
        stage.setScene(scene);
        stage.show();
        panelController = fxmlLoader.getController();

        panelController.setDatabaseDefaults("localhost", "3306", "root", "devcloud");
        panelController.update();
        // Starte alle Methoden mit IController Annotation
        initTimer(panelController);

    }

    public void initTimer(PanelController panelController) {
        Method[] methods = panelController.getClass().getMethods();
        for(Method method : methods) {
            if(method.isAnnotationPresent(ITimerTask.class)) {
                ITimerTask timerTask = method.getAnnotation(ITimerTask.class);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            try {
                                method.invoke(panelController);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                Sentry.captureException(e);
                            }
                        });
                    }
                }, timerTask.delay(), timerTask.period());
            }
        }

    }

}
