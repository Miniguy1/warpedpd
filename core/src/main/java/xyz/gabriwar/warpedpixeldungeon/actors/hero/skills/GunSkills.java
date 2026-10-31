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
		if (w instanceof Gun) return (Gun) w;
		return sidearm( h );
	}

	/** a Gunslinger's sidearm: with something else in hand, the best gun carried in the pack still fires, and the skills still count */
	public static Gun sidearm( Hero h ){
		if (h == null || h.belongings == null || h.heroClass != xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER) return null;
		Gun best = null;
		for (xyz.gabriwar.warpedpixeldungeon.items.Item it : h.belongings.backpack.items){
			if (!(it instanceof Gun)) continue;
			Gun g = (Gun) it;
			if (best == null || g.tier() > best.tier() || (g.tier() == best.tier() && g.level() > best.level())) best = g;
		}
		return best;
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

	/**
	 * The Gunslinger's starting revolver rolls its rarity, type (gamma, beta or alpha) and bonus lines the way any fresh
	 * weapon does. The starting kit is packed before a level exists, so it waits for the first one; the roll happens
	 * once, since an item that has a quality is left alone. A third of the time it also starts at +1 with an enchantment.
	 */
	public static void rollStarter( Hero h ){
		if (h == null || h.heroClass != xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER || h.belongings == null) return;
		Object w = h.belongings.weapon;
		if (!(w instanceof xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver)) return;
		xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver r = (xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver) w;
		if (xyz.gabriwar.warpedpixeldungeon.items.rarity.Quality.of( r ) != null) return;
		xyz.gabriwar.warpedpixeldungeon.items.rarity.Quality.roll( r );
		if (Random.Float() < 0.30f){
			r.upgrade();
			if (r.enchantment == null) r.enchant();
		}
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

	/** the skill whose selection is open (so a second press of that skill can confirm its suggested target) */
	private static Pending pending;

	private static boolean validEnemy( Hero hero, Char e ){
		return hero != null && e != null && e.isAlive() && e.alignment == Char.Alignment.ENEMY && Actor.chars().contains( e )
				&& Dungeon.level != null && Dungeon.level.heroFOV[e.pos] && SkillInteractions.valid( e.pos ) && SkillInteractions.clear( hero.pos, e.pos );
	}

	/** the enemy a skill suggests: the one you last aimed at, else the nearest one you can see */
	public static Char autoTarget( Hero hero ){
		Char best = xyz.gabriwar.warpedpixeldungeon.ui.QuickSlotButton.lastTarget;
		if (validEnemy( hero, best )) return best;
		best = null;
		for (Char e : Actor.chars()){
			if (validEnemy( hero, e ) && (best == null || Dungeon.level.distance( hero.pos, e.pos ) < Dungeon.level.distance( hero.pos, best.pos ))) best = e;
		}
		return best;
	}

	/** a second press of the skill: fire at the suggested target (false when there is none to confirm) */
	public static boolean confirmPending( Hero hero, Skill owner ){
		Pending p = pending;
		if (p == null || owner == null || p.owner != owner || !validEnemy( hero, p.suggested )) return false;
		GameScene.handleCell( p.suggested.pos );
		return true;
	}

	private static class Pending extends CellSelector.Listener {
		final Skill owner;
		final String prompt;
		final Target then;
		Char suggested;
		boolean finished;
		com.watabou.noosa.Image marker;

		Pending( Skill owner, String prompt, Target then ){
			this.owner = owner; this.prompt = prompt; this.then = then;
		}

		void showMarker(){
			if (suggested == null || suggested.sprite == null || suggested.sprite.parent == null) return;
			marker = new com.watabou.noosa.Image( xyz.gabriwar.warpedpixeldungeon.ui.Icons.get( xyz.gabriwar.warpedpixeldungeon.ui.Icons.TARGET ) ){
				@Override
				public void update(){
					super.update();
					visible = validEnemy( Dungeon.hero, suggested ) && suggested.sprite.visible;
					point( suggested.sprite.center( this ) );
				}
			};
			marker.point( suggested.sprite.center( marker ) );
			suggested.sprite.parent.addToFront( marker );
		}

		void finish(){
			finished = true;
			if (pending == this) pending = null;
			if (marker != null){ marker.killAndErase(); marker.destroy(); marker = null; }
		}

		@Override
		public void onSelect( Integer cell ){
			finish();
			if (cell == null) return;
			Char e = Actor.findChar( cell );
			if (!validEnemy( Dungeon.hero, e )){
				GLog.w( "Pick an enemy you can see." );
				return;
			}
			xyz.gabriwar.warpedpixeldungeon.ui.QuickSlotButton.target( e );
			then.on( e );
		}

		@Override
		public String prompt(){ return prompt; }
	}

	/** asks for a visible enemy, with the nearest (or last aimed at) one already marked: press the skill again, or tap it */
	public static void pickEnemy( Skill owner, final String prompt, final Target then ){
		Pending p = new Pending( owner, prompt, then );
		p.suggested = autoTarget( Dungeon.hero );
		GameScene.selectCell( p );
		if (!p.finished){ pending = p; p.showMarker(); }
	}

	public static void pickEnemy( final String prompt, final Target then ){
		pickEnemy( null, prompt, then );
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
