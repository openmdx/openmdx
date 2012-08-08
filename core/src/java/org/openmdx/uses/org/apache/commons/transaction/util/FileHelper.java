/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/util/FileHelper.java,v 1.2 2007/10/10 16:06:14 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2007/10/10 16:06:14 $
 *
 * ====================================================================
 *
 * Copyright 2004 The Apache Software Foundation 
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

package org.openmdx.uses.org.apache.commons.transaction.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper methods for file manipulation. 
 * All methods are <em>thread safe</em>.
 * 
 * @version $Revision: 1.2 $
 */
public final class FileHelper {

    private static int BUF_SIZE = 50000;
    private static byte[] BUF = new byte[BUF_SIZE];

    /**
     * Deletes a file specified by a path.
     *  
     * @param path path of file to be deleted
     * @return <code>true</code> if file has been deleted, <code>false</code> otherwise
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    /**
     * Checks if a file specified by a path exits.
     *  
     * @param path path of file to be checked
     * @return <code>true</code> if file exists, <code>false</code> otherwise
     */
    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * Creates a file specified by a path. All necessary directories will be created.
     * 
     * @param path path of file to be created
     * @return <code>true</code> if file has been created, <code>false</code> if the file already exists
     * @throws  IOException
     *          If an I/O error occurred
     */
    public static boolean createFile(String path) throws IOException {
        File file = new File(path);
        if (file.isDirectory()) {
            return file.mkdirs();
        } else {
            File dir = file.getParentFile();
            // do not check if this worked, as it may also return false, when all neccessary dirs are present
            dir.mkdirs();
            return file.createNewFile();
        }
    }

    /**
     * Removes a file. If the specified file is a directory all contained files will
     * be removed recursively as well. 
     * 
     * @param toRemove file to be removed
     */
    public static void removeRec(File toRemove) {
        if (toRemove.isDirectory()) {
            File fileList[] = toRemove.listFiles();
            for (int a = 0; a < fileList.length; a++) {
                removeRec(fileList[a]);
            }
        }
        toRemove.delete();
    }

    /**
     * Moves one directory or file to another. Existing files will be replaced.
     * 
     * @param source file to move from
     * @param target file to move to
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     */
    public static void moveRec(File source, File target) throws IOException {
        byte[] sharedBuffer = new byte[BUF_SIZE];
        moveRec(source, target, sharedBuffer);
    }

    static void moveRec(File source, File target, byte[] sharedBuffer) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            if (target.isDirectory()) {

                File[] files = source.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    File targetFile = new File(target, file.getName());
                    if (file.isFile()) {
                        if (targetFile.exists()) {
                            targetFile.delete();
                        }
                        if (!file.renameTo(targetFile)) {
                            copy(file, targetFile, sharedBuffer);
                            file.delete();
                        }
                    } else {
                        targetFile.mkdirs();
                        moveRec(file, targetFile);
                    }
                }
                source.delete();
            }
        } else {
            if (!target.isDirectory()) {
                copy(source, target, sharedBuffer);
                source.delete();
            }
        }
    }

    /**
     * Copies one directory or file to another. Existing files will be replaced.
     * 
     * @param source directory or file to copy from
     * @param target directory or file to copy to
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     */
    public static void copyRec(File source, File target) throws IOException {
        byte[] sharedBuffer = new byte[BUF_SIZE];
        copyRec(source, target, sharedBuffer);
    }

    static void copyRec(File source, File target, byte[] sharedBuffer) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            if (target.isDirectory()) {

                File[] files = source.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    File targetFile = new File(target, file.getName());
                    if (file.isFile()) {
                        if (targetFile.exists()) {
                            targetFile.delete();
                        }
                        copy(file, targetFile, sharedBuffer);
                    } else {
                        targetFile.mkdirs();
                        copyRec(file, targetFile);
                    }
                }
            }
        } else {
            if (!target.isDirectory()) {
                if (!target.exists()) {
                    target.getParentFile().mkdirs();
                    target.createNewFile();
                }
                copy(source, target, sharedBuffer);
            }
        }
    }

    /**
     * Copies one file to another using {@link #copy(InputStream, OutputStream)}.
     * 
     * @param input
     *            source file
     * @param output
     *            destination file
     * @return the number of bytes copied
     * @throws IOException
     *             if an I/O error occurs (may result in partially done work)
     * @see #copy(InputStream, OutputStream)
     */
    public static long copy(File input, File output) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(input);
            return copy(in, output);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Copies one file to another using the supplied buffer.
     * 
     * @param input source file
     * @param output destination file
     * @param copyBuffer buffer used for copying
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #copy(InputStream, OutputStream)
     */
    public static long copy(File input, File output, byte[] copyBuffer) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(input);
            out = new FileOutputStream(output);
            return copy(in, out, copyBuffer);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Copies an <code>InputStream</code> to a file using {@link #copy(InputStream, OutputStream)}.
     * 
     * @param in stream to copy from 
     * @param outputFile file to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #copy(InputStream, OutputStream)
     */
    public static long copy(InputStream in, File outputFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            return copy(in, out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using a local internal buffer for performance.
     * Compared to {@link #globalBufferCopy(InputStream, OutputStream)} this method allows for better
     * concurrency, but each time it is called generates a buffer which will be garbage.
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #globalBufferCopy(InputStream, OutputStream)
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        // we need a buffer of our own, so no one else interferes
        byte[] buf = new byte[BUF_SIZE];
        return copy(in, out, buf);
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using a global internal buffer for performance.
     * Compared to {@link #copy(InputStream, OutputStream)} this method generated no garbage,
     * but decreases concurrency.
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #copy(InputStream, OutputStream)
     */
    public static long globalBufferCopy(InputStream in, OutputStream out) throws IOException {
        synchronized (BUF) {
            return copy(in, out, BUF);
        }
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using the specified buffer. 
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @param copyBuffer buffer used for copying
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #globalBufferCopy(InputStream, OutputStream)
     * @see #copy(InputStream, OutputStream)
     */
    public static long copy(InputStream in, OutputStream out, byte[] copyBuffer) throws IOException {
        long bytesCopied = 0;
        int read = -1;

        while ((read = in.read(copyBuffer, 0, copyBuffer.length)) != -1) {
            out.write(copyBuffer, 0, read);
            bytesCopied += read;
        }
        return bytesCopied;
    }
}
