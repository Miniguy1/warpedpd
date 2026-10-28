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

import java.util.EnumMap;
import java.util.Map;

/** TEMPORARY: measures what the world looks like at several island sizes, and reports it through a
 *  failing assertion (the build's error log carries the message). Deleted once a size is chosen. */
public class WorldTrimReportTest {

	@Test
	public void report() {
		StringBuilder out = new StringBuilder( "\nWORLD TRIM REPORT\n" );
		int[] radii = { 0, 360, 480, 600, 800 };
		int keep = WorldModel.WORLD_RADIUS;
		try {
			for (int r : radii) {
				WorldModel.WORLD_RADIUS = r;
				double landSum = 0, landInner = 0;
				Map<WorldStructures.Site, Integer> kinds = new EnumMap<>( WorldStructures.Site.class );
				double nearDungeon = 0, nearTemple = 0, nearVillage = 0;
				int seeds = 5;
				for (int k = 0; k < seeds; k++) {
					long seed = 1000L * (r + 1) + 77L * k + 5;
					//land share of the area out to 800 cells, and out to 360
					int land = 0, n = 0, land2 = 0, n2 = 0;
					WorldModel.Sample s = new WorldModel.Sample();
					for (int y = -800; y <= 800; y += 16) for (int x = -800; x <= 800; x += 16) {
						double d = Math.sqrt( (double)x * x + (double)y * y );
						if (d > 800) continue;
						WorldModel.terrainElev( seed, x, y, s );
						boolean isLand = s.elev >= WorldModel.SEA;
						n++; if (isLand) land++;
						if (d <= 360) { n2++; if (isLand) land2++; }
					}
					landSum += land / (double)n;
					landInner += land2 / (double)n2;
					//sites in the sectors within 9 sectors of the town, and the nearest of a few kinds
					double bestD = 1e9, bestT = 1e9, bestV = 1e9;
					for (int sy = -9; sy <= 9; sy++) for (int sx = -9; sx <= 9; sx++) {
						if (sx == 0 && sy == 0) continue;
						WorldStructures.Site site = WorldStructures.siteType( seed, sx, sy );
						if (site == WorldStructures.Site.NONE) continue;
						double dist = Math.sqrt( Math.pow( WorldStructures.siteX( seed, sx, sy ), 2 ) + Math.pow( WorldStructures.siteY( seed, sx, sy ), 2 ) );
						kinds.merge( site, 1, Integer::sum );
						if (site == WorldStructures.Site.DUNGEON) bestD = Math.min( bestD, dist );
						if (site == WorldStructures.Site.BURIED_TEMPLE) bestT = Math.min( bestT, dist );
						if (site == WorldStructures.Site.VILLAGE) bestV = Math.min( bestV, dist );
					}
					nearDungeon += Math.min( bestD, 2000 ); nearTemple += Math.min( bestT, 2000 ); nearVillage += Math.min( bestV, 2000 );
				}
				out.append( "radius " + (r == 0 ? "ENDLESS" : String.valueOf( r )) + ": land within 800 = " + Math.round( 100 * landSum / seeds )
						+ "%, within 360 = " + Math.round( 100 * landInner / seeds ) + "%; nearest dungeon gate avg " + Math.round( nearDungeon / seeds )
						+ ", temple " + Math.round( nearTemple / seeds ) + ", village " + Math.round( nearVillage / seeds ) + " cells; sites per world (9-sector radius): " );
				for (Map.Entry<WorldStructures.Site, Integer> e : kinds.entrySet()) out.append( e.getKey() + "=" + Math.round( e.getValue() / (float)seeds ) + " " );
				out.append( "\n" );
			}
		} finally {
			WorldModel.WORLD_RADIUS = keep;
		}
		throw new AssertionError( out.toString() );
	}
}
