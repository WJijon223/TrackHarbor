module com.trackharbor.trackharbor {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.auth.oauth2;
    requires firebase.admin;
    requires google.cloud.firestore;
    requires com.google.api.apicommon;
    requires google.cloud.core;

    opens com.trackharbor.trackharbor to javafx.fxml;
    opens com.trackharbor.trackharbor.controllers to javafx.fxml;

    exports com.trackharbor.trackharbor;
    exports com.trackharbor.trackharbor.controllers;
}