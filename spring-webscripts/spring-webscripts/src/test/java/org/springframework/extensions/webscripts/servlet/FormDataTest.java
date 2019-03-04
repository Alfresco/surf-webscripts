package org.springframework.extensions.webscripts.servlet;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FormData.class)
public class FormDataTest
{

    @Test
    public void testFormDataGetFields() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=B0");
        request.setServerName("www.alfresco.com");
        request.setRequestURI("/alfresco");
        request.setQueryString("param1=value1&param");

        final ServletFileUpload servletFileUpload = mock(ServletFileUpload.class);

        whenNew(ServletFileUpload.class).withAnyArguments().thenReturn(servletFileUpload);
        when(servletFileUpload.parseRequest(request)).thenThrow(new FileUploadException("Upload error"));

        FormData formData = new FormData(request);
        try
        {
            formData.getFields();
            fail("Should have failed with FileUploadException");
        }
        catch(WebScriptException e) {}
    }
}
