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

import xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.EndTalents;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Hero;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass;
import xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent;
import xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat;
import xyz.gabriwar.warpedpixeldungeon.items.Amulet;
import xyz.gabriwar.warpedpixeldungeon.items.EndgameLoot;
import xyz.gabriwar.warpedpixeldungeon.items.Heap;
import xyz.gabriwar.warpedpixeldungeon.items.Item;
import xyz.gabriwar.warpedpixeldungeon.levels.Level;
import xyz.gabriwar.warpedpixeldungeon.levels.rooms.WarpedRoomsTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/** The custom content, exercised for real: every new item's text, the drop pool, every level 40/60
 *  talent, the ascent rebuild rules, and real floors built the way the game builds them. */
public class ModContentTest {

	private static final String P = "xyz.gabriwar.warpedpixeldungeon.";

	private static final String[] ITEMS = {
			"items.wands.WandOfCombustion",
			"items.wands.WandOfFreeze",
			"items.wands.WandOfSiphon",
			"items.wands.WandOfEcho",
			"items.weapon.melee.HollowwoodStaff",
			"items.weapon.melee.IronboundStaff",
			"items.weapon.melee.WyrmboneStaff",
			"items.armor.RobeOfFire",
			"items.armor.BleedingArmor",
			"items.armor.RobeOfShattering",
			"items.armor.DragonscaleMail",
			"items.armor.Thornmail",
			"items.armor.Duskplate",
			"items.weapon.melee.Dawnbreaker",
			"items.weapon.melee.Voidreaver",
			"items.weapon.melee.BloodmoonAxe",
			"items.weapon.melee.Stormbreaker",
			"items.weapon.melee.ObsidianGreataxe",
			"items.weapon.melee.WyrmfangGlaive",
			"items.weapon.melee.Frostbrand",
			"items.weapon.melee.Thunderlance",
			"items.weapon.melee.Gravesplitter",
			"items.weapon.melee.Emberfang",
			"items.rings.RingOfVampirism",
			"items.rings.RingOfRetribution",
			"items.rings.RingOfBulwark",
			"items.rings.RingOfCruelty",
			"items.AmuletOfRenewal",
			"items.AmuletOfAegis",
			"items.trinkets.WarlordsMedal",
			"items.trinkets.SagesIdol",
			"items.trinkets.StoneheartPendant",
			"items.trinkets.HuntersFang",
			"items.trinkets.WayfarersBoots",
			"items.trinkets.EmberCharm" };

	private static final String[] TALENTS = {
			"IRONCLAD",
			"BLOODIED_RESOLVE",
			"WARLORDS_FURY",
			"UNBROKEN",
			"OVERCHARGE",
			"ARCANE_RESERVE",
			"ARCHMAGES_SURGE",
			"SPELLWEAVER",
			"SHADE_STRIDE",
			"KNIFE_IN_THE_DARK",
			"DEATHS_HAND",
			"VANISHING_ACT",
			"KEEN_EYE",
			"PATHFINDER",
			"APEX_PREDATOR",
			"BRIAR_HIDE",
			"RIPOSTE_MASTERY",
			"FOOTWORK",
			"BLADE_DANCER",
			"PERFECT_FORM",
			"WARDED_FAITH",
			"MERCIFUL_LIGHT",
			"AVATARS_GRACE",
			"LAST_RITES" };

	@BeforeClass
	public static void boot(){
		WarpedRoomsTest.boot();
		com.watabou.utils.FileUtils.setDefaultFileProperties( com.badlogic.gdx.Files.FileType.Absolute,
				new java.io.File( "build/test-saves" ).getAbsolutePath() + "/" );
		Dungeon.seed = 0xC0FFEEL;
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		Dungeon.init();
	}

	private static boolean missing( String s ){
		return s == null || s.isEmpty() || s.contains( "NO TEXT FOUND" );
	}

	private static void clearTalents( Hero h ){
		for (String t : TALENTS) {
			for (LinkedHashMap<Talent, Integer> tier : h.talents) {
				if (tier.containsKey( Talent.valueOf( t ) )) tier.put( Talent.valueOf( t ), 0 );
			}
		}
	}

