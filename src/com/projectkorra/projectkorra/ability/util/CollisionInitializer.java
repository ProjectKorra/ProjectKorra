package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBubble;
import com.projectkorra.projectkorra.airbending.AirCombo;
import com.projectkorra.projectkorra.airbending.AirCombo.AirStream;
import com.projectkorra.projectkorra.airbending.AirCombo.AirSweep;
import com.projectkorra.projectkorra.airbending.AirFlight;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.LavaFlow;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.Ripple;
import com.projectkorra.projectkorra.earthbending.SandSpout;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.FireCombo;
import com.projectkorra.projectkorra.firebending.FireCombo.FireKick;
import com.projectkorra.projectkorra.firebending.FireCombo.FireSpin;
import com.projectkorra.projectkorra.firebending.FireCombo.FireWheel;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.Lightning;
import com.projectkorra.projectkorra.firebending.WallOfFire;
import com.projectkorra.projectkorra.waterbending.Bloodbending;
import com.projectkorra.projectkorra.waterbending.HealingWaters;
import com.projectkorra.projectkorra.waterbending.IceBlast;
import com.projectkorra.projectkorra.waterbending.IceSpikeBlast;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.PlantArmor;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterBubble;
import com.projectkorra.projectkorra.waterbending.WaterCombo;
import com.projectkorra.projectkorra.waterbending.WaterCombo.IceBullet;
import com.projectkorra.projectkorra.waterbending.WaterCombo.IceWave;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;

/**
 * CollisionInitializer is used to create the default Collisions for a given
 * CollisionManager.
 * 
 * @see Collision
 * @see CollisionManager
 */
public class CollisionInitializer {

	private CollisionManager cm;

	public CollisionInitializer(CollisionManager cm) {
		this.cm = cm;
	}

	@SuppressWarnings("unused")
	public void initializeCollisions() {
		CoreAbility airBlast = CoreAbility.getAbility(AirBlast.class);
		CoreAbility airBubble = CoreAbility.getAbility(AirBubble.class);
		CoreAbility airCombo = CoreAbility.getAbility(AirCombo.class);
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
		CoreAbility fireCombo = CoreAbility.getAbility(FireCombo.class);
		CoreAbility fireJet = CoreAbility.getAbility(FireJet.class);
		CoreAbility fireKick = CoreAbility.getAbility(FireKick.class);
		CoreAbility fireSpin = CoreAbility.getAbility(FireSpin.class);
		CoreAbility fireWheel = CoreAbility.getAbility(FireWheel.class);
		CoreAbility fireShield = CoreAbility.getAbility(FireShield.class);
		CoreAbility lightning = CoreAbility.getAbility(Lightning.class);
		CoreAbility wallOfFire = CoreAbility.getAbility(WallOfFire.class);

		CoreAbility bloodbending = CoreAbility.getAbility(Bloodbending.class);
		CoreAbility healingWaters = CoreAbility.getAbility(HealingWaters.class);
		CoreAbility iceBlast = CoreAbility.getAbility(IceBlast.class);
		CoreAbility iceBullet = CoreAbility.getAbility(IceBullet.class);
		CoreAbility iceWave = CoreAbility.getAbility(IceWave.class);
		CoreAbility iceSpikeBlast = CoreAbility.getAbility(IceSpikeBlast.class);
		CoreAbility octopusForm = CoreAbility.getAbility(OctopusForm.class);
		CoreAbility plantArmor = CoreAbility.getAbility(PlantArmor.class);
		CoreAbility surgeWall = CoreAbility.getAbility(SurgeWall.class);
		CoreAbility surgeWave = CoreAbility.getAbility(SurgeWave.class);
		CoreAbility torrent = CoreAbility.getAbility(Torrent.class);
		CoreAbility torrentWave = CoreAbility.getAbility(TorrentWave.class);
		CoreAbility waterBubble = CoreAbility.getAbility(WaterBubble.class);
		CoreAbility waterCombo = CoreAbility.getAbility(WaterCombo.class);
		CoreAbility waterManipulation = CoreAbility.getAbility(WaterManipulation.class);
		CoreAbility waterSpout = CoreAbility.getAbility(WaterSpout.class);
		CoreAbility waterSpoutWave = CoreAbility.getAbility(WaterSpoutWave.class);

		CoreAbility[] smallDamageAbils = { airSwipe, earthBlast, waterManipulation, fireBlast, combustion, blazeArc };
		CoreAbility[] abilitiesThatRemoveSmall = { earthSmash, airShield, airCombo, fireCombo, waterCombo, fireBlastCharged };
		CoreAbility[] abilsThatRemoveSpouts = { airSwipe, earthBlast, waterManipulation, fireBlast, fireBlastCharged, earthSmash, fireCombo, airCombo, waterCombo };
		CoreAbility[] damageComboAbils = { fireKick, fireSpin, fireWheel, airSweep, iceBullet };

		// All small damaging abilities block each other
		for (int i = 0; i < smallDamageAbils.length; i++) {
			for (int j = i; j < smallDamageAbils.length; j++) {
				cm.add(new Collision(smallDamageAbils[i], smallDamageAbils[j], true, true));
			}
		}

		// All combos block each other
		for (int i = 0; i < damageComboAbils.length; i++) {
			for (int j = i; j < damageComboAbils.length; j++) {
				cm.add(new Collision(damageComboAbils[i], damageComboAbils[j], true, true));
			}
		}

		// These abilities remove all small damaging abilities
		for (CoreAbility abilThatRemoves : abilitiesThatRemoveSmall) {
			for (CoreAbility smallDamageAbil : smallDamageAbils) {
				cm.add(new Collision(abilThatRemoves, smallDamageAbil, false, true));
			}
		}

		for (CoreAbility spoutDestroyAbil : abilsThatRemoveSpouts) {
			cm.add(new Collision(spoutDestroyAbil, airSpout, false, true));
			cm.add(new Collision(spoutDestroyAbil, waterSpout, false, true));
			cm.add(new Collision(spoutDestroyAbil, sandSpout, false, true));
		}

		cm.add(new Collision(airShield, airBlast, false, true));
		cm.add(new Collision(airShield, airSuction, false, true));
		cm.add(new Collision(airShield, airStream, false, true));
		for (CoreAbility comboAbil : damageComboAbils) {
			cm.add(new Collision(airShield, comboAbil, false, true));
		}

		cm.add(new Collision(fireShield, fireBlast, false, true));
		cm.add(new Collision(fireShield, fireBlastCharged, false, true));
		cm.add(new Collision(fireShield, waterManipulation, false, true));
		cm.add(new Collision(fireShield, earthBlast, false, true));
		cm.add(new Collision(fireShield, airSweep, false, true));
	}

}
