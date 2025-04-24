/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DateValue 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 * * Neither the name of the openMDX team nor the names of its
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
 */
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.control.EditInspectorControl;
import org.w3c.spi2.Datatypes;

public class DateValue
    extends AttributeValue
    implements Serializable {

    /**
     * Create a DateValue attribute.
     * @param object
     * @param fieldDef
     * @param application
     * @return
     */
    public static AttributeValue createDateValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        // Return user defined attribute value class or DateValue as default
        String valueClassName = (String)application.getMimeTypeImpls().get(fieldDef.mimeType);
        AttributeValue attributeValue = valueClassName == null ? 
        	null : 
        		AttributeValue.createAttributeValue(
	                valueClassName,
	                object,
	                fieldDef,
	                application
        		);
        return attributeValue != null ? 
        	attributeValue : 
        		new DateValue(
	                object,
	                fieldDef,
	                application
	            );
    }
    
    /**
     * Constructor 
     *
     * @param object
     * @param fieldDef
     * @param application
     */
    protected DateValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        super(
            object,
            fieldDef,
            application
        );
        try {
            this.defaultValue = Datatypes.create(Datatypes.DATE_TIME_CLASS, fieldDef.defaultValue);
        } catch(IllegalArgumentException e) {
        	// ignore
        }
    }

    /**
     * Assert that formatter has a 4-digit year as pattern.
     * @param formatter
     */
    public static void assert4DigitYear(
        SimpleDateFormat formatter
    ) {
        String pattern = formatter.toPattern();
        if((pattern.indexOf("yy") >= 0) && (pattern.indexOf("yyyy") < 0)) {
            pattern = pattern.replaceAll("yy", "yyyy");
        }
        formatter.applyPattern(pattern);
    }
    
    /**
     * Get localized date formatter with given edit style.
     * @param useEditStyle
     * @return
     */
    public SimpleDateFormat getLocalizedDateFormatter(
        boolean useEditStyle
    ) {
        return DateValue.getLocalizedDateFormatter(
            this.fieldDef.qualifiedFeatureName,
            useEditStyle,
            this.app
        );
    }

    /**
     * Get localized date formatter.
     * @param qualifiedFeatureName
     * @param useEditStyle
     * @param app
     * @return
     */
    public static SimpleDateFormat getLocalizedDateFormatter(
        String qualifiedFeatureName,
        boolean useEditStyle,        
        ApplicationContext app
    ) {
    	TimeZone timezone = app.getPortalExtension().getTimeZone(qualifiedFeatureName, app);    	
        Map<String,SimpleDateFormat> cachedDateFormatters = DateValue.cachedLocalizedDateFormatters.get();
        String key = app.getCurrentLocaleAsString() + ":" + timezone.getID() + ":" + useEditStyle;
        SimpleDateFormat dateFormatter = (SimpleDateFormat)cachedDateFormatters.get(key);
        if(dateFormatter == null) {
            dateFormatter = (SimpleDateFormat)SimpleDateFormat.getDateInstance(
                useEditStyle ? java.text.DateFormat.SHORT : app.getPortalExtension().getDateStyle(qualifiedFeatureName, app),
                app.getCurrentLocale()
            );
            dateFormatter.setTimeZone(timezone);
            DateValue.assert4DigitYear(dateFormatter);            
            cachedDateFormatters.put(
                key,
                dateFormatter
            );
        }
        return dateFormatter;
    }

    /**
     * Get localized dateTime formatter for given attribute and edit style.
     * @param useEditStyle
     * @return
     */
    public SimpleDateFormat getLocalizedDateTimeFormatter(
        boolean useEditStyle
    ) {
        return DateValue.getLocalizedDateTimeFormatter(
            this.fieldDef.qualifiedFeatureName,
            useEditStyle,
            this.app
        );
    }
  
    /**
     * Get localized dateTime formatter.
     * @param qualifiedFeatureName
     * @param useEditStyle
     * @param app
     * @return
     */
    public static SimpleDateFormat getLocalizedDateTimeFormatter(
        String qualifiedFeatureName,
        boolean useEditStyle,
        ApplicationContext app
    ) {
    	TimeZone timezone = app.getPortalExtension().getTimeZone(qualifiedFeatureName, app);
        String key = app.getCurrentLocaleAsString() + ":" + timezone.getID() + ":" + useEditStyle;
        Map<String,SimpleDateFormat> cachedDateTimeFormatters = DateValue.cachedLocalizedDateTimeFormatters.get();
        SimpleDateFormat dateTimeFormatter = cachedDateTimeFormatters.get(key);
        if(dateTimeFormatter == null) {
            dateTimeFormatter = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(
                useEditStyle ? java.text.DateFormat.SHORT : app.getPortalExtension().getDateStyle(qualifiedFeatureName, app),
                useEditStyle ? java.text.DateFormat.MEDIUM : app.getPortalExtension().getTimeStyle(qualifiedFeatureName, app),
                app.getCurrentLocale()
            );
            dateTimeFormatter.setTimeZone(timezone);
            DateValue.assert4DigitYear(dateTimeFormatter);                        
            cachedDateTimeFormatters.put(
                key,
                dateTimeFormatter
            );
        }
        return dateTimeFormatter;
    }
  
    /**
     * Return localized and formatted attribute value.
     * @param value
     * @param useEditStyle
     * @return
     */
    private String formatLocalized(
        Object value,
        boolean useEditStyle
    ) {
        SimpleDateFormat dateFormatter = this.getLocalizedDateFormatter(useEditStyle);
        SimpleDateFormat dateTimeFormatter = this.getLocalizedDateTimeFormatter(useEditStyle);
        if(Datatypes.DATE_TIME_CLASS.isInstance(value)) {
            return this.isDate() ? 
            	dateFormatter.format(value) : 
            		dateTimeFormatter.format(value);
        } else if(Datatypes.DATE_CLASS.isInstance(value)) {
            // TODO: kjdd
            // Das ist doch mit Flavour 3 gar nicht kompilierbar, oder?
            GregorianCalendar calendar = (Datatypes.DATE_CLASS.cast(value)).toGregorianCalendar(
                TimeZone.getTimeZone(app.getCurrentTimeZone()),
                this.app.getCurrentLocale(),
                null
            );
            return this.isDate() ? 
            	dateFormatter.format(calendar.getTime()) : 
            		dateTimeFormatter.format(calendar.getTime());
        } else {
        	return value == null ? null : value.toString();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#getStringifiedValueInternal(org.openmdx.portal.servlet.ViewPort, java.lang.Object, boolean, boolean, boolean)
     */
    @Override
    protected String getStringifiedValueInternal(
        ViewPort p,
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        String s = this.formatLocalized(v, forEditing).trim();
        return this.app.getHtmlEncoder().encode(s, false);
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#getDefaultValue()
     */
    @Override
    public Object getDefaultValue(
    ) {
        return this.defaultValue == null ? 
        	null : 
        		this.formatLocalized(this.defaultValue, false);
    }

    /**
     * Returns true if the attribute has format DATE.
     * @return
     */
    public boolean isDate(
    ) {
        return this.getFormat().equals(DATE_FORMAT);
    }

    /**
     * Get format of date field.
     * @return
     */
    public String getFormat(
    ) {
        return this.fieldDef.format;
    }

    /**
     * Get calendar format pattern matching the given formatter.
     * @param formatter
     * @return
     */
    public static String getCalendarFormat(
        SimpleDateFormat formatter
    )  {
        String pattern = formatter.toPattern();
        // Year
        pattern = pattern.replaceAll("yyyy", "%Y");
        pattern = pattern.replaceAll("yy", "%y");
        // Month
        pattern = pattern.replaceAll("MMM", "%b");
        pattern = pattern.replaceAll("MM", "%m");
        pattern = pattern.replaceAll("M", "%m");
        // Days
        if(pattern.indexOf("dd") >= 0) {
            pattern = pattern.replaceAll("dd", "%d");
        }
        else {
            pattern = pattern.replaceAll("d", "%d");
        }
        // Hours
        if(pattern.indexOf("a") >= 0) {
            pattern = pattern.replaceAll("hh", "%I");
            pattern = pattern.replaceAll("h", "%I");
        }
        else {
            if(pattern.indexOf("HH") >= 0) {
                pattern = pattern.replaceAll("HH", "%H");
            }
            else {
                pattern = pattern.replaceAll("H", "%H");
            }
            pattern = pattern.replaceAll("hh", "%H");            
            pattern = pattern.replaceAll("h", "%H");            
        }
        // Minutes
        pattern = pattern.replaceAll("mm", "%M");
        // Seconds
        pattern = pattern.replaceAll("ss", "%S");
        // AM/PM
        pattern = pattern.replaceAll("a", "%p");        
        return pattern;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#paint(org.openmdx.portal.servlet.attribute.Attribute, org.openmdx.portal.servlet.ViewPort, java.lang.String, java.lang.String, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void paint(
        Attribute attribute,
        ViewPort p,
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
        String lockedModifier,
        String stringifiedValue,
        boolean forEditing
    ) throws ServiceException {
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();         
        label = this.getLabel(attribute, p, label);
        String title = this.getTitle(attribute, label);
        // Edit
        if(forEditing && readonlyModifier.isEmpty()) {
            String feature = this.getName();
            id = (id == null) || (id.length() == 0) ? 
                feature + "[" + Integer.toString(tabIndex) + "]" : 
                id;            
            p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");            
            if(this.isSingleValued()) {
                String classModifier = this.isMandatory() 
                	? CssClass.valueR + " " + CssClass.mandatory
                    : CssClass.valueR.toString();                
                p.write("<td ", rowSpanModifier, ">");
                p.write("  <input id=\"", id, "\" name=\"", id, "\" type=\"text\" class=\"", classModifier, lockedModifier, "\" ", readonlyModifier, " tabindex=\"" + tabIndex, "\" value=\"", stringifiedValue, "\"");
                p.writeEventHandlers("    ", attribute.getEventHandler());
                p.write("  >");
                p.write("</td>");
                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, ">");
                if(readonlyModifier.isEmpty()) {
                    if(this.isDate()) {
                        SimpleDateFormat dateFormatter = this.getLocalizedDateFormatter(true);
                        String calendarFormat = DateValue.getCalendarFormat(dateFormatter);
                        p.write("        <a>", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".Trigger\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/cal"), p.getImgType(), "\""), "</a>");
                        p.write("        <script language=\"javascript\" type=\"text/javascript\">");
                        p.write("        Calendar.setup({");
                        p.write("          inputField   : \"", id, "\",");
                        p.write("          ifFormat     : \"" + calendarFormat + "\",");
                        p.write("          firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                        p.write("          timeFormat   : \"24\",");
                        p.write("          button       : \"", id, ".Trigger\",");
                        p.write("          align        : \"Tl\",");
                        p.write("          singleClick  : true,");
                        p.write("          showsTime    : false");
                        p.write("        });");
                        p.write("        </script>");
                    } else {
                        SimpleDateFormat dateTimeFormatter = this.getLocalizedDateTimeFormatter(true);
                        String calendarFormat = DateValue.getCalendarFormat(dateTimeFormatter);
                        p.write("        <a>", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".Trigger\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/cal"), p.getImgType(), "\""), "</a>");
                        p.write("        <script language=\"javascript\" type=\"text/javascript\">");
                        p.write("        Calendar.setup({");
                        p.write("          inputField   : \"", id, "\",");
                        p.write("          ifFormat     : \"" + calendarFormat + "\",");
                        p.write("          firstDay     : ", Integer.toString(dateTimeFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                        p.write("          timeFormat   : \"24\",");
                        p.write("          button       : \"", id, ".Trigger\",");
                        p.write("          align        : \"Tl\",");
                        p.write("          singleClick  : true,");
                        p.write("          showsTime    : true");
                        p.write("        });");
                        p.write("        </script>");
                    }
                }
                p.write("</td>");
            } else {
                p.write("<td ", rowSpanModifier, ">");
                p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"", CssClass.multiStringLocked.toString(), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"" + tabIndex, "\">", stringifiedValue, "</textarea>");
                p.write("</td>");
                p. write("<td class=\"addon\" ", rowSpanModifier, ">");
                if(readonlyModifier.isEmpty()) {
                    if(this.isDate()) {
        				p.write("<a role=\"button\" data-toggle=\"modal\" href=\"#popup_", EditInspectorControl.EDIT_DATES, "\" onclick=\"javascript:multiValuedHigh=", this.getUpperBound("1..10"), "; ", EditInspectorControl.EDIT_DATES, "_showPopup(event, this.id, popup_", EditInspectorControl.EDIT_DATES, ", 'popup_", EditInspectorControl.EDIT_DATES, "', $('", id, "'), new Array());\">");
        				p.write("    ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\" "));
        				p.write("</a>");
                    } else {
        				p.write("<a role=\"button\" data-toggle=\"modal\" href=\"#popup_", EditInspectorControl.EDIT_DATETIMES, "\" onclick=\"javascript:multiValuedHigh=", this.getUpperBound("1..10"), "; ", EditInspectorControl.EDIT_DATETIMES, "_showPopup(event, this.id, popup_", EditInspectorControl.EDIT_DATETIMES, ", 'popup_", EditInspectorControl.EDIT_DATETIMES, "', $('", id, "'), new Array());\">");
        				p.write("    ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\" "));
        				p.write("</a>");                    	
                    }
                }
                p.write("</td>");
            }
        } else {
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
                lockedModifier,
                stringifiedValue,
                forEditing
            );
        }
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3256445798152747057L;
    public static final String DATE_FORMAT = "d";
    public static final String DATETIME_FORMAT = "g";

    private static ThreadLocal<Map<String,SimpleDateFormat>> cachedLocalizedDateFormatters = new ThreadLocal<Map<String,SimpleDateFormat>>() {
        protected synchronized Map<String,SimpleDateFormat> initialValue() {
            return new HashMap<String,SimpleDateFormat>();
        } 
    };
    private static ThreadLocal<Map<String,SimpleDateFormat>> cachedLocalizedDateTimeFormatters = new ThreadLocal<Map<String,SimpleDateFormat>>() {
        protected synchronized Map<String,SimpleDateFormat> initialValue() {
            return new HashMap<String,SimpleDateFormat>();
        }
    };
    private #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif defaultValue = null;

}

//--- End of File -----------------------------------------------------------
