package com.npcstattracker;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.util.ImageUtil;

/** Supplies small skill icons, optionally honoring active RuneLite sprite/theme overrides. */
final class SkillIconSource
{
	private static final int ICON_SIZE = 16;
	private final Client client;
	private final SkillIconManager skillIconManager;
	private final NpcStatTrackerConfig config;
	private final Map<Skill, SpritePixels> lastOverride = new EnumMap<>(Skill.class);
	private final Map<Skill, BufferedImage> themed = new EnumMap<>(Skill.class);
	private final Map<Skill, BufferedImage> classic = new EnumMap<>(Skill.class);

	SkillIconSource(Client client, SkillIconManager skillIconManager, NpcStatTrackerConfig config)
	{
		this.client = client;
		this.skillIconManager = skillIconManager;
		this.config = config;
	}

	BufferedImage defence()
	{
		return defence(config.defenceUseThemeSkillIcons());
	}

	BufferedImage defence(boolean useTheme)
	{
		return icon(Skill.DEFENCE, SpriteID.Staticons.DEFENCE, useTheme);
	}

	BufferedImage magic()
	{
		return magic(config.defenceUseThemeSkillIcons());
	}

	BufferedImage magic(boolean useTheme)
	{
		return icon(Skill.MAGIC, SpriteID.Staticons.MAGIC, useTheme);
	}

	BufferedImage attack()
	{
		return icon(Skill.ATTACK, SpriteID.Staticons.ATTACK, config.defenceUseThemeSkillIcons());
	}

	BufferedImage strength()
	{
		return icon(Skill.STRENGTH, SpriteID.Staticons.STRENGTH, config.defenceUseThemeSkillIcons());
	}

	BufferedImage ranged()
	{
		return icon(Skill.RANGED, SpriteID.Staticons.RANGED, config.defenceUseThemeSkillIcons());
	}

	BufferedImage hitpoints()
	{
		return icon(Skill.HITPOINTS, SpriteID.Staticons.HITPOINTS, config.defenceUseThemeSkillIcons());
	}

	private BufferedImage icon(Skill skill, int spriteId, boolean useTheme)
	{
		if (useTheme)
		{
			SpritePixels override = client.getSpriteOverrides().get(spriteId);
			if (override != null)
			{
				BufferedImage cached = themed.get(skill);
				if (override != lastOverride.get(skill) || cached == null)
				{
					lastOverride.put(skill, override);
					cached = ImageUtil.resizeImage(override.toBufferedImage(), ICON_SIZE, ICON_SIZE);
					themed.put(skill, cached);
				}
				return cached;
			}
		}
		return classic.computeIfAbsent(skill,
			s -> ImageUtil.resizeImage(skillIconManager.getSkillImage(s), ICON_SIZE, ICON_SIZE));
	}
}
