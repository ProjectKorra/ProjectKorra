package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.airbending.combo.AirStream;
import com.projectkorra.projectkorra.airbending.combo.AirSweep;
import com.projectkorra.projectkorra.airbending.flight.AirFlight;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.Ripple;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.earthbending.sand.SandSpout;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.FireBurst;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.FireManipulation;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.WallOfFire;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireKick;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireSpin;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireWheel;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterBubble;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.healing.HealingWaters;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

/**
 * CollisionInitializer is used to create the default Collisions for a given
 * CollisionManager.
 * 
 * @see Collision
 * @see CollisionManager
 */
public class CollisionInitializer {

	private CollisionManager collisionManager;
	private ArrayList<CoreAbility> smallAbilities;
	private ArrayList<CoreAbility> largeAbilities;
	private ArrayList<CoreAbility> comboAbilities;
	private ArrayList<CoreAbility> removeSpoutAbilities;

	public CollisionInitializer(CollisionManager collisionManager) {
		this.collisionManager = collisionManager;
		this.smallAbilities = new ArrayList<>();
		this.comboAbilities = new ArrayList<>();
		this.largeAbilities = new ArrayList<>();
		this.removeSpoutAbilities = new ArrayList<>();
	}

	@SuppressWarnings("unused")
	public void initializeDefaultCollisions() {
		CoreAbility airBlast = CoreAbility.getAbility(AirBlast.class);
		CoreAbility airBurst = CoreAbility.getAbility(AirBurst.class);
		CoreAbility airFlight = CoreAbility.getAbility(AirFlight.class);
		CoreAbility airScooter = CoreAbility.getAbility(AirScooter.class);
		CoreAbility airShield = CoreAbility.getAbility(AirShield.class);
		CoreAbility airSpout = CoreAbility.getAbility(AirSpout.class);
		CoreAbility airStream = CoreAbility.getAbility(AirStream.class);
		CoreAbility airSuction = CoreAbility.getAbility(AirSuction.class);
		CoreAbility airSweep = CoreAbility.getAbility(AirSweep.class);
		CoreAbility airSwipe = CoreAbility.getAbility(AirSwipe.class);
		CoreAbility suffocate = CoreAbility.getAbility(Suffocate.class);
		CoreAbility tornado = CoreAbility.getAbility(Tornado.class);

		CoreAbility catapult = CoreAbility.getAbility(Catapult.class);
		CoreAbility collapse = CoreAbility.getAbility(Collapse.class);
		CoreAbility earthArmor = CoreAbility.getAbility(EarthArmor.class);
		CoreAbility earthBlast = CoreAbility.getAbility(EarthBlast.class);
		CoreAbility earthSmash = CoreAbility.getAbility(EarthSmash.class);
		CoreAbility earthTunnel = CoreAbility.getAbility(EarthTunnel.class);
		CoreAbility lavaFlow = CoreAbility.getAbility(LavaFlow.class);
		CoreAbility raiseEarth = CoreAbility.getAbility(RaiseEarth.class);
		CoreAbility ripple = CoreAbility.getAbility(Ripple.class);
		CoreAbility sandSpout = CoreAbility.getAbility(SandSpout.class);

		CoreAbility blazeArc = CoreAbility.getAbility(BlazeArc.class);
		CoreAbility combustion = CoreAbility.getAbility(Combustion.class);
		CoreAbility fireBlast = CoreAbility.getAbility(FireBlast.class);
		CoreAbility fireBlastCharged = CoreAbility.getAbility(FireBlastCharged.class);
		CoreAbility fireBurst = CoreAbility.getAbility(FireBurst.class);
		CoreAbility fireJet = CoreAbility.getAbility(FireJet.class);
		CoreAbility fireKick = CoreAbility.getAbility(FireKick.class);
		CoreAbility fireSpin = CoreAbility.getAbility(FireSpin.class);
		CoreAbility fireWheel = CoreAbility.getAbility(FireWheel.class);
		CoreAbility fireShield = CoreAbility.getAbility(FireShield.class);
		CoreAbility fireManipulation = CoreAbility.getAbility(FireManipulation.class);
		CoreAbility lightning = CoreAbility.getAbility(Lightning.class);
		CoreAbility wallOfFire = CoreAbility.getAbility(WallOfFire.class);

		CoreAbility bloodbending = CoreAbility.getAbility(Bloodbending.class);
		CoreAbility healingWaters = CoreAbility.getAbility(HealingWaters.class);
		CoreAbility iceBlast = CoreAbility.getAbility(IceBlast.class);
		CoreAbility iceBullet = CoreAbility.getAbility(IceBullet.class);
		CoreAbility iceWave = CoreAbility.getAbility(IceWave.class);
		CoreAbility iceSpikeBlast = CoreAbility.getAbility(IceSpikeBlast.class);
		CoreAbility octopusForm = CoreAbility.getAbility(OctopusForm.class);
		CoreAbility surgeWall = CoreAbility.getAbility(SurgeWall.class);
		CoreAbility surgeWave = CoreAbility.getAbility(SurgeWave.class);
		CoreAbility torrent = CoreAbility.getAbility(Torrent.class);
		CoreAbility torrentWave = CoreAbility.getAbility(TorrentWave.class);
		CoreAbility waterBubble = CoreAbility.getAbility(WaterBubble.class);
		CoreAbility waterManipulation = CoreAbility.getAbility(WaterManipulation.class);
		CoreAbility waterSpout = CoreAbility.getAbility(WaterSpout.class);
		CoreAbility waterSpoutWave = CoreAbility.getAbility(WaterSpoutWave.class);

		CoreAbility[] smallAbils = { airBlast, airSwipe, earthBlast, waterManipulation, fireBlast, combustion, blazeArc };
		CoreAbility[] largeAbils = { earthSmash, airShield, fireBlastCharged, fireKick, fireSpin, fireWheel, airSweep, iceBullet };
		CoreAbility[] comboAbils = { fireKick, fireSpin, fireWheel, airSweep, iceBullet };
		CoreAbility[] removeSpoutAbils = { airSwipe, earthBlast, waterManipulation, fireBlast, fireBlastCharged, earthSmash, fireKick, fireSpin, fireWheel, airSweep, iceBullet };

		for (CoreAbility smallAbil : smallAbils) {
			addSmallAbility(smallAbil);
		}
		for (CoreAbility largeAbil : largeAbils) {
			addLargeAbility(largeAbil);
		}
		for (CoreAbility comboAbil : comboAbils) {
			addComboAbility(comboAbil);
		}
		for (CoreAbility removeSpoutAbil : removeSpoutAbils) {
			addRemoveSpoutAbility(removeSpoutAbil);
		}

		collisionManager.addCollision(new Collision(airShield, airBlast, false, true));
		collisionManager.addCollision(new Collision(airShield, airSuction, false, true));
		collisionManager.addCollision(new Collision(airShield, airStream, false, true));
		collisionManager.addCollision(new Collision(airShield, fireBlast, false, true));
		collisionManager.addCollision(new Collision(airShield, earthBlast, false, true));
		collisionManager.addCollision(new Collision(airShield, waterManipulation, false, true));
		for (CoreAbility comboAbil : comboAbils) {
			collisionManager.addCollision(new Collision(airShield, comboAbil, false, true));
		}

		collisionManager.addCollision(new Collision(fireShield, airBlast, false, true));
		collisionManager.addCollision(new Collision(fireShield, airSuction, false, true));
		collisionManager.addCollision(new Collision(fireShield, fireBlast, false, true));
		collisionManager.addCollision(new Collision(fireShield, fireBlastCharged, false, true));
		collisionManager.addCollision(new Collision(fireShield, waterManipulation, false, true));
		collisionManager.addCollision(new Collision(fireShield, earthBlast, false, true));
		collisionManager.addCollision(new Collision(fireShield, airSweep, false, true));
		
		collisionManager.addCollision(new Collision(fireManipulation, airBlast, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, airSuction, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, fireBlast, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, fireBlastCharged, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, waterManipulation, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, earthBlast, false, true));
		collisionManager.addCollision(new Collision(fireManipulation, airSweep, false, true));
	}

