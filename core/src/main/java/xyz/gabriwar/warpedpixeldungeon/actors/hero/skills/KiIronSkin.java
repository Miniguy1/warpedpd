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

package xyz.gabriwar.warpedpixeldungeon.actors.hero.skills;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Monk;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;

/** Iron Skin (a Monk's passive aura) */
public class KiIronSkin extends Skill {

	{
		name = "Iron Skin";
		image = 20;
		tier = 1;
		tag = "PA1";
	}

	@Override
	protected boolean upgrade(){ return true; }

	private static Hero h(){ return Dungeon.hero; }
	private static int ki(){ return Monk.stacks( Dungeon.hero ); }
	private static float boost(){ return Monk.boost( Dungeon.hero ); }

	//every blow does flat damage less: a point a level, and a quarter of a point more for each ki, a level
	@Override
	public int incomingDamageReduction( int damage ){
		if (level <= 0) return 0;
		return Math.round( (level + ki() * level / 4f) * boost() );
	}
}
