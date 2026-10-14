package com.npcstattracker;

import java.awt.Font;
import net.runelite.client.ui.FontManager;

/** Builds the selected tracker font. */
final class TrackerFontManager
{
	private final NpcStatTrackerConfig config;

	TrackerFontManager(NpcStatTrackerConfig config)
	{
		this.config = config;
	}

	Font font()
	{
		return font(config.defenceFont(), config.defenceFontSize(), config.defenceBoldText());
	}

	private Font font(TrackerFont selection, int configuredSize, boolean bold)
	{
		int size = Math.max(8, Math.min(48, configuredSize));
		Font base;
		switch (selection)
		{
			case SANS_SERIF:
				base = new Font(Font.SANS_SERIF, Font.PLAIN, size);
				break;
			case SERIF:
				base = new Font(Font.SERIF, Font.PLAIN, size);
				break;
			case MONOSPACED:
				base = new Font(Font.MONOSPACED, Font.PLAIN, size);
				break;
			case RUNESCAPE:
			default:
				base = FontManager.getRunescapeSmallFont().deriveFont((float) size);
				break;
		}
		return bold ? base.deriveFont(Font.BOLD) : base.deriveFont(Font.PLAIN);
	}
}
