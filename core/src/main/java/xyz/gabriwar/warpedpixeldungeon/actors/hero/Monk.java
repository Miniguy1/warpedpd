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

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Ki;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.*;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfForce;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;

/**
 * The Monk: bare fists, and power that stacks. Ki builds from landed fists and from standing still; every stack is passive
 * damage and passive protection, and the passive auras of the skill tree read it.
 */
public final class Monk {

	private Monk(){}

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

	/** Mountain Stance: while the hero holds five ki, every aura is stronger */
	public static float boost( Hero h ) {
		float b = 1f;
		if (stacks( h ) >= 5) b += 0.15f * lvl( h, KiMountain.class );
		return b;
	}

	public static boolean full( Hero h ) {
		Ki k = h == null ? null : h.buff( Ki.class );
		return k != null && k.stacks() >= k.max();
	}

	/** a Monk's bare fists: 3 + half the level up to 8 + the level */
	public static int fistRoll( Hero h ) {
		return Hero.heroDamageIntRange( 3 + h.lvl / 2, 8 + h.lvl );
	}

	/** a fist about to land: the innate aura (3% a stack), and the ki it feeds */
	public static int outgoing( Hero h, Char enemy, int out ) {
		if (!isMonk( h ) || out <= 0 || enemy == null || h.belongings == null || h.belongings.thrownWeapon != null) return out;
		if (!RingOfForce.fightingUnarmed( h )) return out;
		Ki k = ki( h );
		int before = k.stacks();
		out = Math.max( 1, Math.round( out * (1f + 0.03f * before * boost( h )) ) );
		int gain = 1;
		if (Random.Float() < 0.25f * lvl( h, KiFlowing.class )) gain++;
		int rapid = lvl( h, KiRapidPalms.class );
		if (rapid >= 2) gain += rapid - 1;
		k.gain( gain );
		return out;
	}

	/** the innate protection: a third of a point off every blow for each stack of ki */
	public static int incoming( Hero h, int in ) {
		if (!isMonk( h ) || in <= 0) return in;
		return Math.max( 1, in - stacks( h ) / 3 );
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
