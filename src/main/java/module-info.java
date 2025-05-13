module org.aut.minitelegram_client {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires com.google.gson;
    requires org.java_websocket;

    exports org.aut.minitelegram_client;
    opens org.aut.minitelegram_client to javafx.graphics;
    opens org.aut.minitelegram_client.controllers to javafx.fxml, javafx.graphics;
    opens org.aut.minitelegram_client.dto to com.google.gson;
}