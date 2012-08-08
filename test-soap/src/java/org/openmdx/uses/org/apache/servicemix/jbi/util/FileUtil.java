/*
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
package org.openmdx.uses.org.apache.servicemix.jbi.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * File utilities
 * 
 * @version $Revision: 1.1 $
 */
public class FileUtil {
    
    /**
     * Buffer size used when copying the content of an input stream to
     * an output stream. 
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * Move a File
     * 
     * @param src
     * @param targetDirectory
     * @throws IOException 
     */
    public static void moveFile(File src, File targetDirectory) throws IOException {
        if (!src.renameTo(new File(targetDirectory, src.getName()))){
            throw new IOException("Failed to move " + src + " to " + targetDirectory);
        }
    }

    /**
     * Build a path- but do not create it
     * 
     * @param parent
     * @param subDirectory
     * @return a File representing the path
     */
    public static File getDirectoryPath(File parent, String subDirectory) {
        File result = null;
        if (parent != null){
            result = new File(parent, subDirectory);
        }
        return result;
    }

    /**
     * Build a directory path - creating directories if neccesary
     * 
     * @param file
     * @return true if the directory exists, or making it was successful
     */
    public static boolean buildDirectory(File file) {
        return file.exists() || file.mkdirs();
    }

    /**
     * Copy in stream to an out stream
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Unpack a zip file
     * 
     * @param theFile
     * @param targetDir
     * @return the file
     * @throws IOException
     */
    public static File unpackArchive(File theFile, File targetDir) throws IOException {
        if (!theFile.exists()) {
            throw new IOException(theFile.getAbsolutePath() + " does not exist");
        }
        if (!buildDirectory(targetDir)) {
            throw new IOException("Could not create directory: " + targetDir);
        }
        ZipFile zipFile;
        zipFile = new ZipFile(theFile);
        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File(targetDir, File.separator + entry.getName());
            // Take the sledgehammer approach to creating directories
            // to work around ZIP's that incorrectly miss directories
            if (!buildDirectory(file.getParentFile())) {
                throw new IOException("Could not create directory: " + file.getParentFile());
            }
            if (!entry.isDirectory()) {
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
            } else {
                if (!buildDirectory(file)) {
                    throw new IOException("Could not create directory: " + file);
                }
            }
        }
        zipFile.close();
        return theFile;
    }

    /**
     * Unpack an archive from a URL
     * 
     * @param url
     * @param targetDir
     * @return the file to the url
     * @throws IOException
     */
    public static File unpackArchive(URL url, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        InputStream in = new BufferedInputStream(url.openStream(), DEFAULT_BUFFER_SIZE);
        // make sure we get the actual file
        File zip = File.createTempFile("arc", ".zip", targetDir);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
        copyInputStream(in, out);
        out.close();
        return unpackArchive(zip, targetDir);
    }

    /**
     * Validate that an archive contains a named entry
     * 
     * @param theFile
     * @param name
     * @return true if the entry exists
     * @throws IOException
     */
    public static boolean archiveContainsEntry(File theFile, String name) throws IOException {
        boolean result = false;
        ZipFile zipFile;
        zipFile = new ZipFile(theFile);
        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().equals(name)) {
                result = true;
                break;
            }
        }
        zipFile.close();
        return result;
    }

    /**
     * Create a unique directory within a directory 'root'
     * 
     * @param rootDir
     * @param seed
     * @return unique directory
     * @throws IOException
     */
    public synchronized static File createUniqueDirectory(File rootDir, String seed) throws IOException {
        int index = seed.lastIndexOf('.');
        if (index > 0) {
            seed = seed.substring(0, index);
        }
        File result = null;
        int count = 0;
        while (result == null) {
            String name = seed + "." + count + ".tmp";
            File file = new File(rootDir, name);
            if (!file.exists()) {
                file.mkdirs();
                result = file;
            }
            count++;
        }
        return result;
    }

    /**
     * Delete a file
     * 
     * @param fileToDelete
     * @return true if the File is deleted
     */
    public static boolean deleteFile(File fileToDelete) {
        boolean result = true;
        if (fileToDelete != null && fileToDelete.exists()) {
            if (fileToDelete.isDirectory()) {
                File[] files = fileToDelete.listFiles();
                if (files == null) {
                    result = false;
                } else {
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (!file.getName().equals(".") && !file.getName().equals("..")) {
                            if (file.isDirectory()) {
                                result &= deleteFile(file);
                            } else {
                            	result &= file.delete();
                            }
                        }
                    }
                }
            }
            result &= fileToDelete.delete();
        }
        return result;
    }

    /**
     * Zip up a directory
     * 
     * @param directory
     * @param zipName
     * @throws IOException
     */
    public static void zipDir(String directory, String zipName) throws IOException {
        // create a ZipOutputStream to zip the data to
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName));
        String path = "";
        zipDir(directory, zos, path);
        // close the stream
        zos.close();
    }

    /**
     * Zip up a directory path
     * @param directory
     * @param zos
     * @param path
     * @throws IOException
     */
    public static void zipDir(String directory, ZipOutputStream zos, String path) throws IOException {
        File zipDir = new File(directory);
        // get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;
        // loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                String filePath = f.getPath();
                zipDir(filePath, zos, path + f.getName() + "/");
                continue;
            }
            FileInputStream fis = new FileInputStream(f);
            ZipEntry anEntry = new ZipEntry(path + f.getName());
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            fis.close();
        }
    }
}