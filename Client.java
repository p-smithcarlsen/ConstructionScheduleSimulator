
import java.io.IOException;
import java.util.TreeMap;

import ScheduleComponents.ConstructionProject;
import ScheduleComponents.Task;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class Client extends Application{

    Program program;
    static LineChart<Number,Number> chart;
    int dayCount = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane box = new BorderPane();
        HBox hbox = addHBox();
        chart = addLineChart();
        box.setTop(hbox);
        box.setCenter(chart);
        // addStackPane(hbox);
        Scene scene = new Scene(box, 1600, 500);
        primaryStage.setTitle("LBMS");
        primaryStage.setScene(scene);
        program = new Program(chart);
        primaryStage.show();        
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(8, 8, 8, 8));
        hbox.setSpacing(15);
        hbox.setStyle("-fx-background-color: #336699;");
    
        Button buttonExperiment = new Button("Experiment");
        buttonExperiment.setPrefSize(100, 20);
        buttonExperiment.setText("Experiment");
        buttonExperiment.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    Program.runExperiment(500);
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
        });

    
        Button buttonSimulate = new Button("Simulate");
        buttonSimulate.setPrefSize(100, 20);
        buttonSimulate.setText("Simulate");
        buttonSimulate.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    chart.getData().removeAll();
                    Program.simulateSchedule(true);
                    dayCount = 0;


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        Button buttonNextDay = new Button("Next Day");
        buttonNextDay.setPrefSize(100, 20);
        buttonNextDay.setText("Next Day");
        buttonNextDay.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (dayCount > 0) {
                    chart.getData().remove(5);
                }
                XYChart.Series<Number, Number> day = new XYChart.Series<Number, Number>();
                day.setName("day");
                day.getData().add(new XYChart.Data<Number,Number>(dayCount, 0));
                day.getData().add(new XYChart.Data<Number,Number>(dayCount, 5));
                dayCount++;
                chart.getData().add(day);
            }
        });


        hbox.getChildren().addAll(buttonExperiment, buttonSimulate, buttonNextDay);
    
        return hbox;
    }

    public LineChart<Number, Number> addLineChart(){
        final NumberAxis xAxis = new NumberAxis("Takt",0.0, 45.0, 1);
        final NumberAxis yAxis = new NumberAxis("Location",0.0, 5.0, 1);

        LineChart<Number, Number> chart = new LineChart<Number,Number>(xAxis, yAxis);
        chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
        return chart;
    }

    public XYChart.Series<Number, Number> addSeries() {
        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

        series.setName("L1");

        series.getData().add(new XYChart.Data<Number, Number>(1.0, 1.0));
        series.getData().add(new XYChart.Data<Number, Number>(2.0, 2.0));
        series.getData().add(new XYChart.Data<Number, Number>(3.0, 3.0));
        series.getData().add(new XYChart.Data<Number, Number>(4.0, 5.0));
        series.getData().add(new XYChart.Data<Number, Number>(5.0, 3.0));

        return series;
    }

    public XYChart.Series<Number, Number> addSeries2() {
        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

        series.setName("L2");

        series.getData().add(new XYChart.Data<Number, Number>(1.0, 3.0));
        series.getData().add(new XYChart.Data<Number, Number>(2.0, 4.0));
        series.getData().add(new XYChart.Data<Number, Number>(3.0, 5.0));
        series.getData().add(new XYChart.Data<Number, Number>(4.0, 2.0));
        series.getData().add(new XYChart.Data<Number, Number>(5.0, 45.0));

        return series;
    }

    public static void setUpChart(ConstructionProject project, LineChart<Number,Number> chart) {
            Task temp;
            TreeMap<String,XYChart.Series<Number,Number>> plots = new TreeMap<>();

            for (int i = 0; i < 5; i++) {
                temp = project.tasks.getTasks().get(i);
                XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
                series.setName("crew " + (i+1) + "-" + temp.activity);
                series.getData().add(new XYChart.Data<Number,Number>(temp.earliestStart, temp.location));
                series.getData().add(new XYChart.Data<Number,Number>(temp.earliestFinish, temp.location+1));
                plots.put(series.getName(),series);
                for (int j = i + 5; j < 25; j = j + 5) {
                    temp = project.tasks.getTasks().get(j);
                    series.getData().add(new XYChart.Data<Number,Number>(temp.earliestStart, temp.location));
                    series.getData().add(new XYChart.Data<Number,Number>(temp.earliestFinish, temp.location+1));
                    // if (j > 9 && j < 15) {
                    //     plots.get("crew " + (i+1) + "-" + temp.activity).getData().add(new XYChart.Data<Number,Number>(temp.earliestStart, temp.location));
                    //     plots.get("crew " + (i+1) + "-" + temp.activity).getData().add(new XYChart.Data<Number,Number>(temp.earliestFinish, 3));
                    // } else if (j > 19) {
                    //     plots.get("crew " + (i+1) + "-" + temp.activity).getData().add(new XYChart.Data<Number,Number>(temp.earliestStart, temp.location));
                    //     plots.get("crew " + (i+1) + "-" + temp.activity).getData().add(new XYChart.Data<Number,Number>(temp.earliestFinish, 5));
                    // } else {
                    //     plots.get("crew " + (i+1) + "-" + temp.activity).getData().add(new XYChart.Data<Number,Number>(temp.earliestStart, temp.location));
                    // }
                }
            }
            for (String Task : plots.keySet()) {
                chart.getData().add(plots.get(Task));
            }
        }

    public static void main(String[] args) {
        launch(args);
    }
    
}
