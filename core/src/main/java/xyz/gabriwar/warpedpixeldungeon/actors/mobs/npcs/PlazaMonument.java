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

package xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.levels.overworld.OverworldLevel;
import xyz.gabriwar.warpedpixeldungeon.sprites.MonumentSprite;

/**
 * The Monument of the Descent: a glowing standing stone on the town's plaza, two steps from where you arrive.
 * Touch it and it opens the same menu as the plaza staircase: the dark mines and the plaza zones.
 * (OverworldLevel puts it on its cell, WorldStructures.TOWN_MONUMENT, and keeps it there.)
 */
public class PlazaMonument extends NPC {

	{
		spriteClass = MonumentSprite.class;
		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	@Override
	public String name() {
		return "monument of the descent";
	}

	@Override
	public String description() {
		return "A tall standing stone that glows with a pale blue light, carved with four doorways. It will open the way down to the dark mines, "
				+ "and to the three places beneath the plaza: the Old Cellars, the Ashen Crypt and the Frostbound Vault.";
	}

	@Override
	public boolean reset() {
		return true;
	}

	//nothing in the world harms it
	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) return true;
		if (Dungeon.level instanceof OverworldLevel){
			((OverworldLevel)Dungeon.level).openPlazaMenu( (Hero)c );
		}
		return true;
	}
}
