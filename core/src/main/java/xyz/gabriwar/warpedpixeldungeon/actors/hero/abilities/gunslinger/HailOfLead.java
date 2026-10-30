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

public class HailOfLead extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_HAWK;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (GunSkills.gun() == null){
			GLog.w( Messages.get( this, "no_gun" ) );
			return;
		}
		int turns = 6 + hero.pointsInTalent( Talent.STEADY_STREAM );
		float boost = 1f + 0.15f * hero.pointsInTalent( Talent.HOT_BARRELS );
		int slow = hero.pointsInTalent( Talent.COVERING_FIRE );
		Buff.affect( hero, Hail.class ).set( turns, boost, slow );
		hero.sprite.operate( hero.pos );

		armor.charge -= chargeUse( hero );
		Item.updateQuickslot();
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.STEADY_STREAM, Talent.HOT_BARRELS, Talent.COVERING_FIRE, Talent.HEROIC_ENERGY };
	}

	/** autofire: every turn it shoots the nearest enemy in view, using no ammunition */
	public static class Hail extends Buff {

		private int left = 0;
		private float boost = 1f;
		private int slow = 0;

		{
			type = buffType.POSITIVE;
			revivePersists = true;
		}

		public void set( int turns, float boost, int slow ){
			left = turns;
			this.boost = boost;
			this.slow = slow;
		}

		public int turnsLeft(){
			return left;
		}

		@Override
		public boolean act() {
			if (!(target instanceof Hero) || !target.isAlive() || left <= 0){
				detach();
				return true;
			}
			Gun gun = GunSkills.gun();
			if (gun != null){
				Mob best = null;
				for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
					if (m.alignment != xyz.gabriwar.warpedpixeldungeon.actors.Char.Alignment.ENEMY || !m.isAlive() || !Dungeon.level.heroFOV[m.pos]) continue;
					if (best == null || Dungeon.level.distance( target.pos, m.pos ) < Dungeon.level.distance( target.pos, best.pos )) best = m;
				}
				if (best != null){
					best.damage( Math.max( 1, Math.round( GunSkills.roll( gun ) * boost ) ), target );
					if (slow > 0 && best.isAlive()) Buff.prolong( best, Slow.class, slow );
					if (best.sprite != null) best.sprite.flash();
				}
			}
			left--;
			if (left <= 0) detach(); else spend( TICK );
			return true;
		}

		private static final String LEFT = "left", BOOST = "boost", SLOW = "slow";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LEFT, left );
			bundle.put( BOOST, boost );
			bundle.put( SLOW, slow );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getInt( LEFT );
			boost = bundle.getFloat( BOOST );
			slow = bundle.getInt( SLOW );
		}
	}
}
