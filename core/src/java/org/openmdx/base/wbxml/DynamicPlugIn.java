/*
 * ==================================================================== 
 * Project: openMDX/Core, http://www.openmdx.org/ 
 * Description: Dynamic Plug-In 
 * Owner: OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as listed in
 * the NOTICE file.
 */
package org.openmdx.base.wbxml;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * The dynamic plug-in is able to amend its tables:<ul>
 * <li><code>ext0()</code> calls amend the code tables
 * <li><code>ext1()</code> calls amend the string table 
 * </ul>
 */
public class DynamicPlugIn extends AbstractPlugIn {

    /**
     * Constructor
     * 
     * @param page0 the (usually static) page 0
     */
    protected DynamicPlugIn(
        Page page0
    ) {
        this.page0 = page0;
    }

    /**
     * Page 0 is usually static
     */
    private final Page page0;

    /**
     * Pages 1.. 254
     */
    private final Page[] pages = new Page[254];

    /**
     * The target is set by an extT0() invocation
     */
    private CodeToken codeTarget;

    /**
     * The target is set by an ext1() invocation
     */
    private int stringTarget = -1;

    /**
     * The dynamic string table size in case of UTF-16 encoding
     */
    private int stringSinkSize = 0;

    /**
     * The dynamic string sink
     */
    private final Map<String, Integer> stringSink = new HashMap<String, Integer>();

    /**
     * The dynamic string sink
     */
    private final Map<Integer, String> stringSource = new HashMap<Integer, String>();

    /**
     * The dynamic plug-in's <em>initial</em> string table is always empty!
     */
    private static ByteBuffer stringTable = ByteBuffer.wrap(new byte[]{});
    
    /**
     * Page factory method
     * 
     * @return a new page
     */
    protected static Page newPage() {
        return new Page();
    }

