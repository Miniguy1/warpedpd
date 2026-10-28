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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The dune basin: the one stretch of desert on the island, so the desert's own places (the oasis and
 * the buried temple, Drylands) are a short walk from the town and not thousands of cells off beyond
 * the snow. The land round the town is wet and temperate whatever the seed, so the desert has to be put
 * there on purpose: a patch about a hundred cells wide where the ground is made flat, hot and dry
 * (WorldModel bends elevation, moisture and temperature toward it by weight(), and the town's snow
 * keeps off it), laid over two neighbouring sectors, one of which is given a buried temple and the other
 * an oasis (Drylands.kindRolled).
 *
 * Where it goes is a pure function of the world seed and the island's size, like everything in the
 * world: the first (by a hash) of the neighbouring sector pairs whose two sites lie 40-120 cells apart,
 * both 250+ cells from the town and the pair's middle 265-360 out, with the whole basin on the island,
 * on dry ordinary land (not sea, river, beach, hills, mountain or bog) as it is without the basin, and
 * whose sectors the cascade would leave empty on desert. The endless world (a radius of 0) has no
 * basin: it has deserts of its own.
 */
public final class DesertBasin {

	private DesertBasin(){}

	/** The basin's reach: full desert within 0.55 of it, fading out to nothing at this many cells. */
	public static final float R_OUT = 130f;

	private static final Map<Long, int[]> cache = new ConcurrentHashMap<>();
	private static final int[] NONE = new int[0];
	//asked for while the basin itself is being placed: the world as it would be without it
	private static final ThreadLocal<Boolean> placing = new ThreadLocal<>();
	private static final long SALT = 0xD35E27B5L;

	private static long hash( long seed, long x, long y ){
		long h = seed ^ SALT;
		h ^= x * 0x9E3779B97F4A7C15L;
		h = Long.rotateLeft( h, 29 );
		h ^= y * 0xC2B2AE3D27D4EB4FL;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 33;
		return h;
	}

	/** {centre x, centre y, sector A x, A y (the temple's), sector B x, B y (the oasis's)}, or null for none. */
	public static int[] of( long seed ){
		if (WorldModel.WORLD_RADIUS <= 0) return null;
		if (Boolean.TRUE.equals( placing.get() )) return null;
		//the island's size is part of the answer: a world of another radius is another basin
		long key = seed * 31L + WorldModel.WORLD_RADIUS;
		int[] r = cache.get( key );
		if (r == null){
			if (cache.size() > 64) cache.clear();
			placing.set( Boolean.TRUE );
			try {
				r = place( seed );
			} finally {
				placing.remove();
			}
			cache.put( key, r == null ? NONE : r );
		}
		return r == NONE || r.length == 0 ? null : r;
	}

	private static boolean dryOrdinaryLand( long seed, int x, int y, WorldModel.Sample s ){
		WorldModel.sample( seed, x, y, 0f, s );
		switch (s.biome){
			case OCEAN: case RIVER: case BEACH: case FOOTHILLS: case MOUNTAIN: case SWAMP:
				return false;
			default:
				return true;
		}
	}

	private static int[] place( long seed ){
		WorldModel.Sample s = new WorldModel.Sample();
		int[] best = null;
		long bestScore = Long.MAX_VALUE;
		int[][] steps = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };
		for (int sy = -6; sy <= 6; sy++){
			for (int sx = -6; sx <= 6; sx++){
				int ax = WorldStructures.siteX( seed, sx, sy ), ay = WorldStructures.siteY( seed, sx, sy );
				if (Math.hypot( ax, ay ) < 250) continue;
				for (int[] st : steps){
					int bx2 = sx + st[0], by2 = sy + st[1];
					int bx = WorldStructures.siteX( seed, bx2, by2 ), by = WorldStructures.siteY( seed, bx2, by2 );
					if (Math.hypot( bx, by ) < 250) continue;
					double gap = Math.hypot( ax - bx, ay - by );
					if (gap < 40 || gap > 120) continue;
					int mx = (ax + bx) / 2, my = (ay + by) / 2;
					double md = Math.hypot( mx, my );
					if (md < 265 || md > 360) continue;
					//all of it on the island
					boolean ok = WorldModel.edgeMask( seed, mx, my ) == 0f;
					for (int k = 0; k < 8 && ok; k++){
						double a = k * Math.PI / 4;
						ok = WorldModel.edgeMask( seed, mx + (int)(Math.cos( a ) * (R_OUT + 8)), my + (int)(Math.sin( a ) * (R_OUT + 8)) ) == 0f;
					}
					if (!ok) continue;
					//dry ordinary land at both sites and the middle, as the world is without the basin
					if (!dryOrdinaryLand( seed, ax, ay, s ) || !dryOrdinaryLand( seed, bx, by, s ) || !dryOrdinaryLand( seed, mx, my, s )) continue;
					//the cascade must leave both sectors empty on desert (no ruin, no barrow in the way)
					if (WorldStructures.baseTypeFor( seed, sx, sy, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
					if (WorldStructures.baseTypeFor( seed, bx2, by2, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
					long score = hash( seed, sx * 1000L + bx2, sy * 1000L + by2 );
					if (score < bestScore){
						bestScore = score;
						best = new int[]{ mx, my, sx, sy, bx2, by2 };
					}
				}
			}
		}
		return best;
	}

	private static float smooth( float t ){
		t = t < 0 ? 0 : t > 1 ? 1 : t;
		return t * t * (3 - 2 * t);
	}

	/** How much of the basin a cell is: 1 in its heart, 0 outside its reach. */
	public static float weight( long seed, int wx, int wy ){
		if (WorldModel.WORLD_RADIUS <= 0) return 0f;
		int[] b = of( seed );
		if (b == null) return 0f;
		float dx = wx - b[0], dy = wy - b[1];
		if (Math.abs( dx ) >= R_OUT || Math.abs( dy ) >= R_OUT) return 0f;
		float d = (float)Math.sqrt( dx * dx + dy * dy );
		if (d >= R_OUT) return 0f;
		return smooth( (R_OUT - d) / (R_OUT * 0.45f) );
	}
}
