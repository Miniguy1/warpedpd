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

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Plate studded with barbs. Whoever hits the wearer takes a fifth of the blow right back. */
public class Thornmail extends EndgameArmor {

	{
		image = ItemSpriteSheet.ARMOR_WARRIOR;
	}

	public Thornmail() {
		super( 5 );
	}

	@Override
	public Class[] grantedImmunities() {
		return new Class[0];
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		damage = super.proc( attacker, defender, damage );
		if (attacker != null && attacker.isAlive() && damage > 0 && attacker != defender) {
			attacker.damage( Math.max( 1, Math.round( damage * 0.2f ) ), defender );
		}
		return damage;
	}
}
