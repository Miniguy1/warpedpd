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

public class Ricochet extends ActiveSkill2 {

	{
		name = "Ricochet";
		castText = "Ricochet!";
		image = 99;
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

	private int armed = 0;
	private float armedAt = -100f;

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
			hero.MP -= getManaCost();
			castTextYell();
			hero.heroSkills.lastUsed = Ricochet.this;
			armed = level;
			armedAt = Actor.now();
			gun.knockBullet().cast( hero, e.pos );
		} );
	}

	@Override
	public void onHeroAttackMiss( Char enemy, boolean ranged ){
		armed = 0;
	}

	//the armed shot bounces on to up to 1/2/3 more enemies within 4 tiles, for 70% of its damage
	@Override
	public int onHitProc( Char enemy, int damage, boolean ranged ){
		Hero hero = Dungeon.hero;
		if (armed <= 0 || !ranged || hero == null || enemy == null || Actor.now() - armedAt > 4f) return damage;
		int chain = armed;
		armed = 0;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (chain <= 0) break;
			if (m == enemy || m.alignment != Char.Alignment.ENEMY || !m.isAlive()) continue;
			if (Dungeon.level.distance( enemy.pos, m.pos ) > 4 || !Dungeon.level.heroFOV[m.pos]) continue;
			m.damage( Math.max( 1, Math.round( damage * 0.7f ) ), hero );
			if (m.sprite != null) m.sprite.flash();
			chain--;
		}
		return damage;
	}
}
