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
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Pacts: optional vows, taken at the dungeon's door in the new cycles, that make the dungeon's enemies
 * harder in exchange for better loot. They only ever change the dungeon's side of the fight (its enemies,
 * its floors, its bosses), never how the hero plays. All of them are off until chosen. Each one that is on
 * adds a tenth of a tier to every loot rating in the dungeon, and a shard to a boss's or a bounty's pay.
 */
public final class Pacts {

	private Pacts(){}

	public enum Pact {
		HARDENED( "Hardened Foes", "enemies have 25% more health and damage" ),
		SWIFT( "Swift Foes", "enemies move 25% faster" ),
		ELITE_PACK( "Elite Pack", "two more elites on every floor" ),
		CROWDED( "Crowded Floors", "30% more enemies on every floor" ),
		WARLORD( "Warlord's Bosses", "every boss carries all four mutations" ),
		RESTLESS( "Restless Dungeon", "half of every floor wakes and hunts you the moment you arrive" ),
		HAUNTED( "Haunted Dungeon", "events happen on 20% more floors" );

		public final String label, effect;
		Pact( String label, String effect ){ this.label = label; this.effect = effect; }
	}

	public static final float LOOT_PER_PACT = 0.10f;

	private static final boolean[] on = new boolean[Pact.values().length];

	public static boolean isOn( Pact p ){ return on[p.ordinal()]; }

	public static void set( Pact p, boolean value ){ on[p.ordinal()] = value; }

	public static void toggle( Pact p ){ on[p.ordinal()] = !on[p.ordinal()]; }

	public static void reset(){ java.util.Arrays.fill( on, false ); }

	public static int activeCount(){
		int n = 0;
		for (boolean b : on) if (b) n++;
		return n;
	}

	/** the tier added to every loot rating in the dungeon */
	public static float lootBonus(){ return LOOT_PER_PACT * activeCount(); }

	/** shards added to a boss's or a bounty's pay */
	public static int shardBonus(){ return activeCount(); }

	public static float hardened(){ return isOn( Pact.HARDENED ) ? 1.25f : 1f; }

	public static float enemySpeed(){ return isOn( Pact.SWIFT ) ? 1.25f : 1f; }

	public static int extraElites(){ return isOn( Pact.ELITE_PACK ) ? 2 : 0; }

	public static float eventBonus(){ return isOn( Pact.HAUNTED ) ? 0.20f : 0f; }

	public static int extraMobs( int mobs ){
		return isOn( Pact.CROWDED ) ? (int)Math.ceil( mobs * 0.30 ) : 0;
	}

	/** a floor of the dungeon is built: more enemies if the floors are crowded */
	public static void crowd( Level level ){
		if (Cycle.number <= 0 || !Cycle.affects( Dungeon.branch, Dungeon.depth ) || !isOn( Pact.CROWDED )) return;
		if (Dungeon.depth % 5 == 0 || Dungeon.depth >= 26) return;   //not the boss floors
		int n = extraMobs( level.mobs.size() );
		for (int i = 0; i < n; i++) ClimbEvents.spawn( level, false );
	}

	/** the hero arrives on a floor: a restless dungeon wakes half of it */
	public static void onArrive( Level level ){
		if (!isOn( Pact.RESTLESS ) || Cycle.number <= 0 || !Cycle.affects( Dungeon.branch, Dungeon.depth ) || Dungeon.hero == null) return;
		for (Mob m : level.mobs){
			if (m.alignment == Char.Alignment.ENEMY && !Char.hasProp( m, Char.Property.BOSS ) && m.sprite != null && Random.Int( 2 ) == 0){
				m.beckon( Dungeon.hero.pos );
			}
		}
	}

	public static void store( Bundle b ){ b.put( "pacts_on", on ); }

	public static void restore( Bundle b ){
		reset();
		if (b.contains( "pacts_on" )){
			boolean[] a = b.getBooleanArray( "pacts_on" );
			for (int i = 0; i < on.length && i < a.length; i++) on[i] = a[i];
		}
	}
}
