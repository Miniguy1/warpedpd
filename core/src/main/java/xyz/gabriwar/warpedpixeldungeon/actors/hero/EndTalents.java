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

package xyz.gabriwar.warpedpixeldungeon.actors.hero;

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.FlavourBuff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Invisibility;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

/** What the level 40 and level 60 talents actually do. Every one of them is a number the rest of
 *  the game asks for at one fixed spot (see the calls in Hero and Wand); nothing here is stored,
 *  everything is read from the points the hero has put into the talent. */
public class EndTalents {

	/** the cooldowns and the short windows */
	public static class UnbrokenCooldown extends FlavourBuff {}
	public static class VanishCooldown extends FlavourBuff {}
	public static class LastRitesCooldown extends FlavourBuff {}
	public static class RiposteReady extends FlavourBuff {}

	private static int p( Hero h, Talent t ) {
		return h == null ? 0 : h.pointsInTalent( t );
	}

	/** heal, tolerating a hero with no sprite (the headless tests have none) */
	private static void heal( Char c, int amount ) {
		if (c.sprite == null) {
			c.HP = Math.min( c.HT, c.HP + amount );
		} else {
			c.heal( amount );
		}
	}

	private static float hpFrac( Char c ) {
		return c.HT <= 0 ? 1f : c.HP / (float)c.HT;
	}

	// ---- Blade Dancer: consecutive hits on one enemy (a short memory, not saved) ----
	private static int danceTarget = -1, danceStacks = 0;

	/** the damage of one melee hit, with every attacking talent applied; heals the hero for
	 *  Warlord's Fury. Called once per hit from Hero.attackProc. */
	public static int melee( Hero h, Char enemy, int damage ) {
		if (damage <= 0 || enemy == null) return damage;
		float m = 1f;
		int a;
		float hp = hpFrac( h );

		if ((a = p( h, Talent.BLOODIED_RESOLVE )) > 0 && hp <= 0.5f) m *= 1f + 0.08f * a;
		if ((a = p( h, Talent.KNIFE_IN_THE_DARK )) > 0 && enemy instanceof Mob && ((Mob)enemy).surprisedBy( h, true )) m *= 1f + 0.15f * a;
		if ((a = p( h, Talent.DEATHS_HAND )) > 0 && enemy.HP <= enemy.HT * 0.30f) m *= 1f + 0.20f * a;
		if ((a = p( h, Talent.APEX_PREDATOR )) > 0 && enemy.HP >= enemy.HT) m *= 1f + 0.20f * a;
		if ((a = p( h, Talent.PERFECT_FORM )) > 0 && hp >= 0.8f) m *= 1f + 0.10f * a;
		if ((a = p( h, Talent.AVATARS_GRACE )) > 0) m *= 1f + 0.05f * a;
		if ((a = p( h, Talent.RIPOSTE_MASTERY )) > 0 && h.buff( RiposteReady.class ) != null) {
			m *= 1f + 0.15f * a;
			h.buff( RiposteReady.class ).detach();
		}
		if ((a = p( h, Talent.BLADE_DANCER )) > 0) {
			if (enemy.id() == danceTarget) {
				danceStacks = Math.min( 5, danceStacks + 1 );
			} else {
				danceTarget = enemy.id();
				danceStacks = 0;
			}
			m *= 1f + 0.05f * a * danceStacks;
		}

		int result = Math.max( 1, Math.round( damage * m ) );

		if ((a = p( h, Talent.WARLORDS_FURY )) > 0 && hp <= 0.35f && h.isAlive()) {
			heal( h,  Math.max( 1, Math.round( result * 0.06f * a ) ) );
		}
		return result;
	}

