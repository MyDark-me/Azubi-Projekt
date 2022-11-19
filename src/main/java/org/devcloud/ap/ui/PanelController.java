package org.devcloud.ap.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.*;
import org.devcloud.ap.utils.OperatingSystemInfo;
import org.devcloud.ap.utils.SQLPostgres;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        statistikTextarea.setText("Starting Capture of Console Output");

        outputStreamForOut = new ByteArrayOutputStream();
        outputStreamForErr = new ByteArrayOutputStream();

        consoleCaptorForOut = new PrintStream(outputStreamForOut);
        consoleCaptorForErr = new PrintStream(outputStreamForErr);

        System.setOut(consoleCaptorForOut);
        System.setErr(consoleCaptorForErr);

        //System.out.println("Test");

        captureConsoleOutput();
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

    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    private ByteArrayOutputStream outputStreamForOut;
    private ByteArrayOutputStream outputStreamForErr;
    private PrintStream consoleCaptorForOut;
    private PrintStream consoleCaptorForErr;

    private List<String> getContent(ByteArrayOutputStream outputStream) {
        return Stream.of(outputStream.toString().split(System.lineSeparator()))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @ITimerTask(delay = 1, period = 1)
    public void captureConsoleOutput() {
        // See https://github.com/Hakky54/console-captor/blob/master/src/main/java/nl/altindag/console/ConsoleCaptor.java
        try {
            for (String line : getContent(outputStreamForOut)) {
                statistikTextarea.appendText(line + System.lineSeparator());
            }

            outputStreamForOut.flush();
            consoleCaptorForOut.flush();

            outputStreamForOut.close();
            outputStreamForErr.close();

            consoleCaptorForOut.close();
            consoleCaptorForErr.close();

            System.setOut(consoleCaptorForOut);
            System.setErr(consoleCaptorForErr);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void configButtonSpeichernAction(ActionEvent actionEvent) {
        actionEvent.getEventType();
        String host = configTextFieldDatabaseHost.getText();
        String port = configTextFieldDatabasePort.getText();
        String user = configTextFieldDatabaseUser.getText();
        String password = configTextFieldDatabasePassword.getText();
        String table = configTextFieldDatabaseTable.getText();

        // TODO: Speichern
        statistikTextarea.setText("Host: " + host + "\n");
        statistikTextarea.appendText("Port: " + port + "\n");
        statistikTextarea.appendText("User: " + user + "\n");
        statistikTextarea.appendText("Password: " + "********" + "\n");
        statistikTextarea.appendText("Table: " + table + "\n");
    }

    @FXML public void configButtonDatabaseTestAction(ActionEvent actionEvent) {
        actionEvent.getEventType();
        String host = configTextFieldDatabaseHost.getText();
        String port = configTextFieldDatabasePort.getText();
        String user = configTextFieldDatabaseUser.getText();
        String password = configTextFieldDatabasePassword.getText();
        String table = configTextFieldDatabaseTable.getText();

        var sqlPostgres = new SQLPostgres(host + ":" + port, user, password, table);

        if(sqlPostgres.isConnection()) {
            statistikTextarea.setText("Verbindung erfolgreich");
        } else {
            statistikTextarea.setText("Verbindung fehlgeschlagen");
        }
    }
}
