package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.OptionalBoolean;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CanBendHook {

    /**
     * Checks if a player can bend. This should be used to change the way the canBend method functions. By default, this
     * should return {@link OptionalBoolean#DEFAULT}. To deny bending, return {@link OptionalBoolean#FALSE}. To allow bending,
     * return {@link OptionalBoolean#TRUE}.
     * @param bPlayer The player being checked
     * @param ability The ability being checked
     * @param isCheckingBind Whether the binds are being checked or not
     * @param isCheckingCooldown Whether the cooldowns are being checked or not
     * @return Whether to deny or grant the player the ability to use the ability
     */
    @NotNull
    OptionalBoolean canBend(@NotNull BendingPlayer bPlayer, @NotNull CoreAbility ability, boolean isCheckingBind, boolean isCheckingCooldown);
}
