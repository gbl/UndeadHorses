// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import java.util.Iterator;
import java.util.Collection;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UndeadHorsesEvent extends BukkitRunnable
{
    private final JavaPlugin plugin;
    
    public UndeadHorsesEvent(final JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void run() {
        for (final World world : Bukkit.getWorlds()) {
            for (final Entity e : world.getEntities()) {
                if (e instanceof Horse) {
                    final Horse h = (Horse)e;
                    if (!(h.getVariant() == Horse.Variant.SKELETON_HORSE | h.getVariant() == Horse.Variant.UNDEAD_HORSE) || UndeadHorses.isDay(world)) {
                        continue;
                    }
                    world.playSound(h.getLocation(), Sound.FIRE, 20.0f, 4.0f);
                    world.playEffect(h.getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
                    if (h.getVariant() == Horse.Variant.SKELETON_HORSE) {
                        h.addPotionEffects((Collection)UndeadHorses.SkeletonEffects);
                    }
                    else {
                        if (h.getVariant() != Horse.Variant.UNDEAD_HORSE) {
                            continue;
                        }
                        h.addPotionEffects((Collection)UndeadHorses.ZombieEffects);
                    }
                }
            }
        }
    }
}
