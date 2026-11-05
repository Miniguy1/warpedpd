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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Poison;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.GunSkills;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.sprites.CharSprite;

/**
 * The legendary items: unique, strong, and found only in white bags from the creatures that drop them (see LootTable).
 * Their effects ride on the same hooks as the gear sets (damage out, damage in, speed, evasion, experience, the turn),
 * plus the kill, the gold pickup, the magazine and the arrival on a floor.
 */
public final class Legendary {

	private Legendary(){}

	public static final Class<?>[] ALL = {
			Dawnrender.class, Widowmaker.class, MantleOfEchoes.class, GildedSignet.class, PhoenixFeather.class, GaleBoots.class,
			CarapaceOfTheDeep.class, Venomfang.class, Soulreaver.class, Thunderband.class,
			WellspringFlask.class, LodestarCompass.class, BandolierOfPlenty.class, AegisOfArrival.class };

	private static int dawnHits = 0, phoenixDepth = -1, wellspring = 0;
	private static boolean charged = false;

	public static void reset(){
		dawnHits = 0;
		phoenixDepth = -1;
		wellspring = 0;
		charged = false;
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

	/** any legendary the hero does not have yet (at the given upgrade level where it can take one), or null if they have them all */
	public static Item roll( int plus ){
		return roll( plus, null );
	}

	private static final String[] ROMAN = { "", "I", "II", "III", "IV" };

	/** the starting upgrade of a legendary of this rank: +1, +2, +3, +5 */
	public static int levelFor( int rank ){
		return rank <= 1 ? 1 : rank == 2 ? 2 : rank == 3 ? 3 : 5;
	}

	/** how often a rank turns up when a bag is made by hand: the weaker ones far more */
	private static int weight( int rank ){
		return rank <= 1 ? 8 : rank == 2 ? 4 : rank == 3 ? 2 : 1;
	}

	/** its colour: pale gold, blue, violet, flame */
	public static int glowFor( Class<?> c ){
		int r = LootTable.rankOf( c );
		return r <= 1 ? 0xFFE08A : r == 2 ? 0x7FD0FF : r == 3 ? 0xC77DFF : 0xFF7A3D;
	}

	/** the rank and the creatures that drop it, for the description */
	public static String rankLine( Class<?> c ){
		return "Power: rank " + ROMAN[Math.max( 1, Math.min( 4, LootTable.rankOf( c ) ) )] + " of IV.\nDropped by: " + LootTable.sourceNames( c ) + ".";
	}

	/** one legendary, identified, and at the given level where it can take one */
	public static Item make( Class<?> c, int level ){
		Item it = (Item) Reflection.newInstance( c );
		if (it == null) return null;
		it.cursed = false;
		it.identify();
		if (level > 0 && it.isUpgradable()) it.upgrade( level );
		return it;
	}

	/** one of the legendaries this creature drops (any, for no creature) that the hero does not have yet, or null if there is none */
	public static Item roll( int plus, Mob from ){
		Hero h = Dungeon.hero;
		ArrayList<Class<?>> pool = new ArrayList<>();
		if (from == null) for (Class<?> c : ALL) pool.add( c ); else pool.addAll( LootTable.legendariesFor( from ) );
		ArrayList<Class<?>> fresh = new ArrayList<>();
		int total = 0;
		for (Class<?> c : pool) if (!carried( h, c )) { fresh.add( c ); total += weight( LootTable.rankOf( c ) ); }
		if (fresh.isEmpty()) return null;
		int r = Random.Int( total );
		Class<?> pick = fresh.get( 0 );
		for (Class<?> c : fresh) {
			r -= weight( LootTable.rankOf( c ) );
			if (r < 0) { pick = c; break; }
		}
		return make( pick, plus >= 0 ? plus : levelFor( LootTable.rankOf( pick ) ) );
	}

	private static void heal( Hero h, int n ){
		h.HP = Math.min( h.HT, h.HP + n );
		if (h.sprite != null){
			h.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
			h.sprite.showStatus( CharSprite.POSITIVE, "+" + n );
		}
	}

	/** a blow about to land: Dawnrender, Soulreaver, Venomfang, Widowmaker and the Thunderband */
	public static int outgoing( Hero h, Char enemy, int out ){
		if (h == null || h.belongings == null || out <= 0) return out;
		boolean shot = h.belongings.thrownWeapon != null;
		if (!shot){
			if (h.belongings.weapon instanceof Dawnrender && ++dawnHits % 4 == 0){
				GunSkills.callOut( "Dawnrender!" );
				if (Dungeon.level != null){
					for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
						if (m != enemy && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( h.pos, m.pos ) <= 1){
							m.damage( Math.round( out * 1.5f ), h );
							if (m.sprite != null) m.sprite.flash();
						}
					}
				}
				heal( h, Math.max( 1, Math.round( h.HT * 0.08f ) ) );
			}
			if (h.belongings.weapon instanceof Soulreaver) heal( h, Math.max( 1, Math.round( out * 0.30f ) ) );
			if (h.belongings.weapon instanceof Venomfang && enemy != null && enemy.isAlive()){
				Buff.affect( enemy, Poison.class ).set( 5f + Dungeon.depth );
			}
		}
		if (shot && enemy != null && GunSkills.gun() instanceof Widowmaker && enemy.HP * 2 <= enemy.HT){
			out = Math.max( 1, Math.round( out * 1.6f ) );
		}
		if (charged && worn( h, Thunderband.class )){
			charged = false;
			out *= 3;
			GunSkills.callOut( "Thunderstruck!" );
		}
		return out;
	}

