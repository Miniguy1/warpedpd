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

package xyz.gabriwar.warpedpixeldungeon.actors.mobs;

import com.watabou.utils.Random;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Poison;
import xyz.gabriwar.warpedpixeldungeon.items.food.MonsterMeat;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.sprites.BogNewtSprite;

/** A newt that spits from up to five tiles away, and every hit poisons. */
public class BogNewt extends Mob {

	{
		spriteClass = BogNewtSprite.class;
		HP = HT = 10 + Dungeon.depth * 2;
		defenseSkill = 4;
		EXP = 3;
		maxLvl = 9;
		loot = MonsterMeat.class;
		lootChance = 0.1f;
	}

	@Override
	public int attackSkill( Char target ) {
		return 9 + statDepth();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 3 + statDepth() / 2 );
	}

	//a clear line and up to five tiles, next to it or not
	@Override
	protected boolean canAttack( Char enemy ) {
		return Dungeon.level.distance( pos, enemy.pos ) <= 5
				&& new Ballistica( pos, enemy.pos, Ballistica.STOP_SOLID ).collisionPos == enemy.pos;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (enemy != null && enemy.isAlive()) Buff.affect( enemy, Poison.class ).set( 2 + statDepth() / 2 );
		return damage;
	}
}
