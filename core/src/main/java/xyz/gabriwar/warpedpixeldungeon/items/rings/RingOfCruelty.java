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

package xyz.gabriwar.warpedpixeldungeon.items.rings;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** Every blow you land hits harder by 4% per ring level. */
public class RingOfCruelty extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_TENACITY;
		buffClass = Cruelty.class;
		anonymous = true;
		image = ItemSpriteSheet.RING_RUBY;
	}

	@Override
	public void reset() {
		super.reset();
		image = ItemSpriteSheet.RING_RUBY;
	}

	@Override
	public String statsInfo() {
		if (isIdentified()) {
			String info = Messages.get( this, "stats", Messages.decimalFormat( "#.##", 4f * soloBuffedBonus() ) );
			if (isEquipped( Dungeon.hero ) && soloBuffedBonus() != combinedBuffedBonus( Dungeon.hero )) {
				info += "\n\n" + Messages.get( this, "combined_stats", Messages.decimalFormat( "#.##", 4f * combinedBuffedBonus( Dungeon.hero ) ) );
			}
			return info;
		} else {
			return Messages.get( this, "typical_stats", Messages.decimalFormat( "#.##", 4f ) );
		}
	}

	@Override
	public String upgradeStat1( int level ) {
		if (cursed && cursedKnown) level = Math.min( -1, level - 3 );
		return Messages.decimalFormat( "#.##", 4f * (level + 1) ) + "%";
	}

	@Override
	protected RingBuff buff() {
		return new Cruelty();
	}

	public class Cruelty extends RingBuff {
	}
}
