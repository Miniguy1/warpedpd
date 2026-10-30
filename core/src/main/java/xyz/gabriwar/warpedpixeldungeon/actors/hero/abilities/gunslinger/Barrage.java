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

package xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.gunslinger;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.*;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.ArmorAbility;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.GunSkills;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor;
import xyz.gabriwar.warpedpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.ui.HeroIcon;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

public class Barrage extends ArmorAbility {

	{
		baseChargeUse = 40;
	}

	@Override
	public int icon() {
		return HeroIcon.SPECTRAL_BLADES;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		Gun gun = GunSkills.needLoaded();
		if (gun == null) return;

		ArrayList<Mob> seen = new ArrayList<>();
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m.alignment == xyz.gabriwar.warpedpixeldungeon.actors.Char.Alignment.ENEMY && m.isAlive()
					&& Dungeon.level.heroFOV[m.pos] && Dungeon.level.distance( hero.pos, m.pos ) <= 10) seen.add( m );
		}
		if (seen.isEmpty()){
			GLog.w( Messages.get( this, "no_target" ) );
			return;
		}
		final Hero h = hero;
		Collections.sort( seen, ( a, b ) -> Dungeon.level.distance( h.pos, a.pos ) - Dungeon.level.distance( h.pos, b.pos ) );

		int again = hero.pointsInTalent( Talent.LONG_BURST );
		int cripple = hero.pointsInTalent( Talent.PIERCING_FIRE );
		float focus = 1f + 0.10f * hero.pointsInTalent( Talent.FOCUSED_VOLLEY );

		//one round for each enemy in view, nearest first, until the magazine runs dry
		int fired = 0;
		for (Mob m : seen){
			if (fired >= gun.round()) break;
			m.damage( Math.max( 1, Math.round( GunSkills.roll( gun ) * focus ) ), hero );
			if (again > 0 && m.isAlive()) m.damage( Math.max( 1, Math.round( GunSkills.roll( gun ) * 0.25f * again ) ), hero );
			if (cripple > 0 && m.isAlive()) Buff.prolong( m, Cripple.class, 2f * cripple );
			if (m.sprite != null) m.sprite.flash();
			fired++;
		}
		for (int i = 0; i < fired; i++) gun.useRound();
		hero.sprite.operate( hero.pos );

		armor.charge -= chargeUse( hero );
		Item.updateQuickslot();
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.LONG_BURST, Talent.PIERCING_FIRE, Talent.FOCUSED_VOLLEY, Talent.HEROIC_ENERGY };
	}
}
