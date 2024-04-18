// import minicraft.core.Game;
// import minicraft.entity.Entity;
// import minicraft.entity.furniture.Bed;
// import minicraft.entity.mob.Player;
// import minicraft.item.FurnitureItem;
// import minicraft.item.Inventory;
// import minicraft.item.Item;
// import minicraft.item.Items;
// import minicraft.item.PowerGloveItem;
// import minicraft.item.StackableItem;
// import minicraft.level.Level;
// import minicraft.level.tile.BossDoorTile;
// import minicraft.level.tile.GrassTile;
// import minicraft.level.tile.Tile;
// import minicraft.level.tile.Tiles;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.MockedStatic;
//
//
// import java.util.ArrayList;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// public class furnitureBlackBoxTests {
// 	private Bed bed;
// 	private FurnitureItem furniture;
//
// 	@BeforeEach
// 	public void setUp() {
// 		bed = new Bed();
// 		furniture = new FurnitureItem(bed);
// 	}
//
//
//
// 	/**
// 	 * Testing can attack. This mothod always return false
// 	 */
//
// 	@Test
// 	void testCanAttack() {
// 		assertFalse(furniture.canAttack());
// 	}
//
// 	/**
// 	 *  GrassTile, x < 0,
// 	 */
//
// 	@Test
// 	public void testInteractOnFurniture() {
// 		Level mockLevel = mock(Level.class);
// 		Level level = new Level(12, 12, 12, mockLevel);
// 		Player player = new Player();
//
// 		GrassTile tile = (GrassTile)Tiles.get("grass");
//
//
//
// 	}
//
//
//
// }
