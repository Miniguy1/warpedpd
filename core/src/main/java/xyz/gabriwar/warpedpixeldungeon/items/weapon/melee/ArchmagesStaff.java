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

import xyz.gabriwar.warpedpixeldungeon.items.wands.Wand;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** archmage's staff: a piece of the Archmage's Vestments set. A staff that takes a wand like the Mage's own. */
public class ArchmagesStaff extends MagesStaff {

	{
		image = ItemSpriteSheet.MAGES_STAFF;
		tier = 3;
		unique = false;
	}

	public ArchmagesStaff() {
		super();
	}

	public ArchmagesStaff( Wand wand ) {
		super( wand );
	}

	@Override
	public int max( int lvl ) {
		return 4 * (tier + 1) +   //16 base
				lvl * (tier + 1);
	}
}
