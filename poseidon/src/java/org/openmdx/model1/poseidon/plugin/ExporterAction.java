/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExporterAction.java,v 1.25 2007/05/11 14:08:02 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/11 14:08:02 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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

package org.openmdx.model1.poseidon.plugin;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.omg.uml.foundation.core.ModelElement;
import org.omg.uml.foundation.core.Namespace;
import org.omg.uml.modelmanagement.Model;
import org.omg.uml.modelmanagement.UmlPackage;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.exporter.spi.Model_1Accessor;
import org.openmdx.model1.poseidon.plugin.MofRepositoryImporter.Warning;
import org.openmdx.uses.java.lang.StringBuilder;

import com.gentleware.jboogie.openapi.Project;
import com.gentleware.jboogie.openapi.ProjectAction;
import com.gentleware.poseidon.openapi.PoseidonProjectConnector;
import com.gentleware.poseidon.openapi.PoseidonResourceConnector;
import com.gentleware.poseidon.openapi.PoseidonUIConnector;
import com.gentleware.services.Services;
import com.gentleware.services.swingx.XButton;
import com.gentleware.services.swingx.XMenuItem;

public class ExporterAction
  extends ProjectAction
  implements ActionListener, ExporterListener
{

  /**
   * Implements <code>Seerializable</code> 
   */
  private static final long serialVersionUID = -8479351313564004254L;
  
  public ExporterAction(
    final Project owner
  ) {
    super(
      owner,    // project
      ExporterPlugin.RESOURCE_BUNDLE_NAME,    // resource bundle
      "menu"    // resource key
    );
  }

  //---------------------------------------------------------------------------
  public void actionPerformed(
    ActionEvent e
  ) {
    if (((Model)PoseidonProjectConnector.getModel()).getOwnedElement().isEmpty())
    {
      JOptionPane.showMessageDialog(
        PoseidonUIConnector.getApplicationWindow(),
        PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "noModelElements"),
        PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
        JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }
    
    String lastModelName = new String();
    String lastFilePath = new String();
    
    if (
      PoseidonUIConnector.getTarget().getElementCount() == 1 &&
      !(e.getSource() instanceof XButton) && 
      !(e.getSource() instanceof XMenuItem))
    {
      // this code was called by means of a popup menu entry and exactly one
      // element was selected
      if (PoseidonUIConnector.getTarget().getElement() instanceof Model)
      {
        lastModelName = String.valueOf(EXPORTER_WILDCARD);
      }
      else if (PoseidonUIConnector.getTarget().getElement() instanceof UmlPackage)
      {
        UmlPackage pkg = (UmlPackage)PoseidonUIConnector.getTarget().getElement();
        lastModelName = this.getQualifiedName(pkg, ":");
      }
      else
      {
        lastModelName = Services.getInstance().getConfiguration().getString(this.lastModelNameKey, "");
      }
      lastFilePath = Services.getInstance().getConfiguration().getString(this.lastFilePathKey, "");
    }
    else
    {
      lastModelName = Services.getInstance().getConfiguration().getString(this.lastModelNameKey, "");
      lastFilePath = Services.getInstance().getConfiguration().getString(this.lastFilePathKey, "");
    }

    if(exporterDialog == null) {
      exporterDialog = new ExporterDialog(
        PoseidonUIConnector.getApplicationWindow(),
        ExporterPlugin.RESOURCE_BUNDLE_NAME,
        this
      );
    }
    exporterDialog.showDialog(lastModelName, lastFilePath);
  }
    
  //---------------------------------------------------------------------------
  public void doExport(
    String modelName,
    String fileName
  ) {
	doExport(
		modelName,
		fileName,
		null // openmdxjdoDirectoryName
	);  
  }
  
  public void doExport(
    String modelName,
    String fileName,
    String openmdxjdoDirectoryName
  ) {
    if (modelName == null || modelName.length() == 0)
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidModelName");
      String[] msgArgs = new String[]{ modelName };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      JOptionPane.showMessageDialog(
        exporterDialog,
        msg,
        PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
        JOptionPane.ERROR_MESSAGE
      );
    }
    else if (fileName == null || fileName.length() == 0)
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidFileName");
      String[] msgArgs = new String[]{ fileName };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      JOptionPane.showMessageDialog(
        exporterDialog,
        msg,
        PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
        JOptionPane.ERROR_MESSAGE
      );
    }
    else
    {
      exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      exporterDialog.disableDialog();
      
      // for convenience: if a user uses '::' to separate package names instead of ':'
      modelName = this.replaceDoubleBySingleColons(modelName);
      
      // store chosen values for next time
      Services.getInstance().getConfiguration().putString(this.lastModelNameKey, modelName);
      Services.getInstance().getConfiguration().putString(this.lastFilePathKey, fileName);

      ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
      try
      {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try
        {
          if(modelAccessor == null) {
            modelAccessor = new Model_1Accessor("Mof", openmdxjdoDirectoryName);
          }
      
          if(importer == null) {
            importer = new MofRepositoryImporter();
          }

          SysLog.info("Importing all model elements into openMDX MOF repository");
          exporterDialog.setProgressText(PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "progressImport"));
          exporterDialog.setProgressBarValue(10);
          modelAccessor.importModel(importer);
          SysLog.info(importer.getNumberOfElements() + " elements imported into openMDX MOF repository");
          for(
            Iterator it = importer.getWarnings().iterator();
            it.hasNext();
          ) {
            this.handleImporterWarnings((Warning)it.next());
          }
          exporterDialog.setProgressBarValue(33);

          SysLog.info("Externalizing selected model package");
          exporterDialog.setProgressText(PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "progressExternalize"));
          byte[] jarOutput = modelAccessor.externalizePackageAsJar(
    		  modelName, 
    		  Collections.EMPTY_LIST // use default format set
          ); 
          exporterDialog.setProgressBarValue(66);

          SysLog.info("Saving externalized model package to selected JAR file");
          exporterDialog.setProgressText(PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "progressSave"));
          FileOutputStream fos = new FileOutputStream(fileName);
          fos.write(jarOutput);
          fos.close();
          exporterDialog.setProgressBarValue(100);

          if (importer.getWarnings().isEmpty())
          {
            exporterDialog.addNoProblem(PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "noProblemsFound"));
          }

          exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(
            exporterDialog,
            PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "exportSuccess"),
            PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
            JOptionPane.INFORMATION_MESSAGE
          );
          exporterDialog.enableDialog();
        }
        catch(ServiceException ex)
        {
          if (this.handleModelingExceptions(ex))
          {
            exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            JOptionPane.showMessageDialog(
              exporterDialog,
              PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "exportFailure"),
              PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
              JOptionPane.ERROR_MESSAGE
            );
            exporterDialog.enableDialog();
          }
          else  // do not handle exception in ExporterDialog, use generic exception dialog
          {
            throw ex;
          }
        }      

      }
      catch (ServiceException ex)
      {
        SysLog.error("exception while exporting model", ex);
        exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        JOptionPane.showMessageDialog(
          exporterDialog,
          "ServiceException " + ex,
          PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
          JOptionPane.ERROR_MESSAGE
        );
        exporterDialog.enableDialog();
      }
      catch (IOException ex)
      {
        SysLog.error("io exception while saving externalized jar file", ex);
        exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "cannotWriteFile");
        String[] msgArgs = new String[]{ fileName };
        String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  

        JOptionPane.showMessageDialog(
          exporterDialog,
          msg,
          PoseidonResourceConnector.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "dialog"),
          JOptionPane.ERROR_MESSAGE
        );
        exporterDialog.enableDialog();
      }
      finally {
        exporterDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        exporterDialog.enableDialog();
        Thread.currentThread().setContextClassLoader(savedClassLoader);
      }
    }
  }

  //---------------------------------------------------------------------------
  private void handleImporterWarnings(
    Warning warning
  ) {
    String msgWithoutArgs;
    String msg;
    switch(warning.getCode())
    {
      case MofRepositoryImporter.WARNING_INVISIBLE_ATTRIBUTE:
        msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invisibleAttribute");
        msg = MessageFormat.format(msgWithoutArgs, warning.getParameters());  
        exporterDialog.addWarningProblem(msg);
        break;
      case MofRepositoryImporter.WARNING_INVISIBLE_OPERATION:
        msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invisibleOperation");
        msg = MessageFormat.format(msgWithoutArgs, warning.getParameters());  
        exporterDialog.addWarningProblem(msg);
        break;
      case MofRepositoryImporter.WARNING_DERIVED_AND_CHANGEABLE_ATTRIBUTE:
        msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "derivedAndChangeableAttribute");
        msg = MessageFormat.format(msgWithoutArgs, warning.getParameters());  
        exporterDialog.addWarningProblem(msg);
        break;
      case MofRepositoryImporter.WARNING_INVALID_EXCEPTION_DECLARATION:
        msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidExceptionDeclaration");
        msg = MessageFormat.format(msgWithoutArgs, warning.getParameters());  
        exporterDialog.addWarningProblem(msg);
        break;
      case MofRepositoryImporter.WARNING_INVALID_PRIMITIVE_TYPE_DECLARATION:
        msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidPrimitiveTypeDeclaration");
        msg = MessageFormat.format(msgWithoutArgs, warning.getParameters());  
        exporterDialog.addWarningProblem(msg);
        break;
      default:
    }
  }

  //---------------------------------------------------------------------------
  private boolean handleModelingExceptions(
    ServiceException ex
  ) {
    if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.PACKAGE_TO_EXTERNALIZE_DOES_NOT_EXIST
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "packageNotFound");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("package")).getBase(),
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.CONSTRAINT_VIOLATION
    )
    {
      BasicException.Parameter[] parameters = ex.getExceptionStack().getParameters();
      // skip first parameter (which is exception class)
      for(int i = 0; i < parameters.length; i++)
      {
        String name = parameters[i].getName();
        if(
          !BasicException.Parameter.EXCEPTION_CLASS.equals(name) &&
          !BasicException.Parameter.EXCEPTION_SOURCE.equals(name)
        ) {
          StringBuilder sb = new StringBuilder("constraint violation: ");
          sb.append(name);
          if (parameters[i].getValue().length() != 0)
          {
            sb.append(" (");
            sb.append(parameters[i].getValue());
            sb.append(")");
          }
          exporterDialog.addErrorProblem(sb.toString());
        }
      }
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.REFERENCED_ELEMENT_TYPE_NOT_FOUND_IN_REPOSITORY
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "referencedTypeNotFound");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("element")).getBase(),
        new Path(ex.getExceptionStack().getParameter("referenced element type")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.EXCEPTION_TYPE_NOT_FOUND_IN_REPOSITORY
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "exceptionTypeNotFound");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("operation")).getBase(),
        new Path(ex.getExceptionStack().getParameter("exception")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.COMPOSITE_AGGREGATION_NOT_NAVIGABLE
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "compositeAssociationEndNotNavigable");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("end")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_OPPOSITE_AGGREGATION_FOR_COMPOSITE_AGGREGATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidOppositeAggregationForCompositeAggregation");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("end2")).getBase(),
        new Path(ex.getExceptionStack().getParameter("end1")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_MULTIPLICITY_FOR_COMPOSITE_AGGREGATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidMultiplicityForCompositeAggregation");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("end2")).getBase(),
        new Path(ex.getExceptionStack().getParameter("end1")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_MULTIPLICITY_FORMAT
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidMultiplicityFormat");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("multiplicity"),
        new Path(ex.getExceptionStack().getParameter("container")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.UNNECESSARY_QUALIFIER_FOUND
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "unnecessaryQualifierFound");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("end")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.ALIAS_TYPE_REQUIRES_EXACTLY_ONE_ATTRIBUTE
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "aliasTypeRequiresExactlyOneAttribute");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("alias type")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_ALIAS_ATTRIBUTE_NAME
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidAliasAttributeName");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("attribute name"),
        new Path(ex.getExceptionStack().getParameter("alias type")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.ASSOCIATION_NAME_IS_EMPTY
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "associationNameIsEmpty");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("class 1"),
				ex.getExceptionStack().getParameter("class 2")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.ASSOCIATION_END_NAME_IS_EMPTY
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "associationEndNameIsEmpty");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("association")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.UNEXPECTED_END_OF_QUALIFIER_DECLARATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "unexpectedEndOfQualifierDeclaration");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("qualifier text")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.MISSING_COLON_IN_QUALIFIER_DECLARATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "missingColonInQualifierDeclaration");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("qualifier text")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.MISSING_SEMICOLON_IN_QUALIFIER_DECLARATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "missingSemicolonInQualifierDeclaration");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("qualifier text")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_PARAMETER_DECLARATION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidParameterDeclaration");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("operation")).getBase(),
        Stereotypes.STRUCT
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.NO_ATTRIBUTE_TYPE_SPECIFIED
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "noAttributeTypeSpecified");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("attribute")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.INVALID_ATTRIBUTE_TYPE
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "invalidAttributeType");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("attribute")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.MODEL_ELEMENT_NOT_IN_PACKAGE
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "modelElementNotInPackage");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("element type"),
        ex.getExceptionStack().getParameter("element")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionDomain().equals(ModelExceptions.MODEL_DOMAIN) &&
      ex.getExceptionStack().getExceptionCode() == ModelExceptions.CIRCULAR_ALIAS_TYPE_DEFINITION
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "circularAliasTypeDefinition");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("element")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionCode() == BasicException.Code.DUPLICATE
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "duplicateModelElement");
      String[] msgArgs = new String[]{
        new Path(ex.getExceptionStack().getParameter("path")).getBase()
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }
    else if (
      ex.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND
    )
    {
      String msgWithoutArgs = Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "modelElementNotFound");
      String[] msgArgs = new String[]{
        ex.getExceptionStack().getParameter("element")
      };
      String msg = MessageFormat.format(msgWithoutArgs, msgArgs);  
      exporterDialog.addErrorProblem(msg);
      return true;
    }

    return false;
  }
  
  //---------------------------------------------------------------------------
  /**
   * Convert a string of the form 
   * 'org::omg::model1'
   * to a string of the form 
   * 'org:omg:model1'
   *
   */
  private String replaceDoubleBySingleColons(
    String in
  ) {
    if(in.length() == 0) { return new String(); }
    
    StringBuilder out = new StringBuilder();
    int i = 0;
    while(i < in.length()-1) {
      if("::".equals(in.substring(i,i+2))) {
        out.append(":");
        i++;
      }
      else {
        out.append(in.charAt(i));
      }
      i++;
    }
    out.append(in.charAt(i));
    return out.toString();
  }

  //---------------------------------------------------------------------------
  private String getQualifiedName(
    ModelElement modelElement,
    String namespaceSeparator
  ) {
    Namespace namespace = modelElement.getNamespace();
    if (namespace instanceof Model) { return modelElement.getName(); }

    StringBuilder sb = new StringBuilder(this.getQualifiedName(namespace, namespaceSeparator));
    sb.append(namespaceSeparator);
    sb.append(modelElement.getName() == null ? "" : modelElement.getName());
    return sb.toString();
  }

  //---------------------------------------------------------------------------
  private String lastModelNameKey = Services.getInstance().getConfiguration().makeKey("org", "openmdx", "poseidon", "lastModelName");
  private String lastFilePathKey = Services.getInstance().getConfiguration().makeKey("org", "openmdx", "poseidon", "lastFileName");
  private static ExporterDialog exporterDialog = null;
  private static Model_1Accessor modelAccessor = null;
  private static MofRepositoryImporter importer = null;
  private static final char EXPORTER_WILDCARD = '%';
}