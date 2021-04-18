package com.projectkorra.projectkorra.configuration.fire

interface FireBlast {
    val damage: Double
    val dissipate: Boolean
    val cooldown: Long
    val range: Double
    val speed: Double
    val collisionRadius: Double
    val fireTicks: Double
    val knockback: Double
    val flameParticlesRadius: Double
}