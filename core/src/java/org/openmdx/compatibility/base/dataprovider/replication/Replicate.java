/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Replicate.java,v 1.16 2007/12/06 16:30:52 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/06 16:30:52 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.replication;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.openmdx.base.application.control.Application;
import org.openmdx.base.application.control.ApplicationController;
import org.openmdx.base.application.control.CmdLineArgs;
import org.openmdx.base.application.control.CmdLineOption;
import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.exporter.FeedbackHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.ProviderTraverser;
import org.openmdx.compatibility.base.dataprovider.exporter.ReplicateHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;


/**
 * @author anyff
 */
public class Replicate
    extends Application
    implements ExceptionListener
{

    public Replicate() {
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
        Deployment deployment = new InProcessDeployment(
            toStringArray(arguments.getValues("connector")),
            toStringArray(arguments.getValues("application")),
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
            System.err
        );
        //
        // source and target provider 
        //
        String sourceId = arguments.getFirstValue("source");
        _sourceProvider = new Dataprovider_1Deployment(
            deployment,
            null, // model deployment already done
            sourceId
        ).createConnection(
        );
        String targetId = arguments.getFirstValue("target");
        if (arguments.hasArg("target")){
            _targetProvider = new Dataprovider_1Deployment(
                deployment,
                null, // model deployment already done
                arguments.getFirstValue("target")
            ).createConnection(
            );
        } else {
            // use the same provider for target and source
            _targetProvider = _sourceProvider;
        }

        // 
        // model package
        //
        String modelPackageName = null;

        modelPackageName = arguments.getFirstValue("modelPackage");
       // _model = (Model_1_0) getModelPackage(modelPackageName).refMetaObject();
        _model = getModelPackage(modelPackageName);


        //
        // segment paths
        //
        String sourceSegmentString = null;
        String targetSegmentString = null;

        sourceSegmentString = arguments.getFirstValue("sourceSegment");
        targetSegmentString = arguments.getFirstValue("targetSegment");

        _sourcePath = new Path(sourceSegmentString);
        _targetPath = new Path(targetSegmentString);

        if (_sourcePath.equals(_targetPath)) {
            // equal segments require differing providers
            if (_targetProvider == _sourceProvider ||
                targetId.equals(sourceId)
            ) {
                throw new Exception(
                    "Using the same provider and the same segment for source"
                    + " and target for replication is not possible."
                    + EOL
                    + " segment: " + sourceSegmentString
                    + EOL
                    + " providerId: " + sourceId
                );
            }
        }

        //
        // syncPath       
        //
        String syncPathString = null;

        if (arguments.hasArg("syncPath")) {
            syncPathString = arguments.getFirstValue("syncPath");

            StringTokenizer tokenizer = new StringTokenizer(syncPathString, ";");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                if (token.startsWith(sourceSegmentString)) {
                    _startPoints.add(new Path(token));
                }
                else {
                    // prefix with source segment:
                    _startPoints.add(_sourcePath.getChild(token));
                }
            }
        }

        // none specified, default to whole provider
        if (_startPoints.size() == 0) {
            _startPoints.add(_sourcePath);
        }

        //
        // feedback level
        //
        if (arguments.hasArg("feedback")) {
            Short fb = Short.decode(arguments.getFirstValue("feedback"));

            _busyFeedback = fb.shortValue();
        }

    }


  /**
   * start replication
   * 
   * @see org.openmdx.base.application.control.Application#run()
   */
  protected void run()
      throws Exception
  {
      //
      // setup handler
      //
      Map pathMap = new HashMap();

      pathMap.put(_sourcePath, _targetPath);

      TraversalHandler traversalHandler = new ReplicateHandler(
          new ServiceHeader(),
          _targetProvider,
          _model,
          pathMap
      );

      //
      // setup feedback handler
      // 
      traversalHandler = new FeedbackHandler(
          traversalHandler,
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

      traverser.setTraversalHandler(traversalHandler);

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

        controller.registerApplication(new Replicate());

        controller.run();
    }

    private static String[] toStringArray(
        List list
    ){
        return list == null ?
            new String[]{} :
            (String[])list.toArray(new String[list.size()]);
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
            "Registration Id of the source provider. This must correspond to"
            + " the entry in the configuration file.",
            1,
            1));

        options.add(
            new CmdLineOption(
            "target",
            "Registration Id of the target provider. This must correspond to"
            + " the entry in the configuration file."
            + EOL
            + " This entry may be omitted if the target provider is the same"
            + " as the source provider. In this case the source provider"
            + " must provide both, target and source segment.",
            0,
            1));

        options.add(
            new CmdLineOption(
            "modelPackage",
            "Model of the data to replicate. Required is the qualified"
            + " model package name."
            + " (e.g. org.openmdx.deployment1.cci.deployment1PackageImpl)."
            + " The modelPackage itself must be part of the class path.",
            1,
            1));

        options.add(
            new CmdLineOption(
            "sourceSegment",
            "Path of the source segment. This must be"
            + " within one of the exposed paths of the source provider."
            + " (e.g. xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/source)",
            1,
            1));

        options.add(
            new CmdLineOption(
            "targetSegment",
            "Path of the target segment. This must be"
            + " within one of the exposed paths of the target provider."
            + " (e.g. xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/target)",
            1,
            1));

        options.add(
           new CmdLineOption(
            "syncPath",
            "One or more paths which specify start points for the"
            + " replication within the source. Paths which don't start"
            + " with the specified sourceSegment get prefixed with it."
            + " The replication executes in the order specified."
            + EOL
            + " Paths are separated by a semicolon."
            + EOL
            + " If omitted, the replication starts at sourceSegment. ",
            0,
            1));

        options.add(
            new CmdLineOption(
            "transactionNone",
            "Use no specific transaction behaviour. Each request uses its own"
            + " transaction."
            ));

        options.add(
            new CmdLineOption(
            "transactionPerObject",
            "The transaction scope embraces an object and its containments. An"
            + " object in this sense is the entity which starts at the segment."
            ));

        options.add(
            new CmdLineOption(
            "transactionSpan",
            "The transaction scope embraces the entire replication. "
            ));

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
     * Get the model package.
     * 
     * @param modelPackageName  qualified class name of the modelPackage
     * @return ManagedRefPackage_1_0
     */
    private Model_1_0 getModelPackage(
        String modelPackageName
    ) throws Exception {
        return new Model_1(modelPackageName);
    }


    /** The End-Of-Line separator for the platform */
    private static final String EOL = System.getProperty("line.separator");

    /** The version gets set by CVS */
    private static final String VERSION = "$Revision: 1.16 $";

    /** The application name */
    private static final String APP_NAME = "Replicate";

    /** The logging configuration name */
    private static final String LOG_CONFIG_NAME = "replicate";

    /** The logging log source name */
    private static final String LOG_SOURCE = APP_NAME;

    /** application help */
    private static final String HELP_TEXT =
        "Replicate the content of a source provider to"
        + " a target provider. The content of the target provider is brought"
        + " to reflect the content of the source provider as close as possible,"
        + " while maintaining it's own history.";

    private static boolean LOG_DEPLOYMENT_DETAIL = true;
    
    /** provider for the source */
    private Dataprovider_1_0 _sourceProvider = null;

    /** provider for the target */
    private Dataprovider_1_0 _targetProvider = null;

    /** segment of the source */
    private Path _sourcePath = null;

    /** segment of the target */
    private Path _targetPath = null;

    /** The model package for the data */
    private Model_1_0 _model = null;

    /** start points for the replication */
    private ArrayList _startPoints = new ArrayList();

    /** Level of the busy feedback */
    private short _busyFeedback = 0;
}
