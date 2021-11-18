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

package org.springframework.extensions.webscripts.ui.common;

import junit.framework.TestCase;

/**
 * String Utils Unit Test
 * 
 * @author Tiago Salvado
 */
public class StringUtilsTest extends TestCase
{
    private final String DOCTYPE_HTML = "<!DOCTYPE html>";
    private final String HTML_ELEM = "<html>";
    private final String BODY_ELEM = "<body>";
    private final String ONCLICK_ATTR = "onclick";
    
    private final String HTML_SNIPPET1 = "<p>a & ab <> c</p>";
    private final String HTML_SNIPPET2 = "<p>a &amp; ab &lt;&gt; c</p>";
    private final String HTML_SNIPPET3 = "<%<script>alert('XSS');//<%</script>";
    private final String HTML_SNIPPET4 = "<style>div { background-image: url('img.jpg'); }</style>";
    private final String HTML_SNIPPET5 = "<TABLE BACKGROUND=\"javascript:alert('XSS')\">";
    private final String HTML_SNIPPET6 = "<META HTTP-EQUIV=\"refresh\" CONTENT=\"0;url=javascript:alert('XSS');\">";
    private final String HTML_SNIPPET7 = "<IFRAME SRC=\"javascript:alert('XSS');\"></IFRAME>";

    private final String HTML_SNIPPET8 = "<STYLE>BODY{-moz-binding:url(\"http://xss.rocks/xssmoz.xml#xss\")}</STYLE>";
    private final String HTML_SNIPPET9 = "<a href=\"http://example.com/attack.html\" style=\"display: block; z-index: 100000; opacity: 0.5; position: fixed; top: 0px; left: 0; width: 1000000px; height: 100000px; background-color: red;\"> </a> ";
    private final String HTML_SNIPPET10 = "<DIV STYLE=\"this-is-js-property: alert 'XSS';\">";
    private final String HTML_SNIPPET11 = "<DIV STYLE=\"background-image: url(javascript:alert('XSS'))\">";
    private final String HTML_SNIPPET12 = "<DIV style=\"width: expression(alert('XSS'));\">";
    private final String HTML_SNIPPET13 = "<table><tbody><tr><td style=\"border:1px solid\">&amp;nbsp;text</td><td>&amp;nbsp;text</td></tr></tbody></table>";
    private final String HTML_SNIPPET14 = "<table><tbody><tr><td style=\"border:1px solid; background-image: url('img.jpg');\">&amp;nbsp;text</td><td>&amp;nbsp;text</td></tr></tbody></table>";
    private final String HTML_SNIPPET15 = "exp/*<A STYLE='no\\xss:noxss(\"*//*\");xss:ex/*XSS*//*/*/pression(alert(\"XSS\"))'>";
    private final String HTML_SNIPPET16 = "<XSS STYLE=\"xss:expression(alert('XSS'))\">";
    private final String HTML_SNIPPET17 = "<IMG style=\"xss:expr/*XSS*/ession(alert('XSS'))\">";

    private final String HTML_DOC1 = "<!DOCTYPE>\n"
            + "<html>\n"
            + "<body>\n"
            + "        <p><button onclick=alert('CLICK')></p>\n"
            + "</body>\n"
            + "</html>";
    
