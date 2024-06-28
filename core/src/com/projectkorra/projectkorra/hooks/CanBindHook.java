package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface CanBindHook {

    /**
     * Checks if a player can bind this ability. This should be used to change the way the canBind method functions. By default, this
     * should return {@link Optional#empty()}. To deny binding, return {@code Optional.of(false)}. To allow binding,
     * return {@code Optional.of(true)}
     * @param bPlayer The player being checked
     * @param ability The ability bind being checked
     * @return Whether to deny or grant the player the ability to bind the ability
     */
    @NotNull
    Optional<Boolean> canBind(@NotNull BendingPlayer bPlayer, @NotNull CoreAbility ability);
}
