package com.trackharbor.trackharbor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.*;
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
        VBox legend = new VBox(18);
        legend.setPadding(new Insets(20));
        legend.setStyle("-fx-background-color: #f9dada; -fx-border-radius: 10; -fx-background-radius: 10;");
        legend.getChildren().addAll(
                createLegendItem(Color.web("#FF69B4"), "No response"),
                createLegendItem(Color.PURPLE, "2nd Interview"),
                createLegendItem(Color.LIMEGREEN, "Waiting"),
                createLegendItem(Color.RED, "Rejected"),
                createLegendItem(Color.BLUE, "Email Response"),
                createLegendItem(Color.BLACK, "Will Not Accept"),
                createLegendItem(Color.GOLD,"Job Offered")
        );

        // ===== Pie Chart =====
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("No response", 37),
                new PieChart.Data("2nd Interview", 8),
                new PieChart.Data("Waiting", 4),
                new PieChart.Data("Rejected", 20),
                new PieChart.Data("Email Response", 14),
                new PieChart.Data("Will Not Accept", 5),
                new PieChart.Data("Job Offered", 2)
        );
        PieChart pieChart = new PieChart(pieData);

        Circle innerCircle = new Circle();
        innerCircle.setFill(Color.WHITE); // or match your background
        innerCircle.setStroke(Color.LIGHTGRAY);

        // Bind position to center of chart
        innerCircle.centerXProperty().bind(pieChart.widthProperty().divide(2));
        innerCircle.centerYProperty().bind(pieChart.heightProperty().divide(2));

        // Adjust size for donut thickness
        innerCircle.radiusProperty().bind(
                pieChart.widthProperty().add(pieChart.heightProperty()).divide(12)
        );

        StackPane chartContainer = new StackPane();
        chartContainer.getChildren().addAll(pieChart, innerCircle);


        pieChart.setTitle("Application Status");
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false); // use our custom legend
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);

        // ===== Assign slice colors to match legend =====
        String[] colors = {"#FF69B4", "#800080", "#32CD32", "#FF0000", "#0000FF", "#000000", "#FFD700"};
        for (int i = 0; i < pieChart.getData().size(); i++) {
            PieChart.Data data = pieChart.getData().get(i);

            // Set base color
            data.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");

            // Hover effect
            final int index = i;
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle(
                        "-fx-pie-color: " + colors[index] + "; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0.2, 0, 0); " +
                                "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"
                );
            });

            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle(
                        "-fx-pie-color: " + colors[index] + "; " +
                                "-fx-effect: none; " +
                                "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"
                );
            });
        }
        HBox.setMargin(chartContainer, new Insets(0, 50, 0, 0));
        VBox detailsPanel = new VBox();
        detailsPanel.setPrefWidth(300);
        detailsPanel.setSpacing(15);
        detailsPanel.setPadding(new Insets(20));
        detailsPanel.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.2, 0, 4);"
        );

        Label title = new Label("Details");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        detailsPanel.getChildren().add(title);

        detailsPanel.getChildren().add(createDetailsItem("Google", "2024‑02‑10"));
        detailsPanel.getChildren().add(createDetailsItem("Amazon", "2024‑02‑08"));
        detailsPanel.getChildren().add(createDetailsItem("Netflix", "2024‑02‑05"));



        // ===== Layout =====
        HBox mainContent = new HBox(20); // spacing between legend and chart
        HBox.setHgrow(chartContainer, Priority.ALWAYS);
        HBox.setHgrow(legend, Priority.NEVER);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setSpacing(50);
        mainContent.getChildren().addAll(legend, chartContainer, detailsPanel);

        VBox root = new VBox();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        root.getChildren().addAll(menuBar, mainContent);

        chartContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pieChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Scene scene = new Scene(root, 1080, 600);






        // Add stylesheet
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        menuBar.getStyleClass().add("menu-bar-custom");
        legend.getStyleClass().add("legend-box");
        pieChart.getStyleClass().add("chart-custom");
        mainContent.getStyleClass().add("main-content");
        root.getStyleClass().add("root");

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
    private VBox createDetailsItem(String companyName, String date) {

        Label nameLabel = new Label(companyName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label dateLabel = new Label("Date: " + date);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        VBox box = new VBox(nameLabel, dateLabel);
        box.setSpacing(2);
        box.setPadding(new Insets(8, 0, 8, 0));

        return box;
    }


}