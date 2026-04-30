package com.trackharbor.trackharbor.controllers;

import com.trackharbor.trackharbor.model.Position;
import com.trackharbor.trackharbor.service.PositionService;
import com.trackharbor.trackharbor.session.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.Group;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TablePageController implements Initializable {

    @FXML private Pane logoPane;
    @FXML private TableView<RowItem> applicationsTable;
    @FXML private TableColumn<RowItem, String> nameColumn;
    @FXML private TableColumn<RowItem, String> linkColumn;
    @FXML private TableColumn<RowItem, Boolean> appliedColumn;
    @FXML private TableColumn<RowItem, String> dateColumn;
    @FXML private TableColumn<RowItem, String> statusColumn;
    @FXML private TableColumn<RowItem, Void> actionsColumn;

    @FXML private VBox modalOverlay;
    @FXML private TextField companyNameField;
    @FXML private TextField positionField;
    @FXML private TextField linkField;
    @FXML private TextField dateField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button cancelButton;
    @FXML private Button addApplicationButton;
    @FXML private Button newApplicationButton;
    @FXML private Button backToDashboardButton;

    private final PositionService positionService = new PositionService();
    private final ObservableList<RowItem> tableData = FXCollections.observableArrayList();
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter PARSE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final List<String> VALID_STATUSES = List.of("Interview", "OA", "Rejected", "Waiting", "Accepted", "Other");

    public static class RowItem {
        private final String positionId;
        private final SimpleStringProperty company;
        private final SimpleStringProperty subtitle;
        private final SimpleStringProperty link;
        private final SimpleBooleanProperty applied;
        private final SimpleStringProperty date;
        private final SimpleStringProperty status;

        public RowItem(String positionId, String company, String subtitle, String link, boolean applied, String date, String status) {
            this.positionId = positionId;
            this.company = new SimpleStringProperty(company);
            this.subtitle = new SimpleStringProperty(subtitle);
            this.link = new SimpleStringProperty(link);
            this.applied = new SimpleBooleanProperty(applied);
            this.date = new SimpleStringProperty(date);
            this.status = new SimpleStringProperty(status);
        }

        public String getPositionId() { return positionId; }
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
        setupTableColumns();
        loadPositionsFromFirebase();
    }

    private void setupTableColumns() {
        applicationsTable.setEditable(false);
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
                    RowItem row = getTableRow().getItem();
                    if (row != null) {
                        companyLabel.setText(row.getCompany());
                        subtitleLabel.setText(row.getSubtitle());
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
                    label.setStyle(resolveStatusStyle(item));
                    setGraphic(label);
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button viewBtn = new Button("View");
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                String baseBtn = "-fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;";
                viewBtn.setStyle(baseBtn + "-fx-background-color: #f3e8ff; -fx-text-fill: #6E11B0; -fx-opacity: 1;");
                editBtn.setStyle(baseBtn + "-fx-background-color: #f3e8ff; -fx-text-fill: #6E11B0; -fx-opacity: 1;");
                deleteBtn.setStyle(baseBtn + "-fx-background-color: #f3e8ff; -fx-text-fill: #6E11B0; -fx-opacity: 1;");
                hbox.setAlignment(Pos.CENTER_LEFT);

                viewBtn.setOnAction(e -> {
                    RowItem row = getTableRow().getItem();
                    if (row != null) handleViewDetails(row);
                });
                editBtn.setOnAction(e -> {
                    RowItem row = getTableRow().getItem();
                    if (row != null) handleEdit(row);
                });
                deleteBtn.setOnAction(e -> {
                    RowItem row = getTableRow().getItem();
                    if (row != null) handleDelete(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    setGraphic(hbox);
                    setStyle("-fx-opacity: 1; -fx-background-color: transparent;");
                    setOpacity(1.0);
                    hbox.setOpacity(1.0);
                    viewBtn.setOpacity(1.0);
                    editBtn.setOpacity(1.0);
                    deleteBtn.setOpacity(1.0);
                }
            }
        });

        applicationsTable.setItems(tableData);
    }

    private void loadPositionsFromFirebase() {
        String userId = SessionManager.getCurrentUser().getId();
        Thread thread = new Thread(() -> {
            try {
                List<Position> positions = positionService.getPositionsForUser(userId);
                List<RowItem> rows = new ArrayList<>();
                for (Position p : positions) {
                    rows.add(positionToRowItem(p));
                }
                Platform.runLater(() -> tableData.setAll(rows));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load positions: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private RowItem positionToRowItem(Position p) {
        String fullName = p.getName() != null ? p.getName() : "";
        String company = fullName;
        String subtitle = "";
        if (fullName.contains("|")) {
            String[] parts = fullName.split("\\|", 2);
            company = parts[0].trim();
            subtitle = parts[1].trim();
        }
        String dateStr = p.getDateApplied() != null ? p.getDateApplied().format(DISPLAY_FORMAT) : "";
        return new RowItem(p.getId(), company, subtitle, p.getLink() != null ? p.getLink() : "",
                p.isApplied(), dateStr, p.getStatus() != null ? p.getStatus() : "");
    }

    @FXML
    private void handleNewApplication() {
        clearModalFields();
        modalOverlay.setVisible(true);
    }

    @FXML
    private void handleCancelModal() {
        modalOverlay.setVisible(false);
    }

    @FXML
    private void handleAddApplication() {
        String company = companyNameField.getText().trim();
        String subtitle = positionField.getText().trim();
        String link = linkField.getText().trim();
        String dateText = dateField.getText().trim();
        String status = statusComboBox.getValue();

        if (company.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Company name is required.");
            return;
        }
        if (status == null || status.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please select a status.");
            return;
        }

        LocalDate dateApplied = null;
        if (!dateText.isEmpty()) {
            try {
                dateApplied = LocalDate.parse(dateText, PARSE_FORMAT);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Date must be in MM/DD/YYYY format.");
                return;
            }
        }

        String combinedName = subtitle.isEmpty() ? company : company + " | " + subtitle;
        String userId = SessionManager.getCurrentUser().getId();

        Position position = new Position();
        position.setName(combinedName);
        position.setLink(link);
        position.setApplied(!dateText.isEmpty());
        position.setDateApplied(dateApplied);
        position.setStatus(status);
        position.setAiTipsGenerated(false);
        position.setAiTips(new ArrayList<>());

        modalOverlay.setVisible(false);

        Thread thread = new Thread(() -> {
            try {
                String newId = positionService.createPosition(userId, position);
                position.setId(newId);
                RowItem row = positionToRowItem(position);
                Platform.runLater(() -> tableData.add(row));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save position: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void handleEdit(RowItem row) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Application");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-padding: 32;");

        Label title = new Label("Edit Application");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #000000;");

        TextField companyEdit = styledTextField(row.getCompany(), "Company Name");
        TextField subtitleEdit = styledTextField(row.getSubtitle(), "Position");
        TextField linkEdit = styledTextField(row.getLink(), "Link (https://)");
        TextField dateEdit = styledTextField(row.getDate(), "MM/DD/YYYY");

        ComboBox<String> statusEdit = new ComboBox<>(FXCollections.observableArrayList(VALID_STATUSES));
        statusEdit.setValue(row.getStatus());
        statusEdit.setMaxWidth(Double.MAX_VALUE);
        statusEdit.setStyle("-fx-background-radius: 10; -fx-font-size: 13;");

        VBox form = new VBox(14,
                title,
                labeledField("Company Name", companyEdit),
                labeledField("Position", subtitleEdit),
                labeledField("Link", linkEdit),
                new HBox(16, labeledField("Date Applied", dateEdit), labeledField("Status", statusEdit))
        );
        form.setPrefWidth(460);

        pane.setContent(form);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.setText("Save Changes");
        okButton.setStyle("-fx-background-color: #6E11B0; -fx-text-fill: #ffffff; -fx-background-radius: 9999; -fx-padding: 10 26; -fx-font-size: 13; -fx-font-weight: bold;");

        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4A5565; -fx-background-radius: 9999; -fx-padding: 10 26; -fx-font-size: 13;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String company = companyEdit.getText().trim();
                String subtitle = subtitleEdit.getText().trim();
                String link = linkEdit.getText().trim();
                String dateText = dateEdit.getText().trim();
                String status = statusEdit.getValue();

                if (company.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Company name is required.");
                    return;
                }
                if (status == null) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Please select a status.");
                    return;
                }

                LocalDate dateApplied = null;
                if (!dateText.isEmpty()) {
                    try {
                        dateApplied = LocalDate.parse(dateText, PARSE_FORMAT);
                    } catch (DateTimeParseException e) {
                        showAlert(Alert.AlertType.WARNING, "Validation", "Date must be in MM/DD/YYYY format.");
                        return;
                    }
                }

                String combinedName = subtitle.isEmpty() ? company : company + " | " + subtitle;
                String userId = SessionManager.getCurrentUser().getId();

                Position updated = new Position();
                updated.setId(row.getPositionId());
                updated.setName(combinedName);
                updated.setLink(link);
                updated.setApplied(!dateText.isEmpty());
                updated.setDateApplied(dateApplied);
                updated.setStatus(status);
                updated.setAiTipsGenerated(false);
                updated.setAiTips(new ArrayList<>());

                Thread thread = new Thread(() -> {
                    try {
                        positionService.updatePosition(userId, updated);
                        RowItem newRow = positionToRowItem(updated);
                        Platform.runLater(() -> {
                            int index = findRowIndex(row.getPositionId());
                            if (index >= 0) tableData.set(index, newRow);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update: " + e.getMessage()));
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
    }

    private void handleDelete(RowItem row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Application");
        confirm.setHeaderText("Delete " + row.getCompany() + "?");
        confirm.setContentText("This cannot be undone.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String userId = SessionManager.getCurrentUser().getId();
                Thread thread = new Thread(() -> {
                    try {
                        positionService.deletePosition(userId, row.getPositionId());
                        Platform.runLater(() -> {
                            int index = findRowIndex(row.getPositionId());
                            if (index >= 0) tableData.remove(index);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete: " + e.getMessage()));
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
    }

    private void handleViewDetails(RowItem row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Application Details");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-padding: 32;");

        Label title = new Label(row.getCompany());
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitleLabel = new Label(row.getSubtitle());
        subtitleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #9810FA;");

        Label statusLabel = new Label(row.getStatus());
        statusLabel.setStyle(resolveStatusStyle(row.getStatus()));

        Hyperlink linkLabel = new Hyperlink(row.getLink());
        linkLabel.setStyle("-fx-text-fill: #9810FA; -fx-font-size: 13;");

        Label dateLabel = new Label("Applied: " + (row.getDate().isEmpty() ? "N/A" : row.getDate()));
        dateLabel.setStyle("-fx-text-fill: #4A5565; -fx-font-size: 13;");

        Label appliedLabel = new Label("Applied: " + (row.isApplied() ? "Yes" : "No"));
        appliedLabel.setStyle("-fx-text-fill: #4A5565; -fx-font-size: 13;");

        VBox content = new VBox(12, title, subtitleLabel, statusLabel, new Separator(), linkLabel, dateLabel, appliedLabel);
        content.setPrefWidth(400);

        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) pane.lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color: #6E11B0; -fx-text-fill: #ffffff; -fx-background-radius: 9999; -fx-padding: 10 26; -fx-font-size: 13; -fx-font-weight: bold;");

        dialog.showAndWait();
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        navigateToDashboard();
    }

    @FXML
    private void handleBackToDashboard() {
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/trackharbor/trackharbor/dashboard-page.fxml"));
            logoPane.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dashboard page.", e);
        }
    }

    private void clearModalFields() {
        companyNameField.clear();
        positionField.clear();
        linkField.clear();
        dateField.clear();
        statusComboBox.setValue(null);
    }

    private int findRowIndex(String positionId) {
        for (int i = 0; i < tableData.size(); i++) {
            if (tableData.get(i).getPositionId().equals(positionId)) return i;
        }
        return -1;
    }

    private String resolveStatusStyle(String status) {
        return switch (status) {
            case "Rejected" -> "-fx-background-color: #8A1A1A; -fx-text-fill: #ffffff; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
            case "Waiting" -> "-fx-background-color: #FFE2A4; -fx-text-fill: #4A5565; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
            case "Interview" -> "-fx-background-color: #E1F3DB; -fx-text-fill: #4A5565; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
            case "Accepted" -> "-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
            case "OA" -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
            default -> "-fx-background-color: #f3f4f6; -fx-text-fill: #4A5565; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 16 5 16; -fx-background-radius: 20;";
        };
    }

    private TextField styledTextField(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-font-size: 13;");
        return tf;
    }

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #59168B; -fx-font-weight: bold; -fx-font-size: 13;");
        VBox box = new VBox(6, label, field);
        VBox.setVgrow(field, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}