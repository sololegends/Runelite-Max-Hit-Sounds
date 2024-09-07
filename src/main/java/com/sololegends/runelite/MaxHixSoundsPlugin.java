package com.sololegends.runelite;

import java.util.*;

import com.google.inject.Inject;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Max Hit Sounds", description = "Add a special sound for when you hit max hit", tags = {
		"max-hit", "max", "hit", "sound", "alert" })
public class MaxHixSoundsPlugin extends Plugin {

	private boolean focused = true;
	private long last_hit_splat = 0l;

	private Set<String> WHITELIST = new HashSet<>();

	@Inject
	Client client;

	@Inject
	MaxHixSoundsPluginConfig config;

	@Override
	protected void startUp() throws Exception {
		log.info("Starting Max Hit Sounds");
		updateWhiteList();
	}

	private void updateWhiteList() {
		WHITELIST.clear();
		String[] items = config.whitelistItemNames().split(",|[\r\n]+");
		for (String item : items) {
			item = item.trim().toLowerCase();
			if (item.length() == 0) {
				continue;
			}
			WHITELIST.add(item);
			// Handle some rewrites that are common shorthands
			if (item.equals("t-bow")) {
				WHITELIST.add("twisted bow");
			} else if (item.equals("fang")) {
				WHITELIST.add("osmumten's fang");
			} else if (item.equals("rbc")) {
				WHITELIST.add("rune crossbow");
			} else if (item.equals("blowpipe")) {
				WHITELIST.add("toxic blowpipe");
			} else if (item.equals("shadow")) {
				WHITELIST.add("tumeken's shadow");
			} else if (item.equals("trident")) {
				WHITELIST.add("trident of the swamp");
				WHITELIST.add("trident of the seas");
			} else if (item.equals("cudgle")) {
				WHITELIST.add("sarachnis cudgle");
			} else if (item.equals("msb") || item.equals("magic shortbow") || item.equals("magic shortbow (i)")) {
				WHITELIST.add("magic shortbow (i)");
				WHITELIST.add("magic shortbow");
			}
		}
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Stopping Max Hit Sounds");
	}

	@Subscribe
	public void onFocusChanged(FocusChanged event) {
		focused = event.isFocused();
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hit_splat) {
		if (config.soundsOnlyWhileFocused() && !focused) {
			return;
		}
		// If whitelist enabled and weapon NOT on it
		if (WHITELIST.size() > 0 && !WHITELIST.contains(getHeldItemName())) {
			return;
		}
		Hitsplat hs = hit_splat.getHitsplat();
		if (hs.isMine() && hs.getHitsplatType() == HitsplatID.DAMAGE_MAX_ME) {
			playSoundPerType();
		}

	}

	private void playSoundPerType() {
		// One per tick with wiggle room for latency
		if (System.currentTimeMillis() - last_hit_splat < 500) {
			return;
		}
		switch (getCurrentWeaponType()) {
			case MAGIC:
				if (config.magicSoundID() != -1) {
					client.playSoundEffect(config.magicSoundID());
				}
				break;
			case MELEE:
				if (config.meleeSoundID() != -1) {
					client.playSoundEffect(config.meleeSoundID());
				}
				break;
			case RANGE:
				if (config.rangedSoundID() != -1) {
					client.playSoundEffect(config.rangedSoundID());
				}
				break;
			case OTHER:
			case FISTS:
			default:
				// No sound here
				return;
		}
		last_hit_splat = System.currentTimeMillis();
	}

	/*
	 * Varbits.EQUIPPED_WEAPON_TYPE values
	 * 1 = Battle Axe
	 * 2 = War Hammer
	 * 3 = Bow
	 * 5 = Crossbow
	 * 9 = Scimitar
	 * 10 = 2H
	 * 13 = Pharos Sceptre
	 * 15 = Spear
	 * 16 = Crush
	 * 17 = Bladed
	 * 18 = Magic Staff / Iban Staff / Castables
	 * 19 = Blowpipe / Darts / Throwing Axes
	 * 20 = Whip
	 * 22 = Blue Moon Staff
	 * 24 = Power Staff
	 * 26 = Halberd
	 * 30 = Keris
	 * 
	 */
	public WeaponType getCurrentWeaponType() {
		int weapon_type_id = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		if (weapon_type_id == 0) {
			return WeaponType.FISTS;
		} else if (weapon_type_id == 3 || weapon_type_id == 5 || weapon_type_id == 19) {
			return WeaponType.RANGE;
		} else if (weapon_type_id == 18 || weapon_type_id == 22 || weapon_type_id == 24) {
			return WeaponType.MAGIC;
		} else if (weapon_type_id == 1 || weapon_type_id == 2 || weapon_type_id == 9 || weapon_type_id == 10
				|| weapon_type_id == 13 || weapon_type_id == 15 || weapon_type_id == 16 || weapon_type_id == 17
				|| weapon_type_id == 20 || weapon_type_id == 26 || weapon_type_id == 30) {
			return WeaponType.MELEE;
		}
		return WeaponType.OTHER;
	}

	public String getHeldItemName() {
		int idx = EquipmentInventorySlot.WEAPON.getSlotIdx();
		Item[] equipment;
		if (client.getItemContainer(InventoryID.EQUIPMENT) != null) {
			equipment = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
			if (equipment != null) {
				if (equipment.length > idx && equipment[idx] != null) {
					return client.getItemDefinition(equipment[idx].getId()).getName().toLowerCase();
				}
			}
		}
		return "";
	}

	public static Item[] getCurrentlyEquipped(Client client) {
		Item[] equipment;
		if (client.getItemContainer(InventoryID.EQUIPMENT) != null) {
			equipment = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
		} else {
			equipment = null;
		}

		return equipment;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		// Update whitelist when whitelist config changed
		if (event.getKey().equals("max-hit-sound-whitelist")) {
			updateWhiteList();
		}
	}

	@Provides
	MaxHixSoundsPluginConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(MaxHixSoundsPluginConfig.class);
	}

	private static enum WeaponType {
		MAGIC, RANGE, MELEE, FISTS, OTHER
	}

}
