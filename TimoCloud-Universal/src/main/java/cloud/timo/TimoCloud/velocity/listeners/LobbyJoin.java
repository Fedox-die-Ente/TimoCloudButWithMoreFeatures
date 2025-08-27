package cloud.timo.TimoCloud.velocity.listeners;

import cloud.timo.TimoCloud.api.objects.ServerObject;
import cloud.timo.TimoCloud.common.utils.ChatColorUtil;
import cloud.timo.TimoCloud.velocity.TimoCloudVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LobbyJoin {
    private final Set<UUID> pending;

    public LobbyJoin() {
        pending = new HashSet<>();
    }

    private boolean isPending(UUID uuid) {
        return pending.contains(uuid);
    }

    @Subscribe
    public void onPlayerConnect(PostLoginEvent event) {
        if (!useFallback()) return;
        pending.add(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        if (!useFallback()) return;
        if (!isPending(event.getPlayer().getUniqueId())) return;

        Player player = event.getPlayer();

        TimoCloudVelocity.getInstance().info("Finding lobby server for player " + player.getUsername());

        final ServerObject freeLobby = TimoCloudVelocity.getInstance().getLobbyManager().getFreeLobby(player.getUniqueId());
        if (freeLobby == null) {
            TimoCloudVelocity.getInstance().severe("No lobby server found for player " + player.getUsername());
            pending.remove(player.getUniqueId());
            kickPlayer(player);
            return;
        }

        final Optional<RegisteredServer> server = TimoCloudVelocity.getInstance().getServer().getServer(freeLobby.getName());
        if (!server.isPresent()) {
            TimoCloudVelocity.getInstance().severe("Lobby server " + freeLobby.getName() +
                " not registered in Velocity for player " + player.getUsername());
            pending.remove(player.getUniqueId());
            kickPlayer(player);
            return;
        }

        if (server.get().getServerInfo().getAddress().getPort() <= 0) {
            TimoCloudVelocity.getInstance().severe("Lobby server " + freeLobby.getName() +
                " has invalid port " + server.get().getServerInfo().getAddress().getPort());
            pending.remove(player.getUniqueId());
            kickPlayer(player);
            return;
        }

        TimoCloudVelocity.getInstance().info("Connecting " + player.getUsername() +
            " to lobby server: " + server.get().getServerInfo().getName() +
            " at " + server.get().getServerInfo().getAddress());
        event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.get()));
        pending.remove(player.getUniqueId());
    }

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) {
        if (!useFallback()) return;

        TimoCloudVelocity.getInstance().info("LobbyJoin handling kick for player " + event.getPlayer().getUsername());

        final ServerObject freeLobby = TimoCloudVelocity.getInstance().getLobbyManager().getFreeLobby(event.getPlayer().getUniqueId());
        if (freeLobby == null) {
            TimoCloudVelocity.getInstance().severe("No fallback server found in LobbyJoin for player " + event.getPlayer().getUsername());
            return;
        }

        final Optional<RegisteredServer> server = TimoCloudVelocity.getInstance().getServer().getServer(freeLobby.getName());
        if (!server.isPresent()) {
            TimoCloudVelocity.getInstance().severe("Fallback server " + freeLobby.getName() +
                " not registered in Velocity (LobbyJoin) for player " + event.getPlayer().getUsername());
            return;
        }

        if (server.get().getServerInfo().getAddress().getPort() <= 0) {
            TimoCloudVelocity.getInstance().severe("Fallback server " + freeLobby.getName() +
                " has invalid port " + server.get().getServerInfo().getAddress().getPort() + " (LobbyJoin)");
            return;
        }

        TimoCloudVelocity.getInstance().info("LobbyJoin redirecting " + event.getPlayer().getUsername() +
            " to server: " + server.get().getServerInfo().getName());
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.get()));
    }

    private void kickPlayer(Player player) {
        String fallBackMessage = TimoCloudVelocity.getInstance().getFileManager().getMessages().getString("NoFallBackGroupFound");
        player.disconnect(Component.text(ChatColorUtil.translateAlternateColorCodes('&', fallBackMessage)));
    }

    private boolean useFallback() {
        return TimoCloudVelocity.getInstance().getFileManager().getConfig().getBoolean("useFallback");
    }

}
