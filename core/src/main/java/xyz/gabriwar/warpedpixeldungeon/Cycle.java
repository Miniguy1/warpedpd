/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package xyz.gabriwar.warpedpixeldungeon;

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Blacksmith;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.CagedKobold;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Ghost;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Imp;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Wandmaker;
import xyz.gabriwar.warpedpixeldungeon.items.Amulet;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Generator;
import xyz.gabriwar.warpedpixeldungeon.items.Gold;
import xyz.gabriwar.warpedpixeldungeon.levels.rooms.secret.SecretRoom;
import xyz.gabriwar.warpedpixeldungeon.levels.rooms.special.SpecialRoom;
import xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The cycles: once the hero has climbed out of the dungeon with the amulet and the curse is lifted
 * (Statistics.ascended), the dungeon's door offers a new cycle. A new cycle wipes the dungeon (its floors,
 * its quests, its limited drops, the amulet goes back to its heart) and makes every enemy in it stronger:
 * cycle 1 is twice as strong, cycle 2 three times, and so on. Loot and boss hoards grow with the cycle, and
 * on the climb back up the floors hold events (levels/ClimbEvents). The hero, the town, the homestead, the
 * world and every other place (the mines, the plaza zones) are untouched.
 *
 * Cycle 0 is the first run through the dungeon, as it always was.
 */
public final class Cycle {

	private Cycle(){}

	/** the hero's current cycle: 0 until the first reset */
	public static int number = 0;

	/** the dungeon proper and the branches hanging off it (not the mines, the zones or the surface) */
	public static boolean affects( int branch, int depth ){
		return (branch == 0 && depth >= 1 && depth <= 26) || (branch >= 1 && branch <= 5);
	}

	/** the health / damage factor of an enemy on this floor in this cycle */
	public static float powerAt( int cycle, int branch, int depth ){
		return cycle > 0 && affects( branch, depth ) ? 1f + cycle : 1f;
	}

	/** accuracy / evasion grow at half the rate */
	public static float skillAt( int cycle, int branch, int depth ){
		return cycle > 0 && affects( branch, depth ) ? 1f + 0.5f * cycle : 1f;
	}

	private static boolean scaled( Char ch ){
		return ch instanceof Mob && ch.alignment == Char.Alignment.ENEMY;
	}

	/** the cycle's factor on an enemy (and a blood moon's, on top) */
	public static float power( Char ch ){
		if (!scaled( ch )) return 1f;
		float p = powerAt( number, Dungeon.branch, Dungeon.depth );
		if (Dungeon.level != null && Dungeon.level.climbEvent == ClimbEvents.BLOOD_MOON && affects( Dungeon.branch, Dungeon.depth )){
			p *= ClimbEvents.BLOOD_MOON_POWER;
		}
		return p;
	}

	public static float skill( Char ch ){
		return scaled( ch ) ? skillAt( number, Dungeon.branch, Dungeon.depth ) : 1f;
	}

	/** the rating of a floor's loot in this cycle: 6 at cycle 0 on the first floor, up with the cycle and the depth */
	public static float lootRating( int cycle, int depth ){
		return Math.min( 9f, 6f + 0.9f * cycle + depth / 26f );
	}

	/** is the door's new-cycle offer open? Only once the climb is done and the curse lifted. */
	public static boolean offersReset(){
		return Statistics.ascended;
	}

	// ------------------------------------------------------------------ the reset

	/** Starts the next cycle: the dungeon forgets everything, the hero keeps everything else. */
	public static void begin( Hero hero ){
		number++;
		resetDungeonState( hero );
		deleteDungeonFiles();
	}

