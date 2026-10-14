package com.npcstattracker;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.client.util.Text;

/**
 * Base combat stats for an NPC whose stats this plugin tracks.
 *
 * <p>The named constants are the hand-tuned bosses inherited from Better Party Defence: their
 * stats come from the OSRS Wiki DPS calculator (github.com/weirdgloop/osrs-dps-calc), they can
 * carry raid/phase flags, and they are tracked as one logical encounter that survives NPC index
 * and form changes. Every other attackable NPC is a {@link Flag#GENERIC} entry built at runtime
 * from {@link NpcStatTable}; generic NPCs are tracked strictly per NPC actor.
 *
 * <p>A monster's magic-defence roll is {@code (9 + Magic level) * (Magic-def bonus + 64)}.
 * A handful of monsters ({@link Flag#MAGIC_USES_DEFENCE}) roll magic defence off their
 * Defence level instead of their Magic level, so draining their Defence also
 * lowers their magic defence.
 */
@Getter
public final class BossDefence
{
	/** Known bosses by key, in declaration order. Must be initialised before the constants. */
	private static final Map<String, BossDefence> KNOWN = new LinkedHashMap<>();

	// key, name, Defence, Magic, Attack, Strength, magic-def bonus, Defence floor, Ranged, Hitpoints, flags
	public static final BossDefence ABYSSAL_PORTAL = new BossDefence("ABYSSAL_PORTAL", "Abyssal portal", 176, 176, 1, 1, 60, 0, 1, 250, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE);
	// The Sire's magic-def bonus drops to -40 once it's stunned in phase 3; 20 covers the rest.
	public static final BossDefence ABYSSAL_SIRE = new BossDefence("ABYSSAL_SIRE", "Abyssal Sire", 250, 200, 180, 136, 20, 0, 1, 425, Flag.DEMON);
	public static final BossDefence AKKHA = new BossDefence("AKKHA", "Akkha", 80, 100, 100, 140, 10, 70, 100, 400);
	public static final BossDefence AKKHAS_SHADOW = new BossDefence("AKKHAS_SHADOW", "Akkha's Shadow", 30, 100, 100, 140, 10, 0, 100, 70, Flag.PER_ACTOR);
	public static final BossDefence ALCHEMICAL_HYDRA = new BossDefence("ALCHEMICAL_HYDRA", "Alchemical Hydra", 100, 260, 100, 100, 150, 0, 260, 1100);
	public static final BossDefence ARAXXOR = new BossDefence("ARAXXOR", "Araxxor", 135, 190, 320, 320, 237, 90, 210, 1020);
	public static final BossDefence BRANDA_THE_FIRE_QUEEN = new BossDefence("BRANDA_THE_FIRE_QUEEN", "Branda the Fire Queen", 100, 100, 300, 250, 700, 0, 150, 600);
	public static final BossDefence ARTIO = new BossDefence("ARTIO", "Artio", 150, 90, 250, 270, 0, 0, 120, 450);
	public static final BossDefence BA_BA = new BossDefence("BA_BA", "Ba-Ba", 80, 100, 150, 160, 280, 60, 0, 380);
	public static final BossDefence CALLISTO = new BossDefence("CALLISTO", "Callisto", 225, 140, 350, 300, 0, 0, 200, 1000);
	public static final BossDefence CALVARION = new BossDefence("CALVARION", "Calvar'ion", 225, 178, 250, 250, 198, 0, 1, 150);
	public static final BossDefence CERBERUS = new BossDefence("CERBERUS", "Cerberus", 100, 220, 220, 220, 65, 0, 220, 600);
	public static final BossDefence CHAOS_ELEMENTAL = new BossDefence("CHAOS_ELEMENTAL", "Chaos Elemental", 270, 270, 270, 270, 70, 0, 270, 250);
	public static final BossDefence COMMANDER_ZILYANA = new BossDefence("COMMANDER_ZILYANA", "Commander Zilyana", 300, 300, 280, 196, 100, 0, 250, 255);
	public static final BossDefence CORPOREAL_BEAST = new BossDefence("CORPOREAL_BEAST", "Corporeal Beast", 310, 350, 320, 320, 150, 0, 150, 2000);
	public static final BossDefence DAGANNOTH_PRIME = new BossDefence("DAGANNOTH_PRIME", "Dagannoth Prime", 255, 255, 255, 255, 255, 0, 0, 255);
	public static final BossDefence DAGANNOTH_REX = new BossDefence("DAGANNOTH_REX", "Dagannoth Rex", 255, 0, 255, 255, 10, 0, 255, 255);
	public static final BossDefence DAGANNOTH_SUPREME = new BossDefence("DAGANNOTH_SUPREME", "Dagannoth Supreme", 128, 255, 255, 255, 255, 0, 255, 255);
	public static final BossDefence DEATHLY_MAGE = new BossDefence("DEATHLY_MAGE", "Deathly mage", 155, 210, 1, 1, 0, 0, 1, 120, Flag.COX_SCALED, Flag.PER_ACTOR);
	public static final BossDefence DEATHLY_RANGER = new BossDefence("DEATHLY_RANGER", "Deathly ranger", 155, 155, 1, 1, 0, 0, 210, 120, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE, Flag.PER_ACTOR);
	// Wardens phase 3 only. Phase-2 Warden actors are filtered by NPC id in matchingNpc().
	public static final BossDefence ELIDINIS_WARDEN = new BossDefence("ELIDINIS_WARDEN", "Elidinis' Warden", 150, 150, 150, 150, 20, 120, 150, 880);
	public static final BossDefence ELDRIC_THE_ICE_KING = new BossDefence("ELDRIC_THE_ICE_KING", "Eldric the Ice King", 100, 100, 300, 250, 700, 0, 150, 600);
	public static final BossDefence GENERAL_GRAARDOR = new BossDefence("GENERAL_GRAARDOR", "General Graardor", 250, 80, 280, 350, 298, 0, 350, 255);
	public static final BossDefence GIANT_MOLE = new BossDefence("GIANT_MOLE", "Giant Mole", 200, 200, 200, 200, 80, 0, 1, 200);
	public static final BossDefence GREAT_OLM = new BossDefence("GREAT_OLM", "Great Olm", 150, 250, 250, 250, 200, 0, 250, 800, Flag.COX_SCALED);
	public static final BossDefence GREAT_OLM_LEFT_CLAW = new BossDefence("GREAT_OLM_LEFT_CLAW", "Great Olm (Left claw)", 175, 175, 250, 250, 200, 0, 250, 600, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE);
	public static final BossDefence GREAT_OLM_RIGHT_CLAW = new BossDefence("GREAT_OLM_RIGHT_CLAW", "Great Olm (Right claw)", 175, 87, 250, 250, 50, 0, 250, 600, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE);
	public static final BossDefence ICE_DEMON = new BossDefence("ICE_DEMON", "Ice Demon", 160, 390, 1, 1, 40, 0, 390, 140, Flag.COX_SCALED, Flag.MAGIC_USES_DEFENCE);
	// The Queen's magic-def bonus is 10 while airborne; 100 is the crawling (meleeable) form.
	public static final BossDefence KALPHITE_QUEEN = new BossDefence("KALPHITE_QUEEN", "Kalphite Queen", 300, 150, 300, 300, 100, 0, 1, 255);
	public static final BossDefence KEPHRI = new BossDefence("KEPHRI", "Kephri", 80, 125, 0, 0, 200, 60, 0, 80);
	public static final BossDefence KING_BLACK_DRAGON = new BossDefence("KING_BLACK_DRAGON", "King Black Dragon", 240, 240, 240, 240, 80, 0, 1, 240);
	public static final BossDefence KREE_ARRA = new BossDefence("KREE_ARRA", "Kree'arra", 260, 200, 300, 200, 200, 0, 380, 255);
	public static final BossDefence KRIL_TSUTSAROTH = new BossDefence("KRIL_TSUTSAROTH", "K'ril Tsutsaroth", 270, 200, 340, 300, 80, 0, 1, 255, Flag.DEMON);
	public static final BossDefence LIZARDMAN_SHAMAN = new BossDefence("LIZARDMAN_SHAMAN", "Lizardman shaman", 140, 130, 120, 120, 50, 0, 120, 150, Flag.COX_SCALED, Flag.PER_ACTOR);
	public static final BossDefence NEX = new BossDefence("NEX", "Nex", 260, 230, 315, 200, 300, 250, 350, 3400);
	public static final BossDefence NYLOCAS_VASILIAS = new BossDefence("NYLOCAS_VASILIAS", "Nylocas Vasilias", 50, 50, 400, 350, 0, 0, 140, 1440);
	public static final BossDefence OBELISK = new BossDefence("OBELISK", "<col=00ffff>Obelisk</col>", 100, 100, 200, 150, 50, 60, 100, 260);
	public static final BossDefence PESTILENT_BLOAT = new BossDefence("PESTILENT_BLOAT", "Pestilent Bloat", 100, 150, 250, 340, 600, 0, 180, 2400);
	// Muspah's magic-def bonus is phase-dependent (34 melee, 437 most phases); using the common 437.
	public static final BossDefence PHANTOM_MUSPAH = new BossDefence("PHANTOM_MUSPAH", "Phantom Muspah", 200, 150, 280, 280, 437, 0, 280, 850);
	public static final BossDefence PHOSANIS_NIGHTMARE = new BossDefence("PHOSANIS_NIGHTMARE", "Phosani's Nightmare", 150, 150, 150, 150, 600, 120, 150, 3200);
	public static final BossDefence SARACHNIS = new BossDefence("SARACHNIS", "Sarachnis", 150, 150, 200, 240, 150, 0, 300, 400);
	public static final BossDefence SCORPIA = new BossDefence("SCORPIA", "Scorpia", 180, 1, 250, 150, 44, 0, 1, 200);
	public static final BossDefence SKELETAL_MYSTIC = new BossDefence("SKELETAL_MYSTIC", "Skeletal Mystic", 187, 140, 140, 140, 140, 0, 1, 160, Flag.COX_SCALED, Flag.PER_ACTOR);
	public static final BossDefence SKOTIZO = new BossDefence("SKOTIZO", "Skotizo", 200, 280, 240, 250, 80, 0, 1, 450);
	public static final BossDefence SOTETSEG = new BossDefence("SOTETSEG", "Sotetseg", 200, 250, 250, 250, 30, 100, 250, 4000);
	public static final BossDefence SPINDEL = new BossDefence("SPINDEL", "Spindel", 225, 235, 200, 130, 205, 0, 286, 515);
	public static final BossDefence TEKTON = new BossDefence("TEKTON", "Tekton", 205, 205, 390, 390, 0, 0, 1, 300, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE, Flag.COX_CM_SMALL_DEFENCE_BUMP);
	public static final BossDefence TEKTON_ENRAGED = new BossDefence("TEKTON_ENRAGED", "Tekton (enraged)", 205, 205, 390, 390, 0, 0, 1, 300, Flag.COX_SCALED, Flag.COX_MAGIC_IS_DEFENSIVE, Flag.COX_CM_SMALL_DEFENCE_BUMP);
	public static final BossDefence THE_HUEYCOATL = new BossDefence("THE_HUEYCOATL", "The Hueycoatl", 125, 50, 150, 50, 200, 120, 50, 250);
	public static final BossDefence THE_MAIDEN_OF_SUGADINTI = new BossDefence("THE_MAIDEN_OF_SUGADINTI", "The Maiden of Sugadinti", 200, 350, 350, 350, 0, 0, 350, 1050);
	public static final BossDefence THE_NIGHTMARE = new BossDefence("THE_NIGHTMARE", "The Nightmare", 150, 150, 150, 150, 600, 120, 150, 2400);
	public static final BossDefence TUMEKENS_WARDEN = new BossDefence("TUMEKENS_WARDEN", "Tumeken's Warden", 150, 150, 150, 150, 20, 120, 150, 880);
	public static final BossDefence TZKAL_ZUK = new BossDefence("TZKAL_ZUK", "TzKal-Zuk", 260, 150, 350, 600, 350, 0, 400, 1200);
	public static final BossDefence TZTOK_JAD = new BossDefence("TZTOK_JAD", "TzTok-Jad", 480, 480, 640, 960, 0, 0, 960, 250);
	public static final BossDefence VASA = new BossDefence("VASA", "Vasa Nistirio", 175, 230, 1, 1, 400, 0, 230, 300, Flag.COX_SCALED);
	public static final BossDefence VENENATIS = new BossDefence("VENENATIS", "Venenatis", 321, 300, 300, 200, 300, 0, 350, 850);
	public static final BossDefence VETION = new BossDefence("VETION", "Vet'ion", 395, 300, 430, 430, 250, 0, 1, 255);
	public static final BossDefence VORKATH = new BossDefence("VORKATH", "Vorkath", 214, 150, 560, 308, 240, 0, 308, 750);
	public static final BossDefence XARPUS = new BossDefence("XARPUS", "Xarpus", 250, 220, 1, 1, 0, 0, 100, 5000);
	// Yama is stance-dependent: -30 Magic Defence normally, +60 after his primary target
	// attacks with magic in phases 1/2, and +81 in phase 3. DefenceTracker applies those
	// live stance changes while preserving Eye of ayak drain across them.
	public static final BossDefence YAMA = new BossDefence("YAMA", "Yama", 225, 250, 320, 350, -30, 145, 210, 2500, Flag.DEMON);
	public static final BossDefence ZEBAK = new BossDefence("ZEBAK", "Zebak", 70, 100, 250, 140, 200, 50, 120, 580);
	// Zulrah's magic-def bonus is form-dependent (-45 serpentine, 0 magma, 300 tanzanite); using 0.
	// A single-value entry can't follow the rotation, so the Eye of ayak drain won't read exactly per form.
	public static final BossDefence ZULRAH = new BossDefence("ZULRAH", "Zulrah", 300, 300, 1, 1, 0, 0, 300, 500);


