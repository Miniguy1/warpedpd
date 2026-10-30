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

package xyz.gabriwar.warpedpixeldungeon;

import xyz.gabriwar.warpedpixeldungeon.actors.Char;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Blacksmith;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.CagedKobold;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Ghost;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Imp;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.Wandmaker;
import xyz.gabriwar.warpedpixeldungeon.items.Amulet;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Generator;
import xyz.gabriwar.warpedpixeldungeon.items.Gold;
import xyz.gabriwar.warpedpixeldungeon.levels.rooms.secret.SecretRoom;
import xyz.gabriwar.warpedpixeldungeon.levels.rooms.special.SpecialRoom;
import xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents;
import xyz.gabriwar.warpedpixeldungeon.messages.Messages;
import xyz.gabriwar.warpedpixeldungeon.utils.GLog;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The cycles: once the hero has climbed out of the dungeon with the amulet and the curse is lifted
 * (Statistics.ascended), the dungeon's door offers a new cycle. A new cycle wipes the dungeon (its floors,
 * its quests, its limited drops, the amulet goes back to its heart) and makes every enemy in it stronger:
 * cycle 1 is twice as strong, cycle 2 three times, and so on. Loot and boss hoards grow with the cycle, and
 * on the climb back up the floors hold events (levels/ClimbEvents). The hero, the town, the homestead, the
 * world and every other place (the mines, the plaza zones) are untouched.
 *
 * Cycle 0 is the first run through the dungeon, as it always was.
 */
public final class Cycle {

	private Cycle(){}

	/** the hero's current cycle: 0 until the first reset */
	public static int number = 0;

	/** the dungeon proper and the branches hanging off it (not the mines, the zones or the surface) */
	public static boolean affects( int branch, int depth ){
		return (branch == 0 && depth >= 1 && depth <= 26) || (branch >= 1 && branch <= 5);
	}

	//What the main dungeon's own enemies come to at a depth d: health 6 + 3.6 d, damage likewise,
	//accuracy 9 + 0.9 d, and the experience they pay 1 + 0.75 d. A cycle's enemies are brought up to
	//what the enemies of a deeper floor are, whatever they are native to.
	static float typicalHealth( float d ){ return 6f + 3.6f * d; }
	static float typicalSkill( float d ){ return 9f + 0.9f * d; }
	static float typicalExp( float d ){ return 1f + 0.75f * d; }

	/** the depth whose enemies a floor's enemies match in a cycle. The first floor of the first new cycle
	 *  matches the bottom floor of the last run (24), and every floor after it is 0.8 deeper, on into the
	 *  next cycle: a cycle's first floor is where the last cycle's last floor left off. They never fall back
	 *  when the dungeon resets, and they grow slowly. */
	public static float effectiveDepth( int cycle, int depth ){
		return 24f + 0.8f * ((Math.max( 1, cycle ) - 1) * 25 + (Math.max( 1, depth ) - 1));
	}

	/** the health / damage factor of an enemy on this floor in this cycle: its native strength brought up to that of the matching depth */
	public static float powerAt( int cycle, int branch, int depth ){
		if (cycle <= 0 || !affects( branch, depth )) return 1f;
		return typicalHealth( effectiveDepth( cycle, depth ) ) / typicalHealth( Math.max( 1, depth ) );
	}

	/** accuracy / evasion, the same way */
	public static float skillAt( int cycle, int branch, int depth ){
		if (cycle <= 0 || !affects( branch, depth )) return 1f;
		return typicalSkill( effectiveDepth( cycle, depth ) ) / typicalSkill( Math.max( 1, depth ) );
	}

	/** the experience an enemy pays, grown the same way */
	public static float expFactorAt( int cycle, int branch, int depth ){
		if (cycle <= 0 || !affects( branch, depth )) return 1f;
		return typicalExp( effectiveDepth( cycle, depth ) ) / typicalExp( Math.max( 1, depth ) );
	}

	public static float expFactor( Char ch ){
		return scaled( ch ) ? expFactorAt( number, Dungeon.branch, Dungeon.depth ) : 1f;
	}

	private static boolean scaled( Char ch ){
		return ch instanceof Mob && ch.alignment == Char.Alignment.ENEMY;
	}

