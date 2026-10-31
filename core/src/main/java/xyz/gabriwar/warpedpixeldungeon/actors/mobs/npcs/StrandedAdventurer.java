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

/** A Stranded Adventurer, cornered by enemies: clear them and he thanks you with a reward. */
public class StrandedAdventurer extends NPC {

	{
		spriteClass = xyz.gabriwar.warpedpixeldungeon.sprites.StrandedAdventurerSprite.class;
		properties.add( Property.IMMOVABLE );
	}

	private boolean rewarded = false;

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

	public boolean rewarded() { return rewarded; }

	/** are there enemies in view and near? Then he will not be thanked yet */
	public boolean threatened( Hero hero ) {
		Level level = Dungeon.level;
		if (level == null) return false;
		for (Mob m : level.mobs.toArray( new Mob[0] )) {
			if (m != this && m.alignment == Char.Alignment.ENEMY && m.isAlive() && level.heroFOV[m.pos]
					&& level.distance( hero.pos, m.pos ) <= 8) return true;
		}
		return false;
	}

  /** a random find; if one cannot be made, gold stands in for it */
  private Item safeFind() {
    try {
      Item it = Generator.random();
      if (it != null) return it;
    } catch (RuntimeException e) {
      //fall through to the gold
    }
    return new xyz.gabriwar.warpedpixeldungeon.items.Gold( 30 + 10 * Math.max( 1, Dungeon.depth ) );
  }

	/** the reward: two random finds, two smoke flares and a bandage */
	public ArrayList<Item> payout() {
		ArrayList<Item> out = new ArrayList<>();
		out.add( safeFind() );
		out.add( safeFind() );
		out.add( new xyz.gabriwar.warpedpixeldungeon.items.field.SmokeFlare().quantity( 2 ) );
		out.add( new BandageRoll() );
		return out;
	}

  /** thank him once the floor is clear: the reward at the hero's feet, a line in the log, and he is on his way */
  public boolean thank( Hero hero ) {
    if (rewarded || hero == null || threatened( hero )) return false;
    rewarded = true;
    xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents.give( hero, payout() );
    xyz.gabriwar.warpedpixeldungeon.utils.GLog.p( Messages.get( this, "thanks" ) );
    leave();
    return true;
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
    Hero h = Dungeon.hero;
    if (isAlive() && !rewarded && h != null && h.isAlive() && Dungeon.level != null
        && Dungeon.level.distance( pos, h.pos ) <= 2 && thank( h )) {
      spend( TICK );
      return true;
    }
    return super.act();
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
    Hero hero = (Hero) c;
    if (rewarded) return true;
    if (threatened( hero )) {
      say( Messages.get( this, "danger" ) );
      return true;
    }
    thank( hero );
    return true;
  }


	private static final String REWARDED = "rewarded";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( REWARDED, rewarded );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		rewarded = bundle.getBoolean( REWARDED );
	}
}