	public enum Flag
	{
		/** The monster rolls magic defence off its Defence level rather than its Magic level. */
		MAGIC_USES_DEFENCE,
		/** In CoX the monster's Magic level scales with the defensive party-size factor, not the offensive one. */
		COX_MAGIC_IS_DEFENSIVE,
		/** A Chambers of Xeric monster, so its levels take the party-size and Challenge Mode scaling. */
		COX_SCALED,
		/** Takes a smaller Challenge Mode defensive bump than the rest of CoX (Tekton). */
		COX_CM_SMALL_DEFENCE_BUMP,
		/** A demon, so the arclight family drains it harder. */
		DEMON,
		/** Any non-boss NPC from the stat table: tracked per actor, never re-bound or party-synced. */
		GENERIC,
		/** Special attacks do not lower this NPC's stats (e.g. Awakened Desert Treasure II bosses). */
		DRAIN_IMMUNE,
		/**
		 * Several identical copies fight at once (CoX Skeletal Mystics, Lizardman shamans, Deathly
		 * mages/rangers, ToA Akkha's Shadows): tracked per actor like generic NPCs, never as one
		 * logical encounter and never party state-synced.
		 */
		PER_ACTOR
	}

	private final String key;
	private final String npcName;
	private final int baseDef;
	private final int baseMagic;
	private final int baseAtk;
	private final int baseStr;
	private final int baseMagicDef;
	private final int minDef;
	private final int baseRanged;
	private final int baseHp;
	private final Set<Flag> flags;

