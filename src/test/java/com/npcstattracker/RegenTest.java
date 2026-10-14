package com.npcstattracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;
import org.junit.Before;
import org.junit.Test;

/** Natural NPC regeneration, per the OSRS wiki regen table (User:Orion/Regen). */
public class RegenTest
{
	private static final int WORLD = 1;

	private Client client;
	private NpcStatTrackerConfig config;
	private IndexedObjectSet<? extends NPC> npcs;
	private List<NPC> sceneNpcs;
	private int tick;
	private DefenceTracker tracker;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp()
	{
		client = mock(Client.class);
		config = mock(NpcStatTrackerConfig.class);
		WorldView worldView = mock(WorldView.class);
		npcs = mock(IndexedObjectSet.class);
		sceneNpcs = new ArrayList<>();

		when(client.getWorld()).thenReturn(WORLD);
		when(client.getTopLevelWorldView()).thenReturn(worldView);
		org.mockito.Mockito.doReturn(npcs).when(worldView).npcs();
		when(npcs.iterator()).thenAnswer(invocation -> sceneNpcs.iterator());
		when(client.getVarbitValue(VarbitID.RAIDS_CLIENT_INDUNGEON)).thenReturn(0);
		when(client.getVarbitValue(VarbitID.RAIDS_CLIENT_PARTYSIZE_SCALED)).thenReturn(1);
		when(client.getTickCount()).thenAnswer(invocation -> tick);
		tracker = new DefenceTracker(client, config);
	}

	private NPC fakeNpc(String name, int index, int id)
	{
		NPC npc = mock(NPC.class);
		when(npc.getName()).thenReturn(name);
		when(npc.getIndex()).thenReturn(index);
		when(npc.getId()).thenReturn(id);
		when(npc.getHealthRatio()).thenReturn(1);
		when(npcs.byIndex(index)).thenReturn(npc);
		sceneNpcs.add(npc);
		return npc;
	}

	/** Same order as the plugin: regen first, then this tick's queued drains. */
	private void runUntil(int lastTick)
	{
		while (tick < lastTick)
		{
			tick++;
			tracker.applyRegen();
			tracker.onGameTick();
		}
	}

	private long defence()
	{
		return tracker.state().getCurrent();
	}

	@Test
	public void defaultNpcRegainsOneDefencePer100TicksFromSpawn()
	{
		NPC mole = fakeNpc("Giant Mole", 5, NpcID.MOLE_GIANT);
		tracker.onNpcSpawned(mole);
		runUntil(10);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 5, 1, WORLD, "Me");
		runUntil(11);
		long drained = defence();
		assertTrue(drained < BossDefence.GIANT_MOLE.getBaseDef());
		assertFalse(tracker.state().isRegenEstimated());

