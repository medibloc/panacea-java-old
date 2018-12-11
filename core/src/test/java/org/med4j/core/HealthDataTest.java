package org.med4j.core;

import org.junit.Test;
import org.med4j.healthdata.HealthData;
import org.med4j.utils.Numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HealthDataTest {
    @Test
    public void testHashData() {
        String actual = Numeric.toHexStringNoPrefix(HealthData.hashData("abcd".getBytes(), "dicom", null));
        assertEquals("6f6f129471590d2c91804c812b5750cd44cbdfb7238541c451e1ea2bc0193177", actual);

        actual = Numeric.toHexStringNoPrefix(HealthData.hashData("abcdxxxx".getBytes(), "dicom", null));
        assertNotEquals("6f6f129471590d2c91804c812b5750cd44cbdfb7238541c451e1ea2bc0193177", actual);
    }
}
