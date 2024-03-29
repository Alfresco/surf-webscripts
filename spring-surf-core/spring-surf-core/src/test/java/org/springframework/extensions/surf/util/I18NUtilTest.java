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

package org.springframework.extensions.surf.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

/**
 * I18NUtil unit tests
 * 
 * @author Roy Wetherall
 */
public class I18NUtilTest extends TestCase
{
    private static final String BASE_RESOURCE_NAME = "org.springframework.extensions.surf.util.testMessages";
    private static final String BUNDLE1_RESOURCE_NAME = "org.springframework.extensions.surf.util.bundle1";
    private static final String BUNDLE2_RESOURCE_NAME = "org.springframework.extensions.surf.util.bundle2";
    private static final String BUNDLE_MESSAGE = "the.same.message";
    private static final String BUNDLE_VALUE1 = "bundle1_default";
    private static final String BUNDLE_VALUE1_FR = "bundle1_fr";
    private static final String BUNDLE_VALUE2 = "bundle2_default";
    private static final String BUNDLE_VALUE2_FR = "bundle2_fr";
    private static final String PARAM_VALUE = "television";
    private static final String MSG_YES = "msg_yes";    
    private static final String MSG_NO = "msg_no";
    private static final String MSG_PARAMS = "msg_params";
    private static final String VALUE_YES = "Yes";
    private static final String VALUE_NO = "No";
    private static final String VALUE_PARAMS = "What no " + PARAM_VALUE + "?";
    private static final String VALUE_FR_YES = "Oui";
    private static final String VALUE_FR_NO = "Non";
    private static final String VALUE_FR_PARAMS = "Que non " + PARAM_VALUE + "?";
   
    @Override
    protected void setUp() throws Exception
    {
        // Re-set the current locale to be the default
        Locale.setDefault(Locale.ENGLISH);
        I18NUtil.setLocale(Locale.getDefault());
    }
    
    /**
     * Test the set and get methods
     */
    public void testSetAndGet()
    {
        // Check that the default locale is returned 
        assertEquals(Locale.getDefault(), I18NUtil.getLocale());
        
        // Set the locals
        I18NUtil.setLocale(Locale.CANADA_FRENCH);
        assertEquals(Locale.CANADA_FRENCH, I18NUtil.getLocale());
        
        // Reset the locale
        I18NUtil.setLocale(null);
        assertEquals(Locale.getDefault(), I18NUtil.getLocale());
    }
    
    /**
     * Test getMessage
     */
    public void testGetMessage()
    {
        // Check with no bundles loaded
        assertNull(I18NUtil.getMessage(MSG_NO));        
        
        // Register the bundle
        I18NUtil.registerResourceBundle(BASE_RESOURCE_NAME);

        // Check default values
        assertEquals(VALUE_YES, I18NUtil.getMessage(MSG_YES));
        assertEquals(VALUE_NO, I18NUtil.getMessage(MSG_NO));
        
        // Check not existant value
        assertNull(I18NUtil.getMessage("bad_key"));        
        
        // Change the locale and re-test
        I18NUtil.setLocale(new Locale("fr", "FR"));
        
        // Check values
        assertEquals(VALUE_FR_YES, I18NUtil.getMessage(MSG_YES));
        assertEquals(VALUE_FR_NO, I18NUtil.getMessage(MSG_NO));
        
        // Check values when overriding the locale
        assertEquals(VALUE_YES, I18NUtil.getMessage(MSG_YES, Locale.getDefault()));
        assertEquals(VALUE_NO, I18NUtil.getMessage(MSG_NO, Locale.getDefault()));
    }
    
    /**
     * Test getting a parameterised message
     */
    public void testGetMessageWithParams()
    {
        // Register the bundle
        I18NUtil.registerResourceBundle(BASE_RESOURCE_NAME);
        
        // Check the default value
        assertEquals(VALUE_PARAMS, I18NUtil.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));
            
        // Change the locale and re-test
        I18NUtil.setLocale(new Locale("fr", "FR"));
        