	private static void give( Hero h, Talent t, int points ){
		//any tier map will do: pointsInTalent reads them all
		h.talents.get( 4 ).put( t, points );
	}

	@Test
	public void everyItemHasItsTextAndBuilds() throws Exception {
		for (String name : ITEMS) {
			Class<?> c = Class.forName( P + name );
			Item it = (Item)c.getDeclaredConstructor().newInstance();
			assertFalse( name + ": no name", missing( it.name() ) );
			assertFalse( name + ": no info", missing( it.info() ) );
			assertTrue( name + ": no image", it.image >= 0 );
		}
	}

	@Test
	public void theDropPoolHoldsEveryItemAndNeverCurses() {
		Set<String> seen = new HashSet<>();
		for (int i = 0; i < 12000; i++) {
			Item it = EndgameLoot.roll();
			assertNotNull( it );
			assertFalse( "cursed: " + it.getClass().getSimpleName(), it.cursed );
			seen.add( it.getClass().getName() );
		}
		for (String name : ITEMS) {
			//the pool holds every item that has a place in it
			assertTrue( name + " never drops", seen.contains( P + name ) );
		}
		assertEquals( "the pool and the list disagree", ITEMS.length, seen.size() );
	}

	@Test
	public void everyLevel40And60TalentIsDefined() {
		for (String t : TALENTS) {
			Talent talent = Talent.valueOf( t );
			assertFalse( t + ": no title", missing( talent.title() ) );
			assertFalse( t + ": no description", missing( talent.desc( false ) ) );
			assertEquals( t, 3, talent.maxPoints() );
		}
		for (HeroClass cls : HeroClass.values()) {
			ArrayList<LinkedHashMap<Talent, Integer>> list = new ArrayList<>();
			Talent.initClassTalents( cls, list );
			assertEquals( cls + ": tiers", 6, list.size() );
			assertEquals( cls + ": level 40", 2, list.get( 4 ).size() );
			assertEquals( cls + ": level 60", 2, list.get( 5 ).size() );
		}
		assertEquals( 40, Talent.tierLevelThresholds[5] - 1 );
		assertEquals( 60, Talent.tierLevelThresholds[6] - 1 );
	}

	@Test
	public void theTalentsDoWhatTheySay() {
		Hero h = Dungeon.hero;
		clearTalents( h );
		h.HT = 100; h.HP = 100;

		give( h, Talent.IRONCLAD, 3 );
		assertEquals( "Ironclad", 88, EndTalents.beforeDamage( h, 100, null ) );
		clearTalents( h );

		give( h, Talent.SHADE_STRIDE, 2 );
		assertEquals( 1.12f, EndTalents.evasionMultiplier( h ), 0.001f );
		clearTalents( h );
		give( h, Talent.KEEN_EYE, 3 );
		assertEquals( 1.18f, EndTalents.accuracyMultiplier( h ), 0.001f );
		clearTalents( h );
		give( h, Talent.PATHFINDER, 3 );
		assertEquals( 1.12f, EndTalents.speedMultiplier( h ), 0.001f );
		clearTalents( h );
		give( h, Talent.OVERCHARGE, 3 );
		assertEquals( 1.24f, EndTalents.wandDamageMultiplier(), 0.001f );
		clearTalents( h );
		give( h, Talent.ARCANE_RESERVE, 2 );
		assertEquals( 1.24f, EndTalents.wandRechargeMultiplier( h ), 0.001f );
		clearTalents( h );

		Rat rat = new Rat();
		give( h, Talent.APEX_PREDATOR, 2 );
		assertEquals( "Apex Predator hits a full-health enemy", 14, EndTalents.melee( h, rat, 10 ) );
		rat.HP = rat.HT - 1;
		assertEquals( "but not a wounded one", 10, EndTalents.melee( h, rat, 10 ) );
		clearTalents( h );

		give( h, Talent.DEATHS_HAND, 1 );
		rat.HP = 1;
		assertEquals( "Death's Hand on a dying enemy", 12, EndTalents.melee( h, rat, 10 ) );
		clearTalents( h );

		//Unbroken: a killing blow leaves one health, once
		give( h, Talent.UNBROKEN, 3 );
		h.HP = 10;
		int left = EndTalents.beforeDamage( h, 500, null );
		assertTrue( "Unbroken lets you live", left < h.HP + h.shielding() );
		assertTrue( "and heals you first", h.HP > 10 );
		assertNotNull( h.buff( EndTalents.UnbrokenCooldown.class ) );
		int again = EndTalents.beforeDamage( h, 5000, null );
		assertTrue( "but only once", again >= h.HP + h.shielding() );
		Buff.detach( h, EndTalents.UnbrokenCooldown.class );
		clearTalents( h );
	}

