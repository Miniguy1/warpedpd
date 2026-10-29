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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bleeding;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** Barbed, blood-dark plate. Whoever strikes the wearer starts to bleed, and the wearer cannot be
 *  made to bleed at all. */
public class BleedingArmor extends EndgameArmor {

	{
		image = ItemSpriteSheet.ARMOR_WARRIOR;
	}

	public BleedingArmor() {
		super( 5 );
	}

	@Override
	public Class[] grantedImmunities() {
		return new Class[]{ Bleeding.class };
	}

	@Override
	public int specialProc( Char attacker, Char defender, int damage ) {
		if (attacker != null && attacker.isAlive() && damage > 0) {
			Buff.affect( attacker, Bleeding.class ).set( Math.max( 3f, damage * 0.25f ) );
		}
		return damage;
	}
}
