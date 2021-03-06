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

import java.io.OutputStream;
import java.io.Writer;


/**
 * Converts a Java Object to a mimetype
 * 
 * @author davidc
 * @param <Type>
 */
public interface FormatWriter<Type>
{
    /**
     * Gets the source Java class to convert from
     * 
     * @return  Java class
     */
    public Class<? extends Type> getSourceClass();
    
    /**
     * Gets the mimetype to convert to
     * 
     * @return  mimetype
     */
    public String getDestinationMimetype();

    /**
     * Converts Java object to mimetype
     * 
     * @param object Type
     * @param output Writer
     */
    public void write(Type object, Writer output);
    
    /**
     * Converts Java object to mimetype
     * 
     * @param object Type
     * @param output OutputStream
     */
    public void write(Type object, OutputStream output);
    
}
