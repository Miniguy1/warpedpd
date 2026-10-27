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

	/** one random new item, already identified. Armor and weapons come at +1 to +3, rings and wands
	 *  at their own random levels, the rest as they are. Never cursed. */
	public static Item roll() {
		Item item;
		boolean leveled = false;
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
			case 6:  item = new RobeOfFire();       leveled = true; break;
			case 7:  item = new BleedingArmor();    leveled = true; break;
			case 8:  item = new RobeOfShattering(); leveled = true; break;
			case 9:  item = new DragonscaleMail();  leveled = true; break;
			//weapons
			case 10: item = new Dawnbreaker();      leveled = true; break;
			case 11: item = new Voidreaver();       leveled = true; break;
			case 12: item = new BloodmoonAxe();     leveled = true; break;
			case 13: item = new Stormbreaker();     leveled = true; break;
			case 14: item = new ObsidianGreataxe(); leveled = true; break;
			case 15: item = new WyrmfangGlaive();   leveled = true; break;
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
			case 24: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Frostbrand();   leveled = true; break;
			case 25: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Thunderlance(); leveled = true; break;
			case 26: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Gravesplitter(); leveled = true; break;
			case 27: item = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Emberfang();     leveled = true; break;
			case 28: item = new xyz.gabriwar.warpedpixeldungeon.items.armor.Thornmail();  leveled = true; break;
			case 29: item = new xyz.gabriwar.warpedpixeldungeon.items.armor.Duskplate();  leveled = true; break;
			case 30: item = new xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfBulwark();
					item.level( Random.IntRange( 0, 2 ) ); break;
			case 31: item = new xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfCruelty();
					item.level( Random.IntRange( 0, 2 ) ); break;
			case 32: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.HuntersFang();   break;
			case 33: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.WayfarersBoots(); break;
			default: item = new xyz.gabriwar.warpedpixeldungeon.items.trinkets.EmberCharm();     break; //34
		}
		if (leveled) {
			item.level( Random.IntRange( 1, 3 ) );
		}
		item.cursed = false;
		item.identify();
		return item;
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
