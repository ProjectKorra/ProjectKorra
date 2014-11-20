package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class MetalClips 
{
	public static ConcurrentHashMap<Player, MetalClips> instances = new ConcurrentHashMap<Player, MetalClips>();
	public static int armorTime = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Duration");
	public static int crushInterval = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.DamageInterval");;
	public static int cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Cooldown");
	public static int crushDamage = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Damage");
	
	private Player player;
	private LivingEntity target;
	private boolean isBeingWorn = false;
	private boolean isControlling = false;
	private boolean canThrow = false;
	public int metalclips = 0;
	private long startTime;
	private long time;
	
	private ItemStack[] oldarmor;
	private List<Item> trackedIngots = new ArrayList<Item>();
	
	public MetalClips(Player player)
	{
		if(instances.containsKey(player))
			return;
		
		this.player = player;
		
		if(!isEligible())
			return;
		
		shootMetal();
		
		instances.put(player, this);
	}

	public boolean isEligible()
	{
		final BendingPlayer bplayer = Methods.getBendingPlayer(player.getName());
		
		if(!Methods.canBend(player.getName(), "MetalClips"))
			return false;
		
		if(Methods.getBoundAbility(player) == null)
			return false;
		
		if(!Methods.getBoundAbility(player).equalsIgnoreCase("MetalClips"))
			return false;
		
		if(Methods.isRegionProtectedFromBuild(player, "MetalClips", player.getLocation()))
			return false;
		
		if(!Methods.canMetalbend(player))
			return false;
		
		if(bplayer.isOnCooldown("MetalClips"))
			return false;
		
		return true;
	}
	
	public void shootMetal()
	{	
		ItemStack is = new ItemStack(Material.IRON_INGOT, 1);

		if(!player.getInventory().containsAtLeast(is, 1))
		{
			//ProjectKorra.log.info("Player doesn't have enough ingots!");
			remove();
			return;
		}
		Item ii = player.getWorld().dropItemNaturally(player.getLocation(), is);
		ii.setVelocity(player.getEyeLocation().getDirection().normalize().add(new Vector(0, .5, 0)));
		trackedIngots.add(ii);
		player.getInventory().removeItem(is);

		Methods.getBendingPlayer(player.getName()).addCooldown("MetalManipulation", cooldown);
	}
	
	public void formArmor()
	{
		if(metalclips >= 4)
			return;
		
		metalclips = (metalclips < 4) ? metalclips + 1 : 4;
		
		if(target instanceof Player)
		{
			Player target = (Player) this.target;
			if(oldarmor == null)
				oldarmor = target.getInventory().getArmorContents();
			
			ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (metalclips >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldarmor[2];
			metalarmor[0] = (metalclips >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldarmor[0];
			metalarmor[1] = (metalclips >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldarmor[1];
			metalarmor[3] = (metalclips >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldarmor[3];
			
			target.getInventory().setArmorContents(metalarmor);
		}
		
		else
		{
			if(oldarmor == null)
				oldarmor = target.getEquipment().getArmorContents();
			
			ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (metalclips >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldarmor[2];
			metalarmor[0] = (metalclips >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldarmor[0];
			metalarmor[1] = (metalclips >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldarmor[1];
			metalarmor[3] = (metalclips >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldarmor[3];
			
			target.getEquipment().setArmorContents(metalarmor);			
		}

		if(metalclips == 4) time = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		isBeingWorn = true;
	}
	
	public void resetArmor()
	{
		if(target == null || oldarmor == null)
			return;
		
		if(target instanceof Player)
			((Player) target).getInventory().setArmorContents(oldarmor);
		else
			target.getEquipment().setArmorContents(oldarmor);
		
		player.getWorld().dropItem(target.getLocation(), new ItemStack(Material.IRON_INGOT, metalclips));
		
		isBeingWorn = false;
	}
	
	public void control()
	{
		isControlling = true;
	}
	
	public boolean controlling()
	{
		return isControlling;
	}
	
	public void launch() 
	{
		if(!canThrow)
			return;
		
		Location location = player.getLocation();
		double dx, dy, dz;
		Location target = this.target.getLocation().clone();
		dx = target.getX() - location.getX();
		dy = target.getY() - location.getY();
		dz = target.getZ() - location.getZ();
		Vector vector = new Vector(dx, dy, dz);
		vector.normalize();
		this.target.setVelocity(vector.multiply(2));
		remove();
	}
	
	public void progress()
	{
		if(!player.isOnline() || player.isDead())
		{
			remove();
			return;
		}
		
		if(target != null)
		{
			if((target instanceof Player && !((Player) target).isOnline()) || target.isDead())
			{
				remove();
				return;
			}
		}
		
		if(!player.isSneaking())
		{
			isControlling = false;
		}
		
		
		if(isBeingWorn && System.currentTimeMillis() > startTime + armorTime)
		{
			remove();
			return;
		}
		
		if(isControlling && player.isSneaking())
		{
			if(metalclips >= 1)
			{
				Location oldLocation = target.getLocation();
				Location loc = Methods.getTargetedLocation(player, 
						(int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);
				
				Vector v = Methods.getDirection(target.getLocation(), player.getLocation());
				
				if(distance > .5)
					target.setVelocity(v.normalize().multiply(0.1));
				
				Methods.breakBreathbendingHold(target);
			}
			
			if(metalclips >= 2)
			{
				Location oldLocation = target.getLocation();
				Location loc = Methods.getTargetedLocation(player, 
						(int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);
				
				Vector v = Methods.getDirection(target.getLocation(), player.getLocation());
				
				if(distance > .5)
					target.setVelocity(v.normalize().multiply(0.2));
				
				Methods.breakBreathbendingHold(target);
			}
			
			if(metalclips >= 3)
			{
				Location oldLocation = target.getLocation();
				Location loc = Methods.getTargetedLocation(player, 
						(int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);
				double dx = loc.getX() - oldLocation.getX();
				double dy = loc.getY() - oldLocation.getY();
				double dz = loc.getZ() - oldLocation.getZ();
				
				Vector v = new Vector(dx, dy, dz);
				if(distance > .5)
					target.setVelocity(v.normalize().multiply(.5));
				else
					target.setVelocity(new Vector(0, 0, 0));
				
				target.setFallDistance(0);
				Methods.breakBreathbendingHold(target);
			}
			
			if(metalclips == 4)
			{
				if(System.currentTimeMillis() > time + crushInterval)
				{
					time = System.currentTimeMillis();
					Methods.damageEntity(player, target, crushDamage);
				}
			}
		}
	
		for(int i = 0; i < trackedIngots.size(); i++)
		{
			Item ii = trackedIngots.get(i);
			if(ii.getItemStack().getType() == Material.IRON_INGOT)
			{
				for(Entity e : Methods.getEntitiesAroundPoint(ii.getLocation(), 2))
				{
					if(e instanceof LivingEntity && e.getEntityId() != player.getEntityId())
					{
						Methods.damageEntity(player, e, 0.5);
						
						if(e instanceof Player ||
								e instanceof Zombie ||
								e instanceof Skeleton)
						{
							if(target == null)
								target =  (LivingEntity) e;
							
							formArmor();
						}
						
						else
						{
							ii.getWorld().dropItem(ii.getLocation(), ii.getItemStack());
							remove();
						}

						ii.remove();
					}
				}
			}
		}
		
		removeDeadIngots();
	}
	
	public void removeDeadIngots()
	{
		for(int i = 0; i < trackedIngots.size(); i++)
		{
			Item ii = trackedIngots.get(i);
			if(ii.isDead())
			{
				trackedIngots.remove(ii);
			}
		}
	}
	
	public LivingEntity getTarget()
	{
		return target;
	}
	
	public void remove()
	{
		for(Item i : trackedIngots)
		{
			i.remove();
		}
		resetArmor();
		trackedIngots.clear();
		instances.remove(player);
	}
	
	public static void removeAll()
	{
		for(Player p : instances.keySet())
		{
			instances.get(p).remove();
		}
	}
	
	public static void progressAll()
	{
		for(Player p : instances.keySet())
		{
			instances.get(p).progress();
		}
	}
	
	public static boolean isControlled(Player player)
	{
		for(Player p : instances.keySet())
		{
			if(instances.get(p).getTarget() != null && instances.get(p).getTarget().getEntityId() == player.getEntityId())
			{
				return true;
			}
		}
		return false;
	}
}
