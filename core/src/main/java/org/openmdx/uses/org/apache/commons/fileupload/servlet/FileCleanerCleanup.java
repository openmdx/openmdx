/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * ====================================================================
 *
 * This software has been copied from its original 
 * org.apache.commons.fileupload namespace to the 
 * org.openmdx.uses.org.apache.commons.fileupload namespace in order 
 * to be used by openMDX based applications. 
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
package org.openmdx.uses.org.apache.commons.fileupload.servlet;

#if JAVA_8
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
#else
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
#endif

import org.openmdx.uses.org.apache.commons.io.FileCleaningTracker;

/**
 * A servlet context listener, which ensures that the
 * {@link FileCleaningTracker}'s reaper thread is terminated,
 * when the web application is destroyed.
 *
 * @version $Id: FileCleanerCleanup.java 1564788 2014-02-05 14:36:41Z markt $
 */
public class FileCleanerCleanup implements ServletContextListener {

    /**
     * Attribute name, which is used for storing an instance of
     * {@link FileCleaningTracker} in the web application.
     */
    public static final String FILE_CLEANING_TRACKER_ATTRIBUTE
        = FileCleanerCleanup.class.getName() + ".FileCleaningTracker";

    /**
     * Returns the instance of {@link FileCleaningTracker}, which is
     * associated with the given {@link ServletContext}.
     *
     * @param pServletContext The servlet context to query
     * @return The contexts tracker
     */
    public static FileCleaningTracker
            getFileCleaningTracker(ServletContext pServletContext) {
        return (FileCleaningTracker)
            pServletContext.getAttribute(FILE_CLEANING_TRACKER_ATTRIBUTE);
    }

    /**
     * Sets the instance of {@link FileCleaningTracker}, which is
     * associated with the given {@link ServletContext}.
     *
     * @param pServletContext The servlet context to modify
     * @param pTracker The tracker to set
     */
    public static void setFileCleaningTracker(ServletContext pServletContext,
            FileCleaningTracker pTracker) {
        pServletContext.setAttribute(FILE_CLEANING_TRACKER_ATTRIBUTE, pTracker);
    }

    /**
     * Called when the web application is initialized. Does
     * nothing.
     *
     * @param sce The servlet context, used for calling
     *   {@link #setFileCleaningTracker(ServletContext, FileCleaningTracker)}.
     */
    public void contextInitialized(ServletContextEvent sce) {
        setFileCleaningTracker(sce.getServletContext(),
                new FileCleaningTracker());
    }

    /**
     * Called when the web application is being destroyed.
     * Calls {@link FileCleaningTracker#exitWhenFinished()}.
     *
     * @param sce The servlet context, used for calling
     *     {@link #getFileCleaningTracker(ServletContext)}.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        getFileCleaningTracker(sce.getServletContext()).exitWhenFinished();
    }

}
