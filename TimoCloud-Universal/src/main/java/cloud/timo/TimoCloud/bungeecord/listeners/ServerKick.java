package cloud.timo.TimoCloud.bungeecord.listeners;

import cloud.timo.TimoCloud.api.objects.ServerObject;
import cloud.timo.TimoCloud.bungeecord.TimoCloudBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerKick implements Listener {

    @EventHandler(priority = 65)
    public void onServerKickEvent(ServerKickEvent event) {
        if (!TimoCloudBungee.getInstance().getFileManager().getConfig().getBoolean("useFallback")) return;
        if (!event.getPlayer().isConnected()) return;
        final ServerObject freeLobby = TimoCloudBungee.getInstance().getLobbyManager().getFreeLobby(event.getPlayer().getUniqueId(), true);
        if (freeLobby == null) {
            TimoCloudBungee.getInstance().info("No fallback server found for player " + event.getPlayer().getName());
            return;
        }

        if (freeLobby.getSocketAddress() == null || freeLobby.getSocketAddress().getPort() <= 0) {
            TimoCloudBungee.getInstance().warning("Fallback server " + freeLobby.getName() +
                " has invalid port. Skipping fallback connection.");
            return;
        }

        ServerInfo server = ProxyServer.getInstance().getServerInfo(freeLobby.getName());
        if (server == null) {
            TimoCloudBungee.getInstance().info("No fallback server found for player " + event.getPlayer().getName());
            return;
        }

        if (server.getName().equals(event.getCancelServer().getName()))
            return;
        TimoCloudBungee.getInstance().info("Connecting " + event.getPlayer().getName() +
            " to fallback server: " + server.getName() + " (" + server.getAddress() + ")");
        event.setCancelled(true);
        event.setCancelServer(server);
    }

}
