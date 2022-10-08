package com.projectkorra.projectkorra.earthbending;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempArmorStand;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthGrab extends EarthAbility {

	private LivingEntity target;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long lastHit;
	private long interval;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double dragSpeed;
	@Attribute("TrapHealth")
	private double trapHP;
	private double trappedHP;
	private double damageThreshold;
	private GrabMode mode;
	private boolean initiated = false;
	private MovementHandler mHandler;
	private ArmorStand trap;
	private Location origin;
	private Vector direction;
	private TempArmor armor;
	private final Material[] crops = new Material[] { Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.SUGAR_CANE, Material.MELON, Material.PUMPKIN };

	public static enum GrabMode {
		TRAP, DRAG, PROJECTING;
	}

	public EarthGrab(final Player player, final GrabMode mode) {
		super(player);

		if (hasAbility(player, EarthGrab.class)) {
			getAbility(player, EarthGrab.class).remove();
			return;
		}

		if (bPlayer != null && this.bPlayer.isOnCooldown(this)) { //bPlayer can be null if the ability is disabled. If it is, it just won't start()
			return;
		}

		if (!this.isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}

		this.mode = mode;
		this.setFields();
		this.start();
	}

	private void setFields() {
		this.range = getConfig().getDouble("Abilities.Earth.EarthGrab.Range");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthGrab.Cooldown");
		this.dragSpeed = getConfig().getDouble("Abilities.Earth.EarthGrab.DragSpeed");
		this.interval = getConfig().getLong("Abilities.Earth.EarthGrab.TrapHitInterval");
		this.trapHP = getConfig().getDouble("Abilities.Earth.EarthGrab.TrapHP");
		this.damageThreshold = getConfig().getDouble("Abilities.Earth.EarthGrab.DamageThreshold");
		this.origin = this.player.getLocation().clone();
		this.direction = this.player.getLocation().getDirection().setY(0).normalize();
		this.lastHit = 0;
	}

	@Override
	public void progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.remove();
			return;
		}

		if (this.target != null) {
			if (this.target instanceof Player) {
				final Player pt = (Player) this.target;
				if (!pt.isOnline()) {
					this.remove();
					return;
				}
			}

			if (this.target.isDead()) {
				this.remove();
				return;
			}
		}

		switch (this.mode) {
			case PROJECTING:
				this.project();
				break;
			case TRAP:
				this.trap();
				break;
			case DRAG:
				this.drag();
				break;
		}
	}

	public void project() {
		this.origin = this.origin.add(this.direction);
		Block top = GeneralMethods.getTopBlock(this.origin, 2);
		if (this.origin.distance(this.player.getLocation()) > this.range) {
			this.remove();
			return;
		}

		if (!this.isTransparent(top.getRelative(BlockFace.UP))) {
			this.remove();
			return;
		}

		if (top.getType() == Material.FIRE) {
			top.setType(Material.AIR);
		}

		if (!this.isEarthbendable(top)) {
			Block under = top.getRelative(BlockFace.DOWN);

			if (this.isTransparent(top) && this.isEarthbendable(under)) {
				top = under;
			} else {
				this.remove();
				return;
			}
		}

		if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.origin)) {
			this.remove();
			return;
		}

		this.origin.setY(top.getY() + 1);

		ParticleEffect.BLOCK_DUST.display(this.origin, 27, 0.2, 0.5, 0.2, this.origin.getBlock().getRelative(BlockFace.DOWN).getBlockData());
		playEarthbendingSound(this.origin);
		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.origin, 1)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId() && this.isEarthbendable(entity.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
				if (entity instanceof Player && BendingPlayer.getBendingPlayer((Player) entity) != null) {
					if (CoreAbility.hasAbility((Player) entity, AvatarState.class)) {
						continue;
					}
				}
				this.target = (LivingEntity) entity;
				this.trappedHP = this.target.getHealth();
				this.mode = GrabMode.TRAP;
				this.origin = this.target.getLocation().clone();
			}
		}
	}

	public void trap() {
		if (!this.initiated) {
			final Material m = this.target.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
			final TempArmorStand tas = new TempArmorStand(this.target.getLocation());
			this.trap = tas.getArmorStand();
			this.trap.setVisible(false);
			this.trap.setInvulnerable(false);
			this.trap.setSmall(true);
			this.trap.setHelmet(new ItemStack(m));
			this.trap.setHealth(this.trapHP);
			this.trap.setMetadata("earthgrab:trap", new FixedMetadataValue(ProjectKorra.plugin, this));

			new TempBlock(this.target.getLocation().clone().subtract(0, 1, 0).getBlock(), this.target.getLocation().clone().subtract(0, 1, 0).getBlock().getType());

			this.mHandler = new MovementHandler(this.target, this);
			this.mHandler.stop(Element.EARTH.getColor() + "* Trapped *");

			if (this.target instanceof Player || this.target instanceof Zombie || this.target instanceof Skeleton) {
				final ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
				final LeatherArmorMeta legmeta = (LeatherArmorMeta) legs.getItemMeta();
				legmeta.setColor(Color.fromRGB(EarthArmor.getColor(m)));
				legs.setItemMeta(legmeta);

				final ItemStack feet = new ItemStack(Material.LEATHER_BOOTS);
				final LeatherArmorMeta footmeta = (LeatherArmorMeta) feet.getItemMeta();
				footmeta.setColor(Color.fromRGB(EarthArmor.getColor(m)));
				feet.setItemMeta(footmeta);

				final ItemStack[] pieces = { (this.target.getEquipment().getArmorContents()[0] == null || this.target.getEquipment().getArmorContents()[0].getType() == Material.AIR) ? feet : null, (this.target.getEquipment().getArmorContents()[1] == null || this.target.getEquipment().getArmorContents()[1].getType() == Material.AIR) ? legs : null, null, null };
				this.armor = new TempArmor(this.target, 36000000L, this, pieces);
			}

			playEarthbendingSound(this.target.getLocation());
			this.initiated = true;
		}

		ParticleEffect.BLOCK_DUST.display(this.target.getLocation(), 36, 0.3, 0.6, 0.3, this.target.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData());

		if (!ElementalAbility.isAir(this.trap.getLocation().clone().subtract(0, 0.1, 0).getBlock().getType())) {
			this.trap.setGravity(false);
		} else {
			this.trap.setGravity(true);
		}

		if (!this.isEarthbendable(this.target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.remove();
			return;
		}

		if (this.trap.getLocation().distance(this.target.getLocation()) > 2) {
			this.remove();
			return;
		}

		if (this.trappedHP - this.target.getHealth() >= this.damageThreshold) {
			this.remove();
			return;
		}

		if (this.trapHP <= 0) {
			this.remove();
			return;
		}

		if (this.trap.isDead()) {
			this.remove();
			return;
		}

		if (this.player.getLocation().distance(this.target.getLocation()) > this.range) {
			this.remove();
			return;
		}

		if (!GeneralMethods.isSolid(this.target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.remove();
			return;
		}

		if (GeneralMethods.isSolid(this.target.getLocation().getBlock())) {
			this.remove();
			return;
		}
	}

	public void drag() {
		if (!this.player.isOnGround()) {
			return;
		}

		if (!this.player.isSneaking()) {
			this.remove();
			return;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.player.getLocation())) {
			this.remove();
			return;
		}

		for (final Location l : GeneralMethods.getCircle(this.player.getLocation(), (int) Math.floor(this.range), 2, false, false, 0)) {
			if (!Arrays.asList(this.crops).contains(l.getBlock().getType())) {
				continue;
			}

			final Block b = l.getBlock();
			if ((b.getBlockData() instanceof Ageable && ((Ageable) b.getBlockData()).getAge() == ((Ageable) b.getBlockData()).getMaximumAge()) || b.getType() == Material.MELON || b.getType() == Material.PUMPKIN) {
				b.breakNaturally();
			}
		}

		final List<Entity> ents = GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.range);
		if (ents.isEmpty()) {
			this.remove();
			return;
		}

		for (Entity entity : ents) {
			if (!isEarth(entity.getLocation().clone().subtract(0, 1, 0).getBlock()) && (this.bPlayer.canSandbend() && !isSand(entity.getLocation().clone().subtract(0, 1, 0).getBlock())) && entity.getLocation().clone().subtract(0, 1, 0).getBlock().getType() != Material.FARMLAND) {
				continue;
			}
			if (entity instanceof Trident) {
				continue;
			} else if (entity instanceof Arrow) {
				final Arrow arrow = (Arrow) entity;
				if (arrow.getPickupStatus() == Arrow.PickupStatus.ALLOWED) {
					final Location l = entity.getLocation();
					entity.remove();
					entity = l.getWorld().dropItem(l, new ItemStack(Material.ARROW, 1));
				}
			} else if (!(entity instanceof Item)) {
				continue;
			}
			final Block b = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			GeneralMethods.setVelocity(this, entity, GeneralMethods.getDirection(entity.getLocation(), this.player.getLocation()).normalize().multiply(this.dragSpeed));
			ParticleEffect.BLOCK_CRACK.display(entity.getLocation(), 2, 0, 0, 0, b.getBlockData());
			playEarthbendingSound(entity.getLocation());
		}
	}

	public void damageTrap() {
		if (System.currentTimeMillis() >= this.lastHit + this.interval) {
			this.trapHP -= 1;
			this.lastHit = System.currentTimeMillis();
			ParticleEffect.BLOCK_CRACK.display(this.target.getLocation().clone().add(0, 1, 0), 7, 0.06, 0.3, 0.06, this.target.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData());
			playEarthbendingSound(this.target.getLocation());
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.mode == GrabMode.TRAP && this.initiated) {
			this.mHandler.reset();
			this.trap.remove();
			if (TempArmor.getTempArmorList(this.target).contains(this.armor)) {
				this.armor.revert();
			}
		}
		this.bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "EarthGrab";
	}

	@Override
	public Location getLocation() {
		return this.target == null ? null : this.target.getLocation();
	}

	public GrabMode getMode() {
		return this.mode;
	}

	public double getRange() {
		return this.range;
	}

	public LivingEntity getTarget() {
		return this.target;
	}
}
