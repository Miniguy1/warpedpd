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
import xyz.gabriwar.warpedpixeldungeon.items.CycleShard;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The bounty board at the dungeon's door, for the new cycles: three contracts each cycle, the same
 * three whenever it is read (a pure function of the seed, the cycle and the slot). Take a contract, do the
 * deed in the dungeon, and claim its pay at the door: shards, gold and a piece of your own class set.
 *
 *  - HUNTERS: bring down so many bounty hunters (and boss rematches).
 *  - BOSS: kill the boss of one named floor.
 *  - SLAYER: kill so many enemies in the dungeon.
 *  - ZONE: break one of the plaza zones (kill its guardian).
 *
 * A new cycle posts new contracts and forgets the old.
 */
public final class BountyBoard {

	private BountyBoard(){}

	public enum Kind { HUNTERS, BOSS, SLAYER, ZONE }

	public static final int SLOTS = 3;

	public static final class Contract {
		public Kind kind;
		public int need = 1;
		/** BOSS: the floor */
		public int depth;
		public int shards, gold;
		public float rating;

		public String text(){
			switch (kind){
				case HUNTERS: return "Hunt the hunters: bring down " + need + " bounty hunters";
				case BOSS:    return "Boss slayer: kill the boss of floor " + depth;
				case SLAYER:  return "Slayer: kill " + need + " enemies in the dungeon";
				default:      return "Zone breaker: kill the guardian of a plaza zone";
			}
		}
	}

	/** the contract in a slot for a cycle: pure */
	public static Contract contract( long seed, int cycle, int slot ){
		ArrayList<Kind> kinds = new ArrayList<>( java.util.Arrays.asList( Kind.values() ) );
		Collections.shuffle( kinds, new Random( seed * 31L + cycle * 7919L + 17L ) );
		Contract c = new Contract();
		c.kind = kinds.get( slot );
		int cy = Math.max( 1, cycle );
		switch (c.kind){
			case HUNTERS: c.need = 2 + cy; c.shards = 3 + 2 * cy; break;
			case BOSS: {
				c.need = 1;
				c.depth = 5 * (1 + new Random( seed * 131L + cycle * 31L ).nextInt( 5 ));
				c.shards = 6 + 2 * cy;
				break;
			}
			case SLAYER: c.need = 50 + 25 * cy; c.shards = 3 + 2 * cy; break;
			default: c.need = 1; c.shards = 5 + 2 * cy; break;
		}
		c.gold = 300 * cy;
		c.rating = Cycle.lootRating( cy, 26 ) + 0.2f;
		return c;
	}

	// ------------------------------------------------------------------ the board's state

	private static int boardCycle = -1;
	private static final boolean[] accepted = new boolean[SLOTS], claimed = new boolean[SLOTS];
	private static final int[] progress = new int[SLOTS];

	public static void reset(){
		boardCycle = -1;
		java.util.Arrays.fill( accepted, false );
		java.util.Arrays.fill( claimed, false );
		java.util.Arrays.fill( progress, 0 );
	}

	/** a new cycle posts new contracts */
	private static void sync(){
		if (boardCycle != Cycle.number){
			boardCycle = Cycle.number;
			java.util.Arrays.fill( accepted, false );
			java.util.Arrays.fill( claimed, false );
			java.util.Arrays.fill( progress, 0 );
		}
	}

	/** only the new cycles have a board */
	public static boolean available(){
		return Cycle.number >= 1;
	}

	public static Contract current( int slot ){
		return contract( Dungeon.seed, Cycle.number, slot );
	}

	public static boolean isAccepted( int slot ){ sync(); return accepted[slot]; }
	public static boolean isClaimed( int slot ){ sync(); return claimed[slot]; }
	public static int progressOf( int slot ){ sync(); return progress[slot]; }
	public static boolean isReady( int slot ){ sync(); return accepted[slot] && !claimed[slot] && progress[slot] >= current( slot ).need; }

	public static boolean accept( int slot ){
		sync();
		if (!available() || accepted[slot] || claimed[slot]) return false;
		accepted[slot] = true;
		return true;
	}

	/** the pay: shards and a piece of the hero's own set in the pack (or at their feet), and gold */
	public static boolean claim( Hero hero, int slot ){
		if (!isReady( slot )) return false;
		Contract c = current( slot );
		claimed[slot] = true;
		give( hero, new CycleShard().quantity( c.shards ) );
		if (hero.heroClass != null){
			give( hero, EndgameLoot.rollClassSet( hero.heroClass, EndgameLoot.tierOfRating( c.rating ), EndgameLoot.excessLevels( c.rating ) ) );
		}
		Dungeon.gold += c.gold;
		GLog.p( "The bounty is paid: " + c.shards + " shards, " + c.gold + " gold and a trophy." );
		return true;
	}

	private static void give( Hero hero, Item item ){
		if (!item.collect( hero.belongings.backpack ) && Dungeon.level != null){
			Dungeon.level.drop( item, hero.pos );
		}
	}

	// ------------------------------------------------------------------ progress

	/** every death passes through here (Cycle.onDeath) */
	public static void onKill( Mob mob ){
		sync();
		if (!available() || mob == null || mob.alignment != Char.Alignment.ENEMY) return;
		boolean inDungeon = Cycle.affects( Dungeon.branch, Dungeon.depth );
		boolean boss = Char.hasProp( mob, Char.Property.BOSS ) && !Char.hasProp( mob, Char.Property.MINIBOSS );
		for (int s = 0; s < SLOTS; s++){
			if (!accepted[s] || claimed[s]) continue;
			Contract c = current( s );
			switch (c.kind){
				case HUNTERS:
					if (mob.buff( Bounty.class ) != null) progress[s] = Math.min( c.need, progress[s] + 1 );
					break;
				case BOSS:
					if (boss && inDungeon && Dungeon.depth == c.depth) progress[s] = 1;
					break;
				case SLAYER:
					if (inDungeon) progress[s] = Math.min( c.need, progress[s] + 1 );
					break;
				default:
			}
		}
	}

	/** a plaza zone's guardian has fallen (Delves.onDeath) */
	public static void onZoneCleared(){
		sync();
		if (!available()) return;
		for (int s = 0; s < SLOTS; s++){
			if (accepted[s] && !claimed[s] && current( s ).kind == Kind.ZONE) progress[s] = 1;
		}
	}

	// ------------------------------------------------------------------ saves

	public static void store( Bundle b ){
		b.put( "board_cycle", boardCycle );
		b.put( "board_accepted", accepted );
		b.put( "board_claimed", claimed );
		b.put( "board_progress", progress );
	}

	public static void restore( Bundle b ){
		reset();
		if (b.contains( "board_cycle" )) boardCycle = b.getInt( "board_cycle" );
		if (b.contains( "board_accepted" )){
			boolean[] a = b.getBooleanArray( "board_accepted" ), c = b.getBooleanArray( "board_claimed" );
			int[] p = b.getIntArray( "board_progress" );
			for (int i = 0; i < SLOTS && i < a.length; i++){
				accepted[i] = a[i];
				claimed[i] = c[i];
				progress[i] = p[i];
			}
		}
	}
}