    private final String HTML_DOC2 = "<html>\n"
            + "<body>\n"
            + "        <p><button onclick=alert('CLICK')></p>\n"
            + "</body>\n"
            + "</html>";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testStripHTML() throws Exception
    {
        // Test if 'html' has been added to <!DOCTYPE>, if html and body elements haven't been removed and onclick was
        // cleaned
        String test1 = StringUtils.stripUnsafeHTMLDocument(HTML_DOC1, false);
        assertTrue(test1.contains(DOCTYPE_HTML));
        assertTrue(test1.contains(HTML_ELEM));
        assertTrue(test1.contains(BODY_ELEM));
        assertFalse(test1.contains(ONCLICK_ATTR));

        // Test if <!DOCTYPE html> has been added to doc, if html and body elements haven't been removed and onclick was
        // cleaned
        String test2 = StringUtils.stripUnsafeHTMLDocument(HTML_DOC2, false);
        assertTrue(test2.contains(DOCTYPE_HTML));
        assertTrue(test2.contains(HTML_ELEM));
        assertTrue(test2.contains(BODY_ELEM));
        assertFalse(test2.contains(ONCLICK_ATTR));

        // Test that <!DOCTYPE html> hasn't been added to doc
        String test3 = StringUtils.stripUnsafeHTMLTags(HTML_DOC2, false, false);
        assertFalse(test3.contains(DOCTYPE_HTML));

        // Test if the snippet text has been encoded
        String test4 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET1);
        assertTrue(test4.contains("&amp;"));
        assertTrue(test4.contains("&lt;&gt;"));

        // Test if the snippet text hasn't been encoded
        String test5 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET1, false);
        assertTrue(test5.equals(HTML_SNIPPET1));

        // Test if the snippet text has been encoded (even if text was already encoded)
        String test6 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET2);
        assertTrue(test6.contains("&amp;amp;"));
        assertTrue(test6.contains("&amp;lt;&amp;gt;"));

        // Test if the snippet text hasn't been encoded again and didn't decode the existing text
        String test7 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET2, false);
        assertTrue(test7.equals(HTML_SNIPPET2));

        // Test if script and alert have been cleaned
        String test8 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET3);
        assertFalse(test8.contains("script"));
        assertFalse(test8.contains("alert"));

        // Test if style, background, url and img references have been cleaned
        String test9 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET4);
        assertFalse(test9.contains("style"));
        assertFalse(test9.contains("background-image"));
        assertFalse(test9.contains("url"));
        assertFalse(test9.contains("img.jpg"));

        String test10 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET5);
        assertFalse(test10.contains("BACKGROUND"));
        assertFalse(test10.contains("javascript"));
        assertFalse(test10.contains("alert"));

        String test11 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET6);
        assertFalse(test11.contains("CONTENT"));
        assertFalse(test11.contains("javascript"));
        assertFalse(test11.contains("alert"));

        String test12 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET7);
        assertFalse(test12.contains("IFRAME"));
        assertFalse(test12.contains("javascript"));
        assertFalse(test12.contains("alert"));
    }

    public void testStripHTMLWithStyles() throws Exception
    {
        String test0 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET8);
        assertFalse(test0.contains("style"));
        assertFalse(test0.contains("http"));
        assertFalse(test0.contains("xss"));

        String test1 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET9);
        assertTrue(test1.contains("style"));

        String test2 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET10);
        assertFalse(test2.contains("style"));
        assertFalse(test2.contains("XSS"));

        String test3 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET11);
        assertFalse(test3.contains("style"));
        assertFalse(test3.contains("XSS"));
        assertFalse(test3.contains("alert"));

        String test4 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET12);
        assertFalse(test4.contains("style"));
        assertFalse(test4.contains("XSS"));
        assertFalse(test4.contains("alert"));

        String test5 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET13);
        assertEquals(test5, HTML_SNIPPET13);

        String test6 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET14);
        assertFalse(test6.contains("background-image"));
        assertFalse(test6.contains("url"));
        assertFalse(test6.contains("img.jpg"));

        String test7 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET15);
        assertFalse(test7.contains("<A"));
        assertFalse(test7.contains("style"));
        assertFalse(test7.contains("xss"));
        assertFalse(test7.contains("alert"));

        String test8 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET16);
        assertFalse(test8.contains("XSS"));
        assertFalse(test8.contains("style"));
        assertFalse(test8.contains("expression"));
        assertFalse(test8.contains("alert"));

        String test9 = StringUtils.stripUnsafeHTMLTags(HTML_SNIPPET17);
        assertFalse(test9.contains("style"));
        assertFalse(test9.contains("xss"));
        assertFalse(test9.contains("expr"));
        assertFalse(test9.contains("alert"));
    }
}