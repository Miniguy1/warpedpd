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
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.armor.*;
import xyz.gabriwar.warpedpixeldungeon.items.rings.*;
import xyz.gabriwar.warpedpixeldungeon.items.trinkets.*;
import xyz.gabriwar.warpedpixeldungeon.items.wands.*;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.*;

import java.util.HashSet;
import java.util.Set;

/** Matching sets among the new items. Wear (or carry) several pieces of one set and it pays out:
 *  every piece beyond the first is worth one "tier" of the set's bonus, up to a full set of five.
 *
 *  What counts as a piece: the armor you wear, the weapon you wield (a staff counts as the wand it
 *  holds), the rings you wear, and the trinkets and amulets in your pack (they work carried). A
 *  piece counts once however many copies you hold. Nothing here is stored: it is read from what
 *  the hero has on, every time it is asked. */
public class GearSets {

	public static final String[] NAMES = { "Emberwrought", "Frostbound", "Bloodbound", "Stonewarden", "Stormcaller", "Wanderer's Vigil" };

	/** what one tier of each set gives, in words (the item descriptions quote it) */
	public static final String[] PER_TIER = {
			"+8% damage dealt",
			"-4% damage taken and +4% evasion",
			"heal for 3% of the damage you deal",
			"-4% damage taken and 4% of every blow you take returned to the attacker",
			"+5% accuracy and +3% movement speed",
			"+8% experience, and a slow regeneration that quickens with each tier" };

	public static final int MAX_TIER = 4;

	@SuppressWarnings("unchecked")
	private static final Class<?>[][] PIECES = {
			{ RobeOfFire.class, Dawnbreaker.class, Emberfang.class, EmberCharm.class, WandOfCombustion.class },
			{ RobeOfShattering.class, Frostbrand.class, WandOfFreeze.class, WayfarersBoots.class, RingOfBulwark.class },
			{ BleedingArmor.class, BloodmoonAxe.class, Voidreaver.class, RingOfVampirism.class, WarlordsMedal.class },
			{ DragonscaleMail.class, ObsidianGreataxe.class, Gravesplitter.class, StoneheartPendant.class, Thornmail.class },
			{ Thunderlance.class, Stormbreaker.class, WandOfEcho.class, HuntersFang.class, RingOfCruelty.class },
			{ Duskplate.class, WyrmfangGlaive.class, SagesIdol.class, AmuletOfRenewal.class, AmuletOfAegis.class },
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

	public static int tier( int pieces ) {
		return Math.max( 0, Math.min( MAX_TIER, pieces - 1 ) );
	}

	/** everything the sets add up to, for a number of pieces in each set */
	public static class Effects {
		public float damage = 1f, taken = 1f, evasion = 1f, accuracy = 1f, speed = 1f, exp = 1f;
		public float lifesteal = 0f, thorns = 0f;
		/** turns between the one point of health the Vigil gives, or 0 for none */
		public int regenEvery = 0;
	}

	/** pure: the effects of owning this many pieces of each set */
	public static Effects effects( int[] pieces ) {
		Effects e = new Effects();
		int ember = tier( pieces[0] ), frost = tier( pieces[1] ), blood = tier( pieces[2] ),
				stone = tier( pieces[3] ), storm = tier( pieces[4] ), vigil = tier( pieces[5] );
		e.damage += 0.08f * ember;
		e.taken -= 0.04f * frost + 0.04f * stone;
		e.evasion += 0.04f * frost;
		e.lifesteal = 0.03f * blood;
		e.thorns = 0.04f * stone;
		e.accuracy += 0.05f * storm;
		e.speed += 0.03f * storm;
		e.exp += 0.08f * vigil;
		e.regenEvery = vigil > 0 ? 30 - 4 * vigil : 0;
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
		if (weapon instanceof MagesStaff) {
			Class<?> w = ((MagesStaff)weapon).wandClass();
			if (w != null) seen.add( w );
		} else {
			count( weapon, seen );
		}
		if (h.belongings.backpack != null) {
			for (Item it : h.belongings.backpack.items) {
				if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.trinkets.Trinket
						|| it instanceof AmuletOfRenewal || it instanceof AmuletOfAegis) {
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

	// ---- what the rest of the game asks for ----

	/** a melee blow about to land: its damage after the sets, and the Bloodbound's heal */
	public static int outgoing( Hero h, int damage ) {
		if (damage <= 0) return damage;
		Effects e = of( h );
		int out = e.damage == 1f ? damage : Math.max( 1, Math.round( damage * e.damage ) );
		if (e.lifesteal > 0f && h.isAlive()) {
			int heal = Math.max( 1, Math.round( out * e.lifesteal ) );
			if (h.sprite == null) h.HP = Math.min( h.HT, h.HP + heal ); else h.heal( heal );
		}
		return out;
	}

	/** a blow about to land on the hero: its damage after the sets, and the Stonewarden's thorns */
	public static int incoming( Hero h, int dmg, Object src ) {
		if (dmg <= 0) return dmg;
		Effects e = of( h );
		int in = e.taken == 1f ? dmg : Math.max( 1, Math.round( dmg * Math.max( 0.4f, e.taken ) ) );
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

	private static int vigil = 0;

	/** once per hero turn: the Vigil's slow regeneration */
	public static void tick( Hero h ) {
		int every = of( h ).regenEvery;
		if (every <= 0) { vigil = 0; return; }
		if (++vigil >= every) {
			vigil = 0;
			if (h.isAlive() && h.HP < h.HT) {
				if (h.sprite == null) h.HP = Math.min( h.HT, h.HP + 1 ); else h.heal( 1 );
			}
		}
	}

	/** the line an item's description carries when it belongs to a set (empty otherwise) */
	public static String describe( Item item ) {
		int s = setOf( item.getClass() );
		if (s < 0) return "";
		int have = Dungeon.hero == null ? 0 : pieceCounts( Dungeon.hero )[s];
		return "\n\n_Set: " + NAMES[s] + "_ (" + have + "/" + PIECES[s].length + " owned). Each piece beyond the first: "
				+ PER_TIER[s] + ". A full set of five is worth four times that.";
	}
}