	/** the cycle's factor on an enemy (and a blood moon's, on top) */
	public static float power( Char ch ){
		if (!scaled( ch )) return 1f;
		float p = powerAt( number, Dungeon.branch, Dungeon.depth );
		if (Dungeon.level != null && Dungeon.level.climbEvent == ClimbEvents.BLOOD_MOON && affects( Dungeon.branch, Dungeon.depth )){
			p *= ClimbEvents.BLOOD_MOON_POWER;
		}
		if (number > 0 && affects( Dungeon.branch, Dungeon.depth )){
			p *= Pacts.hardened();
			//the bosses are tougher than the rest
			if (Char.hasProp( ch, Char.Property.BOSS ) && !Char.hasProp( ch, Char.Property.MINIBOSS )) p *= bossBoost( number );
		}
		return p;
	}

	public static float skill( Char ch ){
		return scaled( ch ) ? skillAt( number, Dungeon.branch, Dungeon.depth ) : 1f;
	}

	/** the rating of a floor's loot in a cycle, from the same ramp as its enemies: 7 (tier 7, what the last
	 *  run's climb paid) where the enemies match the old bottom floor, a tier more for every 16 depths of
	 *  strength after it. It carries on across cycles, so a new cycle never pays less than the last ended on. */
	public static float lootRating( int cycle, int depth ){
		//the first run and the first new cycle pay tier 6 (and the plain pool, tier 5); each later cycle starts a
		//tier higher and climbs through it over the dungeon's floors: cycle 2 is 6 to 7, cycle 3 is 7 to 8, cycle 4 is 8 to 9
		float base = cycle <= 1 ? 6f : Math.min( 9f, (cycle + 4) + 0.9f * (Math.max( 1, Math.min( 26, depth ) ) - 1) / 25f );
		return Math.min( 9f, base + (cycle > 0 ? Pacts.lootBonus() : 0f) );
	}

	/** how much tougher a boss is than the cycle's ordinary enemies on its floor */
	public static float bossBoost( int cycle ){
		return Math.min( 3f, 1.5f + 0.25f * (cycle - 1) );
	}

	/** the pact of swift foes */
	public static float enemySpeed( Char ch ){
		return scaled( ch ) && number > 0 && affects( Dungeon.branch, Dungeon.depth ) ? Pacts.enemySpeed() : 1f;
	}

	/** is the door's new-cycle offer open? Only once the climb is done and the curse lifted. */
	public static boolean offersReset(){
		return Statistics.ascended;
	}

	// ------------------------------------------------------------------ the reset

	/** the one-of-a-kind items that are never found twice: the ancient keys that open the bonus stages and the bags.
	 *  The hero keeps them from the last run, so a new cycle does not drop them again (the potions, scrolls and
	 *  other limited drops all do come back). */
	public static final Dungeon.LimitedDrops[] ONCE_ONLY = {
			Dungeon.LimitedDrops.SEWER_KEY, Dungeon.LimitedDrops.PRISON_KEY, Dungeon.LimitedDrops.CAVES_KEY,
			Dungeon.LimitedDrops.CITY_KEY, Dungeon.LimitedDrops.HALLS_KEY, Dungeon.LimitedDrops.TENGU_KEY,
			Dungeon.LimitedDrops.KEY_RING, Dungeon.LimitedDrops.VELVET_POUCH, Dungeon.LimitedDrops.SCROLL_HOLDER,
			Dungeon.LimitedDrops.POTION_BANDOLIER, Dungeon.LimitedDrops.MAGICAL_HOLSTER, Dungeon.LimitedDrops.ANKH_CHAIN,
			Dungeon.LimitedDrops.FOOD_POUCH, Dungeon.LimitedDrops.ARCHERS_QUIVER
	};

	/** Starts the next cycle: the dungeon forgets everything, the hero keeps everything else. */
	public static void begin( Hero hero ){
		number++;
		resetDungeonState( hero );
		deleteDungeonFiles();
	}

