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

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/** Marks an enemy with a price on its head: when it falls the hero is paid (Cycle.onDeath). It says so the
 *  first time the hero sees it. */
public class Bounty extends Buff {

	private boolean announced = false;
	/** 1 for a bounty, 2 for a boss rematch: the payout grows with it */
	public int boost = 1;

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	@Override
	public boolean act() {
		if (!announced && target != null && Dungeon.level != null && Dungeon.level.heroFOV[target.pos]) {
			announced = true;
			GLog.w( "The " + target.name() + " is the bounty on this floor. It is stronger than it looks." );
		}
		spend( TICK );
		return true;
	}

	private static final String ANNOUNCED = "announced";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ANNOUNCED, announced );
		bundle.put( "boost", boost );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		announced = bundle.getBoolean( ANNOUNCED );
		boost = bundle.contains( "boost" ) ? bundle.getInt( "boost" ) : 1;
	}
}
