// 
// Decompiled by Procyon v0.5.30
// 

package me.extremespancake.undeadhorses;

import org.bukkit.World;
import java.util.Iterator;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.GameMode;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse;
import org.bukkit.ChatColor;
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
    Logger logger;
    public UndeadHorsesListener listener;
    
    static {
        UndeadHorses.ZombieEffects = new ArrayList<PotionEffect>();
        UndeadHorses.SkeletonEffects = new ArrayList<PotionEffect>();
    }
    
    public UndeadHorses() {
        this.logger = Logger.getLogger("Minecraft");
    }
    
    public void onEnable() {
        this.loadConfig();
        this.pdf = this.getDescription();
        this.listener = new UndeadHorsesListener();
        this.logger.info(String.valueOf(this.pdf.getName()) + "[" + this.pdf.getVersion() + "]" + " has been enabled! By extremespancake.");
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)new UndeadHorsesEvent(this), 60L, 60L);
    }
    
    public void onDisable() {
        this.logger.info(String.valueOf(this.pdf.getName()) + "[" + this.pdf.getVersion() + "]" + " has been disabled!");
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
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (commandLabel.equalsIgnoreCase("undeadhorses") && sender.hasPermission("undeadhorses.reload")) {
            if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                this.loadConfig();
            }
        }
        else {
            sender.sendMessage(ChatColor.GREEN + "Prefix for all UndeadHorses commands!");
        }
        return true;
    }
    
    public static void convertHorse(final Horse.Variant variant, final Horse horse, final Player player) {
        if (!UndeadHorses.MustBeOwnerToConvert | player.hasPermission("undeadhorses.convertunownedhorsesbypass") | (horse.isTamed() && (horse.getOwner().getName() == player.getName() | horse.getOwner().getName() == player.getDisplayName()))) {
            if (horse.getInventory().getArmor() == null) {
                horse.setVariant(variant);
                player.sendMessage(ChatColor.GREEN + "You turned that horse over to the undead...");
                horse.setTamed(true);
                horse.setOwner((AnimalTamer)player);
                player.getWorld().playSound(horse.getLocation(), Sound.ZOMBIE_INFECT, 10.0f, 1.0f);
                horse.getWorld().playEffect(horse.getLocation(), Effect.MOBSPAWNER_FLAMES, 2004);
            }
            else {
                player.sendMessage(ChatColor.RED + "You must remove that horse's armour first!");
            }
        }
        else if (horse.getOwner() != null) {
            player.sendMessage(ChatColor.RED + "You are not the owner of this horse! The owner is: " + horse.getOwner().getName());
        }
        else {
            player.sendMessage(ChatColor.RED + "You are not the owner of this horse! It is wild!");
        }
    }
    
    public static void cureHorse(final Horse horse, final Player player) {
        if (horse.getVariant() == Horse.Variant.UNDEAD_HORSE | horse.getVariant() == Horse.Variant.SKELETON_HORSE) {
            if (!UndeadHorses.MustBeOwnerToCure | player.hasPermission("undeadhorses.cureunownedhorsesbypass") | (horse.isTamed() && (horse.getOwner().getName() == player.getName() | horse.getOwner().getName() == player.getDisplayName()))) {
                horse.setVariant(Horse.Variant.HORSE);
                player.sendMessage(ChatColor.GREEN + "You cured that horse!");
                player.playSound(horse.getLocation(), Sound.ZOMBIE_REMEDY, 10.0f, 1.0f);
            }
            else {
                player.sendMessage(ChatColor.RED + "You are not the owner of this horse!");
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
            player.sendMessage(new StringBuilder().append(level).toString());
            player.sendMessage(ChatColor.GREEN + "You have been charged " + UndeadHorses.XPCost + " Levels!");
            return true;
        }
        player.sendMessage(ChatColor.RED + "You do not have enough levels to convert a horse, you need " + UndeadHorses.XPCost + " levels");
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
                this.logger.severe("Could not convert " + ZombValues[1] + " to a strength (Must be an whole number!)");
            }
            if (type == null) {
                this.logger.severe("Could not convert " + ZombValues[0] + " to a potion type");
            }
            else {
                UndeadHorses.ZombieEffects.add(type.createEffect(duration * 20, strength));
                this.logger.info("Added effect " + ZombValues[0] + " strength " + strength + " for zombie horses!");
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
                this.logger.severe("Could not convert " + SkelValues[1] + " to a strength (Must be an whole number!)");
            }
            if (type == null) {
                this.logger.severe("Could not convert " + SkelValues[0] + " to a potion type");
            }
            else {
                UndeadHorses.SkeletonEffects.add(type.createEffect(duration * 20, strength));
                this.logger.info("Added effect " + SkelValues[0] + " strength " + strength + " for skeleton horses!");
            }
        }
    }
    
    public static boolean isDay(final World world) {
        final long time = world.getTime();
        return time < 12300L || time > 23850L;
    }
}
