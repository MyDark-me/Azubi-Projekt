package org.devcloud.ap.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.*;
import org.devcloud.ap.utils.OperatingSystemInfo;

import java.util.Random;

public class PanelController {
    // Statistik
    @FXML private Label statistikTextCpuLabel;
    @FXML private Label statistikTextMemoryLabel;
    @FXML private TextArea statistikTextarea;
    @FXML private AreaChart<String, Integer> statistikAreachart;

    // Database Config
    @FXML private TextField configTextFieldDatabaseHost;
    @FXML private TextField configTextFieldDatabasePort;
    @FXML private TextField configTextFieldDatabaseUser;
    @FXML private PasswordField configTextFieldDatabasePassword;
    @FXML private TextField configTextFieldDatabaseTable;

    @FXML public Button configButtonDatabaseTest;
    @FXML public Button configButtonSpeichern;

    private final SeriesBuilder seriesBuilder = new SeriesBuilder("Random");
    private final SeriesBuilder seriesBuilderCpu = new SeriesBuilder("CPU");
    private final SeriesBuilder seriesBuilderRam = new SeriesBuilder("RAM");
    private final Random random = new Random();

    public void update() {
        update1();
        update2();
    }

    /**
     * Aktualisiert die Statistik
     * Jede Sekunde
     */
    @ITimerTask(delay = 1)
    public void update1() {
        // CPU
        statistikTextCpuLabel.setText(OperatingSystemInfo.getCPUUsage());

        // Memory
        statistikTextMemoryLabel.setText(OperatingSystemInfo.getMemoryUsage());
    }

    @ITimerTask(delay = 1, period = 5000)
    public void update2() {
        seriesBuilder.addValue(random.nextInt(100));
        statistikAreachart = seriesBuilder.setAreaChart(statistikAreachart);

        seriesBuilderCpu.addValue((int) OperatingSystemInfo.getRawCPUUsage());
        statistikAreachart = seriesBuilderCpu.setAreaChart(statistikAreachart);

        seriesBuilderRam.addValue((int) OperatingSystemInfo.getRawMemoryUsage());
        statistikAreachart = seriesBuilderRam.setAreaChart(statistikAreachart);
    }

    public void setDatabaseDefaults(String localhost, String port, String user, String table) {
        configTextFieldDatabaseHost.setText(localhost);
        configTextFieldDatabasePort.setText(port);
        configTextFieldDatabaseUser.setText(user);
        configTextFieldDatabasePassword.setText("NotRealpassword");
        configTextFieldDatabaseTable.setText(table);
    }

    @FXML public void configButtonSpeichernAction(ActionEvent actionEvent) {
        actionEvent.getEventType();
        String host = configTextFieldDatabaseHost.getText();
        String port = configTextFieldDatabasePort.getText();
        String user = configTextFieldDatabaseUser.getText();
        String password = configTextFieldDatabasePassword.getText();
        String table = configTextFieldDatabaseTable.getText();

        // TODO: Speichern
        statistikTextarea.setText("Host: " + host);
        statistikTextarea.appendText("Port: " + port);
        statistikTextarea.appendText("User: " + user);
        statistikTextarea.appendText("Password: " + password);
        statistikTextarea.appendText("Table: " + table);
    }

    @FXML public void configButtonDatabaseTestAction(ActionEvent actionEvent) {
        actionEvent.getEventType();
        // TODO: Testen ob die Datenbank erreichbar ist
    }
}
