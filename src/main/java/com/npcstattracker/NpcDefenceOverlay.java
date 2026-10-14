package com.npcstattracker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import com.npcstattracker.DefenceTracker.DefenceState;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Draws every tracked NPC's stats in scene space, above each NPC. */
public class NpcDefenceOverlay extends Overlay
{
	private final Client client;
	private final DefenceTracker tracker;
	private final NpcStatTrackerConfig config;
	private final TrackerFontManager fontManager;
	private final DefenceOverlayRenderer renderer;
	private final InitialSyncDisplayGate initialSyncDisplayGate;

	public NpcDefenceOverlay(Client client, DefenceTracker tracker, NpcStatTrackerConfig config,
		SkillIconSource skillIcons, TrackerFontManager fontManager,
		InitialSyncDisplayGate initialSyncDisplayGate)
	{
		this.client = client;
		this.tracker = tracker;
		this.config = config;
		this.fontManager = fontManager;
		this.renderer = new DefenceOverlayRenderer(config, skillIcons);
		this.initialSyncDisplayGate = initialSyncDisplayGate;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.defenceHpBar())
		{
			return null;
		}

		graphics.setFont(fontManager.font());
		if (config.showAllTrackedNpcs())
		{
			renderAll(graphics, tracker.states());
		}
		else
		{
			renderMainTarget(graphics, tracker.state());
		}
		return null;
	}

	/**
	 * Every remembered NPC that is live in the scene, each above its own NPC. This includes NPCs
	 * drained by party members while you fight something else.
	 */
	private void renderAll(Graphics2D graphics, List<DefenceState> states)
	{
		DefenceOverlayPosition position = config.defenceHpBarPosition();
		DefenceState current = tracker.state();
		int currentIndex = current == null ? -1 : current.getNpcIndex();
		for (DefenceState state : states)
		{
			if (!isDisplayable(state))
			{
				continue;
			}
			// "Display before special att" previews only the NPC you are fighting; other NPCs you
			// clicked earlier show up only once a spec has actually drained them.
			if (!state.isDrained() && state.getNpcIndex() != currentIndex)
			{
				continue;
			}
			NPC npc = liveNpc(state.getNpcIndex());
			if (npc != null)
			{
				renderAtNpc(graphics, state, npc, position, config.defenceHpBarYOffset());
			}
		}
	}

	private void renderMainTarget(Graphics2D graphics, DefenceState current)
	{
		if (!isDisplayable(current))
		{
			return;
		}
		NPC npc = liveNpc(current.getNpcIndex());
		if (npc != null)
		{
			renderAtNpc(graphics, current, npc,
				config.defenceHpBarPosition(), config.defenceHpBarYOffset());
		}
	}

	private void renderAtNpc(Graphics2D graphics, DefenceState state, NPC npc,
		DefenceOverlayPosition position, int yOffset)
	{
		int zOffset = (int) (npc.getLogicalHeight() * position.getHeightFactor()) + position.getHeightOffset();
		Point anchor = npc.getCanvasTextLocation(graphics, "", zOffset);
		if (anchor == null)
		{
			return;
		}
		int centreX = anchor.getX() + position.getXNudge();
		int baseline = anchor.getY() - yOffset;
		renderer.renderAt(graphics, state, centreX, baseline);
	}

	private boolean isDisplayable(DefenceState state)
	{
		// The checkbox controls only optional pre-spec preview. Any actual local/party drain
		// remains displayable because drained=true is part of the stored/shared state.
		return state != null
			&& (state.isDrained() || config.defenceAlwaysShow())
			&& state.getNpcIndex() >= 0
			&& !initialSyncDisplayGate.isWaiting(state, client.getTickCount());
	}

	private NPC liveNpc(int index)
	{
		if (index < 0 || client.getTopLevelWorldView() == null || client.getTopLevelWorldView().npcs() == null)
		{
			return null;
		}
		NPC npc = client.getTopLevelWorldView().npcs().byIndex(index);
		return npc != null && !npc.isDead() && npc.getHealthRatio() != 0 ? npc : null;
	}
}
