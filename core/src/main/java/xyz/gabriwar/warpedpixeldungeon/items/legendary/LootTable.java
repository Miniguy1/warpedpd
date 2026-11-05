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

package xyz.gabriwar.warpedpixeldungeon.items.legendary;

import com.watabou.utils.Random;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.*;
import xyz.gabriwar.warpedpixeldungeon.items.field.WhiteBag;

/**
 * The legendaries' loot table, ROTMG style: each legendary has a power RANK (I to IV, not a gear tier) and its own source
 * creatures. The harder the creature, the stronger the legendary it can drop and the rarer that drop is:
 * each legendary rolls its own chance, the killer's base (a boss 10%, an elite 2%, anything else 0.2%) times the rank
 * (I x1, II x0.6, III x0.35, IV x0.2). A creature in no row can never drop a white bag.
 */
public final class LootTable {

	private LootTable(){}

	/** a marker source: any elite at all */
	public static final class AnyElite {}
	public static final Class<?> ANY_ELITE = AnyElite.class;

	/** a legendary, its rank, then the creatures that drop it (anything that is an instance of one of them counts) */
	private static final Object[][] TABLE = {
		//rank I: modest but real, and some come from any elite: they are there to help the power creep
		{ WellspringFlask.class,   1, Goo.class, Slime.class, SlimeBrown.class, SlimeRed.class, FungalSentry.class, ANY_ELITE },
		{ LodestarCompass.class,   1, Guard.class, Tinkerer.class, Brute.class, GnollTrickster.class, ANY_ELITE },
		{ GaleBoots.class,         1, Bat.class, BrownBat.class, Velocirooster.class, BlueWraith.class, ANY_ELITE },
		{ Venomfang.class,         1, Snake.class, Spinner.class, SpiderQueen.class, BogNewt.class, Scorpio.class },
		{ GildedSignet.class,      1, Thief.class, GoldThief.class, Collector.class, Golem.class, DwarfKing.class },
		//rank II: from the early bosses and the tougher creatures
		{ CarapaceOfTheDeep.class, 2, Goo.class, Crab.class, GreatCrab.class, CrabKing.class, ClayGolem.class },
		{ Soulreaver.class,        2, Tengu.class, Skeleton.class, Zombie.class, Ghoul.class, SkeletonKing.class },
		{ BandolierOfPlenty.class, 2, DM300.class, DM200.class, SpiderBot.class, BrokenRobot.class, GnollArcher.class },
		{ AegisOfArrival.class,    2, Tengu.class, CrystalGuardian.class, Oni.class, GreyOni.class, Minotaur.class },
		//rank III: from the middle bosses
		{ Widowmaker.class,        3, ThiefKing.class, BanditKing.class, Scorpio.class, GoldThief.class },
		{ Thunderband.class,       3, DM300.class, DM200.class, DM100.class, Eye.class },
		{ MantleOfEchoes.class,    3, DwarfKing.class, SpiderQueen.class, Necromancer.class, Warlock.class, ChaosMage.class, LostSoul.class },
		{ Dawnrender.class,        3, DwarfKing.class, YogDzewa.class, DemonLord.class, Monk.class, Minotaur.class },
		//rank IV: the last bosses
		{ PhoenixFeather.class,    4, YogDzewa.class, DemonLord.class, Succubus.class, DemonSpawner.class },
	};

	private static final int FIRST_SOURCE = 2;

	public static int rankOf( Class<?> legendary ) {
		for (Object[] row : TABLE) if (row[0] == legendary) return (Integer) row[1];
		return 1;
	}

	/** how much rarer each rank is */
	public static float rankFactor( int rank ) {
		return rank <= 1 ? 1f : rank == 2 ? 0.6f : rank == 3 ? 0.35f : 0.2f;
	}

	private static boolean matches( Object source, Mob mob ) {
		if (source == ANY_ELITE) return mob.buff( Elite.class ) != null;
		return ((Class<?>) source).isInstance( mob );
	}

	/** the legendaries this creature can drop (empty for most creatures) */
	public static ArrayList<Class<?>> legendariesFor( Mob mob ) {
		ArrayList<Class<?>> out = new ArrayList<>();
		if (mob == null) return out;
		for (Object[] row : TABLE) {
			for (int i = FIRST_SOURCE; i < row.length; i++) {
				if (matches( row[i], mob )) { out.add( (Class<?>) row[0] ); break; }
			}
		}
		return out;
	}

	public static boolean isSource( Mob mob ) {
		return !legendariesFor( mob ).isEmpty();
	}

	/** the creatures that drop one legendary (ANY_ELITE stands for every elite) */
	public static ArrayList<Class<?>> sourcesOf( Class<?> legendary ) {
		ArrayList<Class<?>> out = new ArrayList<>();
		for (Object[] row : TABLE) {
			if (row[0] == legendary) for (int i = FIRST_SOURCE; i < row.length; i++) out.add( (Class<?>) row[i] );
		}
		return out;
	}

	/** the killer's base chance: a boss 10%, an elite (or mini-boss) 2%, anything else 0.2% */
	public static float baseChance( Mob mob ) {
		int s = WhiteBag.sourceOf( mob );
		return s == WhiteBag.BOSS ? 0.10f : s == WhiteBag.ELITE ? 0.02f : s == WhiteBag.NORMAL ? 0.002f : 0f;
	}

	/** the chance this creature drops this legendary on a kill */
	public static float chanceFor( Mob mob, Class<?> legendary ) {
		return baseChance( mob ) * rankFactor( rankOf( legendary ) );
	}

	/** a kill: each legendary this creature drops, and the hero does not have, rolls its own chance. Returns the ones that dropped. */
	public static ArrayList<Class<?>> rollDrops( Mob mob ) {
		Hero h = Dungeon.hero;
		ArrayList<Class<?>> won = new ArrayList<>();
		for (Class<?> c : legendariesFor( mob )) {
			if (!Legendary.carried( h, c ) && Random.Float() < chanceFor( mob, c )) won.add( c );
		}
		return won;
	}

	/** "Dwarf King, Tengu, any elite" */
	public static String sourceNames( Class<?> legendary ) {
		StringBuilder sb = new StringBuilder();
		for (Class<?> c : sourcesOf( legendary )) {
			if (sb.length() > 0) sb.append( ", " );
			sb.append( c == ANY_ELITE ? "any elite" : c.getSimpleName().replaceAll( "([a-z0-9])([A-Z])", "$1 $2" ) );
		}
		return sb.toString();
	}
}
