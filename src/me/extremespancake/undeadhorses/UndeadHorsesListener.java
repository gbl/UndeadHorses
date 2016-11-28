// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Horse;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;

public class UndeadHorsesListener implements Listener
{
    private final UndeadHorses plugin;
    
    UndeadHorsesListener(UndeadHorses p) {
        plugin=p;
    }
    
    @EventHandler
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        Entity target=event.getEntity();
        if (target instanceof SkeletonHorse || target instanceof ZombieHorse || target instanceof Horse) {
            final AbstractHorse horse = (AbstractHorse)target;
            if ((horse instanceof SkeletonHorse && UndeadHorses.SkelDealBack)
              || (horse instanceof ZombieHorse   && UndeadHorses.ZombDealBack)) {
                
                if (event.getCause().equals((Object)EntityDamageEvent.DamageCause.PROJECTILE)) {
                    final Projectile proj = (Projectile)event.getDamager();
                    if (proj.getShooter() instanceof LivingEntity) {
                        final LivingEntity shooter = (LivingEntity) proj.getShooter();
                        shooter.damage((double)Math.round(event.getDamage() / 3.0), (Entity)shooter);
                    }
                } else {
                    final LivingEntity damager = (LivingEntity)event.getDamager();
                    damager.damage((double)Math.round(event.getDamage() / 3.0), (Entity)damager);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player p = event.getPlayer();
        final Entity e = event.getRightClicked();
        if (e instanceof SkeletonHorse || e instanceof ZombieHorse || e instanceof Horse) {
            if (e instanceof SkeletonHorse & !p.hasPermission("undeadhorses.skeletonride")) {
                p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.cannotride"));
                event.setCancelled(true);
            }
            else if (e instanceof ZombieHorse & !p.hasPermission("undeadhorses.zombieride")) {
                p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.cannotride"));
                event.setCancelled(true);
            }
            Material material=p.getInventory().getItemInMainHand().getType();
            if (material==Material.AIR)
                material=p.getInventory().getItemInOffHand().getType();
            if (e instanceof Horse && material == Material.BONE && p.hasPermission("undeadhorses.skeleton")) {
                event.setCancelled(true);
                if (UndeadHorses.MustBeNight && UndeadHorses.isDay(p.getWorld())) {
                    p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.onlyatnight"));
                    return;
                }
                if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(SkeletonHorse.class, (Horse) e, p);
                }
            }
            else if (e instanceof Horse && material == Material.ROTTEN_FLESH && p.hasPermission("undeadhorses.zombie")) {
                event.setCancelled(true);
                if (UndeadHorses.MustBeNight && UndeadHorses.isDay(p.getWorld())) {
                    p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.onlyatnight"));
                    return;
                }
                if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(ZombieHorse.class, (Horse) e, p);
                    event.setCancelled(true);
                }
            }
            else if (material == Material.GOLD_NUGGET || material == Material.GOLD_INGOT) {
                if (e instanceof SkeletonHorse) {
                    if (p.hasPermission("undeadhorses.skeletoncure")) {
                        UndeadHorses.cureHorse((SkeletonHorse)e, p);
                    } else {
                        p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.nocureperms"));
                    }
                } else if (e instanceof ZombieHorse) {
                    if (p.hasPermission("undeadhorses.zombiecure")) {
                        UndeadHorses.cureHorse((ZombieHorse)e, p);
                    } else {
                        p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.nocureperms"));
                    }
                } else {
                    p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.nocureneeded"));
                }
                event.setCancelled(true);
            }
        }
    }
}
