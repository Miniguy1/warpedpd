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

import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.bags.Bag;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Carried, not worn: for as long as it is in your pack, it knits you back together, one point of
 *  health every few turns. */
public class AmuletOfRenewal extends Item {

	private static final ItemSprite.Glowing GREEN = new ItemSprite.Glowing( 0x44DD66 );

	{
		image = ItemSpriteSheet.AMULET;
		stackable = false;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )) {
			if (container.owner instanceof Hero) {
				Buff.affect( container.owner, Renewal.class );
			}
			return true;
		}
		return false;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (Dungeon.hero != null && Dungeon.hero.belongings.getItem( AmuletOfRenewal.class ) == null) {
			Buff.detach( Dungeon.hero, Renewal.class );
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREEN;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public static class Renewal extends Buff {

		{
			actPriority = HERO_PRIO - 1;
		}

		@Override
		public boolean act() {
			if (target.isAlive() && target.HP < target.HT) {
				target.heal( 1 );
			}
			spend( 6f );
			return true;
		}
	}
}
