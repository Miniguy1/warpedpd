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

public class CalledShot extends Skill {

	{
		name = "Called Shot";
		castText = "Called shot!";
		image = 101;
		mana = 5;
		tier = 3;
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

	/** the mark on an enemy the next shot is called on */
	public static class Mark extends FlavourBuff {
		{ type = buffType.NEGATIVE; }
	}

	//a second press of the skill fires at the target it has marked
	@Override
	public boolean confirmTarget( Hero hero ){ return GunSkills.confirmPending( hero, this ); }

	@Override
	public void execute( final Hero hero, String action ){
		if (!AC_CAST.equals( action ) || level <= 0 || hero.MP < getManaCost()) return;
		if (confirmTarget( hero )) return;
		if (GunSkills.gun() == null){ GLog.w( "You need a firearm equipped." ); return; }
		GunSkills.pickEnemy( this, Messages.get( this, "prompt" ), e -> {
			Buff.prolong( e, Mark.class, 20f );
			if (e.sprite != null) e.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 6 );
			GunSkills.finish( hero, CalledShot.this, TIME_TO_USE );
		} );
	}

	//the next shot at the marked enemy hits x1.5/x2/x2.5
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		if (level <= 0 || !ranged || enemy == null) return damage;
		Mark m = enemy.buff( Mark.class );
		if (m == null) return damage;
		m.detach();
		return Math.round( damage * (1f + 0.5f * level) );
	}

	//and at level 3 it goes through armor
	@Override
	public boolean ignoresArmor( Char target ){
		return level >= MAX_LEVEL && target != null && target.buff( Mark.class ) != null;
	}
}
