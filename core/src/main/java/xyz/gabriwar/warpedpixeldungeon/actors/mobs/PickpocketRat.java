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

package xyz.gabriwar.warpedpixeldungeon.actors.mobs;

import com.watabou.utils.Random;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.Gold;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.PickpocketRatSprite;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/** A rat with quick paws: it takes some of your gold and bolts. Kill it to get the gold back. */
public class PickpocketRat extends Thief {

	{
		spriteClass = PickpocketRatSprite.class;
		HP = HT = 10 + Dungeon.depth * 2;
		defenseSkill = 5;
		EXP = 2;
		maxLvl = 6;
		loot = null;
		lootChance = 0f;
		properties.remove( Property.UNDEAD );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 2 + statDepth() / 2 );
	}

	@Override
	public int attackSkill( xyz.gabriwar.warpedpixeldungeon.actors.Char target ) {
		return 8 + statDepth();
	}

	//gold, never gear: a few coins' worth for the floor it is on
	@Override
	protected boolean steal( Hero hero ) {
		if (Dungeon.gold <= 0) return false;
		int take = Math.min( Dungeon.gold, 15 + 6 * Math.max( 1, Dungeon.depth ) );
		Dungeon.gold -= take;
		item = new Gold( take );
		GLog.w( Messages.get( this, "stole", take ) );
		return true;
	}
}
