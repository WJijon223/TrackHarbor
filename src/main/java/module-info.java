module com.trackharbor.trackharbor {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.trackharbor.trackharbor to javafx.fxml;
    opens com.trackharbor.trackharbor.controllers to javafx.fxml;

    exports com.trackharbor.trackharbor;
    exports com.trackharbor.trackharbor.controllers;
}