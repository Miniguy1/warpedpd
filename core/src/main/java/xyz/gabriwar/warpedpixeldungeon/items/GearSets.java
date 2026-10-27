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

package xyz.gabriwar.warpedpixeldungeon.items;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.FlavourBuff;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.armor.*;
import xyz.gabriwar.warpedpixeldungeon.items.rings.*;
import xyz.gabriwar.warpedpixeldungeon.items.trinkets.*;
import xyz.gabriwar.warpedpixeldungeon.items.wands.*;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.*;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

/** Matching sets among the new items. Wear (or carry) several pieces of one set and it pays out:
 *  every piece beyond the first is worth one "tier" of the set's bonus, up to a full set, and a
 *  few sets keep a bonus back for four or all of their pieces.
 *
 *  Thirteen sets: six general ones, one for each of the six classes (built around how that class
 *  plays), and the Yog-Forged set, the strongest, of six pieces.
 *
 *  What counts as a piece: the armor you wear, the weapon you wield (a staff counts as itself, and
 *  as the wand it holds), the rings you wear, and the trinkets, amulets and relics in your pack
 *  (they work carried). A piece counts once however many copies you hold. Nothing here is stored:
 *  it is read from what the hero has on, every time it is asked. */
public class GearSets {

	public static final String[] NAMES = { "Emberwrought", "Frostbound", "Bloodbound", "Stonewarden", "Stormcaller", "Wanderer's Vigil",
			"Warlord's Regalia", "Archmage's Vestments", "Shadowblade's Shroud", "Warden's Hunt", "Duelist's Flourish", "Saint's Raiment",
			"Yog-Forged" };

	/** what one tier of each set gives, in words (the item descriptions quote it) */
	public static final String[] PER_TIER = {
			"+8% damage dealt",
			"-4% damage taken and +4% evasion",
			"heal for 3% of the damage you deal",
			"-4% damage taken and 4% of every blow you take returned to the attacker",
			"+5% accuracy and +3% movement speed",
			"+8% experience, and a slow regeneration that quickens with each tier",
			"+6% damage dealt and -3% damage taken",
			"+8% wand damage and +8% faster wand and staff recharge",
			"+4% evasion and +12% damage against enemies that have not noticed you",
			"+5% accuracy and +10% damage against enemies at full health",
			"+4% damage dealt, +3% accuracy and +3% evasion",
			"-3% damage taken and a slow regeneration that quickens with each tier",
			"+6% damage dealt, -4% damage taken, heal for 1.5% of the damage you deal, +2% movement speed and +6% experience" };

	/** what a set keeps back for its later pieces (empty for none) */
	public static final String[] EXTRA = {
			"A full set of five is worth four times that.", "A full set of five is worth four times that.", "A full set of five is worth four times that.",
			"A full set of five is worth four times that.", "A full set of five is worth four times that.", "A full set of five is worth four times that.",
			"With four pieces you also heal for 4% of the damage you deal; with all five you deal 25% more damage while at 35% health or less.",
			"With four pieces each wand zap has a 10% chance to cost nothing; with all five each zap also wraps you in a barrier of 5% of your health.",
			"With four pieces you deal 25% more damage to enemies at 30% health or less; with all five you move 10% faster.",
			"With four pieces you move 6% faster; with all five 10% of every blow you take is returned to the attacker.",
			"With all five pieces you deal a further 10% more damage.",
			"With four pieces a barrier of 3% of your health forms every 20 turns; with all five you also heal for 5% of the damage you deal.",
			"With the full set of six, once every 300 turns a killing blow leaves you alive and heals you for 30% of your health." };

	public static final int GENERIC_SETS = 6;
	public static final int YOG = 12;

