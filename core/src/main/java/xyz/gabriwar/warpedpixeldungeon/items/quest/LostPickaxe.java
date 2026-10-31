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

package xyz.gabriwar.warpedpixeldungeon.items.quest;

import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;

/** the Old Prospector's pickaxe, lost somewhere on his floor */
public class LostPickaxe extends Item {

	public static final String AC_GIVE = "GIVE";

	@Override
	public java.util.ArrayList<String> actions( xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero hero ) {
		java.util.ArrayList<String> actions = super.actions( hero );
		actions.add( AC_GIVE );
		return actions;
	}

	/** hand it to the Old Prospector if he is near and in view */
	@Override
	public void execute( xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero hero, String action ) {
		super.execute( hero, action );
		if (!AC_GIVE.equals( action )) return;
		xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.OldProspector near = null;
		xyz.gabriwar.warpedpixeldungeon.levels.Level level = xyz.gabriwar.warpedpixeldungeon.Dungeon.level;
		if (level != null) {
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs.toArray( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob[0] )) {
				if (m instanceof xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.OldProspector && m.isAlive()
						&& level.heroFOV[m.pos] && level.distance( m.pos, hero.pos ) <= 6) near = (xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.OldProspector) m;
			}
		}
		if (near == null) {
			xyz.gabriwar.warpedpixeldungeon.utils.GLog.w( xyz.gabriwar.warpedpixeldungeon.messages.Messages.get( this, "nobody" ) );
			return;
		}
		near.turnIn( hero );
	}


	{
		image = ItemSpriteSheet.PICKAXE;
		unique = true;
		defaultAction = AC_GIVE;
		stackable = false;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
