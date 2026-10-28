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
 * world: the first (by a hash) of the neighbouring sector pairs whose two sites lie 30-100 cells apart,
 * both 250+ cells from the town and the pair's middle 240-330 out, with the whole basin on the island,
 * on dry ordinary land (not sea, river, beach, hills, mountain or bog) as it is without the basin, and
 * whose sectors the cascade would leave empty on desert. The endless world (a radius of 0) has no
 * basin: it has deserts of its own.
 */
public final class DesertBasin {

	private DesertBasin(){}

	/** The basin's reach: full desert within 0.55 of it, fading out to nothing at this many cells. */
	public static final float R_OUT = 92f;

	private static final Map<Long, int[]> cache = new ConcurrentHashMap<>();
	private static final int[] NONE = new int[0];
	//asked for while the basin itself is being placed: the world as it would be without it
	private static final ThreadLocal<Boolean> placing = new ThreadLocal<>();
	//a candidate basin the search is trying out (weight and of answer as if it were the basin)
	private static final ThreadLocal<int[]> trial = new ThreadLocal<>();
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
		//a candidate being tried: the world as it would be with it
		int[] tr = trial.get();
		if (tr != null) return tr;
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
			if (r == null) r = NONE;
			cache.put( key, r );
			//everything the sector caches worked out while placing was the world without the basin
			WorldStructures.resetCaches();
		}
		return r.length == 0 ? null : r;
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
		//the first pass is picky; the second takes what a crowded island has left
		int[] r = search( seed, 30, 100, 240, 330, 0.25f );
		if (r == null) r = search( seed, 25, 130, 215, 345, 0.40f );
		//a crowded island: a basin round a single temple
		if (r == null) r = searchSingle( seed, 240, 345, 0.40f );
		//an island hemmed in by villages and roads: look further out and further in
		if (r == null) r = searchSingle( seed, 190, 410, 0.30f );
		return r;
	}

	//with the candidate switched on, is the ground round each place (a ring of desert on the surface's
	//band, clear of cave mouths) what Drylands will ask of it?
	private static boolean groundClear( long seed, int[] cand ){
		trial.set( cand );
		try {
			WorldModel.Sample t = new WorldModel.Sample();
			int tx = WorldStructures.siteX( seed, cand[2], cand[3] ), ty = WorldStructures.siteY( seed, cand[2], cand[3] );
			if (!Drylands.landClear( seed, tx, ty, Drylands.TEMPLE_REACH + 1, t )) return false;
			if (cand[4] != Integer.MIN_VALUE){
				int ox = WorldStructures.siteX( seed, cand[4], cand[5] ), oy = WorldStructures.siteY( seed, cand[4], cand[5] );
				if (!Drylands.landClear( seed, ox, oy, Drylands.OASIS_REACH + 1, t )) return false;
			}
			return true;
		} finally {
			trial.remove();
		}
	}

	private static int[] searchSingle( long seed, int dLo, int dHi, float rimTol ){
		WorldModel.Sample s = new WorldModel.Sample();
		int[] best = null;
		long bestScore = Long.MAX_VALUE;
		for (int sy = -6; sy <= 6; sy++){
			for (int sx = -6; sx <= 6; sx++){
				int cx = WorldStructures.siteX( seed, sx, sy ), cy = WorldStructures.siteY( seed, sx, sy );
				double d = Math.hypot( cx, cy );
				if (d < dLo || d > dHi) continue;
				boolean ok = WorldModel.edgeMask( seed, cx, cy ) == 0f;
				for (int k = 0; k < 16 && ok; k++){
					double a = k * Math.PI / 8;
					ok = WorldModel.edgeMask( seed, cx + (int)(Math.cos( a ) * (R_OUT + 8)), cy + (int)(Math.sin( a ) * (R_OUT + 8)) ) <= rimTol;
				}
				if (!ok || !dryOrdinaryLand( seed, cx, cy, s )) continue;
				if (WorldStructures.baseTypeFor( seed, sx, sy, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
				if (!siteFree( seed, sx, sy, WorldStructures.Site.BURIED_TEMPLE )) continue;
				int[] cand = { cx, cy, sx, sy, Integer.MIN_VALUE, Integer.MIN_VALUE };
				if (!groundClear( seed, cand )) continue;
				long score = hash( seed, sx * 1000L, sy * 1000L );
				if (score < bestScore){
					bestScore = score;
					best = cand;
				}
			}
		}
		return best;
	}

	//would a place of this kind at this sector's site pass what Drylands.site will ask of it (the world
	//as it is without the basin: a village or a road in the way now is in the way then)
	private static boolean siteFree( long seed, int sx, int sy, WorldStructures.Site kind ){
		int cx = WorldStructures.siteX( seed, sx, sy ), cy = WorldStructures.siteY( seed, sx, sy );
		int reach = Drylands.reach( kind );
		return Drylands.clearOfPlaces( seed, sx, sy, cx, cy, reach ) && Drylands.clearOfRoads( seed, cx, cy, reach + 1 );
	}

	private static int[] search( long seed, int gapLo, int gapHi, int mdLo, int mdHi, float rimTol ){
		WorldModel.Sample s = new WorldModel.Sample();
		int[] best = null;
		long bestScore = Long.MAX_VALUE;
		int[][] steps = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };
		for (int sy = -6; sy <= 6; sy++){
			for (int sx = -6; sx <= 6; sx++){
				int ax = WorldStructures.siteX( seed, sx, sy ), ay = WorldStructures.siteY( seed, sx, sy );
				if (Math.hypot( ax, ay ) < 250) continue;
				for (int[] st : steps){
					//the temple in this sector and the oasis in the next, or the other way about
					for (int flip = 0; flip < 2; flip++){
						int tsx = flip == 0 ? sx : sx + st[0], tsy = flip == 0 ? sy : sy + st[1];
						int osx = flip == 0 ? sx + st[0] : sx, osy = flip == 0 ? sy + st[1] : sy;
						int bx = WorldStructures.siteX( seed, sx + st[0], sy + st[1] ), by = WorldStructures.siteY( seed, sx + st[0], sy + st[1] );
						if (Math.hypot( bx, by ) < 250) continue;
						double gap = Math.hypot( ax - bx, ay - by );
						if (gap < gapLo || gap > gapHi) continue;
						int mx = (ax + bx) / 2, my = (ay + by) / 2;
						double md = Math.hypot( mx, my );
						if (md < mdLo || md > mdHi) continue;
						//all of it on the island: the middle on the flat, the rim no further than the coast's
						//first slope
						boolean ok = WorldModel.edgeMask( seed, mx, my ) == 0f;
						for (int k = 0; k < 16 && ok; k++){
							double a = k * Math.PI / 8;
							ok = WorldModel.edgeMask( seed, mx + (int)(Math.cos( a ) * (R_OUT + 8)), my + (int)(Math.sin( a ) * (R_OUT + 8)) ) <= rimTol;
						}
						if (!ok) continue;
						//dry ordinary land at both sites and the middle, as the world is without the basin
						if (!dryOrdinaryLand( seed, ax, ay, s ) || !dryOrdinaryLand( seed, bx, by, s ) || !dryOrdinaryLand( seed, mx, my, s )) continue;
						//the cascade must leave both sectors empty on desert (no ruin, no barrow in the way)
						if (WorldStructures.baseTypeFor( seed, tsx, tsy, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
						if (WorldStructures.baseTypeFor( seed, osx, osy, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
						//and neither place may be crowded by a village or a road
						if (!siteFree( seed, tsx, tsy, WorldStructures.Site.BURIED_TEMPLE ) || !siteFree( seed, osx, osy, WorldStructures.Site.OASIS )) continue;
						int[] cand = { mx, my, tsx, tsy, osx, osy };
						if (!groundClear( seed, cand )) continue;
						long score = hash( seed, tsx * 1000L + osx, tsy * 1000L + osy );
						if (score < bestScore){
							bestScore = score;
							best = cand;
						}
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

	/** a report on why a world has (or has not) a basin, and what each of its sectors says */
	static String explain( long seed ){
		int[] a = of( seed );
		if (a == null) return "seed " + seed + ": none, single-site stages: " + stages( seed );
		StringBuilder o = new StringBuilder( "seed " + seed + ": basin " + a[0] + "," + a[1] );
		WorldModel.Sample smp = new WorldModel.Sample();
		for (int k = 0; k < 2; k++){
			if (a[2 + 2 * k] == Integer.MIN_VALUE){ o.append( " [no oasis]" ); continue; }
			int sx = a[2 + 2 * k], sy = a[3 + 2 * k];
			WorldStructures.Site kind = k == 0 ? WorldStructures.Site.BURIED_TEMPLE : WorldStructures.Site.OASIS;
			int cx = WorldStructures.siteX( seed, sx, sy ), cy = WorldStructures.siteY( seed, sx, sy );
			int reach = Drylands.reach( kind );
			WorldModel.sample( seed, cx, cy, 0f, smp );
			o.append( " [" + kind + " base=" + WorldStructures.baseType( seed, sx, sy ) + " wetland=" + (WetlandSites.place( seed, sx, sy ) != null)
					+ " rolled=" + Drylands.kindRolled( seed, sx, sy ) + " biome=" + smp.biome + " places=" + Drylands.clearOfPlaces( seed, sx, sy, cx, cy, reach )
					+ " roads=" + Drylands.clearOfRoads( seed, cx, cy, reach + 1 ) + " land=" + Drylands.landClear( seed, cx, cy, reach + 1, smp )
					+ " result=" + WorldStructures.siteType( seed, sx, sy ) + "]" );
		}
		return o.toString();
	}

	//how many sectors survive each condition of the single-site search
	private static String stages( long seed ){
		placing.set( Boolean.TRUE );
		try {
			WorldModel.Sample smp = new WorldModel.Sample();
			int inRing = 0, island = 0, land = 0, cascade = 0, free = 0, ground = 0, near = 0;
			for (int sy = -6; sy <= 6; sy++) for (int sx = -6; sx <= 6; sx++) {
				int cx = WorldStructures.siteX( seed, sx, sy ), cy = WorldStructures.siteY( seed, sx, sy );
				double d = Math.hypot( cx, cy );
				if (d < 190 || d > 410) continue;
				inRing++;
				boolean ok = WorldModel.edgeMask( seed, cx, cy ) == 0f;
				for (int k = 0; k < 16 && ok; k++) {
					double a = k * Math.PI / 8;
					ok = WorldModel.edgeMask( seed, cx + (int)(Math.cos( a ) * (R_OUT + 8)), cy + (int)(Math.sin( a ) * (R_OUT + 8)) ) <= 0.30f;
				}
				if (!ok) continue;
				island++;
				if (!dryOrdinaryLand( seed, cx, cy, smp )) continue;
				land++;
				if (WorldStructures.baseTypeFor( seed, sx, sy, WorldModel.Biome.DESERT ) != WorldStructures.Site.NONE) continue;
				cascade++;
				if (!siteFree( seed, sx, sy, WorldStructures.Site.BURIED_TEMPLE )) continue;
				free++;
				if (!groundClear( seed, new int[]{ cx, cy, sx, sy, Integer.MIN_VALUE, Integer.MIN_VALUE } )) continue;
				ground++;
			}
			return "ring " + inRing + " island " + island + " land " + land + " cascade " + cascade + " free " + free + " ground " + ground + " (sectors 150-420 out: " + near + ")";
		} finally {
			placing.remove();
		}
	}
}
