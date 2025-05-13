module org.aut.minitelegram_client {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.aut.minitelegram_client to javafx.fxml;
    exports org.aut.minitelegram_client;
}