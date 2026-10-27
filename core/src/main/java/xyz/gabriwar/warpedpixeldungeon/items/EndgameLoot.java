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
		Hero h = Dungeon.hero;
		if (h != null && h.heroClass != null && Random.Float() < CLASS_SHARE) {
			return rollClassSet( h.heroClass );
		}
		return rollGeneral();
	}

	/** identified, never cursed; armor, weapons and rings at a small bonus level */
	static Item prepare( Item item ) {
		if (item instanceof Armor || (item instanceof MeleeWeapon && !(item instanceof MagesStaff))) {
			item.level( Random.IntRange( 1, 3 ) );
		} else if (item instanceof Ring) {
			item.level( Random.IntRange( 0, 2 ) );
		}
		item.cursed = false;
		item.identify();
		return item;
	}

	private static Item fromSet( int set ) {
		Class<?>[] pieces = GearSets.piecesOf( set );
		Item item = (Item)Reflection.newInstance( pieces[Random.Int( pieces.length )] );
		return item == null ? rollGeneral() : prepare( item );
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
		}
	}

	/** one piece of the Yog-Forged set */
	public static Item rollYog() {
		return fromSet( GearSets.YOG );
	}

	/** the dark mines: the general pool, and now and then a Yog-Forged piece */
	public static void populateMines( Level level ) {
		populate( level, 0.35f, 0.08f );
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

	/** a chance of one item, and a smaller chance of a second */
	public static void populate( Level level, float first, float second ) {
		int count = 0;
		if (Random.Float() < first) count++;
		if (Random.Float() < second) count++;
		for (int i = 0; i < count; i++) {
			int cell = level.randomDestination( null );
			if (cell != -1) level.drop( roll(), cell );
		}
	}

	/** floors rebuilt on the climb */
	public static void populate( Level level ) {
		populate( level, 0.60f, 0.15f );
	}
}
