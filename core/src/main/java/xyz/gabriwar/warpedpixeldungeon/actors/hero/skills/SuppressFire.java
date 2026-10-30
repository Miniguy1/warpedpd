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

public class SuppressFire extends ActiveSkill3 {

	{
		name = "Suppress Fire";
		castText = "Suppressing fire!";
		image = 100;
		mana = 4;
		tier = 2;
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
		final Gun gun = GunSkills.needLoaded();
		if (gun == null) return;
		GunSkills.pickSpot( Messages.get( this, "prompt" ), cell -> {
			Ballistica line = new Ballistica( hero.pos, cell, Ballistica.WONT_STOP );
			java.util.LinkedHashSet<Mob> caught = new java.util.LinkedHashSet<>();
			int steps = 0;
			for (int c : line.path){
				if (steps++ > 6) break;
				for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
					if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.heroFOV[m.pos]
							&& Dungeon.level.distance( c, m.pos ) <= 1) caught.add( m );
				}
				if (Dungeon.level.solid[c]) break;
			}
			if (caught.isEmpty()){
				GLog.w( "There is no enemy in that line of fire." );
				return;
			}
			gun.useRound();
			//half the round's damage to everything the burst sweeps, and a slowdown of 3/5/7 turns
			for (Mob m : caught){
				m.damage( Math.max( 1, Math.round( GunSkills.roll( gun ) * 0.5f ) ), hero );
				if (m.isAlive()) Buff.prolong( m, Slow.class, 1f + 2f * level );
				if (m.sprite != null) m.sprite.flash();
			}
			GunSkills.finish( hero, SuppressFire.this, TIME_TO_USE );
		} );
	}
}
