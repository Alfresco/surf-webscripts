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

package org.springframework.extensions.webscripts.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaFileCleaner;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.WebScriptsPlatformException;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.InputStreamContent;
import org.springframework.extensions.webscripts.WebScriptException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Form Data
 * 
 * @author davidc
 */
public class FormData implements Serializable
{
    private static final long serialVersionUID = 1832644544828452385L;
    
    /** Logger */
    private static Log logger = LogFactory.getLog(FormData.class);
    
    private HttpServletRequest req;
    private Charset encoding = null;
    private JakartaServletFileUpload upload;
    private FormField[] fields = null;
    private Map<String, String[]> parameters = null;

    
    /**
     * Construct
     * 
     * @param req HttpServletRequest
     */
    public FormData(HttpServletRequest req)
    {
        this.req = req;
    }

    /**
     * Determine if multi-part form data has been provided
     * 
     * @return  true => multi-part
     */
    public boolean getIsMultiPart()
    {
        return upload.isMultipartContent(req);
    }

    /**
     * Determine if form data has specified field
     * 
     * @param name  field to look for
     * @return  true => form data contains field
     */
    public boolean hasField(String name)
    {
        for (FormField field : fields)
        {
           if (field.getName().equals(name))
           {
               return true;
           }
        }
        return false;
    }

    /**
     * Helper to parse servlet request form data
     * 
     * @return  map of all form fields
     */
    public FormField[] getFields()
    {
        // NOTE: This class is not thread safe - it is expected to be constructed on each thread.
        if (fields == null)
        {
            FileItemFactory factory = getFileItemFactory();
            upload = new JakartaServletFileUpload(factory);
            encoding = getRequestCharsetEncoding();
            upload.setHeaderCharset(encoding);
            
            try
            {
                List<FileItem> fileItems = upload.parseRequest(req);
                fields = new FormField[fileItems.size()];
                for (int i = 0; i < fileItems.size(); i++)
                {
                    FormField formField = new FormField(fileItems.get(i));
                    fields[i] = formField;
                }
            }
            catch(FileUploadException e)
            {
                if(e.getMessage().contains("no multipart boundary was found"))
                {
                    throw new WebScriptException(415, e.getMessage(), e);
                }

                throw new WebScriptException(507, e.getMessage(), e);
            }
            
        }
        return fields;
    }

    private FileItemFactory getFileItemFactory()
    {
        FileCleaningTracker fileCleaningTracker = JakartaFileCleaner.getFileCleaningTracker(req.getServletContext());
        return DiskFileItemFactory.builder().setFileCleaningTracker(fileCleaningTracker).get();
    }

    private Charset getRequestCharsetEncoding()
    {
        final String enc = req.getCharacterEncoding();
        if (enc == null || enc.isBlank()) return null;

        try
        {
            return Charset.forName(enc);
        }
        catch (UnsupportedCharsetException e)
        {
            logger.warn("Failed to obtain request encoding for `" + enc + "`.");
            return null;
        }
    }

    /**
     * Cleanup all temporary resources used by this form data
     * 
     * NOTE: Only invoke this after all required processing of FileItems is done (e.g.
     *       retrieval of content)
     */
    public void cleanup()
    {
        if (fields == null)
        {
            return;
        }

        for (int i = 0; i < fields.length; i++)
        {
            fields[i].cleanup();
        }
    }
    
    /**
     * Gets parameters encoded in the form data
     * 
     * @return  map (name, value) of parameters
     */
    /*package*/ public Map<String, String[]> getParameters()
    {
        if (parameters == null)
        {
            FormField[] fields = null;
            try
            {
                fields = getFields();
            }
            catch (WebScriptException e)
            {
                fields = new FormField[0];
            }
            parameters = new HashMap<String, String[]>(fields.length);
            for (FormField field : fields)
            {
                String[] vals = parameters.get(field.getName());
                if (vals == null)
                {
                    parameters.put(field.getName(), new String[] {field.getValue()});
                }
                else
                {
                    String[] valsNew = new String[vals.length +1]; 
                    System.arraycopy(vals, 0, valsNew, 0, vals.length);
                    valsNew[vals.length] = field.getValue();
                    parameters.put(field.getName(), valsNew);
                }
            }
        }
        return parameters;
    }
    

    /**
     * Form Field
     * 
     * @author davidc
     */
    public class FormField implements Serializable
    {
        private static final long serialVersionUID = -6061565518843862346L;
        private FileItem file;

        /**
         * Construct
         * 
         * @param file FileItem
         */
        public FormField(FileItem file)
        {
            this.file = file;
        }
        
        /**
         * @return  field name
         */
        public String getName()
        {
            return file.getFieldName();
        }
        
        /**
         * @return  true => field represents a file
         */
        public boolean getIsFile()
        {
            return !file.isFormField();
        }
        
        /**
         * @return field value (for form fields)
         *         for file upload fields, the file name is returned - use getContent() instead.
         */
        public String getValue()
        {
            try
            {
                String value;
                if (file.isFormField())
                {
                    value = (encoding != null ? file.getString(encoding) : file.getString());
                }
                else
                {
                    // For large/binary files etc. we never immediately load the content directly into memory!
                    // This would be extremely bad for say large binary file uploads etc.
                    value = file.getName();
                }
                return value;
            }
            catch (IOException e)
            {
                throw new WebScriptsPlatformException("Unable to decode form field", e);
            }
        }
        
        /**
         * @return  field as content
         */
        public Content getContent()
        {
            try
            {
                return new InputStreamContent(file.getInputStream(), getMimetype(), null);
            }
            catch (IOException e)
            {
                if (logger.isWarnEnabled())
                    logger.warn("Failed to get content: " + e.getMessage());
                
                return null;
            }
        }
        
        /**
         * @return InputStream to contents of file
         */
        public InputStream getInputStream()
        {
            try
            {
                return file.getInputStream();
            }
            catch (IOException e)
            {
                if (logger.isWarnEnabled())
                    logger.warn("Failed to get input stream: " + e.getMessage());
                
                return null;
            }
        }

        /**
         * @return  mimetype
         */
        public String getMimetype()
        {
            return file.getContentType();
        }

        /**
         * @return  filename (only for file fields, otherwise null)
         */
        public String getFilename()
        {
            // workaround a bug in IE where the full path is returned
            return FilenameUtils.getName(file.getName());
        }
        
        /**
         * Cleanup any temporary resources associated with this form field
         * 
         * NOTE: This should only be invoked after processing (e.g. retrieval of content) of the form field is done
         */
        public void cleanup()
        {
            if (getIsFile())
            {
                try
                {
                    file.delete();
                }
                catch (IOException e)
                {
                    throw new WebScriptsPlatformException("Unable to cleanup form field", e);
                }
            }
        }
    }
    
}
