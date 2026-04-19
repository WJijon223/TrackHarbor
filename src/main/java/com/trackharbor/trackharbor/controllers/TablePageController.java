package com.trackharbor.trackharbor.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class TablePageController implements Initializable {

    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, String> nameColumn;
    @FXML private TableColumn<Application, String> linkColumn;
    @FXML private TableColumn<Application, Boolean> appliedColumn;
    @FXML private TableColumn<Application, String> dateColumn;
    @FXML private TableColumn<Application, String> statusColumn;
    @FXML private TableColumn<Application, Void> actionsColumn;

    public static class Application {
        private final SimpleStringProperty company;
        private final SimpleStringProperty subtitle;
        private final SimpleStringProperty link;
        private final SimpleBooleanProperty applied;
        private final SimpleStringProperty date;
        private final SimpleStringProperty status;

        public Application(String company, String subtitle, String link, boolean applied, String date, String status) {
            this.company = new SimpleStringProperty(company);
            this.subtitle = new SimpleStringProperty(subtitle);
            this.link = new SimpleStringProperty(link);
            this.applied = new SimpleBooleanProperty(applied);
            this.date = new SimpleStringProperty(date);
            this.status = new SimpleStringProperty(status);
        }

        public String getCompany() { return company.get(); }
        public String getSubtitle() { return subtitle.get(); }
        public String getLink() { return link.get(); }
        public boolean isApplied() { return applied.get(); }
        public SimpleBooleanProperty appliedProperty() { return applied; }
        public String getDate() { return date.get(); }
        public String getStatus() { return status.get(); }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        applicationsTable.setEditable(true);
        applicationsTable.setSelectionModel(null);

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().company);
        nameColumn.setCellFactory(col -> new TableCell<>() {
            private final VBox vbox = new VBox(2);
            private final Label companyLabel = new Label();
            private final Label subtitleLabel = new Label();

            {
                companyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #111827;");
                subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #4A5565;");
                vbox.getChildren().addAll(companyLabel, subtitleLabel);
                vbox.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Application app = getTableRow().getItem();
                    if (app != null) {
                        companyLabel.setText(app.getCompany());
                        subtitleLabel.setText(app.getSubtitle());
                    }
                    setGraphic(vbox);
                }
            }
        });

        linkColumn.setCellValueFactory(cellData -> cellData.getValue().link);
        linkColumn.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setStyle("-fx-text-fill: #9810FA; -fx-underline: true; -fx-font-size: 13;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    hyperlink.setText(item);
                    setGraphic(hyperlink);
                }
            }
        });

        appliedColumn.setCellValueFactory(cellData -> cellData.getValue().appliedProperty());
        appliedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(appliedColumn));
        appliedColumn.setEditable(false);

        dateColumn.setCellValueFactory(cellData -> cellData.getValue().date);
        dateColumn.setCellFactory(col -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setStyle("-fx-text-fill: #4A5565; -fx-font-size: 13;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> cellData.getValue().status);
        statusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    switch (item) {
                        case "Rejected" -> label.setStyle(
                                "-fx-background-color: #8A1A1A; -fx-text-fill: #ffffff;" +
                                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                                        "-fx-padding: 5 16 5 16; -fx-background-radius: 20;");
                        case "Waiting" -> label.setStyle(
                                "-fx-background-color: #FFE2A4; -fx-text-fill: #4A5565;" +
                                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                                        "-fx-padding: 5 16 5 16; -fx-background-radius: 20;");
                        case "Interview" -> label.setStyle(
                                "-fx-background-color: #E1F3DB; -fx-text-fill: #4A5565;" +
                                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                                        "-fx-padding: 5 16 5 16; -fx-background-radius: 20;");
                    }
                    setGraphic(label);
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox hbox = new HBox(5);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        ObservableList<Application> data = FXCollections.observableArrayList(
                new Application("Capital One", "Technology Internship", "https://capitalone.com/careers", true, "8/14/2025", "Rejected"),
                new Application("Chicago Trading Company", "SWE Internship", "https://chicagotrading.com", true, "8/17/2025", "Rejected"),
                new Application("American Express", "SWE Internship", "https://americanexpress.com", true, "9/1/2025", "Waiting"),
                new Application("Dropbox", "SWE Internship Summer 2026", "https://jobs.dropbox.com", true, "9/9/2025", "Waiting"),
                new Application("Figma", "SWE Internship San Francisco", "https://figma.com/careers", true, "9/19/2025", "Interview")
        );

        applicationsTable.setItems(data);
    }
}