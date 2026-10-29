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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.ChampionEnemy;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.MobSpawner;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * What happens on the floors of the climb in a cycle of 1 or more: each floor rebuilt for the climb may
 * hold one event.
 *
 *  - BOUNTY: a hunter with a price on its head: four times the health, a champion's aura, and a hoard when it falls.
 *  - BLOOD_MOON: every enemy on the floor is 30% stronger, and the floor holds more loot.
 *  - HUNTED: a pack of enemies is set loose and told where the hero is the moment the hero arrives.
 *  - CACHE: a forgotten cache: extra tiered loot, lying about.
 *
 * The event is chosen when the floor is built (roll), kept in the level's save (Level.climbEvent), and
 * announced the first time the hero arrives (announce).
 */
public final class ClimbEvents {

	private ClimbEvents(){}

	public static final int NONE = 0, BOUNTY = 1, BLOOD_MOON = 2, HUNTED = 3, CACHE = 4;

	public static final float BLOOD_MOON_POWER = 1.3f;

	//out of 100: nothing, bounty, blood moon, hunted, cache
	static final int[] WEIGHTS = { 45, 20, 12, 11, 12 };

	public static int pick(){
		int r = Random.Int( 100 );
		for (int e = 0; e < WEIGHTS.length; e++){
			if (r < WEIGHTS[e]) return e;
			r -= WEIGHTS[e];
		}
		return NONE;
	}

	/** may a floor hold an event? Only the dungeon proper, in a cycle, on a floor rebuilt for the climb. */
	public static void roll( Level level ){
		if (Cycle.number <= 0 || Dungeon.branch != 0) return;
		apply( level, pick() );
	}

	public static void apply( Level level, int event ){
		level.climbEvent = event;
		level.climbEventAnnounced = false;
		float rating = Cycle.lootRating( Cycle.number, Dungeon.depth );
		switch (event){
			case BOUNTY:
				spawn( level, true );
				break;
			case BLOOD_MOON:
				EndgameLoot.populate( level, 1f, 0.5f, rating );
				break;
			case HUNTED:
				for (int i = 0; i < 5; i++) spawn( level, false );
				break;
			case CACHE:
				EndgameLoot.populate( level, 1f, 1f, rating + 0.5f );
				EndgameLoot.populate( level, 1f, 0.5f, rating + 0.5f );
				break;
			default:
		}
	}

	/** one enemy native to this depth, placed away from the entrance; a bounty's is made into a hunter */
	static Mob spawn( Level level, boolean bounty ){
		ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation( Dungeon.depth );
		if (rotation == null || rotation.isEmpty()) return null;
		Mob m = Reflection.newInstance( Random.element( rotation ) );
		if (m == null) return null;
		m.pos = level.randomRespawnCell( m );
		if (m.pos == -1) return null;
		if (bounty){
			m.HT = m.HT * 4;
			m.HP = m.HT;
			Buff.affect( m, ChampionEnemy.Giant.class );
			Buff.affect( m, Bounty.class );
		}
		level.mobs.add( m );
		return m;
	}

	/** the first arrival on an event floor: the hero is told, and a hunted floor comes for them */
	public static void announce( Level level ){
		if (level.climbEvent == NONE || level.climbEventAnnounced || Dungeon.hero == null) return;
		level.climbEventAnnounced = true;
		switch (level.climbEvent){
			case BOUNTY:
				GLog.w( "A bounty has been posted on this floor: a hunter with a price on its head." );
				break;
			case BLOOD_MOON:
				GLog.n( "A blood moon hangs over this floor. Everything here is stronger, and more is hidden." );
				break;
			case HUNTED:
				GLog.n( "The floor wakes. Something is coming for you." );
				for (Mob m : level.mobs){
					if (m.alignment == Char.Alignment.ENEMY) m.beckon( Dungeon.hero.pos );
				}
				break;
			case CACHE:
				GLog.p( "A forgotten cache lies somewhere on this floor." );
				break;
			default:
		}
	}
}
