package cloud.timo.TimoCloud.bungeecord.listeners;

import cloud.timo.TimoCloud.api.events.EventHandler;
import cloud.timo.TimoCloud.api.events.Listener;
import cloud.timo.TimoCloud.api.events.server.ServerRegisterEvent;
import cloud.timo.TimoCloud.api.events.server.ServerUnregisterEvent;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import cloud.timo.TimoCloud.bungeecord.TimoCloudBungee;
import cloud.timo.TimoCloud.common.utils.ChatColorUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Ohldag (Fedox)
 * @version 1.1 (Improved by v0)
 * @since 8/4/2025, 4:44 PM
 */
public class ServerNotify implements Listener {

    private static final Map<NotifyType, String> MESSAGE_KEYS = new HashMap<>();

    static {
        MESSAGE_KEYS.put(NotifyType.REGISTER, "ServerStarted");
        MESSAGE_KEYS.put(NotifyType.UNREGISTER, "ServerStopped");
    }

    @EventHandler
    public void onServerRegister(ServerRegisterEvent event) {
        sendMessage(NotifyType.REGISTER, event.getServer());
    }

    @EventHandler
    public void onServerUnregister(ServerUnregisterEvent event) {
        sendMessage(NotifyType.UNREGISTER, event.getServer());
    }

    private void sendMessage(NotifyType type, ServerObject serverObject) {
        String messageKey = MESSAGE_KEYS.get(type);
        if (messageKey == null) {
            throw new IllegalArgumentException("Unknown NotifyType: " + type);
        }

        String rawMessage = TimoCloudBungee.getInstance().getFileManager().getMessages().getString(messageKey);
        if (rawMessage == null) {
            TimoCloudBungee.getInstance()
                .getLogger()
                .warning("No message found for key: " + messageKey + ". Please check your configuration.");
            return;
        }

        String formattedMessage = formatServerMessage(rawMessage, serverObject);

        for (ProxiedPlayer player : TimoCloudBungee.getInstance().getProxy()
            .getPlayers()) {
            if (player.hasPermission("timocloud.notify.server")){
                player.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Formats the raw message string by replacing placeholders with actual server data.
     *
     * @param rawMessage The message string with placeholders.
     * @param serverObject The ServerObject containing the data.
     * @return The formatted message string.
     */
    private String formatServerMessage(String rawMessage, ServerObject serverObject) {
        String message = rawMessage;

        message = message.replace("{server_name}", serverObject.getName());
        message = message.replace("{server_id}", serverObject.getId());
        message = message.replace("{server_state}", serverObject.getState());
        message = message.replace("{online_players}", String.valueOf(serverObject.getOnlinePlayerCount()));
        message = message.replace("{max_players}", String.valueOf(serverObject.getMaxPlayerCount()));
        message = message.replace("{ip_address}", serverObject.getIpAddress() != null ? serverObject.getIpAddress().getHostAddress() : "N/A");
        message = message.replace("{port}", String.valueOf(serverObject.getPort()));

        message = message.replace("{server_extra}", serverObject.getExtra() != null ? serverObject.getExtra() : "");
        message = message.replace("{server_map}", serverObject.getMap() != null ? serverObject.getMap() : "");
        message = message.replace("{server_motd}", serverObject.getMotd() != null ? serverObject.getMotd() : "");

        if (serverObject.getGroup() != null) {
            message = message.replace("{group_name}", serverObject.getGroup().getName());
        } else {
            message = message.replace("{group_name}", "N/A");
        }

        if (serverObject.getBase() != null) {
            message = message.replace("{base_name}", serverObject.getBase().getName());
        } else {
            message = message.replace("{base_name}", "N/A");
        }

        return ChatColorUtil.translateAlternateColorCodes('&', message);
    }

    private enum NotifyType {
        REGISTER, UNREGISTER;
    }
}
