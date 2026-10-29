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

package xyz.gabriwar.warpedpixeldungeon.items;

import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSprite;
import xyz.gabriwar.warpedpixeldungeon.sprites.ItemSpriteSheet;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import xyz.gabriwar.warpedpixeldungeon.windows.WndBag;
import xyz.gabriwar.warpedpixeldungeon.windows.WndOptions;

import java.util.ArrayList;

/** What the new cycles' bosses, bounties and caches leave behind. Spend them to reforge a tier 7+ set piece
 *  (see Reforge). */
public class CycleShard extends Item {

	public static final String AC_REFORGE = "REFORGE";

	{
		image = ItemSpriteSheet.OBLIVION_SHARD;
		stackable = true;
		defaultAction = AC_REFORGE;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_REFORGE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_REFORGE )){
			GameScene.selectItem( selector );
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0xA060FF );
	}

	@Override
	public boolean isUpgradable() { return false; }

	@Override
	public boolean isIdentified() { return true; }

	private final WndBag.ItemSelector selector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() { return "Reforge which piece?"; }

		@Override
		public boolean itemSelectable( Item item ) { return Reforge.eligible( item ); }

		@Override
		public void onSelect( Item item ) {
			if (item != null && Reforge.eligible( item )) menu( item );
		}
	};

	private void pay( Hero hero, int cost ) {
		quantity -= cost;
		if (quantity <= 0) detachAll( hero.belongings.backpack );
		else updateQuickslot();
	}

	private void menu( final Item item ) {
		final Hero hero = xyz.gabriwar.warpedpixeldungeon.Dungeon.hero;
		final int tier = Reforge.tierOf( item );
		final ArrayList<String> labels = new ArrayList<>();
		final ArrayList<Integer> kinds = new ArrayList<>();
		labels.add( "Reroll piece (" + Reforge.costPiece( tier ) + ")" ); kinds.add( 0 );
		if (Reforge.canAffix( item )) { labels.add( "Reroll affix (" + Reforge.costAffix( tier ) + ")" ); kinds.add( 1 ); }
		if (Reforge.canTemper( item )) { labels.add( "Temper +1 or 2 (" + Reforge.costTemper( tier ) + ")" ); kinds.add( 2 ); }
		if (Reforge.canAwaken( item )) { labels.add( "Awaken to tier " + (tier + 1) + " (" + Reforge.costAwaken( tier ) + ")" ); kinds.add( 3 ); }
		labels.add( "Cancel" ); kinds.add( -1 );
		GameScene.show( new WndOptions( new ItemSprite( item ), item.name(),
				"You have " + quantity + " shards. Each choice costs the shards in brackets.\n\n"
				+ "_Reroll piece_: it becomes another piece of its set and tier, at the same level.\n"
				+ "_Reroll affix_: a weapon gets a new enchantment, an armor a new glyph.\n"
				+ "_Temper_: one or two levels more, up to +" + Reforge.TEMPER_CAP + ".\n"
				+ "_Awaken_: the same piece one tier up, if this cycle allows that tier.",
				labels.toArray( new String[0] ) ) {
			@Override
			protected void onSelect( int index ) {
				int kind = kinds.get( index );
				if (kind < 0) return;
				int cost = kind == 0 ? Reforge.costPiece( tier ) : kind == 1 ? Reforge.costAffix( tier )
						: kind == 2 ? Reforge.costTemper( tier ) : Reforge.costAwaken( tier );
				if (quantity < cost){
					GLog.w( "You need " + cost + " shards for that." );
					return;
				}
				if (kind == 0){
					Item neu = Reforge.rerollPiece( item );
					pay( hero, cost );
					Reforge.swap( hero, item, neu );
					GLog.p( "The piece reforges into " + neu.name() + "." );
				} else if (kind == 1){
					if (Reforge.rerollAffix( item )){ pay( hero, cost ); GLog.p( item.name() + " takes a new affix." ); }
				} else if (kind == 2){
					if (Reforge.temper( item )){ pay( hero, cost ); GLog.p( item.name() + " is tempered." ); }
				} else {
					Item neu = Reforge.awaken( item );
					pay( hero, cost );
					Reforge.swap( hero, item, neu );
					GLog.p( "It awakens as " + neu.name() + "." );
				}
			}
		} );
	}
}
