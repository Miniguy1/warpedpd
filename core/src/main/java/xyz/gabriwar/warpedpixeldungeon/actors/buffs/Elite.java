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

package xyz.gabriwar.warpedpixeldungeon.actors.buffs;

import xyz.gabriwar.warpedpixeldungeon.Cycle;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.Pacts;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.NPC;
import xyz.gabriwar.warpedpixeldungeon.items.CycleShard;
import xyz.gabriwar.warpedpixeldungeon.items.Gold;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An elite: one of a floor's ordinary enemies, made tougher (two and a half times the health, a champion's
 * aura and twice the experience) in the dungeon of a new cycle. It never drops gear: only gold (often) and
 * a shard (rarely). Its reward is Mob.rollToDropLoot -> reward().
 */
public class Elite extends Buff {

	private boolean announced = false;

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	static final List<Class<? extends Buff>> AFFIXES = Arrays.<Class<? extends Buff>>asList(
			ChampionEnemy.Blazing.class, ChampionEnemy.Projecting.class, ChampionEnemy.AntiMagic.class,
			ChampionEnemy.Giant.class, ChampionEnemy.Blessed.class, ChampionEnemy.Growing.class );

	/** does this floor of the dungeon get elites? Not the boss floors, nor the last */
	public static boolean floorHasElites( int branch, int depth ){
		return Cycle.number > 0 && branch == 0 && depth >= 1 && depth <= 24 && depth % 5 != 0;
	}

	/** how many: one on every floor, a second from floor 10, a third from 18, one more for each cycle after the first (up to two), and the pact's */
	public static int count( int cycle, int depth ){
		return 1 + (depth >= 10 ? 1 : 0) + (depth >= 18 ? 1 : 0) + Math.min( 2, Math.max( 0, cycle - 1 ) ) + Pacts.extraElites();
	}

	private static boolean candidate( Mob m ){
		return m.alignment == Char.Alignment.ENEMY && !(m instanceof NPC)
				&& !Char.hasProp( m, Char.Property.BOSS ) && !Char.hasProp( m, Char.Property.MINIBOSS )
				&& !Char.hasProp( m, Char.Property.IMMOVABLE )
				&& m.buffs( ChampionEnemy.class ).isEmpty() && m.buff( Bounty.class ) == null && m.buff( Elite.class ) == null;
	}

	/** make some of a new floor's enemies elite */
	public static int assign( Level level ){
		if (!floorHasElites( Dungeon.branch, Dungeon.depth )) return 0;
		ArrayList<Mob> pool = new ArrayList<>();
		for (Mob m : level.mobs) if (candidate( m )) pool.add( m );
		if (pool.isEmpty()) return 0;
		Collections.shuffle( pool, new java.util.Random( Random.Long() ) );
		int n = Math.max( 1, Math.min( count( Cycle.number, Dungeon.depth ), (pool.size() + 1) / 2 ) );
		for (int i = 0; i < n && i < pool.size(); i++) make( pool.get( i ) );
		return Math.min( n, pool.size() );
	}

	public static void make( Mob m ){
		m.HT = Math.round( m.HT * 2.5f );
		m.HP = m.HT;
		m.EXP = m.EXP * 2 + 1;
		Buff.affect( m, AFFIXES.get( Random.Int( AFFIXES.size() ) ) );
		Buff.affect( m, Elite.class );
	}

	/** what an elite leaves instead of loot: gold six times in ten, a shard one time in eight */
	public static void reward( Mob mob ){
		if (Dungeon.level == null) return;
		if (Random.Float() < 0.60f){
			int gold = Math.max( 10, Math.round( (20 + 4 * Dungeon.depth) * (1f + 0.5f * Cycle.number) * Random.Float( 0.8f, 1.2f ) ) );
			Dungeon.level.drop( new Gold( gold ), mob.pos );
		}
		if (Random.Float() < 0.125f){
			Dungeon.level.drop( new CycleShard().quantity( 1 + Cycle.number / 2 + Pacts.shardBonus() ), mob.pos );
		}
	}

	@Override
	public boolean act() {
		if (!announced && target != null && Dungeon.level != null && Dungeon.level.heroFOV[target.pos]){
			announced = true;
			GLog.w( "An elite " + target.name() + " stalks this floor." );
		}
		spend( TICK );
		return true;
	}

	private static final String ANNOUNCED = "announced";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ANNOUNCED, announced );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		announced = bundle.getBoolean( ANNOUNCED );
	}
}
