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

import com.watabou.utils.Bundle;

/** the edge a whetstone gives: it lasts for a number of hits, not a number of turns */
public class Sharpened extends Buff {

	private int left = 0;

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public void set( int hits ) {
		left = Math.max( left, hits );
	}

	public int left() {
		return left;
	}

	/** one hit used; the edge is gone after the last */
	public void use() {
		left--;
		if (left <= 0) detach();
	}

	@Override
	public boolean act() {
		spend( TICK );
		return true;
	}

	@Override
	public String desc() {
		return super.desc() + " (" + left + " hits left)";
	}

	private static final String LEFT = "left";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getInt( LEFT );
	}
}
