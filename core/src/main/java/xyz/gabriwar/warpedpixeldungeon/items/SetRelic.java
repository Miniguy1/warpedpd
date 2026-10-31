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

package xyz.gabriwar.warpedpixeldungeon.items;

import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** A carried piece of a gear set: it does nothing alone, but it counts toward its set for as long as
 *  it is in your pack. */
public abstract class SetRelic extends Item {

	{
		image = ItemSpriteSheet.AMULET;
		stackable = false;
	}

	/** the colour it glows (each set has its own) */
	protected int glow = 0xFFFFFF;

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( glow );
	}

	/** a carried relic is worth gold like any other gear (it was worth nothing, so no shop would take it): 30 at tier 6, up to 60 at tier 9 */
	@Override
	public int value() {
		return 20 + 10 * (Math.max( 5, GearSets.gearTier( this ) ) - 5);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
