package org.javacord.util.handler.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.DiscordApi;
import org.javacord.entity.activity.Activity;
import org.javacord.entity.activity.impl.ImplActivity;
import org.javacord.entity.user.User;
import org.javacord.entity.user.UserStatus;
import org.javacord.entity.user.impl.ImplUser;
import org.javacord.event.user.UserChangeActivityEvent;
import org.javacord.event.user.UserChangeAvatarEvent;
import org.javacord.event.user.UserChangeNameEvent;
import org.javacord.event.user.UserChangeStatusEvent;
import org.javacord.event.user.impl.ImplUserChangeActivityEvent;
import org.javacord.event.user.impl.ImplUserChangeAvatarEvent;
import org.javacord.event.user.impl.ImplUserChangeNameEvent;
import org.javacord.event.user.impl.ImplUserChangeStatusEvent;
import org.javacord.listener.user.UserChangeActivityListener;
import org.javacord.listener.user.UserChangeAvatarListener;
import org.javacord.listener.user.UserChangeNameListener;
import org.javacord.listener.user.UserChangeStatusListener;
import org.javacord.util.gateway.PacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the presence update packet.
 */
public class PresenceUpdateHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public PresenceUpdateHandler(DiscordApi api) {
        super(api, true, "PRESENCE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long userId = packet.get("user").get("id").asLong();
        api.getCachedUserById(userId).map(ImplUser.class::cast).ifPresent(user -> {
            if (packet.has("game")) {
                Activity newActivity = null;
                if (!packet.get("game").isNull()) {
                    newActivity = new ImplActivity(packet.get("game"));
                }
                Activity oldActivity = user.getActivity().orElse(null);
                user.setActivity(newActivity);
                if (!Objects.deepEquals(newActivity, oldActivity)) {
                    dispatchUserActivityChangeEvent(user, newActivity, oldActivity);
                }
            }
            if (packet.has("status")) {
                UserStatus newStatus = UserStatus.fromString(
                        packet.has("status") ? packet.get("status").asText(null) : null);
                UserStatus oldStatus = user.getStatus();
                user.setStatus(newStatus);
                if (newStatus != oldStatus) {
                    dispatchUserStatusChangeEvent(user, newStatus, oldStatus);
                }
            }
            if (packet.get("user").has("username")) {
                String newName = packet.get("user").get("username").asText();
                String oldName = user.getName();
                if (!oldName.equals(newName)) {
                    user.setName(newName);
                    dispatchUserChangeNameEvent(user, newName, oldName);
                }
            }
            if (packet.get("user").has("username")) {
                String newName = packet.get("user").get("username").asText();
                String oldName = user.getName();
                if (!oldName.equals(newName)) {
                    user.setName(newName);
                    dispatchUserChangeNameEvent(user, newName, oldName);
                }
            }
            if (packet.get("user").has("avatar")) {
                String newAvatarHash = packet.get("user").get("avatar").asText(null);
                String oldAvatarHash = user.getAvatarHash();
                if (!Objects.deepEquals(newAvatarHash, oldAvatarHash)) {
                    user.setAvatarHash(newAvatarHash);
                    dispatchUserChangeAvatarEvent(user, newAvatarHash, oldAvatarHash);
                }
            }
        });
    }

    private void dispatchUserActivityChangeEvent(User user, Activity newActivity, Activity oldActivity) {
        UserChangeActivityEvent event = new ImplUserChangeActivityEvent(user, newActivity, oldActivity);

        List<UserChangeActivityListener> listeners = new ArrayList<>();
        listeners.addAll(user.getUserChangeActivityListeners());
        user.getMutualServers().forEach(server -> listeners.addAll(server.getUserChangeActivityListeners()));
        listeners.addAll(api.getUserChangeActivityListeners());

        api.getEventDispatcher().dispatchEvent(api, listeners, listener -> listener.onUserChangeActivity(event));
    }

    private void dispatchUserStatusChangeEvent(User user, UserStatus newStatus, UserStatus oldStatus) {
        UserChangeStatusEvent event = new ImplUserChangeStatusEvent(user, newStatus, oldStatus);

        List<UserChangeStatusListener> listeners = new ArrayList<>();
        listeners.addAll(user.getUserChangeStatusListeners());
        user.getMutualServers().forEach(server -> listeners.addAll(server.getUserChangeStatusListeners()));
        listeners.addAll(api.getUserChangeStatusListeners());

        api.getEventDispatcher().dispatchEvent(api, listeners, listener -> listener.onUserChangeStatus(event));
    }

    private void dispatchUserChangeNameEvent(User user, String newName, String oldName) {
        UserChangeNameEvent event = new ImplUserChangeNameEvent(user, newName, oldName);

        List<UserChangeNameListener> listeners = new ArrayList<>();
        listeners.addAll(user.getUserChangeNameListeners());
        user.getMutualServers().forEach(server -> listeners.addAll(server.getUserChangeNameListeners()));
        listeners.addAll(api.getUserChangeNameListeners());

        api.getEventDispatcher().dispatchEvent(api, listeners, listener -> listener.onUserChangeName(event));
    }

    private void dispatchUserChangeAvatarEvent(User user, String newAvatarHash, String oldAvatarHash) {
        UserChangeAvatarEvent event = new ImplUserChangeAvatarEvent(user, newAvatarHash, oldAvatarHash);

        List<UserChangeAvatarListener> listeners = new ArrayList<>();
        listeners.addAll(user.getUserChangeAvatarListeners());
        user.getMutualServers().forEach(server -> listeners.addAll(server.getUserChangeAvatarListeners()));
        listeners.addAll(api.getUserChangeAvatarListeners());

        api.getEventDispatcher().dispatchEvent(api, listeners, listener -> listener.onUserChangeAvatar(event));
    }

}