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

import xyz.gabriwar.warpedpixeldungeon.items.armor.Armor;

/** Legendary: a 15% chance to turn a blow aside completely, and 30% of the damage you do take echoes back at the attacker. Only a white bag drops it. */
public class MantleOfEchoes extends Armor {

	{
		image = ItemSpriteSheet.MANTLE_OF_ECHOES;
	}

	public MantleOfEchoes() {
		super( 5 );
	}

	//unique: it cannot be sold
	@Override
	public int value() {
		return 0;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( Legendary.glowFor( getClass() ), 1.6f );
	}

	@Override
	public String desc() {
		return super.desc() + "\n\n" + Legendary.rankLine( getClass() );
	}
}
