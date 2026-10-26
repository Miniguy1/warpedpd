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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** A long blade that holds a little of the morning in it. Swings at a sword's pace, and a third of
 *  the time the wound it leaves catches fire. */
public class Dawnbreaker extends MeleeWeapon {

	{
		image = ItemSpriteSheet.LONGSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 4;
	}

	@Override
	public int max( int lvl ) {
		return 6 * (tier + 1) +   //30 base
				lvl * (tier + 1);
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (defender != null && defender.isAlive() && Random.Int( 3 ) == 0) {
			Buff.affect( defender, Burning.class ).reignite( defender );
		}
		return super.proc( attacker, defender, damage );
	}
}
