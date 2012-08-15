package me.mannil.autosneak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import net.h31ix.anticheat.api.AnticheatAPI;
import net.h31ix.anticheat.manage.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSneak extends JavaPlugin
{
  static ArrayList<String> sneakingPlayers = new ArrayList<String>();
  private HashMap<String, Long> cooldownTimes = new HashMap<String, Long>();
  private static Timer refreshTimer = null;
  public static AutoSneak Instance;
  private PluginDescriptionFile pdfFile;
  private String name;
  private String version;
  private static final Logger log = Logger.getLogger("Minecraft");
  private Configuration config;
  private String sneakOnMessage;
  private String sneakOffMessage;
  private String sneakCooldownMessage;
  private int refreshInterval;
  private int sneakDuration;
  private int sneakCooldown;
  public static final Boolean debugging = Boolean.valueOf(false);

  public void onEnable()
  {
    this.config = getConfig();
    this.pdfFile = getDescription();
    this.name = this.pdfFile.getName();
    this.version = this.pdfFile.getVersion();

    @SuppressWarnings("unused")
	Listener listener = new AutoSneakListener(this);

    this.config.addDefault("messages.sneakOn", "&7You are now sneaking.".replace("&", "§"));
    this.config.addDefault("messages.sneakOff", "&7You are no longer sneaking.".replace("&", "§"));
    this.config.addDefault("messages.sneakCooldown", "&4You must wait <time> seconds before you may sneak again.".replace("&", "§"));
    this.config.addDefault("options.timers.duration", Integer.valueOf(0));
    this.config.addDefault("options.timers.cooldown", Integer.valueOf(0));
    this.config.addDefault("options.timers.refresh", Integer.valueOf(5));
    this.config.options().copyDefaults(true);
    this.sneakOnMessage = this.config.getString("messages.sneakOn").replace("&", "§");
    this.sneakOffMessage = this.config.getString("messages.sneakOff").replace("&", "§");
    this.sneakCooldownMessage = this.config.getString("messages.sneakCooldown").replace("&", "§");
    this.refreshInterval = this.config.getInt("options.timers.refresh");
    this.sneakDuration = this.config.getInt("options.timers.duration", 0);
    this.sneakCooldown = this.config.getInt("options.timers.cooldown", 0);
    saveConfig();
    setupAutosneak();
    setupRefresh();

    String strEnable = "[" + this.name + "] " + this.version + " enabled.";
    log.info(strEnable);
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (!(sender instanceof Player)) return false;
    Player player = (Player)sender;

    if (!player.hasPermission("autosneak.sneak"))
      return true;
    if (args.length > 1)
      return false;
    if (args.length == 0) {
      toggleSneak(player);
    } else {
      if (args[0].equalsIgnoreCase("on"))
        setSneak(player, true);
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.exemptPlayer((Player)sender, CheckType.SNEAK);
      }
      else if (args[0].equalsIgnoreCase("off"))
        setSneak(player, false);
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.unexemptPlayer((Player)sender, CheckType.SNEAK);
      }
      else
        return false;
    }
    return true;
  }

  private void setupAutosneak() {
    for (Player p : getServer().getOnlinePlayers())
      if (p.hasPermission("autosneak.auto")) {
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
          AnticheatAPI.exemptPlayer(p, CheckType.SNEAK);
        }
        p.setSneaking(true);
        sneakingPlayers.add(p.getName());
      }
  }

  private void toggleSneak(Player player)
  {
    if (sneakingPlayers.contains(player.getName())) {
      setSneak(player, false);
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null)
        AnticheatAPI.unexemptPlayer(player, CheckType.SNEAK);
    }
    else
    {
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.exemptPlayer(player, CheckType.SNEAK);
      }
      setSneak(player, true);
    }
  }

  private void setSneak(Player player, boolean sneak) {
    if (sneak) {
      if ((this.sneakCooldown > 0) && (this.cooldownTimes.containsKey(player.getName())) && (((Long)this.cooldownTimes.get(player.getName())).longValue() > System.currentTimeMillis())) {
        player.sendMessage(this.sneakCooldownMessage.replaceAll("<time>", Integer.toString((int)Math.ceil((((Long)this.cooldownTimes.get(player.getName())).longValue() - System.currentTimeMillis()) / 1000L))));
        return;
      }
      if (!player.hasPermission("autosneak.exempt")) {
        if (this.sneakCooldown > 0) {
          this.cooldownTimes.put(player.getName(), Long.valueOf(System.currentTimeMillis() + this.sneakCooldown * 1000L));
        }
        if (this.sneakDuration > 0) {
          getServer().getScheduler().scheduleSyncDelayedTask(this, new SneakCooldown(player), this.sneakDuration * 20L);
        }
      }
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.exemptPlayer(player, CheckType.SNEAK);
      }
      player.setSneaking(true);
      player.sendMessage(this.sneakOnMessage);

      if (!sneakingPlayers.contains(player.getName()))
        sneakingPlayers.add(player.getName());
    }
    else {
      player.setSneaking(false);
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.unexemptPlayer(player, CheckType.SNEAK);
      }
      player.sendMessage(this.sneakOffMessage);
      if (sneakingPlayers.contains(player.getName()))
        sneakingPlayers.remove(player.getName());
    }
  }

  private void setupRefresh()
  {
    if (this.refreshInterval != 0) {
      refreshTimer = new Timer();
      refreshTimer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          if (!AutoSneak.sneakingPlayers.isEmpty())
            for (String p : AutoSneak.sneakingPlayers) {
              Bukkit.getServer().getPlayerExact(p).setSneaking(false);
              Bukkit.getServer().getPlayerExact(p).setSneaking(true);
            }
        }
      }
      , 500L, this.refreshInterval * 1000L);
    }
  }

  public static void debug(String message)
  {
    if (debugging.booleanValue())
      log.info(message);
  }

  public void onDisable()
  {
    String strDisable = "[" + this.name + "] " + this.version + " disabled.";
    log.info(strDisable);
  }
  public class SneakCooldown implements Runnable {
    private Player player;

    public SneakCooldown(Player player) { this.player = player; }

    public void run()
    {
      if (this.player.isSneaking())
        AutoSneak.this.setSneak(this.player, false);
    }
  }
}