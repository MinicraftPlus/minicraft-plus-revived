import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.item.FishingData;
import minicraft.saveload.Load;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class FishingDataTest {

	@Test
	public void testGetDataWithNoError() {
		ArrayList returnList = new ArrayList<>(Arrays.asList("element1", "element2"));
		try (MockedStatic<Load> utilities = mockStatic(Load.class)) {
			utilities.when(() -> Load.loadFile(anyString()))
				.thenReturn(returnList);

			List<String> resultList = FishingData.getData("name");
			assertEquals(returnList, resultList);
		}
	}

	@Test
	public void testGetDataWithError() {
		try (MockedStatic<Load> utilities = mockStatic(Load.class)) {
			utilities.when(() -> Load.loadFile(anyString()))
				.thenThrow(new IOException());

			List<String> resultList = FishingData.getData("name");
			assertEquals(0, resultList.size());
		}
	}
}
