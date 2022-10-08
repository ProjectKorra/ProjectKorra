package com.projectkorra.projectkorra.ability.functional;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;

public class Functional {
    @FunctionalInterface
    public interface Particle{
        void play(
                CoreAbility ability,
                Location location,
                int amount,
                double xOffset, double yOffset, double zOffset,
                double extra,
                Object data);
    }
}
