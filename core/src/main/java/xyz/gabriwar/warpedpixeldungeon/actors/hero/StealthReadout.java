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

import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/**
 * Shows what is hiding the hero from sleeping enemies. A sleeper wakes with a 1 in (distance + stealth) chance each turn, and
 * the stealth is the sum of the calendar (night, new moon, shadowday), crouching, armor and skills. The first time a sleeper
 * is sighted on a floor (and again when the stealth has changed, no more than once in thirty turns) the game says so.
 */
public final class StealthReadout {

	private StealthReadout(){}

	private static int lastDepth = -1, lastTurn = -1000, lastStealth = Integer.MIN_VALUE;

	public static void reset(){
		lastDepth = -1;
		lastTurn = -1000;
		lastStealth = Integer.MIN_VALUE;
	}

	private static void add( StringBuilder sb, String what, float v ) {
		if (Math.abs( v ) < 0.01f) return;
		if (sb.length() > 0) sb.append( ", " );
		sb.append( what ).append( v > 0 ? " +" : " " ).append( Math.round( v ) );
	}

	/** the parts of the hero's stealth: "night +2, shadowday +3" (the same sums as Char.stealth) */
	public static String breakdown( Hero h ) {
		StringBuilder sb = new StringBuilder();
		float known = 0;
		float night = xyz.gabriwar.warpedpixeldungeon.actors.DayNightCycle.isNight() ? 2f : 0f; add( sb, "night", night ); known += night;
		float moon = xyz.gabriwar.warpedpixeldungeon.actors.GameCalendar.isNewMoon() ? 1f : 0f; add( sb, "new moon", moon ); known += moon;
		float firefly = h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.FireflyGlow.class ) != null ? xyz.gabriwar.warpedpixeldungeon.actors.buffs.FireflyGlow.STEALTH_BONUS : 0f;
		add( sb, "fireflies", firefly ); known += firefly;
		float eclipse = h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.SolarEclipseBuff.class ) != null ? xyz.gabriwar.warpedpixeldungeon.actors.buffs.SolarEclipseBuff.STEALTH_BONUS : 0f;
		add( sb, "eclipse", eclipse ); known += eclipse;
		float weekday = xyz.gabriwar.warpedpixeldungeon.actors.GameCalendar.weekdayEvasionBonus(); add( sb, "shadowday", weekday ); known += weekday;
		float crouch = h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Crouching.class ) != null ? xyz.gabriwar.warpedpixeldungeon.actors.buffs.Crouching.STEALTH_BONUS : 0f;
		add( sb, "crouching", crouch ); known += crouch;
		float glyph = xyz.gabriwar.warpedpixeldungeon.items.armor.glyphs.Obfuscation.stealthBoost( h, h.glyphLevel( xyz.gabriwar.warpedpixeldungeon.items.armor.glyphs.Obfuscation.class ) );
		add( sb, "obfuscation", glyph ); known += glyph;
		float skills = h.heroSkills == null ? 0f : h.heroSkills.allStealth(); add( sb, "skills", skills ); known += skills;
		float hush = xyz.gabriwar.warpedpixeldungeon.items.rarity.Quality.value( h.belongings.armor(), xyz.gabriwar.warpedpixeldungeon.items.rarity.RarityLine.HUSHED );
		add( sb, "hushed armor", hush ); known += hush;
		float soaked = hush <= 0f && h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.SoakedShoes.class ) != null ? xyz.gabriwar.warpedpixeldungeon.actors.buffs.SoakedShoes.STEALTH_PENALTY : 0f;
		add( sb, "soaked shoes", soaked ); known += soaked;
		add( sb, "other", h.stealth() - known );
		return sb.toString();
	}

	/** the whole line for one sleeper */
	public static String describe( Hero h, Mob mob ) {
		int stealth = Math.round( h.stealth() );
		int dist = Dungeon.level == null ? 1 : Dungeon.level.distance( h.pos, mob.pos );
		String parts = breakdown( h );
		return "A sleeping " + mob.name() + " is in view. Your stealth is +" + stealth + (parts.isEmpty() ? "" : " (" + parts + ")")
				+ ": at distance " + dist + " it wakes 1 turn in " + Math.max( 1, dist + stealth ) + ".";
	}

	/** say it on a new floor, and again when the stealth has changed (but not more than once in thirty turns) */
	public static boolean shouldSay( int depth, int stealth, int turn ) {
		boolean say = depth != lastDepth || (stealth != lastStealth && turn - lastTurn >= 30);
		if (say) {
			lastDepth = depth;
			lastStealth = stealth;
			lastTurn = turn;
		}
		return say;
	}

	/** the hero has just sighted this enemy */
	public static void onSpotted( Hero h, Mob mob ) {
		if (h == null || mob == null || h != Dungeon.hero || Dungeon.level == null) return;
		if (mob.state != mob.SLEEPING) return;
		if (!shouldSay( Dungeon.depth, Math.round( h.stealth() ), Dungeon.cycleTurn )) return;
		GLog.i( describe( h, mob ) );
	}
}
