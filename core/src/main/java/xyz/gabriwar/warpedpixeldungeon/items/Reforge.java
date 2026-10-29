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

import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.armor.Armor;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.Weapon;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

/**
 * What a Cycle Shard can do to a tier 7, 8 or 9 set piece (or a Yog-Forged one), at a price in shards:
 *  - REROLL PIECE: it becomes another piece of its own set and tier, at the same level.
 *  - REROLL AFFIX: a weapon gets a new enchantment, an armor a new glyph.
 *  - TEMPER: one or two levels more, up to +15.
 *  - AWAKEN: it becomes the same piece of the next tier up (7 to 8, 8 to 9), if the cycle allows that tier.
 */
public final class Reforge {

	private Reforge(){}

	public static final int TEMPER_CAP = 15;

	public static boolean eligible( Item it ){
		return it != null && (it instanceof SetTier || GearSets.setOf( it.getClass() ) == GearSets.YOG);
	}

	/** a Yog-Forged piece reforges as a tier 9 */
	public static int tierOf( Item it ){
		return it instanceof SetTier ? ((SetTier)it).setTier() : 9;
	}

	public static int costPiece( int tier ){ return tier <= 7 ? 3 : tier == 8 ? 5 : 8; }
	public static int costAffix( int tier ){ return tier <= 7 ? 2 : tier == 8 ? 3 : 5; }
	public static int costTemper( int tier ){ return tier <= 7 ? 2 : tier == 8 ? 3 : 4; }
	public static int costAwaken( int tier ){ return tier <= 7 ? 10 : 16; }

	public static boolean canAwaken( Item it ){
		if (!(it instanceof SetTier)) return false;
		int t = ((SetTier)it).setTier();
		return t < 9 && t + 1 <= EndgameLoot.maxTier();
	}

	public static boolean canAffix( Item it ){
		return (it instanceof Weapon && !(it instanceof MagesStaff)) || it instanceof Armor;
	}

	public static boolean canTemper( Item it ){
		return it.isUpgradable() && it.level() < TEMPER_CAP;
	}

	private static Item make( Class<?> c, Item like ){
		Item n = (Item)Reflection.newInstance( c );
		if (n == null) return null;
		n.level( like.level() );
		n.cursed = false;
		n.identify();
		return n;
	}

	/** another piece of the same set (never the same one) */
	public static Item rerollPiece( Item old ){
		int set = GearSets.setOf( old.getClass() );
		Class<?>[] pieces = GearSets.piecesOf( set );
		Class<?> pick;
		do {
			pick = pieces[Random.Int( pieces.length )];
		} while (pick == old.getClass());
		return make( pick, old );
	}

	/** the same piece, a tier up */
	public static Item awaken( Item old ){
		int set = GearSets.setOf( old.getClass() );
		Class<?>[] pieces = GearSets.piecesOf( set );
		int i = 0;
		while (pieces[i] != old.getClass()) i++;
		Item n = make( GearSets.piecesOf( set + 6 )[i], old );
		if (n instanceof Weapon && old instanceof Weapon) ((Weapon)n).enchantment = ((Weapon)old).enchantment;
		if (n instanceof Armor && old instanceof Armor) ((Armor)n).glyph = ((Armor)old).glyph;
		return n;
	}

	public static boolean rerollAffix( Item it ){
		if (it instanceof Weapon && !(it instanceof MagesStaff)){
			((Weapon)it).enchant( Weapon.Enchantment.random() );
			return true;
		}
		if (it instanceof Armor){
			((Armor)it).inscribe( Armor.Glyph.random() );
			return true;
		}
		return false;
	}

	public static boolean temper( Item it ){
		if (!canTemper( it )) return false;
		int n = Math.min( Random.IntRange( 1, 2 ), TEMPER_CAP - it.level() );
		for (int i = 0; i < n; i++) it.upgrade();
		return true;
	}

	/** puts the new piece where the old one was, equipped if the old one was */
	public static void swap( Hero hero, Item old, Item neu ){
		boolean worn = old.isEquipped( hero );
		if (worn && old instanceof EquipableItem) ((EquipableItem)old).doUnequip( hero, false, false );
		old.detach( hero.belongings.backpack );
		if (!neu.collect( hero.belongings.backpack )){
			hero.belongings.backpack.items.add( neu );
		}
		if (worn && neu instanceof EquipableItem) ((EquipableItem)neu).doEquip( hero );
	}
}