	@Test
	public void theAscentRulesHold() {
		assertTrue( Dungeon.ascentRebuildable( 24, 0 ) );
		assertTrue( Dungeon.ascentRebuildable( 1, 0 ) );
		assertFalse( "boss floor", Dungeon.ascentRebuildable( 25, 0 ) );
		assertFalse( "boss floor", Dungeon.ascentRebuildable( 10, 0 ) );
		assertFalse( "the last floor", Dungeon.ascentRebuildable( 26, 0 ) );
		assertFalse( "a branch", Dungeon.ascentRebuildable( 7, 1 ) );
		assertFalse( Dungeon.ascentRebuildable( 0, 0 ) );

		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();
		long s0 = Dungeon.seedForDepth( 7, 0 );
		Dungeon.rebuildForAscent( 7 );
		long s1 = Dungeon.seedForDepth( 7, 0 );
		assertNotEquals( "a rebuilt floor needs a new seed", s0, s1 );
		Dungeon.rebuildForAscent( 7 );
		assertNotEquals( "and another new one each time", s1, Dungeon.seedForDepth( 7, 0 ) );
		assertEquals( "other floors keep theirs", Dungeon.seedForDepth( 8, 0 ), Dungeon.seedForDepth( 8, 0 ) );
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();

		//without the cursed amulet nothing is rebuilt
		assertFalse( Dungeon.shouldRebuildOnAscent( 7, 0 ) );
		//with it: once per floor per carry
		Buff.affect( Dungeon.hero, AscensionChallenge.class );
		Dungeon.hero.belongings.backpack.items.add( new Amulet() );
		assertTrue( Dungeon.ascendingWithCharm() );
		assertTrue( Dungeon.shouldRebuildOnAscent( 7, 0 ) );
		Dungeon.rebuildForAscent( 7 );
		assertFalse( "not twice in one carry", Dungeon.shouldRebuildOnAscent( 7, 0 ) );
		assertTrue( "but the next floor is", Dungeon.shouldRebuildOnAscent( 6, 0 ) );
		Buff.detach( Dungeon.hero, AscensionChallenge.class );
		Dungeon.hero.belongings.getItem( Amulet.class ).detachAll( Dungeon.hero.belongings.backpack );
		assertFalse( Dungeon.shouldRebuildOnAscent( 5, 0 ) );
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();
	}

	private static int newItems( Level level ) {
		Set<String> mine = new HashSet<>();
		for (String name : ITEMS) mine.add( P + name );
		int n = 0;
		for (Heap heap : level.heaps.valueList()) {
			for (Item item : heap.items) if (mine.contains( item.getClass().getName() )) n++;
		}
		return n;
	}

	@Test
	public void rebuiltFloorsBuildAndHoldTheNewLoot() {
		int total = 0, floors = 0;
		for (int depth = 1; depth <= 24; depth++) {
			if (!Dungeon.ascentRebuildable( depth, 0 )) continue;
			Dungeon.ascentRebuilds.clear();
			Dungeon.generatedLevels.clear();
			Dungeon.branch = 0;
			Dungeon.depth = depth;
			Dungeon.rebuildForAscent( depth );
			Level level = Dungeon.newLevel();
			assertNotNull( "depth " + depth, level );
			total += newItems( level );
			floors++;
		}
		Dungeon.ascentRebuilds.clear();
		Dungeon.generatedLevels.clear();
		assertTrue( "no loot at all on " + floors + " rebuilt floors", total >= 3 );
	}

