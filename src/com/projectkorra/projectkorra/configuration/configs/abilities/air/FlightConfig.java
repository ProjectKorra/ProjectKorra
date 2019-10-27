package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FlightConfig extends AbilityConfig {

	public final long Cooldown = 30000;
	public final long Duration = 25000;
	public final double BaseSpeed = 1.25;
	
	public FlightConfig() {
		super(true, "Fly through the air as Zaheer and Guru Laghima did! This multiability allows for three modes of flight: soaring, gliding, and levitating. You can also right-click another player while flying to have them become your passenger! When flying at fast speeds, flying past nearby enemies will damage them for half your speed and knock them in the direction you're heading!", "\\n- (To start flying, jump and left-click)\\n- (Soar) Left-Click to change flying speeds.\\n- (Glide) Normal minecraft gliding. Slowing down or speeding up in this mode will affect the Soar speed.\\n- (Levitate) Basically minecraft flying, allowing players to fly around for building purposes or a more controlled 'hovering'.\\n- (Ending) Being in this mode sets any gliding and flight back the the state they were before using the ability.");
	}

	@Override
	public String getName() {
		return "Flight";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}