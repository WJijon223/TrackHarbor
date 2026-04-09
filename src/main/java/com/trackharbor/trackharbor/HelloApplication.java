package com.trackharbor.trackharbor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        // ===== Menu Bar =====
        MenuBar menuBar = new MenuBar();
        Menu dashboardMenu = new Menu("Dashboard");
        Menu tableMenu = new Menu("Table");
        Menu notesMenu = new Menu("Notes");
        Menu logout = new Menu("Logout");
        menuBar.getMenus().addAll(dashboardMenu, tableMenu, notesMenu, logout);

        // ===== Legend =====
        VBox legend = new VBox(10);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-background-color: #f9dada; -fx-border-radius: 10; -fx-background-radius: 10;");
        legend.getChildren().addAll(
                createLegendItem(Color.PINK, "No response"),
                createLegendItem(Color.PURPLE, "2nd Interview"),
                createLegendItem(Color.LIMEGREEN, "Waiting"),
                createLegendItem(Color.RED, "Rejected"),
                createLegendItem(Color.BLUE, "Email Response"),
                createLegendItem(Color.BLACK, "Will Not Accept")
        );

        // ===== Pie Chart =====
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("No response", 5),
                new PieChart.Data("2nd Interview", 2),
                new PieChart.Data("Waiting", 3),
                new PieChart.Data("Rejected", 10),
                new PieChart.Data("Email Response", 4),
                new PieChart.Data("Will Not Accept", 1)
        );

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Application Status");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false); // use our custom legend
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);

        // ===== Assign slice colors to match legend =====
        String[] colors = {"#FF69B4", "#800080", "#32CD32", "#FF0000", "#0000FF", "#000000"};
        for (int i = 0; i < pieChart.getData().size(); i++) {
            pieChart.getData().get(i).getNode().setStyle(
                    "-fx-pie-color: " + colors[i] + ";"
            );
        }

        // ===== Layout =====
        HBox mainContent = new HBox(20); // spacing between legend and chart
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(legend, pieChart);

        VBox root = new VBox();
        root.getChildren().addAll(menuBar, mainContent);

        Scene scene = new Scene(root, 1080, 600);

        // Add stylesheet
        scene.getStylesheets().add(getClass().getResource("/HelloApplication-styles.css").toExternalForm());

        primaryStage.setTitle("My App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createLegendItem(Color color, String labelText) {
        Circle circle = new Circle(8, color);
        circle.setStroke(Color.BLACK); // add black border for visibility
        circle.setStrokeWidth(1.5);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14; -fx-padding: 0 0 0 8;");

        HBox box = new HBox(circle, label);
        box.setPadding(new Insets(5, 0, 5, 5));
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}