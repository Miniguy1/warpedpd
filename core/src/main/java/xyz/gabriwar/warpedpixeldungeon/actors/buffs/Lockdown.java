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
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.CycleShard;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Gold;
import xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * A lockdown floor: the stairs are sealed and three waves of enemies come at the hero, a wave every 25 turns.
 * Once the third is down (or after 200 turns more, so nothing out of reach can trap the hero) the floor
 * unseals and pays a hoard. The hero keeps regenerating: only the stairs are shut (Level.locked).
 */
public class Lockdown extends Buff {

	public static final int WAVES = 3, GAP = 25, GIVE_UP = 200;

	public int wave = 0;
	private int timer = 0, finishTimer = 0;

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	public static int waveSize( int wave ){
		return 3 + wave;   //4, 5, 6
	}

	private int enemies(){
		int n = 0;
		if (Dungeon.level != null) for (Mob m : Dungeon.level.mobs) if (m.alignment == Char.Alignment.ENEMY && m.isAlive()) n++;
		return n;
	}

	@Override
	public boolean act() {
		Level lv = Dungeon.level;
		if (lv == null || lv.climbEvent != ClimbEvents.LOCKDOWN || !lv.locked){
			detach();
			return true;
		}
		if (wave == 0){
			startWave();
		} else if (wave < WAVES){
			if (++timer >= GAP){
				timer = 0;
				startWave();
			}
		} else if (enemies() == 0 || ++finishTimer >= GIVE_UP){
			finish();
			return true;
		}
		spend( TICK );
		return true;
	}

	private void startWave(){
		wave++;
		int made = BossMutation.summon( target, waveSize( wave ) );
		GLog.n( "The doors are sealed. Wave " + wave + " of " + WAVES + " (" + made + " enemies)!" );
	}

	private void finish(){
		Level lv = Dungeon.level;
		lv.locked = false;
		lv.climbEvent = ClimbEvents.NONE;
		float rating = Cycle.lootRating( Cycle.number, Dungeon.depth ) + 1f;
		for (int i = 0; i < 3; i++) lv.drop( EndgameLoot.rollRated( rating ), target.pos );
		lv.drop( new CycleShard().quantity( 3 + Cycle.number ), target.pos );
		lv.drop( new Gold( Random.NormalIntRange( 100, 200 ) * Math.max( 1, Cycle.number ) ), target.pos );
		GLog.p( "The doors unseal. The floor's spoils are yours." );
		detach();
	}

	private static final String WAVE = "wave", TIMER = "timer", FINISH = "finishTimer";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WAVE, wave );
		bundle.put( TIMER, timer );
		bundle.put( FINISH, finishTimer );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		wave = bundle.getInt( WAVE );
		timer = bundle.getInt( TIMER );
		finishTimer = bundle.getInt( FINISH );
	}
}
