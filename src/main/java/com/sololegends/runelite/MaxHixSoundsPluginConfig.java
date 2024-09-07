package com.sololegends.runelite;

import net.runelite.client.config.*;

@ConfigGroup("Max Hit Sounds")
public interface MaxHixSoundsPluginConfig extends Config {

	//@formatter:off

	@ConfigItem(
		keyName = "max-hit-only-focused", 
		name = "Play only when focused", 
		description = "When enabled, sounds only plays when window is focused", 
		position = 0
	)
	default boolean soundsOnlyWhileFocused() {
		return false;
	}

	@ConfigItem(
		keyName = "max-hit-sound-id-mage", 
		name = "Mage Sound ID", 
		description = "ID of a sound to be played for magic (0 for none)", 
		position = 2
	)
	default int magicSoundID() {
		return 0;
	}

	@ConfigItem(
		keyName = "max-hit-sound-id-range", 
		name = "Range Sound ID", 
		description = "[default ruby bolt proc] ID of a sound to be played for ranged hits (0 for none)", 
		position = 3
	)
	default int rangedSoundID() {
		return 2911;
	}

	@ConfigItem(
		keyName = "max-hit-sound-id-melee", 
		name = "Melee Sound ID", 
		description = "ID of a sound to be played for melee hits (0 for none)", 
		position = 4
	)
	default int meleeSoundID() {
		return 0;
	}

	@ConfigItem(
		keyName = "max-hit-sound-whitelist", 
		name = "Whitelist Names", 
		description = "Comma or line separated list of item names. Only names in this list will play max hit sounds, unless blank, then all items.", 
		position = 5
	)
	default String whitelistItemNames() {
		return "";
	}
	@ConfigItem(
		keyName = "max-hit-sound-note", 
		name = "Notes", 
		description = "Sound ID can be found on the OSRS Wiki", 
		position = 10
	)
	default String maxHitSoundNotes() {
		return "Sound ID can be found on the OSRS Wiki";
	}

}
