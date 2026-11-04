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

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Cycle;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.CycleShard;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Heap;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.items.legendary.Legendary;
import xyz.gabriwar.warpedpixeldungeon.levels.EarlyEvents;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/**
 * The white bag: the rarest drop. A glowing white sack that opens when the hero walks onto it, and puts what it holds
 * straight into the pack (beside the hero if the pack is full). It never stays in the inventory.
 *
 * From a boss: a small chance, and the best: a piece of the hero's class set of a legendary at +3, and shards. From an elite: a much lower chance, and less: a piece a tier lower, and a few shards. From any other
 * enemy: an incredibly low chance, and one or two items of the floor's ordinary quality.
 */
public class WhiteBag extends Item {

	public static final int NORMAL = 0, ELITE = 1, BOSS = 2;

	public static final float BOSS_CHANCE = 0.07f, ELITE_CHANCE = 0.007f, NORMAL_CHANCE = 0.0004f;

	{
		image = ItemSpriteSheet.WHITE_BAG;
		stackable = false;
	}

	private int source = BOSS;
	private ArrayList<Item> contents = new ArrayList<>();

	public WhiteBag() {
	}

	public WhiteBag( int source, ArrayList<Item> contents ) {
		this.source = source;
		this.contents = contents;
	}

	public int source() {
		return source;
	}

	public ArrayList<Item> contents() {
		return contents;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0xFFFFFF, 1.2f );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public String desc() {
		String from = source == BOSS ? "from_boss" : source == ELITE ? "from_elite" : "from_normal";
		return super.desc() + "\n\n" + Messages.get( this, from );
	}

	/** walking onto it opens it: the contents are the hero's and the bag is gone */
	@Override
	public boolean doPickUp( Hero hero, int pos ) {
		open( hero );
		hero.spendAndNext( TIME_TO_PICK_UP );
		return true;
	}

	/** hands the contents over: into the pack, or beside the hero when it is full. Returns what was given. */
	public ArrayList<Item> open( Hero hero ) {
		if (contents.isEmpty()) contents = contentsFor( source );
		ArrayList<Item> given = new ArrayList<>( contents );
		contents = new ArrayList<>();
		HashSet<Integer> taken = new HashSet<>();
		taken.add( hero.pos );
		for (Item it : given) {
			String what = it.quantity() > 1 ? it.name() + " x" + it.quantity() : it.name();
			if (it.collect( hero.belongings.backpack )) {
				GLog.p( "The white bag holds: " + what + "." );
			} else {
				int at = Dungeon.level == null ? -1 : EarlyEvents.neighbour( Dungeon.level, hero.pos, taken );
				if (at != -1) {
					taken.add( at );
					Dungeon.level.drop( it, at );
				} else {
					hero.belongings.backpack.items.add( it );
				}
				GLog.p( "The white bag holds: " + what + " (your pack is full)." );
			}
		}
		return given;
	}

	/** what kind of white bag, if any, a creature can drop: -1 for summons, decoys and anything that is not an enemy */
	public static int sourceOf( Mob mob ) {
		if (mob == null || mob.alignment != Char.Alignment.ENEMY) return -1;
		if (Char.hasProp( mob, Char.Property.BOSS ) && !Char.hasProp( mob, Char.Property.MINIBOSS )) return BOSS;
		if (mob.buff( Elite.class ) != null || Char.hasProp( mob, Char.Property.MINIBOSS )) return ELITE;
		if (mob.EXP <= 0) return -1;
		return NORMAL;
	}

	public static float chanceFor( int source ) {
		return source == BOSS ? BOSS_CHANCE : source == ELITE ? ELITE_CHANCE : source == NORMAL ? NORMAL_CHANCE : 0f;
	}

	public static boolean rolls( int source ) {
		return Random.Float() < chanceFor( source );
	}

	/** what a bag from this source holds */
	public static ArrayList<Item> contentsFor( int source ) {
		ArrayList<Item> out = new ArrayList<>();
		Hero h = Dungeon.hero;
		int cap = EndgameLoot.maxTier();
		boolean haveClass = h != null && h.heroClass != null;
		//a white bag holds a legendary (never a tiered piece: those drop the ordinary way); with every legendary owned, shards stand in
		if (source == BOSS) {
			Item l = Legendary.roll( 3 );
			out.add( l != null ? l : new CycleShard().quantity( 8 + 2 * Cycle.number ) );
			out.add( new CycleShard().quantity( 3 + Cycle.number ) );
		} else if (source == ELITE) {
			Item l = Legendary.roll( 0 );
			out.add( l != null ? l : new CycleShard().quantity( 4 + Cycle.number ) );
			out.add( new CycleShard().quantity( 1 + Cycle.number / 2 ) );
		} else {
			//the smallest bag: half the time a legendary, half the time one or two scrolls of upgrade
			Item l = Random.Int( 2 ) == 0 ? Legendary.roll( 0 ) : null;
			out.add( l != null ? l : new xyz.gabriwar.warpedpixeldungeon.items.scrolls.ScrollOfUpgrade().quantity( 1 + Random.Int( 2 ) ) );
		}
		for (Item it : out) it.identify();
		return out;
	}

	/** a creature dies: it may leave a white bag */
	public static void onKill( Mob mob ) {
		Hero h = Dungeon.hero;
		if (h == null || !h.isAlive() || Dungeon.level == null) return;
		int src = sourceOf( mob );
		if (src < 0 || !rolls( src )) return;
		drop( mob.pos, src );
	}

	public static Heap drop( int pos, int source ) {
		Heap heap = Dungeon.level.drop( new WhiteBag( source, contentsFor( source ) ), pos );
		GLog.p( source == BOSS ? "A WHITE BAG falls from the boss!" : source == ELITE ? "A WHITE BAG falls from the elite!" : "A WHITE BAG! It fell from something small." );
		if (com.watabou.noosa.Game.scene() instanceof GameScene) {
			try {
				com.watabou.noosa.audio.Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				CellEmitter.center( pos ).burst( Speck.factory( Speck.LIGHT ), 24 );
			} catch (Throwable ignored) {
				//a sound or a spark failing is no reason to lose the bag
			}
		}
		return heap;
	}

	private static final String SOURCE = "source", CONTENTS = "contents";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SOURCE, source );
		bundle.put( CONTENTS, contents );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		source = bundle.getInt( SOURCE );
		contents = new ArrayList<>();
		for (Bundlable b : bundle.getCollection( CONTENTS )) if (b instanceof Item) contents.add( (Item) b );
	}
}
