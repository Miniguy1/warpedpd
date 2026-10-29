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
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.MobSpawner;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * The bosses of a new cycle mutate. Cycle 1 gives every boss the SUMMONER mutation; cycle 2 a second one, cycle 3 and on a third:
 *  - SUMMONER: calls two more of the floor's enemies every 20 turns.
 *  - FRENZY: below half health it moves twice as fast.
 *  - WARDED: every 30 turns it grows a barrier of a tenth of its health.
 *  - VENGEFUL: when it dies, three enemies spring up where it fell.
 * All the mutations of a boss are one buff (a mask of them).
 */
public class BossMutation extends Buff {

	public static final int SUMMONER = 1, FRENZY = 2, WARDED = 4, VENGEFUL = 8;
	static final String[] NAMES = { "Summoner", "Frenzy", "Warded", "Vengeful" };

	public int mask = 0;
	private int summonTimer = 0, wardTimer = 0;
	private boolean announced = false, frenzied = false;

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	public boolean has( int kind ){ return (mask & kind) != 0; }

	public static String describe( int mask ){
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < NAMES.length; i++){
			if ((mask & (1 << i)) != 0) s.append( s.length() > 0 ? ", " : "" ).append( NAMES[i] );
		}
		return s.toString();
	}

	/** how many mutations a boss has in this cycle: 1, 2, then 3 */
	public static int count( int cycle ){
		return Math.max( 0, Math.min( 3, cycle ) );
	}

	/** every boss on a floor of a later cycle gets its mutations */
	public static void assign( Level level ){
		int n = count( Cycle.number );
		if (n <= 0) return;
		for (Mob m : level.mobs){
			if (!Char.hasProp( m, Char.Property.BOSS ) || Char.hasProp( m, Char.Property.MINIBOSS )) continue;
			if (m.buff( BossMutation.class ) != null) continue;
			ArrayList<Integer> others = new ArrayList<>();
			others.add( FRENZY ); others.add( WARDED ); others.add( VENGEFUL );
			int mask = SUMMONER;
			for (int i = 1; i < n; i++) mask |= others.remove( Random.Int( others.size() ) );
			Buff.affect( m, BossMutation.class ).mask = mask;
		}
	}

	/** enemies of the floor's own kind, beside the boss */
	public static int summon( Char boss, int count ){
		Level level = Dungeon.level;
		ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation( Dungeon.depth );
		if (level == null || rotation == null || rotation.isEmpty()) return 0;
		int made = 0;
		for (int i = 0; i < count; i++){
			Mob m = Reflection.newInstance( Random.element( rotation ) );
			if (m == null) continue;
			int pos = -1;
			for (int d : PathFinder.NEIGHBOURS8){
				int c = boss.pos + d;
				if (c >= 0 && c < level.length() && level.passable[c] && Actor.findChar( c ) == null){ pos = c; break; }
			}
			if (pos == -1) pos = level.randomRespawnCell( m );
			if (pos == -1) continue;
			m.pos = pos;
			GameScene.add( m );
			if (Dungeon.hero != null && m.sprite != null) m.beckon( Dungeon.hero.pos );
			made++;
		}
		return made;
	}

	@Override
	public boolean act() {
		if (target == null || !target.isAlive()){
			spend( TICK );
			return true;
		}
		if (!announced && Dungeon.level != null && Dungeon.level.heroFOV[target.pos]){
			announced = true;
			GLog.n( "The " + target.name() + " has mutated: " + describe( mask ) + "." );
		}
		if (has( SUMMONER ) && ++summonTimer >= 20){
			summonTimer = 0;
			int enemies = 0;
			if (Dungeon.level != null) for (Mob m : Dungeon.level.mobs) if (m.alignment == Char.Alignment.ENEMY) enemies++;
			if (enemies < 14 && summon( target, 2 ) > 0 && Dungeon.level != null && Dungeon.level.heroFOV[target.pos]){
				GLog.w( "The " + target.name() + " calls for help!" );
			}
		}
		if (has( FRENZY ) && target.HP <= target.HT / 2){
			if (!frenzied){
				frenzied = true;
				GLog.n( "The " + target.name() + " goes into a frenzy!" );
			}
			Buff.prolong( target, Haste.class, 3f );
		}
		if (has( WARDED ) && ++wardTimer >= 30){
			wardTimer = 0;
			Buff.affect( target, Barrier.class ).incShield( Math.max( 1, target.HT / 10 ) );
		}
		spend( TICK );
		return true;
	}

	private static final String MASK = "mask", SUMMON = "summonTimer", WARD = "wardTimer", ANNOUNCED = "announced", FRENZIED = "frenzied";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MASK, mask );
		bundle.put( SUMMON, summonTimer );
		bundle.put( WARD, wardTimer );
		bundle.put( ANNOUNCED, announced );
		bundle.put( FRENZIED, frenzied );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		mask = bundle.getInt( MASK );
		summonTimer = bundle.getInt( SUMMON );
		wardTimer = bundle.getInt( WARD );
		announced = bundle.getBoolean( ANNOUNCED );
		frenzied = bundle.getBoolean( FRENZIED );
	}
}