	@Test
	public void thePlazaZonesBuildEndToEnd() {
		int lootOnFloors = 0, lootInHoards = 0;
		for (int z = 0; z < xyz.gabriwar.warpedpixeldungeon.levels.Delves.ZONE_NAMES.length; z++) {
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
			Dungeon.generatedLevels.clear();
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.Entry e = xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( z );
			String what = "zone " + z;
			assertTrue( what, xyz.gabriwar.warpedpixeldungeon.levels.Delves.isZone( e ) );
			assertEquals( what, xyz.gabriwar.warpedpixeldungeon.levels.Delves.ZONE_NAMES[z], xyz.gabriwar.warpedpixeldungeon.levels.Delves.name( e ) );
			assertSame( "opening it twice is the same zone", e, xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( z ) );
			assertTrue( what + ": branch", e.branch >= xyz.gabriwar.warpedpixeldungeon.levels.Delves.BRANCH_BASE );
			assertEquals( what + ": floors", xyz.gabriwar.warpedpixeldungeon.levels.Delves.ZONE_FLOORS[z], e.floors );
			int[] out = xyz.gabriwar.warpedpixeldungeon.levels.Delves.stairs( e );
			int town = xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_MINE_GATE - 32;
			assertEquals( what + ": leaves by the plaza", xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.townWorldX( town ), out[0] );
			assertEquals( what, xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.townWorldY( town ), out[1] );

			Dungeon.branch = e.branch;
			for (int f = 0; f < e.floors; f++) {
				Dungeon.depth = e.depthOf( f );
				Level level = Dungeon.newLevel();
				String fl = what + " floor " + f + " depth " + Dungeon.depth;
				assertFalse( fl, level instanceof xyz.gabriwar.warpedpixeldungeon.levels.DeadEndLevel );
				assertNotNull( fl + ": no way in", level.getTransition( xyz.gabriwar.warpedpixeldungeon.levels.features.LevelTransition.Type.REGULAR_ENTRANCE ) );
				lootOnFloors += newItems( level );
				int guardians = 0;
				for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m.buff( xyz.gabriwar.warpedpixeldungeon.levels.Delves.Guardian.class ) != null) guardians++;
				assertEquals( fl + ": guardian", f == e.floors - 1 ? 1 : 0, guardians );
				if (f == e.floors - 1) {
					Dungeon.level = level;
					xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob g = null;
					for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m.buff( xyz.gabriwar.warpedpixeldungeon.levels.Delves.Guardian.class ) != null) g = m;
					xyz.gabriwar.warpedpixeldungeon.levels.Delves.onDeath( g );
					assertTrue( fl + ": cleared", e.cleared );
					Heap hoard = level.heaps.get( g.pos );
					assertNotNull( fl + ": no hoard", hoard );
					int mine = 0;
					Set<String> names = new HashSet<>();
					for (String n : ITEMS) names.add( P + n );
					for (Item it : hoard.items) if (names.contains( it.getClass().getName() )) mine++;
					assertTrue( fl + ": the hoard holds " + mine + " new items, wanted at least " + (2 + z), mine >= 2 + z );
					lootInHoards += mine;
					Dungeon.level = null;
				}
			}
			Dungeon.branch = 0;

			//a zone survives a save
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.storeInBundle( b );
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.restoreFromBundle( b );
			xyz.gabriwar.warpedpixeldungeon.levels.Delves.Entry back = xyz.gabriwar.warpedpixeldungeon.levels.Delves.byBranch( e.branch );
			assertNotNull( what + ": lost in a save", back );
			assertTrue( what, xyz.gabriwar.warpedpixeldungeon.levels.Delves.isZone( back ) );
			assertEquals( what, z, xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneId( back ) );
			assertTrue( what + ": cleared forgotten", back.cleared );
		}
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
		Dungeon.generatedLevels.clear();
		assertTrue( "the zones' floors held no new loot at all", lootOnFloors >= 1 );
	}

	@Test
	public void theDarkMinesHoldTheNewLoot() {
		int total = 0;
		long keep = Dungeon.seed;
		for (int k = 0; k < 30; k++) {
			Dungeon.seed = 0xABC000L + k * 7919L;
			Dungeon.generatedLevels.clear();
			Dungeon.branch = 0;
			Dungeon.depth = 57;
			Level level = Dungeon.newLevel();
			assertNotNull( level );
			total += newItems( level );
		}
		Dungeon.seed = keep;
		Dungeon.generatedLevels.clear();
		assertTrue( "no new items on 30 mine floors", total >= 3 );
	}
}
