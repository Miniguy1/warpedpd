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

package xyz.gabriwar.warpedpixeldungeon.items.legendary;

import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.GunSkills;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.sprites.CharSprite;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;

/**
 * The legendary items: unique, strong, and found only in white bags. Their effects ride on the same hooks as the gear
 * sets (damage out, damage in, speed, evasion), plus the kill and the gold pickup.
 */
public final class Legendary {

	private Legendary(){}

	public static final Class<?>[] ALL = { Dawnrender.class, Widowmaker.class, MantleOfEchoes.class, GildedSignet.class, PhoenixFeather.class, GaleBoots.class };

	private static int dawnHits = 0, phoenixDepth = -1;

	public static void reset(){
		dawnHits = 0;
		phoenixDepth = -1;
	}

	/** is it on the hero (wielded, worn or on a finger)? */
	public static boolean worn( Hero h, Class<?> c ){
		if (h == null || h.belongings == null) return false;
		return c.isInstance( h.belongings.weapon ) || c.isInstance( h.belongings.armor ) || c.isInstance( h.belongings.ring )
				|| c.isInstance( h.belongings.misc ) || c.isInstance( h.belongings.artifact );
	}

	/** is it anywhere on the hero (carried ones count in the pack)? */
	public static boolean carried( Hero h, Class<?> c ){
		if (h == null || h.belongings == null) return false;
		for (Item it : h.belongings) if (c.isInstance( it )) return true;
		return false;
	}

	/** one of the legendaries the hero does not have yet (at the given upgrade level where it can take one), or null if they have them all */
	public static Item roll( int plus ){
		Hero h = Dungeon.hero;
		ArrayList<Class<?>> fresh = new ArrayList<>();
		for (Class<?> c : ALL) if (!carried( h, c )) fresh.add( c );
		if (fresh.isEmpty()) return null;
		Item it = (Item) Reflection.newInstance( fresh.get( Random.Int( fresh.size() ) ) );
		if (it == null) return null;
		it.cursed = false;
		it.identify();
		if (plus > 0 && it.isUpgradable()) it.upgrade( plus );
		return it;
	}

	private static void heal( Hero h, int n ){
		h.HP = Math.min( h.HT, h.HP + n );
		if (h.sprite != null){
			h.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
			h.sprite.showStatus( CharSprite.POSITIVE, "+" + n );
		}
	}

	/** a blow about to land: Dawnrender's burst and Widowmaker's finishing power */
	public static int outgoing( Hero h, Char enemy, int out ){
		if (h == null || h.belongings == null || out <= 0) return out;
		boolean shot = h.belongings.thrownWeapon != null;
		if (!shot && h.belongings.weapon instanceof Dawnrender){
			if (++dawnHits % 5 == 0){
				GunSkills.callOut( "Dawnrender!" );
				if (Dungeon.level != null){
					for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
						if (m != enemy && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( h.pos, m.pos ) <= 1){
							m.damage( out, h );
							if (m.sprite != null) m.sprite.flash();
						}
					}
				}
				heal( h, Math.max( 1, Math.round( h.HT * 0.05f ) ) );
			}
		}
		if (shot && enemy != null && GunSkills.gun() instanceof Widowmaker && enemy.HP * 2 <= enemy.HT){
			out = Math.max( 1, Math.round( out * 1.4f ) );
		}
		return out;
	}

	/** a blow about to land on the hero: the Mantle's echo and the Phoenix's second chance */
	public static int incoming( Hero h, int in, Object src ){
		if (h == null || h.belongings == null || in <= 0) return in;
		if (h.belongings.armor instanceof MantleOfEchoes){
			if (Random.Float() < 0.15f){
				GunSkills.callOut( "Echo!" );
				return 0;
			}
			if (src instanceof Char && src != h){
				Char striker = (Char) src;
				if (striker.isAlive() && striker.alignment != Char.Alignment.ALLY){
					striker.damage( Math.max( 1, Math.round( in * 0.30f ) ), h );
				}
			}
		}
		if (carried( h, PhoenixFeather.class ) && phoenixDepth != Dungeon.depth && in >= h.HP + h.shielding()){
			phoenixDepth = Dungeon.depth;
			h.HP = Math.max( h.HP, h.HT / 2 );
			GunSkills.callOut( "The Phoenix Feather burns!" );
			return 0;
		}
		return in;
	}

	public static float speed( Hero h ){
		return carried( h, GaleBoots.class ) ? 1.3f : 1f;
	}

	public static float evasion( Hero h ){
		return carried( h, GaleBoots.class ) ? 1.2f : 1f;
	}

	/** a kill: Widowmaker's refund and the Gilded Signet's barrier */
	public static void onKill( Mob mob ){
		Hero h = Dungeon.hero;
		if (h == null) return;
		Gun g = GunSkills.gun();
		if (g instanceof Widowmaker && g.round() < g.maxRound()){
			g.manualReload( 1, false );
		}
		if (worn( h, GildedSignet.class )){
			Buff.affect( h, Barrier.class ).incShield( 3 + Dungeon.depth / 3 );
		}
	}

	/** the Gilded Signet: gold picked up is a half greater */
	public static int goldBonus( Hero h, int quantity ){
		return worn( h, GildedSignet.class ) ? Math.round( quantity * 1.5f ) : quantity;
	}
}
