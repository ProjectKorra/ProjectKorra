package com.projectkorra.projectkorra.firebending

import com.projectkorra.projectkorra.Element.SubElement
import com.projectkorra.projectkorra.GeneralMethods
import com.projectkorra.projectkorra.ProjectKorra
import com.projectkorra.projectkorra.ability.*
import com.projectkorra.projectkorra.attribute.Attribute
import com.projectkorra.projectkorra.avatar.AvatarState
import com.projectkorra.projectkorra.command.Commands
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer
import com.projectkorra.projectkorra.util.DamageHandler
import com.projectkorra.projectkorra.utils.manager.LevelManager
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.block.data.type.Campfire
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import java.util.*

class FireBlast : FireAbility {
    @Attribute("PowerFurnace")
    var isPowerFurnace = false
    var isShowParticles = false
    var isDissipate = false
    var isFireBurst = false
    private var fireBurstIgnite = false
    var ticks = 0

    @Attribute(Attribute.COOLDOWN)
    private var cooldown: Long = 0
    var speedFactor = 0.0

    @Attribute(Attribute.RANGE)
    var range = 0.0

    @Attribute(Attribute.DAMAGE)
    var damage: Double = 0.0

    @Attribute(Attribute.SPEED)
    var speed = 0.0
    private var collisionRadius = 0.0

    @Attribute(Attribute.FIRE_TICK)
    var fireTicks = 0.0

    @Attribute(Attribute.KNOCKBACK)
    var pushFactor = 0.0
    private var flameRadius = 0.0
    var random: Random? = null
    private lateinit var location: Location
    lateinit var origin: Location
    lateinit var direction: Vector
    lateinit var safeBlocks: List<Block>
        private set


    constructor(location: Location, direction: Vector, player: Player?, damage: Int, safeBlocks: List<Block>) : super(
        player
    ) {
        if (location.block.isLiquid) {
            return
        }
        setFields()
        this.safeBlocks = safeBlocks
        this.damage = damage.toDouble()
        this.location = location.clone()
        origin = location.clone()
        this.direction = direction.clone().normalize()


        // The following code determines the total additive modifier between Blue Fire & Day Modifiers
        applyModifiers(this.damage, range)
        start()
    }

    constructor(player: Player) : super(player) {
        if (bPlayer.isOnCooldown("FireBlast")) {
            return
        } else if (player.eyeLocation.block.isLiquid || FireBlastCharged.isCharging(player)) {
            return
        }
        setFields()
        isFireBurst = false
        damage = getConfig().getDouble("Abilities.Fire.FireBlast.Damage")
        safeBlocks = ArrayList()
        location = player.eyeLocation
        origin = player.eyeLocation
        direction = player.eyeLocation.direction.normalize()
        location = location.add(direction.clone())

        // The following code determines the total additive modifier between Blue Fire & Day Modifiers
        applyModifiers(damage, range)
        start()
        bPlayer.addCooldown("FireBlast", cooldown)
    }

