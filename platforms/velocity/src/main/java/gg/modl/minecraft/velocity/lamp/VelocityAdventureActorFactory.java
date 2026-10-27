package gg.modl.minecraft.velocity.lamp;

import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.velocity.actor.ActorFactory;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

public class VelocityAdventureActorFactory implements ActorFactory<VelocityCommandActor> {
    @Override
    public @NonNull VelocityCommandActor create(@NotNull CommandSource commandSource, @NotNull Lamp<VelocityCommandActor> lamp) {
        return new VelocityAdventureCommandActor(commandSource, lamp);
    }
}