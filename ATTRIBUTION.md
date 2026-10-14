# Attribution

NPC Stat Tracker is a fork of **Better Party Defence** (https://github.com/Falsary/better-party-defence,
commit 06b3b71, BSD 2-Clause). Its licence notice is preserved in `LICENSE`.

Better Party Defence itself was refactored from defence-tracking code derived from **OSParty**
(https://github.com/osparty/osparty, BSD 2-Clause); that notice is preserved in `LICENSE-OSPARTY.txt`.
Its local special-attack detection follows RuneLite's core Special Attack Counter (BSD 2-Clause).

NPC regeneration intervals come from the OSRS Wiki page "User:Orion/Regen" (measured with
Monster Examine; CoX values confirmed by Mod Ash), licensed CC BY-NC-SA 3.0.

Base stats for non-boss NPCs (`src/main/resources/com/npcstattracker/npc_stats.tsv`) are generated
from weirdgloop/osrs-dps-calc `cdn/json/monsters.json` (OSRS Wiki data, CC BY-NC-SA 3.0).

Changes in this fork: renamed to NPC Stat Tracker (own config group), tracks every NPC in the stat
table and all six combat stats with per-stat toggles, and adds natural stat/HP regeneration tracking.
