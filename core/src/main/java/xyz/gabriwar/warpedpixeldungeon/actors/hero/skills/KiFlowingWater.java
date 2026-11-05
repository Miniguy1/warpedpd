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

/** Flowing Water (a Monk's passive aura): regeneration, and moving in on a fight feeds ki faster (Ki.stepsPerKi reads this level) */
public class KiFlowingWater extends Skill {

	{
		name = "Flowing Water";
		image = 22;
		tier = 3;
		tag = "PA3";
	}

	@Override
	protected boolean upgrade(){ return true; }

	@Override
	public int healthRegenerationBonus(){
		return level;
	}
}