	/** the state half of the reset (tested without any files) */
	public static void resetDungeonState( Hero hero ){
		//the amulet goes back to the dungeon's heart
		if (hero != null && hero.belongings != null){
			Amulet amulet = hero.belongings.getItem( Amulet.class );
			if (amulet != null) amulet.detachAll( hero.belongings.backpack );
		}
		Statistics.ascended = false;
		Statistics.amuletObtained = false;
		Statistics.highestAscent = 0;
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();

		//the dungeon's floors and its branches are unvisited again (the town, the mines and the zones stay)
		ArrayList<Integer> keep = new ArrayList<>();
		for (int g : Dungeon.generatedLevels){
			int branch = g / 1000, depth = g % 1000;
			if (!affects( branch, depth )) keep.add( g );
		}
		Dungeon.generatedLevels.clear();
		Dungeon.generatedLevels.addAll( keep );
		Dungeon.droppedItems = new SparseArray<>();
		Dungeon.fallenMobs = new SparseArray<>();
		Dungeon.chapters = new HashSet<>();
		Dungeon.LimitedDrops.reset();

		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();
		CagedKobold.Quest.reset();

		Dungeon.templeCompleted = false;
		Dungeon.shadowyogkilled = false;
		Dungeon.crabkingkilled = false;
		Dungeon.banditkingkilled = false;
		Dungeon.skeletonkingkilled = false;
		Dungeon.tengukilled = false;
		Dungeon.tengudenkilled = false;
		Dungeon.gnollspawned = false;
		Dungeon.skeletonspawned = false;
		Dungeon.goldthiefspawned = false;
		Dungeon.orbofzotdropped = false;

		//the dungeon's rooms and item decks start afresh, and differently from the last cycle
		Random.pushGenerator( Dungeon.seed + 1 + 7919L * number );
		try {
			SpecialRoom.initForRun();
			SecretRoom.initForRun();
			xyz.gabriwar.warpedpixeldungeon.levels.rooms.warped.WarpedRooms.initForRun();
			Generator.fullReset();
		} finally {
			Random.popGenerator();
		}
	}

	/** the saved floors of the dungeon proper and its branches */
	public static void deleteDungeonFiles(){
		int slot = GamesInProgress.curSlot;
		if (slot <= 0) return;
		try {
			for (int d = 1; d <= 26; d++) FileUtils.deleteFile( GamesInProgress.depthFile( slot, d, 0 ) );
			for (int b = 1; b <= 5; b++){
				for (int d = 1; d <= 30; d++) FileUtils.deleteFile( GamesInProgress.depthFile( slot, d, b ) );
			}
		} catch (Exception e){
			//a floor file that is not there is the point
		}
	}

	// ------------------------------------------------------------------ the rewards

	/** a bounty's hunter or a boss of a cycle has fallen: its hoard */
	public static void onDeath( Mob mob ){
		if (mob == null || Dungeon.level == null || Dungeon.hero == null) return;
		boolean bounty = mob.buff( Bounty.class ) != null;
		boolean boss = Char.hasProp( mob, Char.Property.BOSS ) && !Char.hasProp( mob, Char.Property.MINIBOSS );
		//a vengeful boss does not fall alone
		xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation mut = mob.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.class );
		if (mut != null && mut.has( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.VENGEFUL )){
			xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.summon( mob, 3 );
			GLog.n( "The " + mob.name() + " does not fall alone: its death calls up more." );
		}
		if (!bounty && !(boss && number > 0 && affects( Dungeon.branch, Dungeon.depth ))) return;
		float rating = lootRating( number, Dungeon.depth ) + (bounty ? 1.5f : 1.0f);
		int items = bounty ? 3 : 2;
		for (int i = 0; i < items; i++){
			Dungeon.level.drop( EndgameLoot.rollRated( rating ), mob.pos );
		}
		if (Dungeon.hero.heroClass != null){
			Dungeon.level.drop( EndgameLoot.rollClassSet( Dungeon.hero.heroClass, EndgameLoot.tierOfRating( rating ) ), mob.pos );
		}
		//shards for reforging (items/CycleShard)
		Dungeon.level.drop( new xyz.gabriwar.warpedpixeldungeon.items.CycleShard().quantity( (bounty ? 3 : 2) + number ), mob.pos );
		if (bounty){
			int gold = Random.NormalIntRange( 150, 250 ) * Math.max( 1, number ) + 30 * Dungeon.depth;
			Dungeon.level.drop( new Gold( gold ), mob.pos );
			GLog.p( "The bounty is paid: " + gold + " gold and the hunter's trophies." );
		}
	}
}
