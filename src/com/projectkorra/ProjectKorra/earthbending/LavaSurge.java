package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class LavaSurge
{
    public static ConcurrentHashMap<Player, LavaSurge> instances = new ConcurrentHashMap<Player, LavaSurge>();
    public static int impactDamage = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.Damage");
    public static int cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.Cooldown");
    public static int fractureRadius = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.FractureRadius");
    public static int prepareRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.PrepareRange");
    public static int travelRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.TravelRange");
    public static int maxBlocks = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaSurge.MaxLavaWaves");
    public static boolean canSourceBeEarth = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.LavaSurge.SourceCanBeEarth");
    public static List<FallingBlock> falling = new ArrayList<FallingBlock>();
    public static int particleInterval = 100;
    public static int fallingBlockInterval = 100;
    
    private Player player;
    private Block sourceBlock;
    private long lastTime;
    private long time;
    private int fallingBlocksCount = 0;
    private boolean surgeStarted = false;
    private boolean fractureOpen;
    private Random randy = new Random();
    private Vector direction;
    private Location startLocation;
    // private Location currentLocation; // Unused.
    private List<FallingBlock> fblocks = new ArrayList<FallingBlock>();
    private List<Block> fracture = new ArrayList<Block>();
    private List<TempBlock> fracturetb = new ArrayList<TempBlock>();
    private List<TempBlock> movingLava = new ArrayList<TempBlock>();
    private ConcurrentHashMap<FallingBlock, TempBlock> lava = new ConcurrentHashMap<FallingBlock, TempBlock>();
    private ListIterator<Block> li;
    
    public LavaSurge(Player player)
    {
        this.player = player;
        
        if (!isEligible())
            return;
        
        if (Methods.getBendingPlayer(player.getName()).isOnCooldown("LavaSurge"))
            return;
        
        lastTime = System.currentTimeMillis();
        
        if (prepare())
        {
            instances.put(player, this);
        }
    }
    
    public boolean isEligible()
    {
        final BendingPlayer bplayer = Methods.getBendingPlayer(player.getName());
        
        if (!Methods.canBend(player.getName(), "LavaSurge"))
            return false;
        
        if (Methods.getBoundAbility(player) == null)
            return false;
        
        if (!Methods.getBoundAbility(player).equalsIgnoreCase("LavaSurge"))
            return false;
        
        if (Methods.isRegionProtectedFromBuild(player, "LavaSurge", player.getLocation()))
            return false;
        
        if (!Methods.canLavabend(player))
            return false;
        
        if (bplayer.isOnCooldown("LavaSurge"))
            return false;
        
        return true;
    }
    
    public boolean prepare()
    {
        Block targetBlock = Methods.getEarthSourceBlock(player, prepareRange);
        
        if (targetBlock == null ||
                !(targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) &&
                !isLava(targetBlock.getRelative(BlockFace.UP)))
            return false;
        
        if (instances.containsKey(player))
            instances.get(player).revertFracture();
        
        if ((canSourceBeEarth && Methods.isEarthbendable(player, targetBlock)) ||
                Methods.isLavabendable(targetBlock, player))
        {
            startLocation = targetBlock.getLocation().add(0, 1, 0);
            // currentLocation = startLocation; // Not needed.
            sourceBlock = targetBlock;
            return true;
        }
        
        return false;
    }
    
    public boolean isLava(Block b)
    {
        if (b.getType() == Material.STATIONARY_LAVA || b.getType() == Material.LAVA)
            return true;
        return false;
    }
    
    public void launch()
    {
        Location targetLocation = Methods.getTargetedLocation(player, travelRange * 2);
        
        try {
            targetLocation = Methods.getTargetedEntity(player, travelRange * 2, null).getLocation();
        } catch (NullPointerException e) {
        }
        ;
        
        if (targetLocation == null)
        {
            remove();
            return;
        }
        
        time = System.currentTimeMillis();
        direction = Methods.getDirection(startLocation, targetLocation).multiply(0.07);
        
        if (direction.getY() < 0)
            direction.setY(0);
        
        if (canSourceBeEarth)
            openFracture();
        else
            skipFracture();
    }
    
    public void openFracture()
    {
        
        List<Block> affectedBlocks = Methods.getBlocksAroundPoint(sourceBlock.getLocation(), fractureRadius);
        
        for (Block b : affectedBlocks)
        {
            if (Methods.isEarthbendable(player, b))
            {
                fracture.add(b);
            }
        }
        
        li = fracture.listIterator();
        
        fractureOpen = true;
        
        Methods.getBendingPlayer(player.getName()).addCooldown("LavaSurge", cooldown);
    }
    
    public void skipFracture()
    {
        li = fracture.listIterator();
        
        fractureOpen = true;
    }
    
    public void revertFracture()
    {
        for (TempBlock tb : fracturetb)
        {
            tb.revertBlock();
        }
        
        fracture.clear();
    }
    
    public void remove()
    {
        revertFracture();
        instances.remove(player);
    }
    
    public boolean canMoveThrough(Block block)
    {
        if (Methods.isTransparentToEarthbending(player, startLocation.getBlock()) ||
                Methods.isEarthbendable(player, startLocation.getBlock()) ||
                Methods.isLavabendable(startLocation.getBlock(), player))
            return true;
        return false;
    }
    
    public void removeLava()
    {
        for (TempBlock tb : lava.values())
        {
            tb.revertBlock();
        }
        
        movingLava.clear();
    }
    
    public void progress()
    {
        long curTime = System.currentTimeMillis();
        if (!player.isOnline() || player.isDead())
        {
            remove();
            return;
        }
        
        if (!surgeStarted && !Methods.getBoundAbility(player).equalsIgnoreCase("LavaSurge"))
        {
            remove();
            return;
        }
        
        if (!surgeStarted && sourceBlock != null &&
                curTime > lastTime + particleInterval)
        {
            lastTime = curTime;
            ParticleEffect.LAVA.display(sourceBlock.getLocation(), 0, 0, 0, 0, 1);
        }
        
        else if (surgeStarted && curTime > lastTime + particleInterval)
        {
            lastTime = curTime;
            for (FallingBlock fblock : fblocks)
                ParticleEffect.LAVA.display(fblock.getLocation(), 0, 0, 0, 0, 1);
        }
        
        if (fractureOpen && !surgeStarted)
        {
            if (!li.hasNext())
                surgeStarted = true;
            
            else
            {
                Block b = li.next();
                
                Methods.playEarthbendingSound(b.getLocation());
                
                for (int i = 0; i < 2; i++)
                {
                    TempBlock tb = new TempBlock(b, Material.STATIONARY_LAVA, (byte) 0);
                    fracturetb.add(tb);
                }
            }
        }
        
        if (surgeStarted)
        {
            if (fallingBlocksCount >= maxBlocks)
            {
                return;
            }
            
            if (curTime > time + (fallingBlockInterval * fallingBlocksCount))
            {
                FallingBlock fbs = Methods.spawnFallingBlock(sourceBlock.getLocation().add(0, 1, 0), 11, (byte) 0);
                fblocks.add(fbs);
                falling.add(fbs);
                double x = randy.nextDouble() / 5;
                double z = randy.nextDouble() / 5;
                
                x = (randy.nextBoolean()) ? -x : x;
                z = (randy.nextBoolean()) ? -z : z;
                
                fbs.setVelocity(direction.clone().add(new Vector(x, 0.2, z)).multiply(1.2));
                fbs.setDropItem(false);
                
                for (Block b : fracture)
                {
                    if (randy.nextBoolean() && b != sourceBlock)
                    {
                        FallingBlock fb = Methods.spawnFallingBlock(b.getLocation().add(new Vector(0, 1, 0)), 11, (byte) 0);
                        falling.add(fb);
                        fblocks.add(fb);
                        fb.setVelocity(direction.clone().add(new Vector(randy.nextDouble() / 10, 0.1, randy.nextDouble() / 10)).multiply(1.2));
                        fb.setDropItem(false);
                    }
                }
                
                fallingBlocksCount++;
            }
            
            for (FallingBlock fb : fblocks)
            {
                for (Entity e : Methods.getEntitiesAroundPoint(fb.getLocation(), 2))
                {
                    if (e instanceof LivingEntity)
                    {
                        if (e.getEntityId() != player.getEntityId())
                        {
                            Methods.damageEntity(player, e, impactDamage);
                            e.setFireTicks(100);
                            Methods.setVelocity(e, direction.clone());
                        }
                    }
                }
            }
        }
    }
    
    public static void progressAll()
    {
        for (Player p : instances.keySet())
        {
            instances.get(p).progress();
        }
    }
}
