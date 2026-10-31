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

public class DeadEye extends Skill {

	{
		name = "Dead Eye";
		castText = "Dead eye!";
		image = 105;
		mana = 5;
		tier = 2;
		tag = "D2";
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

	/** two turns of aiming have gone into the next shot */
	public static class Aim extends FlavourBuff {
		{ type = buffType.POSITIVE; }
	}

	@Override
	public void execute( final Hero hero, String action ){
		if (!AC_CAST.equals( action ) || level <= 0 || hero.MP < getManaCost()) return;
		if (GunSkills.needLoaded() == null) return;
		Buff.prolong( hero, Aim.class, 12f );
		hero.sprite.operate( hero.pos );
		GunSkills.finish( hero, this, 2f );
	}

	//the aimed shot hits for +100/150/200% and goes on through up to 1/2/3 more enemies in line
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		Hero hero = Dungeon.hero;
		if (level <= 0 || !ranged || hero == null || enemy == null) return damage;
		Aim aim = hero.buff( Aim.class );
		if (aim == null || GunSkills.gun() == null) return damage;
		aim.detach();
		float mult = 2f + 0.5f * (level - 1);
		int boosted = Math.round( damage * mult );
		Ballistica line = new Ballistica( hero.pos, enemy.pos, Ballistica.WONT_STOP );
		boolean past = false;
		int hits = 0;
		for (int c : line.path){
			if (c == enemy.pos){ past = true; continue; }
			if (!past) continue;
			Char o = Actor.findChar( c );
			if (o != null && o.alignment == Char.Alignment.ENEMY && o.isAlive() && hits < level){
				o.damage( Math.round( boosted * 0.75f ), hero );
				if (o.sprite != null) o.sprite.flash();
				hits++;
			}
			if (Dungeon.level.solid[c]) break;
		}
		return boosted;
	}
}
