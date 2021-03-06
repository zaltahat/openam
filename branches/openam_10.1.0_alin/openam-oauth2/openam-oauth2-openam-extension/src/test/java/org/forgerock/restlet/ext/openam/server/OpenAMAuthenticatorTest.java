/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.forgerock.restlet.ext.openam.server;

import org.forgerock.restlet.ext.openam.OpenAMParameters;
import org.forgerock.restlet.ext.openam.OpenAMTestBase;
import org.forgerock.restlet.ext.openam.OpenAMUser;
import org.forgerock.restlet.ext.openam.client.OpenAMProxy;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * An OpenAMAuthenticatorTest does ...
 * 
 * @author Laszlo Hordos
 */
public class OpenAMAuthenticatorTest extends OpenAMTestBase {
    private Server server;

    @BeforeClass
    public void setUp() throws Exception {
        OpenAMParameters parameters = new OpenAMParameters();
        // Protect the ServerResource
        OpenAMAuthenticator filter = new OpenAMAuthenticator(new Context(), parameters);
        // Authorize
        OpenAMAuthorizer authorizer = new OpenAMAuthorizer("OAUTH2");
        filter.setNext(authorizer);
        authorizer.setNext(OpenAMAuthenticatorTest.class);
        server = new Server(Protocol.HTTP, 8182, filter);
        server.start();
    }

    @AfterClass
    public void shutDown() throws Exception {
        if (null != server) {
            server.stop();
        }
    }

    @Test
    public void testAuthentication() throws Exception {
        ClientResource clientResource = new ClientResource(new Context(), "http://localhost:8182");
        Client client = new Client(new Context(), Protocol.HTTP);

        // Pre-Authenticate the Request
        OpenAMProxy proxy = new OpenAMProxy(clientResource.getContext(), new OpenAMParameters());
        proxy.setNext(client);
        clientResource.setNext(proxy);

        Representation representation = clientResource.get();
        Assert.assertEquals(representation.getText(), "amadmin");

    }

    @Get
    public Representation represent() {
        if (getRequest().getClientInfo().getUser() instanceof OpenAMUser) {
            return new StringRepresentation(((OpenAMUser) getRequest().getClientInfo().getUser())
                    .getIdentifier());
        }
        return new StringRepresentation("failed");
    }
}
