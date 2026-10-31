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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.bags.Bag;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Carried, not worn: it wraps you in a thin shield that rebuilds itself every so often, up to a
 *  tenth of your health. */
public class AmuletOfAegis extends Item {

	private static final ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x55AAFF );

	{
		image = ItemSpriteSheet.AMULET;
		stackable = false;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )) {
			if (container.owner instanceof Hero) {
				Buff.affect( container.owner, Aegis.class );
			}
			return true;
		}
		return false;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (Dungeon.hero != null && Dungeon.hero.belongings.getItem( AmuletOfAegis.class ) == null) {
			Buff.detach( Dungeon.hero, Aegis.class );
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLUE;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public static class Aegis extends Buff {

		{
			actPriority = HERO_PRIO - 1;
		}

		@Override
		public boolean act() {
			int cap = Math.max( 5, target.HT / 10 );
			int have = target.shielding();
			if (target.isAlive() && have < cap) {
				Buff.affect( target, Barrier.class ).incShield( Math.min( cap - have, 2 + target.HT / 50 ) );
			}
			spend( 15f );
			return true;
		}
	}

	/** a set piece is worth gold like any other gear (it never was, so no shop would take it): 30 at tier 6, up to 60 at tier 9 */
	@Override
	public int value() {
		int t = xyz.gabriwar.warpedpixeldungeon.items.GearSets.gearTier( this );
		return t > 0 ? 20 + 10 * (Math.max( 5, t ) - 5) : 0;
	}
}