	/**
	 * An ability that collides with other small abilities. (EarthBlast,
	 * FireBlast). Two colliding small abilities will remove each other. A small
	 * ability is removed when it collides with a large ability.
	 * 
	 * @param smallAbility the small CoreAbility
	 */
	public void addSmallAbility(CoreAbility smallAbility) {
		if (smallAbility == null) {
			return;
		}
		smallAbilities.add(smallAbility);
		for (CoreAbility otherSmallAbility : smallAbilities) {
			collisionManager.addCollision(new Collision(smallAbility, otherSmallAbility, true, true));
		}

		for (CoreAbility largeAbility : largeAbilities) {
			collisionManager.addCollision(new Collision(largeAbility, smallAbility, false, true));
		}

	}

	/**
	 * A large ability that removes small abilities (EarthSmash, Charged
	 * FireBlast). The large ability remains while the small ability is
	 * destroyed.
	 * 
	 * @param largeAbility the large CoreAbility
	 */
	public void addLargeAbility(CoreAbility largeAbility) {
		if (largeAbility == null) {
			return;
		}
		largeAbilities.add(largeAbility);
		for (CoreAbility smallAbility : smallAbilities) {
			collisionManager.addCollision(new Collision(largeAbility, smallAbility, false, true));
		}
	}

	/**
	 * A combo ability that collides with all other combo abilities. Both
	 * colliding combo abilities are destroyed.
	 * 
	 * @param comboAbility the combo CoreAbility
	 */
	public void addComboAbility(CoreAbility comboAbility) {
		if (comboAbility == null) {
			return;
		}
		comboAbilities.add(comboAbility);
		for (CoreAbility otherComboAbility : smallAbilities) {
			collisionManager.addCollision(new Collision(comboAbility, otherComboAbility, true, true));
		}
	}

