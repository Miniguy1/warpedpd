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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Chill;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Frost;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** Cold glass in the shape of a robe. The wearer can be neither chilled nor frozen, and whoever
 *  strikes it is sometimes chilled by the contact. */
public class RobeOfShattering extends EndgameArmor {

	{
		image = ItemSpriteSheet.ARMOR_CLOTH;
	}

	public RobeOfShattering() {
		super( 5 );
	}

	@Override
	public Class[] grantedImmunities() {
		return new Class[]{ Frost.class, Chill.class };
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		damage = super.proc( attacker, defender, damage );
		if (attacker != null && attacker.isAlive() && damage > 0 && Random.Int( 5 ) == 0) {
			Buff.affect( attacker, Chill.class, 4f );
		}
		return damage;
	}
}
