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

import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Monk;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.*;
import xyz.gabriwar.warpedpixeldungeon.ui.BuffIndicator;

/**
 * The Monk's ki: a stack of up to ten (more with training). It builds from every fist that lands and from moving in on a
 * fight (a stack every few steps taken with an enemy in view), and fades when nothing feeds it. Every stack is passive
 * damage and passive protection, and the auras read it.
 */
public class Ki extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private int stacks = 0;
	private int idle = 0;
	private int steps = 0;
	private int lastPos = -1;
	public int renew = 0;

	public int stacks() {
		return stacks;
	}

	/** the most ki the hero can hold */
	public int max() {
		Hero h = target instanceof Hero ? (Hero) target : null;
		return 10 + 2 * Monk.lvl( h, KiFlowing.class ) + 3 * Monk.lvl( h, KiMastery.class )
				+ 2 * Monk.tal( h, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.KI_RESERVE ) + Monk.tal( h, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.KI_MOUNTAIN_HEART );
	}

	/** how many steps with an enemy in view feed one stack: 4, then 3, 2 and every step with Flowing Water */
	public static int stepsPerKi( Hero h ) {
		return Math.max( 1, 4 - Monk.lvl( h, KiFlowingWater.class ) );
	}

	public void gain( int n ) {
		stacks = Math.min( max(), stacks + n );
		idle = 0;
		BuffIndicator.refreshHero();
	}

	public void spend( int n ) {
		stacks = Math.max( 0, stacks - n );
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		if (target instanceof Hero) {
			Hero h = (Hero) target;
			//moving in on a fight feeds ki: a stack for every few steps taken with an enemy in view
			if (lastPos == -1) lastPos = h.pos;
			if (h.pos != lastPos) {
				lastPos = h.pos;
				if (Monk.enemyInView( h ) && ++steps >= stepsPerKi( h )) {
					steps = 0;
					gain( 1 );
				}
			}
			//ki fades when nothing feeds it
			int hold = 10 + 5 * Monk.lvl( h, KiMastery.class ) + 3 * Monk.tal( h, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.KI_ENDURING );
			if (++idle > hold) {
				idle = hold - 4;
				if (stacks > 0) spend( 1 );
			}
			Monk.auras( h, this );
		}
		spend( TICK );
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.COMBO;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 1f, 0.6f, 0.15f );
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString( stacks );
	}

	@Override
	public String desc() {
		return xyz.gabriwar.warpedpixeldungeon.messages.Messages.get( this, "desc", stacks, max() );
	}

	private static final String STACKS = "stacks", IDLE = "idle", STEPS = "steps", LASTPOS = "lastpos", RENEW = "renew";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( STACKS, stacks ); bundle.put( IDLE, idle ); bundle.put( STEPS, steps ); bundle.put( LASTPOS, lastPos ); bundle.put( RENEW, renew );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		stacks = bundle.getInt( STACKS ); idle = bundle.getInt( IDLE ); steps = bundle.getInt( STEPS ); lastPos = bundle.getInt( LASTPOS ); renew = bundle.getInt( RENEW );
	}
}
