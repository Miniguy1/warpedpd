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

/** TEMPORARY: which feature scale and island radius keep every biome (and the sites) on the island. */
public class WorldTrimReportTest {

	@Test
	public void report() {
		StringBuilder out = new StringBuilder( "\nWORLD TRIM REPORT 2\n" );
		float[] scales = { 5f, 3f, 2f, 1.5f, 1f };
		int[] radii = { 480, 480, 480, 480, 480 };
		float keepS = WorldModel.S; int keepR = WorldModel.WORLD_RADIUS;
		try {
			for (int c = 0; c < scales.length; c++) {
				WorldModel.S = scales[c];
				WorldModel.WORLD_RADIUS = radii[c];
				int seeds = 6;
				double[] share = new double[WorldModel.Biome.values().length];
				double[] worst = new double[share.length];
				java.util.Arrays.fill( worst, 1.0 );
				int missingMountain = 0, missingDesert = 0, missingCold = 0, missingSwamp = 0;
				double villages = 0, gates = 0;
				for (int k = 0; k < seeds; k++) {
					long seed = 100000L * (c + 1) + 313L * k + 11;
					int landN = 0;
					int[] cnt = new int[share.length];
					WorldModel.Sample s = new WorldModel.Sample();
					for (int y = -460; y <= 460; y += 12) for (int x = -460; x <= 460; x += 12) {
						if (x * x + y * y > 400 * 400) continue;
						WorldModel.sample( seed, x, y, 0f, s );
						if (s.biome == WorldModel.Biome.OCEAN) continue;
						landN++;
						cnt[s.biome.ordinal()]++;
					}
					for (int b = 0; b < share.length; b++) {
						double f = landN == 0 ? 0 : cnt[b] / (double)landN;
						share[b] += f / seeds;
						worst[b] = Math.min( worst[b], f );
					}
					int hi = cnt[WorldModel.Biome.MOUNTAIN.ordinal()] + cnt[WorldModel.Biome.FOOTHILLS.ordinal()];
					if (hi < landN * 0.02) missingMountain++;
					if (cnt[WorldModel.Biome.DESERT.ordinal()] < landN * 0.01) missingDesert++;
					if (cnt[WorldModel.Biome.TUNDRA.ordinal()] + cnt[WorldModel.Biome.SNOWFIELD.ordinal()] < landN * 0.01) missingCold++;
					if (cnt[WorldModel.Biome.SWAMP.ordinal()] < landN * 0.01) missingSwamp++;
					for (int sy = -6; sy <= 6; sy++) for (int sx = -6; sx <= 6; sx++) {
						if (sx == 0 && sy == 0) continue;
						WorldStructures.Site site = WorldStructures.siteType( seed, sx, sy );
						double dist = Math.sqrt( Math.pow( WorldStructures.siteX( seed, sx, sy ), 2 ) + Math.pow( WorldStructures.siteY( seed, sx, sy ), 2 ) );
						if (dist > 470) continue;
						if (site == WorldStructures.Site.VILLAGE) villages++;
						if (site == WorldStructures.Site.DUNGEON) gates++;
					}
				}
				out.append( "S=" + scales[c] + " R=" + radii[c] + ": land shares " );
				for (WorldModel.Biome b : WorldModel.Biome.values()) {
					if (b == WorldModel.Biome.OCEAN) continue;
					out.append( b + " " + Math.round( 100 * share[b.ordinal()] ) + "% " );
				}
				out.append( "| seeds (of " + seeds + ") WITHOUT: mountains " + missingMountain + ", desert " + missingDesert + ", cold " + missingCold + ", swamp " + missingSwamp
						+ " | per world: villages " + Math.round( villages / seeds ) + ", dungeon gates " + Math.round( gates / seeds ) + "\n" );
			}
		} finally {
			WorldModel.S = keepS; WorldModel.WORLD_RADIUS = keepR;
		}
		throw new AssertionError( out.toString() );
	}
}
