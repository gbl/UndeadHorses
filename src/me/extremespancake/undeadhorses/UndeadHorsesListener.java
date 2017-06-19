package me.extremespancake.undeadhorses;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Horse;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

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
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled=true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Entity e = event.getRightClicked();
        if (e instanceof SkeletonHorse || e instanceof ZombieHorse || e instanceof Horse) {
            // When the original horse is killed, we sometimes get another
            // interact event with the dead horse; ignore that one
            if (((LivingEntity)e).getHealth()==0.0)
                return;
            final Player p = event.getPlayer();
            final Location playerLocation = p.getLocation();
            float pitch = playerLocation.getPitch();
            float yaw = playerLocation.getYaw();
            System.out.println("at event: player pitch="+pitch+", yaw="+yaw);
            System.out.println("target is a "+e.getClass().getSimpleName()+ " with "+((LivingEntity)e).getHealth()+" hp");
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
                }
                else if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(SkeletonHorse.class, (Horse) e, p);
                }
            }
            else if (e instanceof Horse && material == Material.ROTTEN_FLESH && p.hasPermission("undeadhorses.zombie")) {
                event.setCancelled(true);
                if (UndeadHorses.MustBeNight && UndeadHorses.isDay(p.getWorld())) {
                    p.sendMessage(plugin.getMessageLoader().getMessage("feedback.ui.onlyatnight"));
                }
                else if (UndeadHorses.chargePlayerXP(p)) {
                    UndeadHorses.convertHorse(ZombieHorse.class, (Horse) e, p);
                }
            }
            else if (material == Material.GOLD_NUGGET || material == Material.GOLD_INGOT) {
                event.setCancelled(true);
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
            }
            if (event.isCancelled()) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                    @Override
                    public void run() {
                        System.out.println("repairing pitch and yaw to "+playerLocation.getYaw()+"/"+playerLocation.getPitch());
                        // We need cause UNKNOWN, not PLUGIN, to prevent Essentials messing up our /back position
                        p.teleport(playerLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    }
                }, 3);
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(final EntityDeathEvent e) {
        if (e.getEntity() == UndeadHorses.getHorseToReplace()) {
            System.out.println("preventing drops when entity dies, previous number of drops is "+e.getDrops().size());
            List<ItemStack> drops = e.getDrops();
            drops.clear();
        }
    }
}
