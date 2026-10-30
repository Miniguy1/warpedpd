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

public class SmokeAndMirrors extends ArmorAbility {

	{
		baseChargeUse = 35;
	}

	@Override
	public int icon() {
		return HeroIcon.SMOKE_BOMB;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		int decoys = 1 + hero.pointsInTalent( Talent.DECOY_SWARM );
		ScrollOfMirrorImage.spawnImages( hero, decoys );
		Buff.prolong( hero, Invisibility.class, 4f + hero.pointsInTalent( Talent.THICK_SMOKE ) );
		int ghost = hero.pointsInTalent( Talent.GHOST_STEP );
		if (ghost > 0) Buff.prolong( hero, Haste.class, 2f * ghost );
		CellEmitter.center( hero.pos ).burst( Speck.factory( Speck.DUST ), 20 );
		hero.sprite.operate( hero.pos );

		armor.charge -= chargeUse( hero );
		Item.updateQuickslot();
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.THICK_SMOKE, Talent.DECOY_SWARM, Talent.GHOST_STEP, Talent.HEROIC_ENERGY };
	}
}
