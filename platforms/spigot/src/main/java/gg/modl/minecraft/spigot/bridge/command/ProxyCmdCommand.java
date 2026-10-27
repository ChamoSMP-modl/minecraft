package gg.modl.minecraft.spigot.bridge.command;

import gg.modl.minecraft.bridge.locale.BridgeLocaleManager;
import gg.modl.minecraft.bridge.query.BridgeQueryClient;
import lombok.RequiredArgsConstructor;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.arguments.StringArg;
import net.strokkur.commands.arguments.StringArgType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static gg.modl.minecraft.core.util.Java8Collections.mapOf;

@Command("proxycmd")
@RequiredArgsConstructor
public class ProxyCmdCommand {
    private static final String PROXY_CMD_ACTION = "PROXY_CMD";

    private final JavaPlugin plugin;
    private final BridgeLocaleManager localeManager;
    private final BridgeQueryClient bridgeClient;

    @Executes
    public void onExecute(CommandSender sender, @StringArg(StringArgType.GREEDY) String command) {
        if (sender instanceof Player) {
            sender.sendRichMessage(localeManager.getMessage("command.proxycmd.console_only"));
        }
        if (!bridgeClient.isConnected()) {
            sender.sendRichMessage(localeManager.getMessage("command.proxycmd.not_connected"));
        }

        bridgeClient.sendMessage(PROXY_CMD_ACTION, command);
        plugin.getLogger().info("Forwarded command to proxy: " + command);
        sender.sendRichMessage(localeManager.getMessage("command.proxycmd.sent", mapOf("command", command)));
    }
}