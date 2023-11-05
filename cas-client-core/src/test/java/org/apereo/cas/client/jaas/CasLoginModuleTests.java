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
package org.apereo.cas.client.jaas;

import org.apereo.cas.client.PublicTestHttpServer;
import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.beans.PropertyDescriptor;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for {@link CasLoginModule} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 */
public class CasLoginModuleTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8091);

    private static final String CONST_CAS_SERVER_URL = "http://localhost:8091/";

    private CasLoginModule module;

    private Subject subject;

    private Map<String, String> options;


//    @Test
//    public void testConvertIfNecessaryString() throws Exception {
//        // Create an instance of the CasLoginModule class
//        CasLoginModule casLoginModule = new CasLoginModule();
//
//        // Define a property descriptor with String type
//        PropertyDescriptor pd = new PropertyDescriptor("propertyName", CasLoginModule.class);
//
//        // Call the private method convertIfNecessary using reflection
//        String value = "test";
//        Object result = ReflectionUtils.invokeMethod(casLoginModule, "convertIfNecessary", pd, value);
//
//        // Check if the result matches the expected value
//        assertEquals(value, result);
//    }

//    @Test
//    public void testConvertIfNecessaryBoolean() throws Exception {
//        // Create an instance of the CasLoginModule class
//        CasLoginModule casLoginModule = new CasLoginModule();
//
//        // Define a property descriptor with boolean type
//        PropertyDescriptor pd = new PropertyDescriptor("propertyName", CasLoginModule.class);
//
//        // Call the private method convertIfNecessary using reflection
//        String value = "true";
//        Object result = ReflectionUtils.invokeMethod(casLoginModule, "convertIfNecessary", pd, value);
//
//        // Check if the result matches the expected boolean value
//        assertNotNull(result);
//        assertEquals(Boolean.class, result.getClass());
//        assertEquals(true, result);
//    }

    @Test
    public void testConvertIfNecessaryInt() throws Exception {
        // Create an instance of the CasLoginModule class
        CasLoginModule casLoginModule = new CasLoginModule();

        // Define a property descriptor with int type
        PropertyDescriptor pd = new PropertyDescriptor("propertyName", module.getClass());

        // Call the private method convertIfNecessary using reflection
        String value = "123";
        Object result = ReflectionUtils.invokeMethod(module.getClass().getMethod("convertIfNecessary", PropertyDescriptor.class, String.class), "convertIfNecessary", pd, value);

        // Check if the result matches the expected integer value
        assertNotNull(result);
        assertEquals(Integer.class, result.getClass());
        assertEquals(123, result);
    }

