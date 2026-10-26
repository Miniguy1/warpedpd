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
import xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire;
import xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfShattering;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfRetribution;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfVampirism;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.BloodmoonAxe;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dawnbreaker;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Stormbreaker;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Voidreaver;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import com.watabou.utils.Random;

/** The end-game drops: only the floors rebuilt on the climb out of the dungeon (carrying the cursed
 *  amulet) hold them. Each rebuilt floor rolls a chance for one, a smaller chance for a second. */
public class EndgameLoot {

	/** one random piece of end-game gear, already identified, at +1 to +3 (rings +0 to +2) */
	public static Item roll() {
		Item item;
		switch (Random.Int( 11 )) {
			case 0:  item = new RobeOfFire();       break;
			case 1:  item = new BleedingArmor();    break;
			case 2:  item = new RobeOfShattering(); break;
			case 3:  item = new Dawnbreaker();      break;
			case 4:  item = new Voidreaver();       break;
			case 5:  item = new BloodmoonAxe();     break;
			case 6:  item = new Stormbreaker();     break;
			case 7:  item = new RingOfVampirism();  break;
			case 8:  item = new RingOfRetribution();break;
			case 9:  item = new AmuletOfRenewal();  break;
			default: item = new AmuletOfAegis();    break;
		}
		if (item.isUpgradable()) {
			boolean ring = item instanceof xyz.gabriwar.warpedpixeldungeon.items.rings.Ring;
			item.level( ring ? Random.IntRange( 0, 2 ) : Random.IntRange( 1, 3 ) );
		}
		item.identify();
		return item;
	}

	public static void populate( Level level ) {
		int count = 0;
		if (Random.Float() < 0.60f) count++;
		if (Random.Float() < 0.15f) count++;
		for (int i = 0; i < count; i++) {
			int cell = level.randomDestination( null );
			if (cell != -1) level.drop( roll(), cell );
		}
	}
}
