/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sprouted Pixel Dungeon
 * Copyright (C) 2015 dachhack
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

package xyz.gabriwar.warpedpixeldungeon.items.wands;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Frost;
import xyz.gabriwar.warpedpixeldungeon.audio.SpatialSound;
import xyz.gabriwar.warpedpixeldungeon.effects.MagicMissile;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/** Single-target: it shatters the enemy from the inside, then freezes what is left. There is no
 *  blast, so the hit is bigger than Combustion's. The damage lands first, because damage would
 *  break the freeze if it came after. */
public class WandOfFreeze extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FROST;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	@Override
	public int initialCharges() {
		return 2;
	}

	public int min(int lvl) {
		return 5 + 2*lvl;
	}

	public int max(int lvl) {
		return 15 + 6*lvl;
	}

	/** frozen turns: short at +0, long enough to matter from +3 */
	public float freezeTurns() {
		return Math.min(Frost.DURATION, 2f + buffedLvl() * 0.75f);
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max(), freezeTurns());
		else
			return Messages.get(this, "stats_desc", min(0), max(0), 2f);
	}

	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch == null) {
			Dungeon.level.pressCell(bolt.collisionPos);
			return;
		}

		//can't shatter what is already frozen solid
		if (ch.buff(Frost.class) != null) return;

		int damage = damageRoll();
		//a burning target takes the shock harder, and the fire goes out
		if (ch.buff(Burning.class) != null) {
			damage = Math.round(damage * 1.25f);
			ch.buff(Burning.class).detach();
		}

		ch.sprite.burst(0xFF99CCFF, buffedLvl() / 2 + 4);
		wandProc(ch, chargesPerCast());
		ch.damage(damage, this);
		SpatialSound.play(Assets.Sounds.SHATTER, ch.pos, 1f, Random.Float(0.9f, 1.15f));

		if (ch.isAlive()) {
			Buff.affect(ch, Frost.class, freezeTurns());
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(
				curUser.sprite.parent,
				MagicMissile.FROST,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		SpatialSound.play(Assets.Sounds.ZAP, curUser);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//a chance for the staff strike to lock the enemy in ice for a moment
		int level = Math.max(0, buffedLvl());
		float procChance = (level + 1f) / (level + 6f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance && defender.buff(Frost.class) == null) {
			final Char target = defender;
			final float turns = 1f + level * 0.5f;
			//delayed through an actor so the freeze isn't broken by the damage of this very hit
			new xyz.gabriwar.warpedpixeldungeon.actors.buffs.FlavourBuff() {
				{
					actPriority = VFX_PRIO;
				}

				public boolean act() {
					if (target.isAlive()) Buff.affect(target, Frost.class, turns);
					return super.act();
				}
			}.attachTo(defender);
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xA8E8FF);
		particle.am = 0.6f;
		particle.setLifespan(1f);
		particle.setSize(1f, 2f);
		particle.shuffleXY(0.5f);
	}
}
