package com.iot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
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
        final Timer clockTimer = new Timer();
        List<String> baud = new ArrayList<>();
        start = System.currentTimeMillis();
        ArrayList<String> rawData = new ArrayList<>();
        valveOpeningDegrees = 0;
        state = -1;
        smartLight= false;
        
        parser= new SerialParser(console);

        p = new GUIFactoryImpl();
        series = new XYChart.Series<>();
        
        baud.add("4800");
        baud.add("9600");
        baud.add("14400");
        ArrayList<String> port = new ArrayList<>();
       
        for (String string : SerialPortList.getPortNames()) {
            port.add(string);
        }
        
        final ComboBox<String> baudRateComboBox = p.createSelector(baud);
        final ComboBox<String> portComboBox = p.createSelector(port);

        baudRateComboBox.getSelectionModel().select(1);
        //check for presenc of com port
        portComboBox.getSelectionModel().select(0);
        
        if(port.size()!=0){
            console = new SerialCommChannel(portComboBox.getSelectionModel().getSelectedItem().toString(), 9600);
            // console = new SerialCommChannel(portComboBox.getSelectionModel().getSelectedItem().toString(), 
            //                 Integer.valueOf( baudRateComboBox.getItems().get(2)));
            
        }else{
            System.out.println("common interface port not found, did you connect Arduino?");
        }
        //communication update based on combobox values
        // baudRateComboBox.setOnAction((event) -> {
           
        //     String pt = portComboBox.getValue().toString();
        //     int b = Integer.valueOf( baudRateComboBox.getValue().toString());
        //     try {
        //         console = updateChannel(console,pt ,b );
                
        //     } catch (Exception e) {
        //         System.out.println(e.toString());
        //     }
   
        // });

        // portComboBox.setOnAction((event) -> {
           
        //     String pt = portComboBox.getValue();
        //     int b = Integer.valueOf( baudRateComboBox.getValue());
        //     try {
        //         console = updateChannel(console,pt ,b );
                
        //     } catch (Exception e) {
        //         System.out.println(e.toString());
        //     }
            
        // });

        series.setName("water");
        series.getData().add(new XYChart.Data((System.currentTimeMillis() - start), 0));
        
        // Creates a slider
        Slider slider = p.createSlider();
        LineChart<Number,Number> chart = p.createLineChart();
        chart.getData().add(series);
        HBox selectors = new HBox();
        selectors.getChildren().add(portComboBox);
        selectors.getChildren().add(baudRateComboBox);
        
        BorderPane root = new BorderPane(chart,selectors,null,null,slider); //center,top,right,bottom,left
        root.setAlignment(slider, Pos.CENTER_LEFT);
        
        Scene scene = new Scene(root, 1000, 1000); 
        // add Scene to the frame
        stage.setScene(scene);
        stage.show();
        
        //time driven event check
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
                    @Override  public void run() {
                        /*methods to execute every n seconds */
                        rawData.clear();
                        rawData.addAll(console.retiriveMessages());                          
                        series = p.populateChart(parser.getNewChartData(rawData), series, start);
                        valveOpeningDegrees = parser.getServoPosition(rawData);
                        state = parser.getState(rawData);
                        smartLight = parser.getSmartLight(rawData);
                    }
                });
            }
        }, 0, 500  // Sleep for 1 seconds since that is how long it is between
        );              
    }

    // public Series updateChart(float value){

    private SerialCommChannel updateChannel(SerialCommChannel c, String port, int baud) throws Exception{
        c.close();
        return new SerialCommChannel(port, baud);
    }

    public static void main(String[] args) {
        launch();
    }

}