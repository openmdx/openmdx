/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ImportXml.java,v 1.20 2008/09/09 14:32:16 hburger Exp $
 * Description: ImportXml
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:32:16 $
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
import java.util.Date;
import java.util.List;

import org.openmdx.base.application.control.Application;
import org.openmdx.base.application.control.ApplicationController;
import org.openmdx.base.application.control.CmdLineArgs;
import org.openmdx.base.application.control.CmdLineFreeArgOption;
import org.openmdx.base.application.control.CmdLineOption;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;


/**
 * Exporter provides the export functionality. 
 * <p>
 * 
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class ImportXml
  extends Application
  implements ExceptionListener {

  //-------------------------------------------------------------------------    
  public ImportXml(
  ) {
    super(
      APP_NAME,
      VERSION,
      HELP_TEXT,
      createCmdLineOptions(),
      createCmdLineFreeArgOption()
    );
  }

  //-------------------------------------------------------------------------    
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

  //-------------------------------------------------------------------------  
  protected void init(
  ) throws Exception {
      CmdLineArgs arguments = getCmdLineArgs();
    // provider
    String sourceId = arguments.getFirstValue("provider");
    this.provider = new Dataprovider_1Deployment(
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
    if (this.provider == null) {
      throw new Exception(
        "Could not establish connection to provider "
        + sourceId
      );
    }
    // validFrom
    String validFrom = null;
    if (arguments.hasArg("validFrom")) {
      validFrom = arguments.getFirstValue("validFrom");
      this.validFrom = interpretDate(validFrom);
    }

    // validTo
    String validTo = null;
    if (arguments.hasArg("validTo")) {
      validTo = arguments.getFirstValue("validTo");
      this.validTo = interpretDate(validTo);
    }

    // transactional
    this.transactional = false;
    if(arguments.hasArg("transactional")) {
      this.transactional = new Boolean(
        arguments.getFirstValue("transactional")
      ).booleanValue();
    }

    // splitUnitsOfWork
    this.splitUnitsOfWork = true;
    if(arguments.hasArg("splitUnitsOfWork")) {
      this.splitUnitsOfWork = new Boolean(
        arguments.getFirstValue("splitUnitsOfWork")
      ).booleanValue();
    }

    // splitUnitsOfWork
    this.modifiedSince = null;
    if(arguments.hasArg("modifiedSince")) {
      this.modifiedSince = DateFormat.getInstance().parse(
        arguments.getFirstValue("modifiedSince")
      );
    }
    
    // import files
    this.uris = arguments.getFreeArgs();

  }

  //-------------------------------------------------------------------------    
  /**
   * start export
   * 
   * @see org.openmdx.base.application.control.Application#run()
   */
  protected void run(
  ) throws Exception {
    XmlImporter xmlImporter =
      new XmlImporter(
        new ServiceHeader(),
        this.provider,
        this.transactional,
        this.splitUnitsOfWork
      );

    if (this.validFrom != null) {
      xmlImporter.setGlobalValidFrom(this.validFrom);
    }

    if (this.validTo != null) {
      xmlImporter.setGlobalValidTo(this.validTo);
    }
    
    if (this.modifiedSince != null) {
        xmlImporter.setGlobalModifiedSince(this.modifiedSince);
    }

    // now do the import
    xmlImporter.process(
      (String[])this.uris.toArray(new String[this.uris.size()])
    );
  }

  //-------------------------------------------------------------------------    
  protected void release(
  ) throws Exception {
    System.out.println("release");
  }

  //-------------------------------------------------------------------------    
  public static void main(
    String[] args
  ) {
    ApplicationController controller = new ApplicationController(args);
    controller.initLogging(LOG_CONFIG_NAME, LOG_SOURCE);
    controller.registerApplication(new ImportXml());
    controller.run();
  }

  private static String[] toStringArray(
      List list
  ){
      return list == null ?
          EMPTY :
          (String[])list.toArray(new String[list.size()]);
  }
  
  //-------------------------------------------------------------------------        
  /**
   * Define  the command line options.
   * 
   * @return List
   */
  private static List createCmdLineOptions(
  ) {
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

    // provider (mandatory)
    options.add(
      new CmdLineOption(
        "provider",
        "Registration Id of the provider to import to. This must correspond"
        + " to the entry in the configuration file.",
        1,
        1
      )
    );

    // validFrom (optional)
    options.add(
      new CmdLineOption(
        "validFrom",
        "Specify validFrom date which will be applied to all objects"
        + " imported. Note: all the objects must be stated."
        + EOL
        + "Format: " + VALID_DATE_FORMAT
        + " or " + VALID_DATE_FORMAT.substring(0,10),
        0,
        1
      )
    );

    // validTo (optional)
    options.add(
      new CmdLineOption(
        "validTo",
        "Specify validTo date which will be applied to all objects"
        + " imported. Note: all the objects must be stated."
        + EOL
        + "Format: " + VALID_DATE_FORMAT
        + " or " + VALID_DATE_FORMAT.substring(0,10),
        0,
        1
      )
    );

    // transactional (optional)
    options.add(
      new CmdLineOption(
        "transactional",
        "Specify whether the import is performed with transactional unit of work."
        + EOL
        + "Format: [true|*false]",
        0,
        1
      )
    );

    // splitUnitsOfWork (optional)
    options.add(
      new CmdLineOption(
        "splitUnitsOfWork",
        "Specify whether the import is performed in one or more units of work."
        + " If true unit of work demarcation is performed when processing states or at objects with path size equal to the first object with non null operation."
        + EOL
        + "Format: [*true|false]",
        0,
        1
      )
    );

    // modifiedSince (optional)
    options.add(
      new CmdLineOption(
        "modifiedSince",
        "Ignore objects modified no later than the given date"
        + EOL
        + "Format: yyyyMMdd'T'HHmmss.SSS'Z'",
        0,
        1
      )
    );
    
    return options;
  }

  //-------------------------------------------------------------------------    
  /**
   * Define the free command line option.
   * 
   */
  private static CmdLineFreeArgOption createCmdLineFreeArgOption(
  ) {
    return new CmdLineFreeArgOption(
      "XML files to import to the specified provider.",
      1
    );
  }

  //-------------------------------------------------------------------------    
  /**
   * Convert the date ISO 8601 to a Date.
   * 
   * @param dateString
   */
  private Date interpretDate(
    String _dateString
  ) throws Exception {
      String dateString = _dateString.indexOf('T') < 0 ? 
          _dateString + "T00:00:00Z" :
          _dateString;

    if(dateString.endsWith("Z")){ // SimpleDateFormat can't handle 'Z' time zone designator
      dateString = dateString.substring(0,dateString.length()-1)+"GMT+00:00";
    }

    else {
      // add GMT part
      int timePosition = dateString.indexOf('T');

      int timeZonePosition = dateString.lastIndexOf('-');
      if (timeZonePosition < timePosition) {
        timeZonePosition = dateString.lastIndexOf('+');
      }
      if(
        timeZonePosition > timePosition
        &&
        ! dateString.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
      ) {
        dateString = dateString.substring(0, timeZonePosition) + "GMT" + dateString.substring(timeZonePosition);
      }
    }


    return SECOND_FORMAT.parse(dateString);
  }

  //-------------------------------------------------------------------------
  // Variables    
  //-------------------------------------------------------------------------    
  private static final String[] EMPTY = {};

  /** The End-Of-Line separator for the platform */
  private static final String EOL = System.getProperty("line.separator");

  /** The version gets set by CVS */
  private static final String VERSION = "$Revision: 1.20 $";

  /** The application name */
  private static final String APP_NAME = "ImportXml";

  /** The logging configuration name */
  private static final String LOG_CONFIG_NAME = "ImportXml";

  /** The logging log source name */
  private static final String LOG_SOURCE = APP_NAME;

  /** application help */
  private static final String HELP_TEXT =
    "ImportXml imports the objects contained in one or more XML file(s) to a provider. ";

  private static boolean LOG_DEPLOYMENT_DETAIL = true;
  
  // XmlImporter options
  private Dataprovider_1_0 provider = null;
  private List uris = null;
  private Date validFrom = null;
  private Date validTo = null;
  private boolean transactional = false;
  private boolean splitUnitsOfWork = true;
  private Date modifiedSince = null;

  /** format to specify the dates */
  private final static String VALID_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";
  private final static DateFormat SECOND_FORMAT = DateFormat.getInstance(VALID_DATE_FORMAT);

  
}

//--- End of File -----------------------------------------------------------