	private BossDefence(String key, String npcName, int baseDef, int baseMagic, int baseAtk, int baseStr,
		int baseMagicDef, int minDef, int baseRanged, int baseHp, Flag... flags)
	{
		this.key = key;
		this.npcName = npcName;
		this.baseDef = baseDef;
		this.baseMagic = baseMagic;
		this.baseAtk = baseAtk;
		this.baseStr = baseStr;
		this.baseMagicDef = baseMagicDef;
		this.minDef = minDef;
		this.baseRanged = baseRanged;
		this.baseHp = baseHp;
		this.flags = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
		if (!this.flags.contains(Flag.GENERIC))
		{
			KNOWN.put(key, this);
		}
	}

	/** A generic NPC entry; only {@link NpcStatTable} creates these, one per (NPC id, combat level). */
	static BossDefence generic(int npcId, int combatLevel, String npcName, int def, int magic, int atk, int str,
		int magicDef, int ranged, int hp, boolean demon, boolean drainImmune)
	{
		EnumSet<Flag> flags = EnumSet.of(Flag.GENERIC);
		if (demon)
		{
			flags.add(Flag.DEMON);
		}
		if (drainImmune)
		{
			flags.add(Flag.DRAIN_IMMUNE);
		}
		return new BossDefence(genericKey(npcId, combatLevel), npcName, def, magic, atk, str, magicDef, 0, ranged, hp,
			flags.toArray(new Flag[0]));
	}

