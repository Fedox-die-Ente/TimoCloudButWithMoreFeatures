package cloud.timo.TimoCloud.core.commands;

import cloud.timo.TimoCloud.api.core.commands.CommandHandler;
import cloud.timo.TimoCloud.api.core.commands.CommandSender;
import cloud.timo.TimoCloud.core.TimoCloudCore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class HelpCommandHandler implements CommandHandler {

    private final Map<String, String> generalCommands = new LinkedHashMap<>();
    private final Map<String, String> baseManagementCommands = new LinkedHashMap<>();
    private final Map<String, String> groupManagementCommands = new LinkedHashMap<>();
    private final Map<String, String> serverProxyActionCommands = new LinkedHashMap<>();

    public HelpCommandHandler() {
        // General Commands
        generalCommands.put("help", "&6help &7- &7Shows this help page.");
        generalCommands.put("version", "&6version &7- &7Shows the plugin version.");
        generalCommands.put("reload", "&6reload &7- &7Reloads all configurations.");
        generalCommands.put("reloadPlugins", "&6reloadPlugins &7- &7Reloads all plugins.");
        generalCommands.put("shutdown", "&6shutdown &7- &7Shuts down TimoCloud Core.");

        // Base Management Commands
        baseManagementCommands.put("addbase", "&6addbase <publicKey> &7- &7Registers a new base.");
        baseManagementCommands.put("editbase", "&6editbase <name> <setting> <value> &7- &7Edits a setting of a base. &b(Settings: name, maxRam, keepFreeRam, maxCpuLoad)");
        baseManagementCommands.put("baseinfo", "&6baseinfo <baseName> &7- &7Displays base information.");
        baseManagementCommands.put("listbases", "&6listbases &7- &7Lists all registered bases.");

        // Group Management Commands
        groupManagementCommands.put("creategroup server", "&6creategroup server <groupName> <onlineAmount> <ram> <static> [base] &7- &7Creates a server group. &b([base] only if static=true)");
        groupManagementCommands.put("creategroup proxy", "&6creategroup proxy <groupName> <ram> <static> [base] &7- &7Creates a proxy group. &b([base] only if static=true)");
        groupManagementCommands.put("deletegroup", "&6deletegroup <groupName> &7- &7Deletes a group.");
        groupManagementCommands.put("editgroup server", "&6editgroup <name> <setting> <value> &7- &7Edits a setting of a server group. &b(Settings: onlineAmount, maxAmount, ram, static, priority, base, jrePath)");
        groupManagementCommands.put("editgroup proxy", "&6editgroup <name> <setting> <value> &7- &7Edits a setting of a proxy group. &b(Settings: playersPerProxy, maxPlayers, keepFreeSlots, minAmount, maxAmount, ram, static, priority, base, jrePath)");
        groupManagementCommands.put("groupinfo", "&6groupinfo <groupName> &7- &7Displays group information.");
        groupManagementCommands.put("listgroups", "&6listgroups &7- &7Lists all groups and started servers.");

        // Server/Proxy Action Commands
        serverProxyActionCommands.put("start", "&6start <groupName> &7- &7Starts a server or proxy group.");
        serverProxyActionCommands.put("restart", "&6restart <groupName|baseName|serverName|proxyName> &7- &7Restarts the given group, base, server, or proxy.");
        serverProxyActionCommands.put("sendcommand", "&6sendcommand <groupName|serverName|proxyName> <command> &7- &7Sends the given command to all servers of a group or a specific server/proxy.");
    }

    @Override
    public void onCommand(String command, CommandSender sender, String... args) {
        sender.sendMessage("&6--- &bTimo&fCloud &6Help &6---");
        sender.sendMessage(" ");

        sendCategory(sender, "&eGeneral:", generalCommands);
        sendCategory(sender, "&eBase Management:", baseManagementCommands);
        sendCategory(sender, "&eGroup Management:", groupManagementCommands);
        sendCategory(sender, "&eServer/Proxy Actions:", serverProxyActionCommands);

        Set<String> pluginCommands = TimoCloudCore.getInstance().getCommandManager().getPluginCommandHandlers().keySet();
        if (!pluginCommands.isEmpty()) {
            sender.sendMessage(" ");
            sender.sendMessage("&eAvailable Plugin Commands:");
            for (String pluginCommand : pluginCommands) {
                sender.sendMessage("  &6" + pluginCommand);
            }
        }
        sender.sendMessage(" ");
        sender.sendMessage("&6--------------------");
    }

    private void sendCategory(CommandSender sender, String categoryTitle, Map<String, String> commands) {
        sender.sendMessage(categoryTitle);
        commands.values().forEach(sender::sendMessage);
        sender.sendMessage(" ");
    }
}
