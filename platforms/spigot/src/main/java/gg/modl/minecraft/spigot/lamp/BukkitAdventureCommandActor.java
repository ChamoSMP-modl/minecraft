package gg.modl.minecraft.spigot.lamp;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public record BukkitAdventureCommandActor(
        CommandSender sender,
        Lamp<BukkitCommandActor> lamp
) implements BukkitCommandActor {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);


    @Override
    public @NotNull CommandSender sender() {
        return sender;
    }

    @Override
    public void reply(@NotNull ComponentLike message) {
        sender.sendMessage(message);
    }

    @Override
    public @NotNull Optional<Audience> audience() {
        return Optional.of(sender);
    }

    @Override
    public @NotNull UUID uniqueId() {
        if (sender instanceof Player player) {
            return player.getUniqueId();
        } else if (sender instanceof ConsoleCommandSender) {
            return CONSOLE_UUID;
        } else {
            return UUID.nameUUIDFromBytes(name().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public @NotNull Lamp<BukkitCommandActor> lamp() {
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
        reply(message);
    }

    @Override
    public void sendRawError(@NotNull String message) {
        error(message);
    }
}