	static String genericKey(int npcId, int combatLevel)
	{
		return "NPC_" + npcId + "_" + combatLevel;
	}

	public boolean has(Flag flag)
	{
		return flags.contains(flag);
	}

	public boolean isGeneric()
	{
		return flags.contains(Flag.GENERIC);
	}

	/**
	 * Tracked strictly per NPC actor: generic NPCs and multi-copy raid NPCs. Their state never moves
	 * to another NPC of the same type, ends when the actor despawns, and is not party state-synced.
	 */
	public boolean isPerActor()
	{
		return flags.contains(Flag.GENERIC) || flags.contains(Flag.PER_ACTOR);
	}

	/** Stable identifier used in party messages (the boss constant name, or NPC_&lt;id&gt;). */
	public String name()
	{
		return key;
	}

	@Override
	public String toString()
	{
		return key;
	}

	/** The hand-tuned bosses only (never generic NPCs). */
	public static BossDefence[] values()
	{
		return KNOWN.values().toArray(new BossDefence[0]);
	}

	public static BossDefence valueOf(String key)
	{
		BossDefence boss = KNOWN.get(key);
		if (boss == null && key != null && key.startsWith("NPC_"))
		{
			String[] parts = key.split("_");
			int id = Integer.parseInt(parts[1]);
			boss = parts.length > 2 ? NpcStatTable.lookup(id, Integer.parseInt(parts[2]), null) : NpcStatTable.byId(id);
		}
		if (boss == null)
		{
			throw new IllegalArgumentException("Unknown NPC stats key " + key);
		}
		return boss;
	}

