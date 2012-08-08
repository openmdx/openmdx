/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ExportXml.java,v 1.19 2008/03/21 20:17:17 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 20:17:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 * distribution.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.replication;

import java.beans.ExceptionListener;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.application.control.Application;
import org.openmdx.base.application.control.ApplicationController;
import org.openmdx.base.application.control.CmdLineArgs;
import org.openmdx.base.application.control.CmdLineOption;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.exporter.FeedbackHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.ProviderTraverser;
import org.openmdx.compatibility.base.dataprovider.exporter.XMLExportHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.XmlContentHandler;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;


/**
 * Exporter provides the export functionality. 
 * <p>
 * 
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class ExportXml
    extends Application
    implements ExceptionListener
{

    public ExportXml() {
        super(APP_NAME, VERSION, HELP_TEXT, createCmdLineOptions());
    }

    /** 
     * This method is called when a recoverable exception has been caught. 
     *
     * @param eexception
     *        The exception that was caught. 
     * 
     */
    public void exceptionThrown(
           Exception exception
    ){
       exception.printStackTrace();
    }

    protected void init()
        throws Exception
    {
        CmdLineArgs arguments = getCmdLineArgs();

        // 
        // model packages
        //
        _model = getModelPackage(arguments.getValues("model"));

        //
        // source and target provider 
        //        
        _sourceProvider = new Dataprovider_1Deployment(
            new InProcessDeployment(
                toStringArray(arguments.getValues("connector")),
                toStringArray(arguments.getValues("application")),
                LOG_DEPLOYMENT_DETAIL ? System.out : null,
                System.err
            ),
            null, // model deployment already done
            arguments.getFirstValue("source")
        ).createConnection(
        );

        //
        // pathPrefix
        //
        String pathPrefix = null;

        if (arguments.hasArg("pathPrefix")) {
            pathPrefix = arguments.getFirstValue("pathPrefix");
            _pathPrefix = new Path(pathPrefix);
        }

        //
        // export paths
        //
        List exportPaths = null;

        exportPaths = arguments.getValues("path");

        for (Iterator i = exportPaths.iterator(); i.hasNext(); ) {
            String pathString = (String)i.next();

            Path exportPath = null;
            if (pathString.startsWith("spice://") || pathString.startsWith("xri:@openmdx:")) {
                exportPath = new Path(pathString);
            }
            else {
                if (_pathPrefix == null) {
                    throw new Exception(
                        "Must specify \""
                        + "pathPrefix"
                        + "\" to use relative path. "
                        + pathString
                    );
                }

                exportPath = new Path(pathString);
                exportPath = _pathPrefix.getDescendant(
                    new Path(pathString).getSuffix(0)
                );
            }

            _startPoints.add(exportPath);
        }

        //
        // export file
        //
        String exportFileName = null;

        exportFileName = arguments.getFirstValue("out");

        _exportStream = new PrintStream(
            new FileOutputStream(exportFileName),
            true
        );


        //
        // schema string
        //
        _schemaString = arguments.getFirstValue("schema");


        //
        // feedback level
        //
        if (arguments.hasArg("feedback")) {
            Short fb = Short.decode(arguments.getFirstValue("feedback"));

            _busyFeedback = fb.shortValue();
        }

    }


    /**
     * start export
     * 
     * @see org.openmdx.base.application.control.Application#run()
     */
    protected void run()
        throws Exception
    {

        XmlContentHandler ch = setupContentHandler(_exportStream);
        XMLExportHandler exportHandler =
            new XMLExportHandler(
                _model,
                "http://www.w3.org/2001/XMLSchema-instance",
                _schemaString
            );
        exportHandler.setContentHandler(ch);

        //
        // setup feedback handler
        // 
        FeedbackHandler fh = new FeedbackHandler(
            exportHandler,
            System.out,
            _busyFeedback
        );

        // setup traverser
        ProviderTraverser traverser =
            new ProviderTraverser(
                new ServiceHeader(),
                _sourceProvider,
                _model,
                _startPoints,
                null,
                null
            );

        traverser.setTraversalHandler(fh);

        // now run it:
        traverser.traverse();
    }


    protected void release()
        throws Exception
    {
        System.out.println("release");
    }


    public static void main(String[] args) {
        ApplicationController controller = new ApplicationController(args);
        controller.initLogging(LOG_CONFIG_NAME, LOG_SOURCE);

        controller.registerApplication(new ExportXml());

        controller.run();
    }



    /**
     * Define  the command line options.
     * 
     * @return List
     */
    private static List createCmdLineOptions()
    {
        ArrayList options = new ArrayList();

        options.add(
            new CmdLineOption(
                "connector",
                "Connector URL",
            0)
        );
        options.add(
            new CmdLineOption(
                "application",
                "Enterprise Application URL",
                1
            )
        );
        options.add(
            new CmdLineOption(
                "source",
                "Registration Id of the source provider. This must correspond to the entry in the configuration file.",
                1,
                1
            )
        );
        options.add(
            new CmdLineOption(
            "model",
            "Models of the data to synchronize. All models have to be specified"
            + " except \"org:w3c\" and \"org:openmdx:base\" which are present"
            + " by default. "
            + EOL
            + " The corresponding modelPackages must be part of the class path.",
            1));

        options.add(
            new CmdLineOption(
            "pathPrefix",
            "Prefix to the export Path. Use it for easier readability of"
            + " the command line options."
            + " (e.g. \"xri:@openmdx:org.openmdx.example.lab1/provider/org.some-organization\""
            + " and the paths may just contain the remaining parts"
            + " \"segment/source\")",
            0,
            1));

        options.add(
           new CmdLineOption(
            "path",
            "One or more paths which specify start points for the"
            + " export within the source. "
            + " The export executes in the order specified."
            + EOL
            + " If a pathPrefix is specified, it is applied to the relative"
            + " path specifications.",
            1));

        options.add(
            new CmdLineOption(
            "out",
            "Name of the export file.",
            1,
            1));

        options.add(
            new CmdLineOption(
            "schema",
            "Schema of the xml file generated. The schema string is inserted"
            + " to the out file as specified. The schema is not used for"
            + " generation of the file.",
            1,
            1));
        options.add(
            new CmdLineOption(
            "feedback",
            "Level of the feedback output. Numbers from 0..4",
            0,
            1
            ));

        return options;
    }

    /**
     * Setup sax ContentHandler.
     * 
     * @param target
     * @throws Exception
     */
    protected XmlContentHandler setupContentHandler(
        PrintStream target
    ) throws Exception {
        XmlContentHandler contentHandler = new XmlContentHandler(target);

        contentHandler.setAutoCollation(true);
        contentHandler.setEncoding("UTF-8");
        contentHandler.setIndentation(true);
        contentHandler.setIndentationLength(4);

      return contentHandler;

    }

    /**
     * Get the model package.
     * 
     * @param modelPackageName  qualified class name of the modelPackage
     * @return ManagedRefPackage_1_0
     */
    private Model_1_0 getModelPackage(
        List models
    ) throws Exception {

        models.add("org:w3c");
        models.add("org:openmdx:base");

        Model_1 model = new Model_1();

        model.addModels(models);
        return model;
    }

    private static String[] toStringArray(
        List list
    ){
        return list == null ?
            new String[]{} :
            (String[])list.toArray(new String[list.size()]);
    }
    

    /** The End-Of-Line separator for the platform */
    private static final String EOL = System.getProperty("line.separator");

    /** The version gets set by CVS */
    private static final String VERSION = "$Revision: 1.19 $";

    /** The application name */
    private static final String APP_NAME = "ExportXml";

    /** The logging configuration name */
    private static final String LOG_CONFIG_NAME = "ExportXml";

    /** The logging log source name */
    private static final String LOG_SOURCE = APP_NAME;

    /** application help */
    private static final String HELP_TEXT =
        "The ExportXml exports the content of a source provider to an "
        + " xml file.";

    private static boolean LOG_DEPLOYMENT_DETAIL = true;

    /** provider for the source */
    private Dataprovider_1_0 _sourceProvider = null;

    /** prefix of the path */
    private Path _pathPrefix = null;


    /** The model package for the data */
    private Model_1_0 _model = null;

    /** The file to export to */
    PrintStream _exportStream = null;

    /** Schema to put to the XML file generated */
    String _schemaString = null;

    /** start points for the export */
    private ArrayList _startPoints = new ArrayList();

    /** Level of the busy feedback */
    private short _busyFeedback = 0;
}
