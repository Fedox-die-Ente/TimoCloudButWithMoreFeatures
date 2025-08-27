package cloud.timo.TimoCloud.velocity.listeners;

import cloud.timo.TimoCloud.api.objects.ServerObject;
import cloud.timo.TimoCloud.velocity.TimoCloudVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class ServerKick {

    @Subscribe(order = PostOrder.LAST)
    public void onServerKickEvent(KickedFromServerEvent event) {
        if (!TimoCloudVelocity.getInstance().getFileManager().getConfig().getBoolean("useFallback")) return;
        if (!event.getPlayer().getCurrentServer().isPresent()) return;

        TimoCloudVelocity.getInstance().info("Player " + event.getPlayer().getUsername() +
            " kicked from " + event.getPlayer().getCurrentServer().get().getServerInfo().getName());

        final ServerObject freeLobby = TimoCloudVelocity.getInstance().getLobbyManager().getFreeLobby(event.getPlayer().getUniqueId(), true);
        if (freeLobby == null) {
            TimoCloudVelocity.getInstance().severe("No fallback server found for player " + event.getPlayer().getUsername());
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(
                net.kyori.adventure.text.Component.text("§cEs konnte kein Unterserver gefunden werden, womit du dich verbinden kannst.")));
            return;
        }

        final Optional<RegisteredServer> server = TimoCloudVelocity.getInstance().getServer().getServer(freeLobby.getName());
        if (!server.isPresent()) {
            TimoCloudVelocity.getInstance().severe("Fallback server " + freeLobby.getName() +
                " not registered in Velocity for player " + event.getPlayer().getUsername());
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(
                net.kyori.adventure.text.Component.text("§cEs konnte kein Unterserver gefunden werden, womit du dich verbinden kannst.")));
            return;
        }

        if (server.get().getServerInfo().getAddress().getPort() <= 0) {
            TimoCloudVelocity.getInstance().severe("Fallback server " + freeLobby.getName() +
                " has invalid port " + server.get().getServerInfo().getAddress().getPort());
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(
                net.kyori.adventure.text.Component.text("§cEs konnte kein Unterserver gefunden werden, womit du dich verbinden kannst.")));
            return;
        }

        TimoCloudVelocity.getInstance().info("Connecting " + event.getPlayer().getUsername() +
            " to fallback server: " + server.get().getServerInfo().getName() +
            " at " + server.get().getServerInfo().getAddress());
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.get()));
    }

}
