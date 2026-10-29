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

import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.sprites.CharSprite;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Legacy points: one for every run through the dungeon that ends with the climb out, spent on permanent
 * bonuses for the hero (at the dungeon's door). They stay across every cycle.
 */
public final class Legacy {

	private Legacy(){}

	public enum Stat {
		SPEED( "Move speed", "+2% each" ),
		CRIT_CHANCE( "Critical chance", "+2% each" ),
		CRIT_DAMAGE( "Critical damage", "+10% each" ),
		ACCURACY( "Accuracy", "+3% each" ),
		HEALTH( "Health", "+4 max health each" ),
		RESISTANCE( "Resistance", "-1.5% damage taken each" ),
		EVASION( "Evasion", "+3% each" );

		public final String label, per;
		Stat( String label, String per ){ this.label = label; this.per = per; }
	}

	public static final int MAX_PER_STAT = 15;

	private static int unspent = 0, earned = 0;
	private static final int[] spent = new int[Stat.values().length];

	public static void reset(){
		unspent = 0;
		earned = 0;
		java.util.Arrays.fill( spent, 0 );
	}

	public static int unspent(){ return unspent; }
	public static int earned(){ return earned; }
	public static int points( Stat s ){ return spent[s.ordinal()]; }

	/** a run ends: a point */
	public static void award(){
		unspent++;
		earned++;
		GLog.p( "You earned a Legacy point. Spend it at the dungeon's door." );
	}

	public static boolean spend( Stat s ){
		if (unspent <= 0 || spent[s.ordinal()] >= MAX_PER_STAT) return false;
		unspent--;
		spent[s.ordinal()]++;
		if (s == Stat.HEALTH && Dungeon.hero != null) Dungeon.hero.updateHT( true );
		return true;
	}

	// ------------------------------------------------------------------ the bonuses

	public static float speedMultiplier(){ return 1f + 0.02f * points( Stat.SPEED ); }
	public static float accuracyMultiplier(){ return 1f + 0.03f * points( Stat.ACCURACY ); }
	public static float evasionMultiplier(){ return 1f + 0.03f * points( Stat.EVASION ); }
	public static float critChance(){ return 0.02f * points( Stat.CRIT_CHANCE ); }
	public static float critMultiplier(){ return 1.5f + 0.10f * points( Stat.CRIT_DAMAGE ); }
	public static int bonusHP(){ return 4 * points( Stat.HEALTH ); }
	public static float resistance(){ return 0.015f * points( Stat.RESISTANCE ); }

	/** a melee blow about to land: a critical hit now and then */
	public static int outgoing( Hero h, int damage ){
		float c = critChance();
		if (damage <= 0 || c <= 0f || Random.Float() >= c) return damage;
		if (h != null && h.sprite != null) h.sprite.showStatus( CharSprite.WARNING, "crit" );
		return Math.max( 1, Math.round( damage * critMultiplier() ) );
	}

	public static int incoming( int dmg ){
		float r = resistance();
		return dmg <= 0 || r <= 0f ? dmg : Math.max( 1, Math.round( dmg * (1f - r) ) );
	}

	// ------------------------------------------------------------------ saves

	private static final String UNSPENT = "legacy_unspent", EARNED = "legacy_earned", SPENT = "legacy_spent";

	public static void store( Bundle b ){
		b.put( UNSPENT, unspent );
		b.put( EARNED, earned );
		b.put( SPENT, spent );
	}

	public static void restore( Bundle b ){
		reset();
		if (b.contains( UNSPENT )) unspent = b.getInt( UNSPENT );
		if (b.contains( EARNED )) earned = b.getInt( EARNED );
		if (b.contains( SPENT )){
			int[] a = b.getIntArray( SPENT );
			for (int i = 0; i < spent.length && i < a.length; i++) spent[i] = a[i];
		}
	}
}