	/**
	 * An ability that destroys WaterSpout, AirSpout, and SandSpout upon
	 * contact. The spout is destroyed and this ability remains.
	 * 
	 * @param ability the ability that removes spouts
	 */
	public void addRemoveSpoutAbility(CoreAbility ability) {
		if (ability == null) {
			return;
		}
		removeSpoutAbilities.add(ability);
		collisionManager.addCollision(new Collision(ability, CoreAbility.getAbility(AirSpout.class), false, true));
		collisionManager.addCollision(new Collision(ability, CoreAbility.getAbility(WaterSpout.class), false, true));
		collisionManager.addCollision(new Collision(ability, CoreAbility.getAbility(SandSpout.class), false, true));
	}

	public CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public ArrayList<CoreAbility> getSmallAbilities() {
		return smallAbilities;
	}

	public void setSmallAbilities(ArrayList<CoreAbility> smallAbilities) {
		this.smallAbilities = smallAbilities;
	}

	public ArrayList<CoreAbility> getLargeAbilities() {
		return largeAbilities;
	}

	public void setLargeAbilities(ArrayList<CoreAbility> largeAbilities) {
		this.largeAbilities = largeAbilities;
	}

	public ArrayList<CoreAbility> getComboAbilities() {
		return comboAbilities;
	}

	public void setComboAbilities(ArrayList<CoreAbility> comboAbilities) {
		this.comboAbilities = comboAbilities;
	}

	public ArrayList<CoreAbility> getRemoveSpoutAbilities() {
		return removeSpoutAbilities;
	}

	public void setRemoveSpoutAbilities(ArrayList<CoreAbility> removeSpoutAbilities) {
		this.removeSpoutAbilities = removeSpoutAbilities;
	}

}
