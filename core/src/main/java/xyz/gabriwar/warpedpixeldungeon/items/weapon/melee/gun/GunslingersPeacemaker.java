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

import xyz.gabriwar.warpedpixeldungeon.items.GearSets;

/** gunslinger's peacemaker: a piece of the Gunslinger's Rig set. A tier 4 eight-round revolver with a long gold-trimmed barrel. */
public class GunslingersPeacemaker extends Gun {

	{
		image = ItemSpriteSheet.PEACEMAKER;
		tier = 4;
		max_round = 8;
		round = max_round;
		reload_time = 2f;
		shootingAccuracy = 1.3f;
	}

	@Override
	protected int baseBulletMax( int lvl ) {
		return 5 * (tier() + 1) + lvl * (tier() + 1);
	}

	//a set piece asks for the Strength of its tier
	@Override
	public int STRReq( int lvl ) {
		return GearSets.strengthFor( this, lvl );
	}
}
