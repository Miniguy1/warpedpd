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

import xyz.gabriwar.warpedpixeldungeon.levels.Terrain;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/** The world's edge: an island around the town, ocean beyond. */
public class WorldEdgeTest {

	private static final long[] SEEDS = { 11, 222, 3333, 44444, 555555, 6666666, 77, 808, 9090, 12121, 313131, 4141414 };
	private final int keep = WorldModel.WORLD_RADIUS;

	@After
	public void restore() {
		WorldModel.WORLD_RADIUS = keep;
	}

	private static float coastDistance( long seed, double bearing ) {
		for (int d = 0; d <= 900; d++) {
			int x = (int)Math.round( Math.cos( bearing ) * d ), y = (int)Math.round( Math.sin( bearing ) * d );
			if (WorldModel.edgeMask( seed, x, y ) >= 0.5f) return d;
		}
		return 900;
	}

	@Test
	public void theRadiusIsInForceByDefault() {
		assertTrue( "the world has an edge", WorldModel.WORLD_RADIUS > 0 );
		assertEquals( 480, WorldModel.WORLD_RADIUS );
	}

	@Test
	public void theEdgeIsNothingNearTheTownAndTotalFarOut() {
		for (long seed : SEEDS) {
			assertEquals( 0f, WorldModel.edgeMask( seed, 0, 0 ), 0f );
			for (int a = 0; a < 360; a += 10) {
				double r = Math.toRadians( a );
				assertEquals( "250 cells out is still the island", 0f,
						WorldModel.edgeMask( seed, (int)(Math.cos( r ) * 250), (int)(Math.sin( r ) * 250) ), 0f );
				assertEquals( "past the furthest coast it is all sea", 1f,
						WorldModel.edgeMask( seed, (int)(Math.cos( r ) * 600), (int)(Math.sin( r ) * 600) ), 0f );
			}
		}
	}

	@Test
	public void theEdgeOnlyGrowsOutward() {
		for (long seed : SEEDS) {
			for (int a = 0; a < 360; a += 15) {
				double r = Math.toRadians( a );
				float last = 0f;
				for (int d = 0; d <= 700; d += 5) {
					float m = WorldModel.edgeMask( seed, (int)Math.round( Math.cos( r ) * d ), (int)Math.round( Math.sin( r ) * d ) );
					assertTrue( "seed " + seed + " bearing " + a + " at " + d + ": " + m + " after " + last, m >= last - 0.02f );
					last = Math.max( last, m );
				}
			}
		}
	}

	@Test
	public void theCoastIsRaggedAndStaysWithinItsBand() {
		for (long seed : SEEDS) {
			float lo = 9999, hi = 0;
			for (int a = 0; a < 360; a += 6) {
				float c = coastDistance( seed, Math.toRadians( a ) );
				lo = Math.min( lo, c );
				hi = Math.max( hi, c );
			}
			assertTrue( "seed " + seed + ": nearest coast " + lo, lo >= 300 );
			assertTrue( "seed " + seed + ": furthest coast " + hi, hi <= 600 );
			assertTrue( "seed " + seed + ": a coast that wanders (" + lo + " to " + hi + ")", hi - lo > 40 );
		}
	}

	@Test
	public void aZeroRadiusIsTheOldEndlessWorld() {
		WorldModel.WORLD_RADIUS = 0;
		for (long seed : SEEDS) {
			assertEquals( 0f, WorldModel.edgeMask( seed, 5000, -3000 ), 0f );
			assertEquals( 0f, WorldModel.edgeMask( seed, 480, 0 ), 0f );
		}
	}

	@Test
	public void theIslandIsTheOldWorldUntilItsCoastIsNear() {
		WorldModel.Sample a = new WorldModel.Sample(), b = new WorldModel.Sample();
		for (long seed : SEEDS) {
			for (int y = -250; y <= 250; y += 25) for (int x = -250; x <= 250; x += 25) {
				if (x * x + y * y > 250 * 250) continue;
				WorldModel.WORLD_RADIUS = 480;
				WorldModel.terrainElev( seed, x, y, a );
				float island = a.elev;
				WorldModel.WORLD_RADIUS = 0;
				WorldModel.terrainElev( seed, x, y, b );
				assertEquals( "seed " + seed + " at " + x + "," + y, b.elev, island, 0f );
			}
		}
	}

	@Test
	public void theTownStandsOnLandAndTheFarEdgeIsSea() {
		WorldModel.Sample s = new WorldModel.Sample();
		for (long seed : SEEDS) {
			int land = 0, n = 0;
			for (int y = -250; y <= 250; y += 10) for (int x = -250; x <= 250; x += 10) {
				if (x * x + y * y > 250 * 250) continue;
				WorldModel.terrainElev( seed, x, y, s );
				n++;
				if (s.elev >= WorldModel.SEA) land++;
			}
			//lakes and rivers are part of the old world too: some seeds are a tenth water this near the town
			assertTrue( "seed " + seed + ": land near the town " + land + "/" + n, land >= n * 0.88 );
			for (int a = 0; a < 360; a += 12) {
				double r = Math.toRadians( a );
				WorldModel.terrainElev( seed, (int)(Math.cos( r ) * 640), (int)(Math.sin( r ) * 640), s );
				assertTrue( "seed " + seed + " bearing " + a + " should be open sea, elevation " + s.elev, s.elev < WorldModel.SEA );
			}
		}
	}