	/** a blow about to land on the hero: the Carapace, the Mantle's echo and the Phoenix's second chance */
	public static int incoming( Hero h, int in, Object src ){
		if (h == null || h.belongings == null || in <= 0) return in;
		if (h.belongings.armor instanceof CarapaceOfTheDeep){
			in = Math.max( 1, in - (3 + Dungeon.depth / 3) );
		}
		if (h.belongings.armor instanceof MantleOfEchoes){
			if (Random.Float() < 0.20f){
				GunSkills.callOut( "Echo!" );
				return 0;
			}
			if (src instanceof Char && src != h){
				Char striker = (Char) src;
				if (striker.isAlive() && striker.alignment != Char.Alignment.ALLY){
					striker.damage( Math.max( 1, Math.round( in * 0.40f ) ), h );
				}
			}
		}
		if (carried( h, PhoenixFeather.class ) && phoenixDepth != Dungeon.depth && in >= h.HP + h.shielding()){
			phoenixDepth = Dungeon.depth;
			h.HP = Math.max( h.HP, Math.round( h.HT * 0.6f ) );
			Buff.affect( h, Barrier.class ).incShield( Math.max( 1, Math.round( h.HT * 0.25f ) ) );
			GunSkills.callOut( "The Phoenix Feather burns!" );
			return 0;
		}
		return in;
	}

	public static float speed( Hero h ){
		return carried( h, GaleBoots.class ) ? 1.4f : 1f;
	}

	public static float evasion( Hero h ){
		return carried( h, GaleBoots.class ) ? 1.3f : 1f;
	}

	public static float exp( Hero h ){
		return carried( h, LodestarCompass.class ) ? 1.5f : 1f;
	}

	/** the Bandolier: one more round in the magazine */
	public static int magazine(){
		return carried( Dungeon.hero, BandolierOfPlenty.class ) ? 2 : 0;
	}

	/** each turn: the Wellspring Flask's trickle of health */
	public static void tick( Hero h ){
		if (!carried( h, WellspringFlask.class )){
			wellspring = 0;
			return;
		}
		if (++wellspring >= 2){
			wellspring = 0;
			if (h.isAlive() && h.HP < h.HT) heal( h, 1 );
		}
	}

	/** the hero arrives on a floor: the Aegis wraps them in a barrier */
	public static void onArrive( Hero h ){
		if (carried( h, AegisOfArrival.class )){
			Buff.affect( h, Barrier.class ).incShield( 15 + 2 * Dungeon.depth );
		}
	}

	/** a kill: Widowmaker's refund, the Signet's barrier, the Thunderband's charge and the Bandolier's bullets */
	public static void onKill( Mob mob ){
		Hero h = Dungeon.hero;
		if (h == null) return;
		Gun g = GunSkills.gun();
		if (g instanceof Widowmaker && g.round() < g.maxRound()){
			g.manualReload( 1, false );
		}
		if (worn( h, GildedSignet.class )){
			Buff.affect( h, Barrier.class ).incShield( 5 + Dungeon.depth / 2 );
		}
		if (worn( h, Thunderband.class )) charged = true;
		if (carried( h, BandolierOfPlenty.class ) && Random.Int( 4 ) != 0) Dungeon.bullet += 1 + Random.Int( 3 );
	}

	/** the Gilded Signet: gold picked up is a half greater */
	public static int goldBonus( Hero h, int quantity ){
		return worn( h, GildedSignet.class ) ? Math.round( quantity * 2f ) : quantity;
	}
}
