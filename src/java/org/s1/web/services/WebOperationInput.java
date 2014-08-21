package org.s1.web.services;

import groovy.json.JsonSlurper;
import org.s1.web.services.formats.*;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Web Operation input
 *
 * @author Grigory Pykhov
 */
public class WebOperationInput {
    private String id = UUID.randomUUID().toString();
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object data;

    protected <T> T getData(){
        return (T)data;
    }

    protected <T> T setData(T data){
        this.data = data;
        return data;
    }

    /**
     * @param request  Request
     * @param response Response
     */
    public WebOperationInput(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Get request id
     *
     * @return Request id
     */
    public String getId() {
        return id;
    }

    /**
     * @return Request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @return Response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    public JSONData asJSON() {
        if(this.data!=null)
            return getData();
        try {
            return setData(new JSONData(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TextData asText() {
        if(this.data!=null)
            return getData();
        try {
            return setData(new TextData(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public XMLData asXML() {
        if(this.data!=null)
            return getData();
        try {
            return setData(new XMLData(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FormData asForm() {
        if(this.data!=null)
            return getData();
        return setData(new FormData(request));
    }

    public MultipartFormData asMultipart() {
        if(this.data!=null)
            return getData();
        try {
            return setData(new MultipartFormData(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    public SOAPData asSOAP() {
        if(this.data!=null)
            return getData();
        try {
            return setData(new SOAPData(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
