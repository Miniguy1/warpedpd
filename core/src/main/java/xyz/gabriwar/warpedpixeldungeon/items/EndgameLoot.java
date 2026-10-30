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

import xyz.gabriwar.warpedpixeldungeon.Cycle;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass;
import xyz.gabriwar.warpedpixeldungeon.items.armor.Armor;
import xyz.gabriwar.warpedpixeldungeon.items.rings.Ring;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.utils.Reflection;
import xyz.gabriwar.warpedpixeldungeon.items.armor.BleedingArmor;
import xyz.gabriwar.warpedpixeldungeon.items.armor.DragonscaleMail;
import xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire;
import xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfShattering;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfRetribution;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfVampirism;
import xyz.gabriwar.warpedpixeldungeon.items.trinkets.SagesIdol;
import xyz.gabriwar.warpedpixeldungeon.items.trinkets.StoneheartPendant;
import xyz.gabriwar.warpedpixeldungeon.items.trinkets.WarlordsMedal;
import xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfCombustion;
import xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfFreeze;
import xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfSiphon;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.BloodmoonAxe;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dawnbreaker;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.HollowwoodStaff;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.IronboundStaff;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.ObsidianGreataxe;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Stormbreaker;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Voidreaver;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WyrmboneStaff;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WyrmfangGlaive;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import com.watabou.utils.Random;

/** Every one of the new items, and the only places they turn up: the dark mine floors, and the
 *  floors rebuilt on the climb out of the dungeon (carrying the cursed amulet). Nothing here is in
 *  the normal loot tables. */
public class EndgameLoot {

	private static final int KINDS = 35;

	/** the share of rolls that are a piece of the hero's own class set */
	public static final float CLASS_SHARE = 0.30f;

	/** a random new item: a piece of the hero's own class set about three times in ten, otherwise
	 *  anything from the general pool */
	public static Item roll() {
		return rollRated( 6f );
	}

	// ---------------------------------------------------------------- tiers and ratings
	//
	//The class sets come in four tiers: 6 (the first sets), 7 (Ascended), 8 (Mythic) and 9 (Godforged),
	//each stronger. Every place that drops them has a RATING, a number from 6 to 9: a rating of 7.4 drops
	//tier 7 six times in ten and tier 8 four times in ten. The harder the place, the higher its rating.

	/** the rating of the floors rebuilt on the climb with the cursed amulet */
	public static final float ASCENT_RATING = 6.0f;

	/** the highest tier the loot can be: 7 in the first run through the dungeon, 8 from the first new cycle
	 *  (New Game+), 9 from the second. The top tiers are the new cycles' reward. */
	public static int maxTier() {
		//tier 7 is the most a first run or the first new cycle can find (a low chance, from bosses); then one more each cycle
		return 7 + Math.min( 2, Math.max( 0, Cycle.number - 1 ) );
	}

	/** the share of the dungeon's item drops that are pieces of the hero's own class set, which grows with the cycle as the general pool (tier 5) falls behind */
	public static float classShare() {
		return Cycle.number <= 1 ? CLASS_SHARE : Cycle.number == 2 ? 0.60f : Cycle.number == 3 ? 0.85f : 1f;
	}

	/** the tier a rating drops on this roll: its whole part, or the next one up with the fraction's
	 *  chance, and never past the highest tier the cycle allows */
	public static int tierOfRating( float rating ) {
		int cap = maxTier();
		float r = Math.max( 6f, Math.min( (float)cap, rating ) );
		int t = (int)Math.floor( r );
		if (t < cap && Random.Float() < r - t) t++;
		return t;
	}

	/** a place whose rating is past what the cycle allows still pays for it: two bonus levels on a piece
	 *  for each whole tier of the rating over the cap (up to four) */
	public static int excessLevels( float rating ) {
		float over = Math.min( 9f, rating ) - maxTier();
		return over > 0f ? Math.min( 4, Math.round( over * 2f ) ) : 0;
	}

