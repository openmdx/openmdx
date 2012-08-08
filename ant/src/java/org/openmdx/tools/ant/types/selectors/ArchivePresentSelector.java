/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ArchivePresentSelector.java,v 1.1 2005/08/18 15:53:10 hburger Exp $
 * Description: Archive Present Selector
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/08/18 15:53:10 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.tools.ant.types.selectors;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.selectors.BaseSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;

/**
 * Selector that filters files based on whether they appear in another
 * directory tree. It can contain a mapper element, so isn't available
 * as an ExtendSelector (since those parameters can't hold other
 * elements).
 */
public class ArchivePresentSelector extends BaseSelector {

    private ZipFile target = null;
    private Mapper mapperElement = null;
    private FileNameMapper map = null;
    private boolean destmustexist = true;

    /**
     * Creates a new <code>ZipPresentSelector</code> instance.
     */
    public ArchivePresentSelector() {
    }

    /**
     * @return a string describing this object
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("{presentselector target: ");
        if (target == null) {
            buf.append("NOT YET SET");
        } else {
            buf.append(target.getName());
        }
        buf.append(" present: ");
        if (destmustexist) {
            buf.append("both");
        } else {
            buf.append("srconly");
        }
        if (map != null) {
            buf.append(map.toString());
        } else if (mapperElement != null) {
            buf.append(mapperElement.toString());
        }
        buf.append("}");
        return buf.toString();
    }

    /**
     * The name of the file or directory which is checked for matching
     * files.
     *
     * @param target the directory to scan looking for matching files.
     */
    public void setTarget(File target) {
        try {
            this.target = new ZipFile(target);
        } catch (Exception e) {
            setError("Could not open target ZIP file: " + e.getMessage());
        }
    }

    /**
     * Defines the FileNameMapper to use (nested mapper element).
     * @return a mapper to be configured
     * @throws BuildException if more that one mapper defined
     */
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper");
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }


    /**
     * This sets whether to select a file if its dest file is present.
     * It could be a <code>negate</code> boolean, but by doing things
     * this way, we get some documentation on how the system works.
     * A user looking at the documentation should clearly understand
     * that the ONLY files whose presence is being tested are those
     * that already exist in the source directory, hence the lack of
     * a <code>destonly</code> option.
     *
     * @param fp An attribute set to either <code>srconly</code or
     *           <code>both</code>.
     */
    public void setPresent(FilePresence fp) {
        if (fp.getIndex() == 0) {
            destmustexist = false;
        }
    }

    /**
     * Checks to make sure all settings are kosher. In this case, it
     * means that the target attribute has been set and we have a mapper.
     */
    public void verifySettings() {
        if (target == null) {
            setError("The target attribute is required.");
        }
        if (mapperElement == null) {
            map = new IdentityMapper();
        } else {
            map = mapperElement.getImplementation();
        }
        if (map == null) {
            setError("Could not set <mapper> element.");
        }
    }

    /**
     * The heart of the matter. This is where the selector gets to decide
     * on the inclusion of a file in a particular fileset.
     *
     * @param basedir the base directory the scan is being done from
     * @param filename is the name of the file to check
     * @param file is ignored in case of an archive
     * @return whether the file should be selected or not
     */
    public boolean isSelected(File basedir, String filename, File file) {
        // throw BuildException on error
        validate();

        // Determine file whose existence is to be checked
        String[] destfiles = map.mapFileName(filename);
        // If filename does not match the To attribute of the mapper
        // then filter it out of the files we are considering
        if (destfiles == null) return false;
        // Sanity check
        if (destfiles.length != 1 || destfiles[0] == null) throw new BuildException(
            "Invalid destination file results for " + target + " with filename " + filename
        );
        String destname = destfiles[0];
        ZipEntry destEntry = target.getEntry(destname);
        return (destEntry != null) == destmustexist;
    }

    /**
     * Enumerated attribute with the values for indicating where a file's
     * presence is allowed and required.
     */
    public static class FilePresence extends EnumeratedAttribute {
        /**
         * @return the values as an array of strings
         */
        public String[] getValues() {
            return new String[]{"srconly", "both"};
        }
    }
    
}
