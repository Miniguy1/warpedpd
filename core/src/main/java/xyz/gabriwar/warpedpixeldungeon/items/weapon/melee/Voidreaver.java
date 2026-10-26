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

package xyz.gabriwar.warpedpixeldungeon.items.weapon.melee;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** A great black blade, slow and enormous. What it cuts, it keeps: the wielder is healed for a
 *  slice of the damage each blow deals. */
public class Voidreaver extends MeleeWeapon {

	{
		image = ItemSpriteSheet.GREATSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.8f;

		tier = 5;
		DLY = 1.25f; //0.8x speed
	}

	@Override
	public int max( int lvl ) {
		return 7 * (tier + 1) +   //42 base
				lvl * (tier + 1);
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (attacker != null && attacker.isAlive() && damage > 0) {
			attacker.heal( Math.max( 1, Math.round( damage * 0.12f ) ) );
		}
		return super.proc( attacker, defender, damage );
	}
}
