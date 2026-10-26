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

/** The end-game robes: plate-class protection that a lighter hero can still wear (3 less Strength
 *  than the same tier normally asks), +2 more armor, and a standing immunity to one kind of harm.
 *  The immunity is read by Char.isImmune() while the armor is worn. */
public abstract class EndgameArmor extends Armor {

	public EndgameArmor( int tier ) {
		super( tier );
	}

	/** the effects the wearer cannot suffer; subclasses of the listed classes count too */
	public abstract Class[] grantedImmunities();

	@Override
	public int STRReq( int lvl ) {
		return Math.max( 8, super.STRReq( lvl ) - 3 );
	}

	@Override
	public int DRMax( int lvl ) {
		return super.DRMax( lvl ) + 2;
	}
}
