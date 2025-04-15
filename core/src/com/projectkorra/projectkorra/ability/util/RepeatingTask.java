package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import org.bukkit.Location;

public abstract class RepeatingTask extends ElementalAbility {

    private final CoreAbility parentAbility;

    public RepeatingTask(CoreAbility parentAbility) {
        super(parentAbility.getPlayer());
        this.parentAbility = parentAbility;
    }

    public CoreAbility getParentAbility() {
        return parentAbility;
    }

    @Override
    public Element getElement() {
        return parentAbility.getElement();
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public boolean isHiddenAbility() {
        return true;
    }

    @Override
    public boolean isIgniteAbility() {
        return parentAbility.isIgniteAbility();
    }

    @Override
    public boolean isHarmlessAbility() {
        return parentAbility.isHarmlessAbility();
    }

    @Override
    public boolean isExplosiveAbility() {
        return parentAbility.isExplosiveAbility();
    }

    @Override
    public String getName() {
        return parentAbility.getName() + "Task";
    }

    @Override
    public Location getLocation() {
        return parentAbility.getLocation();
    }

    @Override
    public boolean isSneakAbility() {
        return parentAbility.isSneakAbility();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
