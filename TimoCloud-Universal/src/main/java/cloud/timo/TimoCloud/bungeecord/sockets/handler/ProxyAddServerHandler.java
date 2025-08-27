package cloud.timo.TimoCloud.bungeecord.sockets.handler;

import cloud.timo.TimoCloud.bungeecord.TimoCloudBungee;
import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.common.protocol.MessageType;
import cloud.timo.TimoCloud.common.sockets.handler.MessageHandler;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class ProxyAddServerHandler extends MessageHandler {
    public ProxyAddServerHandler() {
        super(MessageType.PROXY_ADD_SERVER);
    }

    @Override
    public void execute(Message message, Channel channel) {
        int port = ((Number) message.get("port")).intValue();
        String serverName = (String) message.get("name");
        String address = (String) message.get("address");

        if (port <= 0) {
            TimoCloudBungee.getInstance().warning("Attempted to register server " + serverName +
                " with invalid port " + port + ". Skipping registration.");
            return;
        }

        try {
            TimoCloudBungee.getInstance().getProxy().getServers().put(serverName,
                TimoCloudBungee.getInstance().getProxy().constructServerInfo(serverName,
                    new InetSocketAddress(address, port), "", false));
            TimoCloudBungee.getInstance().info("Successfully registered server " + serverName +
                " at " + address + ":" + port);
        } catch (Exception e) {
            TimoCloudBungee.getInstance().severe("Failed to register server " + serverName +
                " at " + address + ":" + port + ": " + e.getMessage());
        }
    }
}
