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

package xyz.gabriwar.warpedpixeldungeon.levels;

import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashSet;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.*;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.*;
import xyz.gabriwar.warpedpixeldungeon.items.Generator;
import xyz.gabriwar.warpedpixeldungeon.items.Heap;
import xyz.gabriwar.warpedpixeldungeon.items.field.BandageRoll;
import xyz.gabriwar.warpedpixeldungeon.items.field.SmokeFlare;
import xyz.gabriwar.warpedpixeldungeon.items.field.Whetstone;
import xyz.gabriwar.warpedpixeldungeon.items.quest.LostPickaxe;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/**
 * The early floors' own events, in every run: an abandoned camp, a cave-in, a rat parade. Like the cycle events,
 * every block of the early floors (2 to 4, and 6 to 9; never the boss floor) holds one for certain, picked by the
 * world's seed, and each of the others has one chance in five. The same floors may also hold the Old Prospector
 * (floors 2 to 4) or a Stranded Adventurer (floors 3 to 8), once each in a run.
 */
public final class EarlyEvents {

	private EarlyEvents(){}

	public static final int NONE = 0, CAMP = 1, CAVE_IN = 2, PARADE = 3;

	/** once a run each */
	public static boolean prospectorMet = false, adventurerMet = false;

	public static boolean eligible( int branch, int depth ) {
		return branch == 0 && (depth == 2 || depth == 3 || depth == 4 || (depth >= 6 && depth <= 9));
	}

	/** the one floor of its block that always holds an event: 2 to 4, or 6 to 9 */
	public static boolean forced( long seed, int depth ) {
		if (!eligible( 0, depth )) return false;
		int block = depth <= 5 ? 0 : 1;
		long h = seed * 0x9E3779B97F4A7C15L + (block + 7) * 0xBF58476D1CE4E5B9L;
		h ^= h >>> 31;
		h *= 0xD6E8FEB86659FD93L;
		h ^= h >>> 29;
		int idx = (int) Math.floorMod( h, block == 0 ? 3L : 4L );
		return depth == (block == 0 ? 2 : 6) + idx;
	}

	/** a newly built floor: an event (not on a floor the cycle events already hold), and maybe a visitor */
	public static void roll( Level level ) {
		if (!eligible( Dungeon.branch, Dungeon.depth )) return;
		boolean force = forced( Dungeon.seed, Dungeon.depth );
		if (level.climbEvent == ClimbEvents.NONE && (force || Random.Float() < 0.20f)) {
			apply( level, 1 + Random.Int( 3 ) );
		}
		if (!prospectorMet && Dungeon.depth <= 4 && (Dungeon.depth == 4 || Random.Float() < 0.40f)) spawnProspector( level );
		if (!adventurerMet && Dungeon.depth >= 3 && Dungeon.depth <= 8 && (Dungeon.depth == 8 || Random.Float() < 0.30f)) spawnAdventurer( level );
	}

	public static void apply( Level level, int event ) {
		level.earlyEvent = event;
		switch (event) {
			case CAMP: {
				int c = freeCell( level );
				int chest = c == -1 ? -1 : neighbour( level, c, new HashSet<Integer>() );
				if (c == -1 || chest == -1) { level.earlyEvent = NONE; return; }
				AbandonedCamp camp = new AbandonedCamp();
				camp.pos = c;
				level.mobs.add( camp );
				Heap h = level.drop( Generator.random(), chest );
				h = level.drop( Generator.random(), chest );
				h = level.drop( new BandageRoll(), chest );
				h.type = Heap.Type.CHEST;
				break;
			}
			case CAVE_IN: {
				int c = -1;
				for (int tries = 0; tries < 30; tries++) {
					int t = freeCell( level );
					if (t != -1 && !blocksPath( level, t )) { c = t; break; }
				}
				if (c == -1) { level.earlyEvent = NONE; return; }
				RubblePile pile = new RubblePile();
				pile.pos = c;
				level.mobs.add( pile );
				break;
			}
			case PARADE: {
				int c = freeCell( level );
				if (c == -1) { level.earlyEvent = NONE; return; }
				Heap h = level.drop( Generator.random(), c );
				h = level.drop( Generator.random(), c );
				h = level.drop( new Whetstone(), c );
				h = level.drop( new SmokeFlare().quantity( 2 ), c );
				h.type = Heap.Type.CHEST;
				level.paradeChest = c;
				break;
			}
			default:
				level.earlyEvent = NONE;
		}
	}

	/** the first arrival: the hero is told what this floor holds */
	public static void announce( Level level ) {
		if (level.earlyEvent == NONE || level.earlyAnnounced || Dungeon.hero == null) return;
		level.earlyAnnounced = true;
		switch (level.earlyEvent) {
			case CAMP: GLog.p( "The smell of old woodsmoke: someone has made a camp on this floor." ); break;
			case CAVE_IN: GLog.w( "Dust hangs in the air. Part of this floor has caved in." ); break;
			case PARADE: GLog.w( "A chest sits out in the open. The walls are very quiet." ); break;
			default:
		}
	}

	/** the parade chest is opened: the walls are full of rats */
	public static void onParade( Hero hero ) {
		Level lv = Dungeon.level;
		if (lv == null) return;
		lv.paradeChest = -1;
		GLog.n( "Squeaking, scratching: the walls are full of rats!" );
		spawnAround( lv, hero.pos, Rat.class, 2 );
		spawnAround( lv, hero.pos, PowderRat.class, 2 );
		spawnAround( lv, hero.pos, PickpocketRat.class, 1 );
	}