	/** the state half of the reset (tested without any files) */
	public static void resetDungeonState( Hero hero ){
		//the amulet goes back to the dungeon's heart
		if (hero != null && hero.belongings != null){
			Amulet amulet = hero.belongings.getItem( Amulet.class );
			if (amulet != null) amulet.detachAll( hero.belongings.backpack );
		}
		Statistics.ascended = false;
		Statistics.amuletObtained = false;
		Statistics.highestAscent = 0;
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();

		//the dungeon's floors and its branches are unvisited again (the town, the mines and the zones stay)
		ArrayList<Integer> keep = new ArrayList<>();
		for (int g : Dungeon.generatedLevels){
			int branch = g / 1000, depth = g % 1000;
			if (!affects( branch, depth )) keep.add( g );
		}
		Dungeon.generatedLevels.clear();
		Dungeon.generatedLevels.addAll( keep );
		Dungeon.droppedItems = new SparseArray<>();
		Dungeon.fallenMobs = new SparseArray<>();
		Dungeon.chapters = new HashSet<>();
		//everything limited comes back, except what was found once and is kept for good
		boolean[] kept = new boolean[ONCE_ONLY.length];
		for (int i = 0; i < ONCE_ONLY.length; i++) kept[i] = ONCE_ONLY[i].dropped();
		Dungeon.LimitedDrops.reset();
		for (int i = 0; i < ONCE_ONLY.length; i++) if (kept[i]) ONCE_ONLY[i].drop();

		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();
		CagedKobold.Quest.reset();

		Dungeon.templeCompleted = false;
		Dungeon.shadowyogkilled = false;
		Dungeon.crabkingkilled = false;
		Dungeon.banditkingkilled = false;
		Dungeon.skeletonkingkilled = false;
		Dungeon.tengukilled = false;
		Dungeon.tengudenkilled = false;
		Dungeon.gnollspawned = false;
		Dungeon.skeletonspawned = false;
		Dungeon.goldthiefspawned = false;
		Dungeon.orbofzotdropped = false;

		//the dungeon's rooms and item decks start afresh, and differently from the last cycle
		Random.pushGenerator( Dungeon.seed + 1 + 7919L * number );
		try {
			SpecialRoom.initForRun();
			SecretRoom.initForRun();
			xyz.gabriwar.warpedpixeldungeon.levels.rooms.warped.WarpedRooms.initForRun();
			Generator.fullReset();
		} finally {
			Random.popGenerator();
		}
	}

	/** the saved floors of the dungeon proper and its branches */
	public static void deleteDungeonFiles(){
		int slot = GamesInProgress.curSlot;
		if (slot <= 0) return;
		try {
			for (int d = 1; d <= 26; d++) FileUtils.deleteFile( GamesInProgress.depthFile( slot, d, 0 ) );
			for (int b = 1; b <= 5; b++){
				for (int d = 1; d <= 30; d++) FileUtils.deleteFile( GamesInProgress.depthFile( slot, d, b ) );
			}
		} catch (Exception e){
			//a floor file that is not there is the point
		}
	}

	// ------------------------------------------------------------------ the rewards

	/** a bounty's hunter or a boss of a cycle has fallen: its hoard */
	public static void onDeath( Mob mob ){
		if (mob == null || Dungeon.level == null || Dungeon.hero == null) return;
		BountyBoard.onKill( mob );
		boolean bounty = mob.buff( Bounty.class ) != null;
		boolean boss = Char.hasProp( mob, Char.Property.BOSS ) && !Char.hasProp( mob, Char.Property.MINIBOSS );
		//a vengeful boss does not fall alone
		xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation mut = mob.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.class );
		if (mut != null && mut.has( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.VENGEFUL )){
			xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.summon( mob, 3 );
			GLog.n( "The " + mob.name() + " does not fall alone: its death calls up more." );
		}
		if (!bounty && !(boss && number > 0 && affects( Dungeon.branch, Dungeon.depth ))) return;
		xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty bb0 = mob.buff( Bounty.class );
		float rating = lootRating( number, Dungeon.depth ) + (bounty ? 0.3f + 0.15f * ((bb0 == null ? 1 : bb0.boost) - 1) : 0.2f);
		xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty bb = mob.buff( Bounty.class );
		int boost = bb == null ? 1 : bb.boost;
		int items = bounty ? 3 + 2 * (boost - 1) : 2;
		for (int i = 0; i < items; i++){
			Dungeon.level.drop( EndgameLoot.rollRated( rating ), mob.pos );
		}
		if (Dungeon.hero.heroClass != null){
			Dungeon.level.drop( EndgameLoot.rollClassSet( Dungeon.hero.heroClass, EndgameLoot.tierOfRating( rating ) ), mob.pos );
		}
		//shards for reforging (items/CycleShard)
		Dungeon.level.drop( new xyz.gabriwar.warpedpixeldungeon.items.CycleShard().quantity( (bounty ? 3 : 2) + number + 3 * (boost - 1) + Pacts.shardBonus() ), mob.pos );
		if (bounty){
			int gold = Random.NormalIntRange( 150, 250 ) * Math.max( 1, number ) + 30 * Dungeon.depth;
			Dungeon.level.drop( new Gold( gold ), mob.pos );
			GLog.p( "The bounty is paid: " + gold + " gold and the hunter's trophies." );
		}
	}
}
