// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import java.util.Collection;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class UndeadHorsesEvent extends BukkitRunnable
{
    private final JavaPlugin plugin;
    
    public UndeadHorsesEvent(final JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (final World world : Bukkit.getWorlds()) {
            for (final Entity e : world.getEntities()) {
                if (e instanceof Horse || e instanceof SkeletonHorse || e instanceof ZombieHorse) {
                    if (UndeadHorses.isDay(world)
                        || ( e instanceof Horse)
                    ) {
                        continue;
                    }
                    world.playSound(e.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 20.0f, 4.0f);
                    world.playEffect(e.getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
                    if (e instanceof SkeletonHorse) {
                        ((SkeletonHorse) e).addPotionEffects((Collection<PotionEffect>)UndeadHorses.SkeletonEffects);
                    } else if (e instanceof ZombieHorse) {
                        ((ZombieHorse) e).addPotionEffects((Collection<PotionEffect>)UndeadHorses.ZombieEffects);
                    }
                }
            }
        }
    }
}
