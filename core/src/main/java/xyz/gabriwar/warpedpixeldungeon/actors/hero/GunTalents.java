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
import xyz.gabriwar.warpedpixeldungeon.actors.Actor;
import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.GunSkills;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Gun;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;

/**
 * What the Gunslinger's talents do. The combat hooks in CurrentSkills (damage, accuracy, hits, kills, incoming
 * damage) and in Gun (reload, shot speed, spare rounds) call in here, and every effect needs a firearm in hand.
 */
public final class GunTalents {

	private GunTalents(){}

	private static float lastKill = -100f, lastReload = -100f;
	private static int lastStandDepth = -1;

	private static int pts( Talent t ){
		Hero h = Dungeon.hero;
		return h == null ? 0 : h.pointsInTalent( t );
	}

	/** Ammo Belt: a meal gives 4 / 7 bullets */
	public static void onFood( Hero hero ){
		int p = hero.pointsInTalent( Talent.AMMO_BELT );
		if (p > 0){
			Dungeon.bullet += 1 + 3 * p;
			GLog.p( "You top up your ammo belt: " + (1 + 3 * p) + " bullets." );
		}
	}

	public static float damageMod( boolean ranged ){
		Gun g = GunSkills.gun();
		if (!ranged || g == null) return 1f;
		Hero h = Dungeon.hero;
		float m = 1f + 0.10f * pts( Talent.LEGEND_OF_THE_WEST );
		if (h.HP * 2 <= h.HT) m *= 1f + 0.10f * pts( Talent.DESPERADO );
		if (g.round() * 2 > g.maxRound()) m *= 1f + 0.08f * pts( Talent.COOL_BARREL );
		if (g.round() == 1) m *= 1f + 0.15f * pts( Talent.OVERCHARGE_ROUNDS );
		return m;
	}

	public static float accuracyMod(){
		return GunSkills.gun() == null ? 1f : 1f + 0.10f * pts( Talent.CALLED_SHOTS );
	}

	public static int onHit( Char enemy, int damage, boolean ranged ){
		Gun g = GunSkills.gun();
		Hero h = Dungeon.hero;
		if (!ranged || g == null || h == null || enemy == null) return damage;
		//Steady Hands: an enemy that has not noticed you
		if (enemy instanceof Mob && ((Mob) enemy).state != ((Mob) enemy).HUNTING){
			damage = Math.round( damage * (1f + 0.25f * pts( Talent.STEADY_HANDS )) );
		}
		//Dead to Rights: an enemy below a quarter of its health
		if (enemy.HP * 4 <= enemy.HT){
			damage = Math.round( damage * (1f + 0.20f * pts( Talent.DEAD_TO_RIGHTS )) );
		}
		//Pocket Rounds: a point-blank shot may load a round
		if (Dungeon.level != null && Dungeon.level.adjacent( h.pos, enemy.pos ) && g.round() < g.maxRound()
				&& Random.Float() < 0.15f * pts( Talent.POCKET_ROUNDS )){
			g.manualReload( 1, false );
		}
		//Bigger Bang: the shot splashes
		int bang = pts( Talent.BIGGER_BANG );
		if (bang > 0 && Dungeon.level != null){
			for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
				if (m != enemy && m.alignment == Char.Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( enemy.pos, m.pos ) == 1){
					m.damage( Math.max( 1, Math.round( damage * 0.10f * bang ) ), h );
					if (m.sprite != null) m.sprite.flash();
				}
			}
		}
		return damage;
	}

	public static void onKill( Mob mob, boolean ranged ){
		Hero h = Dungeon.hero;
		if (h == null || GunSkills.gun() == null) return;
		lastKill = Actor.now();
		//a Gunslinger turns up a few bullets now and then, talents or no
		if (h.heroClass == HeroClass.GUNSLINGER && Random.Float() < 0.12f) Dungeon.bullet += Random.IntRange( 1, 3 );
		int nerves = pts( Talent.STEEL_NERVES );
		if (nerves > 0) Buff.affect( h, Barrier.class ).incShield( 3 * nerves );
		int casings = pts( Talent.SPENT_CASINGS );
		if (casings > 0) Dungeon.bullet += casings;
		int bounty = pts( Talent.BLOOD_MONEY );
		if (bounty > 0) Dungeon.gold += Math.round( (5 + Dungeon.depth) * bounty * Random.Float( 0.8f, 1.2f ) );
	}

	/** Reload on the Run: for five turns after a kill, reloads are faster */
	public static float reloadFactor(){
		return Actor.now() - lastKill <= 5f ? 1f - 0.25f * pts( Talent.RELOAD_ON_THE_RUN ) : 1f;
	}

	/** Hair Trigger: shots take less time */
	public static float shotSpeed(){
		return 1f / (1f + 0.10f * pts( Talent.HAIR_TRIGGER ));
	}

	/** Powder Horn: a reload gives some of its bullets back */
	public static int refund( int used ){
		return Math.round( used * 0.25f * pts( Talent.POWDER_HORN ) );
	}

	public static void onReloaded(){
		lastReload = Actor.now();
	}

	/** Trigger Happy: a shot may not use a round */
	public static boolean sparesRound(){
		return Random.Float() < 0.05f * pts( Talent.TRIGGER_HAPPY )
				|| Random.Float() < xyz.gabriwar.warpedpixeldungeon.items.GearSets.of( Dungeon.hero ).spare;   //and the Gunslinger's Rig
	}

	/** Gunsmith's Eye: the firearm counts as higher level */
	public static int levelBonus(){
		return GunSkills.gun() == null ? 0 : pts( Talent.GUNSMITHS_EYE );
	}

	public static int incoming( int dmg, Object source ){
		Hero h = Dungeon.hero;
		Gun g = GunSkills.gun();
		if (h == null || g == null || dmg <= 0) return dmg;
		//Gunsmoke: three turns after a reload
		int smoke = pts( Talent.GUNSMOKE );
		if (smoke > 0 && Actor.now() - lastReload <= 3f) dmg = Math.round( dmg * (1f - (0.1f + 0.1f * smoke)) );
		dmg = Math.round( dmg * (1f - 0.05f * pts( Talent.SHRAPNEL_VEST )) );
		//Last Bullet: once a floor, a blow that would leave you under a quarter of your health
		int last = pts( Talent.LAST_BULLET );
		if (last > 0 && lastStandDepth != Dungeon.depth && h.HP - dmg <= h.HT / 4 && h.HP - dmg > 0){
			lastStandDepth = Dungeon.depth;
			g.quickReload();
			Buff.affect( h, Barrier.class ).incShield( 5 * last );
			GLog.p( "Last bullet!" );
		}
		return Math.max( 0, dmg );
	}
}
