module ua.nure.blockchainservice {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens ua.nure.blockchainservice to javafx.fxml;
    exports ua.nure.blockchainservice;
}