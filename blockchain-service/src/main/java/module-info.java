module ua.nure.blockchainservice {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;

    opens ua.nure.blockchainservice to javafx.fxml;
    exports ua.nure.blockchainservice;

    opens ua.nure.blockchainservice.controller to javafx.fxml;
    exports ua.nure.blockchainservice.controller;

    opens ua.nure.blockchainservice.db.util to javafx.fxml;
    exports ua.nure.blockchainservice.db.util;

    opens ua.nure.blockchainservice.model to javafx.fxml;
    exports ua.nure.blockchainservice.model;

    opens ua.nure.blockchainservice.service to javafx.fxml;
    exports ua.nure.blockchainservice.service;

    opens ua.nure.blockchainservice.thread to javafx.fxml;
    exports ua.nure.blockchainservice.thread;
}