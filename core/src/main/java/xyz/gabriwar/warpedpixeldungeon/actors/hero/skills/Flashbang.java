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

public class Flashbang extends Skill {

	{
		name = "Flashbang";
		castText = "Flashbang!";
		image = 102;
		mana = 5;
		tier = 3;
		tag = "A5";
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
		GunSkills.pickSpot( Messages.get( this, "prompt" ), cell -> {
			if (Dungeon.level.distance( hero.pos, cell ) > 8){
				GLog.w( "That is too far to throw." );
				return;
			}
			int radius = 1 + level;
			CellEmitter.center( cell ).burst( Speck.factory( Speck.LIGHT ), 14 );
			for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
				if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( cell, m.pos ) <= radius){
					Buff.prolong( m, Blindness.class, 2f + level );
				}
			}
			GunSkills.finish( hero, Flashbang.this, TIME_TO_USE );
		} );
	}

	//a blinded enemy cannot see the shot coming: +30% damage
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		if (level <= 0 || !ranged || enemy == null || enemy.buff( Blindness.class ) == null) return damage;
		return Math.round( damage * 1.3f );
	}
}
