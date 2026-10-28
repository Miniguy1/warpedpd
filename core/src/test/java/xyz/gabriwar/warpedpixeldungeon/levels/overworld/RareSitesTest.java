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

package xyz.gabriwar.warpedpixeldungeon.levels.overworld;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/** The island's rare places turn up often enough to find. */
public class RareSitesTest {

	private static final long[] SEEDS = { 11, 222, 3333, 44444, 555555, 6666666, 77, 808, 9090, 12121, 313131, 4141414 };
	private final int keep = WorldModel.WORLD_RADIUS;

	@After
	public void restore() {
		WorldModel.WORLD_RADIUS = keep;
	}

	private static int count( long seed, WorldStructures.Site kind ) {
		int n = 0;
		for (int sy = -6; sy <= 6; sy++) for (int sx = -6; sx <= 6; sx++) {
			if (sx == 0 && sy == 0) continue;
			if (Math.hypot( WorldStructures.siteX( seed, sx, sy ), WorldStructures.siteY( seed, sx, sy ) ) > 470) continue;
			if (WorldStructures.siteType( seed, sx, sy ) == kind) n++;
		}
		return n;
	}

	@Test
	public void theIslandHoldsTheRarePlaces() {
		int dragon = 0, tree = 0, stones = 0, temple = 0, villages = 0, gates = 0;
		StringBuilder per = new StringBuilder();
		for (long seed : SEEDS) {
			int dr = count( seed, WorldStructures.Site.DRAGON ), bt = count( seed, WorldStructures.Site.BIGTREE ), st = count( seed, WorldStructures.Site.STONES );
			if (dr > 0) dragon++;
			if (bt > 0) tree++;
			if (st > 0) stones++;
			if (count( seed, WorldStructures.Site.BURIED_TEMPLE ) > 0) temple++;
			if (count( seed, WorldStructures.Site.VILLAGE ) >= 3) villages++;
			if (count( seed, WorldStructures.Site.DUNGEON ) >= 1) gates++;
			per.append( seed + ":" + dr + "/" + bt + "/" + st + " " );
		}
		String info = " (dragon/tree/stones per world: " + per + ")";
		assertTrue( "dragon caves in " + dragon + "/12" + info, dragon >= 10 );
		assertTrue( "great oaks in " + tree + "/12" + info, tree >= 8 );
		assertTrue( "stone circles in " + stones + "/12" + info, stones >= 9 );
		assertEquals( "a buried temple in every world", 12, temple );
		assertTrue( "villages in " + villages + "/12", villages >= 11 );
		assertTrue( "dungeon gates in " + gates + "/12", gates >= 10 );
	}

	@Test
	public void theEndlessWorldKeepsItsOldOdds() {
		WorldModel.WORLD_RADIUS = 0;
		int dragons = 0;
		for (long seed : SEEDS) {
			for (int sy = -8; sy <= 8; sy++) for (int sx = -8; sx <= 8; sx++) {
				if (WorldStructures.siteType( seed, sx, sy ) == WorldStructures.Site.DRAGON) dragons++;
			}
		}
		//one in sixteen of the high sectors: a handful in 12 worlds of 17x17 sectors, never dozens
		assertTrue( "dragons in the endless world: " + dragons, dragons < 90 );
	}
}
