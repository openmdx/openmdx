/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/file/FileSequence.java,v 1.2 2007/10/10 16:06:14 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2007/10/10 16:06:14 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * Fail-Safe sequence store implementation using the file system. Works by versioning
 * values of sequences and throwing away all versions, but the current and the previous one.
 * 
 * @version $Revision: 1.2 $
 */
public class FileSequence {

    protected final String storeDir;
    protected final LoggerFacade logger;

    /**
     * Creates a new resouce manager operation on the specified directories.
     * 
     * @param storeDir directory where sequence information is stored
     * @param logger logger used for warnings only
     */
    public FileSequence(String storeDir, LoggerFacade logger) throws ResourceManagerException {
        this.storeDir = storeDir;
        this.logger = logger;
        File file = new File(storeDir);
        file.mkdirs();
        if (!file.exists()) {
            throw new ResourceManagerException("Can not create working directory " + storeDir);
        }
    }

	/**
	 * Checks if the sequence already exists.
	 * 
	 * @param sequenceName the name of the sequence you want to check 
	 * @return <code>true</code> if the sequence already exists, <code>false</code> otherwise
	 */
    public synchronized boolean exists(String sequenceName) {
        String pathI = getPathI(sequenceName);
        String pathII = getPathII(sequenceName);

        return (FileHelper.fileExists(pathI) || FileHelper.fileExists(pathII));
    }

	/**
	 * Creates a sequence if it does not already exist.
	 * 
	 * @param sequenceName the name of the sequence you want to create 
	 * @return <code>true</code> if the sequence has been created, <code>false</code> if it already existed
	 * @throws ResourceManagerException if anything goes wrong while accessing the sequence 
	 */
    public synchronized boolean create(String sequenceName, long initialValue) throws ResourceManagerException {
        if (exists(sequenceName))
            return false;
        write(sequenceName, initialValue);
        return true;
    }

	/**
	 * Deletes a sequence if it exists.
	 * 
	 * @param sequenceName the name of the sequence you want to delete 
	 * @return <code>true</code> if the sequence has been deleted, <code>false</code> if not
	 */
    public synchronized boolean delete(String sequenceName) {
        if (!exists(sequenceName))
            return false;
        String pathI = getPathI(sequenceName);
        String pathII = getPathII(sequenceName);

        // XXX be careful no to use shortcut eval with || might not delete second file        
        boolean res1 = FileHelper.deleteFile(pathI);
        boolean res2 = FileHelper.deleteFile(pathII);

        return (res1 || res2);
    }

	/**
	 * Gets the next value of the sequence. 
	 * 
	 * @param sequenceName the name of the sequence you want the next value for
	 * @param increment the increment for the sequence, i.e. how much to add to the sequence with this call
	 * @return the next value of the sequence <em>not yet incremented</em>, i.e. the increment is recorded
	 * internally, but not returned with the next call to this method
	 * @throws ResourceManagerException if anything goes wrong while accessing the sequence 
	 */
    public synchronized long nextSequenceValueBottom(String sequenceName, long increment)
        throws ResourceManagerException {
        if (!exists(sequenceName)) {
            throw new ResourceManagerException("Sequence " + sequenceName + " does not exist");
        }
        if (increment <= 0) {
            throw new IllegalArgumentException("Increment must be greater than 0, was " + increment);
        }
        long value = read(sequenceName);
        long newValue = value + increment;
        write(sequenceName, newValue);
        return value;
    }

    protected long read(String sequenceName) throws ResourceManagerException {
        String pathI = getPathI(sequenceName);
        String pathII = getPathII(sequenceName);

        long returnValue = -1;

        long valueI = -1;
        if (FileHelper.fileExists(pathI)) {
            try {
                valueI = readFromPath(pathI);
            } catch (NumberFormatException e) {
                throw new ResourceManagerException("Fatal internal error: Backup sequence value corrupted");
            } catch (FileNotFoundException e) {
                throw new ResourceManagerException("Fatal internal error: Backup sequence vanished");
            } catch (IOException e) {
                throw new ResourceManagerException("Fatal internal error: Backup sequence value corrupted");
            }
        }

        long valueII = -1;
        if (FileHelper.fileExists(pathII)) {
            try {
                valueII = readFromPath(pathII);
                if (valueII > valueI) {
                    returnValue = valueII;
                } else {
                    // if it is smaller than previous this *must* be an error as we constantly increment
                    logger.logWarning("Latest sequence value smaller than previous, reverting to previous");
                    FileHelper.deleteFile(pathII);
                    returnValue = valueI;
                }
            } catch (NumberFormatException e) {
                logger.logWarning("Latest sequence value corrupted, reverting to previous");
                FileHelper.deleteFile(pathII);
                returnValue = valueI;
            } catch (FileNotFoundException e) {
                logger.logWarning("Can not find latest sequence value, reverting to previous");
                FileHelper.deleteFile(pathII);
                returnValue = valueI;
            } catch (IOException e) {
                logger.logWarning("Can not read latest sequence value, reverting to previous");
                FileHelper.deleteFile(pathII);
                returnValue = valueI;
            }
        } else {
            logger.logWarning("Can not read latest sequence value, reverting to previous");
            returnValue = valueI;
        }

        if (returnValue != -1) {
            return returnValue;
        } else {
            throw new ResourceManagerException("Fatal internal error: Could not compute valid sequence value");
        }
    }

    protected void write(String sequenceName, long value) throws ResourceManagerException {
        String pathII = getPathII(sequenceName);

        File f2 = new File(pathII);
        // by contract when this method is called an f2 exists it must be valid
        if (f2.exists()) {
            // move previous value to backup position
            String pathI = getPathI(sequenceName);
            File f1 = new File(pathI);
            f1.delete();
            if (!f2.renameTo(f1)) {
                throw new ResourceManagerException("Fatal internal error: Can not create backup value at" + pathI);
            }
        }
        try {
            if (!f2.createNewFile()) {
                throw new ResourceManagerException("Fatal internal error: Can not create new value at" + pathII);
            }
        } catch (IOException e) {
            throw new ResourceManagerException("Fatal internal error: Can not create new value at" + pathII, e);
        }
        writeToPath(pathII, value);
    }

    protected String getPathI(String sequenceName) {
        return storeDir + "/" + sequenceName + "_1.seq";
    }

    protected String getPathII(String sequenceName) {
        return storeDir + "/" + sequenceName + "_2.seq";
    }

    protected long readFromPath(String path)
        throws ResourceManagerException, NumberFormatException, FileNotFoundException, IOException {
        File file = new File(path);
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);

            // we do not care for encoding as we only have numbers
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String valueString = reader.readLine();
            long value = Long.parseLong(valueString);
            return value;
        } catch (UnsupportedEncodingException e) {
            throw new ResourceManagerException("Fatal internal error, encoding UTF-8 unknown");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }

            }
        }
    }

    protected void writeToPath(String path, long value) throws ResourceManagerException {
        File file = new File(path);
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String valueString = Long.toString(value);
            writer.write(valueString);
            writer.write('\n');
        } catch (FileNotFoundException e) {
            throw new ResourceManagerException("Fatal internal error: Can not find sequence at " + path);
        } catch (IOException e) {
            throw new ResourceManagerException("Fatal internal error: Can not write to sequence at " + path);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }

            }
        }
    }
}
