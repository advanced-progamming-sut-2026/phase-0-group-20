package io.java.pvz.controllers.GameController;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.function.Consumer;

public class ReactionController {

    public enum Category {
        TEXT, EMOJI, STICKER
    }

    public static class IncomingReaction {
        public final String fromUsername;
        public final Category category;
        public final int index;

        public IncomingReaction(String fromUsername, Category category, int index) {
            this.fromUsername = fromUsername;
            this.category = category;
            this.index = index;
        }
    }

    private static ReactionController instance;
    private static boolean pushListenersRegistered = false;

    private Consumer<IncomingReaction> onReactionReceived;

    public static synchronized ReactionController getInstance() {
        if (instance == null) instance = new ReactionController();
        return instance;
    }

    private ReactionController() {
        registerPushListenersOnce();
    }

    private void registerPushListenersOnce() {
        if (pushListenersRegistered) return;
        pushListenersRegistered = true;

        NetworkClient.getInstance().onPush(MessageType.REACTION_SEND, message -> Gdx.app.postRunnable(() -> {
            if (onReactionReceived == null) return;
            try {
                Category category = Category.valueOf(message.getString("category"));
                onReactionReceived.accept(new IncomingReaction(
                    message.getString("fromUsername"), category, message.getInt("index")));
            } catch (Exception ignored) {

            }
        }));
    }

    public void setOnReactionReceived(Consumer<IncomingReaction> listener) {
        this.onReactionReceived = listener;
    }

    public void sendReaction(Category category, int index, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.REACTION_SEND);
        request.put("category", category.name());
        request.put("index", index);

        Thread worker = new Thread(() -> {
            NetworkMessage response;
            try {
                response = NetworkClient.getInstance().sendAndWait(request, 10);
            } catch (Exception e) {
                response = NetworkMessage.failure(request, "Network error: " + e.getMessage());
            }
            NetworkMessage finalResponse = response;
            Gdx.app.postRunnable(() -> {
                if (callback != null) callback.accept(finalResponse);
            });
        }, "reaction-send-request");
        worker.setDaemon(true);
        worker.start();
    }
}
