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

package xyz.gabriwar.warpedpixeldungeon.items.field;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.*;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/** a smoke flare: thrown, it blinds everything within two tiles of where it lands */
public class SmokeFlare extends FieldItem {

	{
		image = ItemSpriteSheet.SMOKE_FLARE;
		defaultAction = AC_THROW;
		stackable = true;
	}

	/** the flash and the smoke: enemies within two tiles are blind for six turns */
	public void burst( int cell ) {
		if (Dungeon.level == null) return;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( cell, m.pos ) <= 2) {
				Buff.prolong( m, Blindness.class, 6f );
			}
		}
		if (com.watabou.noosa.Game.scene() instanceof GameScene) CellEmitter.center( cell ).burst( Speck.factory( Speck.DUST ), 16 );
	}

	@Override
	protected void onThrow( int cell ) {
		burst( cell );
	}

	@Override
	public int value() {
		return 12 * quantity;
	}
}
