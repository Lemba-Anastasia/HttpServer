package Model;

import junit.framework.Assert;
import junit.framework.TestCase;

public class IdCounterTest extends TestCase {
    public void testGetInstance()
    {
        Assert.assertNotNull(IdCounter.generateId());
    }

    public void testGetId() {
        int i=0;
        Assert.assertEquals(1,++i);
    }

}