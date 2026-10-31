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

import com.watabou.utils.Random;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.*;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfBlastWave;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.CellSelector;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

public class PistolWhip extends Skill {

	{
		name = "Pistol Whip";
		image = 93;
		tier = 2;
		tag = "PB4";
	}

	@Override
	protected boolean upgrade(){ return true; }


	//a melee blow with a firearm cripples (2/3/4 turns) and knocks the enemy back a tile
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		Hero hero = Dungeon.hero;
		if (level <= 0 || ranged || hero == null || enemy == null || !(hero.belongings.weapon() instanceof Gun)) return damage;
		Buff.prolong( enemy, Cripple.class, 1f + level );
		if (enemy.isAlive() && !enemy.rooted && !Char.hasProp( enemy, Char.Property.IMMOVABLE ) && enemy.pos != hero.pos){
			Ballistica trajectory = new Ballistica( hero.pos, enemy.pos, Ballistica.STOP_TARGET );
			trajectory = new Ballistica( trajectory.collisionPos, trajectory.path.get( trajectory.path.size() - 1 ), Ballistica.PROJECTILE );
			WandOfBlastWave.throwChar( enemy, trajectory, 1, false, false, this );
		}
		return damage;
	}
}