    private fun applyModifiers(damage: Double, range: Double) {
        var damageMod = 0
        var rangeMod = 0
        damageMod = (this.getDayFactor(damage) - damage).toInt()
        rangeMod = (this.getDayFactor(range) - range).toInt()
        damageMod =
            (if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) BlueFireAbility.getDamageFactor() * damage - damage + damageMod else damageMod) as Int
        rangeMod =
            (if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) BlueFireAbility.getRangeFactor() * range - range + rangeMod else rangeMod) as Int
        this.range += rangeMod.toDouble()
        this.damage += damageMod.toDouble()
    }

    private fun setFields() {
        isFireBurst = true
        isPowerFurnace = true
        isShowParticles = true
        fireBurstIgnite = getConfig().getBoolean("Abilities.Fire.FireBurst.Ignite")
        isDissipate = getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate")
        cooldown = getConfig().getLong("Abilities.Fire.FireBlast.Cooldown")
        range = getConfig().getDouble("Abilities.Fire.FireBlast.Range")
        speed = getConfig().getDouble("Abilities.Fire.FireBlast.Speed")
        collisionRadius = getConfig().getDouble("Abilities.Fire.FireBlast.CollisionRadius")
        fireTicks = getConfig().getDouble("Abilities.Fire.FireBlast.FireTicks")
        pushFactor = getConfig().getDouble("Abilities.Fire.FireBlast.Knockback")
        flameRadius = getConfig().getDouble("Abilities.Fire.FireBlast.FlameParticleRadius")
        random = Random()
    }

    private fun advanceLocation() {
        if (isFireBurst) {
            flameRadius += 0.06
        }
        if (isShowParticles) {
            playFirebendingParticles(location, 6, flameRadius, flameRadius, flameRadius)
        }
        if (GeneralMethods.checkDiagonalWall(location, direction)) {
            this.remove()
            return
        }
        val blocks = BlockIterator(
            getLocation().world!!, location.toVector(), direction, 0.0,
            Math.ceil(direction.clone().multiply(speedFactor).length()).toInt()
        )
        while (blocks.hasNext() && checkLocation(blocks.next()));
        location.add(direction.clone().multiply(speedFactor))
        if (random!!.nextInt(4) == 0) {
            playFirebendingSound(location)
        }
    }

    fun checkLocation(block: Block): Boolean {
        if (!block.isPassable) {
            if (block.type == Material.FURNACE && isPowerFurnace) {
                val furnace = block.state as Furnace
                furnace.burnTime = 800.toShort()
                furnace.update()
            } else if (block.type == Material.SMOKER && isPowerFurnace) {
                val smoker = block.state as Smoker
                smoker.burnTime = 800.toShort()
                smoker.update()
            } else if (block.type == Material.BLAST_FURNACE && isPowerFurnace) {
                val blastF = block.state as BlastFurnace
                blastF.burnTime = 800.toShort()
                blastF.update()
            } else if (block is Campfire) {
                val campfire = block.blockData as Campfire
                if (!campfire.isLit) {
                    if (block.type != Material.SOUL_CAMPFIRE || bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
                        campfire.isLit = true
                    }
                }
            } else if (isIgnitable(block.getRelative(BlockFace.UP))) {
                if (isFireBurst && fireBurstIgnite || !isFireBurst) {
                    ignite(location)
                }
            }
            this.remove()
            return false
        }
        return true
    }

    private fun affect(entity: Entity) {
        if (entity.uniqueId !== player.uniqueId && !GeneralMethods.isRegionProtectedFromBuild(
                this,
                entity.location
            ) && !(entity is Player && Commands.invincible.contains(
                entity.name
            ))
        ) {
            if (bPlayer.isAvatarState) {
                GeneralMethods.setVelocity(this, entity, direction.clone().multiply(AvatarState.getValue(pushFactor)))
            } else {
                GeneralMethods.setVelocity(this, entity, direction.clone().multiply(pushFactor))
            }
            if (entity is LivingEntity) {
                entity.setFireTicks((fireTicks * 20).toInt())
                DamageHandler.damageEntity(entity, damage, this)
                AirAbility.breakBreathbendingHold(entity)
                FireDamageTimer(entity, player)
                this.remove()
            }
        }
    }

    private fun ignite(location: Location) {
        for (block in GeneralMethods.getBlocksAroundPoint(location, collisionRadius)) {
            if (isIgnitable(block) && !safeBlocks.contains(block) && !GeneralMethods.isRegionProtectedFromBuild(
                    this,
                    block.location
                )
            ) {
                if (canFireGrief()) {
                    if (isPlant(block) || isSnow(block)) {
                        PlantRegrowth(player, block)
                    }
                }
                createTempFire(block.location)
            }
        }
    }

    override fun progress() {
        if (!bPlayer.canBendIgnoreBindsCooldowns(this) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
            this.remove()
            return
        }
        speedFactor = speed * (ProjectKorra.time_step / 1000.0)
        ticks++
        if (ticks > maxTicks) {
            this.remove()
            return
        }
        if (location.distanceSquared(origin) > range * range) {
            this.remove()
            return
        }
        val entity = GeneralMethods.getClosestEntity(location, collisionRadius)
        if (entity != null) {
            affect(entity)
        }
        advanceLocation()
    }

    override fun getName(): String {
        return if (isFireBurst) "FireBurst" else "FireBlast"
    }

    override fun getLocation(): Location {
        return if (location != null) location else origin
    }

    override fun getCooldown(): Long {
        return cooldown
    }

    override fun isSneakAbility(): Boolean {
        return true
    }

    override fun isHarmlessAbility(): Boolean {
        return false
    }

    override fun getCollisionRadius(): Double {
        return collisionRadius
    }

    fun setCollisionRadius(collisionRadius: Double) {
        this.collisionRadius = collisionRadius
    }

    fun setCooldown(cooldown: Long) {
        this.cooldown = cooldown
    }

    fun setLocation(location: Location) {
        this.location = location
    }

    companion object {
        const val maxTicks = 10000

        /**
         * This method was used for the old collision detection system. Please see
         * [Collision] for the new system.
         */
        @Deprecated("")
        fun annihilateBlasts(location: Location, radius: Double, source: Player): Boolean {
            var broke = false
            for (blast in getAbilities(FireBlast::class.java)) {
                val fireBlastLocation = blast.location
                if (location.world == fireBlastLocation.world && blast.player != source) {
                    if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
                        blast.remove()
                        broke = true
                    }
                }
            }
            if (FireBlastCharged.annihilateBlasts(location, radius, source)) {
                broke = true
            }
            return broke
        }

        fun getAroundPoint(location: Location, radius: Double): ArrayList<FireBlast> {
            val list = ArrayList<FireBlast>()
            for (fireBlast in getAbilities(FireBlast::class.java)) {
                val fireblastlocation = fireBlast.location
                if (location.world == fireblastlocation.world) {
                    if (location.distanceSquared(fireblastlocation) <= radius * radius) {
                        list.add(fireBlast)
                    }
                }
            }
            return list
        }

        fun removeFireBlastsAroundPoint(location: Location, radius: Double) {
            for (fireBlast in getAbilities(FireBlast::class.java)) {
                val fireBlastLocation = fireBlast.location
                if (location.world == fireBlastLocation.world) {
                    if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
                        fireBlast.remove()
                    }
                }
            }
            FireBlastCharged.removeFireballsAroundPoint(location, radius)
        }
    }
}