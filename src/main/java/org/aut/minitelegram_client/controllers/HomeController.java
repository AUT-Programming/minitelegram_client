package org.aut.minitelegram_client.controllers;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.aut.minitelegram_client.Session;
import org.aut.minitelegram_client.dto.MessageDTO;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeController {
    private static final String SERVER_URL = "http://localhost:8080";

    @FXML private Label currentUserLabel;
    @FXML private ListView<JsonObject> chatListView;
    @FXML private TextField inputUsername;
    @FXML private Label status;
    @FXML private Label chatTitle;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageField;

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private WebSocketClient ws;

    @FXML
    public void initialize() {
        System.out.println("Initializing HomeController");
        currentUserLabel.setText(Session.username);

        chatListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.get("name").getAsString());
            }
        });

        try {
            loadChats();
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("Failed to load chats");
        }
        currentUserLabel.setOnMouseClicked(event -> {
            try {
                loadChats();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadChats() throws Exception {
        System.out.println("Loading chats...");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/chat"))
                .header("Authorization", Session.token)
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("Chat list response " + resp.statusCode() + ": " + resp.body());
        if (resp.statusCode() / 100 == 2) {
            JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
            List<JsonObject> chats = new ArrayList<>();
            for (JsonElement el : arr) {
                String title = el.getAsJsonObject().get("name").getAsString();
                String[] names = title.split("##");
                String user = (names[0]).equals(Session.username) ? names[1] : names[0];
                el.getAsJsonObject().addProperty("name", user);
                chats.add(el.getAsJsonObject());
            }
            chatListView.setItems(FXCollections.observableArrayList(chats));
        } else {
            throw new RuntimeException("HTTP " + resp.statusCode());
        }
    }

    @FXML
    void onCreateChat() {
        String other = inputUsername.getText().trim();
        if (other.isEmpty()) {
            status.setText("Enter a username");
            return;
        }
        try {
            String body = gson.toJson(
                    Map.of("name", Session.username + "##" + other, "isGroup", false));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/chat"))
                    .header("Content-Type","application/json")
                    .header("Authorization", Session.token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                status.setText("Chat creation failed: " + resp.statusCode());
                return;
            }
            int chatId = gson.fromJson(resp.body(), JsonObject.class).get("id").getAsInt();

            for (String u : List.of(Session.username, other)) {
                String cmBody = gson.toJson(Map.of(
                        "chatId", chatId,
                        "username", u,
                        "role", "member"
                ));
                HttpRequest cmReq = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + "/chatmember"))
                        .header("Content-Type","application/json")
                        .header("Authorization", Session.token)
                        .POST(HttpRequest.BodyPublishers.ofString(cmBody))
                        .build();
                HttpResponse<String> cmResp = http.send(cmReq, HttpResponse.BodyHandlers.ofString());
                if (cmResp.statusCode() / 100 != 2) {
                    status.setText("Adding member failed: " + u);
                    return;
                }
            }

            inputUsername.clear();
            status.setText("Chat with " + other + " created");
            loadChats();
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    void onChatSelect() {
        JsonObject sel = chatListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Session.currentChatId = sel.get("id").getAsInt();
        Session.currentChatName = sel.get("name").getAsString();
        chatTitle.setText("Chat with " + Session.currentChatName);
        messageListView.getItems().clear();
        openWebSocket();
    }

    private void openWebSocket() {
        try {
            if (ws != null) ws.close();
            ws = new WebSocketClient(
                    new URI("ws://localhost:8090/ws/chat/" + Session.currentChatId),
                    Map.of("Authorization", Session.token)
            ) {
                @Override public void onOpen(ServerHandshake hs) {}
                @Override public void onMessage(String msg) {
                    MessageDTO dto = gson.fromJson(msg, MessageDTO.class);
                    String line = "[" + dto.getSenderDisplayName() + " @ " + dto.getSentAt() + "] " + dto.getContent();
                    Platform.runLater(() -> messageListView.getItems().add(line));
                }
                @Override public void onClose(int code, String reason, boolean remote) {}
                @Override public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            ws.connect();
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("WebSocket error");
        }
    }

    @FXML
    void onSendMessage() {
        if (ws != null && ws.isOpen() && !messageField.getText().isBlank()) {
            MessageDTO dto = new MessageDTO();
            dto.setContent(messageField.getText());
            ws.send(gson.toJson(dto));
            messageField.clear();
        }
    }
}
