package com.npcstattracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Development launcher: ./gradlew run */
public class NpcStatTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NpcStatTrackerPlugin.class);
		RuneLite.main(args);
	}
}
