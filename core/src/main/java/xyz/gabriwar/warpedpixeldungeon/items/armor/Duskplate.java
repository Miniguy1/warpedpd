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

package xyz.gabriwar.warpedpixeldungeon.items.armor;

import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Dark, light plate: not quite plate's protection, but it asks 5 less Strength than ordinary
 *  armor of its tier, so almost anyone can wear it. No tricks. */
public class Duskplate extends EndgameArmor {

	{
		image = ItemSpriteSheet.ARMOR_ROGUE;
	}

	public Duskplate() {
		super( 4 );
	}

	@Override
	public Class[] grantedImmunities() {
		return new Class[0];
	}

}
