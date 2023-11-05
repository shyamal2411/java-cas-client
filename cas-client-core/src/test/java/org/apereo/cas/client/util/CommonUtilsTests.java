/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.util;

import org.apereo.cas.client.PublicTestHttpServer;
import org.apereo.cas.client.ssl.HttpsURLConnectionFactory;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertThrows;

/**
 * Tests for the CommonUtils.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class CommonUtilsTests extends TestCase {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8092);

    public void testRedirectUrlWithParam() {
        final var loginUrl = "http://localhost:8080/login?myName=foo";
        final var fullyConstructedUrl = CommonUtils.constructRedirectUrl(loginUrl, "foo", "foo", false, false, null);

        assertEquals("http://localhost:8080/login?myName=foo&foo=foo", fullyConstructedUrl);

        var count = 0;
        final var chars = fullyConstructedUrl.toCharArray();

        for (final char aChar : chars) {
            if (aChar == '?') {
                count++;
            }
        }

        assertEquals(1, count);
    }

    public void testRedirectUrlWithMethod() {
        final var loginUrl = "http://localhost:8080/login";
        final var redirectUrl = CommonUtils.constructRedirectUrl(loginUrl, "foo", "foo", true, true, "post");

        assertEquals("http://localhost:8080/login?foo=foo&renew=true&gateway=true&method=post", redirectUrl);
    }

    public void testAssertNotNull() {
        final var CONST_MESSAGE = "test";
        CommonUtils.assertNotNull(new Object(), CONST_MESSAGE);
        try {
            CommonUtils.assertNotNull(null, CONST_MESSAGE);
        } catch (final IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testAssertNotEmpty() {
        final var CONST_MESSAGE = "test";
        final Collection<Object> c = new ArrayList<>();
        c.add(new Object());
        CommonUtils.assertNotEmpty(c, CONST_MESSAGE);
        try {
            CommonUtils.assertNotEmpty(new ArrayList<>(), CONST_MESSAGE);
        } catch (final IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }

        try {
            CommonUtils.assertNotEmpty(null, CONST_MESSAGE);
        } catch (final IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testAssertTrue() {
        final var CONST_MESSAGE = "test";
        CommonUtils.assertTrue(true, CONST_MESSAGE);
        try {
            CommonUtils.assertTrue(false, CONST_MESSAGE);
        } catch (final IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testIsEmpty() {
        assertFalse(CommonUtils.isEmpty("test"));
        assertFalse(CommonUtils.isEmpty(" test"));
        assertTrue(CommonUtils.isEmpty(""));
        assertTrue(CommonUtils.isEmpty(null));
        assertFalse(CommonUtils.isEmpty("   "));
    }

    public void testIsNotEmpty() {
        assertTrue(CommonUtils.isNotEmpty("test"));
        assertTrue(CommonUtils.isNotEmpty(" test"));
        assertFalse(CommonUtils.isNotEmpty(""));
        assertFalse(CommonUtils.isNotEmpty(null));
        assertTrue(CommonUtils.isNotEmpty("   "));
    }

    public void testIsBlank() {
        assertFalse(CommonUtils.isBlank("test"));
        assertFalse(CommonUtils.isBlank(" test"));
        assertTrue(CommonUtils.isBlank(""));
        assertTrue(CommonUtils.isBlank(null));
        assertTrue(CommonUtils.isBlank("   "));
    }

    public void testIsNotBlank() {
        assertTrue(CommonUtils.isNotBlank("test"));
        assertTrue(CommonUtils.isNotBlank(" test"));
        assertFalse(CommonUtils.isNotBlank(""));
        assertFalse(CommonUtils.isNotBlank(null));
        assertFalse(CommonUtils.isNotBlank("   "));
    }

    public void testGetResponseFromServer() throws Exception {
        final var RESPONSE = "test1\r\ntest2";
        server.content = RESPONSE.getBytes(server.encoding);

        final var responsedContent = CommonUtils.getResponseFromServer(new URL("http://localhost:8092"), new HttpsURLConnectionFactory(), null);
        assertEquals(RESPONSE, responsedContent);
    }

    public void testUrlEncode() {
        assertEquals("this+is+a+very+special+parameter+with+%3D%25%2F",
                CommonUtils.urlEncode("this is a very special parameter with =%/"));
    }

    @Test
    public void testToIntPositiveCase() {
        String value = "12";
        assertEquals(12, CommonUtils.toInt(value, -1));
    }

    @Test
    public void testToIntNegativeCase() {
        String value = null;
        int expected_value = -1;
        assertEquals(expected_value, CommonUtils.toInt(value, -1));
    }

    @Test
    public void testToIntExceptionCase() {
        String value = "ABC";
        int defaultValue = -1;
        int expectedValue = -1;
        assertEquals(expectedValue, CommonUtils.toInt(value, defaultValue));
    }

    @Test
    public void testToBooleanObjectWithTrue() {

        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("True"));
    }

    @Test
    public void testToBooleanObjectWithFalse() {
        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("False"));
    }

    @Test
    public void testToBooleanObjectWithOtherValues() {
        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("YES"));
        assertEquals(Boolean.TRUE, CommonUtils.toBooleanObject("Yes"));

        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("NO"));
        assertEquals(Boolean.FALSE, CommonUtils.toBooleanObject("No"));
    }


    @Test
    public void testToBooleanObjectWithInvalidValues() {
        assertNull(CommonUtils.toBooleanObject("abc"));
        assertNull(CommonUtils.toBooleanObject("123"));
        assertNull(CommonUtils.toBooleanObject("onoff"));
    }
}
