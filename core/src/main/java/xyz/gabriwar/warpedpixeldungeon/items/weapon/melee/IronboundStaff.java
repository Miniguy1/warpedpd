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
 *

package xyz.gabriwar.warpedpixeldungeon.items.weapon.melee;

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.items.wands.Wand;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** The heavy one. A wand-staff that is a real weapon: its swing is as strong as a tier-4 blade, and it
 *  charges its wand just like any other staff. The price is strength (16 at +0), so a weak hero
 *  will swing it badly. The wand's spell power comes from the wand's own level, not from this swing. */
public class IronboundStaff extends MagesStaff {

	{
		image = ItemSpriteSheet.MAGE_STAFF;
		tier = 4;
		unique = false;
	}

	public IronboundStaff() {
		super();
	}

	public IronboundStaff(Wand wand) {
		super(wand);
	}

	@Override
	public int max(int lvl) {
		return 4 * (tier + 1) +   //20 base: a sword's reach, a notch under a pure tier-4 weapon
				lvl * (tier + 1);
	}
}
