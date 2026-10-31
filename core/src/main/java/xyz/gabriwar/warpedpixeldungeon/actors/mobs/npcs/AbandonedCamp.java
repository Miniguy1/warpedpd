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

/** An abandoned camp: rest by its fire once for a full recovery, but a patrol turns up if you linger. */
public class AbandonedCamp extends NPC {

	{
		spriteClass = xyz.gabriwar.warpedpixeldungeon.sprites.BedrollSprite.class;
		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	public static final int PATROL_AFTER = 20;

	private boolean used = false;
	private boolean patrolSent = false;
	private int timer = 0;

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	protected void say( String text ) {
		//the words are always in the log, whatever the window does
    xyz.gabriwar.warpedpixeldungeon.utils.GLog.i( text );
    try {
      if (com.watabou.noosa.Game.scene() instanceof GameScene) GameScene.show( new WndQuest( this, text ) );
    } catch (Throwable t) {
      xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents.report( this, t );
    }
	}

	protected void leave() {
		destroy();
		if (sprite != null) sprite.killAndErase();
		if (Dungeon.level != null) Dungeon.level.mobs.remove( this );
	}

	public boolean used() { return used; }

	public boolean patrolSent() { return patrolSent; }

	/** rest: the first time, a full heal (and an end to the bleeding); never again */
	public boolean rest( Hero hero ) {
		if (used) return false;
		used = true;
		hero.HP = hero.HT;
		hero.MP = hero.MT;
		xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff.detach( hero, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bleeding.class );
		return true;
	}

	/** one turn of the camp's clock: twenty turns after a rest, a patrol arrives */
	public void step() {
		if (!used || patrolSent) return;
		if (++timer >= PATROL_AFTER) {
			patrolSent = true;
			if (com.watabou.noosa.Game.scene() instanceof GameScene && Dungeon.hero != null) {
				GLog.n( Messages.get( this, "patrol" ) );
				BossMutation.summon( Dungeon.hero, 2 );
			}
		}
	}

	@Override
  protected boolean act() {
    try {
      return actBody();
    } catch (Throwable t) {
      xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents.report( this, t );
      spend( TICK );
      return true;
    }
  }

  private boolean actBody() {
		step();
		spend( TICK );
		return true;
	}

	@Override
  public boolean interact( Char c ) {
    try {
      return interactBody( c );
    } catch (Throwable t) {
      xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents.report( this, t );
      return true;
    }
  }

  private boolean interactBody( Char c ) {
		if (c != Dungeon.hero) return true;
		if (rest( (Hero) c )) {
			((Hero) c).spend( 10f );
			((Hero) c).busy();
			GLog.p( Messages.get( this, "rested" ) );
		} else {
			say( Messages.get( this, "cold" ) );
		}
		return true;
	}

	private static final String USED = "used", SENT = "sent", TIMER = "timer";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USED, used ); bundle.put( SENT, patrolSent ); bundle.put( TIMER, timer );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		used = bundle.getBoolean( USED ); patrolSent = bundle.getBoolean( SENT ); timer = bundle.getInt( TIMER );
	}
}
