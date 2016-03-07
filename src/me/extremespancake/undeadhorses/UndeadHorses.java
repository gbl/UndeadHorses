// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import de.guntram.bukkit.library.DefaultMessageLoader;
import de.guntram.bukkit.library.MessageLoader;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.GameMode;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionEffect;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class UndeadHorses extends JavaPlugin implements Listener
{
    public static boolean MustBeOwnerToConvert;
    public static boolean MustBeOwnerToCure;
    public static boolean MustBeNight;
    public static int XPCost;
    public static List<PotionEffect> ZombieEffects;
    public static List<PotionEffect> SkeletonEffects;
    public static boolean SkelDealBack;
    public static boolean ZombDealBack;
    PluginDescriptionFile pdf;
    static final Logger logger = Logger.getLogger("Minecraft");
    public UndeadHorsesListener listener;
    private static MessageLoader messageLoader;

    static {
        UndeadHorses.ZombieEffects = new ArrayList<>();
        UndeadHorses.SkeletonEffects = new ArrayList<>();
    }
    
    @Override
    public void onEnable() {
        this.loadConfig();
        this.pdf = this.getDescription();
        this.listener = new UndeadHorsesListener(this);
        logger.log(Level.INFO,"{0}[{1}" + "]" + " has been enabled! By extremespancake.", new Object[]{String.valueOf(this.pdf.getName()), this.pdf.getVersion()});
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)new UndeadHorsesEvent(this), 60L, 60L);
        messageLoader = DefaultMessageLoader.getInstance(this);
    }
    
    @Override
    public void onDisable() {
        logger.log(Level.INFO,"{0}[{1}" + "]" + " has been disabled!", new Object[]{String.valueOf(this.pdf.getName()), this.pdf.getVersion()});
    }
    
    public void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.getConfig().options().copyHeader(true);
        this.saveDefaultConfig();
        this.saveConfig();
        final FileConfiguration config = this.getConfig();
        this.loadEffects(config);
        UndeadHorses.MustBeOwnerToConvert = config.getBoolean("MustBeOwnerToConvert");
        UndeadHorses.MustBeOwnerToCure = config.getBoolean("MustBeOwnerToCure");
        UndeadHorses.MustBeNight = config.getBoolean("MustBeNight");
        UndeadHorses.XPCost = config.getInt("XPCostInLevels");
        UndeadHorses.ZombDealBack = config.getBoolean("SkeletonHorsesReturnDamage");
        UndeadHorses.ZombDealBack = config.getBoolean("ZombieHorsesReturnDamage");
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (commandLabel.equalsIgnoreCase("undeadhorses") && sender.hasPermission("undeadhorses.reload")) {
            if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
                this.loadConfig();
                messageLoader.reloadMessages();
                sender.sendMessage(messageLoader.getMessage("feedback.cmd.configreloaded"));
            }
        }
        else {
            sender.sendMessage(messageLoader.getMessage("feedback.cmd.prefix"));
        }
        return true;
    }
    
    public static void convertHorse(final Horse.Variant variant, final Horse horse, final Player player) {
        if (!UndeadHorses.MustBeOwnerToConvert
        || player.hasPermission("undeadhorses.convertunownedhorsesbypass") 
        || (horse.isTamed() && (horse.getOwner().getName().equals(player.getName()) 
        || horse.getOwner().getName().equals(player.getDisplayName())))) {
            if (horse.getInventory().getArmor() == null) {
                horse.setVariant(variant);
                player.sendMessage(messageLoader.getMessage("feedback.ui.convertedhorse"));
                horse.setTamed(true);
                horse.setOwner((AnimalTamer)player);
                player.getWorld().playSound(horse.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 10.0f, 1.0f);
                horse.getWorld().playEffect(horse.getLocation(), Effect.MOBSPAWNER_FLAMES, 2004);
            }
            else {
                player.sendMessage(messageLoader.getMessage("feedback.ui.removearmor"));
            }
        }
        else if (horse.getOwner() != null) {
            player.sendMessage(messageLoader.getMessage("feedback.ui.hasowner", horse.getOwner().getName()));
        }
        else {
            player.sendMessage(messageLoader.getMessage("feedback.ui.iswild"));
        }
    }
    
    public static void cureHorse(final Horse horse, final Player player) {
        if (horse.getVariant() == Horse.Variant.UNDEAD_HORSE | horse.getVariant() == Horse.Variant.SKELETON_HORSE) {
            if (!UndeadHorses.MustBeOwnerToCure || player.hasPermission("undeadhorses.cureunownedhorsesbypass")
            || (horse.isTamed()
                && (  horse.getOwner().getName().equals(player.getName())
                ||    horse.getOwner().getName().equals(player.getDisplayName())
                   )
               )
            ) {
                horse.setVariant(Horse.Variant.HORSE);
                player.sendMessage(messageLoader.getMessage("feedback.ui.curedhorse"));
                player.playSound(horse.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 10.0f, 1.0f);
            }
            else {
                player.sendMessage(messageLoader.getMessage("feedback.ui.notowner"));
            }
        }
    }
    
    public static boolean chargePlayerXP(final Player player) {
        final int level = player.getLevel();
        if (UndeadHorses.XPCost <= 0) {
            return true;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        if (level >= UndeadHorses.XPCost) {
            player.setLevel(player.getLevel() - UndeadHorses.XPCost);
            player.sendMessage(messageLoader.getMessage("feedback.ui.xpcharged", level, UndeadHorses.XPCost));
            return true;
        }
        player.sendMessage(messageLoader.getMessage("feedback.ui.xpcharged", UndeadHorses.XPCost));
        return false;
    }
    
    public void loadEffects(final FileConfiguration config) {
        for (final String EffectInf : config.getStringList("ZombieHorseEffects")) {
            final String[] ZombValues = EffectInf.toUpperCase().replaceAll(" ", "").split(",");
            final int duration = 30;
            int strength = 1;
            final PotionEffectType type = PotionEffectType.getByName(ZombValues[0].toUpperCase());
            try {
                if (ZombValues.length > 1) {
                    strength = Integer.parseInt(ZombValues[1]);
                }
            }
            catch (NumberFormatException e) {
                logger.log(Level.SEVERE, "Could not convert {0} to a strength (Must be an whole number!)", ZombValues[1]);
            }
            if (type == null) {
                logger.log(Level.SEVERE, "Could not convert {0} to a potion type", ZombValues[0]);
            }
            else {
                ZombieEffects.add(type.createEffect(duration * 20, strength));
                logger.log(Level.INFO, "Added effect {0} strength {1} for zombie horses!", new Object[]{ZombValues[0], strength});
            }
        }
        for (final String EffectInf : config.getStringList("SkeletonHorseEffects")) {
            final String[] SkelValues = EffectInf.toUpperCase().replaceAll(" ", "").split(",");
            final int duration = 30;
            int strength = 1;
            final PotionEffectType type = PotionEffectType.getByName(SkelValues[0].toUpperCase());
            try {
                if (SkelValues.length > 1) {
                    strength = Integer.parseInt(SkelValues[1]);
                }
            }
            catch (NumberFormatException e) {
                logger.log(Level.SEVERE, "Could not convert {0} to a strength (Must be an whole number!)", SkelValues[1]);
            }
            if (type == null) {
                logger.log(Level.SEVERE, "Could not convert {0} to a potion type", SkelValues[0]);
            }
            else {
                UndeadHorses.SkeletonEffects.add(type.createEffect(duration * 20, strength));
                logger.log(Level.INFO, "Added effect {0} strength {1} for skeleton horses!", new Object[]{SkelValues[0], strength});
            }
        }
    }
    
    public static boolean isDay(final World world) {
        final long time = world.getTime();
        return time < 12300L || time > 23850L;
    }
    
    public MessageLoader getMessageLoader() {
        return messageLoader;
    }
}
