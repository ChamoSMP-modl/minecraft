package gg.modl.minecraft.spigot.lamp;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class BukkitAdventureActorFactory implements ActorFactory<BukkitCommandActor> {
    @Override
    public @NonNull BukkitCommandActor create(@NotNull CommandSender sender, @NotNull Lamp<BukkitCommandActor> lamp) {
        return new BukkitAdventureCommandActor(sender, lamp);
    }
}