	@SuppressWarnings("unchecked")
	private static final Class<?>[][] PIECES = {
			{ RobeOfFire.class, Dawnbreaker.class, Emberfang.class, EmberCharm.class, WandOfCombustion.class },
			{ RobeOfShattering.class, Frostbrand.class, WandOfFreeze.class, WayfarersBoots.class, RingOfBulwark.class },
			{ BleedingArmor.class, BloodmoonAxe.class, Voidreaver.class, RingOfVampirism.class, WarlordsMedal.class },
			{ DragonscaleMail.class, ObsidianGreataxe.class, Gravesplitter.class, StoneheartPendant.class, Thornmail.class },
			{ Thunderlance.class, Stormbreaker.class, WandOfEcho.class, HuntersFang.class, RingOfCruelty.class },
			{ Duskplate.class, WyrmfangGlaive.class, SagesIdol.class, AmuletOfRenewal.class, AmuletOfAegis.class },
			{ WarlordsPlate.class, WarlordsGreatsword.class, WarlordsSignet.class, WarlordsBanner.class, WarlordsCrest.class },
			{ ArchmagesRobe.class, ArchmagesStaff.class, ArchmagesBand.class, ArchmagesOrb.class, ArchmagesSigil.class },
			{ ShadowbladesCloak.class, ShadowbladesDirk.class, ShadowbladesRing.class, ShadowbladesMask.class, ShadowbladesToken.class },
			{ WardensHide.class, WardensGlaive.class, WardensRing.class, WardensHorn.class, WardensTotem.class },
			{ DuelistsCoat.class, DuelistsRapier.class, DuelistsSignet.class, DuelistsGlove.class, DuelistsMedal.class },
			{ SaintsVestment.class, SaintsMace.class, SaintsHalo.class, SaintsChalice.class, SaintsRelic.class },
			{ YogForgedPlate.class, YogForgedBlade.class, YogForgedBand.class, YogsEye.class, YogsHeart.class, YogsFang.class },
	};

	/** the set an item belongs to (an index into NAMES), or -1 */
	public static int setOf( Class<?> c ) {
		for (int s = 0; s < PIECES.length; s++) {
			for (Class<?> p : PIECES[s]) if (p == c) return s;
		}
		return -1;
	}

	public static Class<?>[] piecesOf( int set ) {
		return PIECES[set];
	}

	/** the tier a number of pieces of a set is worth: one per piece beyond the first */
	public static int tier( int set, int pieces ) {
		return Math.max( 0, Math.min( PIECES[set].length - 1, pieces - 1 ) );
	}

	/** everything the sets add up to */
	public static class Effects {
		public float damage = 1f, taken = 1f, evasion = 1f, accuracy = 1f, speed = 1f, exp = 1f;
		public float lifesteal = 0f, thorns = 0f;
		/** extra damage: while the hero is at 35% health or less / against enemies that have not noticed
		 *  the hero / at 30% health or less / at full health */
		public float lowHp = 0f, sneak = 0f, vsWeak = 0f, vsFull = 0f;
		/** wands: damage and recharge multipliers, the chance a zap is free, the barrier a zap gives */
		public float wandDamage = 1f, wandRecharge = 1f, freeZap = 0f, zapBarrier = 0f;
		/** turns between the one point of health the regenerating sets give, or 0 for none */
		public int regenEvery = 0;
		/** a barrier of this share of health every barrierEvery turns, or none */
		public int barrierEvery = 0;
		public float barrierPct = 0f;
		/** the Yog-Forged capstone: a killing blow, survived, once in a long while */
		public boolean lastStand = false;
	}

	private static int p( int[] pieces, int set ) {
		return set < pieces.length ? pieces[set] : 0;
	}

