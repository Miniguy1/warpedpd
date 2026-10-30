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

public class BulletTime extends Skill {

	{
		name = "Bullet Time";
		castText = "Bullet time!";
		image = 108;
		mana = 8;
		tier = 4;
	}

	@Override public boolean toggleable(){ return false; }

	@Override
	public void restoreInBundle( com.watabou.utils.Bundle bundle ){ super.restoreInBundle( bundle ); active = false; }

	@Override
	public ArrayList<String> actions( Hero hero ){
		ArrayList<String> out = new ArrayList<>();
		if (level > 0 && hero.MP >= getManaCost()) out.add( AC_CAST );
		return out;
	}

	@Override public int getManaCost(){ return (int)Math.ceil( mana * (1 + 0.4 * level) ); }

	@Override protected boolean upgrade(){ return true; }

	@Override
	public void execute( final Hero hero, String action ){
		if (!AC_CAST.equals( action ) || level <= 0 || hero.MP < getManaCost()) return;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.heroFOV[m.pos]
					&& Dungeon.level.distance( hero.pos, m.pos ) <= 10){
				Buff.prolong( m, Slow.class, 2f + level );
			}
		}
		hero.sprite.operate( hero.pos );
		GunSkills.finish( hero, this, TIME_TO_USE );
	}
}
