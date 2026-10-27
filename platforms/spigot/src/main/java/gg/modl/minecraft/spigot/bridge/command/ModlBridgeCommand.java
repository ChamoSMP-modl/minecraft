package gg.modl.minecraft.spigot.bridge.command;

import gg.modl.minecraft.bridge.BridgeReloadPresenter;
import lombok.RequiredArgsConstructor;
import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

@Permission("modl.bridge.reload")
@Command("modlbridge")
@RequiredArgsConstructor
public class ModlBridgeCommand {
    private final BridgeReloadPresenter presenter;

    @Executes("reload")
    public void onReload(CommandSender sender) {
        presenter.reload(sender::sendRichMessage);
    }

    @DefaultExecutes
    public void onDefault(CommandSender sender) {
        sender.sendRichMessage(presenter.usage());
    }
}