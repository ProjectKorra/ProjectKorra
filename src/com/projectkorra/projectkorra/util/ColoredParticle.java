package com.projectkorra.projectkorra.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;

public class ColoredParticle {

	private DustOptions dust;
	
	public ColoredParticle(Color color, float size) {
		dust = new DustOptions(color, size);
	}
	
	public DustOptions getDustOptions() {
		return dust;
	}
	
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		ParticleEffect.REDSTONE.display(loc, amount, offsetX, offsetY, offsetZ, dust);
	}
}
