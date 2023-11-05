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
package org.apereo.cas.client.ssl;

import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public final class HttpsURLConnectionFactoryTests {

    private HttpsURLConnectionFactory httpsURLConnectionFactory;


    @Before
    public void setUp() throws Exception {
        this.httpsURLConnectionFactory = new HttpsURLConnectionFactory();
    }

    @Test
    public void testHashCode(){
        HostnameVerifier hostnameVerifier = httpsURLConnectionFactory.hostnameVerifier;
        Properties sslConfiguration = httpsURLConnectionFactory.sslConfiguration;
        httpsURLConnectionFactory = new HttpsURLConnectionFactory(hostnameVerifier, sslConfiguration);
        assertEquals(-1278795289, httpsURLConnectionFactory.hashCode());
    }


    @Test
    public void serializeAndDeserialize() throws Exception {
        final var baos = new ByteArrayOutputStream();
        final ObjectOutput oos = new ObjectOutputStream(baos);

        oos.writeObject(this.httpsURLConnectionFactory);
        oos.close();

        final var serializedHttpsUrlConnectionFactory = baos.toByteArray();

        final var bais = new ByteArrayInputStream(serializedHttpsUrlConnectionFactory);
        final var ois = new ObjectInputStream(bais);

        final var deserializedObject = (HttpsURLConnectionFactory) ois.readObject();
        assertEquals(this.httpsURLConnectionFactory, deserializedObject);
    }
}
