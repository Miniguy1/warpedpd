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

public class FanTheHammer extends Skill {

	{
		name = "Fan the Hammer";
		castText = "Fan the hammer!";
		image = 103;
		mana = 5;
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

	//a second press of the skill fires at the target it has marked
	@Override
	public boolean confirmTarget( Hero hero ){ return GunSkills.confirmPending( hero, this ); }

	@Override
	public void execute( final Hero hero, String action ){
		if (!AC_CAST.equals( action ) || level <= 0 || hero.MP < getManaCost()) return;
		if (confirmTarget( hero )) return;
		final Gun gun = GunSkills.needLoaded();
		if (gun == null) return;
		GunSkills.pickEnemy( this, Messages.get( this, "prompt" ), e -> {
			int fired = 0;
			float factor = 0.45f + 0.15f * level;
			//the whole magazine, one round after another, until the target drops
			for (int i = gun.round(); i > 0 && e.isAlive(); i--){
				e.damage( Math.max( 1, Math.round( GunSkills.roll( gun ) * factor ) ), hero );
				fired++;
			}
			for (int i = 0; i < fired; i++) gun.useRound();
			if (e.sprite != null) e.sprite.flash();
			GLog.i( "You fan the hammer: " + fired + " rounds." );
			GunSkills.finish( hero, FanTheHammer.this, TIME_TO_USE );
		} );
	}
}
