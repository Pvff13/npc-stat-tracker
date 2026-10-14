package com.npcstattracker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

/**
 * Per-NPC natural regeneration intervals, in game ticks. Every interval the NPC regains one
 * level of each drained stat (Defence, Attack, Strength, Magic) and one hitpoint.
 *
 * <p>Data from the OSRS wiki's measured regen table (User:Orion/Regen, via Monster Examine).
 * NPCs that are not listed use the game's default of 100 ticks. {@link #NONE} means the NPC
 * does not regenerate that stat at all (the wiki's "N/A"); intervals of 10000 ticks, which Jagex
 * uses to switch regen off, are stored as {@link #NONE} too.</p>
 */
final class NpcRegen
{
	static final int NONE = -1;
	static final int DEFAULT_INTERVAL = 100;
	private static final int NEVER = 10000;

	/** Sleeping Vorkath regenerates every 100 ticks; the awake NPC every 5 ticks. */
	private static final int VORKATH_SLEEPING_INTERVAL = 100;
	private static final int VORKATH_AWAKE_INTERVAL = 5;

	/** Lower-case NPC name -> {hp interval, stat interval}. */
	private static final Map<String, int[]> RATES = new HashMap<>();

	static
	{
		// Slayer / general monsters.
		put(100, 100, "hill giant", "mutated bloodveld", "warped jelly", "cyclops", "ankou",
			"king sand crab", "dust devil", "moss giant", "abyssal demon", "magic axe", "possessed pickaxe",
			"fire giant", "skeleton", "crawling hand", "bloodveld", "infernal mage", "rockslug",
			"cockatrice", "pyrefiend", "turoth");
		put(5, 5, "greater nechryael", "deviant spectre", "twisted banshee", "aberrant spectre", "banshee",
			"nechryael", "gargoyle", "cave crawler", "cave bug", "kurask");
		put(20, 20, "greater demon", "lesser demon", "dagannoth", "black demon", "iron dragon", "bronze dragon",
			"steel dragon", "brutal red dragon", "brutal blue dragon", "brutal black dragon", "green dragon",
			"blue dragon", "red dragon", "black dragon");
		put(50, 50, "ghost", "shade", "hellhound", "mountain troll");
		put(10, 10, "skeletal wyvern");

		// Bosses.
		put(10, 10, "general graardor", "commander zilyana");
		put(NONE, 50, "nex");
		put(NONE, 100, "kephri", "zebak", "ba-ba", "phantom muspah", "vardorvis");
		put(NONE, 20, "akkha");
		put(100, 100, "kalphite queen", "alchemical hydra", "giant mole");
		put(20, 20, "cerberus");
		put(5, 5, "zulrah");

		// Chambers of Xeric (confirmed by Mod Ash; anything else in CoX uses the default 100).
		put(15, 15, "tekton", "tekton (enraged)", "muttadile");
		put(100, 100, "ice demon", "great olm", "great olm (left claw)", "great olm (right claw)",
			"great olm left claw", "great olm right claw", "vanguard");
		put(20, 20, "lizardman shaman");
		put(8, 8, "guardian");
		put(25, 25, "skeletal mystic");
		put(10, 10, "vasa nistirio", "vasa", "vespula");
		put(9, 9, "glowing crystal");
		put(1, 1, "jewelled crab");
		put(45, 45, "abyssal portal");
		put(NEVER, NEVER, "lux grub");

		// Theatre of Blood.
		put(NONE, 5, "pestilent bloat");
		put(100, 100, "the maiden of sugadinti", "nylocas vasilias", "sotetseg", "xarpus");
	}

	private NpcRegen()
	{
	}

	private static void put(int hp, int stat, String... names)
	{
		int[] rate = {hp >= NEVER ? NONE : hp, stat >= NEVER ? NONE : stat};
		for (String name : names)
		{
			RATES.put(name, rate);
		}
	}

	/** Ticks between natural stat (non-HP) regeneration, or {@link #NONE}. */
	static int statInterval(String name, int npcId)
	{
		if (isVorkath(name, npcId))
		{
			return isSleepingVorkath(npcId) ? VORKATH_SLEEPING_INTERVAL : VORKATH_AWAKE_INTERVAL;
		}
		int[] rate = RATES.get(normalize(name));
		return rate == null ? DEFAULT_INTERVAL : rate[1];
	}

	/** Ticks between natural hitpoint regeneration, or {@link #NONE}. */
	static int hpInterval(String name, int npcId)
	{
		if (isVorkath(name, npcId))
		{
			return isSleepingVorkath(npcId) ? VORKATH_SLEEPING_INTERVAL : VORKATH_AWAKE_INTERVAL;
		}
		int[] rate = RATES.get(normalize(name));
		return rate == null ? DEFAULT_INTERVAL : rate[0];
	}

	/** Whether the wiki table actually measured this NPC (otherwise the default is assumed). */
	static boolean isMeasured(String name, int npcId)
	{
		return isVorkath(name, npcId) || RATES.containsKey(normalize(name));
	}

	private static boolean isVorkath(String name, int npcId)
	{
		return "vorkath".equals(normalize(name)) || isSleepingVorkath(npcId) || npcId == NpcID.VORKATH;
	}

	private static boolean isSleepingVorkath(int npcId)
	{
		return npcId == NpcID.VORKATH_SLEEPING || npcId == NpcID.VORKATH_SLEEPING_NOOP;
	}

	private static String normalize(String name)
	{
		return name == null ? "" : Text.removeTags(name).replace(' ', ' ').trim().toLowerCase(Locale.ROOT);
	}
}
