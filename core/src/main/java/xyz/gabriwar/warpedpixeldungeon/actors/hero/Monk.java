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

package xyz.gabriwar.warpedpixeldungeon.actors.hero;

import com.watabou.utils.Random;

import java.util.HashSet;

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Ki;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.*;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfForce;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;

/**
 * The Monk: bare fists, and power that stacks. Ki builds from landed fists and from moving in on a fight; every stack is
 * passive damage and passive protection, and the passive auras of the skill tree (and his talents) read it.
 */
public final class Monk {

	private Monk(){}

	//the enemies whose first fist has landed, on this floor (not saved), and the fists landed in a row for the storm of palms
	private static final HashSet<Integer> opened = new HashSet<>();
	private static int openedDepth = -1;
	private static int palms = 0;

	public static void reset(){
		opened.clear();
		openedDepth = -1;
		palms = 0;
	}

	private static boolean fresh( Char enemy ) {
		if (openedDepth != Dungeon.depth) {
			opened.clear();
			openedDepth = Dungeon.depth;
		}
		return !opened.contains( enemy.id() );
	}

	public static boolean isMonk( Hero h ) {
		return h != null && h.heroClass == HeroClass.MONK;
	}

	/** the hero's ki, made if it is missing (a Monk always has it) */
	public static Ki ki( Hero h ) {
		if (!isMonk( h )) return null;
		Ki k = h.buff( Ki.class );
		if (k == null) k = Buff.affect( h, Ki.class );
		return k;
	}

	public static int stacks( Hero h ) {
		Ki k = h == null ? null : h.buff( Ki.class );
		return k == null ? 0 : k.stacks();
	}

	/** the level of one of the hero's skills (0 if they do not have it) */
	public static int lvl( Hero h, Class<? extends Skill> c ) {
		if (h == null || h.heroSkills == null) return 0;
		Skill s = h.heroSkills.get( c );
		return s == null ? 0 : s.level;
	}

	/** the points the hero has in a talent (0 for no hero) */
	public static int tal( Hero h, Talent t ) {
		return h == null ? 0 : h.pointsInTalent( t );
	}

	/** Mountain Stance (and the talent): while the hero holds five ki, every aura is stronger */
	public static float boost( Hero h ) {
		float b = 1f;
		if (stacks( h ) >= 5) b += 0.15f * lvl( h, KiMountain.class ) + 0.05f * tal( h, Talent.KI_MOUNTAIN_HEART );
		return b;
	}

