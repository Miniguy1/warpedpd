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

package xyz.gabriwar.warpedpixeldungeon.items.armor;

/** The end-game robes: plate-class protection that a lighter hero can still wear (3 less Strength
 *  than the same tier normally asks), +2 more armor, and a standing immunity to one kind of harm.
 *  The immunity is read by Char.isImmune() while the armor is worn. */
public abstract class EndgameArmor extends Armor {

	public EndgameArmor( int tier ) {
		super( tier );
	}

	/** the effects the wearer cannot suffer; subclasses of the listed classes count too */
	public abstract Class[] grantedImmunities();

	@Override
	public int DRMax( int lvl ) {
		return super.DRMax( lvl ) + 2;
	}

	/** what this armor does to whoever strikes the wearer, on top of an armor's usual glyph (none by default).
	 *  A class armor made from it carries this on. */
	public int specialProc( xyz.gabriwar.warpedpixeldungeon.actors.Char attacker, xyz.gabriwar.warpedpixeldungeon.actors.Char defender, int damage ) {
		return damage;
	}

	@Override
	public int proc( xyz.gabriwar.warpedpixeldungeon.actors.Char attacker, xyz.gabriwar.warpedpixeldungeon.actors.Char defender, int damage ) {
		damage = super.proc( attacker, defender, damage );
		return specialProc( attacker, defender, damage );
	}

	private static final Class[] NONE = new Class[0];

	/** what the worn armor is proof against: an end-game robe's own, or the one a class armor was made from */
	public static Class[] grantedBy( Armor worn ) {
		if (worn instanceof EndgameArmor) return ((EndgameArmor)worn).grantedImmunities();
		if (worn instanceof ClassArmor) {
			Armor p = ((ClassArmor)worn).proto();
			if (p instanceof EndgameArmor) return ((EndgameArmor)p).grantedImmunities();
		}
		return NONE;
	}
}