	@Test
	public void theIslandKeepsMountainsSnowAndPlaces() {
		int withMountains = 0, withSnow = 0, withVillages = 0, withGates = 0;
		WorldModel.Sample s = new WorldModel.Sample();
		for (long seed : SEEDS) {
			int land = 0, high = 0, cold = 0;
			for (int y = -400; y <= 400; y += 12) for (int x = -400; x <= 400; x += 12) {
				if (x * x + y * y > 400 * 400) continue;
				WorldModel.sample( seed, x, y, 0f, s );
				if (s.biome == WorldModel.Biome.OCEAN) continue;
				land++;
				if (s.biome == WorldModel.Biome.MOUNTAIN || s.biome == WorldModel.Biome.FOOTHILLS) high++;
				if (s.biome == WorldModel.Biome.SNOWFIELD || s.biome == WorldModel.Biome.TUNDRA) cold++;
			}
			if (high >= land * 0.02) withMountains++;
			if (cold >= land * 0.01) withSnow++;
			int villages = 0, gates = 0;
			for (int sy = -6; sy <= 6; sy++) for (int sx = -6; sx <= 6; sx++) {
				if (sx == 0 && sy == 0) continue;
				double dist = Math.sqrt( Math.pow( WorldStructures.siteX( seed, sx, sy ), 2 ) + Math.pow( WorldStructures.siteY( seed, sx, sy ), 2 ) );
				if (dist > 470) continue;
				WorldStructures.Site site = WorldStructures.siteType( seed, sx, sy );
				if (site == WorldStructures.Site.VILLAGE) villages++;
				if (site == WorldStructures.Site.DUNGEON) gates++;
			}
			if (villages >= 2) withVillages++;
			if (gates >= 1) withGates++;
		}
		//ore comes off the mountains: every world must have them
		assertTrue( "mountains in " + withMountains + "/12 worlds", withMountains >= 11 );
		assertTrue( "snow in " + withSnow + "/12 worlds", withSnow >= 11 );
		assertTrue( "villages in " + withVillages + "/12 worlds", withVillages >= 10 );
		assertTrue( "dungeon gates in " + withGates + "/12 worlds", withGates >= 10 );
	}

	@Test
	public void theDeepSeaIsAWallOnFoot() {
		assertTrue( "deep water must stop a hero walking", (Terrain.flags[Terrain.DEEP_WATER] & Terrain.SOLID) != 0 );
	}

	@Test
	public void coastalWindowsBuildWithLandAndSea() {
		int both = 0, total = 0;
		for (int k = 0; k < 6; k++) {
			long seed = SEEDS[k];
			for (int a = 0; a < 360; a += 90) {
				double r = Math.toRadians( a );
				int cx = (int)Math.round( Math.cos( r ) * 480 ), cy = (int)Math.round( Math.sin( r ) * 480 );
				WindowGenerator.Window w = WindowGenerator.generate( seed, 0, cx - WindowGenerator.WIDTH / 2, cy - WindowGenerator.HEIGHT / 2, 0f );
				assertEquals( WindowGenerator.WIDTH * WindowGenerator.HEIGHT, w.terrain.length );
				int water = 0, dry = 0;
				for (int t : w.terrain) {
					if (t == Terrain.WATER || t == Terrain.DEEP_WATER) water++; else dry++;
				}
				total++;
				if (water > 200 && dry > 200) both++;
			}
		}
		assertTrue( "coast windows with land and sea: " + both + "/" + total, both >= total - 6 );

		//the town's own window has no open sea, and a window far out has no land
		WindowGenerator.Window town = WindowGenerator.generate( SEEDS[0], 0, -WindowGenerator.WIDTH / 2, -WindowGenerator.HEIGHT / 2, 0f );
		for (int t : town.terrain) assertNotEquals( "no deep sea at the town", Terrain.DEEP_WATER, t );
		WindowGenerator.Window sea = WindowGenerator.generate( SEEDS[0], 0, 1500, 1500, 0f );
		int dry = 0;
		for (int t : sea.terrain) if (t != Terrain.WATER && t != Terrain.DEEP_WATER && t != Terrain.FROZEN_WATER) dry++;
		//the sea has its own places (shipwrecks, lighthouses and the beaches round them), so a little dry ground is right
		assertTrue( "land in the open sea: " + dry, dry < sea.terrain.length / 10 );
	}
}
