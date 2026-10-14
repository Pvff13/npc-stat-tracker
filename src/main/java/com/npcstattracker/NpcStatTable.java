package com.npcstattracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.Text;

/**
 * Base combat stats for every attackable NPC, from {@code npc_stats.tsv} (generated from the OSRS
 * Wiki DPS calculator's monster data). Loaded lazily on first lookup.
 *
 * <p>Lookups go by NPC id and combat level first: some ids are shared by versions with different
 * stats (Awakened Desert Treasure II bosses, Doom of Mokhaiotl delves) whose combat levels differ.
 * Then by id alone (the generator puts the normal version first), then by name for ids missing from
 * the data, which can pick the wrong version when one name has several stat blocks.</p>
 */
@Slf4j
final class NpcStatTable
{
	private static final String RESOURCE = "npc_stats.tsv";

	private static Map<Integer, BossDefence> byId;
	private static Map<Long, BossDefence> byIdAndLevel;
	private static Map<String, BossDefence> byName;

	private NpcStatTable()
	{
	}

	static synchronized BossDefence byId(int npcId)
	{
		load();
		return byId.get(npcId);
	}

	static synchronized BossDefence lookup(int npcId, int combatLevel, String name)
	{
		load();
		BossDefence stats = byIdAndLevel.get(idAndLevel(npcId, combatLevel));
		if (stats == null)
		{
			stats = byId.get(npcId);
		}
		if (stats == null && name != null)
		{
			stats = byName.get(normalize(name));
		}
		return stats;
	}

	private static long idAndLevel(int npcId, int combatLevel)
	{
		return ((long) npcId << 32) | (combatLevel & 0xffffffffL);
	}

	private static String normalize(String name)
	{
		return Text.removeTags(name).replace(' ', ' ').trim().toLowerCase(Locale.ROOT);
	}

	private static void load()
	{
		if (byId != null)
		{
			return;
		}
		byId = new HashMap<>();
		byIdAndLevel = new HashMap<>();
		byName = new HashMap<>();
		try (InputStream in = NpcStatTable.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				log.warn("NPC stat table {} is missing", RESOURCE);
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				// id, name, version, level, atk, str, def, magic, ranged, hp, magicDefBonus, demon, drainImmune
				String[] f = line.split("\t", -1);
				if (f.length < 13)
				{
					continue;
				}
				int id = Integer.parseInt(f[0]);
				int level = Integer.parseInt(f[3]);
				BossDefence stats = BossDefence.generic(id, level, f[1],
					Integer.parseInt(f[6]), Integer.parseInt(f[7]), Integer.parseInt(f[4]), Integer.parseInt(f[5]),
					Integer.parseInt(f[10]), Integer.parseInt(f[8]), Integer.parseInt(f[9]),
					"1".equals(f[11]), "1".equals(f[12]));
				byIdAndLevel.putIfAbsent(idAndLevel(id, level), stats);
				byId.putIfAbsent(id, stats);
				byName.putIfAbsent(normalize(f[1]), stats);
			}
		}
		catch (IOException | RuntimeException ex)
		{
			log.warn("Failed to load NPC stat table", ex);
		}
	}
}
