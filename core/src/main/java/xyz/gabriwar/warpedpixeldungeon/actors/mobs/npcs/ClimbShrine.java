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

import xyz.gabriwar.warpedpixeldungeon.Cycle;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bless;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Cripple;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Haste;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Vulnerable;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Weakness;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.items.CycleShard;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.sprites.CaveShrineSprite;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import xyz.gabriwar.warpedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

/**
 * The two shrines a climb floor may hold (levels/ClimbEvents), each good for one use:
 *  - the BLESSING shrine gives a boon, with a bane to go with it;
 *  - the CURSED shrine takes a price (blood, gold, or a fight) for a reward.
 */
public class ClimbShrine extends NPC {

	public static final int BLESSING = 0, CURSED = 1;

	{
		spriteClass = CaveShrineSprite.class;
		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	public int kind = BLESSING;
	public boolean used = false;

	@Override
	public String name() {
		return kind == BLESSING ? "blessing shrine" : "cursed shrine";
	}

	@Override
	public String description() {
		return kind == BLESSING
				? "A small idol on a plinth of pale stone. It will give you something, for one use only, and it will want something back."
				: "A black idol, cold to look at. It offers a great deal for a price, once.";
	}

	@Override
	public boolean reset() { return true; }

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) return true;
		final Hero hero = (Hero)c;
		if (used){
			GLog.i( "The shrine is spent." );
			return true;
		}
		final String[] opts = kind == BLESSING
				? new String[]{ "Swiftness (and weakness to blows)", "Fortitude (and a weakened arm)", "Sight (and a slowed step)", "Leave" }
				: new String[]{ "Offer blood (lose 30% of your health)", "Offer gold (lose half of it)", "Wake the guardians (a fight)", "Leave" };
		Game.runOnRenderThread( () -> GameScene.show( new WndOptions( sprite(), name(), description(), opts ){
			@Override
			protected void onSelect( int index ){
				if (index < 0 || index > 2) return;
				boolean ok = kind == BLESSING ? bless( hero, index ) : curse( hero, index, Dungeon.level );
				if (ok){
					used = true;
					hero.spendAndNext( 1f );
				}
			}
		} ) );
		return true;
	}

	/** a boon, and a bane: 0 swiftness and vulnerability, 1 a barrier and weakness, 2 a blessing and a crippled step */
	public static boolean bless( Hero hero, int choice ){
		switch (choice){
			case 0:
				Buff.prolong( hero, Haste.class, 100f );
				Buff.prolong( hero, Vulnerable.class, 100f );
				GLog.p( "You move like the wind, and every blow finds you." );
				return true;
			case 1:
				Buff.affect( hero, Barrier.class ).incShield( Math.max( 1, Math.round( hero.HT * 0.4f ) ) );
				Buff.prolong( hero, Weakness.class, 200f );
				GLog.p( "A shell of light closes round you, and your arm grows heavy." );
				return true;
			case 2:
				Buff.prolong( hero, Bless.class, 300f );
				Buff.prolong( hero, Cripple.class, 100f );
				GLog.p( "Your aim is blessed, and your feet will not be hurried." );
				return true;
			default:
				return false;
		}
	}

	/** a price, and a reward dropped at the hero's feet: 0 blood, 1 gold, 2 a fight */
	public static boolean curse( Hero hero, int choice, xyz.gabriwar.warpedpixeldungeon.levels.Level level ){
		float rating = Cycle.lootRating( Cycle.number, Dungeon.depth );
		switch (choice){
			case 0: {
				int cost = Math.min( hero.HP - 1, Math.round( hero.HP * 0.3f ) );
				if (cost < 1){
					GLog.w( "You have no blood to spare." );
					return false;
				}
				hero.HP -= cost;
				for (int i = 0; i < 2; i++) level.drop( EndgameLoot.rollRated( rating + 1.5f ), hero.pos );
				GLog.n( "The idol drinks, and gives." );
				return true;
			}
			case 1: {
				if (Dungeon.gold < 100){
					GLog.w( "You have too little gold for the idol." );
					return false;
				}
				Dungeon.gold -= Dungeon.gold / 2;
				level.drop( new CycleShard().quantity( 3 + Cycle.number ), hero.pos );
				level.drop( EndgameLoot.rollRated( rating + 1.5f ), hero.pos );
				GLog.n( "The gold is gone, and the idol is pleased." );
				return true;
			}
			case 2: {
				BossMutation.summon( hero, 6 );
				for (int i = 0; i < 2; i++) level.drop( EndgameLoot.rollRated( rating + 2f ), hero.pos );
				level.drop( new CycleShard().quantity( 2 + Cycle.number ), hero.pos );
				GLog.n( "The guardians wake, and the idol has paid in advance." );
				return true;
			}
			default:
				return false;
		}
	}

	private static final String KIND = "kind", USED = "used";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( KIND, kind );
		bundle.put( USED, used );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		kind = bundle.getInt( KIND );
		used = bundle.getBoolean( USED );
	}
}
