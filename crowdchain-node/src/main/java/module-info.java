module ua.nure.crowdchainnode {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.xml.crypto;
    requires jdk.httpserver;
    requires org.xerial.sqlitejdbc;


    opens ua.nure.crowdchainnode to javafx.fxml;
    exports ua.nure.crowdchainnode;
    exports ua.nure.crowdchainnode.controller;
    exports ua.nure.crowdchainnode.model;
    opens ua.nure.crowdchainnode.controller to javafx.fxml;
}