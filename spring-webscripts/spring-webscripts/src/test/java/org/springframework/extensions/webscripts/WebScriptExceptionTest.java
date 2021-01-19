/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This file is part of the Spring Surf Extension project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.extensions.webscripts;

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test Web Script API
 * 
 * @author davidc
 */
public class WebScriptExceptionTest extends TestCase
{
    private static final TestWebScriptServer TEST_SERVER = TestWebScriptServer.getTestServer();

    /**
     * Ensure that, for a non request type specific .js script, the request body
     * is available as requestbody.
     * 
     * @throws Exception
     */
    public void testScriptStatusTemplate() throws Exception
    {
        String url = "/test/exception?a=1";
        String res = "Failed /alfresco/service/test/exception - args 1";
        Response resp = sendRequest(new GetRequest(url));
        assertEquals("Unexpected status code", Status.STATUS_INTERNAL_SERVER_ERROR, resp.getStatus());
        assertEquals("Unexpected response", res, resp.getContentAsString());
    }

    public void testScriptStatus404() throws Exception
    {
        String url = "/admin/support";
        String res = "Web Script Status 404 - Not Found";
        Response resp = sendRequest(new GetRequest(url));
        assertEquals("Unexpected status code", Status.STATUS_NOT_FOUND, resp.getStatus());
        assertTrue(resp.getContentAsString().contains(res));
    }

    /**
     * @param req Request
     * @return Response
     * @throws IOException
     */
    private Response sendRequest(Request req) throws IOException
    {
        System.out.println();
        System.out.println("* Request: " + req.getMethod() + " " + req.getFullUri() + (req.getBody() == null ? "" : "\n" + req.getBody()));

        Response res = TEST_SERVER.submitRequest(req);

        System.out.println();
        System.out.println("* Response: " + res.getStatus() + " " + req.getMethod() + " " + req.getFullUri() + "\n" + res.getContentAsString());
        return res;
    }
}
