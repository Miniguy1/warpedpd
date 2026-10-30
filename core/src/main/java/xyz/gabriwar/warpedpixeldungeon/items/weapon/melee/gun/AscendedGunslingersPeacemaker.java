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

import xyz.gabriwar.warpedpixeldungeon.items.SetTier;

/** A tier 7 (Ascended) GunslingersPeacemaker: the same piece, stronger. */
public class AscendedGunslingersPeacemaker extends GunslingersPeacemaker implements SetTier {

	@Override
	public int setTier() {
		return 7;
	}

	@Override
	public int bulletMin( int lvl ) {
		return Math.round( super.bulletMin( lvl ) * 1.25f );
	}

	@Override
	public int bulletMax( int lvl ) {
		return Math.round( super.bulletMax( lvl ) * 1.25f );
	}
}
