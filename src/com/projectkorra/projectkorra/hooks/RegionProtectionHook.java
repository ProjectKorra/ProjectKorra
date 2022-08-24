package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RegionProtectionHook {

    boolean isRegionProtected(@NotNull Player player, @NotNull Location location, @Nullable CoreAbility ability);
}
