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

package xyz.gabriwar.warpedpixeldungeon.actors.hero.skills;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Monk;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.MagicMissile;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/**
 * Ki Bolt, the Monk's one active skill: a bolt of ki flame at an enemy in view. It does the Monk's fist damage, multiplied
 * by his ki (+12% a stack) and by the skill (+20% a level), it ignores armor, and it costs a good part of the mana pool, so
 * it is not for spamming. At the top level it bursts: every other enemy beside the target takes half.
 */
public class KiBolt extends Skill {

	{
		name = "Ki Bolt";
		castText = "Ki bolt!";
		image = 44;
		mana = 14;
		tier = 2;
		tag = "A4";
	}

	@Override public boolean toggleable(){ return false; }

	@Override protected boolean upgrade(){ return true; }

	@Override
	public ArrayList<String> actions( Hero hero ){
		ArrayList<String> out = new ArrayList<>();
		if (level > 0 && hero.MP >= getManaCost()) out.add( AC_CAST );
		return out;
	}

	/** 14, 12, 10: a third to a half of the thirty-point pool */
	@Override
	public int getManaCost(){
		return 14 - 2 * Math.max( 0, level - 1 );
	}

	/** how far the bolt reaches: 6, 7, 8 */
	public static int range( int level ){
		return 5 + Math.max( 1, level );
	}

	/** the bolt's strength over a punch: 12% for each ki, 20% for each level, and the aura boost */
	public static float power( Hero h, int level ){
		return (1f + 0.12f * Monk.stacks( h )) * (1f + 0.20f * level) * Monk.boost( h );
	}

	public static int damage( Hero h, int level ){
		return Math.max( 1, Math.round( Monk.fistRoll( h ) * power( h, level ) ) );
	}

	/** the bolt lands: damage on the target (through armor), and at the top level half of it on every other enemy beside. Returns the damage dealt to the target. */
	public static int land( Hero h, int level, Char target ) {
		if (h == null || target == null || !target.isAlive()) return 0;
		int dmg = damage( h, level );
		target.damage( dmg, h );
		if (level >= MAX_LEVEL && Dungeon.level != null) {
			int splash = Math.max( 1, Math.round( dmg * 0.5f ) );
			for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
				if (m != target && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( target.pos, m.pos ) <= 1) {
					m.damage( splash, h );
				}
			}
		}
		return dmg;
	}

	//a second press of the skill fires at the target it has marked
	@Override
	public boolean confirmTarget( Hero hero ){ return GunSkills.confirmPending( hero, this ); }

	@Override
	public void execute( final Hero hero, String action ){
		if (!AC_CAST.equals( action ) || level <= 0 || hero.MP < getManaCost()) return;
		if (confirmTarget( hero )) return;
		GunSkills.pickEnemy( this, Messages.get( this, "prompt" ), e -> {
			if (Dungeon.level.distance( hero.pos, e.pos ) > range( level )) {
				GLog.w( Messages.get( KiBolt.this, "too_far" ) );
				return;
			}
			final int cast = level;
			final Char target = e;
			hero.sprite.zap( target.pos );
			hero.busy();
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
			MagicMissile.boltFromChar( hero.sprite.parent, MagicMissile.FIRE, hero.sprite, target.pos, new Callback() {
				@Override
				public void call() {
					land( hero, cast, target );
					CellEmitter.center( target.pos ).burst( Speck.factory( Speck.LIGHT ), 8 );
					GunSkills.finish( hero, KiBolt.this, 1f );
				}
			} );
		} );
	}
}
