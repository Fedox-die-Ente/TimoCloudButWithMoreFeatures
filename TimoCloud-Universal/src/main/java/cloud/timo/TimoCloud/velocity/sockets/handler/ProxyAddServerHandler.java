package cloud.timo.TimoCloud.velocity.sockets.handler;

import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.common.protocol.MessageType;
import cloud.timo.TimoCloud.common.sockets.handler.MessageHandler;
import cloud.timo.TimoCloud.velocity.TimoCloudVelocity;
import com.velocitypowered.api.proxy.server.ServerInfo;
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
            TimoCloudVelocity.getInstance().getLogger().warn("Attempted to register server " + serverName +
                " with invalid port " + port + ". Skipping registration.");
            return;
        }

        try {
            TimoCloudVelocity.getInstance().getServer().registerServer(
                new ServerInfo(serverName, new InetSocketAddress(address, port)));
            TimoCloudVelocity.getInstance().getLogger().info("Successfully registered server " + serverName +
                " at " + address + ":" + port);
        } catch (Exception e) {
            TimoCloudVelocity.getInstance().getLogger().error("Failed to register server " + serverName +
                " at " + address + ":" + port + ": " + e.getMessage());
        }
    }
}
