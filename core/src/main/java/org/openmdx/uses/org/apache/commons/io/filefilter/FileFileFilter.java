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
 * This filter accepts {@code File}s that are files (not directories).
 * <p>
 * For example, here is how to print out a list of the real files
 * within the current directory:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( FileFileFilter.FILE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons I/O 1.3
 * @version $Id: FileFileFilter.java 1642757 2014-12-01 21:09:30Z sebb $
 * @see FileFilterUtils#fileFileFilter()
 */
public class FileFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 5345244090827540862L;
    /** Singleton instance of file filter */
    public static final IOFileFilter FILE = new FileFileFilter();

    /**
     * Restrictive consructor.
     */
    protected FileFileFilter() {
    }

    /**
     * Checks to see if the file is a file.
     *
     * @param file  the File to check
     * @return true if the file is a file
     */
    @Override
    public boolean accept(final File file) {
        return file.isFile();
    }

}
