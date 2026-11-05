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

	public static final int NONE = 0, CAMP = 1, CAVE_IN = 2, PARADE = 3, CACHE = 4, HOARD = 5, STASH = 6, MIMIC = 7;

	/** once a run each */
	public static boolean prospectorMet = false, adventurerMet = false;

	/** every ordinary floor of the dungeon proper, from the second to the last before the final boss (not the boss floors) */
	public static boolean eligible( int branch, int depth ) {
		return branch == 0 && depth >= 2 && depth <= 24 && depth % 5 != 0;
	}

	/** the one floor of each block of five (2 to 4, 6 to 9, 11 to 14, 16 to 19, 21 to 24) that always holds an event */
	public static boolean forced( long seed, int depth ) {
		if (!eligible( 0, depth )) return false;
		int block = (depth - 1) / 5;
		long h = seed * 0x9E3779B97F4A7C15L + (block + 7) * 0xBF58476D1CE4E5B9L;
		h ^= h >>> 31;
		h *= 0xD6E8FEB86659FD93L;
		h ^= h >>> 29;
		int first = block == 0 ? 2 : 5 * block + 1;
		int idx = (int) Math.floorMod( h, block == 0 ? 3L : 4L );
		return depth == first + idx;
	}

	/** which event: the camp, the cave-in and the parade, the supply cache (the commonest), the guarded hoard, the fallen hero's stash and the mimic */
	public static int pickEvent() {
		int[] w = { 0, 2, 2, 2, 3, 2, 2, 2 };
		int total = 0;
		for (int e = 1; e < w.length; e++) total += w[e];
		int r = Random.Int( total );
		for (int e = 1; e < w.length; e++) {
			if (r < w[e]) return e;
			r -= w[e];
		}
		return CACHE;
	}

	//---- what an event pays and what it sends, by depth ----

	/** gold for this floor: it grows with depth */
	public static int goldFor( int depth ) {
		return Math.max( 10, Math.round( (30 + 8 * Math.max( 1, depth )) * Random.Float( 0.8f, 1.2f ) ) );
	}

	/** how many random finds an event pays: 2 up to floor 9, 3 from 10, 4 from 20 */
	public static int findsFor( int depth ) {
		return 2 + Math.max( 0, depth ) / 10;
	}

	/** a weapon or an armor of the floor's own tier */
	public static xyz.gabriwar.warpedpixeldungeon.items.Item gearFor( int depth, boolean weapon ) {
		if (!weapon) return Generator.random( Generator.Category.ARMOR );
		Generator.Category c = depth <= 4 ? Generator.Category.WEP_T2 : depth <= 9 ? Generator.Category.WEP_T3
				: depth <= 14 ? Generator.Category.WEP_T4 : Generator.Category.WEP_T5;
		return Generator.random( c );
	}

	/** n of the floor's own creatures, around a cell */
	public static void spawnNatives( Level lv, int around, int n ) {
		java.util.ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation( Dungeon.depth );
		for (int i = 0; i < n && rotation != null && !rotation.isEmpty(); i++) {
			spawnAround( lv, around, Random.element( rotation ), 1 );
		}
	}

	/** what lives behind rubble: rats on the early floors, the floor's own creatures below them */
	public static void nest( Level level, int cell ) {
		if (Dungeon.depth <= 9) {
			spawnAround( level, cell, Rat.class, 2 );
			spawnAround( level, cell, PowderRat.class, 1 );
		} else {
			spawnNatives( level, cell, 3 + Dungeon.depth / 6 );
		}
	}

	/** a newly built floor: an event (not on a floor the cycle events already hold), and maybe a visitor */
	public static void roll( Level level ) {
    //the first floor has no event on the way down, but it is rebuilt on the climb out with the charm, and then it can
    boolean climbOutFirst = Dungeon.branch == 0 && Dungeon.depth == 1 && Dungeon.rebuildCount( 1 ) > 0;
    if (!eligible( Dungeon.branch, Dungeon.depth ) && !climbOutFirst) return;
    boolean force = forced( Dungeon.seed, Dungeon.depth );
		if (level.climbEvent == ClimbEvents.NONE && (force || Random.Float() < 0.20f)) {
			apply( level, pickEvent() );
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
				Heap h = null;
        for (int i = 0; i < findsFor( Dungeon.depth ); i++) h = level.drop( Generator.random(), chest );
        h = level.drop( new BandageRoll().quantity( 1 + Dungeon.depth / 8 ), chest );
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
				Heap h = null;
        for (int i = 0; i < findsFor( Dungeon.depth ); i++) h = level.drop( Generator.random(), c );
        h = level.drop( new xyz.gabriwar.warpedpixeldungeon.items.Gold( goldFor( Dungeon.depth ) ), c );
        h = level.drop( new Whetstone(), c );
				h = level.drop( new SmokeFlare().quantity( 2 ), c );
				h.type = Heap.Type.CHEST;
				level.paradeChest = c;
				break;
			}
			case CACHE: {
        int c = freeCell( level );
        if (c == -1) { level.earlyEvent = NONE; return; }
        Heap h = null;
        for (int i = 0; i < findsFor( Dungeon.depth ); i++) h = level.drop( Generator.random(), c );
        h = level.drop( new xyz.gabriwar.warpedpixeldungeon.items.Gold( goldFor( Dungeon.depth ) ), c );
        h = level.drop( new BandageRoll().quantity( 1 + Dungeon.depth / 8 ), c );
        h.type = Heap.Type.CHEST;
        break;
      }
      case HOARD: {
        int c = freeCell( level );
        if (c == -1) { level.earlyEvent = NONE; return; }
        Heap h = null;
        for (int i = 0; i < findsFor( Dungeon.depth ) + 1; i++) h = level.drop( Generator.random(), c );
        h = level.drop( new xyz.gabriwar.warpedpixeldungeon.items.Gold( Math.round( goldFor( Dungeon.depth ) * 1.5f ) ), c );
        h = level.drop( new BandageRoll().quantity( 1 + Dungeon.depth / 8 ), c );
        h.type = Heap.Type.CHEST;
        //its guards sleep around it: more of them, and tougher ones, the deeper the floor
        java.util.ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation( Dungeon.depth );
        HashSet<Integer> taken = new HashSet<>();
        taken.add( c );
        for (int i = 0; i < 2 + Dungeon.depth / 8 && rotation != null && !rotation.isEmpty(); i++) {
          Mob m = Reflection.newInstance( Random.element( rotation ) );
          int at = neighbour( level, c, taken );
          if (m == null || at == -1) continue;
          taken.add( at );
          m.pos = at;
          level.mobs.add( m );
        }
        break;
      }
      case STASH: {
        int c = freeCell( level );
        if (c == -1) { level.earlyEvent = NONE; return; }
        Heap h = level.drop( gearFor( Dungeon.depth, true ), c );
        h = level.drop( gearFor( Dungeon.depth, false ), c );
        h = level.drop( new xyz.gabriwar.warpedpixeldungeon.items.Gold( goldFor( Dungeon.depth ) ), c );
        h.type = Heap.Type.SKELETON;
        break;
      }
      case MIMIC: {
        int c = freeCell( level );
        if (c == -1) { level.earlyEvent = NONE; return; }
        java.util.ArrayList<xyz.gabriwar.warpedpixeldungeon.items.Item> prize = new java.util.ArrayList<>();
        for (int i = 0; i < findsFor( Dungeon.depth ); i++) prize.add( Generator.random() );
        prize.add( new xyz.gabriwar.warpedpixeldungeon.items.Gold( goldFor( Dungeon.depth ) ) );
        xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mimic mimic = xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mimic.spawnAt( c, prize.toArray( new xyz.gabriwar.warpedpixeldungeon.items.Item[0] ) );
        if (mimic == null) { level.earlyEvent = NONE; return; }
        mimic.pos = c;
        level.mobs.add( mimic );
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
      case CACHE: GLog.p( "A forgotten chest is tucked away somewhere on this floor." ); break;
      case HOARD: GLog.w( "Something is guarding a stash on this floor." ); break;
      case STASH: GLog.p( "A fallen adventurer lies somewhere on this floor, still holding their gear." ); break;
      case MIMIC: GLog.w( "A chest sits out here unguarded. It seems too good to be true." ); break;
			default:
		}
	}

	/** the parade chest is opened: the walls are full of rats */
	public static void onParade( Hero hero ) {
		Level lv = Dungeon.level;
		if (lv == null) return;
		lv.paradeChest = -1;
		if (Dungeon.depth <= 9) {
      GLog.n( "Squeaking, scratching: the walls are full of rats!" );
      spawnAround( lv, hero.pos, Rat.class, 2 );
      spawnAround( lv, hero.pos, PowderRat.class, 2 );
      spawnAround( lv, hero.pos, PickpocketRat.class, 1 );
    } else {
      GLog.n( "The walls tear open: the floor's creatures pour out!" );
      spawnNatives( lv, hero.pos, 4 + Dungeon.depth / 5 );
    }
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

	/** An NPC's own code failed: say what and where in the game log (instead of closing the game), so the cause can be read off it. Returns the line. */
	public static String report( Mob who, Throwable t ) {
		StackTraceElement[] st = t.getStackTrace();
		StringBuilder at = new StringBuilder();
		for (int i = 0; i < Math.min( 3, st.length ); i++) {
			if (i > 0) at.append( " < " );
			at.append( st[i].getClassName().replaceAll( ".*\\.", "" ) ).append( '.' ).append( st[i].getMethodName() ).append( ':' ).append( st[i].getLineNumber() );
		}
		String text = (who == null ? "NPC" : who.getClass().getSimpleName()) + " trouble: " + t.getClass().getSimpleName()
				+ (t.getMessage() == null ? "" : " (" + t.getMessage() + ")") + " at " + at;
		try { xyz.gabriwar.warpedpixeldungeon.utils.GLog.n( text ); } catch (Throwable ignored) {}
		try { com.watabou.noosa.Game.reportException( t ); } catch (Throwable ignored) {}
		return text;
	}

	/** a reward is handed over at the hero's feet (and beside them when there are several), never as a string of pickups that each spend the hero's turn */
	public static void give( Hero hero, java.util.List<xyz.gabriwar.warpedpixeldungeon.items.Item> items ) {
		Level level = Dungeon.level;
		if (level == null || hero == null) return;
		HashSet<Integer> taken = new HashSet<>();
		taken.add( hero.pos );
		boolean first = true;
		for (xyz.gabriwar.warpedpixeldungeon.items.Item it : items) {
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
