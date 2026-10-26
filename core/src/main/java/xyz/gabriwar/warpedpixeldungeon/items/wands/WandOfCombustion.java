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
 */

package xyz.gabriwar.warpedpixeldungeon.items.wands;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.blobs.Blob;
import xyz.gabriwar.warpedpixeldungeon.actors.blobs.Fire;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning;
import xyz.gabriwar.warpedpixeldungeon.audio.SpatialSound;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.MagicMissile;
import xyz.gabriwar.warpedpixeldungeon.effects.particles.BlastParticle;
import xyz.gabriwar.warpedpixeldungeon.effects.particles.SmokeParticle;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** The strongest wand in the game: the target itself detonates. The target takes the full hit,
 *  everything standing around it takes a share of it, and all of it burns. Allies and the caster
 *  are never caught in the blast. */
public class WandOfCombustion extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FIREBOLT;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	//fewer charges than most wands: every zap is a bomb
	@Override
	public int initialCharges() {
		return 1;
	}

	public int min(int lvl) {
		return 4 + 2*lvl;
	}

	public int max(int lvl) {
		return 12 + 6*lvl;
	}

	/** the blast spreads 1 tile, and 2 tiles from +6 */
	public int radius() {
		return buffedLvl() >= 6 ? 2 : 1;
	}

	/** what the surrounding tiles take, as a share of the hit */
	private static final float SPLASH = 0.7f;

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max(), radius(), Math.round(SPLASH * 100));
		else
			return Messages.get(this, "stats_desc", min(0), max(0), 1, Math.round(SPLASH * 100));
	}

	@Override
	public void onZap(Ballistica bolt) {
		int cell = bolt.collisionPos;
		Char target = Actor.findChar(cell);

		if (target == null) {
			//even a miss sets the ground alight where it lands
			explodeAt(cell, 0, null);
			return;
		}

		int damage = damageRoll();
		wandProc(target, chargesPerCast());
		explodeAt(cell, damage, target);
	}

	private void explodeAt(int center, int damage, Char target) {
		int w = Dungeon.level.width(), h = Dungeon.level.height();
		int cx = center % w, cy = center / w, r = radius();

		CellEmitter.center(center).burst(BlastParticle.FACTORY, 20 + 5 * r);
		SpatialSound.play(Assets.Sounds.BLAST, center);

		//work out everyone caught first, so a death in the middle can't change who is hit
		ArrayList<Char> splashed = new ArrayList<>();
		for (int dy = -r; dy <= r; dy++) {
			for (int dx = -r; dx <= r; dx++) {
				int nx = cx + dx, ny = cy + dy;
				if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
				int c = ny * w + nx;
				if (Dungeon.level.solid[c]) continue;

				if (dx != 0 || dy != 0) {
					CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
				}
				if (Dungeon.level.flamable[c]) {
					GameScene.add(Blob.seed(c, 4, Fire.class));
				}

				Char ch = Actor.findChar(c);
				if (ch != null && ch != target && ch != curUser && ch.alignment != Char.Alignment.ALLY) {
					splashed.add(ch);
				}
			}
		}

		if (target != null) {
			target.damage(damage, this);
			if (target.isAlive()) Buff.affect(target, Burning.class).reignite(target);
		}

		int splash = Math.round(damage * SPLASH);
		for (Char ch : splashed) {
			if (splash > 0) ch.damage(splash, this);
			if (ch.isAlive()) Buff.affect(ch, Burning.class).reignite(ch);
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(
				curUser.sprite.parent,
				MagicMissile.FIRE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		SpatialSound.play(Assets.Sounds.ZAP, curUser);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//the Battlemage's staff leaves the enemy smouldering, and sometimes it flares
		int level = Math.max(0, buffedLvl());
		float procChance = (level + 1f) / (level + 3f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {
			Buff.affect(defender, Burning.class).reignite(defender);
			CellEmitter.center(defender.pos).burst(BlastParticle.FACTORY, 8);
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xFF5A14);
		particle.am = 0.6f;
		particle.setLifespan(1.2f);
		particle.speed.polar(-Random.Float(3.1415926f), 1.5f);
		particle.setSize(1f, 2.5f);
		particle.radiateXY(1f);
	}
}
