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

package xyz.gabriwar.warpedpixeldungeon.items.wands;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.audio.SpatialSound;
import xyz.gabriwar.warpedpixeldungeon.effects.MagicMissile;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/** The bolt strikes, and then strikes again: an echo for 60% of the first hit. */
public class WandOfEcho extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_MAGIC_MISSILE;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	public int min(int lvl) {
		return 2 + lvl;
	}

	public int max(int lvl) {
		return 7 + 3*lvl;
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max(), Math.round(60f));
		else
			return Messages.get(this, "stats_desc", min(0), max(0), Math.round(60f));
	}

	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch == null) return;

		int damage = damageRoll();
		wandProc(ch, chargesPerCast());
		ch.damage(damage, this);
		if (ch.isAlive()) {
			//the echo
			ch.damage(Math.max(1, Math.round(damage * 0.6f)), this);
			ch.sprite.burst(0xFFCCCCFF, 4);
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(
				curUser.sprite.parent,
				MagicMissile.MAGIC_MISSILE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		SpatialSound.play(Assets.Sounds.ZAP, curUser);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		int level = Math.max(0, buffedLvl());
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance && defender.isAlive()) {
			defender.damage(Math.max(1, Math.round(damage * 0.4f)), this);
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xCCCCFF);
		particle.am = 0.6f;
		particle.setLifespan(1f);
		particle.setSize(1f, 2f);
		particle.shuffleXY(0.5f);
	}
}
