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

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

/** TEMPORARY: what the old (endless) world holds near the town and far from it. */
public class WorldContentReportTest {

	private static void add( Map<String, int[]> m, String k, double d ) {
		int[] c = m.computeIfAbsent( k, x -> new int[2] );
		if (d <= 450) c[0]++; else if (d <= 1400) c[1]++;
	}

	@Test
	public void report() {
		int keep = WorldModel.WORLD_RADIUS;
		WorldModel.WORLD_RADIUS = 0;
		int seeds = 4;
		Map<String, int[]> m = new TreeMap<>();
		try {
			for (int k = 0; k < seeds; k++) {
				long seed = 5000L + 911L * k;
				for (int a = 1; a <= 10; a++) {
					for (MountainSites.Site s : MountainSites.sitesIn( seed, a, -1400, -1400, 1400, 1400 )) {
						add( m, "MOUNTAIN " + s.kind, Math.sqrt( (double)s.wx * s.wx + (double)s.wy * s.wy ) );
					}
				}
				for (int a = -1; a >= -12; a--) {
					for (CaveSites.Site s : CaveSites.inRect( seed, a, -1400, -1400, 1400, 1400 )) {
						add( m, "CAVE " + s.type, Math.sqrt( (double)s.cx * s.cx + (double)s.cy * s.cy ) );
					}
				}
				for (int sy = -15; sy <= 15; sy++) for (int sx = -15; sx <= 15; sx++) {
					if (sx == 0 && sy == 0) continue;
					WorldStructures.Site site = WorldStructures.siteType( seed, sx, sy );
					if (site == WorldStructures.Site.NONE || WorldStructures.seaSite( site )) continue;
					add( m, "SURFACE " + site, Math.sqrt( Math.pow( WorldStructures.siteX( seed, sx, sy ), 2 ) + Math.pow( WorldStructures.siteY( seed, sx, sy ), 2 ) ) );
				}
			}
		} finally {
			WorldModel.WORLD_RADIUS = keep;
		}
		StringBuilder out = new StringBuilder( "WORLD CONTENT REPORT (endless world, per world average over " + seeds + " seeds: NEAR within 450 / FAR 450-1400) ## " );
		for (Map.Entry<String, int[]> e : m.entrySet()) {
			out.append( "  " + e.getKey() + ": near " + Math.round( 10.0 * e.getValue()[0] / seeds ) / 10.0 + " / far " + Math.round( 10.0 * e.getValue()[1] / seeds ) / 10.0 + " ## " );
		}
		throw new AssertionError( out.toString() );
	}
}
