/**
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.html.Encoding;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * Class containing misc helper methods for managing Strings.
 * 
 * NOTE: Extracted from org.alfresco.web.ui.common.Utils;
 * 
 * @author Kevin Roast
 */
public class StringUtils
{
    private static final Log logger = LogFactory.getLog(StringUtils.class);

    private static final String DOCTYPE = "!DOCTYPE";
    private static final String HTML = "html";
    private static final String BODY = "body";
    private static final String HEAD = "head";

    /** default value - NOTE: see spring-webscripts-application-context.xml */
    protected static boolean overrideDocType = true;

    protected  static PolicyFactory basePolicy, docPolicy;

    static
    {
        // Base policy that will be always used
        basePolicy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES)
                .and(Sanitizers.TABLES);

        // This policy will be used when striping a HTML doc (i.e.: in stripUnsafeHTMLDocument method context)
        docPolicy = new HtmlPolicyBuilder()
                .allowElements(HTML, BODY, HEAD)
                .toFactory();
    }

    /**
     * @param overrideDocType    Decides if legacy html !DOCTYPE instructions shall be transformed to the default mode
     */
    public void setOverrideDocType(boolean overrideDocType)
    {
        StringUtils.overrideDocType = overrideDocType;
    }

    /**
     * Encodes the given string, so that it can be used within an HTML page.
     * 
     * @param string     the String to convert
     */
    public static String encode(final String string)
    {
        if (string == null)
        {
            return "";
        }
        
        StringBuilder sb = null;      //create on demand
        String enc;
        char c;
        for (int i = 0; i < string.length(); i++)
        {
            enc = null;
            c = string.charAt(i);
            switch (c)
            {
                case '\'': enc = "&#39;"; break;    //'
                case '"': enc = "&#34;"; break;     //"
                case '&': enc = "&amp;"; break;     //&
                case '<': enc = "&lt;"; break;      //<
                case '>': enc = "&gt;"; break;      //>
                
                case '\u20AC': enc = "&euro;";  break;
                case '\u00AB': enc = "&laquo;"; break;
                case '\u00BB': enc = "&raquo;"; break;
                case '\u00A0': enc = "&nbsp;"; break;
                
                default:
                    if (((int)c) >= 0x80)
                    {
                        //encode all non basic latin characters
                        enc = "&#" + ((int)c) + ";";
                    }
                break;
            }
            
            if (enc != null)
            {
                if (sb == null)
                {
                    String soFar = string.substring(0, i);
                    sb = new StringBuilder(i + 16);
                    sb.append(soFar);
                }
                sb.append(enc);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
        }
        
        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * Crop a label within a SPAN element, using ellipses '...' at the end of label and
     * and encode the result for HTML output. A SPAN will only be generated if the label
     * is beyond the default setting of 32 characters in length.
     * 
     * @param text       to crop and encode
     * 
     * @return encoded and cropped resulting label HTML
     */
    public static String cropEncode(String text)
    {
        return cropEncode(text, 32);
    }

    /**
     * Crop a label within a SPAN element, using ellipses '...' at the end of label and
     * and encode the result for HTML output. A SPAN will only be generated if the label
     * is beyond the specified number of characters in length.
     * 
     * @param text       to crop and encode
     * @param length     length of string to crop too
     * 
     * @return encoded and cropped resulting label HTML
     */
    public static String cropEncode(String text, int length)
    {
        if (text.length() > length)
        {
            String label = text.substring(0, length - 3) + "...";
            StringBuilder buf = new StringBuilder(length + 32 + text.length());
            buf.append("<span title=\"")
               .append(StringUtils.encode(text))
               .append("\">")
               .append(StringUtils.encode(label))
               .append("</span>");
            return buf.toString();
        }
        else
        {
            return StringUtils.encode(text);
        }
    }

    /**
     * Encode a string to the %AB hex style JavaScript compatible notation.
     * Used to encode a string to a value that can be safely inserted into an HTML page and
     * then decoded (and probably eval()ed) using the unescape() JavaScript method.
     * 
     * @param s      string to encode
     * 
     * @return %AB hex style encoded string
     */
    public static String encodeJavascript(String s)
    {
        StringBuilder buf = new StringBuilder(s.length() * 3);
        for (int i=0; i<s.length(); i++)
        {
            char c = s.charAt(i);
            int iChar = (int)c;
            buf.append('%');
            buf.append(Integer.toHexString(iChar));
        }
        return buf.toString();
    }

    /**
     * Strip unsafe HTML tags from a string - only leaves most basic formatting tags
     * and encodes the remaining characters.
     * 
     * @param s HTML string to strip tags from
     * 
     * @return safe string
     */
    public static String stripUnsafeHTMLTags(String s)
    {
        return stripUnsafeHTMLTags(s, true);
    }
    
    /**
     * Strip unsafe HTML tags from a string - only leaves most basic formatting tags
     * and optionally encodes or strips the remaining characters.
     * 
     * @param s         HTML string to strip tags from
     * @param encode    if true then encode remaining html data
     * 
     * @return safe string
     */
    public static String stripUnsafeHTMLTags(String s, boolean encode)
    {
        return stripUnsafeHTMLTags(s, encode, false);
    }

    /**
     * Strip unsafe HTML tags from a string that represent an entire hml doc - only leaves most basic formatting tags
     * and optionally encodes or strips the remaining characters.
     *
     * @param doc       HTML string representing an entire hml doc to strip tags from
     * @param encode    if true then encode remaining html data
     *
     * @return safe string
     */
    public static String stripUnsafeHTMLDocument(String doc, boolean encode)
    {
        return stripUnsafeHTMLTags(doc, encode, overrideDocType, true);
    }

    /**
     * Strip unsafe HTML tags from a string - only leaves most basic formatting tags
     * and optionally encodes or strips the remaining characters.
     *
     * @param s         HTML string to strip tags from
     * @param encode    if true then encode remaining html data
     * @param overrideDocumentType if true a doctype enforcing the latest browser rendition mode will used
     *
     * @return safe string
     */
    public static String stripUnsafeHTMLTags(String s, boolean encode, boolean overrideDocumentType)
    {
        return stripUnsafeHTMLTags(s, encode, overrideDocumentType, overrideDocumentType);
    }

    /**
     * Strip unsafe HTML tags from a string - only leaves most basic formatting tags

     * @param s         HTML string to strip tags from
     * @param encode    if true then encode remaining html data (not in use)
     * @param overrideDocumentType if true a doctype enforcing the latest browser rendition mode will used
     * @param isHTMLDoc if true elements as html, head, body will be allowed in the sanitization
     *
     * @return safe string
     */
    public static String stripUnsafeHTMLTags(String s, boolean encode, boolean overrideDocumentType, boolean isHTMLDoc)
    {
        if (s != null)
        {
            PolicyFactory policy = isHTMLDoc ? docPolicy.and(basePolicy) : basePolicy;

            String unescapedHTML = Encoding.decodeHtml(s);
            String sanitizedHTML = policy.sanitize(unescapedHTML);
            StringBuffer buf = new StringBuffer();

            if (isHTMLDoc && overrideDocumentType)
            {
                buf.append('<').append(DOCTYPE).append(' ').append(HTML).append('>');
            }
            buf.append(sanitizedHTML);

            return buf.toString();
        }

        return "";
    }

    /**
     * Replace one string instance with another within the specified string
     * 
     * @param str String
     * @param repl String
     * @param with String
     * 
     * @return replaced string
     */
    public static String replace(String str, String repl, String with)
    {
        if (str == null)
        {
            return null;
        }
        
        int lastindex = 0;
        int pos = str.indexOf(repl);

        // If no replacement needed, return the original string
        // and save StringBuffer allocation/char copying
        if (pos < 0)
        {
            return str;
        }

        int len = repl.length();
        int lendiff = with.length() - repl.length();
        StringBuilder out = new StringBuilder((lendiff <= 0) ? str.length() : (str.length() + (lendiff << 3)));
        for (; pos >= 0; pos = str.indexOf(repl, lastindex = pos + len))
        {
            out.append(str.substring(lastindex, pos)).append(with);
        }

        return out.append(str.substring(lastindex, str.length())).toString();
    }

    /**
     * Remove all occurances of a String from a String
     * 
     * @param str     String to remove occurances from
     * @param match   The string to remove
     * 
     * @return new String with occurances of the match removed
     */
    public static String remove(String str, String match)
    {
        int lastindex = 0;
        int pos = str.indexOf(match);

        // If no replacement needed, return the original string
        // and save StringBuffer allocation/char copying
        if (pos < 0)
        {
            return str;
        }

        int len = match.length();
        StringBuilder out = new StringBuilder(str.length());
        for (; pos >= 0; pos = str.indexOf(match, lastindex = pos + len))
        {
            out.append(str.substring(lastindex, pos));
        }

        return out.append(str.substring(lastindex, str.length())).toString();
    }

    /**
     * Replaces carriage returns and line breaks with the &lt;br&gt; tag.
     * 
     * @param str The string to be parsed
     * @return The string with line breaks removed
     */
    public static String replaceLineBreaks(String str, boolean xhtml)
    {
        String replaced = null;

        if (str != null)
        {
            try
            {
                StringBuilder parsedContent = new StringBuilder(str.length() + 32);
                BufferedReader reader = new BufferedReader(new StringReader(str));
                String line = reader.readLine();
                while (line != null)
                {
                    parsedContent.append(line);
                    line = reader.readLine();
                    if (line != null)
                    {
                        parsedContent.append(xhtml ? "<br/>" : "<br>");
                    }
                }

                replaced = parsedContent.toString();
            }
            catch (IOException ioe)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Failed to replace line breaks in string: " + str);
                }
            }
        }

        return replaced;
    }
    
    /**
     * Join an array of values into a String value
     * 
     * @param value non-null array of objects - toString() of each value is used
     * 
     * @return concatenated string value
     */
    public static String join(final Object[] value)
    {
        return join(value, null);
    }
    
    /**
     * Join an array of values into a String value using supplied delimiter between each.
     * 
     * @param value non-null array of objects - toString() of each value is used
     * @param delim delimiter value to apply between each value - null indicates no delimiter
     * 
     * @return concatenated string value
     */
    public static String join(final Object[] value, final String delim)
    {
        final StringBuilder buf = new StringBuilder(value.length << 4);
        for (int i=0; i<value.length; i++)
        {
            if (i != 0 && delim != null)
            {
                buf.append(delim);
            }
            buf.append(value[i] != null ? value[i].toString() : "");
        }
        return buf.toString();
    }
}
