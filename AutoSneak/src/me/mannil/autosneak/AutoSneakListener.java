package me.mannil.autosneak;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AutoSneakListener implements Listener{
private final AutoSneak plugin;

  public AutoSneakListener(AutoSneak plugin)
  {
    this.plugin = AutoSneak.Instance;
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) { 
	  if (event.getPlayer().hasPermission("autosneak.auto")) {
	      plugin.setSneak(event.getPlayer().getUniqueId(), true);
	  }
    } 

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getUniqueId())) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getUniqueId());
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    if ((!AutoSneak.sneakingPlayers.isEmpty()) && (AutoSneak.sneakingPlayers.contains(event.getPlayer().getUniqueId()))) {
    	plugin.setSneak(event.getPlayer().getUniqueId(), false);
    }
  }
  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e){
	  if(!AutoSneak.sneakingPlayers.isEmpty()){
		  if(AutoSneak.debugging){
			  AutoSneak.debug("sneakingPlayers Count: " + AutoSneak.sneakingPlayers.size());
		  }
		  if(AutoSneak.sneakingPlayers.contains(e.getPlayer().getUniqueId())){
			  if(AutoSneak.debugging){
				  AutoSneak.debug("sneakingPlayers contains name: " + e.getPlayer().getName());
			  }
			  e.getPlayer().setSneaking(true);
			  e.setCancelled(true);
		  }
	  }
  }

}