	/**
	 * Match a concrete NPC actor: a hand-tuned boss first, otherwise any NPC in the stat table.
	 * Wardens reuse the same visible names in phases 2 and 3, but only the phase-3 actors use the
	 * drainable Defence model, so other Warden actors are deliberately not tracked at all.
	 */
	public static BossDefence matchingNpc(NPC npc)
	{
		return npc == null ? null : matchingNpc(npc.getId(), npc.getCombatLevel(), npc.getName());
	}

	/** Match without a combat level: an NPC id with several versions resolves to its default one. */
	public static BossDefence matchingNpc(int npcId, String name)
	{
		return matchingNpc(npcId, -1, name);
	}

	/**
	 * Some NPC ids share one id between versions with different stats (e.g. Awakened Desert Treasure II
	 * bosses, Doom of Mokhaiotl delve levels); the combat level tells them apart.
	 */
	public static BossDefence matchingNpc(int npcId, int combatLevel, String name)
	{
		BossDefence boss = matchingNpcName(name);
		if (boss == ELIDINIS_WARDEN)
		{
			return npcId == 11761 || npcId == 11763 ? boss : null;
		}
		if (boss == TUMEKENS_WARDEN)
		{
			return npcId == 11762 || npcId == 11764 ? boss : null;
		}
		if (boss != null)
		{
			return boss;
		}
		return NpcStatTable.lookup(npcId, combatLevel, name);
	}

	/**
	 * The hand-tuned boss for an in-game NPC name. Exact first, then a word-boundary prefix so a
	 * phase suffix the table doesn't list still matches its base entry (e.g. "Vet'ion Reborn").
	 * Never returns a generic NPC.
	 */
	public static BossDefence matchingNpcName(String name)
	{
		if (name == null || name.trim().isEmpty())
		{
			return null;
		}
		String trimmed = Text.removeTags(name).trim();
		for (BossDefence boss : KNOWN.values())
		{
			String candidate = Text.removeTags(boss.npcName).trim();
			if (candidate.equalsIgnoreCase(trimmed))
			{
				return boss;
			}
		}
		for (BossDefence boss : KNOWN.values())
		{
			String candidate = Text.removeTags(boss.npcName).trim();
			if (trimmed.regionMatches(true, 0, candidate, 0, candidate.length())
				&& trimmed.length() > candidate.length()
				&& trimmed.charAt(candidate.length()) == ' ')
			{
				return boss;
			}
		}
		return null;
	}
}
