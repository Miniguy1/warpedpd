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

package xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun;

import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Tier 4: five slow, accurate, hard-hitting rounds. */
public class HuntingRifle extends Gun {

	{
		image = ItemSpriteSheet.HUNTING_RIFLE;
		tier = 4;
		max_round = 5;
		round = max_round;
		reload_time = 3f;
		shootingAccuracy = 1.4f;
		shootingSpeed = 1.25f;
	}

	//the same damage as a melee weapon of its tier, spent a round at a time
	@Override
	protected int baseBulletMax( int lvl ) {
		return 5 * (tier() + 1) + lvl * (tier() + 1);
	}
}
