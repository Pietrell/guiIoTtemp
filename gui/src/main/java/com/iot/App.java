package com.iot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jssc.SerialPortList;

/**
 * JavaFX App
 */
public class App extends Application {

    private SerialCommChannel console;
    XYChart.Series<Number, Number> series;
    SerialParser parser;
    GUIFactoryImpl p;
    float valveOpeningDegrees; // apertura della valvola in gradi
    int state; // stato di arduino: 0=normal, 1=pre-alarm , 2= alarm
    boolean smartLight;

    long start;

    @Override
    public void start(Stage stage) throws Exception {
        console = null;
        final Timer clockTimer = new Timer();

        ArrayList<String> rawData = new ArrayList<>();

        valveOpeningDegrees = 0;
        state = -1;
        smartLight = false;

        parser = new SerialParser();

        p = new GUIFactoryImpl();
        series = new XYChart.Series<>();

        ArrayList<String> port = new ArrayList<>();

        for (String string : SerialPortList.getPortNames()) {
            port.add(string);
        }
        final ComboBox<String> portComboBox = p.createSelector(port);

        // check for presence of com port
        portComboBox.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        System.out.println("Value is: " + newValue);
                        try {
                            if (console != null) {
                                console.close();
                                console = new SerialCommChannel(newValue, 9600);
                            } else {
                                console = new SerialCommChannel(newValue, 9600);
                            }
                            // console = updateChannel(console,pt ,b );

                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                });

        series.setName("water");
        series.getData().add(new XYChart.Data(0, 0));

        // Creates a slider
        Slider slider = p.createSlider();

        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number newVal) {

                if (state == 2) {
                    valveOpeningDegrees = newVal.floatValue();
                    console.sendMsg("valve-" + valveOpeningDegrees);
                }

            }

        });

        LineChart<Number, Number> chart = p.createLineChart();
        chart.getData().add(series);
        chart.setAnimated(false);
        HBox selectors = new HBox();
        selectors.getChildren().add(portComboBox);

        BorderPane root = new BorderPane(chart, selectors, null, null, slider); // center,top,right,bottom,left
        root.setAlignment(slider, Pos.CENTER_LEFT);

        Scene scene = new Scene(root, 1000, 1000);
        // add Scene to the frame
        stage.setScene(scene);
        stage.show();

        // time driven event check
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                /*
                 * Calculates the time in base 10 time and calls the 4 methods
                 * to set the GUI display.
                 * 
                 * In a constant while loop in order to continuously update
                 * the GUI.
                 */

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        /* methods to execute every n seconds */
                        System.out.println("UPDATE");
                        rawData.clear();
                        try {
                            rawData.addAll(console.retiriveMessages());

                        } catch (Exception e) {
                            // TODO: handle exception
                            System.out.println("error in serial communiation");
                        }
                        series = p.populateChart(parser.getNewChartData(rawData), series);
                        chart.getData().clear();
                        chart.getData().add(series);
                        valveOpeningDegrees = parser.getServoPosition(rawData);
                        // state = parser.getState(rawData);
                        // smartLight = parser.getSmartLight(rawData);
                    }
                });
            }
        }, 0, 1000 // Sleep for 1 seconds since that is how long it is between
        );

    }

    @Override
    public void stop() {
        if (console != null) {
            console.close();
        }
        System.out.println("console closed");
        // Save file
        System.exit(0);
    }

    // public Series updateChart(float value){

    private SerialCommChannel updateChannel(SerialCommChannel c, String port, int baud) throws Exception {
        c.close();
        return new SerialCommChannel(port, baud);
    }

    public static void main(String[] args) {
        launch();
    }

}
