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

/** Tier 1: a two-shot pocket pistol, quick to reload. */
public class Derringer extends Gun {

	{
		image = ItemSpriteSheet.DERRINGER;
		tier = 1;
		max_round = 2;
		round = max_round;
		reload_time = 1f;
		shootingAccuracy = 1.15f;
	}

	//the same damage as a melee weapon of its tier, spent a round at a time
	@Override
	protected int baseBulletMax( int lvl ) {
		return 5 * (tier() + 1) + lvl * (tier() + 1);
	}
}
