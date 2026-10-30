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

import static org.junit.Assert.assertArrayEquals;
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
			Cycle.number = 3;
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
		Cycle.number = 3;   //every tier open
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
		assertEquals( 6.05f, EndgameLoot.zoneRating( 1, 0 ), 0.001f );
		assertEquals( 6.1f, EndgameLoot.zoneRating( 2, 0 ), 0.001f );
		assertEquals( "each clearing is a twentieth of a tier more", 6.2f, EndgameLoot.zoneRating( 2, 2 ), 0.001f );
		assertEquals( 6.55f, EndgameLoot.zoneRating( 2, 9 ), 0.001f );
		assertEquals( 6.1f, EndgameLoot.minesRating( 56 ), 0.001f );
		assertEquals( 6.26f, EndgameLoot.minesRating( 64 ), 0.001f );
		assertEquals( 6.0f, EndgameLoot.ASCENT_RATING, 0.001f );
		assertEquals( 6.15f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.RATING, 0.001f );
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
		Cycle.number = 3;   //tiers 7 and 8 are this cycle's
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
								if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.SetTier && ((xyz.gabriwar.warpedpixeldungeon.items.SetTier)it).setTier() >= 7 && ((xyz.gabriwar.warpedpixeldungeon.items.SetTier)it).setTier() <= 8) found8++; else wrong++;
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
		assertTrue( "a side branch of the dungeon too", Cycle.powerAt( 1, 2, 22 ) > 1.5f );
		assertEquals( "but not the mines (depth 56+)", 1f, Cycle.powerAt( 1, 0, 60 ), 0f );
		assertEquals( "not the surface", 1f, Cycle.powerAt( 5, 0, 97 ), 0f );
		assertEquals( "not a plaza zone", 1f, Cycle.powerAt( 3, 105, 12 ), 0f );
		assertEquals( "not the extra stages past the bottom", 1f, Cycle.powerAt( 3, 0, 28 ), 0f );
		assertEquals( 1f, Cycle.skillAt( 0, 0, 8 ), 0f );
		assertEquals( 1f, Cycle.expFactorAt( 0, 0, 8 ), 0f );
		//the loot ladder: tier 6 in the first run and the first new cycle, then a tier higher each cycle
		assertEquals( 6.0f, Cycle.lootRating( 0, 5 ), 0.001f );
		assertEquals( 6.0f, Cycle.lootRating( 1, 1 ), 0.001f );
		assertEquals( 6.0f, Cycle.lootRating( 1, 26 ), 0.001f );
		assertEquals( 6.0f, Cycle.lootRating( 2, 1 ), 0.001f );
		assertEquals( 6.9f, Cycle.lootRating( 2, 26 ), 0.001f );
		assertEquals( 7.0f, Cycle.lootRating( 3, 1 ), 0.001f );
		assertEquals( 8.0f, Cycle.lootRating( 4, 1 ), 0.001f );
		assertEquals( 9.0f, Cycle.lootRating( 5, 1 ), 0.001f );
		assertTrue( Cycle.lootRating( 2, 5 ) < Cycle.lootRating( 2, 20 ) );
		assertTrue( Cycle.lootRating( 3, 5 ) > Cycle.lootRating( 2, 5 ) );
		assertEquals( 9f, Cycle.lootRating( 9, 26 ), 0f );

		//the shared enemy-strength function carries it
		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		try {
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 8;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( Cycle.powerAt( 1, 0, 8 ), xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.statModifier( rat ), 0.0001f );
			assertEquals( Cycle.skillAt( 1, 0, 8 ), xyz.gabriwar.warpedpixeldungeon.actors.buffs.AscensionChallenge.skillModifier( rat ), 0.0001f );
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
			assertEquals( Cycle.powerAt( 1, 0, 7 ) * 1.3f, Cycle.power( rat ), 0.0001f );
			xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.apply( level, xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.NONE );
			assertEquals( Cycle.powerAt( 1, 0, 7 ), Cycle.power( rat ), 0.0001f );

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
				assertTrue( "event " + climb.climbEvent, climb.climbEvent >= 0 && climb.climbEvent <= 9 );
			}
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.ascentRebuilds.clear();
			Dungeon.generatedLevels.clear();
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
			assertFalse( "tier 8 is not open in the first new cycle", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate() ) );
			Cycle.number = 2;
			assertTrue( Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate() ) );
			assertFalse( "tier 9 waits for the second cycle", Reforge.canAwaken( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate() ) );
			Cycle.number = 3;
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
		assertEquals( 2, BossMutation.count( 1 ) );
		assertEquals( 3, BossMutation.count( 2 ) );
		assertEquals( 4, BossMutation.count( 3 ) );
		assertEquals( 4, BossMutation.count( 9 ) );
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
					assertEquals( "cycle " + cycle + " mutations", BossMutation.count( cycle ), Integer.bitCount( m.mask ) );
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
	public void newCycleEnemiesStartAtTheOldBottomAndGrowSlowly() {
		float[] typical = new float[1];
		//floor 1 of the first new cycle matches floor 24 of the last run: a rat of 6 + 3.6 x 1 is brought up to 6 + 3.6 x 24
		assertEquals( 6f + 3.6f * 24f, (6f + 3.6f * 1f) * Cycle.powerAt( 1, 0, 1 ), 0.01f );
		assertEquals( 9.6f, Cycle.powerAt( 1, 0, 1 ), 0.1f );
		//from there they only get stronger, and slowly: every floor a little more than the last
		float last = 0f;
		for (int d = 1; d <= 26; d++) {
			float strength = (6f + 3.6f * d) * Cycle.powerAt( 1, 0, d );
			assertTrue( "floor " + d + " is stronger than " + (d - 1), strength > last );
			if (d > 1) assertTrue( "floor " + d + " is a small step: " + (strength / last), strength / last < 1.08f );
			last = strength;
		}
		//the last floor of the first new cycle is above the old bottom, but not far
		float top = (6f + 3.6f * 24f) * Cycle.powerAt( 1, 0, 24 );
		assertTrue( "floor 24 of cycle 1 is " + (top / (6f + 3.6f * 24f)) + " times the old one", top > 1.5f * (6f + 3.6f * 24f) && top < 2.2f * (6f + 3.6f * 24f) );
		//the next cycle starts higher than the last one did, and never falls back to a low start
		for (int d = 1; d <= 26; d++) {
			assertTrue( "cycle 2 over cycle 1 at floor " + d, Cycle.powerAt( 2, 0, d ) * (6f + 3.6f * d) > Cycle.powerAt( 1, 0, d ) * (6f + 3.6f * d) );
			assertTrue( Cycle.powerAt( 3, 0, d ) > Cycle.powerAt( 2, 0, d ) );
		}
		assertEquals( "cycle 2 floor 1 matches depth 44, just past where cycle 1 ended", 6f + 3.6f * 44f, (6f + 3.6f * 1f) * Cycle.powerAt( 2, 0, 1 ), 0.01f );
		for (int c = 1; c <= 6; c++) {
			assertTrue( "cycle " + (c + 1) + " must not start weaker than cycle " + c + " ended",
					(6f + 3.6f * 1f) * Cycle.powerAt( c + 1, 0, 1 ) >= (6f + 3.6f * 24f) * Cycle.powerAt( c, 0, 24 ) );
			assertTrue( Cycle.lootRating( c + 1, 1 ) >= Cycle.lootRating( c, 24 ) );
		}
		//accuracy and experience follow
		assertEquals( 9f + 0.9f * 24f, (9f + 0.9f * 1f) * Cycle.skillAt( 1, 0, 1 ), 0.01f );
		assertTrue( "a rat's experience is brought up: " + Math.round( 1 * Cycle.expFactorAt( 1, 0, 1 ) ), Math.round( 1 * Cycle.expFactorAt( 1, 0, 1 ) ) >= 10 );
		assertTrue( Cycle.expFactorAt( 1, 0, 1 ) > Cycle.expFactorAt( 1, 0, 24 ) );
		assertTrue( Cycle.expFactorAt( 1, 0, 24 ) > 1f );
		//and the mob sees the factor for its own floor
		int nk = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		try {
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 1;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( Cycle.expFactorAt( 1, 0, 1 ), Cycle.expFactor( rat ), 0.0001f );
			Dungeon.branch = 0; Dungeon.depth = 40;
			assertEquals( 1f, Cycle.expFactor( rat ), 0f );
		} finally {
			Cycle.number = nk; Dungeon.branch = bk; Dungeon.depth = dk;
		}
	}

	@Test
	public void aCrownedArmorKeepsTheSpecialEffects() throws Exception {
		Hero h = Dungeon.hero;
		xyz.gabriwar.warpedpixeldungeon.items.armor.Armor keep = h.belongings.armor;
		try {
			xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire robe = new xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire();
			robe.level( 5 );
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor crowned = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, robe );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire.class, crowned.origin );
			h.belongings.armor = crowned;
			assertTrue( "still proof against fire", h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning.class ) );
			assertFalse( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bleeding.class ) );
			assertEquals( "its armor bonus carries", robe.DRMax( 5 ), crowned.DRMax( 5 ) );
			assertEquals( "and its Strength", robe.STRReq( 5 ), crowned.STRReq( 5 ) );
			//the other special armors
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor bleed = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, new xyz.gabriwar.warpedpixeldungeon.items.armor.BleedingArmor() );
			h.belongings.armor = bleed;
			assertTrue( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Bleeding.class ) );
			assertFalse( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning.class ) );
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor shatter = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, new xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfShattering() );
			h.belongings.armor = shatter;
			assertTrue( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Frost.class ) );
			assertTrue( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Chill.class ) );
			//the thorns and the rest carry as the armor's own effect, still there to be run
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor thorn = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, new xyz.gabriwar.warpedpixeldungeon.items.armor.Thornmail() );
			assertTrue( thorn.proto() instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Thornmail );

			//a crowned tier 7 piece still counts toward its set, and still shows and asks for its tier
			xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate t7 = new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate();
			t7.level( 3 );
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor c7 = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, t7 );
			h.belongings.armor = keep;
			int before = xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T7];
			h.belongings.armor = c7;
			assertEquals( "the set still counts it, one more than without it", before + 1, xyz.gabriwar.warpedpixeldungeon.items.GearSets.pieceCounts( h )[xyz.gabriwar.warpedpixeldungeon.items.GearSets.FIRST_T7] );
			assertEquals( 7, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( c7, 3 ) );
			assertEquals( t7.STRReq( 3 ), c7.STRReq( 3 ) );
			assertEquals( t7.DRMax( 3 ), c7.DRMax( 3 ) );
			assertTrue( c7.desc().contains( "Ascended Warlord's Regalia" ) );

			//it saves and loads with its origin
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			c7.storeInBundle( b );
			xyz.gabriwar.warpedpixeldungeon.items.armor.WarriorArmor back = new xyz.gabriwar.warpedpixeldungeon.items.armor.WarriorArmor();
			back.restoreFromBundle( b );
			assertEquals( xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate.class, back.origin );

			//an ordinary armor made into class armor has no origin and no effects
			xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor plain = xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, new xyz.gabriwar.warpedpixeldungeon.items.armor.ClothArmor() );
			assertNull( plain.origin );
			h.belongings.armor = plain;
			assertFalse( h.isImmune( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Burning.class ) );
		} finally {
			h.belongings.armor = keep;
		}
	}

	@Test
	public void theNewGearAsksForStrengthByItsTier() throws Exception {
		//weapons 18, 20, 22, 24: armors 17, 19, 21, 23
		assertEquals( 18, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword().STRReq( 0 ) );
		assertEquals( 20, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword().STRReq( 0 ) );
		assertEquals( 22, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MythicWarlordsGreatsword().STRReq( 0 ) );
		assertEquals( 24, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.GodforgedWarlordsGreatsword().STRReq( 0 ) );
		assertEquals( 24, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.YogForgedBlade().STRReq( 0 ) );
		assertEquals( 17, new xyz.gabriwar.warpedpixeldungeon.items.armor.WarlordsPlate().STRReq( 0 ) );
		assertEquals( 19, new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate().STRReq( 0 ) );
		assertEquals( 21, new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate().STRReq( 0 ) );
		assertEquals( 23, new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate().STRReq( 0 ) );
		assertEquals( 23, new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate().STRReq( 0 ) );
		assertEquals( "a mage's staff too", 18, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.ArchmagesStaff().STRReq( 0 ) );
		assertEquals( 24, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.GodforgedArchmagesStaff().STRReq( 0 ) );
		assertEquals( "the general pool is tier 5", 16, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dawnbreaker().STRReq( 0 ) );
		assertEquals( 15, new xyz.gabriwar.warpedpixeldungeon.items.armor.RobeOfFire().STRReq( 0 ) );
		assertEquals( "the lighter plate", 13, new xyz.gabriwar.warpedpixeldungeon.items.armor.Duskplate().STRReq( 0 ) );
		//a level still lowers it, as it always did
		assertEquals( 15, new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword().STRReq( 6 ) );
		//and a higher tier piece at its starting level still asks for more than a lower tier one at its own
		assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword().STRReq( 4 ) > new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword().STRReq( 2 ) );
		assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.GodforgedWarlordsGreatsword().STRReq( 8 ) > new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MythicWarlordsGreatsword().STRReq( 6 ) );

		//the tier the text shows
		assertEquals( 6, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.WarlordsGreatsword(), 5 ) );
		assertEquals( 7, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword(), 5 ) );
		assertEquals( 8, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate(), 5 ) );
		assertEquals( 9, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate(), 5 ) );
		assertEquals( "anything else keeps its own", 3, xyz.gabriwar.warpedpixeldungeon.items.GearSets.tierLabel( new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Sword(), 3 ) );
		xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword g = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.AscendedWarlordsGreatsword();
		g.identify();
		assertTrue( g.info(), g.info().contains( "tier-7" ) );
		xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate p = new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate();
		p.identify();
		assertTrue( p.info(), p.info().contains( "tier-8" ) );
		xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Sword s = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Sword();
		s.identify();
		assertTrue( s.info().contains( "tier-3" ) );

		//and every weapon and armor of the new gear follows the rule, with none left behind
		int checked = 0;
		for (String name : all()) {
			Object o = Class.forName( P + name ).getDeclaredConstructor().newInstance();
			if (!(o instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Armor) && !(o instanceof xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MeleeWeapon)) continue;
			Item it = (Item)o;
			int tier = xyz.gabriwar.warpedpixeldungeon.items.GearSets.gearTier( it );
			assertTrue( name + " has no tier", tier >= 5 && tier <= 9 );
			int want = (it instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Armor ? 17 : 18) + 2 * (tier - 6);
			if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Duskplate) want -= 2;
			int got = it instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.Armor ? ((xyz.gabriwar.warpedpixeldungeon.items.armor.Armor)it).STRReq( 0 ) : ((xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.MeleeWeapon)it).STRReq( 0 );
			assertEquals( name + " (tier " + tier + ")", want, got );
			checked++;
		}
		assertTrue( "weapons and armors checked: " + checked, checked >= 69 );
	}

	@Test
	public void theTierLadderFollowsTheCycles() {
		int keep = Cycle.number;
		try {
			//which tier is the most that can drop
			Cycle.number = 0; assertEquals( 7, EndgameLoot.maxTier() );
			Cycle.number = 1; assertEquals( 7, EndgameLoot.maxTier() );
			Cycle.number = 2; assertEquals( 8, EndgameLoot.maxTier() );
			Cycle.number = 3; assertEquals( 9, EndgameLoot.maxTier() );
			Cycle.number = 8; assertEquals( 9, EndgameLoot.maxTier() );
			//the share of drops that are class pieces (the rest are the plain tier 5 pool) grows with the cycle
			Cycle.number = 0; assertEquals( 0.30f, EndgameLoot.classShare(), 0.001f );
			Cycle.number = 1; assertEquals( 0.30f, EndgameLoot.classShare(), 0.001f );
			Cycle.number = 2; assertEquals( 0.60f, EndgameLoot.classShare(), 0.001f );
			Cycle.number = 3; assertEquals( 0.85f, EndgameLoot.classShare(), 0.001f );
			Cycle.number = 4; assertEquals( 1.00f, EndgameLoot.classShare(), 0.001f );

			//cycle 1 (and the first run): floors pay tier 6 and never 7; a boss has a low chance of 7
			for (int c = 0; c <= 1; c++) {
				Cycle.number = c;
				for (int d : new int[]{ 1, 13, 24 }) for (int i = 0; i < 400; i++) assertEquals( 6, EndgameLoot.tierOfRating( Cycle.lootRating( c, d ) ) );
				int t7 = 0;
				for (int i = 0; i < 8000; i++) {
					int t = EndgameLoot.tierOfRating( Cycle.lootRating( c, 10 ) + 0.2f );
					assertTrue( "tier " + t, t == 6 || t == 7 );
					if (t == 7) t7++;
				}
				assertEquals( "a boss: 1 in 5 for tier 7", 0.20, t7 / 8000.0, 0.03 );
			}
			//cycle 2: tiers 6 to 7 through the floors, a boss's chance of 8
			Cycle.number = 2;
			for (int i = 0; i < 300; i++) assertEquals( 6, EndgameLoot.tierOfRating( Cycle.lootRating( 2, 1 ) ) );
			int late7 = 0, boss8 = 0;
			for (int i = 0; i < 8000; i++) {
				int t = EndgameLoot.tierOfRating( Cycle.lootRating( 2, 24 ) );
				assertTrue( t == 6 || t == 7 );
				if (t == 7) late7++;
				if (EndgameLoot.tierOfRating( Cycle.lootRating( 2, 24 ) + 0.2f ) == 8) boss8++;
			}
			assertEquals( "late cycle 2 floors are mostly tier 7", 0.83, late7 / 8000.0, 0.04 );
			assertTrue( "a cycle 2 boss can reach tier 8: " + boss8, boss8 > 0 );
			//cycle 3: 7 to 8 (a boss's chance of 9), cycle 4: 8 to 9
			Cycle.number = 3;
			for (int i = 0; i < 300; i++) assertEquals( 7, EndgameLoot.tierOfRating( Cycle.lootRating( 3, 1 ) ) );
			int boss9 = 0;
			for (int i = 0; i < 8000; i++) if (EndgameLoot.tierOfRating( Cycle.lootRating( 3, 24 ) + 0.2f ) == 9) boss9++;
			assertTrue( "a cycle 3 boss can reach tier 9: " + boss9, boss9 > 0 );
			Cycle.number = 4;
			for (int i = 0; i < 300; i++) assertEquals( 8, EndgameLoot.tierOfRating( Cycle.lootRating( 4, 1 ) ) );
			Cycle.number = 5;
			for (int i = 0; i < 300; i++) assertEquals( 9, EndgameLoot.tierOfRating( Cycle.lootRating( 5, 12 ) ) );

			//the first run: no Yog-Forged, no tier 8 or 9, whatever the source
			Cycle.number = 0;
			Hero h = Dungeon.hero;
			Set<String> forbidden = new HashSet<>();
			for (String n : YOG_ITEMS) forbidden.add( P + n );
			for (String n : TIER8_ITEMS) forbidden.add( P + n );
			for (String n : TIER9_ITEMS) forbidden.add( P + n );
			for (int i = 0; i < 20000; i++) assertFalse( forbidden.contains( EndgameLoot.rollRated( 9.5f ).getClass().getName() ) );
			for (int i = 0; i < 300; i++) assertFalse( forbidden.contains( EndgameLoot.rollYog().getClass().getName() ) );
			//the old sources, in the first run: tier 6 with a small chance of 7
			assertEquals( 6.0f, EndgameLoot.ASCENT_RATING, 0.001f );
			assertTrue( EndgameLoot.zoneRating( 2, 0 ) < 6.2f );
			assertTrue( EndgameLoot.minesRating( 64 ) <= 6.4f && EndgameLoot.minesRating( 56 ) >= 6.1f );
			assertEquals( 6.15f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.ratingAt( 0, 16 ), 0.001f );
			assertEquals( 6.35f, xyz.gabriwar.warpedpixeldungeon.levels.SideStages.bossRatingAt( 0, 25 ), 0.001f );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.SideStages.ratingAt( 2, 16 ) > xyz.gabriwar.warpedpixeldungeon.levels.SideStages.ratingAt( 1, 16 ) );
			Cycle.number = 1;
			for (int i = 0; i < 300; i++) assertTrue( EndgameLoot.tierOfRating( xyz.gabriwar.warpedpixeldungeon.levels.SideStages.bossRatingAt( 1, 25 ) ) <= 7 );

			//the item text says where each high tier turns up
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.MythicWarlordsPlate().desc().contains( "second new cycle" ) );
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.GodforgedWarlordsPlate().desc().contains( "third new cycle" ) );
			assertTrue( new xyz.gabriwar.warpedpixeldungeon.items.armor.YogForgedPlate().desc().contains( "third new cycle" ) );
			assertFalse( new xyz.gabriwar.warpedpixeldungeon.items.armor.AscendedWarlordsPlate().desc().contains( "new cycle" ) );
		} finally {
			Cycle.number = keep;
		}
	}

	@Test
	public void everyFiveFloorsHoldAnEvent() {
		//a block of five floors always has exactly one forced floor, never a boss floor, whatever the seed and cycle
		int[] perIndex = new int[4];
		for (int cycle = 1; cycle <= 6; cycle++) {
			for (long seed = 1; seed <= 150; seed++) {
				for (int block = 0; block < 5; block++) {
					int found = 0, at = -1;
					for (int depth = block * 5 + 1; depth <= block * 5 + 5; depth++) {
						if (xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.forced( seed, cycle, depth )) { found++; at = depth; }
					}
					assertEquals( "cycle " + cycle + " seed " + seed + " block " + block, 1, found );
					assertTrue( "never the boss floor", at % 5 != 0 );
					if (cycle == 1 && block == 0) perIndex[at - 1]++;
				}
			}
		}
		for (int i = 0; i < 4; i++) assertTrue( "floor " + (i + 1) + " is chosen " + perIndex[i] + " of 150 times", perIndex[i] > 15 && perIndex[i] < 60 );
		assertFalse( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.forced( 7, 1, 25 ) );
		assertFalse( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.forced( 7, 1, 26 ) );
		assertFalse( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.eligible( 1, 12 ) );   //a side branch
		assertFalse( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.eligible( 0, 10 ) );   //a boss floor
		//and the events picked for them are real ones
		for (int i = 0; i < 2000; i++) assertTrue( xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.pickEvent() >= 1 && xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.pickEvent() <= 9 );

		//in a real build of the floors: the forced floor of each block holds an event, on the way down, every time
		int keep = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		try {
			Cycle.number = 1; Dungeon.branch = 0;
			for (int block = 0; block < 5; block++) {
				int forcedFloor = -1;
				for (int depth = block * 5 + 1; depth <= block * 5 + 4; depth++) if (xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.forced( Dungeon.seed, 1, depth )) forcedFloor = depth;
				assertTrue( forcedFloor > 0 );
				Dungeon.generatedLevels.clear();
				Dungeon.depth = forcedFloor;
				Level level = Dungeon.newLevel();
				assertTrue( "floor " + forcedFloor + " has event " + level.climbEvent, level.climbEvent != xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.NONE );
			}
			//the others: sometimes
			int with = 0, without = 0;
			for (int i = 0; i < 40; i++) {
				int depth = 2;
				while (xyz.gabriwar.warpedpixeldungeon.levels.ClimbEvents.forced( Dungeon.seed, 1, depth ) || depth % 5 == 0) depth++;
				Dungeon.generatedLevels.clear();
				Dungeon.depth = depth;
				if (Dungeon.newLevel().climbEvent != 0) with++; else without++;
			}
			assertTrue( "events on some of the other floors: " + with + "/" + without, with > 0 && without > 0 );
		} finally {
			Cycle.number = keep; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void eliteEnemiesHoldTheFloorsAndDropNoGear() {
		int keep = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Pacts.reset();
		Dungeon.generatedLevels.clear();
		try {
			Cycle.number = 0;
			assertFalse( "no elites in the first run", xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.floorHasElites( 0, 7 ) );
			Cycle.number = 1;
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.floorHasElites( 0, 7 ) );
			assertFalse( "not the boss floors", xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.floorHasElites( 0, 5 ) );
			assertFalse( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.floorHasElites( 0, 25 ) );
			assertFalse( "not a side branch", xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.floorHasElites( 1, 17 ) );
			assertEquals( 1, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.count( 1, 1 ) );
			assertEquals( 2, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.count( 1, 12 ) );
			assertEquals( 3, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.count( 1, 20 ) );
			assertEquals( "more in later cycles", 5, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.count( 3, 20 ) );
			Pacts.set( Pacts.Pact.ELITE_PACK, true );
			assertEquals( "the elite pack", 3, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.count( 1, 1 ) );
			Pacts.reset();

			//a real floor of cycle 1 holds elites: tougher, with an aura, paying more experience
			int elitesSeen = 0;
			for (int rep = 0; rep < 6; rep++) {
				Dungeon.generatedLevels.clear();
				Dungeon.branch = 0; Dungeon.depth = 7;
				Level level = Dungeon.newLevel();
				for (xyz.gabriwar.warpedpixeldungeon.actors.mobs.Mob m : level.mobs) {
					if (m.buff( xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.class ) == null) continue;
					elitesSeen++;
					assertEquals( m.HT, m.HP );
					assertFalse( "an elite has an aura", m.buffs( xyz.gabriwar.warpedpixeldungeon.actors.buffs.ChampionEnemy.class ).isEmpty() );
					assertEquals( "and only one", 1, m.buffs( xyz.gabriwar.warpedpixeldungeon.actors.buffs.ChampionEnemy.class ).size() );
					assertTrue( "elite experience " + m.EXP, m.EXP >= 3 );
				}
			}
			assertTrue( "elites on the floors: " + elitesSeen, elitesSeen >= 6 );

			//what an elite leaves: gold or a shard, never gear
			Dungeon.generatedLevels.clear();
			Dungeon.depth = 9;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			int gold = 0, shard = 0, n = 300;
			for (int i = 0; i < n; i++) {
				xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
				xyz.gabriwar.warpedpixeldungeon.actors.buffs.Buff.affect( rat, xyz.gabriwar.warpedpixeldungeon.actors.buffs.Elite.class );
				rat.pos = emptyCell( level );
				rat.rollToDropLoot();
				xyz.gabriwar.warpedpixeldungeon.items.Heap heap = level.heaps.get( rat.pos );
				if (heap == null) continue;
				for (Item it : heap.items) {
					assertTrue( "an elite dropped " + it.getClass().getSimpleName(),
							it instanceof xyz.gabriwar.warpedpixeldungeon.items.Gold || it instanceof xyz.gabriwar.warpedpixeldungeon.items.CycleShard );
					if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.Gold) gold++; else shard++;
				}
			}
			assertEquals( "gold about six in ten", 0.60, gold / (double)n, 0.10 );
			assertEquals( "a shard about one in eight", 0.125, shard / (double)n, 0.07 );
		} finally {
			Pacts.reset();
			Cycle.number = keep; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void bossesAreToughestAndPactsOnlyHardenTheDungeon() {
		int keep = Cycle.number, bk = Dungeon.branch, dk = Dungeon.depth;
		Level lvKeep = Dungeon.level;
		Pacts.reset();
		Dungeon.generatedLevels.clear();
		try {
			assertEquals( 1.5f, Cycle.bossBoost( 1 ), 0.001f );
			assertEquals( 1.75f, Cycle.bossBoost( 2 ), 0.001f );
			assertEquals( 3.0f, Cycle.bossBoost( 9 ), 0.001f );
			Cycle.number = 1; Dungeon.branch = 0; Dungeon.depth = 5;
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo goo = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Goo();
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			assertEquals( "a boss is tougher than the floor's ordinary enemies", Cycle.powerAt( 1, 0, 5 ) * 1.5f, Cycle.power( goo ), 0.001f );
			assertEquals( Cycle.powerAt( 1, 0, 5 ), Cycle.power( rat ), 0.001f );

			//the pacts: off until chosen, and each only hardens the dungeon
			assertEquals( 0, Pacts.activeCount() );
			assertEquals( 0f, Pacts.lootBonus(), 0f );
			float floorRating = Cycle.lootRating( 1, 5 );
			float base = Cycle.powerAt( 1, 0, 5 );
			Pacts.set( Pacts.Pact.HARDENED, true );
			assertEquals( base * 1.25f, Cycle.power( rat ), 0.001f );
			Pacts.set( Pacts.Pact.SWIFT, true );
			assertEquals( 1.25f, Cycle.enemySpeed( rat ), 0.001f );
			Pacts.set( Pacts.Pact.WARLORD, true );
			assertEquals( "every boss carries all four", 4, BossMutation.count( 1 ) );
			Pacts.set( Pacts.Pact.CROWDED, true );
			assertEquals( 3, Pacts.extraMobs( 10 ) );
			Pacts.set( Pacts.Pact.HAUNTED, true );
			assertEquals( 0.2f, Pacts.eventBonus(), 0.001f );
			assertEquals( 5, Pacts.activeCount() );
			assertEquals( "each vow is a tenth of a tier", floorRating + 0.5f, Cycle.lootRating( 1, 5 ), 0.001f );
			assertEquals( 5, Pacts.shardBonus() );
			Cycle.number = 0;
			assertEquals( "no pact outside a new cycle", 1f, Cycle.power( rat ), 0f );
			assertEquals( 1f, Cycle.enemySpeed( rat ), 0f );
			Cycle.number = 1;
			Dungeon.depth = 40;
			assertEquals( "nor outside the dungeon", 1f, Cycle.enemySpeed( rat ), 0f );
			Dungeon.depth = 5;

			//crowded floors: more enemies on a floor
			Dungeon.depth = 7;
			Level level = Dungeon.newLevel();
			Dungeon.level = level;
			int before = level.mobs.size();
			Pacts.crowd( level );
			assertTrue( "crowded: " + before + " -> " + level.mobs.size(), level.mobs.size() > before );
			Pacts.set( Pacts.Pact.CROWDED, false );
			before = level.mobs.size();
			Pacts.crowd( level );
			assertEquals( before, level.mobs.size() );

			//the pacts are saved with the run
			Pacts.reset();
			Pacts.set( Pacts.Pact.SWIFT, true );
			Pacts.set( Pacts.Pact.RESTLESS, true );
			com.watabou.utils.Bundle b = new com.watabou.utils.Bundle();
			Dungeon.storeAscentState( b );
			Pacts.reset();
			assertEquals( 0, Pacts.activeCount() );
			Dungeon.restoreAscentState( b );
			assertTrue( Pacts.isOn( Pacts.Pact.SWIFT ) );
			assertTrue( Pacts.isOn( Pacts.Pact.RESTLESS ) );
			assertFalse( Pacts.isOn( Pacts.Pact.HARDENED ) );
			Dungeon.restoreAscentState( new com.watabou.utils.Bundle() );
			assertEquals( "an old save has no pacts", 0, Pacts.activeCount() );
		} finally {
			Pacts.reset();
			Cycle.number = keep; Dungeon.branch = bk; Dungeon.depth = dk;
			Dungeon.level = lvKeep;
			Dungeon.generatedLevels.clear();
		}
	}

	@Test
	public void candlesAreSeparateAndDropOneAtATime() {
		Hero h = Dungeon.hero;
		int packSize = h.belongings.backpack.items.size();
		try {
			//a new candle does not stack, so four stay four separate items
			xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle one = new xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle();
			assertFalse( "candles must not stack", one.stackable );
			for (int i = 0; i < 4; i++) new xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle().collect( h.belongings.backpack );
			int candles = 0;
			for (Item it : h.belongings.backpack.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle) { candles++; assertEquals( 1, it.quantity() ); }
			assertEquals( "four candles, four items", 4, candles );

			//taking one out of the pack takes just that one
			xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle first = null;
			for (Item it : h.belongings.backpack.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle) { first = (xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle)it; break; }
			Item taken = first.takeOne( h );
			assertEquals( 1, taken.quantity() );
			int left = 0;
			for (Item it : h.belongings.backpack.items) if (it instanceof xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle) left++;
			assertEquals( 3, left );

			//an old save may still hold a stack: dropping it still lets go of one candle only
			xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle stack = new xyz.gabriwar.warpedpixeldungeon.items.quest.CeremonialCandle();
			stack.quantity( 4 );
			h.belongings.backpack.items.add( stack );
			Item one2 = stack.takeOne( h );
			assertEquals( "one candle comes off the stack", 1, one2.quantity() );
			assertEquals( "and three stay", 3, stack.quantity() );
			assertTrue( h.belongings.backpack.items.contains( stack ) );
		} finally {
			while (h.belongings.backpack.items.size() > packSize) h.belongings.backpack.items.remove( h.belongings.backpack.items.size() - 1 );
		}
	}

	@Test
	public void newCyclesDoNotDropTheKeysAndBagsAgain() {
		Hero h = Dungeon.hero;
		int keep = Cycle.number;
		Dungeon.LimitedDrops.reset();
		try {
			//the keys and bags found in the last run: found, and not to be found again
			Dungeon.LimitedDrops.SEWER_KEY.drop();
			Dungeon.LimitedDrops.PRISON_KEY.drop();
			Dungeon.LimitedDrops.TENGU_KEY.drop();
			Dungeon.LimitedDrops.KEY_RING.drop();
			Dungeon.LimitedDrops.ARCHERS_QUIVER.drop();
			//and some limited drops that should come back
			Dungeon.LimitedDrops.STRENGTH_POTIONS.drop();
			Dungeon.LimitedDrops.UPGRADE_SCROLLS.drop();
			assertFalse( "never found: the caves key", Dungeon.LimitedDrops.CAVES_KEY.dropped() );

			Cycle.number = 1;
			Cycle.resetDungeonState( h );

			for (Dungeon.LimitedDrops d : new Dungeon.LimitedDrops[]{ Dungeon.LimitedDrops.SEWER_KEY, Dungeon.LimitedDrops.PRISON_KEY,
					Dungeon.LimitedDrops.TENGU_KEY, Dungeon.LimitedDrops.KEY_RING, Dungeon.LimitedDrops.ARCHERS_QUIVER }) {
				assertTrue( d + " must not drop again", d.dropped() );
			}
			assertFalse( "a key never found can still be found", Dungeon.LimitedDrops.CAVES_KEY.dropped() );
			assertFalse( "strength potions come back", Dungeon.LimitedDrops.STRENGTH_POTIONS.dropped() );
			assertFalse( "upgrade scrolls come back", Dungeon.LimitedDrops.UPGRADE_SCROLLS.dropped() );
			//every key and bag is on the list
			java.util.Set<Dungeon.LimitedDrops> once = new java.util.HashSet<>( java.util.Arrays.asList( Cycle.ONCE_ONLY ) );
			for (Dungeon.LimitedDrops d : Dungeon.LimitedDrops.values()) {
				if (d.name().endsWith( "_KEY" ) || d == Dungeon.LimitedDrops.KEY_RING) assertTrue( d + " is a key", once.contains( d ) );
			}
		} finally {
			Dungeon.LimitedDrops.reset();
			Cycle.number = keep;
		}
	}

	@Test
	public void theMonumentStandsOnOpenGroundNearTheArrival() {
		int cell = xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_MONUMENT;
		int arrival = xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_PLAZA;
		int[] grid = xyz.gabriwar.warpedpixeldungeon.levels.TownLayouts.TOWN_LAYOUT_REMIXED;
		assertTrue( cell >= 0 && cell < 1024 );
		int dx = Math.abs( cell % 32 - arrival % 32 ), dy = Math.abs( cell / 32 - arrival / 32 );
		assertTrue( "within a few steps of where you arrive: " + dx + "," + dy, Math.max( dx, dy ) >= 2 && Math.max( dx, dy ) <= 4 );
		assertTrue( "on ground you can reach", (xyz.gabriwar.warpedpixeldungeon.levels.Terrain.flags[grid[cell]] & xyz.gabriwar.warpedpixeldungeon.levels.Terrain.PASSABLE) != 0 );
		//open all round, so it never blocks a path or walls anything in
		for (int ddy = -1; ddy <= 1; ddy++) for (int ddx = -1; ddx <= 1; ddx++) {
			int c = cell + ddx + ddy * 32;
			assertTrue( "neighbour " + ddx + "," + ddy, (xyz.gabriwar.warpedpixeldungeon.levels.Terrain.flags[grid[c]] & xyz.gabriwar.warpedpixeldungeon.levels.Terrain.PASSABLE) != 0 );
		}
		//and on nothing else the town already uses
		int[] taken = { arrival, xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_MINE_GATE, xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_DUNGEON_GATE,
				564, 523, 176, 324, 366, 247, 687, 689, 174, 172, 525, 357, 214, 756,
				131, 197, 107, 289, 283, 732, 891, 740, 1022 };
		for (int c : taken) assertNotEquals( "taken by " + c, c, cell );
		for (int d : xyz.gabriwar.warpedpixeldungeon.levels.overworld.WorldStructures.TOWN_DOORS) assertNotEquals( "a door", d, cell );

		//what it is
		xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.PlazaMonument m = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.npcs.PlazaMonument();
		assertEquals( xyz.gabriwar.warpedpixeldungeon.sprites.MonumentSprite.class, m.spriteClass );
		assertTrue( m.properties().contains( xyz.gabriwar.warpedpixeldungeon.actors.Char.Property.IMMOVABLE ) );
		assertTrue( m.name().contains( "monument" ) );
		assertTrue( m.description().contains( "Frostbound Vault" ) && m.description().contains( "dark mines" ) );
		assertTrue( "it stays through a level reset", m.reset() );
		int hp = m.HP;
		m.damage( 500, null );
		assertEquals( "nothing harms it", hp, m.HP );
		assertTrue( m.isAlive() );
	}

	@Test
	public void theGunslingerClassIsWiredIn() {
		xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass g = xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER;
		assertEquals( "the seventh class", 7, xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.values().length );
		assertEquals( "sprites/gunslinger.png", g.spritesheet() );
		assertEquals( "splashes/gunslinger.jpg", g.splashArt() );
		assertTrue( "open from the start", g.isUnlocked() );
		assertArrayEquals( new xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW, xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.DEMOLITIONIST }, g.subClasses() );
		assertEquals( Badges.Badge.MASTERY_GUNSLINGER, g.masteryBadge() );
		assertEquals( 3, g.armorAbilities().length );
		//its text
		assertEquals( "gunslinger", g.title() );
		assertTrue( g.desc(), g.desc().contains( "revolver" ) );
		assertEquals( "outlaw", xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW.title() );
		assertEquals( "demolitionist", xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.DEMOLITIONIST.title() );
		assertFalse( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW.desc().contains( "!!!" ) );

		//the revolver: six rounds, loaded, with a name and a description
		xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver r = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver();
		assertEquals( 6, r.maxRound() );
		assertEquals( 6, r.round() );
		assertEquals( "revolver", r.name() );
		assertTrue( r.desc().contains( "six-round" ) );

		//its coat, its talents and its skill tree
		Hero h = Dungeon.hero;
		xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass keep = h.heroClass;
		try {
			h.heroClass = g;
			assertTrue( "its crown armor", xyz.gabriwar.warpedpixeldungeon.items.armor.ClassArmor.upgrade( h, new xyz.gabriwar.warpedpixeldungeon.items.armor.ClothArmor() ) instanceof xyz.gabriwar.warpedpixeldungeon.items.armor.GunslingerArmor );
			java.util.ArrayList<java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer>> talents = new java.util.ArrayList<>();
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.initClassTalents( g, talents );
			assertTrue( "talents in tier 1", talents.size() >= 1 && !talents.get( 0 ).isEmpty() );
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills sk = xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.forHero( h );
			assertEquals( g, sk.heroClass );
			assertFalse( "a skill tree to spend points in", sk.passiveASkills.isEmpty() );
			assertFalse( sk.passiveBSkills.isEmpty() );
			assertFalse( sk.activeSkills.isEmpty() );
		} finally {
			h.heroClass = keep;
		}
	}

	@Test
	public void theGunslingerSkillTreeWorks() {
		Hero h = Dungeon.hero;
		xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass keepClass = h.heroClass;
		xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills keepSkills = h.heroSkills;
		xyz.gabriwar.warpedpixeldungeon.items.KindOfWeapon keepWeapon = h.belongings.weapon;
		int keepBullet = Dungeon.bullet;
		try {
			h.heroClass = xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER;
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills sk = xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.forHero( h );
			h.heroSkills = sk;
			//the shape of the tree
			assertEquals( 4, sk.passiveASkills.size() );
			assertEquals( 6, sk.passiveBSkills.size() );
			assertEquals( 5, sk.activeSkills.size() );
			assertEquals( 5, sk.fourthSkills.size() );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill s : sk.passiveBSkills) if (s instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.DoubleBarrel) assertTrue( s.exclusiveWith instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.LongBarrel );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill s : sk.fourthSkills) if (s instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.BulletTime) assertTrue( s.exclusiveWith instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.LastRound );
			assertEquals( 3, xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.subclassSkills( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW ).size() );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.subclassSkills( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW ).get( 0 ) instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.DoubleTap );
			assertTrue( xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.subclassSkills( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.DEMOLITIONIST ).get( 2 ) instanceof xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Shrapnel );
			//every skill has a name and a description of its own
			java.util.List<xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill> all = new java.util.ArrayList<>();
			all.addAll( sk.passiveASkills ); all.addAll( sk.passiveBSkills ); all.addAll( sk.activeSkills ); all.addAll( sk.fourthSkills );
			all.addAll( xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.subclassSkills( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW ) );
			all.addAll( xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.CurrentSkills.subclassSkills( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.DEMOLITIONIST ) );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill s : all) {
				String info = xyz.gabriwar.warpedpixeldungeon.messages.Messages.get( s, "desc" );
				assertFalse( s.getClass().getSimpleName() + " has no text", info.contains( "!!!" ) );
				assertTrue( s.getClass().getSimpleName() + " needs +1 +2 +3 lines", info.contains( "_+1:_" ) && info.contains( "_+3:_" ) );
			}

			//the gun hooks
			xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver r = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver();
			h.belongings.weapon = r;
			assertEquals( 6, r.maxRound() );
			assertEquals( 2f, r.reloadTime( h ), 0.001f );
			sk.passiveASkills.get( 2 ).level = 3;   //Deep Pockets
			assertEquals( "deep pockets: +3 rounds", 9, r.maxRound() );
			sk.passiveASkills.get( 1 ).level = 3;   //Quick Hands
			assertEquals( "quick hands: half the reload", 1f, r.reloadTime( h ), 0.001f );
			sk.passiveBSkills.get( 5 ).level = 1;   //Long Barrel
			assertEquals( "a long barrel reloads 20% slower", 1.2f, r.reloadTime( h ), 0.001f );
			sk.passiveBSkills.get( 5 ).level = 0;

			//Dead Calm: only the first shot after a full reload
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill calm = sk.passiveASkills.get( 3 );
			calm.level = 3;
			r.quickReload();
			assertTrue( r.round() >= r.maxRound() );
			assertEquals( 1.9f, calm.rangedDamageModifier(), 0.001f );
			assertTrue( calm.sureHit( null ) );
			r.useRound();
			assertEquals( 1f, calm.rangedDamageModifier(), 0.001f );
			assertFalse( calm.sureHit( null ) );

			//Last Round: only when one round is left
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill last = sk.fourthSkills.get( 4 );
			last.level = 3;
			assertEquals( 1f, last.rangedDamageModifier(), 0.001f );
			while (r.round() > 1) r.useRound();
			assertEquals( 5f, last.rangedDamageModifier(), 0.001f );
			assertTrue( last.sureHit( null ) );

			//Double Barrel: 4th, 3rd, 2nd shot
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill dbl = sk.passiveBSkills.get( 4 );
			dbl.level = 3;
			int twice = 0;
			for (int i = 0; i < 100; i++) if (dbl.doubleShot()) twice++;
			assertEquals( 50, twice );

			//Scavenger: kills leave bullets, but only with a firearm in hand
			xyz.gabriwar.warpedpixeldungeon.actors.hero.skills.Skill scav = sk.passiveBSkills.get( 2 );
			scav.level = 3;
			Dungeon.bullet = 0;
			for (int i = 0; i < 300; i++) scav.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat(), true );
			assertTrue( "bullets from kills: " + Dungeon.bullet, Dungeon.bullet > 150 );
			int got = Dungeon.bullet;
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dagger();
			for (int i = 0; i < 300; i++) scav.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat(), true );
			assertEquals( "no firearm, no bullets", got, Dungeon.bullet );

			//the shot hooks leave other weapons alone
			calm.level = 3; last.level = 3;
			assertEquals( 1f, calm.rangedDamageModifier(), 0.001f );
			assertEquals( 1f, last.rangedDamageModifier(), 0.001f );
			assertEquals( 1f, sk.passiveASkills.get( 0 ).toHitModifier(), 0.001f );
		} finally {
			h.heroClass = keepClass;
			h.heroSkills = keepSkills;
			h.belongings.weapon = keepWeapon;
			Dungeon.bullet = keepBullet;
		}
	}

	@Test
	public void theGunslingerTalentsAndCrownWork() {
		Hero h = Dungeon.hero;
		xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass keepClass = h.heroClass;
		java.util.ArrayList<java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer>> keepTalents = h.talents;
		xyz.gabriwar.warpedpixeldungeon.items.KindOfWeapon keepWeapon = h.belongings.weapon;
		int keepBullet = Dungeon.bullet, keepHP = h.HP, keepHT = h.HT;
		try {
			xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass g = xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroClass.GUNSLINGER;
			h.heroClass = g;
			//the class talents, tier by tier
			java.util.ArrayList<java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer>> t = new java.util.ArrayList<>();
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.initClassTalents( g, t );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.AMMO_BELT, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEADY_HANDS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.POWDER_HORN, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEEL_NERVES }) assertTrue( x + " tier 1", t.get( 0 ).containsKey( x ) );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.COOL_BARREL, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.RELOAD_ON_THE_RUN, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.HAIR_TRIGGER, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.POCKET_ROUNDS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMITHS_EYE }) assertTrue( x + " tier 2", t.get( 1 ).containsKey( x ) );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.CALLED_SHOTS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.OVERCHARGE_ROUNDS }) assertTrue( x + " tier 3", t.get( 2 ).containsKey( x ) );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DEAD_TO_RIGHTS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMOKE, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LEGEND_OF_THE_WEST, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LAST_BULLET }) {
				boolean found = false;
				for (java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer> m : t) if (m.containsKey( x )) found = true;
				assertTrue( x + " at level 40 or 60", found );
			}
			//its own, not the Duelist's
			assertFalse( t.get( 0 ).containsKey( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STRENGTHENING_MEAL ) );
			//the subclass talents
			java.util.ArrayList<java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer>> so = new java.util.ArrayList<>();
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.initSubclassTalents( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.OUTLAW, so );
			java.util.ArrayList<java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer>> sd = new java.util.ArrayList<>();
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.initSubclassTalents( xyz.gabriwar.warpedpixeldungeon.actors.hero.HeroSubClass.DEMOLITIONIST, sd );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.TRIGGER_HAPPY, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.BLOOD_MONEY, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DESPERADO }) { boolean f = false; for (java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer> m : so) if (m.containsKey( x )) f = true; assertTrue( x + " outlaw", f ); }
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : new xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[]{ xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.BIGGER_BANG, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SHRAPNEL_VEST, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SPENT_CASINGS }) { boolean f = false; for (java.util.LinkedHashMap<xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent, Integer> m : sd) if (m.containsKey( x )) f = true; assertTrue( x + " demolitionist", f ); }

			//every new talent has a title and a +1 description
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent[] mine = { xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.AMMO_BELT, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEADY_HANDS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.POWDER_HORN, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEEL_NERVES, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.COOL_BARREL, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.RELOAD_ON_THE_RUN, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.HAIR_TRIGGER, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.POCKET_ROUNDS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMITHS_EYE,
					xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.CALLED_SHOTS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.OVERCHARGE_ROUNDS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DEAD_TO_RIGHTS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMOKE, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LEGEND_OF_THE_WEST, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LAST_BULLET, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.TRIGGER_HAPPY, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.BLOOD_MONEY, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DESPERADO,
					xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.BIGGER_BANG, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SHRAPNEL_VEST, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SPENT_CASINGS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LONG_BURST, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.PIERCING_FIRE, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.FOCUSED_VOLLEY, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.THICK_SMOKE, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DECOY_SWARM, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GHOST_STEP, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEADY_STREAM, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.HOT_BARRELS, xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.COVERING_FIRE };
			assertEquals( 30, mine.length );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent x : mine) {
				assertFalse( x + " title", x.title().contains( "!!!" ) );
				assertTrue( x + " description: " + x.desc(), x.desc().contains( "_+1:_" ) );
			}

			//the crown abilities
			xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.ArmorAbility[] abs = g.armorAbilities();
			assertEquals( 3, abs.length );
			assertEquals( "barrage", abs[0].name() );
			assertEquals( "smoke and mirrors", abs[1].name() );
			assertEquals( "hail of lead", abs[2].name() );
			for (xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.ArmorAbility a : abs) {
				assertEquals( 4, a.talents().length );
				assertEquals( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.HEROIC_ENERGY, a.talents()[3] );
				assertFalse( a.desc().contains( "!!!" ) );
				assertFalse( a.shortDesc().contains( "!!!" ) );
			}

			//the numbers, with a firearm in hand and the points spent
			h.talents = new java.util.ArrayList<>();
			xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.initClassTalents( h );
			for (int i = 0; i < 6; i++) while (h.talents.size() <= i) h.talents.add( new java.util.LinkedHashMap<>() );
			xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver r = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.gun.Revolver();
			h.belongings.weapon = r;
			r.quickReload();
			h.HT = 100; h.HP = 100;
			assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			h.talents.get( 5 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LEGEND_OF_THE_WEST, 3 );
			assertEquals( "legend: +30%", 1.3f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			assertEquals( "and only for ranged", 1f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( false ), 0.001f );
			h.talents.get( 1 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.COOL_BARREL, 2 );
			assertEquals( "cool barrel on a full magazine", 1.3f * 1.16f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			h.talents.get( 2 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.OVERCHARGE_ROUNDS, 3 );
			while (r.round() > 1) r.useRound();
			assertEquals( "overcharge on the last round, no cool barrel on a nearly empty one", 1.3f * 1.45f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			h.HP = 40;
			h.talents.get( 5 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DESPERADO, 3 );
			assertEquals( "desperado below half health", 1.3f * 1.45f * 1.3f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			h.HP = 100;

			h.talents.get( 2 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.CALLED_SHOTS, 3 );
			assertEquals( 1.3f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.accuracyMod(), 0.001f );
			h.talents.get( 1 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.HAIR_TRIGGER, 2 );
			assertEquals( 1f / 1.2f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.shotSpeed(), 0.001f );
			h.talents.get( 0 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.POWDER_HORN, 2 );
			assertEquals( 5, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.refund( 10 ) );
			h.talents.get( 1 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMITHS_EYE, 2 );
			assertEquals( 2, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.levelBonus() );
			h.talents.get( 0 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.AMMO_BELT, 2 );
			Dungeon.bullet = 0;
			xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onFood( h );
			assertEquals( "ammo belt", 7, Dungeon.bullet );
			h.talents.get( 5 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.TRIGGER_HAPPY, 3 );
			int spared = 0;
			for (int i = 0; i < 3000; i++) if (xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.sparesRound()) spared++;
			assertEquals( "trigger happy: 15%", 0.15, spared / 3000.0, 0.04 );

			//a kill opens the window for quicker reloads, and a reload for the gunsmoke
			h.talents.get( 1 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.RELOAD_ON_THE_RUN, 2 );
			xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onKill( new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat(), true );
			assertEquals( "reload on the run", 0.5f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.reloadFactor(), 0.001f );
			h.talents.get( 4 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMOKE, 3 );   //a level 40 talent
			h.talents.get( 5 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SHRAPNEL_VEST, 3 );
			assertEquals( "the vest", 85, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.incoming( 100, null ) );
			xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onReloaded();
			assertEquals( "gunsmoke and the vest", 51, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.incoming( 100, null ) );

			//what a shot does to an enemy that has not noticed you, and to a nearly dead one
			xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat rat = new xyz.gabriwar.warpedpixeldungeon.actors.mobs.Rat();
			h.talents.get( 0 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.STEADY_HANDS, 2 );
			assertEquals( "steady hands: +50%", 150, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onHit( rat, 100, true ) );
			assertEquals( "not for a melee blow", 100, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onHit( rat, 100, false ) );
			h.talents.get( 4 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.DEAD_TO_RIGHTS, 3 );
			rat.HP = rat.HT / 4;
			assertEquals( "dead to rights: +60%", 240, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onHit( rat, 100, true ) );

			//Last Bullet: once a floor
			h.talents.get( 5 ).put( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.LAST_BULLET, 3 );
			h.talents.get( 4 ).remove( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.GUNSMOKE ); h.talents.get( 5 ).remove( xyz.gabriwar.warpedpixeldungeon.actors.hero.Talent.SHRAPNEL_VEST );
			r.useRound();
			int before = r.round();
			h.HP = 100;
			xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.incoming( 80, null );
			assertEquals( "a full magazine", r.maxRound(), r.round() );
			r.useRound();
			h.HP = 30;
			xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.incoming( 5, null );
			assertTrue( "only once a floor", r.round() < r.maxRound() );

			//no firearm, no talent
			h.belongings.weapon = new xyz.gabriwar.warpedpixeldungeon.items.weapon.melee.Dagger();
			assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.damageMod( true ), 0.001f );
			assertEquals( 1f, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.accuracyMod(), 0.001f );
			assertEquals( 0, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.levelBonus() );
			assertEquals( 100, xyz.gabriwar.warpedpixeldungeon.actors.hero.GunTalents.onHit( rat, 100, true ) );

			//the hail of lead remembers itself across a save
			xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.gunslinger.HailOfLead.Hail hail = new xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.gunslinger.HailOfLead.Hail();
			hail.set( 8, 1.3f, 2 );
			com.watabou.utils.Bundle bd = new com.watabou.utils.Bundle();
			hail.storeInBundle( bd );
			xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.gunslinger.HailOfLead.Hail back = new xyz.gabriwar.warpedpixeldungeon.actors.hero.abilities.gunslinger.HailOfLead.Hail();
			back.restoreFromBundle( bd );
			assertEquals( 8, back.turnsLeft() );
		} finally {
			h.heroClass = keepClass;
			h.talents = keepTalents;
			h.belongings.weapon = keepWeapon;
			Dungeon.bullet = keepBullet; h.HP = keepHP; h.HT = keepHT;
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
