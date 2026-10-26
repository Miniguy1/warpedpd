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
import xyz.gabriwar.warpedpixeldungeon.audio.SpatialSound;
import xyz.gabriwar.warpedpixeldungeon.effects.MagicMissile;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/** Damages the target and drains part of what it took back into the caster. Modest damage, but a
 *  fight with it doesn't leave you any weaker. */
public class WandOfSiphon extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_TRANSFUSION;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	public int min(int lvl) {
		return 2 + lvl;
	}

	public int max(int lvl) {
		return 8 + 3*lvl;
	}

	/** the share of the damage that comes back as health */
	private static final float DRAIN = 0.4f;

	/** one cast can never restore more than this */
	public int drainCap() {
		return 6 + 2 * Math.max(0, buffedLvl());
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max(), Math.round(DRAIN * 100), drainCap());
		else
			return Messages.get(this, "stats_desc", min(0), max(0), Math.round(DRAIN * 100), 6);
	}

	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch == null) {
			GLog.i(Messages.get(this, "nothing"));
			return;
		}

		int damage = damageRoll();
		wandProc(ch, chargesPerCast());
		ch.sprite.burst(0xFF8A2BE2, buffedLvl() / 2 + 3);
		ch.damage(damage, this);
		SpatialSound.play(Assets.Sounds.HIT_MAGIC, ch, 1, Random.Float(0.9f, 1.1f));

		//drawn from enemies only: draining an ally would just be a worse heal
		if (ch.alignment != Char.Alignment.ALLY) {
			int heal = Math.min(drainCap(), Math.max(1, Math.round(damage * DRAIN)));
			curUser.heal(heal);
			curUser.sprite.emitter().burst(xyz.gabriwar.warpedpixeldungeon.effects.Speck.factory(xyz.gabriwar.warpedpixeldungeon.effects.Speck.HEALING), 2);
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(
				curUser.sprite.parent,
				MagicMissile.SHADOW,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		SpatialSound.play(Assets.Sounds.ZAP, curUser);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//staff strikes feed the wielder a little
		int level = Math.max(0, buffedLvl());
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance && attacker.isAlive()) {
			attacker.heal(Math.max(1, Math.round(damage * 0.2f)));
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x8A2BE2);
		particle.am = 0.6f;
		particle.setLifespan(1.1f);
		particle.setSize(1f, 2f);
		particle.shuffleXY(0.5f);
	}
}
