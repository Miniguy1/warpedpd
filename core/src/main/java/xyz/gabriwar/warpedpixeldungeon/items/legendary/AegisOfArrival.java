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

package xyz.gabriwar.warpedpixeldungeon.items.legendary;

import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;

import xyz.gabriwar.warpedpixeldungeon.items.Item;

/** Legendary, carried: a barrier wraps you every time you arrive on a floor. */
public class AegisOfArrival extends Item {

	{
		image = ItemSpriteSheet.AEGIS_OF_ARRIVAL;
		stackable = false;
		unique = true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	//unique: it cannot be sold
	@Override
	public int value() {
		return 0;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0xFFD24A, 1.6f );
	}
}
