package me.mannil.autosneak;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AutoSneakListener
  implements Listener
{
private final AutoSneak plugin;

  public AutoSneakListener(AutoSneak plugin)
  {
    this.plugin = AutoSneak.Instance;
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) { 
	  if (event.getPlayer().hasPermission("autosneak.auto")) {
	      plugin.setSneak(event.getPlayer(), true);
	  }
    } 

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName())) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getName());
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    if ((!AutoSneak.sneakingPlayers.isEmpty()) && (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName()))) {
    	plugin.setSneak(event.getPlayer(), false);
    }
  }
  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e){
	  if(!AutoSneak.sneakingPlayers.isEmpty()){
		  if(AutoSneak.debugging.booleanValue()){
			  AutoSneak.debug("sneakingPlayers Count: " + AutoSneak.sneakingPlayers.size());
		  }
		  if(AutoSneak.sneakingPlayers.contains(e.getPlayer().getName())){
			  if(AutoSneak.debugging.booleanValue()){
				  AutoSneak.debug("sneakingPlayers contains name: " + e.getPlayer().getName());
			  }
			  e.getPlayer().setSneaking(true);
			  e.setCancelled(true);
		  }
	  }
  }

}