		runUntil(99);
		assertEquals(drained, defence());
		assertEquals(1, tracker.state().getTicksToStatRegen());
		runUntil(100);
		assertEquals(drained + 1, defence());
		runUntil(200);
		assertEquals(drained + 2, defence());
		assertEquals(100, tracker.state().getTicksToStatRegen());
	}

	@Test
	public void regenNeverExceedsStartingDefence()
	{
		NPC mole = fakeNpc("Giant Mole", 5, NpcID.MOLE_GIANT);
		tracker.onNpcSpawned(mole);
		tracker.queue(SpecialWeapon.ARCLIGHT, 5, 1, WORLD, "Me");
		runUntil(1);
		long drained = defence();
		long base = BossDefence.GIANT_MOLE.getBaseDef();
		runUntil((int) (base - drained + 5) * 100);
		assertEquals(base, defence());
	}

	@Test
	public void vorkathUsesSleepingTimerOnceThenFiveTicks()
	{
		NPC vorkath = fakeNpc("Vorkath", 3, NpcID.VORKATH_SLEEPING);
		tracker.onNpcSpawned(vorkath);
		runUntil(20);
		when(vorkath.getId()).thenReturn(NpcID.VORKATH);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 3, 1, WORLD, "Me");
		runUntil(30);
		long drained = defence();

		runUntil(99);
		assertEquals("sleeping 100-tick timer still running", drained, defence());
		runUntil(100);
		assertEquals(drained + 1, defence());
		runUntil(105);
		assertEquals(drained + 2, defence());
		runUntil(110);
		assertEquals(drained + 3, defence());
	}

	@Test
	public void unseenSpawnIsEstimatedFromFirstTrackedTick()
	{
		tick = 500;
		fakeNpc("Giant Mole", 5, NpcID.MOLE_GIANT);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 5, 1, WORLD, "Me");
		runUntil(501);
		runUntil(502);
		assertTrue(tracker.state().isRegenEstimated());
		assertEquals(100, tracker.state().getTicksToStatRegen());
	}

	@Test
	public void nexRegensStatsEvery50TicksButNeverHp()
	{
		NPC nex = fakeNpc("Nex", 9, NpcID.NEX);
		tracker.onNpcSpawned(nex);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 9, 1, WORLD, "Me");
		runUntil(1);
		long drained = defence();
		runUntil(50);
		assertEquals(drained + 1, defence());
		assertEquals(-1, tracker.state().getTicksToHpRegen());
		assertEquals(NpcRegen.NONE, tracker.state().getHpRegenInterval());
		assertEquals(50, tracker.state().getStatRegenInterval());
	}

	@Test
	public void vasaFullyRegeneratesAfterLeavingCrystal()
	{
		NPC vasa = fakeNpc("Vasa Nistirio", 4, NpcID.RAIDS_VASANISTIRIO_WALKING);
		tracker.onNpcSpawned(vasa);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 4, 1, WORLD, "Me");
		runUntil(1);
		long base = tracker.state().getBase();
		assertTrue(defence() < base);

		when(vasa.getId()).thenReturn(NpcID.RAIDS_VASANISTIRIO_HEALING);
		runUntil(3);
		assertTrue(defence() < base);
		when(vasa.getId()).thenReturn(NpcID.RAIDS_VASANISTIRIO_WALKING);
		runUntil(4);
		assertEquals(base, defence());
	}

	@Test
	public void greaterNechryaelIsTrackedAndRegensEvery5Ticks()
	{
		BossDefence stats = BossDefence.matchingNpc(7278, "Greater Nechryael");
		assertTrue(stats.isGeneric());
		assertTrue(stats.has(BossDefence.Flag.DEMON));
		assertEquals(85, stats.getBaseDef());
		assertEquals(205, stats.getBaseHp());
		NPC nech = fakeNpc("Greater Nechryael", 6, 7278);
		tracker.onNpcSpawned(nech);
		tracker.queue(SpecialWeapon.ARCLIGHT, 6, 1, WORLD, "Me");
		runUntil(1);
		long drained = defence();
		assertTrue(drained < 85);
		runUntil(5);
		assertEquals(drained + 1, defence());
	}

	@Test
	public void anyNpcInTheStatTableIsTrackedWithAllCombatStats()
	{
		// Unknown NPC id falls back to the name.
		BossDefence stats = BossDefence.matchingNpc(-12345, "Abyssal demon");
		assertTrue(stats != null && stats.isGeneric());
		assertEquals(stats, BossDefence.valueOf(stats.name()));
		assertEquals(null, BossDefence.matchingNpc(-1, "Banker"));

		NPC demon = fakeNpc("Abyssal demon", 7, -12345);
		when(demon.getHealthScale()).thenReturn(30);
		when(demon.getHealthRatio()).thenReturn(15);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 7, 1, WORLD, "Me");
		runUntil(1);
		DefenceTracker.DefenceState state = tracker.state();
		assertTrue(state.getCurrent() < state.getBase());
		assertEquals(stats.getBaseAtk(), state.getAttackBase());
		assertEquals(stats.getBaseStr(), state.getStrengthBase());
		assertEquals(stats.getBaseRanged(), state.getRangedBase());
		assertEquals(stats.getBaseHp(), state.getHitpointsMax());
		assertEquals(50, state.getHitpointsPercent());
		assertTrue(state.getHitpoints() > 0 && state.getHitpoints() < stats.getBaseHp());
	}

	@Test
	public void genericNpcStateStaysOnItsOwnActorAndEndsOnDespawn()
	{
		NPC first = fakeNpc("Greater Nechryael", 6, 7278);
		NPC second = fakeNpc("Greater Nechryael", 8, 7278);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 6, 1, WORLD, "Me");
		runUntil(1);
		assertEquals(6, tracker.state().getNpcIndex());

		// The drained copy leaves; its state must not move onto the identical second monster.
		sceneNpcs.remove(first);
		when(npcs.byIndex(6)).thenReturn(null);
		tracker.onNpcDespawned(first);
		runUntil(2);
		for (DefenceTracker.DefenceState state : tracker.states())
		{
			assertFalse(state.getNpcIndex() == second.getIndex() && state.isDrained());
		}
		assertEquals(null, tracker.state());
	}

	@Test
	public void sharedNpcIdsAreResolvedByCombatLevel()
	{
		// Duke Sucellus: one NPC id, post-quest level 758 vs Awakened level 1099.
		BossDefence normal = BossDefence.matchingNpc(12191, 758, "Duke Sucellus");
		BossDefence awakened = BossDefence.matchingNpc(12191, 1099, "Duke Sucellus");
		assertEquals(275, normal.getBaseDef());
		assertEquals(485, normal.getBaseHp());
		assertFalse(normal.has(BossDefence.Flag.DRAIN_IMMUNE));
		assertEquals(316, awakened.getBaseDef());
		assertTrue(awakened.has(BossDefence.Flag.DRAIN_IMMUNE));
		// Unknown level falls back to the normal (non-Awakened) version.
		assertEquals(normal, BossDefence.matchingNpc(12191, "Duke Sucellus"));
		assertEquals(awakened, BossDefence.valueOf(awakened.name()));

		assertEquals(250, BossDefence.matchingNpc(12214, 798, "The Leviathan").getBaseDef());
		assertEquals(250, BossDefence.matchingNpc(12204, 791, "The Whisperer").getBaseDef());
		assertEquals(700, BossDefence.matchingNpc(12223, 784, "Vardorvis").getBaseHp());
		assertTrue(BossDefence.matchingNpc(12223, 1136, "Vardorvis").has(BossDefence.Flag.DRAIN_IMMUNE));

		// Doom of Mokhaiotl: HP per delve; delve 8 and Deep Delve share level 908 with different HP.
		assertEquals(575, BossDefence.matchingNpc(14707, 658, "Doom of Mokhaiotl").getBaseHp());
		assertEquals(-1, BossDefence.matchingNpc(14707, 908, "Doom of Mokhaiotl").getBaseHp());
		// Maggot King's Roaring form can't be told apart; the normal form is used.
		assertEquals(200, BossDefence.matchingNpc(15742, 741, "Maggot King").getBaseDef());
	}

	@Test
	public void awakenedBossIgnoresDrains()
	{
		NPC duke = fakeNpc("Duke Sucellus", 3, 12191);
		when(duke.getCombatLevel()).thenReturn(1099);
		when(config.defenceAlwaysShow()).thenReturn(true);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 3, 1, WORLD, "Me");
		runUntil(1);
		assertEquals(316, defence());
		assertFalse(tracker.state().isDrained());
	}

	@Test
	public void whispererStatsResetWhenSheHitsZeroHp()
	{
		NPC whisperer = fakeNpc("The Whisperer", 4, 12204);
		when(whisperer.getCombatLevel()).thenReturn(791);
		tracker.queue(SpecialWeapon.ACCURSED_SCEPTRE, 4, 10, WORLD, "Me");
		runUntil(1);
		assertTrue(tracker.state().isDrained());

		// At 0 HP she heals and restores all drained stats for the enrage phase.
		when(whisperer.getHealthRatio()).thenReturn(0);
		runUntil(2);
		assertEquals(null, tracker.state());
	}

	@Test
	public void twoDrainedNpcsAreBothKeptForDisplay()
	{
		fakeNpc("Greater Nechryael", 6, 7278);
		fakeNpc("Greater Nechryael", 8, 7278);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 6, 1, WORLD, "Me");
		runUntil(1);
		// A party member's spec on the other NPC while we stay on the first.
		tracker.queue(SpecialWeapon.ARCLIGHT, 8, 1, WORLD, "Friend");
		runUntil(2);

		int drained = 0;
		for (DefenceTracker.DefenceState state : tracker.states())
		{
			if (state.isDrained() && (state.getNpcIndex() == 6 || state.getNpcIndex() == 8))
			{
				drained++;
			}
		}
		assertEquals(2, drained);
	}

	@Test
	public void skeletalMysticsAreTrackedSeparatelyAndRegen()
	{
		assertTrue(BossDefence.SKELETAL_MYSTIC.isPerActor());
		NPC a = fakeNpc("Skeletal Mystic", 20, 7604);
		fakeNpc("Skeletal Mystic", 21, 7605);
		NPC c = fakeNpc("Skeletal Mystic", 22, 7606);
		tracker.onNpcSpawned(a);
		tracker.queue(SpecialWeapon.DRAGON_WARHAMMER, 20, 1, WORLD, "Me");
		runUntil(1);
		tracker.queue(SpecialWeapon.ARCLIGHT, 22, 1, WORLD, "Friend");
		runUntil(2);

		long aDef = -1;
		long cDef = -1;
		for (DefenceTracker.DefenceState state : tracker.states())
		{
			if (state.getNpcIndex() == 20)
			{
				aDef = state.getCurrent();
			}
			if (state.getNpcIndex() == 21)
			{
				assertFalse("untouched mystic must not inherit a drain", state.isDrained());
			}
			if (state.getNpcIndex() == 22)
			{
				cDef = state.getCurrent();
			}
		}
		assertEquals(187 - 187 * 30 / 100, aDef);
		assertTrue(cDef < 187 && cDef != aDef);

		// Mystics regen every 25 ticks from their spawn; the DWH'd one goes back up by 1.
		runUntil(25);
		for (DefenceTracker.DefenceState state : tracker.states())
		{
			if (state.getNpcIndex() == 20)
			{
				assertEquals(aDef + 1, state.getCurrent());
			}
		}

		// One mystic leaving the scene ends only its own state.
		sceneNpcs.remove(c);
		when(npcs.byIndex(22)).thenReturn(null);
		tracker.onNpcDespawned(c);
		for (DefenceTracker.DefenceState state : tracker.states())
		{
			assertFalse(state.getNpcIndex() == 22);
		}
	}

	@Test
	public void hitpointEstimateMatchesOpponentInfoMaths()
	{
		assertEquals(205, DefenceTracker.estimateHitpoints(30, 30, 205));
		assertEquals(0, DefenceTracker.estimateHitpoints(0, 30, 205));
		assertEquals(-1, DefenceTracker.estimateHitpoints(-1, 30, 205));
		long half = DefenceTracker.estimateHitpoints(15, 30, 200);
		assertTrue(half >= 96 && half <= 104);
	}

	@Test
	public void wikiTableLookups()
	{
		assertEquals(10, NpcRegen.statInterval("General Graardor", -1));
		assertEquals(15, NpcRegen.statInterval("Tekton", -1));
		assertEquals(NpcRegen.NONE, NpcRegen.hpInterval("Pestilent Bloat", -1));
		assertEquals(5, NpcRegen.statInterval("Pestilent Bloat", -1));
		assertEquals(NpcRegen.NONE, NpcRegen.statInterval("Lux grub", -1));
		assertEquals(NpcRegen.DEFAULT_INTERVAL, NpcRegen.statInterval("Duke Sucellus", -1));
		assertFalse(NpcRegen.isMeasured("Duke Sucellus", -1));
	}
}
