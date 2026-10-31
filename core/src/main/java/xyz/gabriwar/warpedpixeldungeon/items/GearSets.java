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
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.*;
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
			"Yog-Forged", "Ascended Warlord's Regalia", "Ascended Archmage's Vestments", "Ascended Shadowblade's Shroud", "Ascended Warden's Hunt", "Ascended Duelist's Flourish", "Ascended Saint's Raiment", "Mythic Warlord's Regalia", "Mythic Archmage's Vestments", "Mythic Shadowblade's Shroud", "Mythic Warden's Hunt", "Mythic Duelist's Flourish", "Mythic Saint's Raiment", "Godforged Warlord's Regalia", "Godforged Archmage's Vestments", "Godforged Shadowblade's Shroud", "Godforged Warden's Hunt", "Godforged Duelist's Flourish", "Godforged Saint's Raiment",
      "Gunslinger's Rig", "Ascended Gunslinger's Rig", "Mythic Gunslinger's Rig", "Godforged Gunslinger's Rig" };

	/** what one tier of each set gives, in words (the item descriptions quote it) */
	private static final String[] BASE_PER = {
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
			"+6% damage dealt, -4% damage taken, heal for 1.5% of the damage you deal, +2% movement speed and +6% experience",
      "+5% damage dealt, +4% accuracy and 5% faster reloads" };

	/** what a set keeps back for its later pieces (empty for none) */
	private static final String[] BASE_EXTRA = {
			"A full set of five is worth four times that.", "A full set of five is worth four times that.", "A full set of five is worth four times that.",
			"A full set of five is worth four times that.", "A full set of five is worth four times that.", "A full set of five is worth four times that.",
			"With four pieces you also heal for 4% of the damage you deal; with all five you deal 25% more damage while at 35% health or less.",
			"With four pieces each wand zap has a 10% chance to cost nothing; with all five each zap also wraps you in a barrier of 5% of your health.",
			"With four pieces you deal 25% more damage to enemies at 30% health or less; with all five you move 10% faster.",
			"With four pieces you move 6% faster; with all five 10% of every blow you take is returned to the attacker.",
			"With all five pieces you deal a further 10% more damage.",
			"With four pieces a barrier of 3% of your health forms every 20 turns; with all five you also heal for 5% of the damage you deal.",
			"With the full set of six, once every 300 turns a killing blow leaves you alive and heals you for 30% of your health.",
      "With four pieces each shot has an 8% chance not to use a round; with all five you also deal a further 10% more damage." };

	//the tier 7, 8 and 9 sets repeat a class set's bonuses, stronger: the same words with the scale
	public static final float[] TIER_SCALE = { 1f, 1.25f, 1.5f, 2f };
	public static final int FIRST_T7 = 13, FIRST_T8 = 19, FIRST_T9 = 25;

	public static final int GUN_FIRST = 31;   //the Gunslinger's Rig: tier 6 at 31, then 7, 8 and 9

	public static final String[] PER_TIER = new String[35], EXTRA = new String[35];
	static {
		for (int i = 0; i < 35; i++) {
			int base = i >= GUN_FIRST ? 13 : i < 13 ? i : 6 + (i - 13) % 6;
			float m = i >= GUN_FIRST ? TIER_SCALE[i - GUN_FIRST] : i < 13 ? 1f : TIER_SCALE[(i - 13) / 6 + 1];
			PER_TIER[i] = BASE_PER[base] + (m == 1f ? "" : " (x" + (m == 1.25f ? "1.25" : m == 1.5f ? "1.5" : "2") + ")");
			EXTRA[i] = BASE_EXTRA[base];
		}
	}

	// ---------------------------------------------------------------- tier and strength
	//
	//Every piece of the new gear has a tier from 6 to 9 (the general sets and the first class sets are 6,
	//Ascended 7, Mythic 8, Godforged and Yog-Forged 9), and it is what the item's text says and what
	//its Strength requirement follows: a weapon asks 18, 20, 22 or 24, an armor 17, 19, 21 or 23, less
	//the usual small reduction for the item's level.

	private static final Class<?>[] EXTRA_GEAR = { HollowwoodStaff.class, IronboundStaff.class, WyrmboneStaff.class };

	public static int tierOfSet( int set ) {
		if (set >= GUN_FIRST) return 6 + (set - GUN_FIRST);   //the Gunslinger's Rig, tiers 6 to 9
		if (set == YOG) return 9;
		if (set < GENERIC_SETS) return 5;   //the general sets: tier 5
		if (set < FIRST_T7) return 6;
		if (set < FIRST_T8) return 7;
		if (set < FIRST_T9) return 8;
		return 9;
	}

	/** the class a gear piece counts as: a class armor made from a set piece is still that piece */
	public static Class<?> pieceClass( Object it ) {
		if (it instanceof ClassArmor && ((ClassArmor)it).origin != null) return ((ClassArmor)it).origin;
		return it == null ? null : it.getClass();
	}

	/** the tier (6 to 9) of a piece of the new gear, or -1 for anything else */
	public static int gearTier( Item it ) {
		if (it == null) return -1;
		Class<?> c = pieceClass( it );
		int s = setOf( c );
		if (s >= 0) return tierOfSet( s );
		for (Class<?> x : EXTRA_GEAR) if (x == c) return 5;
		return -1;
	}

	/** the tier an item's text shows: its own set tier if it has one, otherwise what it always showed */
	public static int tierLabel( Item it, int fallback ) {
		int t = gearTier( it );
		return t > 0 ? t : fallback;
	}

	/** the Strength an item of level lvl lowers its requirement by: one at +1, two at +3, three at +6, and so on */
	public static int levelReduction( int lvl ) {
		return (int)(Math.sqrt( 8 * Math.max( 0, lvl ) + 1 ) - 1) / 2;
	}

	public static int strengthFor( Item it, int lvl ) {
		int t = gearTier( it );
		int base = (it instanceof Armor ? 17 : 18) + 2 * (t - 6);
		if (pieceClass( it ) == Duskplate.class) base -= 2;   //the lighter plate
		return Math.max( 8, base - levelReduction( lvl ) );
	}

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
			{ AscendedWarlordsPlate.class, AscendedWarlordsGreatsword.class, AscendedWarlordsSignet.class, AscendedWarlordsBanner.class, AscendedWarlordsCrest.class },
			{ AscendedArchmagesRobe.class, AscendedArchmagesStaff.class, AscendedArchmagesBand.class, AscendedArchmagesOrb.class, AscendedArchmagesSigil.class },
			{ AscendedShadowbladesCloak.class, AscendedShadowbladesDirk.class, AscendedShadowbladesRing.class, AscendedShadowbladesMask.class, AscendedShadowbladesToken.class },
			{ AscendedWardensHide.class, AscendedWardensGlaive.class, AscendedWardensRing.class, AscendedWardensHorn.class, AscendedWardensTotem.class },
			{ AscendedDuelistsCoat.class, AscendedDuelistsRapier.class, AscendedDuelistsSignet.class, AscendedDuelistsGlove.class, AscendedDuelistsMedal.class },
			{ AscendedSaintsVestment.class, AscendedSaintsMace.class, AscendedSaintsHalo.class, AscendedSaintsChalice.class, AscendedSaintsRelic.class },
			{ MythicWarlordsPlate.class, MythicWarlordsGreatsword.class, MythicWarlordsSignet.class, MythicWarlordsBanner.class, MythicWarlordsCrest.class },
			{ MythicArchmagesRobe.class, MythicArchmagesStaff.class, MythicArchmagesBand.class, MythicArchmagesOrb.class, MythicArchmagesSigil.class },
			{ MythicShadowbladesCloak.class, MythicShadowbladesDirk.class, MythicShadowbladesRing.class, MythicShadowbladesMask.class, MythicShadowbladesToken.class },
			{ MythicWardensHide.class, MythicWardensGlaive.class, MythicWardensRing.class, MythicWardensHorn.class, MythicWardensTotem.class },
			{ MythicDuelistsCoat.class, MythicDuelistsRapier.class, MythicDuelistsSignet.class, MythicDuelistsGlove.class, MythicDuelistsMedal.class },
			{ MythicSaintsVestment.class, MythicSaintsMace.class, MythicSaintsHalo.class, MythicSaintsChalice.class, MythicSaintsRelic.class },
			{ GodforgedWarlordsPlate.class, GodforgedWarlordsGreatsword.class, GodforgedWarlordsSignet.class, GodforgedWarlordsBanner.class, GodforgedWarlordsCrest.class },
			{ GodforgedArchmagesRobe.class, GodforgedArchmagesStaff.class, GodforgedArchmagesBand.class, GodforgedArchmagesOrb.class, GodforgedArchmagesSigil.class },
			{ GodforgedShadowbladesCloak.class, GodforgedShadowbladesDirk.class, GodforgedShadowbladesRing.class, GodforgedShadowbladesMask.class, GodforgedShadowbladesToken.class },
			{ GodforgedWardensHide.class, GodforgedWardensGlaive.class, GodforgedWardensRing.class, GodforgedWardensHorn.class, GodforgedWardensTotem.class },
			{ GodforgedDuelistsCoat.class, GodforgedDuelistsRapier.class, GodforgedDuelistsSignet.class, GodforgedDuelistsGlove.class, GodforgedDuelistsMedal.class },
			{ GodforgedSaintsVestment.class, GodforgedSaintsMace.class, GodforgedSaintsHalo.class, GodforgedSaintsChalice.class, GodforgedSaintsRelic.class },
			{ GunslingersCoat.class, GunslingersPeacemaker.class, GunslingersSignet.class, GunslingersBelt.class, GunslingersLuckyCoin.class },
			{ AscendedGunslingersCoat.class, AscendedGunslingersPeacemaker.class, AscendedGunslingersSignet.class, AscendedGunslingersBelt.class, AscendedGunslingersLuckyCoin.class },
			{ MythicGunslingersCoat.class, MythicGunslingersPeacemaker.class, MythicGunslingersSignet.class, MythicGunslingersBelt.class, MythicGunslingersLuckyCoin.class },
			{ GodforgedGunslingersCoat.class, GodforgedGunslingersPeacemaker.class, GodforgedGunslingersSignet.class, GodforgedGunslingersBelt.class, GodforgedGunslingersLuckyCoin.class },
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
		/** firearms: the multiplier on a reload's time, and the chance a shot does not use a round */
		public float reload = 1f, spare = 0f;
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

		int yog = tier( YOG, p( pieces, YOG ) );

		//the six class sets, and their tier 7, 8 and 9 forms: the same bonuses, scaled
		for (int f = 0; f < 6; f++) {
			classSet( e, f, tier( 6 + f, p( pieces, 6 + f ) ), 1f );
			classSet( e, f, tier( FIRST_T7 + f, p( pieces, FIRST_T7 + f ) ), TIER_SCALE[1] );
			classSet( e, f, tier( FIRST_T8 + f, p( pieces, FIRST_T8 + f ) ), TIER_SCALE[2] );
			classSet( e, f, tier( FIRST_T9 + f, p( pieces, FIRST_T9 + f ) ), TIER_SCALE[3] );
		}

		//the Gunslinger's Rig, in its four tiers
		for (int k = 0; k < 4; k++) classSet( e, 6, tier( GUN_FIRST + k, p( pieces, GUN_FIRST + k ) ), TIER_SCALE[k] );

		e.damage += 0.06f * yog;
		e.taken -= 0.04f * yog;
		e.lifesteal += 0.015f * yog;
		e.speed += 0.02f * yog;
		e.exp += 0.06f * yog;
		if (yog >= 5) e.lastStand = true;
		return e;
	}

	/** One class set's bonus for this many steps (pieces beyond the first), scaled by m: a class set
	 *  is family 0 Warrior, 1 Mage, 2 Rogue, 3 Huntress, 4 Duelist, 5 Cleric, 6 Gunslinger. */
	private static void classSet( Effects e, int family, int steps, float m ) {
		if (steps <= 0) return;
		switch (family) {
			case 0:
				e.damage += 0.06f * steps * m;
				e.taken -= 0.03f * steps * m;
				if (steps >= 3) e.lifesteal += 0.04f * m;
				if (steps >= 4) e.lowHp = Math.max( e.lowHp, 0.25f * m );
				break;
			case 1:
				e.wandDamage += 0.08f * steps * m;
				e.wandRecharge += 0.08f * steps * m;
				if (steps >= 3) e.freeZap = Math.max( e.freeZap, Math.min( 0.5f, 0.10f * m ) );
				if (steps >= 4) e.zapBarrier = Math.max( e.zapBarrier, 0.05f * m );
				break;
			case 2:
				e.evasion += 0.04f * steps * m;
				e.sneak += 0.12f * steps * m;
				if (steps >= 3) e.vsWeak = Math.max( e.vsWeak, 0.25f * m );
				if (steps >= 4) e.speed += 0.10f * m;
				break;
			case 3:
				e.accuracy += 0.05f * steps * m;
				e.vsFull += 0.10f * steps * m;
				if (steps >= 3) e.speed += 0.06f * m;
				if (steps >= 4) e.thorns += 0.10f * m;
				break;
			case 4:
				e.damage += 0.04f * steps * m;
				e.accuracy += 0.03f * steps * m;
				e.evasion += 0.03f * steps * m;
				if (steps >= 4) e.damage += 0.10f * m;
				break;
			case 6:
				e.damage += 0.05f * steps * m;
				e.accuracy += 0.04f * steps * m;
				e.reload -= 0.05f * steps * m;
				if (steps >= 3) e.spare = Math.max( e.spare, 0.08f * m );
				if (steps >= 4) e.damage += 0.10f * m;
				break;
			default:
				e.taken -= 0.03f * steps * m;
				int every = Math.max( 6, Math.round( (26 - 4 * steps) / m ) );
				e.regenEvery = e.regenEvery == 0 ? every : Math.min( e.regenEvery, every );
				if (steps >= 3) {
					e.barrierEvery = 20;
					e.barrierPct = Math.max( e.barrierPct, 0.03f * m );
				}
				if (steps >= 4) e.lifesteal += 0.05f * m;
				break;
		}
	}

	private static void count( Object it, Set<Class<?>> seen ) {
		if (it != null) seen.add( it.getClass() );
	}

	/** how many pieces of each set the hero has on or carries */
	public static int[] pieceCounts( Hero h ) {
		int[] n = new int[PIECES.length];
		if (h == null || h.belongings == null) return n;
		Set<Class<?>> seen = new HashSet<>();
		Class<?> wornClass = pieceClass( h.belongings.armor() );
		if (wornClass != null) seen.add( wornClass );
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
				//a general set has two weapons, two armors or a wand among its five, which no hero can wield at once: its spare pieces count carried
				int carriedSet = setOf( it.getClass() );
				if (it instanceof Trinket || it instanceof AmuletOfRenewal || it instanceof AmuletOfAegis || it instanceof SetRelic
						|| (carriedSet >= 0 && carriedSet < GENERIC_SETS)) {
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

	/** does the hero have this piece, worn or carried (a class armor made from it counts)? */
	public static boolean owned( Hero h, Class<?> c ) {
		if (h == null || h.belongings == null) return false;
		for (Item it : h.belongings) {
			if (it != null && pieceClass( it ) == c) return true;
		}
		return false;
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
		int s = setOf( pieceClass( item ) );
		if (s < 0) return "";
		int have = Dungeon.hero == null ? 0 : pieceCounts( Dungeon.hero )[s];
		String where = "";
		int tier = item instanceof SetTier ? ((SetTier)item).setTier() : 6;
		if (tier == 8) where = "\n\nOnly found from the second new cycle (cycle 2) on.";
		else if (tier == 9 || s == YOG) where = "\n\nOnly found from the third new cycle (cycle 3) on.";
		return "\n\n_Set: " + NAMES[s] + "_ (" + have + "/" + PIECES[s].length + " owned), a tier " + tierOfSet( s ) + " piece. Each piece beyond the first: "
				+ PER_TIER[s] + ". " + EXTRA[s] + where;
	}
}
