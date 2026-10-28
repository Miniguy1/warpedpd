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

/** The dune basin: a desert with an oasis and a buried temple, a short walk from the town. */
public class DesertBasinTest {

	private static final long[] SEEDS = { 11, 222, 3333, 44444, 555555, 6666666, 77, 808, 9090, 12121, 313131, 4141414 };
	private final int keep = WorldModel.WORLD_RADIUS;

	@After
	public void restore() {
		WorldModel.WORLD_RADIUS = keep;
	}

	@Test
	public void everyIslandHasABasinWithATempleAndAlmostAlwaysAnOasis() {
		StringBuilder bad = new StringBuilder();
		int noOasis = 0;
		for (long seed : SEEDS) {
			int[] b = DesertBasin.of( seed );
			if (b == null) { bad.append( DesertBasin.explain( seed ) + " || " ); continue; }
			double md = Math.hypot( b[0], b[1] );
			if (md < 185 || md > 415) bad.append( "seed " + seed + ": basin at " + md + "; " );
			WorldStructures.Site temple = WorldStructures.siteType( seed, b[2], b[3] );
			if (temple != WorldStructures.Site.BURIED_TEMPLE) bad.append( DesertBasin.explain( seed ) + " || " );
			if (b[4] == Integer.MIN_VALUE || WorldStructures.siteType( seed, b[4], b[5] ) != WorldStructures.Site.OASIS) { noOasis++; bad.append( "(no oasis " + DesertBasin.explain( seed ) + ") " ); }
		}
		//a crowded island may only have room for the temple
		if (noOasis <= 2) bad = new StringBuilder( bad.toString().replaceAll( "\\(no oasis[^)]*\\) ?", "" ) );
		assertEquals( bad.toString(), 0, bad.length() );
	}

	@Test
	public void theBasinIsDesertAndTheTownIsUntouched() {
		WorldModel.Sample s = new WorldModel.Sample();
		for (long seed : SEEDS) {
			int[] b = DesertBasin.of( seed );
			assertNotNull( "seed " + seed, b );
			WorldModel.sample( seed, b[0], b[1], 0f, s );
			assertEquals( "seed " + seed + ": the heart of the basin", WorldModel.Biome.DESERT, s.biome );
			for (int a = 0; a < 360; a += 45) {
				double r = Math.toRadians( a );
				WorldModel.sample( seed, b[0] + (int)(Math.cos( r ) * 45), b[1] + (int)(Math.sin( r ) * 45), 0f, s );
				assertEquals( "seed " + seed + " bearing " + a + " 45 cells out", WorldModel.Biome.DESERT, s.biome );
			}
			for (int y = -135; y <= 135; y += 15) for (int x = -135; x <= 135; x += 15) {
				if (x * x + y * y > 135 * 135) continue;
				assertEquals( "the basin must not reach the town", 0f, DesertBasin.weight( seed, x, y ), 0f );
			}
			assertEquals( 0f, DesertBasin.weight( seed, b[0] + 95, b[1] ), 0f );
		}
	}

	@Test
	public void theEndlessWorldHasNoBasin() {
		WorldModel.WORLD_RADIUS = 0;
		for (long seed : SEEDS) {
			assertNull( DesertBasin.of( seed ) );
			assertEquals( 0f, DesertBasin.weight( seed, 300, 0 ), 0f );
		}
	}

	@Test
	public void theBasinIsPureAndStaysOnTheIsland() {
		for (long seed : SEEDS) {
			int[] a = DesertBasin.of( seed ), b = DesertBasin.of( seed );
			assertArrayEquals( a, b );
			for (int k = 0; k < 16; k++) {
				double r = k * Math.PI / 8;
				int x = a[0] + (int)(Math.cos( r ) * DesertBasin.R_OUT), y = a[1] + (int)(Math.sin( r ) * DesertBasin.R_OUT);
				assertTrue( "seed " + seed + ": the basin's rim is in the sea (" + WorldModel.edgeMask( seed, x, y ) + ")", WorldModel.edgeMask( seed, x, y ) <= 0.35f );
			}
		}
	}

	@Test
	public void theOasisAndTheTempleLayTheirGround() {
		for (long seed : SEEDS) {
			int[] b = DesertBasin.of( seed );
			assertNotNull( b );
			int tx = WorldStructures.siteX( seed, b[2], b[3] ), ty = WorldStructures.siteY( seed, b[2], b[3] );
			boolean hasOasis = b[4] != Integer.MIN_VALUE;
			int ox = hasOasis ? WorldStructures.siteX( seed, b[4], b[5] ) : 0, oy = hasOasis ? WorldStructures.siteY( seed, b[4], b[5] ) : 0;
			assertTrue( "seed " + seed + ": the temple lays no ground", Drylands.terrain( seed, b[2], b[3], WorldStructures.Site.BURIED_TEMPLE, tx, ty ) != -1
					|| Drylands.terrain( seed, b[2], b[3], WorldStructures.Site.BURIED_TEMPLE, tx, ty - 1 ) != -1 );
			if (hasOasis && WorldStructures.siteType( seed, b[4], b[5] ) == WorldStructures.Site.OASIS) {
				assertTrue( "seed " + seed + ": the oasis lays no ground", Drylands.terrain( seed, b[4], b[5], WorldStructures.Site.OASIS, ox, oy ) != -1 );
			}
			//the window that holds them builds
			WindowGenerator.Window w = WindowGenerator.generate( seed, 0, b[0] - WindowGenerator.WIDTH / 2, b[1] - WindowGenerator.HEIGHT / 2, 0f );
			assertEquals( WindowGenerator.WIDTH * WindowGenerator.HEIGHT, w.terrain.length );
		}
	}
}