//    @Test
//    public void testConvertIfNecessaryLong() throws Exception {
//        // Create an instance of the CasLoginModule class
//        CasLoginModule casLoginModule = new CasLoginModule();
//
//        // Define a property descriptor with long type
//        PropertyDescriptor pd = new PropertyDescriptor("propertyName", CasLoginModule.class);
//
//        // Call the private method convertIfNecessary using reflection
//        String value = "12345";
//        Object result = ReflectionUtils.invokeMethod(casLoginModule, "convertIfNecessary", pd, value);
//
//        // Check if the result matches the expected long value
//        assertNotNull(result);
//        assertEquals(Long.class, result.getClass());
//        assertEquals(12345L, result);
//    }
//}

    /* @AfterClass
     public static void classCleanUp() {
         server.shutdown();
     }*/

    @Before
    public void setUp() throws Exception {
        module = new CasLoginModule();
        subject = new Subject();
        options = new HashMap<>();
        options.put("service", "https://service.example.com/webapp");
        options.put("ticketValidatorClass", Cas20ServiceTicketValidator.class.getName());
        options.put("casServerUrlPrefix", CONST_CAS_SERVER_URL);
        options.put("proxyCallbackUrl", "https://service.example.com/webapp/proxy");
        options.put("renew", "true");
        options.put("defaultRoles", "ADMIN");
        options.put("roleGroupName", "Roles");
    }

    /**
     * Test JAAS login success.
     *
     * @throws Exception On errors.
     */
    @Test
    public void testLoginSuccess() throws Exception {
        final var USERNAME = "username";
        final var SERVICE = "https://example.com/service";
        final var TICKET = "ST-100000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final var RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>" + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 1);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
        assertTrue(hasPrincipalName(this.subject, AssertionPrincipal.class, USERNAME));
    }

    /**
     * Test JAAS login failure.
     *
     * @throws Exception On errors.
     */
    @Test
    public void testLoginFailure() throws Exception {
        final var SERVICE = "https://example.com/service";
        final var TICKET = "ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final var RESPONSE =
                "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        try {
            module.login();
            fail("Login did not throw FailedLoginException as expected.");
        } catch (final LoginException e) {
            assertEquals(TicketValidationException.class, e.getCause().getClass());
        }
        module.commit();
        assertNull(module.ticket);
        assertNull(module.assertion);
    }

    /**
     * Test JAAS logout after successful login to ensure subject cleanup.
     *
     * @throws Exception On errors.
     */
    @Test
    public void testLogout() throws Exception {
        testLoginSuccess();
        module.logout();
        assertEquals(0, subject.getPrincipals().size());
        assertEquals(0, subject.getPrivateCredentials().size());
    }

    /**
     * Confirm that CasLoginModule#logout() destroys cached data and prevents subsequent login w/expired ticket.
     *
     * @throws Exception On errors.
     */
    @Test
    public void testAssertionCaching() throws Exception {
        final var USERNAME = "username";
        final var SERVICE = "https://example.com/service";
        final var TICKET = "ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final var SUCCESS_RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>" + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        final var FAILURE_RESPONSE =
                "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";

        options.put("cacheAssertions", "true");
        options.put("cacheTimeout", "1");

        server.content = SUCCESS_RESPONSE.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 1);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());

        // Logout should destroy all authenticated state data including assertion cache entries
        module.logout();
        assertEquals(0, subject.getPrincipals().size());
        assertEquals(0, subject.getPrivateCredentials().size());
        server.content = FAILURE_RESPONSE.getBytes(server.encoding);

        // Verify we can't log in again with same ticket
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        try {
            module.login();
            module.commit();
            Assert.fail("Login should have failed.");
        } catch (final LoginException e) {
            assertEquals(TicketValidationException.class, e.getCause().getClass());
        }
        assertEquals(0, this.subject.getPrincipals().size());
        assertEquals(0, this.subject.getPrivateCredentials().size());
    }

    /**
     * Verify that cached assertions that are expired are never be accessible
     * by {@link CasLoginModule#login()} method.
     *
     * @throws Exception On errors.
     */
    @Test
    public void testAssertionCachingExpiration() throws Exception {
        final var USERNAME = "hizzy";
        final var SERVICE = "https://example.com/service";
        final var TICKET = "ST-12345-ABCDEFGHIJKLMNOPQRSTUVWXYZ-hosta";
        final var SUCCESS_RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>" + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        final var FAILURE_RESPONSE =
                "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-12345-ABCDEFGHIJKLMNOPQRSTUVWXYZ-hosta not recognized</cas:authenticationFailure></cas:serviceResponse>";

        options.put("cacheAssertions", "true");
        // Cache timeout is 1 second
        options.put("cacheTimeoutUnit", "SECONDS");
        options.put("cacheTimeout", "1");

        server.content = SUCCESS_RESPONSE.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        assertTrue(module.login());
        module.commit();

        Thread.sleep(1100);
        // Assertion should now be expired from cache
        server.content = FAILURE_RESPONSE.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<>(),
                options);
        try {
            module.login();
            fail("Should have thrown FailedLoginException.");
        } catch (final LoginException e) {
            assertEquals(TicketValidationException.class, e.getCause().getClass());
        }
    }

    private static boolean hasPrincipalName(final Subject subject, final Class<? extends Principal> principalClass,
                                            final String name) {
        final var principals = subject.getPrincipals(principalClass);
        for (final Principal p : principals) {
            if (p.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
