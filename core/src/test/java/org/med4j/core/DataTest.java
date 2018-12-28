package org.medibloc.panacea.core;

import org.junit.Test;
import org.medibloc.panacea.data.Data;
import org.medibloc.panacea.utils.Numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DataTest {
    @Test
    public void testHashRecord() {
        String actual = Numeric.toHexStringNoPrefix(Data.hashRecord("abcd"));
        assertEquals("6f6f129471590d2c91804c812b5750cd44cbdfb7238541c451e1ea2bc0193177", actual);

        actual = Numeric.toHexStringNoPrefix(Data.hashRecord("abcdxxxx"));
        assertNotEquals("6f6f129471590d2c91804c812b5750cd44cbdfb7238541c451e1ea2bc0193177", actual);
    }
}