	/** an old man who has lost his pickaxe on this floor, and the pickaxe lying somewhere else on it */
	public static OldProspector spawnProspector( Level level ) {
		int c = freeCell( level );
		int p = freeCell( level );
		if (c == -1 || p == -1 || c == p) return null;
		OldProspector npc = new OldProspector();
		npc.pos = c;
		level.mobs.add( npc );
		level.drop( new LostPickaxe(), p );
		prospectorMet = true;
		return npc;
	}

	/** an adventurer with two enemies standing over him */
	public static StrandedAdventurer spawnAdventurer( Level level ) {
		int c = freeCell( level );
		if (c == -1) return null;
		StrandedAdventurer npc = new StrandedAdventurer();
		npc.pos = c;
		level.mobs.add( npc );
		java.util.ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation( Dungeon.depth );
		HashSet<Integer> taken = new HashSet<>();
		taken.add( c );
		for (int i = 0; i < 2 && rotation != null && !rotation.isEmpty(); i++) {
			Mob m = Reflection.newInstance( Random.element( rotation ) );
			int at = neighbour( level, c, taken );
			if (m == null || at == -1) continue;
			taken.add( at );
			m.pos = at;
			level.mobs.add( m );
		}
		adventurerMet = true;
		return npc;
	}

	/** a reward is handed over at the hero's feet (and beside them when there are several), never as a string of pickups that each spend the hero's turn */
	public static void give( Hero hero, java.util.List<Item> items ) {
		Level level = Dungeon.level;
		if (level == null || hero == null) return;
		HashSet<Integer> taken = new HashSet<>();
		taken.add( hero.pos );
		boolean first = true;
		for (Item it : items) {
			int at = hero.pos;
			if (!first) {
				int n = neighbour( level, hero.pos, taken );
				if (n != -1) at = n;
			}
			taken.add( at );
			first = false;
			Heap heap = level.drop( it, at );
			if (heap != null && heap.sprite != null) heap.sprite.drop();
		}
	}

	/** a free, reachable cell: nothing lies on it or stands on it, and it is not a staircase */
	public static int freeCell( Level level ) {
		for (int tries = 0; tries < 60; tries++) {
			int c = level.randomDestination( null );
			if (c < 0 || !level.passable[c] || level.heaps.get( c ) != null || Actor.findChar( c ) != null) continue;
			if (c == level.entrance() || c == level.exit()) continue;
			boolean taken = false;
			for (Mob m : level.mobs) if (m.pos == c) { taken = true; break; }
			if (!taken) return c;
		}
		return -1;
	}

	public static int neighbour( Level level, int cell, HashSet<Integer> taken ) {
		for (int d : PathFinder.NEIGHBOURS8) {
			int c = cell + d;
			if (c < 0 || c >= level.length() || !level.passable[c] || taken.contains( c ) || level.heaps.get( c ) != null) continue;
			if (c == level.entrance() || c == level.exit() || Actor.findChar( c ) != null) continue;
			boolean occupied = false;
			for (Mob m : level.mobs) if (m.pos == c) { occupied = true; break; }
			if (!occupied) return c;
		}
		return -1;
	}

	/** are the stairs joined by walking, with this map of what can be walked on? (a stair cell itself may not be walkable, so a way to its side counts) */
	private static boolean joined( Level level, boolean[] pass, int from, int to ) {
		PathFinder.buildDistanceMap( from, pass );
		if (PathFinder.distance[to] != Integer.MAX_VALUE) return true;
		for (int d : PathFinder.NEIGHBOURS8) {
			int n = to + d;
			if (n >= 0 && n < level.length() && pass[n] && PathFinder.distance[n] != Integer.MAX_VALUE) return true;
		}
		return false;
	}

	/** would a pile of rubble on this cell cut the stairs off from each other? Only if they were joined before it. */
	public static boolean blocksPath( Level level, int cell ) {
		int from = level.entrance(), to = level.exit();
		if (from < 0 || to < 0) return false;
		if (!joined( level, level.passable, from, to )) return false;   //nothing to cut
		boolean[] pass = level.passable.clone();
		pass[cell] = false;
		return !joined( level, pass, from, to );
	}

	/** n enemies of a kind next to a cell (in the running game they come at the hero) */
	public static int spawnAround( Level level, int cell, Class<? extends Mob> kind, int n ) {
		HashSet<Integer> taken = new HashSet<>();
		int made = 0;
		for (int i = 0; i < n; i++) {
			Mob m = Reflection.newInstance( kind );
			if (m == null) continue;
			int at = neighbour( level, cell, taken );
			if (at == -1) at = level.randomRespawnCell( m );
			if (at == -1) continue;
			taken.add( at );
			m.pos = at;
			if (com.watabou.noosa.Game.scene() instanceof GameScene) {
				GameScene.add( m );
				if (Dungeon.hero != null && m.sprite != null) m.beckon( Dungeon.hero.pos );
			} else {
				level.mobs.add( m );
			}
			made++;
		}
		return made;
	}

	public static void reset() {
		prospectorMet = false;
		adventurerMet = false;
	}

	public static void store( Bundle b ) {
		b.put( "early_prospector", prospectorMet );
		b.put( "early_adventurer", adventurerMet );
	}

	public static void restore( Bundle b ) {
		prospectorMet = b.getBoolean( "early_prospector" );
		adventurerMet = b.getBoolean( "early_adventurer" );
	}
}
