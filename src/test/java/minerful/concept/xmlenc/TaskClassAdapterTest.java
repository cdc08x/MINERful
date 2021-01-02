package minerful.concept.xmlenc;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import minerful.concept.AbstractTaskClass;
import minerful.logparser.CharTaskClass;
import minerful.logparser.StringTaskClass;

/**
 * Tests for class TaskClassAdapter
 */
class TaskClassAdapterTest {
	
	private TaskClassAdapter taskClassAdapter;
	
	@BeforeEach
	private void before() {
		taskClassAdapter = new TaskClassAdapter();
	}
	
	@Test
	void testMarshal_positive() throws Exception {
		Assert.assertEquals("a",taskClassAdapter.marshal(new CharTaskClass('a')));
	}

	@Test
	void testUnmarshal_positive() throws Exception {
		Assert.assertEquals(new StringTaskClass("a"),taskClassAdapter.unmarshal("a"));
	}

}