    /**
     * Register values for page 0
     * 
     * @param codeTarget
     * @param value
     */
    protected static void addTo(Page page0, CodeSpace codeSpace, String value) {
        String[] target = page0.get(codeSpace);
        int index = getIndex(value);
        if (target[index] == null) {
            target[index] = value;
            SysLog.log(
                Level.FINE,
                "Value \"{0}\" is mapped to [{1}, {2}, {3}]",
                value,
                codeSpace,
                0,
                index);
        } else if (!value.equals(target[index])) {
            SysLog.log(
                Level.WARNING,
                "Both values \"{0}\" and \"{4}\" are mapped to [{1}, {2}, {3}]]",
                value,
                codeSpace,
                0,
                index,
                target[index]
           );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.wbxml.AbstractPlugIn#reset()
     */
    @Override
    public void reset() {
        super.reset();
        for (Page page : pages) {
            if(page == null) break;
            page.clear();
        }
        this.codeTarget = null;
        this.stringTarget = -1;
        this.stringSinkSize = 0;
        this.stringSink.clear();
        this.stringSource.clear();
        super.reset();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getStringTable()
     */
    @Override
    public ByteBuffer getStringTable(
    ) throws ServiceException {
        return stringTable;
    }

    private static final int getIndex(
        String value
    ){
        return Math.abs(value.hashCode() % Page.SIZE);
    }
    
    /**
     * Retrieve an exact-match token
     * 
     * @param codeSpace
     * @param readOnly
     * @param value
     * 
     * @return the corresponding token, or <code>null</code> if no such entry is
     *         available
     */
    private CodeToken getExactMatch(
        CodeSpace codeSpace, 
        String value
    ) {
        int index = getIndex(value);
        if(value.equals(this.page0.get(codeSpace)[index])) {
            return codeSpace.newToken(
                0, 
                index, 
                value.length(), 
                false
            ); // pre-existing entry
        }
        for(
           int page = 1;
           page <= this.pages.length;
           page++
        ){
            Page lazilyAllocatedPage = this.pages[page-1];
            if(lazilyAllocatedPage == null) {
                lazilyAllocatedPage = this.pages[page-1] = newPage();
            }
            String[] sector = lazilyAllocatedPage.get(codeSpace);
            String entry = sector[index];
            if (entry == null) {
                sector[index] = value;
                return codeSpace.newToken(
                    page, 
                    index, 
                    value.length(), 
                    true // newly created entry
                ); 
            }
            if (entry.equals(value)) {
                return codeSpace.newToken(
                    page, 
                    index, 
                    value.length(), 
                    false // pre-existing entry
                ); 
            }
        }
        return null; // pages exhausted
    }

    /**
     * Retrieve a best-match token
     * 
     * @param codeSpace
     * @param readOnly
     * @param value
     * 
     * @return the corresponding token, or <code>null</code> if no such entry is
     *         available
     */
    private CodeToken getBestMatch(
        CodeSpace codeSpace,
        boolean set,
        String value
    ) {
        int bestPage = -1; // page of best match
        int bestIndex = -1; // index of best match
        int bestLength = -1; // length of best match
        int page = 0;
        Pages: for (
            Page currentPage = this.page0; 
            page < pages.length; 
            currentPage = pages[page++]
        ) {
            if (currentPage == null) {
                if (!set) return null;
                currentPage = pages[page - 1] = newPage(); // create a new page
                                                            // on demand
            }
            String[] sector = currentPage.get(codeSpace);
            int index = 0;
            for (String entry : sector) {
                if (entry == null) {
                    if (page == 0) continue Pages;
                    if (!set) break Pages;
                    sector[index] = value;
                    return codeSpace.newToken(
                        page, 
                        index, 
                        value.length(), 
                        true // newly created entry
                    );
                }
                if (entry.equals(value)) {
                    return codeSpace.newToken(
                        page,
                        index,
                        value.length(),
                        false // pre-existing entry
                     );
                }
                if (entry.length() > bestLength && value.startsWith(entry)) {
                    bestPage = page;
                    bestIndex = index;
                    bestLength = entry.length();
                }
                index++;
            }
        }
        return bestLength > 0 ? codeSpace.newToken(
            bestPage,
            bestIndex,
            bestLength,
            false
        ) : null;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#findStringToken(java.lang.String)
     */
    @Override
    public StringToken findStringToken(String value) {
        Integer oldIndex = this.stringSink.get(value);
        return oldIndex == null ? null : new StringToken(oldIndex.intValue(), false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getStringToken(java.lang.String)
     */
    @Override
    public StringToken getStringToken(String value) {
        StringToken token = findStringToken(value);
        if(token == null) {
            int newIndex = this.stringSinkSize;
            this.stringSinkSize += 2 * (value.length() + 1);
            return new StringToken(newIndex, true);
        } else {
            return token;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getTagToken(java.lang.String
     * )
     */
    @Override
    public CodeToken getTagToken(String namespaceURI, String value) {
        return this.getExactMatch(CodeSpace.TAG, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getAttributeNameToken(java
     * .lang.String)
     */
    @Override
    public CodeToken getAttributeNameToken(
        String namespaceURI, 
        String elementName, 
        String attributeName
    ) {
        return getExactMatch(CodeSpace.ATTRIBUTE_NAME, attributeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getAttributeValueToken(
     * java.lang.String)
     */
    @Override
    public CodeToken getAttributeValueToken(String namespaceURI, String value) {
        return getExactMatch(CodeSpace.ATTRIBUTE_VALUE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getAttributeStartToken(
     * boolean, java.lang.String, java.lang.String)
     */
    @Override
    public CodeToken findAttributeStartToken(
        boolean force,
        String namespaceURI,
        String elementName, 
        String attributeName, 
        String value
    ) {
        CodeToken token = getBestMatch(
            CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX,
            force,
            attributeName + '=' + value
        );
        return token == null ? getExactMatch(CodeSpace.ATTRIBUTE_NAME, attributeName) : token;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getAttributeValueStartToken
     * (boolean, java.lang.String)
     */
    @Override
    public CodeToken findAttributeValueToken(boolean force, String namespaceURI, String value) {
        return getBestMatch(CodeSpace.ATTRIBUTE_VALUE_PREFIX, force, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#ext0(int)
     */
    @Override
    public void ext0(
        int argument
    ) throws ServiceException {
        CodeToken pending = this.codeTarget;
        this.codeTarget = new CodeToken(argument);
        if (pending != null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An ext0(int) invocation must immediately be followed by an ext0(java.lang.String) invocation",
                new BasicException.Parameter("new-token", this.codeTarget),
                new BasicException.Parameter("pending-token", pending)
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#ext0(java.lang.String)
     */
    @Override
    public void ext0(
        String argument
    ) throws ServiceException {
        //
        // Validate the plug-in's state
        //
        CodeToken token = this.codeTarget;
        if (token == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An ext0(int) invocation must immediately be preceeded by an ext0(java.lamg.String) invocation",
                new BasicException.Parameter("token"),
                new BasicException.Parameter("value", argument)
            );
        } else {
            this.codeTarget = null;
        }
        //
        // Provide the page
        //        
        int pageIndex = token.getPage() - 1;
        Page target = pages[pageIndex];
        if (target == null) {
            target = pages[pageIndex] = newPage();
        }
        //
        // Provide the sector
        //
        CodeSpace codeSpace = CodeSpace.ofToken(token);
        String[] sector = target.get(codeSpace);
        //
        // Set the value
        //
        int codeIndex = (token.getCode() & 0x3f) - 5;
        if (sector[codeIndex] == null) {
            sector[codeIndex] = argument;
        } else if (!sector[codeIndex].equals(argument)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Token/value conflict",
                new BasicException.Parameter("token", token),
                new BasicException.Parameter("old-value", sector[codeIndex]),
                new BasicException.Parameter("new-value", argument)
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#ext1(int)
     */
    @Override
    public void ext1(int argument)
        throws ServiceException {
        int pending = this.stringTarget;
        this.stringTarget = argument;
        if (pending != -1){
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An ext0() invocation must immediately be followed by an ext1(java.lang.String) invocation",
                new BasicException.Parameter("new-token", this.stringTarget),
                new BasicException.Parameter("pending-token", pending)
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#ext1(java.lang.String)
     */
    @Override
    public void ext1(String argument)
        throws ServiceException {
        //
        // Validate the plug-in's state
        //
        int token = this.stringTarget;
        if (token < 0) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An ext1(int) invocation must immediately be preceeded by an ext1(java.lamg.String) invocation",
                new BasicException.Parameter("token"),
                new BasicException.Parameter("value", argument));
        }

        String value = this.stringSource.get(token);
        this.stringTarget = -1;
        if (value != null && !value.equals(argument)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Token/value conflict",
                new BasicException.Parameter("token", token),
                new BasicException.Parameter("old-value", value),
                new BasicException.Parameter("new-value", argument));
        }
    }

    /**
     * Retrieve the given attribute value
     * 
     * @param codeSpace
     * @param page
     * @param index
     * 
     * @return the requested value
     *
     * @throws ServiceException
     */
    private String getValue(
        CodeSpace codeSpace, 
        int page, 
        int code
    ) throws ServiceException {
        String[] values = (
            page == 0 ? this.page0 : this.pages[page - 1]
        ).get(
            codeSpace
        );
        int index = (code & 0x3f) - 5;
        String value = values[index];
        if (value == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The given token is undefined",
                new BasicException.Parameter("space", codeSpace),
                new BasicException.Parameter("page", page),
                new BasicException.Parameter("index", index)
            );
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#resolveString(int)
     */
    @Override
    public CharSequence resolveString(int index)
        throws ServiceException {
        String value = this.stringSource.get(index);
        return value == null ? super.resolveString(index) : value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#resolveAttributeValue(int,
     * int)
     */
    @Override
    public CodeResolution resolveAttributeStart(
        int page,
        int code
    ) throws ServiceException {
        String entry = getValue(CodeSpace.ofAttributeCode(code), page, code);
        int separator = entry.indexOf('=');
        if(separator < 0){
            return new CodeResolution(
                "", // namespaceURI
                "", // localName
                entry, // qName
                "" // valueStart
            );
        } else {
            return new CodeResolution(
                "", // namespaceURI
                "", // localName
                entry.substring(0, separator), // qName
                entry.substring(separator + 1) // valueStart
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#resolveTag(int, int)
     */
    @Override
    public Object resolveTag(
        int page, 
        int code
    ) throws ServiceException {
        return getValue(CodeSpace.TAG, page, code);
    }

    /**
     * Tells whether a given code is an attribute value code
     * 
     * @param code
     * 
     * @return <code>false</code> in case of an exact-match code
     */
    static boolean hasAttributeValueFlag(int code) {
        return (code & 0x80) != 0;
    }

    /**
     * Tells whether a given code is a best-match code
     * 
     * @param code
     * 
     * @return <code>false</code> in case of an exact-match code
     */
    static boolean hasStartsWithFlag(int code) {
        return (code & 0x40) != 0;
    }

    
    // ------------------------------------------------------------------------
    // Class CodeSpace
    // ------------------------------------------------------------------------

    /**
     * Code Space
     */
    protected static enum CodeSpace {

        ATTRIBUTE_NAME(0x10000),
        ATTRIBUTE_NAME_WITH_VALUE_PREFIX(0x10040),
        ATTRIBUTE_VALUE(0x10080),
        ATTRIBUTE_VALUE_PREFIX(0x100C0),
        TAG(0x00000);

        /**
         * Constructor
         * 
         * @param pattern
         */
        CodeSpace(int pattern) {
            this.pattern = pattern;
        }

        /**
         * The pattern applied to the token
         */
        final int pattern;

        /**
         * Retrieve the code space a token belongs to
         * 
         * @param token
         * 
         * @return the code space a token belongs to
         */
        static CodeSpace ofToken(CodeToken token) {
            return token.isAttributeCodeSpace() ? ofAttributeCode(token.getCode()) : TAG;
        }

        /**
         * Retrieve the code space an an attribute code belongs to
         * 
         * @param code
         * 
         * @return the code space an an attribute code belongs to
         */
        static CodeSpace ofAttributeCode(int code) {
            return hasAttributeValueFlag(code) ? (
                hasStartsWithFlag(code) ? CodeSpace.ATTRIBUTE_VALUE_PREFIX : CodeSpace.ATTRIBUTE_VALUE
            ) : (
                hasStartsWithFlag(code) ? CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX : CodeSpace.ATTRIBUTE_NAME
            );
        }

        /**
         * Get a token for the given page and index
         * 
         * @param page
         * @param index
         * @param length
         *            the value's length
         * @param created
         *            <code>true</code> if the entry has been created on the fly
         * 
         * @return the corresponding token
         */
        final CodeToken newToken(
            int page,
            int index,
            int length,
            boolean created
        ) {
            return new CodeToken(
                page << 8 | index + 5 | this.pattern,
                length,
                created
            );
        }

    }

    
    // ------------------------------------------------------------------------
    // Class Page
    // ------------------------------------------------------------------------

    /**
     * Page
     */
    protected final static class Page {

        /**
         * Slots per page
         */
        static final int SIZE = 59;

        /**
         * Clear the slots but retain the allocated memory
         */
        final void clear() {
            Arrays.fill(this.tag, null);
            Arrays.fill(this.attributeName, null);
            Arrays.fill(this.attributeNameWithValuePrefix, null);
            Arrays.fill(this.attributeValue, null);
            Arrays.fill(this.attributeValuePrefix, null);
        }

        final String[] get(CodeSpace codeSpace) {
            switch (codeSpace) {
                case TAG:
                    return this.tag;
                case ATTRIBUTE_NAME:
                    return this.attributeName;
                case ATTRIBUTE_NAME_WITH_VALUE_PREFIX:
                    return this.attributeNameWithValuePrefix;
                case ATTRIBUTE_VALUE:
                    return this.attributeValue;
                case ATTRIBUTE_VALUE_PREFIX:
                    return this.attributeValuePrefix;
                default:
                    return null;
            }
        }

        /**
         * Tags
         */
        private final String[] tag = new String[SIZE];

        /**
         * Attribute name without value
         */
        private final String[] attributeName = new String[SIZE];

        /**
         * Attribute name with value prefix
         */
        private final String[] attributeNameWithValuePrefix = new String[SIZE];

        /**
         * Complete attribute value
         */
        private final String[] attributeValue = new String[SIZE];

        /**
         * Attribute value prefix
         */
        private final String[] attributeValuePrefix = new String[SIZE];

    }

}