        // Check the default value
        assertEquals(VALUE_FR_PARAMS, I18NUtil.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));       
        
        // Check values when overriding the locale
        assertEquals(VALUE_PARAMS, I18NUtil.getMessage(MSG_PARAMS, Locale.getDefault(), new Object[]{PARAM_VALUE}));
    }
    
    public void testLocaleMatching()
    {
        Set<Locale> options = new HashSet<Locale>(13);
        options.add(Locale.FRENCH);                 // fr
        options.add(Locale.FRANCE);                 // fr_FR
        options.add(Locale.CANADA);                 // en_CA
        options.add(Locale.CANADA_FRENCH);          // fr_CA
        options.add(Locale.CHINESE);                // zh
        options.add(Locale.TRADITIONAL_CHINESE);    // zh_TW
        options.add(Locale.SIMPLIFIED_CHINESE);     // zh_CN
        // add some variants
        Locale fr_FR_1 = new Locale("fr", "FR", "1");
        Locale zh_CN_1 = new Locale("zh", "CN", "1");
        Locale zh_CN_2 = new Locale("zh", "CN", "2");
        Locale zh_CN_3 = new Locale("zh", "CN", "3");
        options.add(zh_CN_1);                       // zh_CN_1
        options.add(zh_CN_2);                       // zh_CN_2
        
        Set<Locale> chineseMatches = new HashSet<Locale>(3);
        chineseMatches.add(Locale.SIMPLIFIED_CHINESE);
        chineseMatches.add(zh_CN_1);                      
        chineseMatches.add(zh_CN_2);   
        
        Set<Locale> frenchMatches = new HashSet<Locale>(3);
        frenchMatches.add(Locale.FRANCE);
        
        // check
        assertEquals(Locale.CHINA, I18NUtil.getNearestLocale(Locale.CHINA, options));
        assertEquals(Locale.CHINESE, I18NUtil.getNearestLocale(Locale.CHINESE, options));
        assertEquals(zh_CN_1, I18NUtil.getNearestLocale(zh_CN_1, options));
        assertEquals(zh_CN_2, I18NUtil.getNearestLocale(zh_CN_2, options));
        assertTrue(chineseMatches.contains(I18NUtil.getNearestLocale(zh_CN_3, options)));         // must match the last variant - but set can have any order an IBM JDK differs!
        assertEquals(Locale.FRANCE, I18NUtil.getNearestLocale(fr_FR_1, options)); // same here
        
        // now test the match for just anything
        Locale na_na_na = new Locale("", "", "");
        Locale check = I18NUtil.getNearestLocale(na_na_na, options);
        assertNotNull("Expected some kind of value back", check);
    }
    
    public void testLocaleParsing()
    {
        assertEquals(Locale.FRANCE, I18NUtil.parseLocale("fr_FR"));
        assertEquals(new Locale("en", "GB", "cockney"), I18NUtil.parseLocale("en_GB_cockney"));
        assertEquals(new Locale("en", "GB", ""), I18NUtil.parseLocale("en_GB"));
        assertEquals(new Locale("en", "", ""), I18NUtil.parseLocale("en"));
        assertEquals(Locale.getDefault(), I18NUtil.parseLocale(""));
        assertEquals(new Locale("pt", "PT"), I18NUtil.parseLocale("pt_PT"));
        assertEquals(new Locale("pt", "BR"), I18NUtil.parseLocale("pt_BR"));
        assertEquals(Locale.UK, I18NUtil.parseLocale("en_GB"));
        assertEquals(Locale.getDefault(), I18NUtil.parseLocale("\"><sCrIpT>alert(26118)</sCrIpT>"));
        assertEquals(Locale.getDefault(), I18NUtil.parseLocale("alert(1)"));
        assertEquals(Locale.getDefault(), I18NUtil.parseLocale("<button onclick=alert(1)>"));
        assertEquals(Locale.getDefault(), I18NUtil.parseLocale("123abc"));
        assertEquals(new Locale("abc"), I18NUtil.parseLocale("abc"));
        assertEquals(Locale.ITALY, I18NUtil.parseLocale("it_IT"));
        assertEquals(Locale.ITALY, I18NUtil.parseLocale("it_IT_"));
        assertEquals(new Locale("pt"), I18NUtil.parseLocale("pt_"));
    }

    public void testResourceBundleOrder()
    {
        String resourceBundle1 = BUNDLE1_RESOURCE_NAME;
        String resourceBundle2 = BUNDLE2_RESOURCE_NAME;
        I18NUtil.registerResourceBundle(resourceBundle1);
        I18NUtil.registerResourceBundle(resourceBundle2);

        // The latest bundle should override the previous
        I18NUtil.setLocale(null);
        assertEquals(BUNDLE_VALUE2, I18NUtil.getMessage(BUNDLE_MESSAGE));
        I18NUtil.setLocale(new Locale("fr", "FR"));
        assertEquals(BUNDLE_VALUE2_FR, I18NUtil.getMessage(BUNDLE_MESSAGE));
    }

    public void testSetLocaleFromLanguage()
    {
        I18NUtil.setLocaleFromLanguage("pt-PT,en;q=0.9");
        assertEquals(new Locale("pt", "PT"), I18NUtil.getLocale());

        I18NUtil.setLocaleFromLanguage("en,pt-BR;q=0.9,pt;q=0.8,en-US;q=0.7");
        assertEquals(new Locale("en"), I18NUtil.getLocale());

        I18NUtil.setLocaleFromLanguage("\"><sCrIpT>alert(26118)</sCrIpT>");
        assertEquals(Locale.getDefault(), I18NUtil.getLocale());
    }
}