	/** pure: the effects of owning this many pieces of each set (a short array is zeros after its end) */
	public static Effects effects( int[] pieces ) {
		Effects e = new Effects();
		int ember = tier( 0, p( pieces, 0 ) ), frost = tier( 1, p( pieces, 1 ) ), blood = tier( 2, p( pieces, 2 ) ),
				stone = tier( 3, p( pieces, 3 ) ), storm = tier( 4, p( pieces, 4 ) ), vigil = tier( 5, p( pieces, 5 ) );
		e.damage += 0.08f * ember;
		e.taken -= 0.04f * frost + 0.04f * stone;
		e.evasion += 0.04f * frost;
		e.lifesteal += 0.03f * blood;
		e.thorns += 0.04f * stone;
		e.accuracy += 0.05f * storm;
		e.speed += 0.03f * storm;
		e.exp += 0.08f * vigil;
		if (vigil > 0) e.regenEvery = 30 - 4 * vigil;

		int warrior = tier( 6, p( pieces, 6 ) ), mage = tier( 7, p( pieces, 7 ) ), rogue = tier( 8, p( pieces, 8 ) ),
				hunt = tier( 9, p( pieces, 9 ) ), duel = tier( 10, p( pieces, 10 ) ), cleric = tier( 11, p( pieces, 11 ) ),
				yog = tier( YOG, p( pieces, YOG ) );

		e.damage += 0.06f * warrior;
		e.taken -= 0.03f * warrior;
		if (warrior >= 3) e.lifesteal += 0.04f;
		if (warrior >= 4) e.lowHp = 0.25f;

		e.wandDamage += 0.08f * mage;
		e.wandRecharge += 0.08f * mage;
		if (mage >= 3) e.freeZap = 0.10f;
		if (mage >= 4) e.zapBarrier = 0.05f;

		e.evasion += 0.04f * rogue;
		e.sneak += 0.12f * rogue;
		if (rogue >= 3) e.vsWeak = 0.25f;
		if (rogue >= 4) e.speed += 0.10f;

		e.accuracy += 0.05f * hunt;
		e.vsFull += 0.10f * hunt;
		if (hunt >= 3) e.speed += 0.06f;
		if (hunt >= 4) e.thorns += 0.10f;

		e.damage += 0.04f * duel;
		e.accuracy += 0.03f * duel;
		e.evasion += 0.03f * duel;
		if (duel >= 4) e.damage += 0.10f;

		e.taken -= 0.03f * cleric;
		if (cleric > 0) {
			int every = 26 - 4 * cleric;
			e.regenEvery = e.regenEvery == 0 ? every : Math.min( e.regenEvery, every );
		}
		if (cleric >= 3) {
			e.barrierEvery = 20;
			e.barrierPct = 0.03f;
		}
		if (cleric >= 4) e.lifesteal += 0.05f;

		e.damage += 0.06f * yog;
		e.taken -= 0.04f * yog;
		e.lifesteal += 0.015f * yog;
		e.speed += 0.02f * yog;
		e.exp += 0.06f * yog;
		if (yog >= 5) e.lastStand = true;
		return e;
	}

	private static void count( Object it, Set<Class<?>> seen ) {
		if (it != null) seen.add( it.getClass() );
	}

	/** how many pieces of each set the hero has on or carries */
	public static int[] pieceCounts( Hero h ) {
		int[] n = new int[PIECES.length];
		if (h == null || h.belongings == null) return n;
		Set<Class<?>> seen = new HashSet<>();
		count( h.belongings.armor(), seen );
		count( h.belongings.ring(), seen );
		count( h.belongings.misc(), seen );
		count( h.belongings.artifact(), seen );
		Object weapon = h.belongings.weapon();
		count( weapon, seen );
		if (weapon instanceof MagesStaff) {
			//a staff is also the wand it holds
			Class<?> w = ((MagesStaff)weapon).wandClass();
			if (w != null) seen.add( w );
		}
		if (h.belongings.backpack != null) {
			for (Item it : h.belongings.backpack.items) {
				if (it instanceof Trinket || it instanceof AmuletOfRenewal || it instanceof AmuletOfAegis || it instanceof SetRelic) {
					seen.add( it.getClass() );
				}
			}
		}
		for (Class<?> c : seen) {
			int s = setOf( c );
			if (s >= 0) n[s]++;
		}
		return n;
	}

	public static Effects of( Hero h ) {
		return effects( pieceCounts( h ) );
	}

	private static void heal( Hero h, int amount ) {
		if (h.sprite == null) h.HP = Math.min( h.HT, h.HP + amount ); else h.heal( amount );
	}

	// ---- what the rest of the game asks for ----

	public static int outgoing( Hero h, int damage ) {
		return outgoing( h, null, damage );
	}

	/** a melee blow about to land: its damage after the sets, and the heal some of them give */
	public static int outgoing( Hero h, Char enemy, int damage ) {
		if (damage <= 0) return damage;
		Effects e = of( h );
		float m = e.damage;
		if (e.lowHp > 0f && h.HT > 0 && h.HP <= h.HT * 0.35f) m *= 1f + e.lowHp;
		if (enemy != null) {
			if (e.sneak > 0f && enemy instanceof Mob && ((Mob)enemy).surprisedBy( h, true )) m *= 1f + e.sneak;
			if (e.vsWeak > 0f && enemy.HP <= enemy.HT * 0.30f) m *= 1f + e.vsWeak;
			if (e.vsFull > 0f && enemy.HP >= enemy.HT) m *= 1f + e.vsFull;
		}
		int out = m == 1f ? damage : Math.max( 1, Math.round( damage * m ) );
		if (e.lifesteal > 0f && h.isAlive()) {
			heal( h, Math.max( 1, Math.round( out * e.lifesteal ) ) );
		}
		return out;
	}

