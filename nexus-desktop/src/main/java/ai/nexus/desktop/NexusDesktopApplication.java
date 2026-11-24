package ai.nexus.desktop;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexusDesktopApplication extends Application {

    private String sessionId = "desktop-default";
    private Label sessionLabel;
    private String authToken;
    private String username;
    private final CredentialStore credentialStore = PlatformCredentialStore.create();

    @Override
    public void start(Stage stage) {
        restoreSession(stage);
    }

    private void restoreSession(Stage stage) {
        authToken = credentialStore.get("token");
        username = credentialStore.get("username");
        if (authToken == null || username == null) {
            showAuth(stage);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/auth/me"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() / 100 == 2) {
                        username = extractJsonField(response.body(), "username");
                        showMain(stage);
                    } else {
                        clearSession();
                        showAuth(stage);
                    }
                }))
                .exceptionally(error -> {
                    javafx.application.Platform.runLater(() -> showAuth(stage));
                    return null;
                });
    }

    private void showAuth(Stage stage) {
        Label mark = new Label("N");
        mark.getStyleClass().add("brand-mark");
        Label title = new Label("Nexus AI");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Интеллектуальное рабочее пространство");
        subtitle.getStyleClass().add("auth-subtitle");
        Label kicker = new Label("WELCOME TO THE NEXUS");
        kicker.getStyleClass().add("kicker");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль, минимум 8 символов");
        Label status = new Label();
        status.getStyleClass().add("status");

        Button login = new Button("Войти в Nexus");
        login.getStyleClass().add("primary-button");
        Button register = new Button("Создать аккаунт");
        register.getStyleClass().add("ghost-button");
        HBox actions = new HBox(12, login, register);
        VBox brand = new VBox(10, mark, kicker, title, subtitle);
        VBox form = new VBox(24, brand, usernameField, passwordField, actions, status);
        form.setMaxWidth(420);
        form.getStyleClass().add("auth-card");

        login.setOnAction(event -> authenticate(stage, usernameField, passwordField, status, "/login"));
        register.setOnAction(event -> authenticate(stage, usernameField, passwordField, status, "/register"));
        passwordField.setOnAction(event -> authenticate(stage, usernameField, passwordField, status, "/login"));

        BorderPane root = new BorderPane(form);
        root.setCenter(form);
        root.getStyleClass().addAll("root", "auth-root");
        Scene scene = new Scene(root, 900, 620);
        scene.getStylesheets().add(getClass().getResource("/nexus.css").toExternalForm());
        stage.setTitle("Nexus AI — Вход");
        stage.setScene(scene);
        stage.show();
    }

    private void authenticate(Stage stage, TextField usernameField, PasswordField passwordField,
                              Label status, String action) {
        String enteredUsername = usernameField.getText().trim();
        String password = passwordField.getText();
        if (enteredUsername.isEmpty() || password.length() < 8) {
            status.setText("Укажи логин и пароль от 8 символов.");
            return;
        }

        String body = "{\"username\":\"" + escapeJson(enteredUsername)
                + "\",\"password\":\"" + escapeJson(password) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/auth" + action))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        status.setText("Подключение...");
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() / 100 == 2) {
                        authToken = extractJsonField(response.body(), "token");
                        username = extractJsonField(response.body(), "username");
                        credentialStore.put("token", authToken);
                        credentialStore.put("username", username);
                        showMain(stage);
                    } else {
                        status.setText(response.body().isBlank() ? "Не удалось выполнить вход." : response.body());
                    }
                }))
                .exceptionally(error -> {
                    javafx.application.Platform.runLater(() -> status.setText("Сервер недоступен."));
                    return null;
                });
    }

    private void showMain(Stage stage) {
        Label mark = new Label("N");
        mark.getStyleClass().add("brand-mark-small");
        Label brandName = new Label("NEXUS AI");
        brandName.getStyleClass().add("brand-name");
        Label brandTag = new Label("INTELLIGENCE, CONNECTED");
        brandTag.getStyleClass().add("brand-tag");
        VBox brand = new VBox(4, new HBox(10, mark, brandName), brandTag);

        Label title = new Label("Nexus AI");
        title.getStyleClass().add("screen-title");

        Label subtitle = new Label("Твоё пространство для идей, кода и сложных задач");
        subtitle.getStyleClass().add("subtitle");

        Label chatsTitle = new Label("ЧАТЫ");
        chatsTitle.getStyleClass().add("sidebar-title");
        Button newChat = new Button("＋  Новый чат");
        newChat.setMaxWidth(Double.MAX_VALUE);
        newChat.getStyleClass().add("new-chat-button");
        Button deleteChat = new Button("Удалить текущий чат");
        deleteChat.setMaxWidth(Double.MAX_VALUE);
        deleteChat.getStyleClass().add("quiet-button");
        sessionLabel = new Label("desktop-default");
        sessionLabel.getStyleClass().add("current-session");
        Label userLabel = new Label("Пользователь: " + username);
        userLabel.getStyleClass().add("user-chip");
        Button logout = new Button("Выйти");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.getStyleClass().add("quiet-button");
        Button profile = new Button("Профиль");
        profile.setMaxWidth(Double.MAX_VALUE);
        profile.getStyleClass().add("quiet-button");

        VBox sidebar = new VBox(18, brand, chatsTitle, newChat, deleteChat, sessionLabel, userLabel, profile, logout);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(210);
        sidebar.getStyleClass().add("sidebar");

        VBox chat = new VBox(18);
        chat.setPadding(new Insets(28, 32, 28, 32));
        chat.getStyleClass().add("message-list");
        addMessage(chat, "assistant", "Добро пожаловать в Nexus AI.\n\nТвой персональный интеллектуальный слой уже готов к работе.");
        ScrollPane chatScroll = new ScrollPane(chat);
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.getStyleClass().add("chat-scroll");

        newChat.setOnAction(event -> {
            sessionId = "chat-" + System.currentTimeMillis();
            sessionLabel.setText(sessionId);
            chat.getChildren().clear();
            addMessage(chat, "assistant", "Новый чат готов. Чем займёмся?");
        });
        deleteChat.setOnAction(event -> deleteCurrentChat(stage, chat));
        logout.setOnAction(event -> {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/auth/session"))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenRun(() -> javafx.application.Platform.runLater(() -> {
                        clearSession();
                        showAuth(stage);
                    }));
        });
        profile.setOnAction(event -> showProfile(stage));

        TextField input = new TextField();
        input.setPromptText("Напиши сообщение...");
        Button send = new Button("Отправить");
        send.getStyleClass().add("send-button");
        HBox composer = new HBox(12, input, send);
        composer.setPadding(new Insets(16, 24, 24, 24));
        composer.getStyleClass().add("composer");
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);

        Runnable sendMessage = () -> sendMessage(input, chat, send);
        send.setOnAction(event -> sendMessage.run());
        input.setOnAction(event -> sendMessage.run());

        Label modelBadge = new Label("●  DEMO MODEL");
        modelBadge.getStyleClass().add("model-badge");
        HBox headerLine = new HBox(16, new VBox(4, title, subtitle), modelBadge);
        headerLine.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(headerLine.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        VBox header = new VBox(headerLine);
        header.setPadding(new Insets(24));
        header.getStyleClass().add("topbar");

        BorderPane root = new BorderPane(chatScroll);
        root.setLeft(sidebar);
        root.setTop(header);
        root.setBottom(composer);
        root.getStyleClass().addAll("root", "app-shell");

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/nexus.css").toExternalForm());

        stage.setTitle("Nexus AI");
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
        loadProviderStatus(modelBadge);
        loadHistory(chat);
        loadSessions(sidebar, chat);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void sendMessage(TextField input, VBox chat, Button send) {
        String message = input.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        input.clear();
        addMessage(chat, "user", message);
        Label typing = addMessage(chat, "assistant", "Думаю...");
        send.setDisable(true);

        String body = "{\"message\":\"" + escapeJson(message) + "\",\"sessionId\":\""
                + sessionId + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/chat"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    typing.setText(response.statusCode() / 100 == 2
                            ? extractMessage(response.body())
                            : "Не удалось получить ответ от модели.");
                    send.setDisable(false);
                }))
                .exceptionally(error -> {
                    javafx.application.Platform.runLater(() -> {
                        typing.setText("Сервер недоступен.");
                        send.setDisable(false);
                    });
                    return null;
                });
    }

    private void loadHistory(VBox chat) {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api/chat/history?sessionId=" + sessionId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() == 200 && !response.body().equals("[]")) {
                        chat.getChildren().clear();
                        renderHistory(chat, response.body());
                    }
                }));
    }

    private void loadProviderStatus(Label badge) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/providers"))
                .GET()
                .build();
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        if (response.body().contains("\"active\":true") && response.body().contains("OpenAI")) {
                            badge.setText("●  OPENAI");
                        } else {
                            badge.setText("●  NEXUS DEMO");
                        }
                    }
                }));
    }

    private void loadSessions(VBox sidebar, VBox chat) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/chat/sessions"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() != 200) {
                        return;
                    }
                    for (String id : parseSessionIds(response.body())) {
                        Button sessionButton = new Button(id);
                        sessionButton.setMaxWidth(Double.MAX_VALUE);
                        sessionButton.getStyleClass().add("session-button");
                        sessionButton.setOnAction(event -> {
                            sessionId = id;
                            sessionLabel.setText(id);
                            loadHistory(chat);
                        });
                        sidebar.getChildren().add(sessionButton);
                    }
                }));
    }

    private void deleteCurrentChat(Stage stage, VBox chat) {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api/chat/history?sessionId=" + sessionId))
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() == 204) {
                        sessionId = "chat-" + System.currentTimeMillis();
                        sessionLabel.setText(sessionId);
                        chat.getChildren().clear();
                        addMessage(chat, "assistant", "Чат удалён. Можно начать новый.");
                    }
                }));
    }

    private void showProfile(Stage stage) {
        Label title = new Label("Профиль");
        title.getStyleClass().add("title");
        Label account = new Label("Пользователь\n" + username);
        account.getStyleClass().add("subtitle");
        Label status = new Label("Сессия активна\nТокен хранится в защищённом хранилище платформы.");
        status.getStyleClass().add("session-label");
        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Текущий пароль");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Новый пароль, минимум 8 символов");
        Button changePassword = new Button("Сменить пароль");
        Button back = new Button("Вернуться к чатам");
        back.setOnAction(event -> showMain(stage));
        changePassword.setOnAction(event -> changePassword(stage, currentPassword, newPassword, status));

        VBox profileCard = new VBox(16, title, account, status, currentPassword, newPassword, changePassword, back);
        profileCard.setMaxWidth(440);
        profileCard.getStyleClass().add("auth-card");
        BorderPane root = new BorderPane(profileCard);
        root.setCenter(profileCard);
        root.getStyleClass().add("root");
        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("/nexus.css").toExternalForm());
        stage.setTitle("Nexus AI — Профиль");
        stage.setScene(scene);
        stage.show();
    }

    private void changePassword(Stage stage, PasswordField currentPassword, PasswordField newPassword, Label status) {
        if (currentPassword.getText().isBlank() || newPassword.getText().length() < 8) {
            status.setText("Укажи текущий пароль и новый пароль от 8 символов.");
            return;
        }
        String body = "{\"currentPassword\":\"" + escapeJson(currentPassword.getText())
                + "\",\"newPassword\":\"" + escapeJson(newPassword.getText()) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/auth/password"))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        status.setText("Сохраняем новый пароль...");
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                    if (response.statusCode() == 204) {
                        clearSession();
                        showAuth(stage);
                    } else {
                        status.setText(response.body().isBlank() ? "Не удалось сменить пароль." : response.body());
                    }
                }))
                .exceptionally(error -> {
                    javafx.application.Platform.runLater(() -> status.setText("Сервер недоступен."));
                    return null;
                });
    }

    private void clearSession() {
        authToken = null;
        username = null;
        credentialStore.remove("token");
        credentialStore.remove("username");
    }

    private java.util.List<String> parseSessionIds(String json) {
        Matcher matcher = Pattern.compile("\\\"([^\\\"]+)\\\"").matcher(json);
        java.util.List<String> ids = new java.util.ArrayList<>();
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    private Label addMessage(VBox messages, String role, String text) {
        Label roleLabel = new Label(role.equals("user") ? "YOU" : "NEXUS AI");
        roleLabel.getStyleClass().add("message-role");
        Label content = new Label(text);
        content.setWrapText(true);
        content.setMaxWidth(760);
        content.getStyleClass().add("message-content");
        VBox card = new VBox(7, roleLabel, content);
        card.getStyleClass().addAll("message-card", role.equals("user") ? "user-message" : "assistant-message");
        messages.getChildren().add(card);
        return content;
    }

    private void renderHistory(VBox messages, String json) {
        Pattern messagePattern = Pattern.compile("\\\"role\\\":\\\"(user|assistant)\\\".*?\\\"content\\\":\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher matcher = messagePattern.matcher(json);
        while (matcher.find()) {
            addMessage(messages, matcher.group(1), unescapeJson(matcher.group(2)));
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractMessage(String json) {
        String marker = "\"message\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return json;
        }
        start += marker.length();
        int end = json.indexOf("\",\"provider\"", start);
        return end < 0 ? json.substring(start) : json.substring(start, end).replace("\\\"", "\"");
    }

    private String extractJsonField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return "";
        }
        start += marker.length();
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                break;
            }
            end++;
        }
        return unescapeJson(json.substring(start, end));
    }

    private String unescapeJson(String value) {
        return value.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n");
    }
}