	/** the rating of a plaza zone's loot: the zone's own, and half a tier more for each time it has been cleared */
	public static float zoneRating( int zone, int zoneTier ) {
		//a plaza zone's loot: tier 6, and a small chance of 7 that grows a little with the zone and with each clearing
		return Math.min( 9f, 6.0f + 0.05f * zone + 0.05f * zoneTier );
	}

	/** the rating of a dark mine floor: 7.5 at the top, 8.3 at the bottom (the boss floor pays more) */
	public static float minesRating( int depth ) {
		return Math.max( 6.1f, Math.min( 6.4f, 6.1f + 0.02f * (depth - 56) ) );
	}

	/** a random new item for this rating: a piece of the hero's own class set of the tier it drops, three
	 *  times in ten, otherwise anything from the general pool */
	public static Item rollRated( float rating ) {
		Hero h = Dungeon.hero;
		if (h != null && h.heroClass != null && Random.Float() < classShare()) {
			return rollClassSet( h.heroClass, tierOfRating( rating ), excessLevels( rating ) );
		}
		return rollGeneral();
	}

	/** one piece of a class's own set, of tier 6, 7, 8 or 9 */
	public static Item rollClassSet( HeroClass cls, int tier ) {
		return rollClassSet( cls, tier, 0 );
	}

	/** the same, with bonus levels on top */
	public static Item rollClassSet( HeroClass cls, int tier, int extraLevels ) {
		if (cls == HeroClass.GUNSLINGER) return fromSet( GearSets.GUN_FIRST + Math.max( 0, Math.min( 3, tier - 6 ) ), extraLevels );
		int family;
		switch (cls) {
			case WARRIOR: default: family = 0; break;
			case MAGE:     family = 1; break;
			case ROGUE:    family = 2; break;
			case HUNTRESS: family = 3; break;
			case DUELIST:  family = 4; break;
			case CLERIC:   family = 5; break;
		}
		int first = tier >= 9 ? GearSets.FIRST_T9 : tier == 8 ? GearSets.FIRST_T8 : tier == 7 ? GearSets.FIRST_T7 : 6;
		return fromSet( first + family, extraLevels );
	}

	/** identified, never cursed; armor, weapons and rings at a small bonus level */
	static Item prepare( Item item ) {
		return prepare( item, 0 );
	}

	static Item prepare( Item item, int extra ) {
		int tier = item instanceof SetTier ? ((SetTier)item).setTier() : 6;
		//a higher tier starts at a higher level too: 7 at +2 to +4, 8 at +4 to +6, 9 at +6 to +8
		int bonus = tier <= 6 ? 0 : 2 * (tier - 6);
		if (item instanceof Armor || (item instanceof MeleeWeapon && !(item instanceof MagesStaff))) {
			item.level( extra + bonus + Random.IntRange( 1, 3 ) - (tier > 6 ? 1 : 0) );
		} else if (item instanceof Ring) {
			item.level( extra + bonus + Random.IntRange( 0, 2 ) );
		}
		item.cursed = false;
		item.identify();
		return item;
	}

	private static Item fromSet( int set ) {
		return fromSet( set, 0 );
	}

	private static Item fromSet( int set, int extra ) {
		Class<?>[] pieces = GearSets.piecesOf( set );
		Item item = (Item)Reflection.newInstance( pieces[Random.Int( pieces.length )] );
		return item == null ? rollGeneral() : prepare( item, extra );
	}

	/** one piece of a class's own set */
	public static Item rollClassSet( HeroClass cls ) {
		switch (cls) {
			case WARRIOR: default: return fromSet( 6 );
			case MAGE:     return fromSet( 7 );
			case ROGUE:    return fromSet( 8 );
			case HUNTRESS: return fromSet( 9 );
			case DUELIST:  return fromSet( 10 );
			case CLERIC:   return fromSet( 11 );
			case GUNSLINGER: return fromSet( GearSets.GUN_FIRST );
		}
	}

