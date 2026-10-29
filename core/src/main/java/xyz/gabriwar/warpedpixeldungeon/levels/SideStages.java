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

import xyz.gabriwar.warpedpixeldungeon.Cycle;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;

/**
 * The bonus stages hanging off the main dungeon: the Vault (the city's branch, depths 16 to 19) and the
 * Frozen region (depths 21 to 24, and its boss on 25). They pay rating 8 loot: tier 8 in a new cycle, and in
 * the first run the best it allows (tier 7) with bonus levels for the rest (EndgameLoot.maxTier). They are
 * made harder to match: every enemy there has more health and hits harder than the floor of the same
 * depth in the main dungeon, a little more with each floor, and the Frozen region more than the Vault.
 * The factor rides on the shared enemy-strength function (AscensionChallenge.statModifier).
 */
public final class SideStages {

	private SideStages(){}

	/** the rating of a bonus stage's loot */
	public static final float RATING = 8.0f;
	/** the rating of the ice boss's hoard */
	public static final float BOSS_RATING = 8.6f;

	/** a bonus stage's loot rating: 8, or in a new cycle half a tier above what the cycle's own floors pay
	 *  there, so a harder cycle's Vault pays more */
	public static float ratingAt( int cycle, int depth ){
		return cycle > 0 ? Math.max( RATING, Math.min( 9f, Cycle.lootRating( cycle, depth ) + 0.5f ) ) : RATING;
	}

	/** the ice boss's hoard: 8.6, or a tier and a half above the cycle's floor there */
	public static float bossRatingAt( int cycle, int depth ){
		return cycle > 0 ? Math.max( BOSS_RATING, Math.min( 9f, Cycle.lootRating( cycle, depth ) + 1.5f ) ) : BOSS_RATING;
	}

	public static boolean isVault( int branch, int depth ){
		return branch == 1 && depth >= 16 && depth <= 19;
	}

	public static boolean isFrozen( int branch, int depth ){
		return branch == 2 && depth >= 21 && depth <= 25;
	}

	/** the health / damage factor of an enemy on this floor of a bonus stage, 1 anywhere else */
	public static float powerAt( int branch, int depth ){
		if (isVault( branch, depth )) return 1.15f + 0.04f * (depth - 16);
		if (isFrozen( branch, depth )) return 1.20f + 0.04f * (depth - 21);
		return 1f;
	}

	/** the accuracy / evasion factor: half as steep */
	public static float skillAt( int branch, int depth ){
		return 1f + (powerAt( branch, depth ) - 1f) * 0.5f;
	}

	private static boolean scaled( Char ch ){
		return ch instanceof Mob && ch.alignment == Char.Alignment.ENEMY;
	}

	public static float power( Char ch ){
		return scaled( ch ) ? powerAt( Dungeon.branch, Dungeon.depth ) : 1f;
	}

	public static float skill( Char ch ){
		return scaled( ch ) ? skillAt( Dungeon.branch, Dungeon.depth ) : 1f;
	}

	/** a bonus stage's floors hold tier 8 loot (the ice boss pays out when it falls: bossHoard) */
	public static void afterCreate( Level level ){
		if (isVault( Dungeon.branch, Dungeon.depth )){
			EndgameLoot.populate( level, 0.75f, 0.35f, ratingAt( Cycle.number, Dungeon.depth ) );
		} else if (isFrozen( Dungeon.branch, Dungeon.depth ) && Dungeon.depth < 25){
			EndgameLoot.populate( level, 0.65f, 0.30f, ratingAt( Cycle.number, Dungeon.depth ) );
		}
	}

	/** the ice boss's hoard: three new items, tier 8 and sometimes 9, and a piece of the hero's own set */
	public static void bossHoard( Level level, int cell ){
		for (int i = 0; i < 3; i++){
			level.drop( EndgameLoot.rollRated( bossRatingAt( Cycle.number, Dungeon.depth ) ), cell );
		}
		if (Dungeon.hero != null && Dungeon.hero.heroClass != null){
			level.drop( EndgameLoot.rollClassSet( Dungeon.hero.heroClass, EndgameLoot.tierOfRating( bossRatingAt( Cycle.number, Dungeon.depth ) ) ), cell );
		}
	}
}