	/** the damage the hero is about to take, after every defending talent. Also where the
	 *  once-in-a-while saves happen (they work on the HP the hero would be left with). */
	public static int beforeDamage( Hero h, int dmg, Object src ) {
		if (dmg <= 0) return dmg;
		int a;

		float m = 1f;
		if ((a = p( h, Talent.IRONCLAD )) > 0) m *= 1f - 0.04f * a;
		if ((a = p( h, Talent.AVATARS_GRACE )) > 0) m *= 1f - 0.05f * a;
		dmg = Math.max( 1, Math.round( dmg * m ) );

		if (src instanceof Char && src != h) {
			Char striker = (Char)src;
			if ((a = p( h, Talent.RIPOSTE_MASTERY )) > 0) {
				Buff.affect( h, RiposteReady.class, 3f );
			}
			if ((a = p( h, Talent.BRIAR_HIDE )) > 0 && striker.isAlive() && striker.alignment != Char.Alignment.ALLY) {
				striker.damage( Math.max( 1, Math.round( dmg * 0.08f * a ) ), h );
			}
		}

		//Last Rites: before a blow that would drop you under a quarter health, you recover
		if ((a = p( h, Talent.LAST_RITES )) > 0 && h.buff( LastRitesCooldown.class ) == null
				&& (h.HP + h.shielding() - dmg) < h.HT * 0.25f) {
			heal( h,  Math.round( h.HT * (0.10f + 0.10f * a) ) );
			Buff.affect( h, LastRitesCooldown.class, 300f );
		}

		//Unbroken: a blow that would kill you leaves you at 1 health instead
		if ((a = p( h, Talent.UNBROKEN )) > 0 && h.buff( UnbrokenCooldown.class ) == null
				&& dmg >= h.HP + h.shielding()) {
			heal( h,  Math.round( h.HT * (0.10f + 0.10f * a) ) );
			dmg = Math.max( 0, Math.min( dmg, h.HP + h.shielding() - 1 ) );
			Buff.affect( h, UnbrokenCooldown.class, 200f );
		}

		//Vanishing Act: hurt badly, you slip out of sight
		if ((a = p( h, Talent.VANISHING_ACT )) > 0 && h.buff( VanishCooldown.class ) == null
				&& (h.HP + h.shielding() - dmg) > 0 && (h.HP + h.shielding() - dmg) < h.HT * 0.30f) {
			Buff.affect( h, Invisibility.class, 3f * a );
			Buff.affect( h, VanishCooldown.class, 150f );
		}
		return dmg;
	}

	public static float evasionMultiplier( Hero h ) {
		return 1f + 0.06f * p( h, Talent.SHADE_STRIDE ) + 0.05f * p( h, Talent.FOOTWORK );
	}

	public static float accuracyMultiplier( Hero h ) {
		return 1f + 0.06f * p( h, Talent.KEEN_EYE );
	}

	public static float speedMultiplier( Hero h ) {
		return 1f + 0.04f * p( h, Talent.PATHFINDER );
	}

	// ---- the Mage's wands ----
	public static float wandDamageMultiplier() {
		return 1f + 0.08f * p( xyz.gabriwar.warpedpixeldungeon.Dungeon.hero, Talent.OVERCHARGE );
	}

	public static float wandRechargeMultiplier( Char owner ) {
		return owner instanceof Hero ? 1f + 0.12f * p( (Hero)owner, Talent.ARCANE_RESERVE ) : 1f;
	}

	/** this zap costs nothing */
	public static boolean freeZap() {
		int a = p( xyz.gabriwar.warpedpixeldungeon.Dungeon.hero, Talent.ARCHMAGES_SURGE );
		return a > 0 && Random.Float() < 0.10f * a;
	}

	public static void afterZap() {
		Hero h = xyz.gabriwar.warpedpixeldungeon.Dungeon.hero;
		int a = p( h, Talent.SPELLWEAVER );
		if (a > 0 && h.isAlive()) {
			Buff.affect( h, Barrier.class ).incShield( Math.max( 1, Math.round( h.HT * 0.03f * a ) ) );
		}
	}

	// ---- the Cleric's slow gifts: once per hero turn ----
	private static int mercy = 0, ward = 0;

	public static void tick( Hero h ) {
		int a;
		if ((a = p( h, Talent.MERCIFUL_LIGHT )) > 0) {
			if (++mercy >= 20 - 4 * a) {
				mercy = 0;
				if (h.isAlive() && h.HP < h.HT) heal( h,  1 );
			}
		}
		if ((a = p( h, Talent.WARDED_FAITH )) > 0) {
			if (++ward >= 25) {
				ward = 0;
				int cap = Math.max( 2, Math.round( h.HT * 0.02f * a * 2 ) );
				int give = Math.max( 1, Math.round( h.HT * 0.02f * a ) );
				if (h.isAlive() && h.shielding() < cap) {
					Buff.affect( h, Barrier.class ).incShield( Math.min( give, cap - h.shielding() ) );
				}
			}
		}
	}
}
