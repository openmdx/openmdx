//////////////////////////////////////////////////////////////////////////////
//
// Name:        $Id: FileProcessor_1.java,v 1.1 2005/03/09 15:08:03 hburger Exp $
// Description: OMEX/Document Service
//              Persistence Layer Component
// Revision:    $Revision: 1.1 $
// Author:      $Author: hburger $
// Date:        $Date: 2005/03/09 15:08:03 $
// Copyright:   © 2002-2004 OMEX AG
//
//////////////////////////////////////////////////////////////////////////////
package org.openmdx.document1.layer.persistence.jdbc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.document1.layer.persistence.common.ProcessorConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;

/**
 * File Content Checker
 * <p>
 * This plug-in supports the org:openmdx:document1 model only unless it is
 * extended.
 */
public class FileProcessor_1 extends FileAndDatabase_1 {

    /**
     * 
     */
    protected String[] command;
    
    /**
     * 
     */
    protected File workingDirectory;
    
    /**
     * 
     */
    protected String[] environmentConfiguration;

    /**
     * The string to be put between node and revision
     */
    protected final static String NODE_REVISION_SEPARATOR = ",";
    
    
    //------------------------------------------------------------------------
    // Extends Layer_1
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.Layer_1_0#activate(short, ch.omex.spice.common.generic.Configuration, org.openmdx.compatibility.base.dataprovider.cci.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        super.activate(
            id,
            configuration,
            delegation
        );
        //
        // Get file processor configuration
        // 
        String fileProcessor = configuration.getFirstValue(
            ProcessorConfigurationEntries.FILE_PROCESSOR
        );
        if(fileProcessor == null) {
            this.command = null;
            AppLog.info(
                ProcessorConfigurationEntries.FILE_PROCESSOR, 
                null
            );
        } else {
            File executable = new File(fileProcessor);
            if(!executable.isFile()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        ProcessorConfigurationEntries.FILE_PROCESSOR, 
                        fileProcessor
                    )
                },
                "File processor not found"
            );
            this.command = new String[]{
                executable.getCanonicalPath(),
                null // first Argument to be replaced
            };
            AppLog.info(
                ProcessorConfigurationEntries.FILE_PROCESSOR, 
                this.command[0]
             );
            //
            // Get environment configuration
            // 
            List environmentConfiguration = configuration.values(
                ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION
            ).population();
            if(environmentConfiguration.isEmpty()) {
                this.environmentConfiguration = null;
                AppLog.info(
                    ProcessorConfigurationEntries.FILE_PROCESSOR + '.' +
                    ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                    null
                );
            } else {
                this.environmentConfiguration = (String[])environmentConfiguration.toArray(
                     new String[environmentConfiguration.size()]
                );
                for(
                    int i = 0;
                    i < this.environmentConfiguration.length;
                    i++
                ) if (
                    this.environmentConfiguration[i].indexOf('=') <= 0
                ) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                            environmentConfiguration
                        ),
                        new BasicException.Parameter(
                            "index", 
                            i
                        )
                    },
                    "Environment configuration entries must be of the form \"<name>=<value>\""
                ); 
                AppLog.info(
                    ProcessorConfigurationEntries.FILE_PROCESSOR + '.' +
                    ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                    environmentConfiguration
                );
            }
            //
            // Get working directory configuration
            // 
            String workingDirectory = (String)configuration.getFirstValue(
                ProcessorConfigurationEntries.WORKING_DIRECTORY
            );
            if(workingDirectory == null) {
                this.workingDirectory = null;
                AppLog.info(
                    ProcessorConfigurationEntries.FILE_PROCESSOR + '.' +
                    ProcessorConfigurationEntries.WORKING_DIRECTORY, 
                    null
                );
            } else {
                File directory = new File(workingDirectory);
                if(!directory.isDirectory()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            ProcessorConfigurationEntries.WORKING_DIRECTORY, 
                            workingDirectory
                         )
                    },
                    "File processor's working directory not found"
                );
                this.workingDirectory = directory;
                AppLog.info(
                    ProcessorConfigurationEntries.FILE_PROCESSOR + '.' +
                    ProcessorConfigurationEntries.WORKING_DIRECTORY, 
                    this.workingDirectory.getCanonicalFile()
                );
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Extends FileAndDatabase_1
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.persistence.FileAndDatabase_1#getDirectory(org.openmdx.compatibility.base.naming.Path)
     */
    protected File getDirectory(
        Path path
    ){
        //
        // Check the Path
        //
        switch(path.size()){
            case 7: // Cabinet
                if(!"cabinet".equals(path.get(5))) return null;
            case 5: // Segment
                if(!"segment".equals(path.get(3))) return null;
            // 3: a Provider has no corresponding File
                if(!"provider".equals(path.get(1))) return null;
            // 1: the Authority has no corresponding File
                if("org:openmdx:document1".equals(path.get(0))) break;                
            default:
                return null;
        }
        //
        // Create the File
        //
        File file = this.contentLocation;
        for(
            int i = 4; // the provider's id is not taken into account
            i < 10 && i < path.size();
            i+=2
        ) file = new File(file, encode(path.get(i)));
        //
        // Return the File
        //
        return file;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.persistence.FileAndDatabase_1#getFile(org.openmdx.compatibility.base.naming.Path, java.lang.String)
     */
    protected File getFile(
        Path path,
        String feature
    ){
        File directory;
        return
            path.size() == 11 &&            
            "node".equals(path.get(7)) &&
            "revision".equals(path.get(9)) &&
            "value".equals(feature) &&
            (directory = getDirectory(path.getPrefix(7))) != null  ? new File(
                directory, 
                encode(path.get(8)) + NODE_REVISION_SEPARATOR + encode(path.get(10))
            ) : null;
    }

    /* (non-Javadoc)
     * @see ch.omex.model.document1.plugin.persistence.FileContent_1#write(java.io.File, java.io.InputStream, boolean)
     */
    protected void validate(
        File file
    ) throws ServiceException {
        if(this.command != null) try {
            this.command[1] = file.getAbsolutePath();
            Process process = Runtime.getRuntime().exec(
                this.command, 
                this.environmentConfiguration, 
                this.workingDirectory
            );
            //
            //... Ignore the process' standard streams according to CR0001985 
            //
            int exitStatus = process.waitFor();
            if(exitStatus != 0){
                if(file.exists()) file.delete();
                throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            "command", 
                            this.command[0] + ' ' + this.command[1]
                        ),
                        new BasicException.Parameter(
                            ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                            this.environmentConfiguration
                        ),
                        new BasicException.Parameter(
                            ProcessorConfigurationEntries.WORKING_DIRECTORY, 
                            this.workingDirectory == null ? null : this.workingDirectory.getAbsolutePath()
                        ),
                        new BasicException.Parameter(
                            "exitStatus", 
                            exitStatus
                        )
                    },
                    "Content Invalidated by File Processor"
                );
            }
        } catch (IOException e) {
            if(file.exists()) file.delete();
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "command", 
                        this.command[0] + ' ' + this.command[1]
                    ),
                    new BasicException.Parameter(
                        ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                        this.environmentConfiguration
                    ),
                    new BasicException.Parameter(
                        ProcessorConfigurationEntries.WORKING_DIRECTORY,  
                        this.workingDirectory == null ? null : this.workingDirectory.getAbsolutePath()
                    )
                },
                "File processing failed"
            );
        } catch (InterruptedException e) {
            if(file.exists()) file.delete();
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "command", 
                        this.command[0] + ' ' + this.command[1]
                    ),
                    new BasicException.Parameter(
                        ProcessorConfigurationEntries.ENVIRONMENT_CONFIGURATION, 
                        this.environmentConfiguration
                    ),
                    new BasicException.Parameter(
                        ProcessorConfigurationEntries.WORKING_DIRECTORY,  
                        this.workingDirectory == null ? null : this.workingDirectory.getAbsolutePath()
                    )
                },
                "File processing interrupted"
            );
        }
    }

}
