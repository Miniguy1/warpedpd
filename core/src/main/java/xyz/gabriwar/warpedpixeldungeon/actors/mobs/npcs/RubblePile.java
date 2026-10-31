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

import java.util.ArrayList;

import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.Generator;
import xyz.gabriwar.warpedpixeldungeon.items.Heap;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.items.field.BandageRoll;
import xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import xyz.gabriwar.warpedpixeldungeon.windows.WndQuest;

/** A cave-in's rubble: it takes five turns of digging to clear, and a stash and a nest of rats lie behind it. */
public class RubblePile extends NPC {

	{
		spriteClass = xyz.gabriwar.warpedpixeldungeon.sprites.RubbleSprite.class;
		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	public static final int TURNS = 5;

	private int dug = 0;

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	protected void say( String text ) {
		if (com.watabou.noosa.Game.scene() instanceof GameScene) GameScene.show( new WndQuest( this, text ) );
	}

	protected void leave() {
		destroy();
		if (sprite != null) sprite.killAndErase();
		if (Dungeon.level != null) Dungeon.level.mobs.remove( this );
	}

	public int dug() { return dug; }

	/** one turn of digging; true on the turn the last stone comes away */
	public boolean dig() {
		dug++;
		return dug >= TURNS;
	}

	/** the rubble is gone: a stash on its cell, and the rats that nested behind it */
	public void uncover() {
		Level level = Dungeon.level;
		int cell = pos;
		leave();
		if (level == null) return;
		Heap heap = null;
		for (int i = 0; i < 2; i++) heap = level.drop( Generator.random(), cell );
		heap = level.drop( new xyz.gabriwar.warpedpixeldungeon.items.Gold( 20 + 8 * Math.max( 1, Dungeon.depth ) ), cell );
		heap = level.drop( new BandageRoll(), cell );
		EarlyEvents.spawnAround( level, cell, xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat.class, 2 );
		EarlyEvents.spawnAround( level, cell, xyz.gabriwar.warpedpixeldungeon.actors.mobs.PowderRat.class, 1 );
		if (com.watabou.noosa.Game.scene() instanceof GameScene) GLog.w( Messages.get( this, "rats" ) );
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) return true;
		boolean done = dig();
		((Hero) c).spend( 1f );
		((Hero) c).busy();
		if (done) uncover();
		else GLog.i( Messages.get( this, "digging", dug, TURNS ) );
		return true;
	}

	private static final String DUG = "dug";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DUG, dug );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		dug = bundle.getInt( DUG );
	}
}
