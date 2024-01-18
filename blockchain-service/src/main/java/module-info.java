module ua.nure.blockchainservice {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;

    opens ua.nure.blockchainservice to javafx.fxml;
    exports ua.nure.blockchainservice;
}