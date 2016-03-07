// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Horse;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;

public class UndeadHorsesListener implements Listener
{
    @EventHandler
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Horse) {
            final Horse horse = (Horse)event.getEntity();
            if (((horse.getVariant() == Horse.Variant.SKELETON_HORSE && UndeadHorses.SkelDealBack) | (horse.getVariant() == Horse.Variant.UNDEAD_HORSE && UndeadHorses.ZombDealBack)) && !event.getCause().equals((Object)EntityDamageEvent.DamageCause.PROJECTILE)) {
                final LivingEntity Damager = (LivingEntity)event.getDamager();
                Damager.damage((double)Math.round(event.getDamage() / 3.0), (Entity)Damager);
            }
            else if (((horse.getVariant() == Horse.Variant.SKELETON_HORSE && UndeadHorses.SkelDealBack) | (horse.getVariant() == Horse.Variant.UNDEAD_HORSE && UndeadHorses.ZombDealBack)) && event.getCause().equals((Object)EntityDamageEvent.DamageCause.PROJECTILE)) {
                final Projectile proj = (Projectile)event.getDamager();
                if (proj.getShooter() instanceof LivingEntity) {
                    final LivingEntity shooter = (LivingEntity) proj.getShooter();
                    shooter.damage((double)Math.round(event.getDamage() / 3.0), (Entity)shooter);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player p = event.getPlayer();
        final Entity e = event.getRightClicked();
        p.sendMessage("item in cursor is "+p.getItemInHand().getType());
        if (e!=null) 
            p.sendMessage("You clicked a "+e.getClass().getCanonicalName());
        if (e instanceof Horse) {
            final Horse h = (Horse)e;
            if (h.getVariant() == Horse.Variant.SKELETON_HORSE & !p.hasPermission("undeadhorses.skeletonride")) {
                p.sendMessage(ChatColor.RED + "This horse cannot be ridden!");
                event.setCancelled(true);
            }
            else if (h.getVariant() == Horse.Variant.UNDEAD_HORSE & !p.hasPermission("undeadhorses.zombieride")) {
                p.sendMessage(ChatColor.RED + "This horse cannot be ridden!");
                event.setCancelled(true);
            }
            Material material=p.getItemInHand().getType();
            p.sendMessage("item in cursor is "+p.getItemInHand().getType());
            if (material == Material.BONE && p.hasPermission("undeadhorses.skeleton")) {
                if (UndeadHorses.MustBeNight && UndeadHorses.isDay(p.getWorld())) {
                    p.sendMessage(ChatColor.RED + "It must be night time to convert a horse!");
                    return;
                }
                if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(Horse.Variant.SKELETON_HORSE, h, p);
                    event.setCancelled(true);
                }
            }
            else if (material == Material.ROTTEN_FLESH && p.hasPermission("undeadhorses.zombie")) {
                event.setCancelled(true);
                if (UndeadHorses.MustBeNight && UndeadHorses.isDay(p.getWorld())) {
                    p.sendMessage(ChatColor.RED + "It must be night time to convert a horse!");
                    return;
                }
                if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(Horse.Variant.UNDEAD_HORSE, h, p);
                    event.setCancelled(true);
                }
            }
            else if (material == Material.GOLD_NUGGET || material == Material.GOLD_INGOT) {
                if (h.getVariant() == Horse.Variant.SKELETON_HORSE && p.hasPermission("undeadhorses.skeletoncure")) {
                    UndeadHorses.cureHorse(h, p);
                    event.setCancelled(true);
                }
                else if (h.getVariant() == Horse.Variant.UNDEAD_HORSE && p.hasPermission("undeadhorses.zombiecure")) {
                    UndeadHorses.cureHorse(h, p);
                    event.setCancelled(true);
                }
                else {
                    p.sendMessage(ChatColor.RED + "You cannot cure that horse!");
                }
            }
        }
    }
}
