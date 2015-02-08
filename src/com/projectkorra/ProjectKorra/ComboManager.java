package com.projectkorra.ProjectKorra;

import com.projectkorra.ProjectKorra.Ability.Combo.ComboAbilityModule;
import com.projectkorra.ProjectKorra.airbending.AirCombo;
import com.projectkorra.ProjectKorra.firebending.FireCombo;
import com.projectkorra.ProjectKorra.waterbending.WaterCombo;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ComboManager 
{
	public static enum ClickType{
		SHIFTDOWN, SHIFTUP, LEFTCLICK, RIGHTCLICK
	}
	private static final long CLEANUP_DELAY = 10000;
	public static ConcurrentHashMap<String, ArrayList<AbilityInformation>> recentlyUsedAbilities = new ConcurrentHashMap<String, ArrayList<AbilityInformation>>();
	public static ArrayList<ComboAbility> comboAbilityList = new ArrayList<ComboAbility>();
	public static HashMap<String, String> descriptions = new HashMap<String, String>();
	public static HashMap<String, String> instructions = new HashMap<String, String>();
	public static HashMap<String, String> authors = new HashMap<String, String>();
	
	public ComboManager()
	{
		ArrayList<AbilityInformation> fireKick = new ArrayList<AbilityInformation>();
		fireKick.add(new AbilityInformation("FireBlast",ClickType.LEFTCLICK));
		fireKick.add(new AbilityInformation("FireBlast",ClickType.LEFTCLICK));
		fireKick.add(new AbilityInformation("FireBlast",ClickType.SHIFTDOWN));
		fireKick.add(new AbilityInformation("FireBlast",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("FireKick",fireKick,FireCombo.class));
		
		ArrayList<AbilityInformation> fireSpin = new ArrayList<AbilityInformation>();
		fireSpin.add(new AbilityInformation("FireBlast",ClickType.LEFTCLICK));
		fireSpin.add(new AbilityInformation("FireBlast",ClickType.LEFTCLICK));
		fireSpin.add(new AbilityInformation("FireShield",ClickType.LEFTCLICK));
		fireSpin.add(new AbilityInformation("FireShield",ClickType.SHIFTDOWN));
		fireSpin.add(new AbilityInformation("FireShield",ClickType.SHIFTUP));
		comboAbilityList.add(new ComboAbility("FireSpin",fireSpin,FireCombo.class));
		
		ArrayList<AbilityInformation> jetBlast = new ArrayList<AbilityInformation>();
		jetBlast.add(new AbilityInformation("FireJet",ClickType.SHIFTDOWN));
		jetBlast.add(new AbilityInformation("FireJet",ClickType.SHIFTUP));
		jetBlast.add(new AbilityInformation("FireJet",ClickType.SHIFTDOWN));
		jetBlast.add(new AbilityInformation("FireJet",ClickType.SHIFTUP));
		jetBlast.add(new AbilityInformation("FireShield",ClickType.SHIFTDOWN));
		jetBlast.add(new AbilityInformation("FireShield",ClickType.SHIFTUP));
		jetBlast.add(new AbilityInformation("FireJet",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("JetBlast",jetBlast,FireCombo.class));
		
		ArrayList<AbilityInformation> jetBlaze = new ArrayList<AbilityInformation>();
		jetBlaze.add(new AbilityInformation("FireJet",ClickType.SHIFTDOWN));
		jetBlaze.add(new AbilityInformation("FireJet",ClickType.SHIFTUP));
		jetBlaze.add(new AbilityInformation("FireJet",ClickType.SHIFTDOWN));
		jetBlaze.add(new AbilityInformation("FireJet",ClickType.SHIFTUP));
		jetBlaze.add(new AbilityInformation("Blaze",ClickType.SHIFTDOWN));
		jetBlaze.add(new AbilityInformation("Blaze",ClickType.SHIFTUP));
		jetBlaze.add(new AbilityInformation("FireJet",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("JetBlaze",jetBlaze,FireCombo.class));
		
		ArrayList<AbilityInformation> fireWheel = new ArrayList<AbilityInformation>();
		fireWheel.add(new AbilityInformation("FireShield",ClickType.SHIFTDOWN));
		fireWheel.add(new AbilityInformation("FireShield",ClickType.RIGHTCLICK));
		fireWheel.add(new AbilityInformation("FireShield",ClickType.RIGHTCLICK));
		fireWheel.add(new AbilityInformation("Blaze",ClickType.SHIFTUP));
		comboAbilityList.add(new ComboAbility("FireWheel",fireWheel,FireCombo.class));	
		
		ArrayList<AbilityInformation> twister = new ArrayList<AbilityInformation>();
		twister.add(new AbilityInformation("AirShield",ClickType.SHIFTDOWN));
		twister.add(new AbilityInformation("AirShield",ClickType.SHIFTUP));
		twister.add(new AbilityInformation("Tornado",ClickType.SHIFTDOWN));
		twister.add(new AbilityInformation("AirBlast",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("Twister",twister,AirCombo.class));	
		
		ArrayList<AbilityInformation> airStream = new ArrayList<AbilityInformation>();
		airStream.add(new AbilityInformation("AirShield",ClickType.SHIFTDOWN));
		airStream.add(new AbilityInformation("AirSuction",ClickType.LEFTCLICK));
		airStream.add(new AbilityInformation("AirBlast",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("AirStream",airStream,AirCombo.class));	
		
		/*ArrayList<AbilityInformation> airSlice = new ArrayList<AbilityInformation>();
		airSlice.add(new AbilityInformation("AirBlast",ClickType.LEFTCLICK));
		airSlice.add(new AbilityInformation("AirScooter",ClickType.SHIFTDOWN));
		airSlice.add(new AbilityInformation("AirScooter",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("AirSlice",airSlice,AirCombo.class));*/
		
		ArrayList<AbilityInformation> airSweep = new ArrayList<AbilityInformation>();
		airSweep.add(new AbilityInformation("AirSwipe",ClickType.LEFTCLICK));
		airSweep.add(new AbilityInformation("AirSwipe",ClickType.LEFTCLICK));
		airSweep.add(new AbilityInformation("AirBurst",ClickType.SHIFTDOWN));
		airSweep.add(new AbilityInformation("AirBurst",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("AirSweep",airSweep,AirCombo.class));
		
		ArrayList<AbilityInformation> iceWave = new ArrayList<AbilityInformation>();
		iceWave.add(new AbilityInformation("WaterSpout",ClickType.SHIFTUP));
		iceWave.add(new AbilityInformation("PhaseChange",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("IceWave",iceWave,WaterCombo.class));
		
		ArrayList<AbilityInformation> icePillar = new ArrayList<AbilityInformation>();
		icePillar.add(new AbilityInformation("IceSpike",ClickType.LEFTCLICK));
		icePillar.add(new AbilityInformation("IceSpike",ClickType.LEFTCLICK));
		icePillar.add(new AbilityInformation("WaterSpout",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("IcePillar",icePillar,WaterCombo.class));

		ArrayList<AbilityInformation> iceBullet = new ArrayList<AbilityInformation>();
		iceBullet.add(new AbilityInformation("WaterBubble",ClickType.SHIFTDOWN));
		iceBullet.add(new AbilityInformation("WaterBubble",ClickType.SHIFTUP));
		iceBullet.add(new AbilityInformation("IceBlast",ClickType.SHIFTDOWN));
		iceBullet.add(new AbilityInformation("IceBlast",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("IceBullet",iceBullet,WaterCombo.class));
		
		ArrayList<AbilityInformation> iceBulletLeft = new ArrayList<AbilityInformation>();
		iceBulletLeft.add(new AbilityInformation("IceBlast",ClickType.LEFTCLICK));
		comboAbilityList.add(new ComboAbility("IceBulletLeftClick",iceBulletLeft,WaterCombo.class));
		ArrayList<AbilityInformation> iceBulletRight = new ArrayList<AbilityInformation>();
		iceBulletRight.add(new AbilityInformation("IceBlast",ClickType.RIGHTCLICK));
		comboAbilityList.add(new ComboAbility("IceBulletRightClick",iceBulletRight,WaterCombo.class));
		
		startCleanupTask();
	}
	public static void addComboAbility(Player player, ClickType type)
	{
		String abilityName = Methods.getBoundAbility(player);
		if (abilityName == null)
			return;

		AbilityInformation info = new AbilityInformation(abilityName, type, System.currentTimeMillis());
		addRecentAbility(player, info);

		ComboAbility comboAbil = checkForValidCombo(player);
		if (comboAbil == null)
			return;

		if (comboAbil.getComboType().equals(FireCombo.class))
			new FireCombo(player, comboAbil.getName());
		else if (comboAbil.getComboType().equals(AirCombo.class))
			new AirCombo(player, comboAbil.getName());
		else if (comboAbil.getComboType().equals(WaterCombo.class))
			new WaterCombo(player, comboAbil.getName());
		else
		{
			for (ComboAbility ca : comboAbilityList)
			{
				if (comboAbil.getName().equals(ca.getName()))
				{
					if (ca.getComboType() instanceof ComboAbilityModule)
					{
						((ComboAbilityModule) ca.getComboType()).createNewComboInstance(player);
						return;
					}
				}
			}
		}
	}

	public static class AbilityInformation
	{
		private String abilityName;
		private ClickType clickType;
		private long time;
		public AbilityInformation(String name, ClickType type, long time){
			this.abilityName = name;
			this.clickType = type;
			this.time = time;
		}
		public AbilityInformation(String name, ClickType type){
			this(name,type,0);
		}
		public String getAbilityName() {
			return abilityName;
		}
		public void setAbilityName(String abilityName) {
			this.abilityName = abilityName;
		}
		public ClickType getClickType() {
			return clickType;
		}
		public void setClickType(ClickType clickType) {
			this.clickType = clickType;
		}
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		public String toString()
		{
			return abilityName + " " + clickType + " " + time;
		}
		public boolean equalsWithoutTime(AbilityInformation info)
		{
			return this.getAbilityName().equals(info.getAbilityName())
					&& this.getClickType().equals(info.getClickType());
		}
	}
	public static class ComboAbility
	{
		private String name;
		private ArrayList<AbilityInformation> abilities;
		private Object comboType;
		public ComboAbility(String name, ArrayList<AbilityInformation> abilities, Object comboType){
			this.name = name;
			this.abilities = abilities;
			this.comboType = comboType;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public ArrayList<AbilityInformation> getAbilities() {
			return abilities;
		}
		public void setAbilities(ArrayList<AbilityInformation> abilities) {
			this.abilities = abilities;
		}
		
		public Object getComboType() {
			return comboType;
		}
		public void setComboType(Object comboType) {
			this.comboType = comboType;
		}
		public String toString(){
			return name;
		}
	}
	
	public static void addRecentAbility(Player player, AbilityInformation info)
	{
		ArrayList<AbilityInformation> list;
		String name = player.getName();
		if(recentlyUsedAbilities.containsKey(name)){
			list = recentlyUsedAbilities.get(name);
			recentlyUsedAbilities.remove(player);
		}
		else
			list = new ArrayList<AbilityInformation>();
		list.add(info);
		recentlyUsedAbilities.put(name, list);
	}
	public static ArrayList<AbilityInformation> getRecentlyUsedAbilities(Player player, int amount)
	{
		String name = player.getName();
		if(!recentlyUsedAbilities.containsKey(name))
			return new ArrayList<AbilityInformation>();
		
		ArrayList<AbilityInformation> list = recentlyUsedAbilities.get(name);
		if(list.size() < amount) 
			return new ArrayList<AbilityInformation>(list);
		
		ArrayList<AbilityInformation> tempList = new ArrayList<AbilityInformation>();
		for(int i = 0; i < amount; i++)
			tempList.add(0,list.get(list.size() - 1 - i));
		return tempList;		
	}
	public static ComboAbility checkForValidCombo(Player player)
	{
		ArrayList<AbilityInformation> playerCombo = getRecentlyUsedAbilities(player,8);
		for(ComboAbility customAbility : comboAbilityList)
		{
			ArrayList<AbilityInformation> abilityCombo = customAbility.getAbilities();
			int size = abilityCombo.size();
			
			if(playerCombo.size() < size)
				continue;
			
			boolean isValid = true;
			for(int i = 1; i <= size; i++)
			{
				if(!playerCombo.get(playerCombo.size() - i)
						.equalsWithoutTime(abilityCombo.get(abilityCombo.size() - i)))
				{
					isValid = false;
					break;
				}
			}
			if(isValid)
				return customAbility;
		}
		return null;
	}
	public static void startCleanupTask(){
		new BukkitRunnable(){
			public void run(){
				cleanupOldCombos();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, CLEANUP_DELAY);
	}
	public static void cleanupOldCombos()
	{
		Enumeration<String> keys = recentlyUsedAbilities.keys();
		while(keys.hasMoreElements())
		{
			String name = keys.nextElement();
			ArrayList<AbilityInformation> combos = recentlyUsedAbilities.get(name);
			recentlyUsedAbilities.remove(name);
			for(int i = 0; i < combos.size(); i++)
			{
				AbilityInformation info = combos.get(i);
				if(System.currentTimeMillis() - info.getTime() > CLEANUP_DELAY){
					combos.remove(i);
					i--;
				}
			}
			
			if(combos.size() > 0)
				recentlyUsedAbilities.put(name, combos);
		}
	}
}

