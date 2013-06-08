package de.bananaco.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;

/**
 * A class to be used as part of a simple chat formatting plugin
 * Accepts Format<String> as the main container of formatData, which should be parsed from a config
 */
public class FormatHandler {
	/**
	 * This is where we store our stuff.
	 * In the eventuality that this might want to be used slightly differently in future,
	 * generic typing is used. It's unlikely, but you never know.
	 * @param <E>
	 */
	public static class Format<E> {
		// all our stuff belong to us
		private final String key;
		private final E data;
		private final String permission;

		public Format(String key, E data, String permission) {
			this.key = key;
			this.data = data;
			this.permission = permission;
		}

		/**
		 * Returns the (String)key, this is also where the data is stored in formatData
		 * @return key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * Returns the (E) stored data
		 * @return data
		 */
		public E getData() {
			return data;
		}

		/**
		 * Returns the (String) permission
		 * This is used to check if the format should apply
		 * @return permission
		 */
		public String getPermission() {
			return permission;
		}

		@Override
		public String toString() {
			return "{"+key+", "+String.valueOf(data)+", "+permission+"}";
		}

	}

	// contains the data for each format eg <prefix> <suffix>
	private Map<String, List<Format<String>>> formatData = new HashMap<String, List<Format<String>>>();
	protected char[] identifiers = new char[] {'<', '>'};
	protected String format = "<prefix> <name>: <message>";
	private String colorChat = "color.chat";

	/**
	 * Add the Format<String> to the existing Map formatData
	 * Saves the order things are added in (eg highest priority formats should be added first)
	 * @param format
	 */
	public void addFormat(Format<String> format) {
		// does the entry not exist? create a place to put it if not.
		if(!formatData.containsKey(format.getKey())) {
			formatData.put(format.getKey(), new ArrayList<Format<String>>());
		} 
		// now insert into the data
		formatData.get(format.getKey()).add(format);
	}
	
	/**
	 * Used to set the format to whatever metadata based way the user wishes it to be displayed
	 * Should follow the general idea of
	 * <prefix> <name> <suffix>: <message>
	 * @param format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	// only to be used if reloading the existing data
	/**
	 * Clears all the formatData contained within
	 */
	public void clear() {
		formatData.clear();
	}

	// format the key, convenience method
	/**
	 * Internal use, used in format(message, name, perm)
	 * @param key
	 * @return <key>
	 */
	private String formatKey(String key) {
		return new StringBuilder().append(identifiers[0]).append(key).append(identifiers[1]).toString();
	}

	// main use, should be thread safe, reasonably
	/**
	 * Main method, used to format nicely according to Bukkit api
	 * Will insert a blank format, if none with the permission exists.
	 * @param message
	 * @param name
	 * @param perm
	 * @return String<formatted>
	 */
	public String format(String message, String name, Permissible perm) {
		String formatted = format;
		// iterate the format codes
		for(String part : new HashSet<String>(formatData.keySet())) {
			List<Format<String>> data = new ArrayList<Format<String>>(formatData.get(part));
			for(int i=0; i<=data.size(); i++) {
				if(i<data.size()) {
					Format<String> dat = data.get(i);
					if(perm.hasPermission(dat.getPermission())) {
						formatted = formatted.replace(formatKey(part), dat.getData());
						break;
					}
				} else {
					formatted = formatted.replace(formatKey(part), "");
				}
			}
		}
		// name the player
		formatted = formatted.replace(formatKey("name"), name);
		formatted = ChatColor.translateAlternateColorCodes('&', formatted);
		// do we color chat?
		formatted = perm.hasPermission(colorChat)?formatted.replace(formatKey("message"), ChatColor.translateAlternateColorCodes('&', message)) : formatted.replace(formatKey("message"), message);
		return formatted;
	}

}
