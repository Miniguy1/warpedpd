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
 * The Monk's ki: a stack of up to ten (more with training). It builds from every fist that lands and from standing still,
 * and fades when nothing feeds it. Every stack is passive damage and passive protection, and the auras read it.
 */
public class Ki extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private int stacks = 0;
	private int idle = 0;
	private int still = 0;
	private int lastPos = -1;
	public int renew = 0;

	public int stacks() {
		return stacks;
	}

	/** the most ki the hero can hold */
	public int max() {
		Hero h = target instanceof Hero ? (Hero) target : null;
		return 10 + 2 * Monk.lvl( h, KiFlowing.class ) + 3 * Monk.lvl( h, KiMastery.class );
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
			//standing still is meditation: a stack every two turns (every turn with still water)
			if (h.pos == lastPos) {
				if (++still >= 2) {
					still = 0;
					gain( 1 + (Monk.lvl( h, KiStillWater.class ) >= 2 ? 1 : 0) );
				}
			} else {
				still = 0;
				lastPos = h.pos;
			}
			//ki fades when nothing feeds it
			int hold = 10 + 5 * Monk.lvl( h, KiMastery.class );
			if (++idle > hold) {
				idle = hold - 4;
				if (stacks > 0) spend( 1 );
			}
			Monk.auras( h, this );
		}
		spend( TICK );
		return true;
	}

	/** has the hero held their ground this turn? */
	public boolean holdingGround() {
		return still > 0 || (target != null && target.pos == lastPos && idle > 0);
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

	private static final String STACKS = "stacks", IDLE = "idle", STILL = "still", LASTPOS = "lastpos", RENEW = "renew";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( STACKS, stacks ); bundle.put( IDLE, idle ); bundle.put( STILL, still ); bundle.put( LASTPOS, lastPos ); bundle.put( RENEW, renew );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		stacks = bundle.getInt( STACKS ); idle = bundle.getInt( IDLE ); still = bundle.getInt( STILL ); lastPos = bundle.getInt( LASTPOS ); renew = bundle.getInt( RENEW );
	}
}
