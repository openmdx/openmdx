/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * ====================================================================
 *
 * This software has been copied from its original 
 * org.apache.commons.fileupload namespace to the 
 * org.openmdx.uses.org.apache.commons.fileupload namespace in order 
 * to be used by org.opnemdx.uses.org.apache.commons.fileupload.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * This filter accepts {@code File}s that are directories.
 * <p>
 * For example, here is how to print out a list of the 
 * current directory's subdirectories:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( DirectoryFileFilter.INSTANCE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons I/O 1.0
 * @version $Id: DirectoryFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 *
 * @see FileFilterUtils#directoryFileFilter()
 */
public class DirectoryFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -5148237843784525732L;
    /**
     * Singleton instance of directory filter.
     * @since Commons I/O 1.3
     */
    public static final IOFileFilter DIRECTORY = new DirectoryFileFilter();
    /**
     * Singleton instance of directory filter.
     * Please use the identical DirectoryFileFilter.DIRECTORY constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = DIRECTORY;

    /**
     * Restrictive consructor.
     */
    protected DirectoryFileFilter() {
    }

    /**
     * Checks to see if the file is a directory.
     *
     * @param file  the File to check
     * @return true if the file is a directory
     */
    @Override
    public boolean accept(final File file) {
        return file.isDirectory();
    }

}
