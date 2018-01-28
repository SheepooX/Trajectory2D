package org.sheepoox.trajectory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.text.DecimalFormat;

public class Main extends Application {

    /*private ArrayList<XYChart.Series<Number, Number>> trajectories = new ArrayList<>();
    private int currentTrajectory = 0;*/

    private LineChart<Number, Number> lineChart;
    private Spinner velocitySpinner;
    private Slider angleSlider;
    private Slider heightSlider;
    private Spinner gAccelerationSpinner;
    //private VBox trajectoryContainer;

    @Override
    public void start(Stage primaryStage) throws Exception{
        VBox root = FXMLLoader.load(getClass().getResource("main.fxml"));

        primaryStage.setTitle("Vizualizace Trajektorie - Černý");
        int screenWidth = 1400;
        int screenHeight = 1050;
        primaryStage.setWidth(screenWidth);
        primaryStage.setHeight(screenHeight - 200);
        primaryStage.setScene(new Scene(root, screenWidth, screenHeight));
        primaryStage.show();

        velocitySpinner = (Spinner) root.lookup("#velocitySpinner");
        angleSlider = (Slider) root.lookup("#angleSlider");
        gAccelerationSpinner = (Spinner) root.lookup("#gAccelerationSpinner");

        heightSlider = (Slider) root.lookup("#heightSlider");
        Label heightView = (Label) root.lookup("#heightView");

        angleSlider.setMajorTickUnit(15);
        angleSlider.setBlockIncrement(0.5);
        Label angleView = (Label) root.lookup("#angleView");

        velocitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000000000, 40, 1) {});

        velocitySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            setChart();
        });

        angleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            angleView.setText(String.format("%.2f °", newValue));
            setChart();
        });

        heightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            heightView.setText(String.format("%.2f m", newValue));
            setChart();
        });

        gAccelerationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-1000, 1000, 9.806, 1));

        gAccelerationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            setChart();
        });

        NumberAxis xAxis = new NumberAxis(-5, 350, 10);
        xAxis.setLabel("s (m)");
        NumberAxis yAxis = new NumberAxis   (-5, 175, 10);
        yAxis.setLabel("h (m)");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setPrefHeight(800);
        root.getChildren().add(lineChart);

        setChart();
    }

    private void setChart() {
        lineChart.getData().retainAll();
        XYChart.Series<Number, Number>[] series = getTrajectoryGraph((Double) velocitySpinner.getValue(), angleSlider.getValue(), (Double) gAccelerationSpinner.getValue(), heightSlider.getValue());
        lineChart.getData().add(series[0]);
        lineChart.getData().add(series[1]);
    }

    /*
    v0 - initial velocity, a - inclination angle (degrees), g - gravity acceleration, h - initial height
    (1) x = v0 * cos(a) * t
    (2) y = v0 * sin(a) * t - 1/2 * g * t**2 + h = h + t(v0 * sin(a) - 1/2 * g * t)
    */
    private XYChart.Series<Number, Number>[] getTrajectoryGraph(final double v0, final double a, final double g, final double h) {
        XYChart.Series<Number, Number>[] series = new XYChart.Series[2];
        series[0] = new XYChart.Series<>();
        series[1] = new XYChart.Series<>();
        // We cant divide with 0
        if (g == 0) return series;
        //
        final double cpr = v0 * Math.cos(Math.toRadians(a));
        final double spr = v0 * Math.sin(Math.toRadians(a));
        double x;
        double y = h;
        for (double t = 0; y >= 0 && y < 260; t += 0.025) {
            x = cpr * t; // (1)
            y = h + (spr - g * t * 0.5) * t; // (2)
            XYChart.Data<Number, Number> p = new XYChart.Data<>(x, y);
            DecimalFormat df = new DecimalFormat("#.###");
            if (Double.valueOf(df.format(t)) % 1 != 0) {
                Rectangle rect = new Rectangle(0, 0);
                rect.setVisible(false);
                p.setNode(rect);
            }
            if (spr / g < t) {
                series[1].getData().add(p);
            } else if(y >= 0) {
                series[0].getData().add(p);
            }
        }
        return series;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
