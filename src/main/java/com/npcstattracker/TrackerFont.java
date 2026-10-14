package com.npcstattracker;

public enum TrackerFont
{
	RUNESCAPE("RuneScape"),
	SANS_SERIF("Sans serif"),
	SERIF("Serif"),
	MONOSPACED("Monospaced");

	private final String label;

	TrackerFont(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
