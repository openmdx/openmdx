/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: BinaryValue.java,v 1.29 2008/06/18 22:42:30 wfro Exp $
 * Description: BinaryValue
 * Revision:    $Revision: 1.29 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/18 22:42:30 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the location of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.attribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;

public class BinaryValue 
    extends AttributeValue
    implements Serializable {

    //-------------------------------------------------------------------------
    public static String getMimeType(
        Object object,
        String feature,
        String configuredMimeType     
    ) {
        String mimeType = null;

        // configured mime type overrides instance-level mime type
        if(configuredMimeType != null) {
            mimeType = configuredMimeType;
        }
        else {
	        String mimeTypeFeature = feature + "MimeType";
	       
	        // try to get mime type from instance
	        if(object == null) {
	            mimeType = null;
	        }
	        else if(object instanceof RefObject_1_0) {
	          try {
	            mimeType = (String)((RefObject_1_0)object).refGetValue(
	                mimeTypeFeature
	            );
	          }
	          catch(JmiServiceException e) {}
	        }
	        else {
	            mimeType = (String)((Map)object).get(mimeTypeFeature);
	        }
	      	      
	        // not defined on object. return default
	        if(mimeType == null) {
	            mimeType = DEFAULT_MIME_TYPE;
	        }
        }
        return mimeType;
    }
  
    //-------------------------------------------------------------------------
    public static AttributeValue createBinaryValue(
        Object object, 
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        String mimeType = BinaryValue.getMimeType(
            object,
            fieldDef.qualifiedFeatureName,
            fieldDef.mimeType
        );
      
        // find matching user-defined mime type class according to the customized mime type
        String mimeTypeWithoutParams = mimeType;
        int pos = 0;
        if((pos = mimeType.indexOf(";")) >= 0) {
            mimeTypeWithoutParams = mimeType.substring(0, pos);
        }

        // Return user defined attribute value class or BinaryValue as default
        String valueClassName = (String)application.getMimeTypeImpls().get(mimeTypeWithoutParams);
        AttributeValue attributeValue = valueClassName == null
            ? null
            : AttributeValue.createAttributeValue(
                valueClassName,
                object,
                fieldDef,
                application
              );
        return attributeValue != null
            ? attributeValue
            : new BinaryValue(
                object,
                fieldDef,
                application
              );
    }
  
  //-------------------------------------------------------------------------
  protected BinaryValue(
    Object object, 
    FieldDef fieldDef,
    ApplicationContext application
  ) {
    super(
        object, 
        fieldDef,
        application
    );
    
    // mimeType
    AppLog.trace("getting type for " + this.fieldDef.qualifiedFeatureName + " for", this.object);
    this.mimeType = BinaryValue.getMimeType(
        object,
        fieldDef.qualifiedFeatureName,
        fieldDef.mimeType
    );

    // name
    AppLog.trace("getting name for " + this.fieldDef.qualifiedFeatureName + " for", this.object);
    this.name = this.getString(this.fieldDef.qualifiedFeatureName + "Name", false);
    if(this.name == null) {
        this.name = DEFAULT_NAME;
    }

    ModelElement_1_0 featureDef = null;
    try {
        featureDef = application.getModel().getElement(this.fieldDef.qualifiedFeatureName);
    }
    catch(ServiceException e) {
        AppLog.warning("can not get feature definition");
        AppLog.warning(e.getMessage(), e.getCause());
    }
    // For features for type <<stream> binary the binary value 
    // is retrieved on-demand on EVENT_DOWNLOAD
    if(
       (this.object instanceof RefObject_1_0) &&
       (featureDef != null)
    ) {
        // <<stream>> org:w3c:binary can not be optional
        if(Multiplicities.STREAM.equals(featureDef.values("multiplicity").get(0))) {
            this.isNull = false;
        }
        // Check whether binary value is empty
        else {
            Object bytes = super.getValue(false);
            if(bytes instanceof Collection) {
                bytes = ((Collection)bytes).iterator().next();
            }
            this.isNull = bytes == null;
        }
        if(!this.isNull) {
            String encodedName = this.name;
            try {
                encodedName = URLEncoder.encode(this.name, "UTF-8");
            } catch(Exception e) {}
            this.downloadAction = 
                new Action(
                    Action.EVENT_DOWNLOAD_FROM_FEATURE,
                    new Action.Parameter[]{
                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, ((RefObject_1_0)this.object).refMofId()),
                        new Action.Parameter(Action.PARAMETER_FEATURE, this.fieldDef.qualifiedFeatureName),
                        new Action.Parameter(Action.PARAMETER_NAME, encodedName),
                        new Action.Parameter(Action.PARAMETER_MIME_TYPE, this.mimeType)
                    },
                    this.application.getTexts().getClickToDownloadText() + " " + this.name,
                    true
                );
        }        
    }
    // For transient objects the binary value is stored in temporary
    // file which is returned on EVENT_DOWNLOAD
    else {
        String location = null;        
        byte[] bytes = null;
        Object value = super.getValue(false);
        if(value instanceof Collection) {
            bytes = (byte[])((Collection)value).iterator().next();
        }
        else {
            bytes = (byte[])value;
        }
        AppLog.trace("bytes", "" + (bytes == null ? -1 : bytes.length));
        this.isNull = bytes == null;
        this.downloadAction = null;        
        // Only create temporary file if the content is not null
        // and if it muste be prepared for download. inPlace content
        // is never downloaded
        if(!this.isNull && !this.fieldDef.isInPlace) {
            try {
                location = UUIDs.getGenerator().next().toString();
                File f = new File(
                    application.getTempFileName(location, "")
                );
                OutputStream os = new FileOutputStream(f);
                os.write(bytes);
                os.flush();
                os.close();
            }
            catch(Exception e) {
                ServiceException e0 = new ServiceException(e);
                AppLog.warning(e0.getMessage(), e0.getCause());
            }
        }
        if(!this.isNull) {
            this.downloadAction = 
                new Action(
                    Action.EVENT_DOWNLOAD_FROM_LOCATION,
                    new Action.Parameter[]{
                        new Action.Parameter(Action.PARAMETER_LOCATION, location),
                        new Action.Parameter(Action.PARAMETER_NAME, this.name),
                        new Action.Parameter(Action.PARAMETER_MIME_TYPE, this.mimeType)
                    },
                    this.application.getTexts().getClickToDownloadText() + " " + this.name,
                    true
                );
        }        
     }
  }
  
  //-------------------------------------------------------------------------
  public boolean isInPlace(
  ) {
    return this.fieldDef.isInPlace;  
  }
  
  //-------------------------------------------------------------------------
  public String getMimeType(
  ) {
    return this.mimeType;  
  }
  
  //-------------------------------------------------------------------------
  public Object getValue(
      boolean shortFormat
  ) {
      return this.downloadAction;
  }
  
  //-------------------------------------------------------------------------
  public Object getDefaultValue(
  ) {
      return null;
  }
  
  //-------------------------------------------------------------------------
  public void getBinaryValue(
      OutputStream os
  ) throws ServiceException {
      try {
          Object value = super.getValue(false);
          if(value instanceof Collection) {
              value = ((Collection)value).iterator().next();
          }
          if(value instanceof byte[]) {
              byte[] bytes = (byte[])value;
              for(int i = 0; i < bytes.length; i++) {
                  os.write(bytes[i]);
              }
          }
          else if(value instanceof InputStream) {
              InputStream is = (InputStream)value;          
              int b = 0;
              while((b = is.read()) != -1) {
                  os.write(b);              
              }          
          }
      }
      catch(Exception e) {
          throw new ServiceException(e);
      }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Prepares a single stringified Value to append.
   */
  protected String getStringifiedValueInternal(
      HtmlPage p, 
      Object v,
      boolean multiLine,
      boolean forEditing,
      boolean shortFormat
  ) {
      ApplicationContext app = p.getApplicationContext();
      HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
      if(forEditing) {
          return super.getStringifiedValueInternal(
              p, 
              v, 
              multiLine, 
              forEditing,
              shortFormat
          );
      }
      else {
          Action action = (Action)v;
          return "<a href=\"\" onclick=\"javascript:this.href=" + p.getEvalHRef(action) + ";\">" + htmlEncoder.encode(action.getTitle(), false) + "</a>";
      }
  }
  
  //-------------------------------------------------------------------------
  protected Map getMimeTypeParams(
  ) {
      Map<String,String> params = new HashMap<String,String>();
      int pos = 0;
      if((pos = this.mimeType.indexOf(";")) >= 0) {
          StringTokenizer tokenizer = new StringTokenizer(this.mimeType.substring(pos+1), ";");
          while(tokenizer.hasMoreTokens()) {
              String token = tokenizer.nextToken();
              if((pos = token.indexOf("=")) >= 0) {
                  params.put(
                      token.substring(0, pos),
                      token.substring(pos+1)
                  );
              }
          }
      }
      return params;
  }
  
  //-------------------------------------------------------------------------
  protected Set getAcceptedMimeTypes(
      HttpServletRequest request
  ) {
      // get accepted mime types. Required for rendering binaries  
      Set<String> acceptedMimeTypes = new HashSet<String>();
      StringTokenizer mimeTypeTokenizer = new StringTokenizer(request.getHeader("accept"), ", ");
      while(mimeTypeTokenizer.hasMoreTokens()) {
        String mimeType = mimeTypeTokenizer.nextToken();
        // this allows a startsWith comparison later on
        if(mimeType.indexOf("*") >= 0) {
          acceptedMimeTypes.add(mimeType.substring(0, mimeType.indexOf("*")));
        }
        else {
          acceptedMimeTypes.add(mimeType);
        }
      }
      return acceptedMimeTypes;
  }
  
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void paint(
        Attribute attribute,
        HtmlPage p,
        String id,
        String label,
        RefObject_1_0 lookupObject,
        int nCols,
        int tabIndex,
        String gapModifier,
        String styleModifier,
        String widthModifier,
        String rowSpanModifier,
        String readonlyModifier,
        String disabledModifier,
        String lockedModifier,
        String stringifiedValue,
        boolean forEditing
    ) throws ServiceException { 
        Texts_1_0 texts = this.application.getTexts();
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();                
        if(label == null) {
            label = attribute.getLabel();
            label += label.length() == 0 ? "" : ":";        
        }
        if(forEditing) {
            String idTag = id == null
                ? ""
                : "id=\"" + id + "\"";                                                                        
            p.write("<td class=\"label\"><span class=\"nw\">", htmlEncoder.encode(label, false), "</span></td>");            
            String feature = this.getName();
            p.write("<td ", rowSpanModifier, ">");
            p.write("  <input ", idTag, " type=\"file\" class=\"valueL", lockedModifier, "\" name=\"", feature, "[", Integer.toString(tabIndex), "]\" ", readonlyModifier, " ", disabledModifier, " tabindex=\"", Integer.toString(tabIndex), "\" title=\"", texts.getEnterNullToDeleteText(), "\">");
            p.write("</td>");
            p.write("<td class=\"addon\" ", rowSpanModifier, ">");            
        }
        else {
            if(stringifiedValue.length() == 0) {
                super.paint(
                    attribute,
                    p,
                    id, 
                    label,
                    lookupObject,
                    nCols,
                    tabIndex,
                    gapModifier,
                    styleModifier,
                    widthModifier,
                    rowSpanModifier,
                    readonlyModifier,
                    disabledModifier,
                    lockedModifier,
                    stringifiedValue,
                    forEditing
                );
            }
            else {                                            
                Map popupImages = (Map)p.getProperty(HtmlPage.PROPERTY_POPUP_IMAGES);
                HttpServletRequest request = p.getHttpServletRequest();          
                String result = "";
                styleModifier = "style=\"height: " + (1.2+(attribute.getSpanRow()-1)*1.5) + "em\"";
                Action binaryValueAction = (Action)this.getValue(false);
              
                Set acceptedMimeTypes = this.getAcceptedMimeTypes(request);
                // mimeType                                     
                boolean isAcceptedMimeType = false;
                for(Iterator i = acceptedMimeTypes.iterator(); i.hasNext(); ) {
                    if(this.getMimeType().startsWith((String)i.next())) {
                        isAcceptedMimeType = true;
                        break;
                    }
                }          
                // in place
                if(
                    this.isInPlace() && 
                    (binaryValueAction != null) && 
                    isAcceptedMimeType
                ) {
                    String imageId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();
                    CharSequence imageSrc = p.getEncodedHRef(binaryValueAction);
                    if(popupImages != null) {
                        popupImages.put(imageId, imageSrc);
                    }
        
                    // single-valued BinaryValue in place
                    result += gapModifier; 
                    result += "<td class=\"label\"><span class=\"nw\">" + htmlEncoder.encode(label, false) + "</span></td>";
                    result += "<td " + rowSpanModifier + " class=\"valueL\" " + widthModifier + " id=\"tdImage" + imageId + "\">";
                    result += "<div class=\"valuePicture\" " + styleModifier + ">";
                    result += p.getImg("class=\"picture\" src=\"" + imageSrc + "\" id=\"image" + imageId + "\" ondblclick=\"return showImage('divImgPopUp" + imageId + "', 'popUpImg" + imageId + "', 'tdImage" + imageId + "', this.id);\" alt=\"\"");
                    result += "</div>";
                    result += "</td>";                                                    
                }
                // single-valued BinaryValue as link -->
                else {
                    result += gapModifier;
                    result += "<td class=\"label\"><span class=\"nw\">" + label + "</span></td>";
                    result += "<td " + rowSpanModifier + " class=\"valueL\" " + widthModifier + ">";
                    result += "<div class=\"field\">" + attribute.getStringifiedValue(p, false, false) + "</div>";
                    result += "</td>";
                }
                p.write(result);
            }
        }
  }

  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3761967151120333111L;
  
  protected static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  protected static final String DEFAULT_NAME = "unknown.bin";

  protected String name;
  protected boolean isNull;
  protected String mimeType = null;
  protected Action downloadAction = null;
  
}

//--- End of File -----------------------------------------------------------
