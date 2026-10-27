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

package xyz.gabriwar.warpedpixeldungeon.items.trinkets;

import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** A grey pendant that is far heavier than it looks. Every blow you take is softened: -5% damage taken, -5% more per level. */
public class StoneheartPendant extends Trinket {

	{
		image = ItemSpriteSheet.SALT_CUBE;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", Messages.decimalFormat("#.##", 100 * Math.abs(damageTakenMultiplier(buffedLvl())-1f)));
		} else {
			return Messages.get(this, "typical_stats_desc", Messages.decimalFormat("#.##", 100 * Math.abs(damageTakenMultiplier(0)-1f)));
		}
	}

	public static float damageTakenMultiplier(){
		return damageTakenMultiplier(trinketLevel(StoneheartPendant.class));
	}

	public static float damageTakenMultiplier(int level){
		if (level <= -1){
			return 1;
		} else {
			return 1 - 0.05f*(level+1);
		}
	}
}
