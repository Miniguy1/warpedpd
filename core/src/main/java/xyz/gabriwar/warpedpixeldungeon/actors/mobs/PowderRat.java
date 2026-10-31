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

package xyz.gabriwar.warpedpixeldungeon.actors.mobs;

import com.watabou.utils.Random;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.particles.BlastParticle;
import xyz.gabriwar.warpedpixeldungeon.sprites.PowderRatSprite;
import com.watabou.noosa.audio.Sample;

/**
 * A rat that has been at a powder keg: when it dies it bursts, and hurts everything standing next to it (other
 * enemies too, and another Powder Rat in the blast goes off in turn). Best shot from a distance.
 */
public class PowderRat extends Rat {

	{
		spriteClass = PowderRatSprite.class;
		HP = HT = 6 + (Dungeon.depth * Random.NormalIntRange( 1, 2 ));
		EXP = 2;
		maxLvl = 7;
		loot = null;
		lootChance = 0f;
	}

	private boolean exploded = false;

	@Override
	public void setStatDepth( int depth ) {
		super.setStatDepth( depth );
		HP = HT = 6 + (depth * Random.NormalIntRange( 1, 2 ));
	}

	/** the burst: whatever stands next to it, the hero and the other enemies, takes 2 to 4 plus the floor's depth */
	public void explode() {
		if (exploded) return;
		exploded = true;
		if (sprite != null && Dungeon.level != null && Dungeon.level.heroFOV[pos]) {
			CellEmitter.center( pos ).burst( BlastParticle.FACTORY, 8 );
			Sample.INSTANCE.play( Assets.Sounds.BLAST );
		}
		java.util.ArrayList<Char> near = new java.util.ArrayList<>();
		Hero h = Dungeon.hero;
		if (h != null && h.isAlive() && Dungeon.level.adjacent( pos, h.pos )) near.add( h );
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (m != this && m.isAlive() && Dungeon.level.adjacent( pos, m.pos )) near.add( m );
		}
		for (Char c : near) {
			c.damage( Random.NormalIntRange( 2, 4 + statDepth() ), this );
		}
	}

	@Override
	public void die( Object cause ) {
		explode();
		super.die( cause );
	}
}
