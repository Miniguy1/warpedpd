/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sprouted Pixel Dungeon
 * Copyright (C) 2015 dachhack
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
 */

package xyz.gabriwar.warpedpixeldungeon.items.weapon.melee;

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.items.wands.Wand;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** A plain, sturdy staff from the dark mines: a better club than the Mage's own staff, and it takes
 *  a wand the same way. Nothing special, nothing wrong with it. */
public class HollowwoodStaff extends MagesStaff {

	{
		image = ItemSpriteSheet.MAGES_STAFF;
		tier = 2;
		unique = false;
	}

	public HollowwoodStaff() {
		super();
	}

	public HollowwoodStaff(Wand wand) {
		super(wand);
	}

	@Override
	public int max(int lvl) {
		return 4 * (tier + 1) +   //12 base
				lvl * (tier + 1);
	}
}
