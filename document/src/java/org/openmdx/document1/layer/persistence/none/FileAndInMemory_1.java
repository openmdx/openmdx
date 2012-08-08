/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FileAndInMemory_1.java,v 1.9 2006/08/11 13:17:50 hburger Exp $
 * Description: FileAndInMemory_1
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 13:17:50 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.document1.layer.persistence.none;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.none.InMemory_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.document1.layer.persistence.common.PersistenceConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;


/**
 * FileAndInMemory_1
 * <p>
 * Deleted segments, cabinets and resources are made inaccessible through the
 * in-memory store but not removed from the file system by this 
 * implementation.
 * <p>
 * This plug-in is model independent except for org:openmdx:base.
 * Note: All requests for attribute position 1 are considered being 
 * large object size requests!
 */
public class FileAndInMemory_1
    extends InMemory_1
{

    /**
     * The content's location
     */
    protected File contentLocation;

    /**
     * The chunk size defines the large objects' buffer size
     */
    private int chunkSize;

    /**
     * A large object's length is passed as the feature's second value.
     */
    private static final int LARGE_OBJECT_LENGTH_INDEX = 1;

    /**
     * 
     */
    private static final Path STANDARD_OBJECT_PATTERN = new Path(
        "xri:@openmdx:**/provider/**/segment/***"
    );

    /**
     * This method is called during activate in order to set the content
     * location.
     * <p>
     * This method and its way to set the content location may be overridden 
     * by a subclass.
     * 
     * @param contentLocation
     * 
     * @throws Exception 
     */
    protected void setContentLocation(
        String contentLocation
    ) throws Exception{
        try {
            this.contentLocation = new File(contentLocation);
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        PersistenceConfigurationEntries.CONTENT_LOCATION,
                        contentLocation
                    )
                },
                "Content location configuration failure"
            );
        }
        if(! this.contentLocation.isDirectory()) new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            new BasicException.Parameter[]{
                new BasicException.Parameter(
                    PersistenceConfigurationEntries.CONTENT_LOCATION,
                    this.contentLocation.getCanonicalPath()
                )
            },
            "Content directory not found"
        );
    }

    /**
     * Retrieves the file corresponding to a given path if the openMDX object
     * has a corresponding directory in the file system.
     * <p>
     * This method and its way to define the directory paths is usually 
     * overriden by a sublcass in order to avoid having name/value pairs
     * as directory names.
     * 
     * @param path
     * 
     * @return the corresponding file or null
     */
    protected File getDirectory(
        Path path
    ){
        //
        // Check the Path
        //
        if(
           path.size() % 2 != 1 ||
           ! path.isLike(STANDARD_OBJECT_PATTERN)
        ) return null;
        //
        // Create the File
        //
        File file = this.contentLocation;
        for(
            int i = 3; // the provider's id is not taken into account
            i < path.size();
        ) file = new File(file, path.get(i++) + '=' + encode(path.get(i++)));
        //
        // Return the File
        //
        return file;
    }

    /**
     * Retrieves the file corresponding to a given path if the openMDX object
     * has a corresponding file in the file system.
     * <p>
     * This method and its way to define the file paths is usually overriden 
     * by a sub-class in order to avoid having a separate file system directory 
     * per revision.
     * 
     * @param path
     * @param directory 
     * 
     * @return
     */
    protected File getFile(
        Path path,
        String feature
    ){
        File directory = getDirectory(path);
        return directory == null || feature == null ?
            null :
            new File(directory, encode(feature));
    }

    /**
     * Encode a file name part.
     * <p>
     * When encoding a String, the following rules apply:<ul> 
     * <li>The String "." is encoded as "%2E".
     * <li>The String ".." is encoded as "%2E%2E".
     * <li>The alphanumeric characters "a" through "z", "A" through "Z" and 
     *     "0" through "9" remain the same.
     * <li>The special characters ".", "-", and "_" remain the same.
     * <li>The space character " " is converted into a plus sign "+".
     * <li>The asterisk character '*' is converted to an equal sign '='.
     * <li>All other characters are unsafe and are first converted into one or
     *     more bytes using the UTF-8 encoding scheme. Then each byte is 
     *     represented by the 3-character string "%xy", where xy is the 
     *     two-digit hexadecimal representation of the byte. 
     * </ul>
     * The string "The string ü@foo-bar" for example would get converted to 
     * "The+string+%C3%BC%40foo-bar" because in UTF-8 the character 'ü' is 
     * encoded as two bytes C3 (hex) and BC (hex), and the character '@' is 
     * encoded as one byte 40 (hex). 
     * <p>
     * This method and its encoding rules may be overridden by a subclass.
     * 
     * @parame source
     * 
     * @return the encoded value 
     */
    protected String encode(
       String source
    ){
        try {
            return ".".equals(source) ? "%2E" :
                "..".equals(source) ? "%2E%2E" :
                URLEncoder.encode(source, "UTF-8").replace('*','=');
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("encoding", "UTF-8")
                },
                "Encoding not supported"
            );
        }
    }

    /**
     * Get the file's path
     * 
     * @param file
     * 
     * @return the file's (canonical) path
     */
    protected static String getPath(
        File file
    ){
        try {
            return file.getCanonicalPath();
        } catch (IOException exception) {
            return file.getPath();
        }
    }


    //------------------------------------------------------------------------
    // Extends Layer_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        setContentLocation(
            configuration.getFirstValue(
                PersistenceConfigurationEntries.CONTENT_LOCATION
            )
        );
        this.chunkSize = configuration.containsEntry(PersistenceConfigurationEntries.CHUNNK_SIZE) ?
            ((Number)configuration.values(PersistenceConfigurationEntries.CHUNNK_SIZE)).intValue() :
            DEFAULT_CHUNK_SIZE;
    }

    /**
     * Creates a file system directory if required
     * 
     * @param objectPath
     * @throws ServiceException
     */
    private void createDirectory(
        Path objectPath
    ) throws ServiceException{
        File file = getDirectory(objectPath);
        if(file == null) return;
        if(!file.exists()) file.mkdirs();
        if(!file.isDirectory()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.CREATION_FAILURE,
            new BasicException.Parameter[]{
                new BasicException.Parameter("object", objectPath),
                new BasicException.Parameter("file", getPath(file))
            },
            "Directory creation failed"
        );
    }

    /**
     * Creates a file if required
     * 
     * @param objectPath
     * @param feature
     * @return
     * @throws ServiceException
     */
    private File createFile(
        Path objectPath,
        String feature
    ) throws ServiceException{
        File file = getFile(objectPath, feature);
        if(file == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_MEMBER_NAME,
            new BasicException.Parameter[]{
                new BasicException.Parameter("object", objectPath),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("file", getPath(file))
            },
            "Unsupported feature"
        );
        try {
            if(!file.createNewFile()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DUPLICATE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object", objectPath),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("file", getPath(file))
                },
                "File exists already"
            );
        } catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.CREATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object", objectPath),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("file", getPath(file))
                },
                "File could not be created"
            );
        }
        return file;
    }

    /**
     * Tests whether a file exists on the filesystem and returns its handle.
     * 
     * @param objectPath
     * @param feature
     * 
     * @return the requested fiel
     * 
     * @throws ServiceException
     */
    private File retrieveFile(
        Path objectPath,
        String feature
    ) throws ServiceException{
        File file = getFile(objectPath, feature);
        if(file == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_MEMBER_NAME,
            new BasicException.Parameter[]{
                new BasicException.Parameter("object", objectPath),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("file", getPath(file))
            },
            "Unsupported feature"
        );
        if(!file.exists()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_FOUND,
            new BasicException.Parameter[]{
                new BasicException.Parameter("object", objectPath),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("file", getPath(file))
            },
            "File not found"
        );
        return file;
    }

    /**
     * Set a character stream value
     * 
     * @param file the target file name
     * @param source the source stream
     * 
     * @throws ServiceException 
     */
    protected long setCharacterStream(
        File file,
        Reader source
    ) throws ServiceException{
        try {
            Writer target = new FileWriter(file);
            char[] buffer = new char[this.chunkSize];
            long objectSize = 0L;
            int chunkSize;
            while((chunkSize = source.read(buffer)) > 0) {
                objectSize += chunkSize;
                target.write(buffer, 0, chunkSize);
            }
            validate(file);
            return objectSize;
        } catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.CREATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("file", getPath(file))
                },
                "Saving character content failed"
            );
         }
    }

    /**
     * Set a binary stream value
     * 
     * @param file the target file name
     * @param source the source stream
     * 
     * @throws ServiceException 
     */
    protected long setBinaryStream(
        File file,
        InputStream source
    ) throws ServiceException{
        try {
            OutputStream target = new FileOutputStream(file);
            byte[] buffer = new byte[this.chunkSize];
            long objectSize = 0L;
            int chunkSize;
            while((chunkSize = source.read(buffer)) > 0) {
                objectSize += chunkSize;
                target.write(buffer, 0, chunkSize);
            }
            validate(file);
            return objectSize;
        } catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.CREATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("file", getPath(file))
                },
                "Saving binary content failed"
            );
         }
    }

    /**
     * Validate a file
     * 
     * @param file the file to be validated
     * 
     * @exception ServiceException if validation fails
     */
    protected void validate(
        File file
    ) throws ServiceException {
        // sub-classes may add somme validation code
    }

    /**
     * Rejects attributes with stereotype <<stream>>.
     * 
     * @param request
     * 
     * @throws ServiceException 
     */
    private void rejectStreams(
        DataproviderObject object
    ) throws ServiceException{
        for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ){
            String name = (String) i.next();
            Object value = object.getValues(name).get(0);
            if(
                value instanceof InputStream ||
                value instanceof Reader
            ) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object", object.path()),
                    new BasicException.Parameter("feature", name)
                },
                "Stream modification not supported"
            );
        }
    }

    /**
     * Processes attributes with stereotype <<stream>>.
     * 
     * @param request
     * 
     * @throws ServiceException 
     */
    private void acceptStreams(
        DataproviderObject object
    ) throws ServiceException{
        features: for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ){
            String name = (String) i.next();
            Object value = object.getValues(name).get(0);
            if(value instanceof InputStream){
                setBinaryStream(
                    createFile(object.path(), name),
                    (InputStream)value
                );
            } else if (value instanceof Reader) {
                setCharacterStream(
                    createFile(object.path(), name),
                    (Reader)value
                );
            } else continue features;
            i.remove();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
     ) throws ServiceException {
        switch(request.operation()) {
            case DataproviderOperations.OBJECT_RETRIEVAL:
                return retrieve(header, request);
            default:
                return super.get(header, request);
        }
     }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
     ) throws ServiceException {
        switch(request.operation()) {
            case DataproviderOperations.ITERATION_START:
            case DataproviderOperations.ITERATION_CONTINUATION:
                return retrieve(header, request);
            default:
                return super.find(header, request);
        }
     }

    /**
     * Handle object retrieval and iteration equests
     * 
     * @param header
     * @param request
     * 
     * @return the reply
     * 
     * @throws ServiceException
     */
    private DataproviderReply retrieve(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        //
        // Marshal
        //
        DataproviderRequest dataproviderRequest = request;
        AttributeSpecifier[] attributeSpecifiers = request.attributeSpecifier();
        List lengthSpecifiers = null;
        if(attributeSpecifiers.length > 0){
            lengthSpecifiers = new ArrayList();
            List dataproviderSpecifiers = new ArrayList();
            for(
                int i = 0;
                i < attributeSpecifiers.length;
                i++
            )(
                attributeSpecifiers[i].position() == LARGE_OBJECT_LENGTH_INDEX ? lengthSpecifiers : dataproviderSpecifiers
            ).add (
                attributeSpecifiers[i]
            );
            if(lengthSpecifiers.isEmpty()){
                lengthSpecifiers = null;
            } else {
                dataproviderRequest = new DataproviderRequest(
                    request,
                    request.object(),
                    request.operation(),
                    request.attributeFilter(),
                    request.position(),
                    request.size(),
                    request.direction(),
                    request.attributeSelector(),
                    (AttributeSpecifier[])dataproviderSpecifiers.toArray(
                        new AttributeSpecifier[dataproviderSpecifiers.size()]
                    )
                );
            }
        }
        //
        // Delegate
        //
        DataproviderReply reply = request.operation() == DataproviderOperations.OBJECT_RETRIEVAL ?
            super.get(header, dataproviderRequest) :
            super.find(header, dataproviderRequest);
        //
        // Unmarshal
        //
        DataproviderObject[] objects = reply.getObjects();
        if(lengthSpecifiers != null) for(
           int i = 0;
           i < objects.length;
           i++
        ) for(
            Iterator j = lengthSpecifiers.iterator();
            j.hasNext();
        ){
            String feature = ((AttributeSpecifier)j.next()).name();
            File file = getFile(objects[i].path(), feature);
            objects[i].values(
                feature
            ).set(
                LARGE_OBJECT_LENGTH_INDEX,
                file.exists() ? new Long(file.length()) : null
            );
        }
        return reply;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        createDirectory(request.path());
        acceptStreams(request.object());
        return super.create(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        rejectStreams(request.object());
        return super.modify(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        rejectStreams(request.object());
        return super.replace(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#set(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply set(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        rejectStreams(request.object());
        return super.set(header, request);
    }


    //------------------------------------------------------------------------
    // Extends AbstractPersistence_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractPersistence_1#getBinaryStream(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.naming.Path, java.lang.String)
     */
    protected InputStream getBinaryStream(
        ServiceHeader header,
        Path objectPath,
        String feature
    ) throws ServiceException {
        File file = retrieveFile(objectPath, feature);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object", objectPath),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("file", getPath(file))
                },
                "Binary content not found"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractPersistence_1#getCharacterStream(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.naming.Path, java.lang.String)
     */
    protected Reader getCharacterStream(
        ServiceHeader header,
        Path objectPath,
        String feature
    ) throws ServiceException {
        File file = retrieveFile(objectPath, feature);
        try {
            return new FileReader(file);
        } catch (FileNotFoundException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object", objectPath),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("file", getPath(file))
                },
                "Character content not found"
            );
        }
    }

}