	/** one piece of the Yog-Forged set */
	public static Item rollYog() {
		//the Yog-Forged set is a new cycle's reward: in the first run the same place pays the best the run can
		//offer, a tier 7 piece of the hero's own set with two bonus levels
		if (Cycle.number < 3) {
			Hero h = Dungeon.hero;
			return h != null && h.heroClass != null ? rollClassSet( h.heroClass, 7, 2 ) : rollGeneral();
		}
		return fromSet( GearSets.YOG );
	}

	/** the dark mines: the general pool, and now and then a Yog-Forged piece */
	public static void populateMines( Level level ) {
		populate( level, 0.35f, 0.08f, minesRating( Dungeon.depth ) );
		if (Random.Float() < 0.06f) {
			int cell = level.randomDestination( null );
			if (cell != -1) level.drop( rollYog(), cell );
		}
	}

	/** one random new item, already identified. Armor and weapons come at +1 to +3, rings and wands
	 *  at their own random levels, the rest as they are. Never cursed. */
	public static Item rollGeneral() {
		Item item;
		switch (Random.Int( KINDS )) {
			//wands
			case 0:  item = new WandOfCombustion().random(); break;
			case 1:  item = new WandOfFreeze().random();     break;
			case 2:  item = new WandOfSiphon().random();     break;
			//staffs
			case 3:  item = new HollowwoodStaff();           break;
			case 4:  item = new IronboundStaff();            break;
			case 5:  item = new WyrmboneStaff();             break;
			//armor
			case 6:  item = new RobeOfFire(); break;
			case 7:  item = new BleedingArmor(); break;
			case 8:  item = new RobeOfShattering(); break;
			case 9:  item = new DragonscaleMail(); break;
			//weapons
			case 10: item = new Dawnbreaker(); break;
			case 11: item = new Voidreaver(); break;
			case 12: item = new BloodmoonAxe(); break;
			case 13: item = new Stormbreaker(); break;
			case 14: item = new ObsidianGreataxe(); break;
			case 15: item = new WyrmfangGlaive(); break;
			//rings
			case 16: item = new RingOfVampirism();
					item.level( Random.IntRange( 0, 2 ) ); break;
			case 17: item = new RingOfRetribution();
					item.level( Random.IntRange( 0, 2 ) ); break;
			//amulets
			case 18: item = new AmuletOfRenewal();  break;
			case 19: item = new AmuletOfAegis();    break;
			//trinkets
			case 20: item = new WarlordsMedal();    break;
			case 21: item = new SagesIdol();        break;
			case 22: item = new StoneheartPendant();break;
			//the second set
			case 23: item = new xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfEcho().random(); break;
			case 24: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Frostbrand(); break;
			case 25: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Thunderlance(); break;
			case 26: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Gravesplitter(); break;
			case 27: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Emberfang(); break;
			case 28: item = new xyz.gabriwar.warpedpixeldungeon.items.armor.Thornmail(); break;
			case 29: item = new xyz.gabriwar.warpedpixeldungeon.items.armor.Duskplate(); break;
			case 30: item = new xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfBulwark();
					item.level( Random.IntRange( 0, 2 ) ); break;
			case 31: item = new xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfCruelty();
					item.level( Random.IntRange( 0, 2 ) ); break;
			case 32: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.HuntersFang();   break;
			case 33: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.WayfarersBoots(); break;
			default: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.EmberCharm();     break; //34
		}
		return prepare( item );
	}

	/** a chance of one item, and a smaller chance of a second, of this rating */
	public static void populate( Level level, float first, float second, float rating ) {
		int count = 0;
		if (Random.Float() < first) count++;
		if (Random.Float() < second) count++;
		for (int i = 0; i < count; i++) {
			int cell = level.randomDestination( null );
			if (cell != -1) level.drop( rollRated( rating ), cell );
		}
	}

	/** the plain form: rating 6 */
	public static void populate( Level level, float first, float second ) {
		populate( level, first, second, 6f );
	}

	/** floors rebuilt on the climb: tier 7 */
	public static void populate( Level level ) {
		populate( level, 0.60f, 0.15f, ASCENT_RATING );
	}
}
