package org.aut.minitelegram_client.controllers;

import com.google.gson.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.aut.minitelegram_client.Main;
import org.aut.minitelegram_client.Session;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label messageLabel;

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String SERVER_URL = "http://localhost:8080";

    @FXML
    public void onSignUp() {
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: green;");

        try {
            String body = gson.toJson(Map.of(
                    "username", username.getText().trim(),
                    "password", password.getText()
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/user/signup"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 201) {
                messageLabel.setText("Sign-up successful! Please log in.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Sign-up failed: " + resp.statusCode());
            }

        } catch (Exception ex) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Exception: " + ex.getMessage());
        }
    }

    @FXML
    public void onLogin() {
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: red;");  // default to error color

        try {
            String body = gson.toJson(Map.of(
                    "username", username.getText().trim(),
                    "password", password.getText()
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/user/login"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                messageLabel.setText("Login failed: " + resp.statusCode());
                return;
            }

            JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
            Session.token = obj.get("token").getAsString();
            Session.username = username.getText().trim();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Chat Home");
            stage.show();

        } catch (Exception ex) {
            messageLabel.setText("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}