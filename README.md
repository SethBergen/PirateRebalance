
# Pirate Rebalance  
Pirate Rebalance is a mod for [Starsector by Fractal Softworks](https://fractalsoftworks.com/) that seeks to overhaul the behaviour of the pirate AI to open up a wider range of playstyles compatible with colonization.

In its vanilla state, Starsector does a great job in providing many ways to play the game. Want to build a vast military fleet and conquer your way across the sector? You can do that! Want to live out your fantasies running contraband between worlds as a lowly smuggler? You can do that too! Unfortunately, this starts to break down somewhat if the player decides to colonize (at least as of version 0.9.1a). Hyper-aggressive pirates attack every new colony with numbers that quickly get out of hand for players without a whole lot of firepower. This may be fine for the fleet admiral seeking to carve out an empire, but it makes many other playstyles inviable, such as running a low-profile backwater hidden away from the sector core.

Pirate Rebalance makes several adjustments to the base pirate AI that should allow for more variety in how they interact with the player (and factions) and vice-versa. See the _Feature Configuration_ section below for more info on the individual changes.

## Installation  
  
1. Extract contents of the .zip into the mods folder in your Starsector install directory  
2. Toggle Pirate Rebalance on in the mods panel on the Starsector launcher  
  
## Usage  

**This mod will not work with existing saves**, so you will need to start a new game in order for its changes to take effect.
  
You can customize which behaviour Pirate Rebalance overrides by changing values in `pr_config.json`. See the _Feature Configuration_ section below for details of each behaviour change.  
  
Pirate Rebalance overrides several core behaviours, and has therefore been marked as a "total conversion" mod for stability reasons. By default, only utility mods (like [LazyWizard's Console Commands](https://fractalsoftworks.com/forum/index.php?topic=4106.0)) will be loaded alongside it. If you want to use this mod with other, non-utility mods, open `mod_info.json` and set the following:

`"totalConversion": false`  

Be aware that this may cause crashes, or prevent the game from running entirely if used with incompatible mods.

## Feature Configuration

#### "newPirateBaseInactivityMonths"
_Found In:_ `/pr_config.json`  
_Default:_ `12`  

>Sets a period in months after the creation of a new pirate base wherein no raids are launched. This is achieved by setting the internal variable `raidTimeoutMonths` whose default value is 0, equal to `newPirateBaseInactivityMonths` when the base spawns. This should provide new player colonies a longer grace period before pirate raids commence.  
>
>Note: This grace period only applied to _new_ bases. New colonies can still be attacked if they are within an existing pirate base's operational range (see "preferredPirateRange").

#### "disableForceTargettingPlayer"  
_Found In:_ `/pr_config.json`  
_Default:_ `true`  

>In the vanilla game, a new pirate base spawns for each player colony established. This base can only attack player-owned colonies and respawns in another system if killed. This setting lifts the targetting restriction for the pirate base allowing it to attack faction bases in range as well. This is meant to reduce "gameyness" in pirate behaviour and contribute to the emergent quality of safe and unsafe space that this mod seeks to foster.  
>
>**This change represents a major alteration of the game's systems and balance. If you like the late-game challenge pirates present in vanilla, and just want to give new colonies some breathing room, set this flag to `false`.**  
>
>Be warned that by leaving this enabled, factions in your game may have a hard time with the number of pirates able to attack them in some late-game scenarios.

#### "disableScriptedFirstRaid"  
_Found In:_ `/pr_config.json`  
_Default:_ `true`  

>In the vanilla game, new player-targetting pirate bases are scripted to launch a raid 4 months after their associated colony in established. This flag disables this initial raid.  


#### "adaptivePirateBaseTierScaling"
_Found In:_ `/pr_config.json`  
_Default:_ `true`  

>In the vanilla game, the minimum level of new pirate bases in the sector increases with time and there is no restriction on how far bases can upgrade themselves, quickly reaching the highest possible tier if left unchecked. With this setting enabled, the starting level of bases as well as their max level is dependent on player performance. There are two triggers here:
> 1. Should the player own a sufficiently high level colony, the level of new pirate bases and the max upgrade tier will increase. This is intended to prevent things getting out of hand if the player isnt keeping up with destroying pirate bases before they get too powerful.  
> 2. As the player destroys pirate bases, their min and max tiers will gradually increase. This means that even if the player does not colonize, pirates will still present a martial challenge.  
> 
>The thresholds for each of these triggers are independent and the effects do not stack. Whichever the player is "further along" on will be the active determinant of pirate difficulty. 

#### "preferredPirateRange"  
_Found In:_ `/pr_config.json`  
_Default:_ `10000`  

>In the vanilla game, pirate bases have an infinite operational range. They prefer targets within 10 light-years (ly), giving them full weight when choosing a target. Likelyhood of selection decreases linearly from there out to 19ly where chances are 10% that of preferred targets. All targets beyond this point remain at 10% weighting. This is why you'll often see pirate raid indicators on the map stretch massive distances across the sector. Use this value in conjuction with `restrictPirateRange` to determine the effecive operational range of pirate bases.
>
>Note: For reference, The Core is 12ly by 18ly, and 1ly is equal to 2000 units of distance.

#### "restrictPirateRange"
_Found In:_ `/pr_config.json`  
_Default:_ `true`

>Enforces a hard cap on the operational range of pirate bases equal to `2 * preferredPirateRange`

#### "pirateBaseMinMonthsForNextTier"  
_Found In:_ `/data/config/settings.json`  
_PR Default:_ `18`  
_Vanilla Default:_ `6`  

>Pirate bases upgrade over time. In the current version of the game, this happens extremely quickly and is even something the game's lead developer has [expressed interest in changing](https://fractalsoftworks.com/forum/index.php?topic=15958.msg255547#msg255547). By increasing the minimum number of months before a pirate base can upgrade itself, the player is afforded more time to deal with them before they become unmanageably strong for all but the most powerful player fleets.

#### "noPirateRaidDays"
_Found In:_ `/data/config/settings.json`  
_PR Default:_ `0`  
_Vanilla Default:_ `365`  

>Determines the number of days after the start of the game before pirates start conducting random raids.
