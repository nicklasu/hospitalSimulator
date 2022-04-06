module com.example.hospitalsimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.dlsc.formsfx;
    requires java.desktop;

    opens src.view to javafx.fxml;
    opens src.controller to javafx.fxml;
        exports src.view to javafx.graphics;
}