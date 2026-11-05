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

/** Still Water (a Monk's passive aura) */
public class KiStillWater extends Skill {

	{
		name = "Still Water";
		image = 22;
		tier = 3;
		tag = "PA3";
	}

	@Override
	protected boolean upgrade(){ return true; }

	private static Hero h(){ return Dungeon.hero; }
	private static int ki(){ return Monk.stacks( Dungeon.hero ); }
	private static float boost(){ return Monk.boost( Dungeon.hero ); }

	//stillness heals a little; from level 2, meditation builds ki every turn
	@Override
	public int healthRegenerationBonus(){
		return level;
	}
}
