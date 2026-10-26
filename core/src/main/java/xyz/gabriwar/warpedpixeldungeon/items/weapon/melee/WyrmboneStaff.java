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

/** Light and fast to charge: the wand inside holds one extra charge and recharges at the wand's full
 *  rate (other staffs recharge at three quarters of it). The swing is as weak as a Mage's staff. */
public class WyrmboneStaff extends MagesStaff {

	{
		image = ItemSpriteSheet.MAGES_STAFF;
		tier = 1;
		unique = false;
	}

	public WyrmboneStaff() {
		super();
	}

	public WyrmboneStaff(Wand wand) {
		super(wand);
	}

	@Override
	public void updateWand(boolean levelled) {
		super.updateWand(levelled);
		Wand w = staffWand();
		if (w != null) {
			//MagesStaff re-levels the wand first (which resets its charges), so this never stacks up
			w.maxCharges = Math.min(w.maxCharges + 1, 10);
			w.curCharges = Math.min(w.curCharges + (levelled ? 1 : 0), w.maxCharges);
		}
	}

	@Override
	public void applyWandChargeBuff(Char owner) {
		Wand w = staffWand();
		if (w != null) {
			w.charge(owner, 1f);
		}
	}
}
