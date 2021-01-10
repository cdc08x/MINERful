package minerful.concept.xmlenc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for class CharAdapter
 */
class CharAdapterTest {
	
	private CharAdapter charAdapter;
	
	@BeforeEach
	private void before() {
		charAdapter = new CharAdapter();
	}
	
	@Test
	void testMarshal_positive() throws Exception {
		Assertions.assertEquals("a",charAdapter.marshal('a'));
	}

	@Test
	void testUnmarshal_positive() throws Exception {
		Assertions.assertEquals(Character.valueOf('a'),charAdapter.unmarshal("a"));
	}

}
