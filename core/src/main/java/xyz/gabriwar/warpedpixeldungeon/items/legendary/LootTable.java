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

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.actors.mobs.*;

/**
 * The legendaries' loot table, ROTMG style: each legendary has its own source creatures. A creature that is in no row can
 * never drop a white bag, and a white bag from a creature can only hold a legendary that creature drops.
 */
public final class LootTable {

	private LootTable(){}

	/** a legendary, then the creatures that drop it (anything that is an instance of one of them counts) */
	private static final Object[][] TABLE = {
		{ Dawnrender.class,        Tengu.class, DwarfKing.class, YogDzewa.class, Monk.class, Minotaur.class },
		{ Widowmaker.class,        Thief.class, ThiefKing.class, BanditKing.class, Scorpio.class },
		{ MantleOfEchoes.class,    Tengu.class, Necromancer.class, Warlock.class, ChaosMage.class, LostSoul.class },
		{ GildedSignet.class,      DwarfKing.class, GoldThief.class, Collector.class, Golem.class },
		{ PhoenixFeather.class,     Succubus.class, DemonLord.class, DemonSpawner.class, YogDzewa.class },
		{ GaleBoots.class,         Bat.class, BrownBat.class, Velocirooster.class, BlueWraith.class },
		{ CarapaceOfTheDeep.class, Goo.class, Crab.class, GreatCrab.class, CrabKing.class, ClayGolem.class },
		{ Venomfang.class,         Snake.class, Spinner.class, SpiderQueen.class, BogNewt.class },
		{ Soulreaver.class,        Skeleton.class, Zombie.class, Ghoul.class, SkeletonKing.class },
		{ Thunderband.class,       DM100.class, DM200.class, DM300.class, Eye.class },
		{ WellspringFlask.class,   Goo.class, Slime.class, SlimeBrown.class, SlimeRed.class, FungalSentry.class },
		{ LodestarCompass.class,   Guard.class, Tinkerer.class, Brute.class, GnollTrickster.class },
		{ BandolierOfPlenty.class, DM300.class, SpiderBot.class, BrokenRobot.class, GnollArcher.class },
		{ AegisOfArrival.class,    CrystalGuardian.class, Oni.class, GreyOni.class, Minotaur.class },
	};

	/** the legendaries this creature can drop (empty for most creatures) */
	public static ArrayList<Class<?>> legendariesFor( Mob mob ) {
		ArrayList<Class<?>> out = new ArrayList<>();
		if (mob == null) return out;
		for (Object[] row : TABLE) {
			for (int i = 1; i < row.length; i++) {
				if (((Class<?>) row[i]).isInstance( mob )) { out.add( (Class<?>) row[0] ); break; }
			}
		}
		return out;
	}

	public static boolean isSource( Mob mob ) {
		return !legendariesFor( mob ).isEmpty();
	}

	/** the creatures that drop one legendary */
	public static ArrayList<Class<?>> sourcesOf( Class<?> legendary ) {
		ArrayList<Class<?>> out = new ArrayList<>();
		for (Object[] row : TABLE) {
			if (row[0] == legendary) for (int i = 1; i < row.length; i++) out.add( (Class<?>) row[i] );
		}
		return out;
	}
}
