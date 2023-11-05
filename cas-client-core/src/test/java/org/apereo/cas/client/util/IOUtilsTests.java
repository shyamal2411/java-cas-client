package org.apereo.cas.client.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class IOUtilsTests {

    @Test
    public void testReadStringWithUTF8() throws IOException {
        final String testData = "This is a test string.";
        final ByteArrayInputStream inputStream =
                new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8));

        String result = IOUtils.readString(inputStream);
        assertEquals(testData, result);
    }

    @Test
    public void testReadStringWithCharset() throws IOException {
        final String testData = "This is a test string.";
        final ByteArrayInputStream inputStream =
                new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16));

        String result = IOUtils.readString(inputStream, StandardCharsets.UTF_16);
        assertEquals(testData, result);
    }

    @Test
    public void testReadStringEmptyStream() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);

        String result = IOUtils.readString(inputStream);
        assertEquals("", result);
    }

    @Test
    public void testCloseQuietly() {
        IOUtils.closeQuietly(null);
    }
}
