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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Paralysis;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** A hammer that rings like thunder. Heavy and slow, and one hit in five leaves the enemy stunned. */
public class Stormbreaker extends MeleeWeapon {

	{
		image = ItemSpriteSheet.MACE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.8f;

		tier = 5;
		DLY = 1.3f; //~0.77x speed
	}

	@Override
	public int max( int lvl ) {
		return 7 * (tier + 1) +   //42 base
				lvl * (tier + 1);
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (defender != null && defender.isAlive() && Random.Int( 5 ) == 0) {
			Buff.affect( defender, Paralysis.class, 1.5f );
		}
		return super.proc( attacker, defender, damage );
	}
}
