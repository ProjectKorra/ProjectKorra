package com.projectkorra.projectkorra.configuration.water

interface WaterSpout {
    val allowPlantSource: Boolean
    val radius: Double
    val waveRadius: Double
    val animationSpeed: Double
    val selectRange: Double
    val speed: Double
    val chargeTime: Long
    val flightDuration: Long
    val cooldown: Long
}