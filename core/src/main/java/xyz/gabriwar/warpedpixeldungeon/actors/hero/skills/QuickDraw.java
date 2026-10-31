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

public class QuickDraw extends Skill {

	{
		name = "Quick Draw";
		image = 107;
		tier = 3;
		tag = "D3";
	}

	@Override
	protected boolean upgrade(){ return true; }

	private float lastDraw = -100f;

	//an enemy that steps next to you is shot first, once every 12/9/6 turns
	@Override
	public void onEnemyStepsAdjacent( Char enemy, int from ){
		Hero hero = Dungeon.hero;
		Gun gun = GunSkills.gun();
		if (level <= 0 || hero == null || gun == null || gun.round() <= 0 || enemy == null || !enemy.isAlive()
				|| enemy.alignment != Char.Alignment.ENEMY) return;
		float cooldown = level >= MAX_LEVEL ? 6f : level == 2 ? 9f : 12f;
		if (Actor.now() - lastDraw < cooldown) return;
		lastDraw = Actor.now();
		gun.useRound();
		enemy.damage( Math.max( 1, GunSkills.roll( gun ) ), hero );
		if (enemy.sprite != null) enemy.sprite.flash();
		GLog.p( "Quick draw!" );
	}
}
