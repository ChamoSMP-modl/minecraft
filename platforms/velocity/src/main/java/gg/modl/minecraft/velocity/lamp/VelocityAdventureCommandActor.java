package gg.modl.minecraft.velocity.lamp;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record VelocityAdventureCommandActor(
        CommandSource sender,
        Lamp<VelocityCommandActor> lamp
) implements VelocityCommandActor {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);


    @Override
    public @NotNull CommandSource source() {
        return sender;
    }

    @Override
    public void reply(@NotNull ComponentLike message) {
        sender.sendMessage(message);
    }

    @Override
    public void error(@NotNull ComponentLike componentLike) {
        sender.sendMessage(componentLike);
    }

    @Override
    public @NotNull UUID uniqueId() {
        if (sender instanceof Player player) {
            return player.getUniqueId();
        } else if (sender instanceof ConsoleCommandSource) {
            return CONSOLE_UUID;
        } else {
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public @NotNull Lamp<VelocityCommandActor> lamp() {
        return lamp;
    }

    @Override
    public void reply(@NotNull String message) {
        reply(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void error(@NotNull String message) {
        reply(MiniMessage.miniMessage().deserialize("<red>" + message));
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        reply(MiniMessage.miniMessage().deserialize(message));
    }
}