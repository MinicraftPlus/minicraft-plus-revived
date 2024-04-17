import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.item.FishingData;
import minicraft.item.FishingRodItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FishingRodItemTest {
	private static ArrayList<FishingRodItem> fishingRodItems = new ArrayList<>();

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof FishingRodItem) {
				fishingRodItems.add((FishingRodItem) item);
			}
		}

	}

	@Test
	public void testGetAllInstances() {
		assertEquals(4,fishingRodItems.size());
	}

	@Test
	public void testGetChanceIronGettingFish() {
		assertEquals(24,FishingRodItem.getChance(0,1));
	}

	@Test
	public void testGetChanceGoldGettingRareItem() {
		assertEquals(4,FishingRodItem.getChance(3,3));
	}

	@Test
	public void testInteractOnWaterNoneSwimmingPlayer() {
		Tile mockTile = mock(Tile.class);
		Player mockPlayer = mock(Player.class);
		mockPlayer.isFishing = false;
		mockPlayer.fishingLevel = -1;
		when(mockPlayer.isSwimming()).thenReturn(false);
		FishingRodItem woodFishingRod = fishingRodItems.get(0);
		try (MockedStatic<Tiles> utilities = mockStatic(Tiles.class)) {
			utilities.when(() -> Tiles.get("water"))
				.thenReturn(mockTile);


			assertTrue(woodFishingRod.interactOn(mockTile,null,0,0,mockPlayer,null));
			assertTrue(mockPlayer.isFishing);
			assertEquals(mockPlayer.fishingLevel,woodFishingRod.level);
			verify(mockPlayer,times(1)).isSwimming();
		}
	}

	@Test
	public void testInteractOnWaterSwimmingPlayer() {
		Tile mockTile = mock(Tile.class);
		Player mockPlayer = mock(Player.class);
		mockPlayer.isFishing = false;
		mockPlayer.fishingLevel = -1;
		when(mockPlayer.isSwimming()).thenReturn(true);
		FishingRodItem woodFishingRod = fishingRodItems.get(0);
		try (MockedStatic<Tiles> utilities = mockStatic(Tiles.class)) {
			utilities.when(() -> Tiles.get("water"))
				.thenReturn(mockTile);


			assertFalse(woodFishingRod.interactOn(mockTile,null,0,0,mockPlayer,null));
			assertFalse(mockPlayer.isFishing);
			assertEquals(mockPlayer.fishingLevel,-1);
			verify(mockPlayer,times(1)).isSwimming();
		}
	}

	@Test
	public void testInteractOnNoneWaterTile() {
		Tile mockTile = mock(Tile.class);
		Player mockPlayer = mock(Player.class);
		mockPlayer.isFishing = false;
		mockPlayer.fishingLevel = -1;
		when(mockPlayer.isSwimming()).thenReturn(true);
		FishingRodItem woodFishingRod = fishingRodItems.get(0);
		try (MockedStatic<Tiles> utilities = mockStatic(Tiles.class)) {
			utilities.when(() -> Tiles.get("water"))
				.thenReturn(null);


			assertFalse(woodFishingRod.interactOn(mockTile,null,0,0,mockPlayer,null));
			assertFalse(mockPlayer.isFishing);
			assertEquals(mockPlayer.fishingLevel,-1);
			verify(mockPlayer,times(0)).isSwimming();
		}
	}

	@Test
	public void testCanAttack() {
		assertFalse(fishingRodItems.get(0).canAttack());
	}

	@Test
	public void testHigherLevelsHaveLowerChanceOfBreaking() {
		List<String> oldGameNotifications = Game.notifications;
		Tile mockTile = mock(Tile.class);
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.isSwimming()).thenReturn(false);
		int[][] runs = new int[10][];
		try (MockedStatic<Tiles> utilities = mockStatic(Tiles.class)) {
			utilities.when(() -> Tiles.get("water"))
				.thenReturn(mockTile);


			for (int i = 0; i < runs.length; i++) {
				int[] untilBreak = new int[] {0,0,0,0};
				for (int j = 0; j < untilBreak.length; j++) {
					int untilBreaks = 0;
					FishingRodItem currFishRod = (FishingRodItem) fishingRodItems.get(j).copy();
					while (!currFishRod.isDepleted()) {
						currFishRod.interactOn(mockTile,null,0,0,mockPlayer,null);
						untilBreaks++;
					}
					untilBreak[j] = untilBreaks;
				}
				runs[i] = untilBreak;
			}
		}


		int []averageTillBreaks = new int[] {0,0,0,0};
		for(int i = 0; i < runs[0].length; i++) {
            for (int[] run : runs) {
                averageTillBreaks[i] += run[i];
            }
			averageTillBreaks[i] /= runs.length;
		}
		assertTrue(averageTillBreaks[3] > averageTillBreaks[2]);
		assertTrue(averageTillBreaks[2] > averageTillBreaks[1]);
		assertTrue(averageTillBreaks[1] > averageTillBreaks[0]);
		Game.notifications = oldGameNotifications; // Undo changes
	}

	@Test
	public void testCopy() {
		FishingRodItem copiedFishingRodItem = (FishingRodItem)fishingRodItems.get(0).copy();
		assertEquals(copiedFishingRodItem.getName(),fishingRodItems.get(0).getName());
	}




}
