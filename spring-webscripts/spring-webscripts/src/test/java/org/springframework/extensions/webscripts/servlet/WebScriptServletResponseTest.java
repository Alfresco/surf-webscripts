/**
 * Copyright (C) 2005-20019 Alfresco Software Limited.
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

package org.springframework.extensions.webscripts.servlet;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test HTTP Servlet Web Script Response
 *
 * @author mmuller
 */
public class WebScriptServletResponseTest
{
    // REPO-4388 preserve CORS headers
    @Test
    public void testWebScriptServletResponseReset()
    {
        // test if
        MockHttpServletResponse mockedResponse = new MockHttpServletResponse();
        mockedResponse.addHeader("headerA", "valueA");
        WebScriptServletResponse response = new WebScriptServletResponse(null, mockedResponse);
        assertEquals("Mocked response has not headerA", mockedResponse.getHeader("headerA"),"valueA");
        response.reset(null);
        assertNull("Header headerA should be deleted", mockedResponse.getHeader("headerA"));

        // simple preserve headers
        mockedResponse = new MockHttpServletResponse();
        mockedResponse.addHeader("headerA", "valueA");
        mockedResponse.addHeader("headerB", "valueB");
        mockedResponse.addHeader("headerC", "valueC");
        response = new WebScriptServletResponse(null, mockedResponse);
        response.reset("headerB");
        assertNull("Header headerA should be deleted", mockedResponse.getHeader("headerA"));
        assertEquals("Header headerB should be not deleted", "valueB", mockedResponse.getHeader("headerB"));
        assertNull("Header headerC should be deleted", mockedResponse.getHeader("headerC"));

        // more complex CORS headers with using regular expressions
        mockedResponse = new MockHttpServletResponse();
        mockedResponse.addHeader("headerA", "valueA");
        mockedResponse.addHeader("Access-Control-.*", "abc");
        mockedResponse.addHeader("Access-Control-Origin", "abcd");
        mockedResponse.addHeader("Access-Controll-Origin", "abcde");
        mockedResponse.addHeader("Access-Control-", "abcdef");
        response = new WebScriptServletResponse(null, mockedResponse);
        response.reset("Access-Control-.*");
        assertNull("Header headerA should be deleted", mockedResponse.getHeader("headerA"));
        assertNull("Header Access-Controll-Origin should be deleted", mockedResponse.getHeader("Access-Controll-Origin"));
        assertEquals("Header Access-Control-.* should be not deleted", "abc", mockedResponse.getHeader("Access-Control-.*"));
        assertEquals("Header Access-Control-Origin should be not deleted", "abcd", mockedResponse.getHeader("Access-Control-Origin"));
        assertEquals("Header Access-Control- should be not deleted", "abcdef", mockedResponse.getHeader("Access-Control-"));
    }
}
