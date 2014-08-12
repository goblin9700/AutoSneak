package me.mannil.autosneak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

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
  static ArrayList<UUID> sneakingPlayers = new ArrayList<UUID>();
  private HashMap<UUID, Long> cooldownTimes = new HashMap<UUID, Long>();
  public static AutoSneak Instance;
  private PluginDescriptionFile pdfFile;
  private String name;
  private String version;
  private static final Logger log = Logger.getLogger("Minecraft");
  private Configuration config;
  private String sneakOnMessage;
  private String sneakOffMessage;
  private String sneakCooldownMessage;
  private String sneakGiveMessage;
  private int sneakDuration;
  private int sneakCooldown;
  public static final Boolean debugging = false;

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
    this.config.addDefault("messages.sneakGive", "&7Player <player> is now sneaking.".replace("&", "§"));
    this.config.addDefault("messages.sneakCooldown", "&4You must wait <time> seconds before you may sneak again.".replace("&", "§"));
    this.config.addDefault("options.timers.duration", Integer.valueOf(0));
    this.config.addDefault("options.timers.cooldown", Integer.valueOf(0));
    this.config.addDefault("options.timers.refresh", Integer.valueOf(5));
    this.config.options().copyDefaults(true);
    this.sneakOnMessage = this.config.getString("messages.sneakOn").replace("&", "§");
    this.sneakOffMessage = this.config.getString("messages.sneakOff").replace("&", "§");
    this.sneakCooldownMessage = this.config.getString("messages.sneakCooldown").replace("&", "§");
    this.sneakGiveMessage = this.config.getString("messages.sneakGive").replace("&", "§");
    this.sneakDuration = this.config.getInt("options.timers.duration", 0);
    this.sneakCooldown = this.config.getInt("options.timers.cooldown", 0);
    saveConfig();
    setupAutosneak();

    String strEnable = "[" + this.name + "] " + this.version + " enabled.";
    log.info(strEnable);
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (!(sender instanceof Player)) return false;
    Player player = (Player)sender;
    if (!player.hasPermission("autosneak.sneak") && !player.isOp())
      return true;
    if (args.length == 2 && args[0].equals("give")){
    	if(Bukkit.getPlayer(args[1]) == null){
    		sender.sendMessage("Player not online");
    		return true;
    	}
    	Player p = Bukkit.getPlayer(args[1]);
    	toggleSneak(p);
    	sender.sendMessage(this.sneakGiveMessage.replaceAll("<player>", p.getName()));
    }
    else if (args.length == 0) {
      toggleSneak(player);
    } else if (args.length == 1){
      if (args[0].equalsIgnoreCase("on"))
        setSneak(player, true);
     
      else if (args[0].equalsIgnoreCase("off"))
        setSneak(player, false);
      
      else
        return false;
    } else {
    	return false;
    }
    return true;
  }

  private void setupAutosneak() {
    for (Player p : getServer().getOnlinePlayers())
      if (p.hasPermission("autosneak.auto")) {
        setSneak(p, true);
      }
  }

  private void toggleSneak(Player player)
  {
    if (sneakingPlayers.contains(player.getUniqueId())) {
      setSneak(player, false);
    }
    else
    {
      setSneak(player, true);
    }
  }

  public void setSneak(Player player, boolean sneak) {
    if (sneak) {
      if ((this.sneakCooldown > 0) && (this.cooldownTimes.containsKey(player.getUniqueId())) && (this.cooldownTimes.get(player.getUniqueId()) > System.currentTimeMillis())) {
        player.sendMessage(this.sneakCooldownMessage.replaceAll("<time>", Integer.toString((int)Math.ceil((this.cooldownTimes.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000L))));
        return;
      }
      if (!player.hasPermission("autosneak.exempt")) {
        if (this.sneakCooldown > 0) {
          this.cooldownTimes.put(player.getUniqueId(), System.currentTimeMillis() + this.sneakCooldown * 1000L);
        }
        if (this.sneakDuration > 0) {
          getServer().getScheduler().scheduleSyncDelayedTask(this, new SneakCooldown(player), this.sneakDuration * 20L);
        }
      }

      if (!sneakingPlayers.contains(player.getUniqueId())){
        sneakingPlayers.add(player.getUniqueId());
      }
      player.setSneaking(true);
      player.sendMessage(this.sneakOnMessage);
    }
    else {
      player.sendMessage(this.sneakOffMessage);
      if (sneakingPlayers.contains(player.getUniqueId())){
        sneakingPlayers.remove(player.getUniqueId());
      }
      player.setSneaking(false);
    }
  }

  public static void debug(String message)
  {
    if (debugging)
      log.info(message);
  }

  public void onDisable()
  {
    String strDisable = "[" + this.name + "] " + this.version + " disabled.";
    log.info(strDisable);
  }

  public class SneakCooldown implements Runnable {
    private Player player;

    public SneakCooldown(Player player){ this.player = player; }

    public void run(){
      if (this.player.isSneaking())
        AutoSneak.this.setSneak(this.player, false);
    }
  }
}