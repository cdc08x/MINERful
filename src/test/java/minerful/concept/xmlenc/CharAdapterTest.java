package minerful.concept.xmlenc;

import org.junit.Assert;
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
		Assert.assertEquals("a",charAdapter.marshal('a'));
	}

	@Test
	void testUnmarshal_positive() throws Exception {
		Assert.assertEquals(Character.valueOf('a'),charAdapter.unmarshal("a"));
	}

}
