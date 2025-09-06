package cloud.timo.TimoCloud.velocity.listeners;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.events.player.PlayerConnectEventBasicImplementation;
import cloud.timo.TimoCloud.api.events.player.PlayerDisconnectEventBasicImplementation;
import cloud.timo.TimoCloud.api.events.player.PlayerServerChangeEventBasicImplementation;
import cloud.timo.TimoCloud.api.objects.PlayerObject;
import cloud.timo.TimoCloud.common.events.EventTransmitter;
import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.common.protocol.MessageType;
import cloud.timo.TimoCloud.velocity.TimoCloudVelocity;
import cloud.timo.TimoCloud.velocity.utils.PlayerUtil;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.experimental.var;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventMonitor {

    private final Set<UUID> recentlyKickedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> playerCurrentServer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Subscribe
    public void onPlayerConnect(PostLoginEvent event) {
        TimoCloudVelocity.getInstance().sendPlayerCount();
    }

    @Subscribe
    public void onServerSwitchEvent(ServerConnectedEvent event) {
        playerCurrentServer.put(event.getPlayer().getUniqueId(), event.getServer().getServerInfo().getName());

        if (!event.getPreviousServer().isPresent()) { // Join
            EventTransmitter.sendEvent(new PlayerConnectEventBasicImplementation(PlayerUtil.playerToObject(event.getPlayer(), event.getServer())));
        } else { // Server change
            EventTransmitter.sendEvent(new PlayerServerChangeEventBasicImplementation(
                PlayerUtil.playerToObject(event.getPlayer(), event.getServer()),
                event.getPreviousServer().get().getServerInfo().getName(),
                event.getServer().getServerInfo().getName()));
        }
    }

    @Subscribe
    public void onPlayerQuitEvent(DisconnectEvent event) {
        TimoCloudVelocity.getInstance().sendPlayerCount();
        if (!event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN)) return;

        UUID playerUUID = event.getPlayer().getUniqueId();
        playerCurrentServer.remove(playerUUID);

        if (recentlyKickedPlayers.contains(playerUUID)) {
            recentlyKickedPlayers.remove(playerUUID);
            return; // Skip sending disconnect event as it was already sent by kick handler
        }

        EventTransmitter.sendEvent(new PlayerDisconnectEventBasicImplementation(getPlayer(event.getPlayer())));
    }

    @Subscribe
    public void onPlayerKickedEvent(KickedFromServerEvent event) {
        // Only handle kicks that result in complete disconnection from the proxy
        if (event.getResult() instanceof KickedFromServerEvent.DisconnectPlayer) {
            TimoCloudVelocity.getInstance().sendPlayerCount();

            UUID playerUUID = event.getPlayer().getUniqueId();
            String originalServerName = playerCurrentServer.get(playerUUID);

            PlayerObject playerObject;
            if (originalServerName != null) {
                // Use the original server the player was on before being kicked
                RegisteredServer originalServer = TimoCloudVelocity.getInstance().getServer().getServer(originalServerName).orElse(null);
                playerObject = PlayerUtil.playerToObject(event.getPlayer(), originalServer, false);
                playerCurrentServer.remove(playerUUID);
            } else {
                // Fallback to event server if tracking failed
                playerObject = PlayerUtil.playerToObject(event.getPlayer(), event.getServer(), false);
            }

            if (playerObject != null) {
                recentlyKickedPlayers.add(playerUUID);

                // Remove from tracking after 5 seconds to prevent memory leaks
                scheduler.schedule(() -> recentlyKickedPlayers.remove(playerUUID), 5, TimeUnit.SECONDS);

                EventTransmitter.sendEvent(new PlayerDisconnectEventBasicImplementation(playerObject));
            }
        }
        // If player is redirected to another server, the ServerConnectedEvent will handle the server change
    }

    private PlayerObject getPlayer(Player player) {
        PlayerObject existingPlayer = TimoCloudAPI.getUniversalAPI().getPlayer(player.getUniqueId());
        if (existingPlayer != null) {
            return existingPlayer;
        }
        // Fallback to creating new player object
        return PlayerUtil.playerToObject(player);
    }

}
