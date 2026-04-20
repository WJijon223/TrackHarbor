package com.trackharbor.trackharbor.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardPageController {

    @FXML private PieChart pieChart;
    @FXML private StackPane chartContainer;
    @FXML private Circle donutHole;

    @FXML private VBox detailsPanel;
    @FXML private Label detailsTitle;
    @FXML private Label detailsCount;
    @FXML private VBox detailsList;
    @FXML private Button nextButton;
    @FXML private Button backButton;

    private boolean isZoomed = false;
    private PieChart.Data activeSlice;

    // Details Panel Limit
    private List<CompanyEntry> currentEntries = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 5;

    private final String[] colors = {
            "#FF69B4", "#800080", "#32CD32", "#FF0000",
            "#0000FF", "#000000", "#FFD700"
    };

    @FXML
    public void initialize() {
        setupPieChart();

        // Start details panel off-screen
        detailsPanel.setTranslateX(300);

        // Donut hole scales with container
        donutHole.radiusProperty().bind(
                chartContainer.widthProperty().add(chartContainer.heightProperty()).divide(12)
        );

        // Next Button
        nextButton.setOnAction(e -> {
            currentPage++;
            loadPage();
        });
        // Back Button
        backButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });

    }
    // Pie Chart Data
    private void setupPieChart() {
        pieChart.getData().addAll(
                new PieChart.Data("No response", 37),
                new PieChart.Data("2nd Interview", 8),
                new PieChart.Data("Waiting", 4),
                new PieChart.Data("Rejected", 20),
                new PieChart.Data("Email Response", 14),
                new PieChart.Data("Will Not Accept", 5),
                new PieChart.Data("Job Offered", 2)
        );
    // Pie Chart Data Loop
        for (int i = 0; i < pieChart.getData().size(); i++) {
            PieChart.Data data = pieChart.getData().get(i);
            final int index = i;

            data.getNode().setStyle("-fx-pie-color: " + colors[index] + ";");
            data.getNode().setOnMouseClicked(e -> handleSliceClick(data));
        }
    }


    // Panel Slide In 
    private void slideInPanel() {
        TranslateTransition slideIn =
                new TranslateTransition(Duration.seconds(0.4), detailsPanel);
        slideIn.setFromX(300);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_BOTH);
        slideIn.play();
    }

    // Panel Slide Out 
    private void hideDetailsPanel() {
        TranslateTransition slideOut =
                new TranslateTransition(Duration.seconds(0.4), detailsPanel);
        slideOut.setFromX(0);
        slideOut.setToX(300);
        slideOut.setInterpolator(Interpolator.EASE_BOTH);
        slideOut.play();
    }
    // Details Panel Text
    private void showDetailsPanel(String title, int count) {
        detailsTitle.setText(title);
        detailsCount.setText("Count: " + count);

        // Demo Data, Replace with Getters From Table Page
        currentEntries.clear();
        for (int i = 1; i <= count; i++) {
            currentEntries.add(new CompanyEntry("Company " + i, LocalDate.now().minusDays(i)));
        }

        // Sort By Date
        currentEntries.sort(Comparator.comparing(CompanyEntry::getDate).reversed());

        currentPage = 0;
        loadPage();

        slideInPanel();
    }
    
    private void loadPage() {
        detailsList.getChildren().clear();

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, currentEntries.size());

        for (int i = start; i < end; i++) {
            CompanyEntry entry = currentEntries.get(i);

            Label item = new Label(entry.getName() + " — " + entry.getDate());
            item.setStyle("-fx-font-size: 14px;");
            detailsList.getChildren().add(item);
        }

        nextButton.setDisable(end >= currentEntries.size());
        backButton.setDisable(currentPage == 0);
    }

    public void handleSliceClick(PieChart.Data slice) {

        // Pie Chart Node Toggle
        if (isZoomed && activeSlice == slice) {
            resetChart();
            return;
        }

        
        if (isZoomed) return;

        
        isZoomed = true;
        activeSlice = slice;

        showDetailsPanel(slice.getName(), (int) slice.getPieValue());
        animateSliceSelection(slice); // We'll define this next

    }
    private void animateSliceSelection(PieChart.Data slice) {

        for (PieChart.Data data : pieChart.getData()) {

            if (data != slice) {
                FadeTransition fade = new FadeTransition(Duration.seconds(0.3), data.getNode());
                fade.setToValue(0.15);

                ScaleTransition shrink = new ScaleTransition(Duration.seconds(0.3), data.getNode());
                shrink.setToX(0.7);
                shrink.setToY(0.7);

                new ParallelTransition(fade, shrink).play();

            } else {
                FadeTransition fade = new FadeTransition(Duration.seconds(0.3), data.getNode());
                fade.setToValue(1.0);

                ScaleTransition grow = new ScaleTransition(Duration.seconds(0.3), data.getNode());
                grow.setToX(1.05);
                grow.setToY(1.05);

                new ParallelTransition(fade, grow).play();
            }
        }
    }



    public void resetChart() {
        if (!isZoomed) return;

        isZoomed = false;

        hideDetailsPanel();

        for (PieChart.Data data : pieChart.getData()) {

            FadeTransition fade = new FadeTransition(Duration.seconds(0.3), data.getNode());
            fade.setToValue(1.0);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.3), data.getNode());
            scale.setToX(1.0);
            scale.setToY(1.0);

            new ParallelTransition(fade, scale).play();
        }
    }


    // Sample Data Class, Probably Will Be Removed After Firebase
    public static class CompanyEntry {
        private final String name;
        private final LocalDate date;

        public CompanyEntry(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }

        public String getName() { return name; }
        public LocalDate getDate() { return date; }
    }
}