	/** is an enemy in the hero's view? */
	public static boolean enemyInView( Hero h ) {
		Level level = Dungeon.level;
		if (level == null || h == null) return false;
		for (Mob m : level.mobs.toArray( new Mob[0] )) {
			if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && level.heroFOV[m.pos]) return true;
		}
		return false;
	}

	/** is an enemy right beside the hero? */
	public static boolean enemyAdjacent( Hero h ) {
		Level level = Dungeon.level;
		if (level == null || h == null) return false;
		for (Mob m : level.mobs.toArray( new Mob[0] )) {
			if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && level.distance( h.pos, m.pos ) <= 1) return true;
		}
		return false;
	}

	public static boolean full( Hero h ) {
		Ki k = h == null ? null : h.buff( Ki.class );
		return k != null && k.stacks() >= k.max();
	}

	/** a Monk's bare fists: 3 + half the level up to 8 + the level */
	public static int fistRoll( Hero h ) {
		return Hero.heroDamageIntRange( 3 + h.lvl / 2, 8 + h.lvl );
	}

	/** a fist about to land: the innate aura (3% a stack), the talents that add to it, and the ki it feeds */
	public static int outgoing( Hero h, Char enemy, int out ) {
		if (!isMonk( h ) || out <= 0 || enemy == null || h.belongings == null || h.belongings.thrownWeapon != null) return out;
		if (!RingOfForce.fightingUnarmed( h )) return out;
		Ki k = ki( h );
		int before = k.stacks();
		float mult = 1f + 0.03f * before * boost( h );
		//opening palm: the first fist you land on each enemy
		int open = tal( h, Talent.KI_OPENING_PALM );
		if (open > 0 && fresh( enemy )) {
			opened.add( enemy.id() );
			mult += 0.25f * open;
		}
		if (before >= 8) mult += 0.10f * tal( h, Talent.KI_STORM_SURGE );
		mult += 0.08f * tal( h, Talent.KI_DIAMOND_FISTS );
		out = Math.max( 1, Math.round( out * mult ) );
		int gain = 1;
		if (Random.Float() < 0.25f * lvl( h, KiFlowing.class )) gain++;
		int rapid = lvl( h, KiRapidPalms.class );
		if (rapid >= 2) gain += rapid - 1;
		k.gain( gain );
		//flurry: a second fist for half
		int flurry = tal( h, Talent.KI_FLURRY );
		if (flurry > 0 && enemy.isAlive() && Random.Float() < 0.10f * flurry) enemy.damage( Math.max( 1, Math.round( out * 0.5f ) ), h );
		//the storm of palms: every fifth fist strikes everything else beside you
		int storm = tal( h, Talent.KI_STORM_OF_PALMS );
		if (storm > 0 && ++palms % 5 == 0 && Dungeon.level != null) {
			int spill = Math.max( 1, Math.round( out * 0.25f * storm ) );
			for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
				if (m != enemy && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( h.pos, m.pos ) <= 1) m.damage( spill, h );
			}
		}
		return out;
	}

	/** the innate protection (a point off every blow for each three stacks), and the talents that add to it or answer a blow */
	public static int incoming( Hero h, int in, Object src ) {
		if (!isMonk( h ) || in <= 0) return in;
		//pain feeds ki
		int pain = tal( h, Talent.KI_PAIN_FOCUS );
		if (pain > 0 && Random.Float() < 0.30f * pain) {
			Ki k = ki( h );
			if (k != null) k.gain( 1 );
		}
		//counter palm: half a punch back at whoever struck from beside you
		int counter = tal( h, Talent.KI_COUNTER_PALM );
		if (counter > 0 && src instanceof Char && src != h) {
			Char c = (Char) src;
			if (c.alignment == Char.Alignment.ENEMY && c.isAlive() && Dungeon.level != null
					&& Dungeon.level.distance( h.pos, c.pos ) <= 1 && Random.Float() < 0.15f * counter) {
				c.damage( Math.max( 1, fistRoll( h ) / 2 ), h );
			}
		}
		int cut = stacks( h ) / 3 + tal( h, Talent.KI_THICK_SKIN );
		if (stacks( h ) >= 5) cut += 2 * tal( h, Talent.KI_UNSHAKABLE );
		return Math.max( 1, in - cut );
	}

	public static int incoming( Hero h, int in ) {
		return incoming( h, in, null );
	}

	/** a kill: relentless feeds ki, and the stone breath wraps the monk in a barrier */
	public static void onKill( Mob mob ) {
		Hero h = Dungeon.hero;
		if (!isMonk( h )) return;
		Ki k = ki( h );
		int rel = tal( h, Talent.KI_RELENTLESS );
		if (k != null && rel > 0) k.gain( 2 * rel );
		int stone = tal( h, Talent.KI_STONE_BREATH );
		if (stone > 0) Buff.affect( h, Barrier.class ).incShield( 2 * stone + Dungeon.depth / 4 );
	}

	/** a meal: focused meal turns it into ki */
	public static void onFood( Hero h ) {
		int pts = tal( h, Talent.KI_MEAL );
		if (!isMonk( h ) || pts <= 0) return;
		Ki k = ki( h );
		if (k != null) k.gain( 1 + 2 * pts );
	}

	private static void heal( Hero h, int n ) {
		if (n <= 0 || h.HP >= h.HT) return;
		h.HP = Math.min( h.HT, h.HP + n );
		if (h.sprite != null) h.sprite.showStatus( xyz.gabriwar.warpedpixeldungeon.sprites.CharSprite.POSITIVE, "+" + n );
	}

	/** each turn: the auras that work on their own */
	public static void auras( Hero h, Ki k ) {
		Level level = Dungeon.level;
		if (level == null || h == null) return;
		int ki = k.stacks();
		float boost = boost( h );
		int wrath = lvl( h, KiAuraWrath.class );
		if (wrath > 0 && ki >= 5) {
			int dmg = Math.max( 1, Math.round( (wrath + ki / 3f) * boost ) );
			for (Mob m : level.mobs.toArray( new Mob[0] )) {
				if (m.alignment == Char.Alignment.ENEMY && m.isAlive() && level.distance( h.pos, m.pos ) <= 1) m.damage( dmg, h );
			}
		}
		int renewal = lvl( h, KiAuraRenewal.class );
		if (renewal > 0 && ki >= 4 && ++k.renew >= 5 - renewal) {
			k.renew = 0;
			heal( h, renewal );
		}
	}
}
