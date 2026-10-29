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
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff;
import xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation;
import xyz.gabriwar.warpedpixeldungeon.items.Reforge;
import xyz.gabriwar.warpedpixeldungeon.items.SetTier;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotSame;
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

	private static final String[] CLASS_ITEMS = {
			"items.armor.WarlordsPlate",
			"items.weapon.melee.WarlordsGreatsword",
			"items.rings.WarlordsSignet",
			"items.WarlordsBanner",
			"items.WarlordsCrest",
			"items.armor.ArchmagesRobe",
			"items.weapon.melee.ArchmagesStaff",
			"items.rings.ArchmagesBand",
			"items.ArchmagesOrb",
			"items.ArchmagesSigil",
			"items.armor.ShadowbladesCloak",
			"items.weapon.melee.ShadowbladesDirk",
			"items.rings.ShadowbladesRing",
			"items.ShadowbladesMask",
			"items.ShadowbladesToken",
			"items.armor.WardensHide",
			"items.weapon.melee.WardensGlaive",
			"items.rings.WardensRing",
			"items.WardensHorn",
			"items.WardensTotem",
			"items.armor.DuelistsCoat",
			"items.weapon.melee.DuelistsRapier",
			"items.rings.DuelistsSignet",
			"items.DuelistsGlove",
			"items.DuelistsMedal",
			"items.armor.SaintsVestment",
			"items.weapon.melee.SaintsMace",
			"items.rings.SaintsHalo",
			"items.SaintsChalice",
			"items.SaintsRelic" };

	private static final String[] YOG_ITEMS = {
			"items.armor.YogForgedPlate",
			"items.weapon.melee.YogForgedBlade",
			"items.rings.YogForgedBand",
			"items.YogsEye",
			"items.YogsHeart",
			"items.YogsFang" };

	private static final String[] TIER7_ITEMS = {
			"items.armor.AscendedWarlordsPlate",
			"items.weapon.melee.AscendedWarlordsGreatsword",
			"items.rings.AscendedWarlordsSignet",
			"items.AscendedWarlordsBanner",
			"items.AscendedWarlordsCrest",
			"items.armor.AscendedArchmagesRobe",
			"items.weapon.melee.AscendedArchmagesStaff",
			"items.rings.AscendedArchmagesBand",
			"items.AscendedArchmagesOrb",
			"items.AscendedArchmagesSigil",
			"items.armor.AscendedShadowbladesCloak",
			"items.weapon.melee.AscendedShadowbladesDirk",
			"items.rings.AscendedShadowbladesRing",
			"items.AscendedShadowbladesMask",
			"items.AscendedShadowbladesToken",
			"items.armor.AscendedWardensHide",
			"items.weapon.melee.AscendedWardensGlaive",
			"items.rings.AscendedWardensRing",
			"items.AscendedWardensHorn",
			"items.AscendedWardensTotem",
			"items.armor.AscendedDuelistsCoat",
			"items.weapon.melee.AscendedDuelistsRapier",
			"items.rings.AscendedDuelistsSignet",
			"items.AscendedDuelistsGlove",
			"items.AscendedDuelistsMedal",
			"items.armor.AscendedSaintsVestment",
			"items.weapon.melee.AscendedSaintsMace",
			"items.rings.AscendedSaintsHalo",
			"items.AscendedSaintsChalice",
			"items.AscendedSaintsRelic" };

	private static final String[] TIER8_ITEMS = {
			"items.armor.MythicWarlordsPlate",
			"items.weapon.melee.MythicWarlordsGreatsword",
			"items.rings.MythicWarlordsSignet",
			"items.MythicWarlordsBanner",
			"items.MythicWarlordsCrest",
			"items.armor.MythicArchmagesRobe",
			"items.weapon.melee.MythicArchmagesStaff",
			"items.rings.MythicArchmagesBand",
			"items.MythicArchmagesOrb",
			"items.MythicArchmagesSigil",
			"items.armor.MythicShadowbladesCloak",
			"items.weapon.melee.MythicShadowbladesDirk",
			"items.rings.MythicShadowbladesRing",
			"items.MythicShadowbladesMask",
			"items.MythicShadowbladesToken",
			"items.armor.MythicWardensHide",
			"items.weapon.melee.MythicWardensGlaive",
			"items.rings.MythicWardensRing",
			"items.MythicWardensHorn",
			"items.MythicWardensTotem",
			"items.armor.MythicDuelistsCoat",
			"items.weapon.melee.MythicDuelistsRapier",
			"items.rings.MythicDuelistsSignet",
			"items.MythicDuelistsGlove",
			"items.MythicDuelistsMedal",
			"items.armor.MythicSaintsVestment",
			"items.weapon.melee.MythicSaintsMace",
			"items.rings.MythicSaintsHalo",
			"items.MythicSaintsChalice",
			"items.MythicSaintsRelic" };

	private static final String[] TIER9_ITEMS = {
			"items.armor.GodforgedWarlordsPlate",
			"items.weapon.melee.GodforgedWarlordsGreatsword",
			"items.rings.GodforgedWarlordsSignet",
			"items.GodforgedWarlordsBanner",
			"items.GodforgedWarlordsCrest",
			"items.armor.GodforgedArchmagesRobe",
			"items.weapon.melee.GodforgedArchmagesStaff",
			"items.rings.GodforgedArchmagesBand",
			"items.GodforgedArchmagesOrb",
			"items.GodforgedArchmagesSigil",
			"items.armor.GodforgedShadowbladesCloak",
			"items.weapon.melee.GodforgedShadowbladesDirk",
			"items.rings.GodforgedShadowbladesRing",
			"items.GodforgedShadowbladesMask",
			"items.GodforgedShadowbladesToken",
			"items.armor.GodforgedWardensHide",
			"items.weapon.melee.GodforgedWardensGlaive",
			"items.rings.GodforgedWardensRing",
			"items.GodforgedWardensHorn",
			"items.GodforgedWardensTotem",
			"items.armor.GodforgedDuelistsCoat",
			"items.weapon.melee.GodforgedDuelistsRapier",
			"items.rings.GodforgedDuelistsSignet",
			"items.GodforgedDuelistsGlove",
			"items.GodforgedDuelistsMedal",
			"items.armor.GodforgedSaintsVestment",
			"items.weapon.melee.GodforgedSaintsMace",
			"items.rings.GodforgedSaintsHalo",
			"items.GodforgedSaintsChalice",
			"items.GodforgedSaintsRelic" };

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

	private static String[] all(){
		java.util.ArrayList<String> l = new java.util.ArrayList<>();
		l.addAll( java.util.Arrays.asList( ITEMS ) );
		l.addAll( java.util.Arrays.asList( CLASS_ITEMS ) );
		l.addAll( java.util.Arrays.asList( YOG_ITEMS ) );
		l.addAll( java.util.Arrays.asList( TIER7_ITEMS ) );
		l.addAll( java.util.Arrays.asList( TIER8_ITEMS ) );
		l.addAll( java.util.Arrays.asList( TIER9_ITEMS ) );
		return l.toArray( new String[0] );
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
		for (String name : all()) {
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
			Item it = EndgameLoot.rollGeneral();
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
		for (String name : all()) mine.add( P + name );
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
					for (String n : all()) names.add( P + n );
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
	public void gearSetsAreConsistentAndAddUp() throws Exception {
		Set<String> unassigned = new HashSet<>( java.util.Arrays.asList(
				"items.weapon.melee.HollowwoodStaff", "items.weapon.melee.IronboundStaff", "items.weapon.melee.WyrmboneStaff",
				"items.wands.WandOfSiphon", "items.rings.RingOfRetribution" ) );
		Set<Class<?>> all = new HashSet<>();
		for (int set = 0; set < xyz.gabriwar.warpedpixeldungeon.items.GearSets.NAMES.length; set++) {
			Class<?>[] pieces = xyz.gabriwar.warpedpixeldungeon.items.GearSets.piecesOf( set );
			assertEquals( "set " + set + " size", set == xyz.gabriwar.warpedpixeldungeon.items.GearSets.YOG ? 6 : 5, pieces.length );
			for (Class<?> c : pieces) {
				assertTrue( c.getSimpleName() + " is in two sets", all.add( c ) );
				assertEquals( xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( c ), set );
			}
		}
		for (String name : CLASS_ITEMS) assertTrue( name, xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( Class.forName( P + name ) ) >= 6 );
		for (String name : YOG_ITEMS) assertEquals( name, xyz.gabriwar.warpedpixeldungeon.items.GearSets.YOG, xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( Class.forName( P + name ) ) );
		for (String name : ITEMS) {
			Class<?> c = Class.forName( P + name );
			boolean in = xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( c ) >= 0;
			assertEquals( name + (in ? " is in a set but listed as not" : " belongs to no set"), !unassigned.contains( name ), in );
		}
		//the numbers
		int[] none = new int[6];
		xyz.gabriwar.warpedpixeldungeon.items.GearSets.Effects e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( none );
		assertEquals( 1f, e.damage, 0f ); assertEquals( 1f, e.taken, 0f ); assertEquals( 0, e.regenEvery );
		assertEquals( "a lone piece does nothing", 1f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 1, 1, 1, 1, 1, 1 } ).damage, 0f );
		assertEquals( 1.08f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 2, 0, 0, 0, 0, 0 } ).damage, 0.0001f );
		assertEquals( 1.32f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 5, 0, 0, 0, 0, 0 } ).damage, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 3, 0, 2, 0, 0 } );
		assertEquals( "frost 2 tiers + stone 1 tier", 0.88f, e.taken, 0.0001f );
		assertEquals( 1.08f, e.evasion, 0.0001f );
		assertEquals( 0.04f, e.thorns, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 4, 0, 4, 2 } );
		assertEquals( 0.09f, e.lifesteal, 0.0001f );
		assertEquals( 1.15f, e.accuracy, 0.0001f );
		assertEquals( 1.09f, e.speed, 0.0001f );
		assertEquals( 1.08f, e.exp, 0.0001f );
		assertEquals( 26, e.regenEvery );
		assertEquals( "never past the full set", xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 5, 0, 0, 0, 0, 0 } ).damage,
				xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 9, 0, 0, 0, 0, 0 } ).damage, 0f );
		//item descriptions carry their set
		assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dawnbreaker().desc().contains( "Emberwrought" ) );
		assertFalse( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.HollowwoodStaff().desc().contains( "Set:" ) );
	}

	@Test
	public void classSetsDropForTheirOwnClassOnly() throws Exception {
		Hero h = Dungeon.hero;
		HeroClass keep = h.heroClass;
		Set<String> allClassPieces = new HashSet<>();
		for (String n : CLASS_ITEMS) allClassPieces.add( P + n );
		for (String n : YOG_ITEMS) allClassPieces.add( P + n );
		try {
			HeroClass[] order = { HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE, HeroClass.HUNTRESS, HeroClass.DUELIST, HeroClass.CLERIC };
			for (int i = 0; i < order.length; i++) {
				h.heroClass = order[i];
				Set<Class<?>> want = new HashSet<>( java.util.Arrays.asList( xyz.gabriwar.warpedpixeldungeon.items.GearSets.piecesOf( 6 + i ) ) );
				Set<Class<?>> seen = new HashSet<>();
				for (int k = 0; k < 4000; k++) {
					Item it = EndgameLoot.rollClassSet( order[i] );
					assertFalse( "cursed", it.cursed );
					seen.add( it.getClass() );
				}
				assertEquals( order[i] + ": its own five pieces, nothing else", want, seen );
				//the ordinary roll: some class pieces, never another class's and never Yog-Forged
				int mine = 0;
				for (int k = 0; k < 6000; k++) {
					Item it = EndgameLoot.roll();
					if (allClassPieces.contains( it.getClass().getName() )) {
						assertTrue( order[i] + " was given " + it.getClass().getSimpleName(), want.contains( it.getClass() ) );
						mine++;
					}
				}
				assertTrue( order[i] + ": about 30% of rolls are its own set, got " + mine + "/6000", mine > 1400 && mine < 2200 );
			}
			Set<Class<?>> yog = new HashSet<>();
			int cy = Cycle.number;
			Cycle.number = 1;
			try {
				for (int k = 0; k < 3000; k++) yog.add( EndgameLoot.rollYog().getClass() );
			} finally {
				Cycle.number = cy;
			}
			assertEquals( new HashSet<>( java.util.Arrays.asList( xyz.gabriwar.warpedpixeldungeon.items.GearSets.piecesOf( xyz.gabriwar.warpedpixeldungeon.items.GearSets.YOG ) ) ), yog );
		} finally {
			h.heroClass = keep;
		}
	}

	@Test
	public void theClassAndYogSetBonusesAddUp() {
		xyz.gabriwar.warpedpixeldungeon.items.GearSets.Effects e;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 5 } );
		assertEquals( "warrior damage", 1.24f, e.damage, 0.0001f );
		assertEquals( 0.88f, e.taken, 0.0001f );
		assertEquals( 0.04f, e.lifesteal, 0.0001f );
		assertEquals( 0.25f, e.lowHp, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 4 } );
		assertEquals( "four pieces, no rage yet", 0f, e.lowHp, 0f );
		assertEquals( 0.04f, e.lifesteal, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 4 } );
		assertEquals( 1.24f, e.wandDamage, 0.0001f );
		assertEquals( 1.24f, e.wandRecharge, 0.0001f );
		assertEquals( 0.10f, e.freeZap, 0.0001f );
		assertEquals( 0f, e.zapBarrier, 0f );
		assertEquals( 0.05f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 5 } ).zapBarrier, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 5 } );
		assertEquals( 1.16f, e.evasion, 0.0001f );
		assertEquals( 0.48f, e.sneak, 0.0001f );
		assertEquals( 0.25f, e.vsWeak, 0.0001f );
		assertEquals( 1.10f, e.speed, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 } );
		assertEquals( 1.15f, e.accuracy, 0.0001f );
		assertEquals( 0.30f, e.vsFull, 0.0001f );
		assertEquals( 1.06f, e.speed, 0.0001f );
		assertEquals( 0f, e.thorns, 0f );
		assertEquals( 0.10f, xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 5 } ).thorns, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5 } );
		assertEquals( "duelist: 4 tiers of 4% plus the full-set 10%", 1.26f, e.damage, 0.0001f );
		assertEquals( 1.12f, e.accuracy, 0.0001f );
		assertEquals( 1.12f, e.evasion, 0.0001f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 } );
		assertEquals( 0.91f, e.taken, 0.0001f );
		assertEquals( 14, e.regenEvery );
		assertEquals( 20, e.barrierEvery );
		assertEquals( 0.03f, e.barrierPct, 0.0001f );
		assertEquals( 0f, e.lifesteal, 0f );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5 } );
		assertEquals( 0.05f, e.lifesteal, 0.0001f );
		assertEquals( 10, e.regenEvery );
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6 } );
		assertEquals( "yog damage", 1.30f, e.damage, 0.0001f );
		assertEquals( 0.80f, e.taken, 0.0001f );
		assertEquals( 0.075f, e.lifesteal, 0.0001f );
		assertEquals( 1.10f, e.speed, 0.0001f );
		assertEquals( 1.30f, e.exp, 0.0001f );
		assertTrue( "the capstone needs all six", e.lastStand );
		assertFalse( xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5 } ).lastStand );
		//two sets at once simply add (a short array is zeros after its end)
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( new int[]{ 2, 0, 0, 0, 0, 0, 2 } );
		assertEquals( 1.14f, e.damage, 0.0001f );
	}

	@Test
	public void aWholeClassSetIsCountedFromWhatIsWornAndCarried() {
		Hero h = Dungeon.hero;
		Object armor = h.belongings.armor, weapon = h.belongings.weapon, ring = h.belongings.ring;
		int packSize = h.belongings.backpack.items.size();
		try {
			h.belongings.armor = new xyz.gabriwar.warpedpixeldungeon.items.armor.WarlordsPlate();
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword();
			h.belongings.ring = new xyz.gabriwar.warpedpixeldungeon.items.rings.WarlordsSignet();
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.WarlordsBanner() );
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.WarlordsCrest() );
			assertEquals( "the full Warlord's Regalia", 5, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[6] );
			xyz.gabriwar.warpedpixeldungeon.items.GearSets.Effects e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.of( h );
			assertEquals( 1.24f, e.damage, 0.0001f );
			//the rage: +25% while at 35% health or less, and the heal that comes with four pieces
			h.HT = 100; h.HP = 100;
			assertEquals( "healthy: 24% more", 124, xyz.gabriwar.warpedpixeldungeon.items.GearSets.outgoing( h, 100 ) );
			h.HP = 30;
			int hurt = xyz.gabriwar.warpedpixeldungeon.items.GearSets.outgoing( h, 100 );
			assertEquals( "hurt: 24% and the rage 25% on top", Math.round( 100 * 1.24f * 1.25f ), hurt );
			//an unworn relic of another set does nothing for this one
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.ArchmagesOrb() );
			assertEquals( 5, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[6] );
			assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[7] );

			//the Mage's staff is a piece of its own set and of its wand's
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.ArchmagesStaff( new xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfFreeze() );
			int[] n = xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h );
			assertEquals( 2, n[7] );   //the staff and the orb
			assertEquals( 1, n[1] );   //the wand it holds
		} finally {
			h.belongings.armor = (xyz.gabriwar.warpedpixeldungeon.items.armor.Armor)armor;
			h.belongings.weapon = (xyz.gabriwar.warpedpixeldungeon.items.KindOfWeapon)weapon;
			h.belongings.ring = (xyz.gabriwar.warpedpixeldungeon.items.rings.Ring)ring;
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
	}

	@Test
	public void gearSetsReadWhatTheHeroWearsAndCarries() {
		Hero h = Dungeon.hero;
		assertNotNull( h );
		Object armor = h.belongings.armor, weapon = h.belongings.weapon, ring = h.belongings.ring;
		int packSize = h.belongings.backpack.items.size();
		try {
			h.belongings.armor = new xyz.gabriwar.warpedpixeldungeon.items.armor.BleedingArmor();
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.BloodmoonAxe();
			h.belongings.ring = new xyz.gabriwar.warpedpixeldungeon.items.rings.RingOfVampirism();
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.trinkets.WarlordsMedal() );
			int[] n = xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h );
			assertEquals( "armor, weapon, ring and a carried trinket", 4, n[2] );
			//the same trinket twice is still one piece
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.trinkets.WarlordsMedal() );
			assertEquals( 4, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[2] );
			assertEquals( "nothing else matches", 0, n[0] + n[1] + n[3] + n[4] + n[5] );

			//the Bloodbound heal: 3 tiers of 3% on a 100 blow
			h.HT = 100; h.HP = 50;
			assertEquals( "no damage bonus in this set", 100, xyz.gabriwar.warpedpixeldungeon.items.GearSets.outgoing( h, 100 ) );
			assertEquals( "healed 9", 59, h.HP );

			//a staff counts as the wand it holds
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.IronboundStaff( new xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfFreeze() );
			assertEquals( "the staff's wand is a Frostbound piece", 1, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[1] );
		} finally {
			h.belongings.armor = (xyz.gabriwar.warpedpixeldungeon.items.armor.Armor)armor;
			h.belongings.weapon = (xyz.gabriwar.warpedpixeldungeon.items.KindOfWeapon)weapon;
			h.belongings.ring = (xyz.gabriwar.warpedpixeldungeon.items.rings.Ring)ring;
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
	}

	@Test
	public void zonesEscalateTierByTier() {
		int id = 1;
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
		Dungeon.generatedLevels.clear();
		assertEquals( 0, xyz.gabriwar.warpedpixeldungeon.levels.Delves.nextTier( id ) );
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.Entry a = xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( id );
		assertEquals( 0, xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneTier( a ) );
		assertEquals( xyz.gabriwar.warpedpixeldungeon.levels.Delves.ZONE_NAMES[id], xyz.gabriwar.warpedpixeldungeon.levels.Delves.name( a ) );
		assertSame( "unfinished: the same run", a, xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( id ) );
		assertEquals( 0, xyz.gabriwar.warpedpixeldungeon.levels.Delves.nextTier( id ) );

		a.cleared = true;
		assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.levels.Delves.nextTier( id ) );
		assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneBlurb( id ).contains( "Tier II" ) );
		assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneClears( id ) );
		assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneBlurb( id ).contains( "Cleared 1 time." ) );
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.Entry b = xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( id );
		assertNotSame( "a cleared zone re-arms", a, b );
		assertTrue( "on a branch of its own", b.branch > a.branch );
		assertEquals( "a tier is four levels up", a.level + xyz.gabriwar.warpedpixeldungeon.levels.Delves.ZONE_TIER_STEP, b.level );
		assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneTier( b ) );
		assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.Delves.name( b ).endsWith( " II" ) );
		assertSame( b, xyz.gabriwar.warpedpixeldungeon.levels.Delves.openZone( id ) );

		//its floors are new floors, and its guardian pays out one more new item
		Dungeon.branch = b.branch;
		int mine = 0;
		for (int f = 0; f < b.floors; f++) {
			Dungeon.depth = b.depthOf( f );
			Level level = Dungeon.newLevel();
			assertFalse( xyz.gabriwar.warpedpixeldungeon.levels.DeadEndLevel.class.isInstance( level ) );
			if (f == b.floors - 1) {
				Dungeon.level = level;
				xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob g = null;
				for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m.buff( xyz.gabriwar.warpedpixeldungeon.levels.Delves.Guardian.class ) != null) g = m;
				xyz.gabriwar.warpedpixeldungeon.levels.Delves.onDeath( g );
				Set<String> names = new HashSet<>();
				for (String n : all()) names.add( P + n );
				for (Item it : level.heaps.get( g.pos ).items) if (names.contains( it.getClass().getName() )) mine++;
				Dungeon.level = null;
			}
		}
		Dungeon.branch = 0;
		assertTrue( "tier II hoard: 2 + zone 1 + tier 1 = 4 new items, got " + mine, mine >= 4 );
		assertEquals( 2, xyz.gabriwar.warpedpixeldungeon.levels.Delves.nextTier( id ) );

		//the tier survives a save
		com.watabou.utils.Bundle bundle = new com.watabou.utils.Bundle();
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.storeInBundle( bundle );
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.restoreFromBundle( bundle );
		assertEquals( 2, xyz.gabriwar.warpedpixeldungeon.levels.Delves.nextTier( id ) );
		assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.levels.Delves.zoneTier( xyz.gabriwar.warpedpixeldungeon.levels.Delves.byBranch( b.branch ) ) );
		xyz.gabriwar.warpedpixeldungeon.levels.Delves.reset();
		Dungeon.generatedLevels.clear();
	}

	@Test
	public void theAscentCountersSurviveASave() {
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();
		Dungeon.ascentRebuilds.put( 7, 2 );
		Dungeon.ascentRebuilds.put( 12, 1 );
		Dungeon.ascentSession.add( 7 );
		com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
		Dungeon.storeAscentState( b );
		Dungeon.ascentRebuilds.clear();
		Dungeon.ascentSession.clear();
		Dungeon.restoreAscentState( b );
		assertEquals( 2, Dungeon.rebuildCount( 7 ) );
		assertEquals( 1, Dungeon.rebuildCount( 12 ) );
		assertEquals( 0, Dungeon.rebuildCount( 3 ) );
		assertTrue( Dungeon.ascentSession.contains( 7 ) );
		assertEquals( 1, Dungeon.ascentSession.size() );
		//a save from before the feature restores to nothing
		Dungeon.restoreAscentState( new com.watabou.utils.Bundle() );
		assertEquals( 0, Dungeon.rebuildCount( 7 ) );
		assertTrue( Dungeon.ascentSession.isEmpty() );
	}

	@Test
	public void level40And60TalentsSurviveASave() {
		Hero h = Dungeon.hero;
		clearTalents( h );
		h.talents.get( 4 ).put( Talent.IRONCLAD, 3 );
		h.talents.get( 5 ).put( Talent.UNBROKEN, 2 );
		com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
		Talent.storeTalentsInBundle( b, h );
		clearTalents( h );
		h.talents.clear();
		Talent.restoreTalentsFromBundle( b, h );
		assertEquals( 6, h.talents.size() );
		assertEquals( 3, h.pointsInTalent( Talent.IRONCLAD ) );
		assertEquals( 2, h.pointsInTalent( Talent.UNBROKEN ) );
		assertEquals( 0, h.pointsInTalent( Talent.BLOODIED_RESOLVE ) );

		//a save from before the level 40/60 talents: they simply start empty
		com.watabou.utils.Bundle old = new com.watabou.utils.Bundle();
		for (int i = 1; i <= 4; i++) old.put( "talents_tier_" + i, new com.watabou.utils.Bundle() );
		h.talents.clear();
		Talent.restoreTalentsFromBundle( old, h );
		assertEquals( 6, h.talents.size() );
		assertEquals( 0, h.pointsInTalent( Talent.IRONCLAD ) );
		clearTalents( h );
	}

	@Test
	public void theTierSetsAreWiredAndStrongerThanTheirOriginals() throws Exception {
		String[][] lists = { TIER7_ITEMS, TIER8_ITEMS, TIER9_ITEMS };
		int[] firstSet = { xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T7, xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T8, xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T9 };
		for (int k = 0; k < 3; k++) {
			assertEquals( 30, lists[k].length );
			for (int i = 0; i < 30; i++) {
				Class<?> c = Class.forName( P + lists[k][i] );
				assertEquals( lists[k][i] + ": its set", firstSet[k] + i / 5, xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( c ) );
				assertEquals( lists[k][i] + ": its tier", 7 + k, ((xyz.gabriwar.warpedpixeldungeon.items.SetTier)c.getDeclaredConstructor().newInstance()).setTier() );
			}
		}
		//stronger than the tier 6 piece it is made from
		assertEquals( new xyz.gabriwar.warpedpixeldungeon.items.armor.WarlordsPlate().DRMax( 3 ) + 6, new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate().DRMax( 3 ) );
		assertEquals( Math.round( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword().max( 2 ) * 1.25f ), new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword().max( 2 ) );
		assertEquals( Math.round( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword().max( 2 ) * 1.8f ), new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.GodforgedWarlordsGreatsword().max( 2 ) );
		assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate().desc().contains( "Godforged Warlord's Regalia" ) );
	}

	@Test
	public void eachRatingDropsItsTierAndEachTierItsLevels() {
		int cycleKeep = Cycle.number;
		Cycle.number = 2;   //every tier open
		try {
			eachRatingBody();
		} finally {
			Cycle.number = cycleKeep;
		}
	}

	private void eachRatingBody() {
		int[] n = new int[10];
		for (int i = 0; i < 20000; i++) n[EndgameLoot.tierOfRating( 7.4f )]++;
		assertEquals( 0, n[6] ); assertEquals( 0, n[8] + n[9] - n[8] );
		assertEquals( "7.4 is 60% tier 7", 0.60, n[7] / 20000.0, 0.03 );
		assertEquals( "and 40% tier 8", 0.40, n[8] / 20000.0, 0.03 );
		for (int i = 0; i < 200; i++) {
			assertEquals( 6, EndgameLoot.tierOfRating( 6.0f ) );
			assertEquals( 6, EndgameLoot.tierOfRating( 3.0f ) );
			assertEquals( 7, EndgameLoot.tierOfRating( 7.0f ) );
			assertEquals( 8, EndgameLoot.tierOfRating( 8.0f ) );
			assertEquals( 9, EndgameLoot.tierOfRating( 9.0f ) );
			assertEquals( 9, EndgameLoot.tierOfRating( 12.0f ) );
		}
		//each place's rating
		assertEquals( 6.0f, EndgameLoot.zoneRating( 0, 0 ), 0.001f );
		assertEquals( 6.6f, EndgameLoot.zoneRating( 1, 0 ), 0.001f );
		assertEquals( 7.4f, EndgameLoot.zoneRating( 2, 0 ), 0.001f );
		assertEquals( "each clearing is half a tier more", 8.4f, EndgameLoot.zoneRating( 2, 2 ), 0.001f );
		assertEquals( 9.0f, EndgameLoot.zoneRating( 2, 9 ), 0.001f );
		assertEquals( 7.5f, EndgameLoot.minesRating( 56 ), 0.001f );
		assertEquals( 8.3f, EndgameLoot.minesRating( 64 ), 0.001f );
		assertEquals( 7.0f, EndgameLoot.ASCENT_RATING, 0.001f );
		assertEquals( 8.0f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.RATING, 0.001f );
		//the pieces a class gets at each tier are its own five, at the levels that tier starts at
		Hero h = Dungeon.hero;
		HeroClass keep = h.heroClass;
		try {
			HeroClass[] order = { HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE, HeroClass.HUNTRESS, HeroClass.DUELIST, HeroClass.CLERIC };
			for (int tier = 6; tier <= 9; tier++) {
				for (int i = 0; i < order.length; i++) {
					h.heroClass = order[i];
					int set = (tier == 6 ? 6 : tier == 7 ? 13 : tier == 8 ? 19 : 25) + i;
					Set<Class<?>> want = new HashSet<>( java.util.Arrays.asList( xyz.gabriwar.warpedpixeldungeon.items.GearSets.piecesOf( set ) ) );
					Set<Class<?>> seen = new HashSet<>();
					for (int k = 0; k < 1500; k++) {
						Item it = EndgameLoot.rollClassSet( order[i], tier );
						seen.add( it.getClass() );
						assertFalse( it.cursed );
						int lo = tier == 6 ? 1 : 2 * (tier - 6), hi = tier == 6 ? 3 : 2 * (tier - 6) + 2;
						boolean leveled = it instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Armor
								|| (it instanceof xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MeleeWeapon && !(it instanceof xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MagesStaff));
						if (leveled) assertTrue( "tier " + tier + " " + it.getClass().getSimpleName() + " at +" + it.level(), it.level() >= lo && it.level() <= hi );
						if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.rings.Ring) {
							int rlo = tier == 6 ? 0 : 2 * (tier - 6);
							assertTrue( "ring " + it.level(), it.level() >= rlo && it.level() <= rlo + 2 );
						}
					}
					assertEquals( "tier " + tier + " " + order[i], want, seen );
				}
			}
		} finally {
			h.heroClass = keep;
		}
	}

	@Test
	public void theTierSetBonusesScale() {
		int T7 = xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T7, T8 = xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T8, T9 = xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T9;
		int[] a = new int[31];
		a[T7] = 5;
		xyz.gabriwar.warpedpixeldungeon.items.GearSets.Effects e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( "T7 warrior damage: 4 steps of 6% at x1.25", 1.30f, e.damage, 0.0001f );
		assertEquals( 0.85f, e.taken, 0.0001f );
		assertEquals( 0.05f, e.lifesteal, 0.0001f );
		assertEquals( 0.3125f, e.lowHp, 0.0001f );
		a = new int[31]; a[T9] = 5;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( "T9 warrior is double", 1.48f, e.damage, 0.0001f );
		assertEquals( 0.76f, e.taken, 0.0001f );
		a = new int[31]; a[T8 + 1] = 5;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( "T8 mage wand damage", 1.48f, e.wandDamage, 0.0001f );
		assertEquals( 1.48f, e.wandRecharge, 0.0001f );
		assertEquals( 0.15f, e.freeZap, 0.0001f );
		assertEquals( 0.075f, e.zapBarrier, 0.0001f );
		a = new int[31]; a[T9 + 5] = 5;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( "T9 cleric regenerates faster, but never faster than every 6 turns", 6, e.regenEvery );
		assertEquals( 0.06f, e.barrierPct, 0.0001f );
		assertEquals( 0.10f, e.lifesteal, 0.0001f );
		//the original sets are exactly as they were
		a = new int[31]; a[6] = 5;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( 1.24f, e.damage, 0.0001f );
		assertEquals( 0.88f, e.taken, 0.0001f );
		//a tier 6 set and a tier 7 set of the same class add together
		a = new int[31]; a[6] = 2; a[T7] = 2;
		e = xyz.gabriwar.warpedpixeldungeon.items.GearSets.effects( a );
		assertEquals( 1f + 0.06f + 0.075f, e.damage, 0.0001f );
	}

	@Test
	public void theBonusStagesAreHarderAndPayTierEight() {
		//harder: every enemy on a bonus floor is stronger than the main floor of that depth, a little more each floor
		assertEquals( 1.15f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 1, 16 ), 0.0001f );
		assertEquals( 1.27f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 1, 19 ), 0.0001f );
		assertEquals( 1.20f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 2, 21 ), 0.0001f );
		assertEquals( 1.36f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 2, 25 ), 0.0001f );
		for (int d = 1; d <= 30; d++) {
			assertEquals( "the main dungeon is untouched", 1f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 0, d ), 0f );
			assertEquals( "so are the other branches", 1f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 5, d ), 0f );
		}
		assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 1, 14 ), 0f );   //the mining floors
		assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 2, 22 ) > xyz.gabriwar.warpedpixeldungeon.levels.SideStages.powerAt( 1, 17 ) );
		int bk = Dungeon.branch, dk = Dungeon.depth;
		try {
			Dungeon.branch = 2; Dungeon.depth = 22;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( "the shared enemy-strength function carries it", 1.24f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.statModifier( rat ), 0.0001f );
			assertEquals( 1.12f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.skillModifier( rat ), 0.0001f );
			Dungeon.branch = 0;
			assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.statModifier( rat ), 0f );
		} finally {
			Dungeon.branch = bk; Dungeon.depth = dk;
		}

		//richer: the Vault's and the ice floors hold tier 8 pieces (and nothing higher or lower in the class sets)
		Set<String> tier8 = new HashSet<>( java.util.Arrays.asList( TIER8_ITEMS ) );
		int found8 = 0, wrong = 0, floors = 0;
		int cycleKeepB = Cycle.number;
		Cycle.number = 1;   //tier 8 is a new cycle's
		Dungeon.generatedLevels.clear();
		try {
			int[][] stages = { { 1, 16 }, { 1, 17 }, { 1, 18 }, { 1, 19 }, { 2, 21 }, { 2, 22 }, { 2, 23 }, { 2, 24 } };
			for (int rep = 0; rep < 4; rep++) {
				for (int[] st : stages) {
					Dungeon.branch = st[0]; Dungeon.depth = st[1];
					Level level = Dungeon.newLevel();
					assertFalse( "stage " + st[0] + "/" + st[1] + " is a dead end", level instanceof xyz.gabriwar.warpedpixeldungeon.levels.DeadEndLevel );
					floors++;
					for (xyz.gabriwar.warpedpixeldungeon.items.Heap heap : level.heaps.valueList()) {
						for (Item it : heap.items) {
							if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.SetTier || java.util.Arrays.asList( CLASS_ITEMS ).contains( it.getClass().getName().replace( P, "" ) )) {
								if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.SetTier && ((xyz.gabriwar.warpedpixeldungeon.items.SetTier)it).setTier() == 8) found8++; else wrong++;
							}
						}
					}
				}
			}
		} finally {
			Cycle.number = cycleKeepB;
			Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.generatedLevels.clear();
		}
		assertTrue( "tier 8 pieces on " + floors + " bonus floors: " + found8, found8 >= 1 );
		assertEquals( "no other class tier drops on a bonus floor", 0, wrong );

		//the ice boss's hoard
		try {
			Dungeon.branch = 2; Dungeon.depth = 25;
			Level boss = Dungeon.newLevel();
			assertTrue( "the ice boss level", boss instanceof xyz.gabriwar.warpedpixeldungeon.levels.FrozenBossLevel );
			int cell = boss.randomDestination( null );
			xyz.gabriwar.warpedpixeldungeon.levels.SideStages.bossHoard( boss, cell );
			xyz.gabriwar.warpedpixeldungeon.items.Heap hoard = boss.heaps.get( cell );
			assertNotNull( hoard );
			assertTrue( "the hoard holds 4 items, got " + hoard.items.size(), hoard.items.size() >= 4 );
		} finally {
			Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void theCycleMakesTheDungeonStrongerAndNothingElse() {
		//cycle 0 is the dungeon as it always was
		assertEquals( 1f, Cycle.powerAt( 0, 0, 12 ), 0f );
		assertEquals( 2f, Cycle.powerAt( 1, 0, 5 ), 0f );
		assertEquals( 3f, Cycle.powerAt( 2, 0, 26 ), 0f );
		assertEquals( "a side branch of the dungeon too", 2f, Cycle.powerAt( 1, 2, 22 ), 0f );
		assertEquals( "but not the mines (depth 56+)", 1f, Cycle.powerAt( 1, 0, 60 ), 0f );
		assertEquals( "not the surface", 1f, Cycle.powerAt( 5, 0, 97 ), 0f );
		assertEquals( "not a plaza zone", 1f, Cycle.powerAt( 3, 105, 12 ), 0f );
		assertEquals( "not the extra stages past the bottom", 1f, Cycle.powerAt( 3, 0, 28 ), 0f );
		assertEquals( 1.5f, Cycle.skillAt( 1, 0, 8 ), 0f );
		assertEquals( 2.0f, Cycle.skillAt( 2, 0, 8 ), 0f );
		assertEquals( 7.9f, Cycle.lootRating( 1, 26 ), 0.01f );
		assertTrue( Cycle.lootRating( 1, 5 ) < Cycle.lootRating( 1, 20 ) );
		assertTrue( Cycle.lootRating( 2, 5 ) > Cycle.lootRating( 1, 5 ) );
		assertEquals( 9f, Cycle.lootRating( 9, 26 ), 0f );

		//the shared enemy-strength function carries it
		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		try {
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 8;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( 2f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.statModifier( rat ), 0.0001f );
			assertEquals( 1.5f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.skillModifier( rat ), 0.0001f );
			Cycle.number = 0;
			assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.statModifier( rat ), 0f );
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
		}
	}

	@Test
	public void beginningACycleWipesTheDungeonAndKeepsTheHero() {
		Hero h = Dungeon.hero;
		int nk = Cycle.number;
		java.util.ArrayList<Integer> genKeep = new java.util.ArrayList<>( Dungeon.generatedLevels );
		int packSize = h.belongings.backpack.items.size();
		try {
			Cycle.number = 0;
			h.belongings.backpack.items.add( new xyz.gabriwar.warpedpixeldungeon.items.Amulet() );
			Statistics.ascended = true;
			Statistics.amuletObtained = true;
			Statistics.highestAscent = 25;
			Dungeon.ascentRebuilds.put( 7, 1 );
			Dungeon.ascentSession.add( 7 );
			Dungeon.generatedLevels.clear();
			for (int d : new int[]{ 1, 5, 12, 26 }) Dungeon.generatedLevels.add( d );                 //the dungeon
			Dungeon.generatedLevels.add( 16 + 1000 );                                               //the Vault
			Dungeon.generatedLevels.add( 97 );                                                       //the surface
			Dungeon.generatedLevels.add( 60 );                                                       //a dark mine floor
			Dungeon.generatedLevels.add( 12 + 1000 * 105 );                                          //a plaza zone
			Dungeon.tengukilled = true; Dungeon.crabkingkilled = true; Dungeon.templeCompleted = true;
			Dungeon.chapters.add( 3 );
			int levelBefore = h.lvl;
			assertTrue( Cycle.offersReset() );

			Cycle.number++;
			Cycle.resetDungeonState( h );

			assertEquals( 1, Cycle.number );
			assertFalse( "the curse's mark is gone", Statistics.ascended );
			assertFalse( Statistics.amuletObtained );
			assertEquals( 0, Statistics.highestAscent );
			assertFalse( "the amulet goes back to its heart", h.belongings.getItem( xyz.gabriwar.warpedpixeldungeon.items.Amulet.class ) != null );
			assertTrue( Dungeon.ascentRebuilds.isEmpty() );
			assertTrue( Dungeon.ascentSession.isEmpty() );
			for (int d : new int[]{ 1, 5, 12, 26, 1016 }) assertFalse( "floor " + d + " must be unvisited", Dungeon.generatedLevels.contains( d ) );
			assertTrue( "the surface stays", Dungeon.generatedLevels.contains( 97 ) );
			assertTrue( "the mines stay", Dungeon.generatedLevels.contains( 60 ) );
			assertTrue( "a plaza zone stays", Dungeon.generatedLevels.contains( 12 + 1000 * 105 ) );
			assertFalse( Dungeon.tengukilled );
			assertFalse( Dungeon.crabkingkilled );
			assertFalse( Dungeon.templeCompleted );
			assertTrue( Dungeon.chapters.isEmpty() );
			assertEquals( "the hero keeps their level", levelBefore, h.lvl );
			assertFalse( "the offer is closed until the next climb", Cycle.offersReset() );

			//the cycle survives a save, and an old save is cycle 0
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			Cycle.number = 3;
			Dungeon.storeAscentState( b );
			Cycle.number = 0;
			Dungeon.restoreAscentState( b );
			assertEquals( 3, Cycle.number );
			Dungeon.restoreAscentState( new com.watabou.utils.Bundle() );
			assertEquals( 0, Cycle.number );
		} finally {
			Cycle.number = nk;
			Dungeon.generatedLevels.clear();
			Dungeon.generatedLevels.addAll( genKeep );
			Dungeon.ascentRebuilds.clear();
			Dungeon.ascentSession.clear();
			Statistics.ascended = false;
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
	}

	private static int emptyCell( Level level ) {
		int c;
		do { c = level.randomDestination( null ); } while (level.heaps.get( c ) != null);
		return c;
	}

	@Test
	public void theClimbEventsBuildAndPay() {
		//the odds add up to a hundred, and an event of each kind comes up
		int[] counts = new int[10];
		for (int i = 0; i < 40000; i++) counts[xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.pick()]++;
		double[] want = { 0.30, 0.14, 0.08, 0.08, 0.08, 0.08, 0.07, 0.06, 0.06, 0.05 };
		for (int e = 0; e < 10; e++) assertEquals( "event " + e, want[e], counts[e] / 40000.0, 0.02 );

		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Dungeon.generatedLevels.clear();
		try {
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 7;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			int mobsBefore = level.mobs.size();

			//a bounty: one hunter, four times the health, marked and a champion
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.BOUNTY );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.BOUNTY, level.climbEvent );
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob hunter = null;
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty.class ) != null) hunter = m;
			assertNotNull( "the bounty's hunter", hunter );
			assertEquals( mobsBefore + 1, level.mobs.size() );
			assertEquals( hunter.HT, hunter.HP );
			assertNotNull( hunter.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.ChampionEnemy.Giant.class ) );

			//its fall pays a hoard: three items, a piece of the hero's set, and gold
			Cycle.onDeath( hunter );
			xyz.gabriwar.warpedpixeldungeon.items.Heap hoard = level.heaps.get( hunter.pos );
			assertNotNull( hoard );
			boolean gold = false;
			for (Item it : hoard.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.Gold) gold = true;
			assertTrue( "the bounty pays gold", gold );
			assertTrue( "and trophies: " + hoard.items.size(), hoard.items.size() >= 5 );

			//a hunted floor: a pack of five
			int before = level.mobs.size();
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.HUNTED );
			assertTrue( "pack of " + (level.mobs.size() - before), level.mobs.size() - before >= 4 );

			//a cache: loot lying about
			int heapsBefore = level.heaps.valueList().size();
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.CACHE );
			assertTrue( "the cache's items", level.heaps.valueList().size() > heapsBefore );

			//a blood moon: 30% on top of the cycle
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.BLOOD_MOON );
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( 2f * 1.3f, Cycle.power( rat ), 0.0001f );
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.NONE );
			assertEquals( 2f, Cycle.power( rat ), 0.0001f );

			//a floor's event is saved with the floor
			level.climbEvent = xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.HUNTED;
			level.climbEventAnnounced = true;
			com.watabou.utils.Bundle lb = new com.watabou.utils.Bundle();
			level.storeInBundle( lb );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.HUNTED, lb.getInt( "climbEvent" ) );
			assertTrue( lb.getBoolean( "climbEventAnnounced" ) );

			//a boss of a later cycle pays too: Goo, on floor 5
			Dungeon.depth = 5;
			level.climbEvent = 0;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo goo = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo();
			goo.pos = emptyCell( level );
			Cycle.onDeath( goo );
			xyz.gabriwar.warpedpixeldungeon.items.Heap bossHoard = level.heaps.get( goo.pos );
			assertNotNull( "a later cycle's boss pays", bossHoard );
			assertTrue( "three items, got " + bossHoard.items.size(), bossHoard.items.size() >= 3 );
			Cycle.number = 0;
			int heapsB = level.heaps.valueList().size();
			goo.pos = emptyCell( level );
			Cycle.onDeath( goo );
			org.junit.Assert.assertNull( "cycle 0 boss pays nothing extra", level.heaps.get( goo.pos ) );
			Cycle.number = 1;

			//many climb floors in a row build without trouble
			Dungeon.ascentRebuilds.put( 9, 1 );
			for (int i = 0; i < 12; i++) {
				Dungeon.depth = 9;
				Level climb = Dungeon.newLevel();
				assertFalse( climb instanceof xyz.gabriwar.warpedpixeldungeon.levels.DeadEndLevel );
				assertTrue( climb.climbEvent >= 0 && climb.climbEvent <= 4 );
			}
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.ascentRebuilds.clear();
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void theHigherTiersWaitForNewGamePlus() throws Exception {
		int keep = Cycle.number;
		Hero h = Dungeon.hero;
		Set<String> yogNames = new HashSet<>(), t8 = new HashSet<>(), t9 = new HashSet<>(), t7 = new HashSet<>();
		for (String n : YOG_ITEMS) yogNames.add( P + n );
		for (String n : TIER7_ITEMS) t7.add( P + n );
		for (String n : TIER8_ITEMS) t8.add( P + n );
		for (String n : TIER9_ITEMS) t9.add( P + n );
		try {
			//the cap by cycle
			Cycle.number = 0; assertEquals( 7, EndgameLoot.maxTier() );
			Cycle.number = 1; assertEquals( 8, EndgameLoot.maxTier() );
			Cycle.number = 2; assertEquals( 9, EndgameLoot.maxTier() );
			Cycle.number = 7; assertEquals( 9, EndgameLoot.maxTier() );

			//the bonus levels for a rating over the cap
			Cycle.number = 0;
			assertEquals( 0, EndgameLoot.excessLevels( 7.0f ) );
			assertEquals( 0, EndgameLoot.excessLevels( 6.0f ) );
			assertEquals( 1, EndgameLoot.excessLevels( 7.4f ) );
			assertEquals( 2, EndgameLoot.excessLevels( 8.0f ) );
			assertEquals( 4, EndgameLoot.excessLevels( 9.0f ) );
			Cycle.number = 1;
			assertEquals( 0, EndgameLoot.excessLevels( 8.0f ) );
			assertEquals( 2, EndgameLoot.excessLevels( 9.0f ) );
			Cycle.number = 2;
			assertEquals( 0, EndgameLoot.excessLevels( 9.0f ) );

			//no rating, however high, drops past the cap
			for (int c = 0; c <= 2; c++) {
				Cycle.number = c;
				for (float r = 5f; r <= 12f; r += 0.5f) {
					for (int i = 0; i < 300; i++) assertTrue( "cycle " + c + " rating " + r, EndgameLoot.tierOfRating( r ) <= 7 + c );
				}
				for (int i = 0; i < 100; i++) assertEquals( 7 + c, EndgameLoot.tierOfRating( 9.5f ) );
			}

			//the first run: tier 7 at most, no Yog-Forged, and a rich place's pieces start higher
			Cycle.number = 0;
			int t7found = 0;
			for (int i = 0; i < 20000; i++) {
				Item it = EndgameLoot.rollRated( 9.0f );
				String n = it.getClass().getName();
				assertFalse( "tier 8 in the first run: " + n, t8.contains( n ) );
				assertFalse( "tier 9 in the first run: " + n, t9.contains( n ) );
				assertFalse( "Yog-Forged in the first run: " + n, yogNames.contains( n ) );
				if (t7.contains( n )) {
					t7found++;
					if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Armor) assertTrue( "+4 over the cap: level " + it.level(), it.level() >= 6 && it.level() <= 8 );
				}
			}
			assertTrue( "tier 7 pieces dropped: " + t7found, t7found > 1000 );
			for (int i = 0; i < 400; i++) assertFalse( yogNames.contains( EndgameLoot.rollYog().getClass().getName() ) );

			//the first new cycle opens tier 8 (and Yog-Forged), not 9
			Cycle.number = 1;
			int t8found = 0;
			for (int i = 0; i < 20000; i++) {
				String n = EndgameLoot.rollRated( 9.0f ).getClass().getName();
				assertFalse( "tier 9 in the first new cycle: " + n, t9.contains( n ) );
				if (t8.contains( n )) t8found++;
			}
			assertTrue( "tier 8 pieces dropped: " + t8found, t8found > 1000 );
			boolean yog = false;
			for (int i = 0; i < 400; i++) if (yogNames.contains( EndgameLoot.rollYog().getClass().getName() )) yog = true;
			assertTrue( "Yog-Forged in a new cycle", yog );

			//the second opens tier 9
			Cycle.number = 2;
			int t9found = 0;
			for (int i = 0; i < 20000; i++) if (t9.contains( EndgameLoot.rollRated( 9.0f ).getClass().getName() )) t9found++;
			assertTrue( "tier 9 pieces dropped: " + t9found, t9found > 1000 );

			//the descriptions say so
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate().desc().contains( "New Game+" ) );
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate().desc().contains( "second new cycle" ) );
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate().desc().contains( "New Game+" ) );
			assertFalse( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate().desc().contains( "New Game+" ) );
		} finally {
			Cycle.number = keep;
		}
	}

	@Test
	public void shardsReforgeTheHighTiers() throws Exception {
		int keep = Cycle.number;
		Hero h = Dungeon.hero;
		int packSize = h.belongings.backpack.items.size();
		try {
			//what can be reforged
			assertTrue( Reforge.eligible( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate() ) );
			assertTrue( Reforge.eligible( new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate() ) );
			assertTrue( "Yog-Forged too", Reforge.eligible( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate() ) );
			assertFalse( "tier 6 is not high enough", Reforge.eligible( new xyz.gabriwar.warpedpixeldungeon.items.armor.WarlordsPlate() ) );
			assertFalse( Reforge.eligible( new xyz.gabriwar.warpedpixeldungeon.items.wands.WandOfFreeze() ) );
			assertEquals( 9, Reforge.tierOf( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate() ) );
			//costs rise with the tier
			assertTrue( Reforge.costPiece( 7 ) < Reforge.costPiece( 8 ) && Reforge.costPiece( 8 ) < Reforge.costPiece( 9 ) );
			assertTrue( Reforge.costAwaken( 7 ) < Reforge.costAwaken( 8 ) );

			//reroll piece: another piece of the same set and tier, at the same level, over every piece
			for (int tier = 7; tier <= 9; tier++) {
				String[] list = tier == 7 ? TIER7_ITEMS : tier == 8 ? TIER8_ITEMS : TIER9_ITEMS;
				for (int base = 0; base < 30; base += 5) {
					Item start = (Item)Class.forName( P + list[base] ).getDeclaredConstructor().newInstance();
					start.level( 4 );
					Set<Class<?>> got = new HashSet<>();
					for (int i = 0; i < 300; i++) {
						Item n = Reforge.rerollPiece( start );
						assertNotSame( start.getClass(), n.getClass() );
						assertEquals( "the same set", xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( start.getClass() ), xyz.gabriwar.warpedpixeldungeon.items.GearSets.setOf( n.getClass() ) );
						assertEquals( "the same tier", tier, ((SetTier)n).setTier() );
						assertEquals( "the same level", 4, n.level() );
						assertFalse( n.cursed );
						got.add( n.getClass() );
					}
					assertEquals( "all four other pieces turn up", 4, got.size() );
				}
			}

			//awaken: the same piece, a tier up, only as far as the cycle allows
			Cycle.number = 0;
			assertFalse( "tier 8 does not exist yet", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate() ) );
			Cycle.number = 1;
			assertTrue( Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate() ) );
			assertFalse( "tier 9 waits for the second cycle", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate() ) );
			Cycle.number = 2;
			assertTrue( Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate() ) );
			assertFalse( "nothing above tier 9", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate() ) );
			assertFalse( "Yog-Forged does not awaken", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate() ) );
			for (int i = 0; i < 30; i++) {
				Item t7 = (Item)Class.forName( P + TIER7_ITEMS[i] ).getDeclaredConstructor().newInstance();
				t7.level( 6 );
				Item t8 = Reforge.awaken( t7 );
				assertEquals( "its twin one tier up", P + TIER8_ITEMS[i], t8.getClass().getName() );
				assertEquals( 6, t8.level() );
				Item t9 = Reforge.awaken( t8 );
				assertEquals( P + TIER9_ITEMS[i], t9.getClass().getName() );
			}

			//affix and temper
			xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword sword = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword();
			assertTrue( Reforge.canAffix( sword ) );
			assertTrue( Reforge.rerollAffix( sword ) );
			assertNotNull( sword.enchantment );
			xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate plate = new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate();
			assertTrue( Reforge.rerollAffix( plate ) );
			assertNotNull( plate.glyph );
			assertFalse( "a ring has no affix", Reforge.canAffix( new xyz.gabriwar.warpedpixeldungeon.items.rings.AscendedWarlordsSignet() ) );
			assertFalse( "nor a relic", Reforge.canAffix( new xyz.gabriwar.warpedpixeldungeon.items.AscendedWarlordsBanner() ) );
			Item tp = new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate();
			tp.level( 3 );
			for (int i = 0; i < 40 && Reforge.canTemper( tp ); i++) {
				int before = tp.level();
				assertTrue( Reforge.temper( tp ) );
				int gain = tp.level() - before;
				assertTrue( "tempered by " + gain, gain >= 1 && gain <= 2 );
			}
			assertEquals( "tempering stops at the cap", Reforge.TEMPER_CAP, tp.level() );
			assertFalse( Reforge.temper( tp ) );

			//swapping a piece in the pack
			Item old = new xyz.gabriwar.warpedpixeldungeon.items.AscendedWarlordsBanner();
			old.collect( h.belongings.backpack );
			Item neu = new xyz.gabriwar.warpedpixeldungeon.items.AscendedWarlordsCrest();
			Reforge.swap( h, old, neu );
			assertFalse( h.belongings.backpack.items.contains( old ) );
			assertTrue( h.belongings.backpack.items.contains( neu ) );
		} finally {
			Cycle.number = keep;
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
	}

	@Test
	public void legacyPointsAreEarnedSpentAndSaved() {
		Hero h = Dungeon.hero;
		int hpKeep = h.HT;
		Legacy.reset();
		try {
			assertEquals( 0, Legacy.unspent() );
			assertFalse( "nothing to spend", Legacy.spend( Legacy.Stat.SPEED ) );
			assertEquals( 1f, Legacy.speedMultiplier(), 0f );
			assertEquals( 0, Legacy.bonusHP() );
			for (int i = 0; i < 20; i++) Legacy.award();
			assertEquals( 20, Legacy.unspent() );
			assertEquals( 20, Legacy.earned() );
			for (int i = 0; i < 3; i++) assertTrue( Legacy.spend( Legacy.Stat.SPEED ) );
			assertEquals( 17, Legacy.unspent() );
			assertEquals( 1.06f, Legacy.speedMultiplier(), 0.0001f );
			for (int i = 0; i < 5; i++) Legacy.spend( Legacy.Stat.ACCURACY );
			assertEquals( 1.15f, Legacy.accuracyMultiplier(), 0.0001f );
			for (int i = 0; i < 4; i++) Legacy.spend( Legacy.Stat.EVASION );
			assertEquals( 1.12f, Legacy.evasionMultiplier(), 0.0001f );
			for (int i = 0; i < 2; i++) Legacy.spend( Legacy.Stat.RESISTANCE );
			assertEquals( 0.03f, Legacy.resistance(), 0.0001f );
			assertEquals( 97, Legacy.incoming( 100 ) );
			assertEquals( "never to nothing", 1, Legacy.incoming( 1 ) );
			for (int i = 0; i < 2; i++) Legacy.spend( Legacy.Stat.HEALTH );
			assertEquals( 8, Legacy.bonusHP() );
			assertEquals( "the hero's health follows", 5 * (h.lvl - 1) + 20 + h.HTBoost + 8, h.HT );
			//a stat stops at its limit
			Legacy.reset();
			for (int i = 0; i < 40; i++) Legacy.award();
			int spentOnCrit = 0;
			for (int i = 0; i < 40; i++) if (Legacy.spend( Legacy.Stat.CRIT_CHANCE )) spentOnCrit++;
			assertEquals( Legacy.MAX_PER_STAT, spentOnCrit );
			assertEquals( 0.30f, Legacy.critChance(), 0.0001f );
			assertEquals( 25, Legacy.unspent() );
			//critical hits: about three in ten, for half as much again
			int crits = 0;
			for (int i = 0; i < 10000; i++) {
				int d = Legacy.outgoing( null, 100 );
				if (d != 100) { crits++; assertEquals( 150, d ); }
			}
			assertEquals( "crit rate", 0.30, crits / 10000.0, 0.03 );
			for (int i = 0; i < 5; i++) Legacy.spend( Legacy.Stat.CRIT_DAMAGE );
			assertEquals( 2.0f, Legacy.critMultiplier(), 0.0001f );

			//saved with the run, and an old save has none
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			Dungeon.storeAscentState( b );
			Legacy.reset();
			assertEquals( 0, Legacy.earned() );
			Dungeon.restoreAscentState( b );
			assertEquals( 40, Legacy.earned() );
			assertEquals( 20, Legacy.unspent() );
			assertEquals( 15, Legacy.points( Legacy.Stat.CRIT_CHANCE ) );
			assertEquals( 5, Legacy.points( Legacy.Stat.CRIT_DAMAGE ) );
			Dungeon.restoreAscentState( new com.watabou.utils.Bundle() );
			assertEquals( 0, Legacy.earned() );
			assertEquals( 0, Legacy.points( Legacy.Stat.CRIT_CHANCE ) );
		} finally {
			Legacy.reset();
			h.updateHT( false );
			h.HT = Math.max( h.HT, 1 );
		}
	}

	@Test
	public void bossesMutateInNewCycles() {
		assertEquals( 0, BossMutation.count( 0 ) );
		assertEquals( 1, BossMutation.count( 1 ) );
		assertEquals( 2, BossMutation.count( 2 ) );
		assertEquals( 3, BossMutation.count( 3 ) );
		assertEquals( 3, BossMutation.count( 9 ) );
		assertEquals( "Summoner, Frenzy", BossMutation.describe( BossMutation.SUMMONER | BossMutation.FRENZY ) );
		int keep = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Dungeon.generatedLevels.clear();
		try {
			Dungeon.branch = 0; Dungeon.depth = 5;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			for (int cycle = 0; cycle <= 4; cycle++) {
				Cycle.number = cycle;
				xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo goo = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo();
				goo.pos = level.randomDestination( null );
				level.mobs.add( goo );
				BossMutation.assign( level );
				BossMutation m = goo.buff( BossMutation.class );
				if (cycle == 0) {
					assertNull( "cycle 0 bosses do not mutate", m );
				} else {
					assertNotNull( m );
					assertTrue( "every mutated boss summons", m.has( BossMutation.SUMMONER ) );
					assertEquals( "cycle " + cycle + " mutations", Math.min( 3, cycle ), Integer.bitCount( m.mask ) );
				}
				level.mobs.remove( goo );
			}
			//the summoning and the vengeance
			Cycle.number = 1;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo boss = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo();
			boss.pos = emptyCell( level );
			level.mobs.add( boss );
			int before = level.mobs.size();
			assertEquals( 2, BossMutation.summon( boss, 2 ) );
			assertEquals( before + 2, level.mobs.size() );
			Buff.affect( boss, BossMutation.class ).mask = BossMutation.VENGEFUL;
			before = level.mobs.size();
			Cycle.onDeath( boss );
			assertEquals( "three spring up where it fell", before + 3, level.mobs.size() );
			//and it pays shards
			xyz.gabriwar.warpedpixeldungeon.items.Heap hoard = level.heaps.get( boss.pos );
			assertNotNull( hoard );
			boolean shards = false;
			for (Item it : hoard.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.CycleShard) shards = it.quantity() >= 3;
			assertTrue( "a boss of a new cycle pays shards", shards );
		} finally {
			Cycle.number = keep; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void theFiveNewClimbEventsWork() {
		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Hero h = Dungeon.hero;
		int posKeep = h.pos, hpKeep = h.HP, htKeep = h.HT, goldKeep = Dungeon.gold;
		Dungeon.generatedLevels.clear();
		try {
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 7;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			h.pos = emptyCell( level );

			//the cursed chest: a chest of loot that wakes the floor, once
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.CURSED_CHEST );
			assertTrue( level.cursedChest != -1 );
			xyz.gabriwar.warpedpixeldungeon.items.Heap chest = level.heaps.get( level.cursedChest );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.items.Heap.Type.CHEST, chest.type );
			assertTrue( "chest items " + chest.items.size(), chest.items.size() >= 4 );
			com.watabou.utils.Bundle lb = new com.watabou.utils.Bundle();
			level.storeInBundle( lb );
			assertEquals( "saved with the floor", level.cursedChest, lb.getInt( "cursedChest" ) );
			int before = level.mobs.size();
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.onChestOpened( chest, h );
			assertEquals( -1, level.cursedChest );
			assertTrue( "the floor wakes: " + (level.mobs.size() - before), level.mobs.size() - before >= 4 );
			int after = level.mobs.size();
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.onChestOpened( chest, h );
			assertEquals( "only once", after, level.mobs.size() );

			//lockdown: sealed, three waves, then the doors open and the floor pays
			h.pos = emptyCell( level );
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.LOCKDOWN );
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.announce( level );
			assertTrue( "the stairs are sealed", level.locked );
			xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown lock = h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.class );
			assertNotNull( lock );
			int m0 = level.mobs.size();
			lock.act();
			assertEquals( 1, lock.wave );
			assertTrue( "wave 1: " + (level.mobs.size() - m0), level.mobs.size() - m0 >= 3 );
			for (int i = 0; i < xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.GAP - 1; i++) lock.act();
			assertEquals( "not yet the second wave", 1, lock.wave );
			lock.act();
			assertEquals( 2, lock.wave );
			for (int i = 0; i < xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.GAP; i++) lock.act();
			assertEquals( 3, lock.wave );
			assertTrue( "still sealed while the third wave lives", level.locked );
			assertEquals( 4, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.waveSize( 1 ) );
			assertEquals( 6, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.waveSize( 3 ) );
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : new java.util.ArrayList<>( level.mobs )) {
				if (m.alignment == xyz.gabriwar.warpedpixeldungeon.actors.Char.Alignment.ENEMY) level.mobs.remove( m );
			}
			lock.act();
			assertFalse( "the doors unseal", level.locked );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.NONE, level.climbEvent );
			assertNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.class ) );
			xyz.gabriwar.warpedpixeldungeon.items.Heap spoils = level.heaps.get( h.pos );
			assertNotNull( spoils );
			assertTrue( "spoils " + spoils.items.size(), spoils.items.size() >= 4 );

			//a floor still sealed when the hero returns takes up the waves again
			level.climbEvent = xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.LOCKDOWN;
			level.locked = true;
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.announce( level );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.class ) );
			h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Lockdown.class ).detach();
			level.locked = false;
			level.climbEvent = xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.NONE;

			//the rematch: an echo with eight times the health, a boss's mutations and a double payout
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.REMATCH );
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob echo = null;
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) {
				xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty bb = m.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty.class );
				if (bb != null && bb.boost == 2) echo = m;
			}
			assertNotNull( "the rematch's echo", echo );
			xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation mut = echo.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.class );
			assertNotNull( mut );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.SUMMONER | xyz.gabriwar.warpedpixeldungeon.actors.buffs.BossMutation.WARDED, mut.mask );
			assertNotNull( echo.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.ChampionEnemy.Giant.class ) );
			echo.pos = emptyCell( level );
			Cycle.onDeath( echo );
			xyz.gabriwar.warpedpixeldungeon.items.Heap pay = level.heaps.get( echo.pos );
			assertNotNull( pay );
			int shards = 0;
			for (Item it : pay.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.CycleShard) shards += it.quantity();
			assertTrue( "a rematch pays 3 + cycle + 3 shards, got " + shards, shards >= 7 );
			assertTrue( "and five items and more: " + pay.items.size(), pay.items.size() >= 6 );

			//the blessing shrine: a boon with a bane
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.BLESSING );
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine shrine = null;
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m instanceof xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine) shrine = (xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine)m;
			assertNotNull( shrine );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.BLESSING, shrine.kind );
			h.HT = 100; h.HP = 100;
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.bless( h, 0 ) );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Haste.class ) );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Vulnerable.class ) );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.bless( h, 1 ) );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Weakness.class ) );
			assertTrue( "a barrier of 40%", h.shielding() >= 40 );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.bless( h, 2 ) );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bless.class ) );
			assertNotNull( h.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Cripple.class ) );
			assertFalse( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.bless( h, 5 ) );
			for (Class<? extends xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff> bc : java.util.Arrays.<Class<? extends xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff>>asList(
					xyz.gabriwar.warpedpixeldungeon.actors.buffs.Haste.class, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Vulnerable.class, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Weakness.class,
					xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bless.class, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Cripple.class, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Barrier.class )) {
				if (h.buff( bc ) != null) h.buff( bc ).detach();
			}
			//a shrine keeps what it was and whether it is spent
			shrine.used = true;
			com.watabou.utils.Bundle sb = new com.watabou.utils.Bundle();
			shrine.storeInBundle( sb );
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine back = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine();
			back.restoreFromBundle( sb );
			assertTrue( back.used );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.BLESSING, back.kind );

			//the cursed shrine: blood, gold or a fight, for a reward at the hero's feet
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.CURSED_SHRINE );
			boolean cursedShrine = false;
			for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) if (m instanceof xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine && ((xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine)m).kind == xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.CURSED) cursedShrine = true;
			assertTrue( cursedShrine );
			h.HT = 50; h.HP = 50;
			h.pos = emptyCell( level );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 0, level ) );
			assertEquals( "30% of the blood", 35, h.HP );
			assertTrue( "two rewards: " + level.heaps.get( h.pos ).items.size(), level.heaps.get( h.pos ).items.size() >= 2 );
			h.HP = 3;
			h.pos = emptyCell( level );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 0, level ) );
			assertTrue( "the shrine never kills", h.HP >= 1 );
			h.HP = 1;
			assertFalse( "no blood to spare", xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 0, level ) );
			assertEquals( 1, h.HP );
			Dungeon.gold = 1000;
			h.pos = emptyCell( level );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 1, level ) );
			assertEquals( "half the gold", 500, Dungeon.gold );
			boolean gotShards = false;
			for (Item it : level.heaps.get( h.pos ).items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.CycleShard) gotShards = true;
			assertTrue( gotShards );
			Dungeon.gold = 50;
			assertFalse( "too poor", xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 1, level ) );
			assertEquals( 50, Dungeon.gold );
			h.pos = emptyCell( level );
			int mb = level.mobs.size();
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.ClimbShrine.curse( h, 2, level ) );
			assertTrue( "the guardians: " + (level.mobs.size() - mb), level.mobs.size() - mb >= 5 );
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			h.pos = posKeep; h.HT = htKeep; h.HP = hpKeep; Dungeon.gold = goldKeep;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void theBountyBoardPostsTakesAndPays() {
		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Hero h = Dungeon.hero;
		int goldKeep = Dungeon.gold, packSize = h.belongings.backpack.items.size();
		java.util.Set<BountyBoard.Kind> covered = new java.util.HashSet<>();
		Dungeon.generatedLevels.clear();
		try {
			//no board in the first run
			Cycle.number = 0;
			BountyBoard.reset();
			assertFalse( BountyBoard.available() );
			assertFalse( BountyBoard.accept( 0 ) );
			Dungeon.branch = 0; Dungeon.depth = 7;
			Cycle.number = 1;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			for (int cycle = 1; cycle <= 8; cycle++) {
				Cycle.number = cycle;
				BountyBoard.reset();
				//three different contracts, the same each time they are read
				java.util.Set<BountyBoard.Kind> kinds = new java.util.HashSet<>();
				for (int s = 0; s < 3; s++) {
					BountyBoard.Contract c = BountyBoard.current( s );
					BountyBoard.Contract again = BountyBoard.contract( Dungeon.seed, cycle, s );
					assertEquals( c.kind, again.kind );
					assertEquals( c.need, again.need );
					assertEquals( c.depth, again.depth );
					kinds.add( c.kind );
					if (c.kind == BountyBoard.Kind.BOSS) assertTrue( "boss floor " + c.depth, c.depth % 5 == 0 && c.depth >= 5 && c.depth <= 25 );
					assertTrue( c.shards > 0 && c.gold > 0 );
				}
				assertEquals( "three different kinds", 3, kinds.size() );

				for (int s = 0; s < 3; s++) {
					BountyBoard.Contract c = BountyBoard.current( s );
					covered.add( c.kind );
					assertTrue( BountyBoard.accept( s ) );
					assertFalse( "not twice", BountyBoard.accept( s ) );
					assertFalse( "not done yet", BountyBoard.isReady( s ) );
					assertFalse( "nothing to claim yet", BountyBoard.claim( h, s ) );
					Dungeon.branch = 0; Dungeon.depth = 7;
					switch (c.kind) {
						case HUNTERS:
							for (int i = 0; i < c.need; i++) {
								BountyBoard.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat() );   //an ordinary enemy does not count
							}
							assertEquals( 0, BountyBoard.progressOf( s ) );
							for (int i = 0; i < c.need; i++) {
								xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat hunter = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
								xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff.affect( hunter, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bounty.class );
								BountyBoard.onKill( hunter );
							}
							break;
						case BOSS:
							Dungeon.depth = (c.depth == 5 ? 10 : 5);
							BountyBoard.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo() );   //the wrong floor
							assertEquals( 0, BountyBoard.progressOf( s ) );
							Dungeon.depth = c.depth;
							BountyBoard.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo() );
							break;
						case SLAYER:
							for (int i = 0; i < c.need; i++) BountyBoard.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat() );
							break;
						default:
							BountyBoard.onZoneCleared();
					}
					assertTrue( c.kind + " done", BountyBoard.isReady( s ) );
					int gold = Dungeon.gold;
					assertTrue( BountyBoard.claim( h, s ) );
					assertEquals( c.gold, Dungeon.gold - gold );
					assertTrue( BountyBoard.isClaimed( s ) );
					assertFalse( "claimed once", BountyBoard.claim( h, s ) );
					assertFalse( BountyBoard.isReady( s ) );
				}
				//a new cycle posts new contracts
				Cycle.number = cycle + 1;
				assertFalse( BountyBoard.isAccepted( 0 ) );
				assertFalse( BountyBoard.isClaimed( 0 ) );
			}
			assertEquals( "every kind of contract was posted and paid", 4, covered.size() );
			int shards = 0;
			for (Item it : h.belongings.backpack.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.CycleShard) shards += it.quantity();
			assertTrue( "the pay includes shards: " + shards, shards > 0 );

			//the board is saved with the run
			Cycle.number = 2;
			BountyBoard.reset();
			assertTrue( BountyBoard.accept( 1 ) );
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			Dungeon.storeAscentState( b );
			BountyBoard.reset();
			assertFalse( BountyBoard.isAccepted( 1 ) );
			Dungeon.restoreAscentState( b );
			assertEquals( 2, Cycle.number );
			assertTrue( BountyBoard.isAccepted( 1 ) );
			assertFalse( BountyBoard.isAccepted( 0 ) );
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.gold = goldKeep;
			BountyBoard.reset();
			Dungeon.generatedLevels.clear();
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
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
