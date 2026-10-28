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

/** TEMPORARY: what the island holds, for several sizes of the town's snowfield. */
public class WorldContentReportTest {

	@Test
	public void report() {
		int keepR = WorldModel.WORLD_RADIUS, keepC = WorldModel.TOWN_COLD;
		WorldModel.WORLD_RADIUS = 480;
		int[] colds = { 450, 300, 200, 120 };
		int seeds = 6;
		StringBuilder out = new StringBuilder( "ISLAND CONTENT BY SNOWFIELD SIZE (per world, 6 seeds, island 480) ## " );
		try {
			for (int c = 0; c < colds.length; c++) {
				WorldModel.TOWN_COLD = colds[c];
				Map<String, Double> m = new TreeMap<>();
				double[] share = new double[WorldModel.Biome.values().length];
				int desertSeeds = 0;
				for (int k = 0; k < seeds; k++) {
					long seed = 7000000L * (c + 1) + 1337L * k + 3;
					int land = 0; int[] cnt = new int[share.length];
					WorldModel.Sample s = new WorldModel.Sample();
					for (int y = -460; y <= 460; y += 12) for (int x = -460; x <= 460; x += 12) {
						if (x * x + y * y > 430 * 430) continue;
						WorldModel.sample( seed, x, y, 0f, s );
						if (s.biome == WorldModel.Biome.OCEAN) continue;
						land++; cnt[s.biome.ordinal()]++;
					}
					for (int b = 0; b < share.length; b++) share[b] += (land == 0 ? 0 : cnt[b] / (double)land) / seeds;
					if (cnt[WorldModel.Biome.DESERT.ordinal()] >= land * 0.01) desertSeeds++;
					for (int a = 1; a <= 10; a++) {
						for (MountainSites.Site st : MountainSites.sitesIn( seed, a, -480, -480, 480, 480 )) {
							if (Math.hypot( st.wx, st.wy ) <= 470 && (st.kind == MountainSites.Kind.HERMIT || st.kind == MountainSites.Kind.TOWER)) m.merge( "MOUNTAIN " + st.kind, 1.0 / seeds, Double::sum );
						}
					}
					for (int sy = -6; sy <= 6; sy++) for (int sx = -6; sx <= 6; sx++) {
						if (sx == 0 && sy == 0) continue;
						if (Math.hypot( WorldStructures.siteX( seed, sx, sy ), WorldStructures.siteY( seed, sx, sy ) ) > 470) continue;
						WorldStructures.Site site = WorldStructures.siteType( seed, sx, sy );
						if (site == WorldStructures.Site.NONE || WorldStructures.seaSite( site )) continue;
						m.merge( "" + site, 1.0 / seeds, Double::sum );
					}
				}
				out.append( "COLD " + colds[c] + ": shares " );
				for (WorldModel.Biome b : WorldModel.Biome.values()) if (b != WorldModel.Biome.OCEAN) out.append( b + " " + Math.round( 100 * share[b.ordinal()] ) + "% " );
				out.append( "| seeds with desert " + desertSeeds + "/" + seeds + " | " );
				for (Map.Entry<String, Double> e : m.entrySet()) out.append( e.getKey() + " " + Math.round( 10 * e.getValue() ) / 10.0 + ", " );
				out.append( " ## " );
			}
		} finally {
			WorldModel.WORLD_RADIUS = keepR; WorldModel.TOWN_COLD = keepC;
		}
		throw new AssertionError( out.toString() );
	}
}
