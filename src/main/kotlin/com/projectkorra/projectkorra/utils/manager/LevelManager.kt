package com.projectkorra.projectkorra.utils.manager

import com.projectkorra.projectkorra.DatabaseRoom
import com.projectkorra.projectkorra.ability.CoreAbility.getConfig
import com.projectkorra.projectkorra.configuration.Fire
import com.projectkorra.projectkorra.configuration.Water
import com.projectkorra.projectkorra.configuration.fire.FireBlast
import com.projectkorra.projectkorra.configuration.water.IceWave
import com.projectkorra.projectkorra.configuration.water.WaterSpout
import org.bukkit.entity.Player

class LevelManager (val player: Player){
    val level by lazy { DatabaseRoom.getPlayerLevel(player) }
    /*val Water = object: Water {
        override val WaterSpout = object: WaterSpout{
            override val allowPlantSource: Boolean
                get() = TODO("Not yet implemented")
            override val radius: Double
                get() = TODO("Not yet implemented")
            override val waveRadius: Double
                get() = TODO("Not yet implemented")
            override val animationSpeed: Double
                get() = TODO("Not yet implemented")
            override val selectRange: Double
                get() = TODO("Not yet implemented")
            override val speed: Double
                get() = TODO("Not yet implemented")
            override val chargeTime: Long
                get() = TODO("Not yet implemented")
            override val flightDuration: Long
                get() = TODO("Not yet implemented")
            override val cooldown: Long
                get() = TODO("Not yet implemented")
        }
        override val IceWave = object: IceWave{
            override val ThawRadius: Double
                get() = TODO("Not yet implemented")
            override val Damage: Double
                get() = TODO("Not yet implemented")
            override val RevertSphereTime: Long
                get() = TODO("Not yet implemented")
            override val RevertSphere: Boolean
                get() = TODO("Not yet implemented")

        }
    }*/
    val config = getConfig()
    val Fire = object: Fire{
        override val FireBlast = object:FireBlast {
            override val damage: Double
                get() {
                    return config.getDouble("Abilities.Fire.FireBlast.Damage")
                }
            override val dissipate: Boolean
                get() = TODO("Not yet implemented")
            override val cooldown: Long
                get() = TODO("Not yet implemented")
            override val range: Double
                get() = TODO("Not yet implemented")
            override val speed: Double
                get() = TODO("Not yet implemented")
            override val collisionRadius: Double
                get() = TODO("Not yet implemented")
            override val fireTicks: Double
                get() = TODO("Not yet implemented")
            override val knockback: Double
                get() = TODO("Not yet implemented")
            override val flameParticlesRadius: Double
                get() = TODO("Not yet implemented")

        }

    }
}
