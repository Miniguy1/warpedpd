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

/** Thunderclap (a Monk's passive aura) */
public class KiThunderclap extends Skill {

	{
		name = "Thunderclap";
		image = 38;
		tier = 2;
		tag = "S2";
	}

	@Override
	protected boolean upgrade(){ return true; }

	private static Hero h(){ return Dungeon.hero; }
	private static int ki(){ return Monk.stacks( Dungeon.hero ); }
	private static float boost(){ return Monk.boost( Dungeon.hero ); }

	//with 6 ki or more, a landed fist sends a quarter of its damage, a level, through every other enemy beside you
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		Hero hero = Dungeon.hero;
		if (level <= 0 || ranged || hero == null || Dungeon.level == null || ki() < 6) return damage;
		int spill = Math.max( 1, Math.round( damage * 0.25f * level * boost() ) );
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (m != enemy && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( hero.pos, m.pos ) <= 1) m.damage( spill, hero );
		}
		return damage;
	}
}
