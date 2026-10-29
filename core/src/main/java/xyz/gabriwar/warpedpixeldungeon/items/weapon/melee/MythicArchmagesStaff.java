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

package xyz.gabriwar.warpedpixeldungeon.items.weapon.melee;

import xyz.gabriwar.warpedpixeldungeon.items.SetTier;

/** A tier 8 (Mythic) ArchmagesStaff: the same piece, stronger. */
public class MythicArchmagesStaff extends ArchmagesStaff implements SetTier {

	public MythicArchmagesStaff() {
		super();
	}

	public MythicArchmagesStaff( xyz.gabriwar.warpedpixeldungeon.items.wands.Wand wand ) {
		super( wand );
	}

	@Override
	public int setTier() {
		return 8;
	}

	@Override
	public int max( int lvl ) {
		return Math.round( super.max( lvl ) * 1.5f );
	}
}
