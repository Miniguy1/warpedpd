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

/** The Old Prospector: lost his pickaxe somewhere on this floor. Bring it back for a reward. */
public class OldProspector extends NPC {

	{
		spriteClass = xyz.gabriwar.warpedpixeldungeon.sprites.OldProspectorSprite.class;
		properties.add( Property.IMMOVABLE );
	}

	private boolean told = false;
	private boolean done = false;

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

	public boolean told() { return told; }

	public boolean done() { return done; }

	/** the hero comes with the pickaxe: it is taken, and the reward is due (false if they do not have it) */
	public boolean receive( Hero hero ) {
		if (done) return false;
		xyz.gabriwar.warpedpixeldungeon.items.quest.LostPickaxe p = hero.belongings.getItem( xyz.gabriwar.warpedpixeldungeon.items.quest.LostPickaxe.class );
		if (p == null) return false;
		p.detachAll( hero.belongings.backpack );
		done = true;
		return true;
	}

	/** a weapon of the floor's tier, identified and a level better, and two bandage rolls */
	public ArrayList<Item> payout() {
		ArrayList<Item> out = new ArrayList<>();
		Item w = null;
    //a reward must never be what brings the game down: if the weapon cannot be made, gold stands in for it
    try {
      w = Generator.random( Dungeon.depth <= 5 ? Generator.Category.WEP_T3 : Generator.Category.WEP_T4 );
      w.cursed = false;
      w.identify();
      w.upgrade();
    } catch (RuntimeException e) {
      w = null;
    }
    out.add( w != null ? w : new xyz.gabriwar.warpedpixeldungeon.items.Gold( 40 + 12 * Math.max( 1, Dungeon.depth ) ) );
		out.add( new BandageRoll().quantity( 2 ) );
		return out;
	}

  /** the whole exchange: the pickaxe taken, the reward at the hero's feet, a line in the log and he is on his way (false when the hero has no pickaxe for him) */
  public boolean turnIn( Hero hero ) {
    if (!receive( hero )) return false;
    xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents.give( hero, payout() );
    xyz.gabriwar.warpedpixeldungeon.utils.GLog.p( Messages.get( this, "thanks" ) );
    leave();
    return true;
  }

  /** with the pickaxe in the pack, walking up to him is enough: there is nothing to tap */
  public boolean tryAutoTurnIn( Hero hero ) {
    if (done || hero == null || !hero.isAlive() || Dungeon.level == null) return false;
    if (Dungeon.level.distance( pos, hero.pos ) > 2) return false;
    return turnIn( hero );
  }

  @Override
  protected boolean act() {
    if (isAlive() && tryAutoTurnIn( Dungeon.hero )) {
      spend( TICK );
      return true;
    }
    return super.act();
  }

  @Override
  public boolean interact( Char c ) {
    if (c != Dungeon.hero) return true;
    Hero hero = (Hero) c;
    if (done) {
      say( Messages.get( this, "after" ) );
    } else if (!turnIn( hero )) {
      told = true;
      say( Messages.get( this, "asks" ) );
    }
    return true;
  }


	private static final String TOLD = "told", DONE = "done";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( TOLD, told ); bundle.put( DONE, done );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		told = bundle.getBoolean( TOLD ); done = bundle.getBoolean( DONE );
	}
}