	public static class LastStandCooldown extends FlavourBuff {}

	/** a blow about to land on the hero: its damage after the sets, the Yog-Forged last stand, and
	 *  the thorns some of them give */
	public static int incoming( Hero h, int dmg, Object src ) {
		if (dmg <= 0) return dmg;
		Effects e = of( h );
		int in = e.taken == 1f ? dmg : Math.max( 1, Math.round( dmg * Math.max( 0.4f, e.taken ) ) );
		if (e.lastStand && h.buff( LastStandCooldown.class ) == null && in >= h.HP + h.shielding()) {
			heal( h, Math.round( h.HT * 0.30f ) );
			in = Math.max( 0, Math.min( in, h.HP + h.shielding() - 1 ) );
			Buff.affect( h, LastStandCooldown.class, 300f );
		}
		if (e.thorns > 0f && src instanceof Char && src != h) {
			Char striker = (Char)src;
			if (striker.isAlive() && striker.alignment != Char.Alignment.ALLY) {
				striker.damage( Math.max( 1, Math.round( in * e.thorns ) ), h );
			}
		}
		return in;
	}

	public static float evasionMultiplier( Hero h ) { return of( h ).evasion; }
	public static float accuracyMultiplier( Hero h ) { return of( h ).accuracy; }
	public static float speedMultiplier( Hero h ) { return of( h ).speed; }
	public static float expMultiplier( Hero h ) { return of( h ).exp; }

	// ---- the Mage set's wands ----
	public static float wandDamageMultiplier() {
		return Dungeon.hero == null ? 1f : of( Dungeon.hero ).wandDamage;
	}

	public static float wandRechargeMultiplier( Char owner ) {
		return owner instanceof Hero ? of( (Hero)owner ).wandRecharge : 1f;
	}

	public static boolean freeZap() {
		if (Dungeon.hero == null) return false;
		float chance = of( Dungeon.hero ).freeZap;
		return chance > 0f && Random.Float() < chance;
	}

	public static void afterZap() {
		Hero h = Dungeon.hero;
		if (h == null) return;
		float pct = of( h ).zapBarrier;
		if (pct > 0f && h.isAlive()) {
			Buff.affect( h, Barrier.class ).incShield( Math.max( 1, Math.round( h.HT * pct ) ) );
		}
	}

	private static int regen = 0, barrier = 0;

	/** once per hero turn: the regenerating sets and the barrier-making ones */
	public static void tick( Hero h ) {
		Effects e = of( h );
		if (e.regenEvery <= 0) {
			regen = 0;
		} else if (++regen >= e.regenEvery) {
			regen = 0;
			if (h.isAlive() && h.HP < h.HT) heal( h, 1 );
		}
		if (e.barrierEvery <= 0) {
			barrier = 0;
		} else if (++barrier >= e.barrierEvery) {
			barrier = 0;
			int cap = Math.max( 2, Math.round( h.HT * e.barrierPct * 2 ) );
			int give = Math.max( 1, Math.round( h.HT * e.barrierPct ) );
			if (h.isAlive() && h.shielding() < cap) {
				Buff.affect( h, Barrier.class ).incShield( Math.min( give, cap - h.shielding() ) );
			}
		}
	}

	/** the line an item's description carries when it belongs to a set (empty otherwise) */
	public static String describe( Item item ) {
		int s = setOf( item.getClass() );
		if (s < 0) return "";
		int have = Dungeon.hero == null ? 0 : pieceCounts( Dungeon.hero )[s];
		return "\n\n_Set: " + NAMES[s] + "_ (" + have + "/" + PIECES[s].length + " owned). Each piece beyond the first: "
				+ PER_TIER[s] + ". " + EXTRA[s];
	}
}
