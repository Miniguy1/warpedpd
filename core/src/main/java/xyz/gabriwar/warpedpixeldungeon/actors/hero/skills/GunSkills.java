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

import com.watabou.utils.Random;

import java.util.ArrayList;

import xyz.gabriwar.warpedpixeldungeon.Assets;
import xyz.gabriwar.warpedpixeldungeon.Dungeon;
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.*;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.effects.CellEmitter;
import xyz.gabriwar.warpedpixeldungeon.effects.Speck;
import xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfBlastWave;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.mechanics.Ballistica;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.scenes.CellSelector;
import xyz.gabriwar.warpedpixeldungeon.scenes.GameScene;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/** What the Gunslinger's skills ask of the firearm, and what the firearm asks of them. */
public final class GunSkills {

	private GunSkills(){}

	/** scales the time a shot takes (Snap Shot sets it for the one shot it fires) */
	public static float shotDelay = 1f;

	public static Gun gun(){
		Hero h = Dungeon.hero;
		if (h == null || h.belongings == null) return null;
		Object w = h.belongings.weapon();
		return w instanceof Gun ? (Gun) w : null;
	}

	public static int lvl( Class<? extends Skill> cls ){
		Hero h = Dungeon.hero;
		return h == null || h.heroSkills == null ? 0 : h.heroSkills.skillLevel( cls );
	}

	public static boolean full( Gun g ){
		return g != null && g.round() >= g.maxRound();
	}

	/** Deep Pockets: rounds added to the magazine */
	public static int magazineBonus(){
		return lvl( DeepPockets.class );
	}

	/** Quick Hands shortens a reload by 20/35/50%; a Long Barrel makes it 20% slower */
	public static float reloadFactor(){
		int q = lvl( QuickHands.class );
		float f = q > 0 ? 1f - (0.05f + 0.15f * q) : 1f;
		if (lvl( LongBarrel.class ) > 0) f *= 1.2f;
		f *= Math.max( 0.35f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.of( Dungeon.hero ).reload );   //the Gunslinger's Rig
		return f * xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.reloadFactor();
	}

	public static boolean hipFire(){
		return lvl( HipFire.class ) > 0;
	}

	/** a roll of a firearm's own damage, the way the Gunslinger's skills deal it */
	public static int roll( Gun g ){
		return Random.NormalIntRange( g.bulletMin(), Math.max( g.bulletMin(), g.bulletMax() ) );
	}

	/** the Gunslinger knows firearms: they ask for 2 less Strength than they ask of anyone else (a new hero has 10, and the
	 *  revolver, a tier 2 gun, asks 12) */
	public static int strengthDiscount(){
		Hero h = Dungeon.hero;
		return h != null && h.heroClass == xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER ? 2 : 0;
	}

	/** a firearm in hand with a round in it, else a word to the player and null */
	public static Gun needLoaded(){
		Gun g = gun();
		if (g == null){ GLog.w( "You need a firearm equipped." ); return null; }
		if (g.round() <= 0){ GLog.w( "Your gun is empty." ); return null; }
		return g;
	}

	public interface Target { void on( Char enemy ); }

	public interface Spot { void on( int cell ); }

	/** asks for a visible enemy */
	public static void pickEnemy( final String prompt, final Target then ){
		GameScene.selectCell( new CellSelector.Listener(){
			@Override
			public void onSelect( Integer cell ){
				if (cell == null) return;
				Char e = Actor.findChar( cell );
				if (e == null || e.alignment != Char.Alignment.ENEMY || !Dungeon.level.heroFOV[cell]){
					GLog.w( "Pick an enemy you can see." );
					return;
				}
				then.on( e );
			}
			@Override
			public String prompt(){ return prompt; }
		} );
	}

	/** asks for any visible tile */
	public static void pickSpot( final String prompt, final Spot then ){
		GameScene.selectCell( new CellSelector.Listener(){
			@Override
			public void onSelect( Integer cell ){
				if (cell == null) return;
				if (!Dungeon.level.heroFOV[cell]){ GLog.w( "You cannot see that spot." ); return; }
				then.on( cell );
			}
			@Override
			public String prompt(){ return prompt; }
		} );
	}

	/** the end of a cast: the mana, the shout, and the time */
	public static void finish( Hero hero, Skill skill, float time ){
		hero.MP -= skill.getManaCost();
		skill.castTextYell();
		Invisibility.dispel();
		hero.heroSkills.lastUsed = skill;
		hero.spendAndNext( time );
	}
}
