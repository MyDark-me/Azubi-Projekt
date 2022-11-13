package org.devcloud.ap.ui;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;

public class SeriesBuilder {

    private static final ArrayList<SeriesBuilder> seriesBuilders = new ArrayList<>();
    private final String name;
    private final ArrayList<Integer> statistikAreachartList = new ArrayList<>();

    public SeriesBuilder(String name) {
        this.name = name;
        seriesBuilders.add(this);
    }

    public void addValue(int value) {
        statistikAreachartList.add(value);
    }

    public XYChart.Series<String, Integer> getSeries() {
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName(name);

        if(statistikAreachartList.size() > 20) {
            statistikAreachartList.remove(0);
        }

        for (int i = 0; i < statistikAreachartList.size(); i++) {
            XYChart.Data<String, Integer> data = new XYChart.Data<>(String.valueOf(i), statistikAreachartList.get(i));
            series.getData().add(data);
        }
        return series;
    }

    public AreaChart<String, Integer> setAreaChart(AreaChart<String, Integer> areaChart) {

        if(!areaChart.getData().isEmpty()) {
            areaChart.getData().clear();
        }

        for (SeriesBuilder seriesBuilder : seriesBuilders) {
            areaChart.getData().add(seriesBuilder.getSeries());
        }
        return areaChart;
    }
}
