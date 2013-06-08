package de.bananaco.format;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.format.FormatHandler.Format;

public class FormatPlugin extends JavaPlugin implements Listener {
	
	FormatHandler handler = new FormatHandler();
	
	/*
	 * Bukkity stuff
	 */
	
	@Override
	public void onEnable() {
		loadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		handler.clear();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		event.setFormat(handler.format(event.getMessage(), event.getPlayer().getDisplayName(), event.getPlayer()));
	}
	
	/*
	 * Funky stuff
	 */
	
	public void loadConfig() {
		Configuration config = getConfig();
		// check the format section
		if(config.getString("format") == null) {
			config.set("format", handler.format);
			saveConfig();
		}
		// check the data section
		if(config.getConfigurationSection("data") == null) {
			ConfigurationSection data = config.createSection("data");
			// create example prefixes
			ConfigurationSection prefix = data.createSection("prefix");
			// admin prefix
			prefix.set("admin.format", "[&5Admin&f]");
			prefix.set("admin.node", "admin.prefix");
			// create example suffixes
			ConfigurationSection suffix = data.createSection("suffix");
			// admin suffix
			suffix.set("admin.format", "&5MLGP&f");
			suffix.set("admin.node", "admin.suffix");
			// save to file
			saveConfig();
		}
		// now the loading bits
		handler.setFormat(config.getString("format"));
		ConfigurationSection data = config.getConfigurationSection("data");
		// prefix, suffix etc
		for(String type : data.getKeys(false)) {
			ConfigurationSection names = data.getConfigurationSection(type);
			// ordered in the order that they should be loaded
			for(String name : names.getKeys(false)) {
				Format<String> f = new Format<String>(type, names.getConfigurationSection(name).getString("format"), names.getConfigurationSection(name).getString("node"));
				// load to plugin
				handler.addFormat(f);
			}
		}
	}

}
