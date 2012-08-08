/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExporterPlugin.java,v 1.7 2007/08/13 16:47:12 hburger Exp $
 * Description: Together Model Export (both run in same VM as Together)
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/13 16:47:12 $
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
package org.openmdx.model1.together.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.TogetherImporterErrorCodes;
import org.openmdx.model1.exporter.spi.Model_1Accessor;
import org.openmdx.model1.together.importer.TogetherImporter_1;
import org.openmdx.model1.mapping.MappingTypes;

import com.togethersoft.openapi.ide.IdeContext;
import com.togethersoft.openapi.ide.IdeScript;
import com.togethersoft.openapi.ide.message.IdeMessageType;
import com.togethersoft.openapi.ide.window.IdeButtonType;
import com.togethersoft.openapi.ide.window.IdeDialogType;
import com.togethersoft.openapi.ide.window.IdeFileChooser;
import com.togethersoft.openapi.ide.window.IdeFileChooserConstant;
import com.togethersoft.openapi.ide.window.IdeWindowManager;
import com.togethersoft.openapi.ide.window.IdeWindowManagerAccess;

//---------------------------------------------------------------------------  
// Note: because TogetherExporterPlugin has to run as a Together module, the
//       interface IdeScript must be implemented

public class ExporterPlugin
  implements IdeScript {

  //---------------------------------------------------------------------------
  private class JarFilter extends FileFilter {
      
    // Accept all directories and all jar files.
    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }
  
      String extension = getExtension(f);
      if (extension != null) {
        return (extension.equals("jar"));
      }

      return false;
    }
      
    // The description of this filter
    public String getDescription() {
      return "JAR Files (*.jar)";
    }
    
    public String getExtension(File f) {
      String ext = null;
      String s = f.getName();
      int i = s.lastIndexOf('.');
      
      if (i > 0 && i < s.length() - 1) {
        ext = s.substring(i+1).toLowerCase();
      }
      return ext;
    }
  }

  //---------------------------------------------------------------------------
  public ExporterPlugin(
  ) {
  }

  //---------------------------------------------------------------------------
  private List getExportFormats(
  ) {
		// Export formats
		List formats = new ArrayList();
		String formatsAsString = System.getProperty("org.openmdx.model1.exporter.together.formats");
		if(formatsAsString != null) {
				StringTokenizer tokenizer = new StringTokenizer(formatsAsString, ",", false);
				while(tokenizer.hasMoreTokens()) {
					 formats.add(tokenizer.nextToken());
				}
	  }
	  else {
			  formats = Arrays.asList(new String[]{
            MappingTypes.JMI_OPENMDX_1, 
            MappingTypes.XMI1, 
            MappingTypes.UML_OPENMDX_1, 
            MappingTypes.TOGETHER_OPENMDX_1
        });	  
    }
		return formats;
	}		
  	
  //---------------------------------------------------------------------------
  /**
   * Runs this module.
   *
   * @param context the IdeContext instance containing
   * the selection information at the moment the module was called.
   */
  public void run(
    IdeContext context
  ) {
  
    // openMDX requires a valid context class loader set on the
    // current thread. Because it is not done by Together it is
    // done here
    Thread.currentThread().setContextClassLoader(
      this.getClass().getClassLoader()
    );

    SysLog.trace(
      "System property org.openmdx.model1.exporter.together.exitOnFailure is" +
      ((System.getProperty("org.openmdx.model1.exporter.together.exitOnFailure") != null) ? "" : " not") +
      " set"
    );

    String modelName = System.getProperty("org.openmdx.model1.exporter.together.modelName");
    SysLog.trace("System property org.openmdx.model1.exporter.together.modelName=" + modelName);

    // the system property for modelName was specified, therefore hide dialog
    if(modelName != null) {
      this.doExport(
        modelName,
        new Boolean(System.getProperty("org.openmdx.model1.exporter.together.autoCorrection", "true")).booleanValue(), 
        new Boolean(System.getProperty("org.openmdx.model1.exporter.together.showWarnings", "true")).booleanValue(),
        this.getExportFormats(), 
        System.getProperty("org.openmdx.model1.exporter.together.openmdxjdo")
      ); 
    } 

    // no system property for modelName was specified, therefore show dialog
    else {
      JLabel label1 = new JLabel("Model package (i.e. org:omg:model1)");
      JCheckBox checkBoxAutoCorrection = new JCheckBox("Auto correction of missing properties", true);
      JCheckBox checkBoxShowWarnings = new JCheckBox("Show warnings", true);
      JTextField modelNameField = new JTextField(ExporterPlugin.lastModelName);
      
      String answer = IdeWindowManagerAccess.getWindowManager().showOptionDialog(
        null, 
        "openMDX Model Exporter for Together 6.x",
        IdeMessageType.INFORMATION, 
        new Object[]{ 
          label1, 
          modelNameField,
          checkBoxAutoCorrection, 
          checkBoxShowWarnings 
        },
        IdeButtonType.OK_CANCEL_BUTTONS, 
        null
      );

      if(IdeButtonType.OK.equals(answer)) {
        if (modelNameField.getText().length() == 0) {
          IdeWindowManagerAccess.getWindowManager().showMessageDialog(
            "Together Exporter Error",
            IdeDialogType.ERROR,
            new Object[]{ 
              new JLabel("Invalid model package name:"),
              new JLabel("You must enter a valid model package name.")
            }
          );
        } 
        else {
          modelName = modelNameField.getText();
          this.doExport(
            modelName,
            checkBoxAutoCorrection.isSelected(), 
            checkBoxShowWarnings.isSelected(),
            this.getExportFormats(), null
          );
        }
      }
    }
    if(modelName != null) ExporterPlugin.lastModelName = modelName;
  }

  //---------------------------------------------------------------------------  
  private void doExport(
    String modelToExport,
    boolean autoCorrection, 
    boolean showWarnings,
    List formats, 
    String openmdxjdo
  ) {
    try {

      if(ExporterPlugin.modelAccessor == null) {
        ExporterPlugin.modelAccessor = new Model_1Accessor(
          "Mof", 
          openmdxjdo
        );
      }
      if(ExporterPlugin.modelImporter == null) {
        ExporterPlugin.modelImporter = new TogetherImporter_1(
          autoCorrection,
          showWarnings
        );
      }

      SysLog.trace("importing model to MOF repository");
      modelAccessor.importModel(
        ExporterPlugin.modelImporter
      );
        
      SysLog.trace("externalizing model package");
      byte[] jarOutput = modelAccessor.externalizePackageAsJar(
          modelToExport,
          formats
      );

      String jarFileName = System.getProperty("org.openmdx.model1.exporter.together.jarFileName");
      SysLog.trace("System property org.openmdx.model1.exporter.together.jarFileName=" + jarFileName);

      // the system property for jarFileName was specified, therefore hide file chooser dialog
      if (jarFileName != null) {
        File file = new File(jarFileName);
        saveToFile(
          file,
          jarOutput
        );
      } 

      // no system property for jarFileName was specified, therefore show file chooser dialog
      else {  
        File file = chooseFile(IdeWindowManagerAccess.getWindowManager());
        if(file != null) { 
          saveToFile(
            file, 
            jarOutput
          ); 
        }
      }
    } 
    catch(ServiceException ex) {
      ex.log();

      // if the java system property 'org.openmdx.model1.exporter.together.exitOnFailure'
      // is set, exit immediately with exit code CommonExceptions.EXECUTION_FAILURE
      if (System.getProperty("org.openmdx.model1.exporter.together.exitOnFailure") != null) {
        System.exit(BasicException.Code.PROCESSING_FAILURE);
      }

      showErrorMessage(ex);
    } 
    catch(Exception ex) {
      ServiceException svcEx = new ServiceException(ex);
      svcEx.log();

      // if the java system property 'org.openmdx.model1.exporter.together.exitOnFailure'
      // is set, exit immediately with exit code CommonExceptions.EXECUTION_FAILURE
      if (System.getProperty("org.openmdx.model1.exporter.together.exitOnFailure") != null) {
          System.exit(BasicException.Code.PROCESSING_FAILURE);
      }
      showErrorMessage(svcEx);
    }
  }
  
  //---------------------------------------------------------------------------  
  private void saveToFile(
    File file, 
    byte[] jarOutput
  ) throws Exception {
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(jarOutput);
    fos.close();
  }
  
  //---------------------------------------------------------------------------  
  private void showErrorMessage(
    ServiceException ex
  ) {
    if(ex.getExceptionCode()== BasicException.Code.NOT_FOUND) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Invalid model package name:"),
          new JLabel("The chosen name for the model package does not exist."),
          new JLabel("You must enter a valid model package name.")
        }
      );
//  } else if (ex.getExceptionCode()== ModelExceptions.SHARED_AGGREGATION_NOT_SUPPORTED) {
//    IdeWindowManagerAccess.getWindowManager().showMessageDialog(
//      "Together Exporter Error",
//      IdeDialogType.ERROR,
//      new Object[]{ 
//        new JLabel("Illegal aggregation type:"),
//        new JLabel(
//          ex.toString("The association end '%{end}'")
//        ),
//        new JLabel("is a shared aggregation which is not allowed."),
//        new JLabel("Only composite aggregations or simple associations are allowed.")
//      }
//    );
    } else if (ex.getExceptionCode()== ModelExceptions.INVALID_MULTIPLICITY_FOR_COMPOSITE_AGGREGATION) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Illegal multiplicity:"),
          new JLabel("The association with the following association ends"),
          new JLabel(
            ex.toString("- association end '%{end1}' with aggregation type '%{end1.aggregation}' and multiplicity '%{end1.multiplicity}'")
          ),
          new JLabel(
            ex.toString("- association end '%{end2}' with aggregation type '%{end2.aggregation}' and multiplicity '%{end2.multiplicity}'")
          ),
          new JLabel("is a composite aggregation. The opposite end of a composite aggregation must have the multiplicity set to '1..1' or '0..1'.")
        }
      );
    } else if (ex.getExceptionCode()== ModelExceptions.INVALID_OPPOSITE_AGGREGATION_FOR_COMPOSITE_AGGREGATION) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Illegal aggregation type:"),
          new JLabel("The association with the following association ends"),
          new JLabel(
            ex.toString("- association end '%{end1}' has aggregation type '%{end1.aggregation}'")
          ),
          new JLabel(
            ex.toString("- association end '%{end2}' has aggregation type '%{end2.aggregation}'")
          ),
          new JLabel("is a composite aggregation. The opposite end of a composite aggregation must be of aggregation type 'none'.")
        }
      );
//  } else if (ex.getExceptionCode()== ModelExceptions.ILLEGAL_NAVIGABLE_ASSOCIATION) {
//    IdeWindowManagerAccess.getWindowManager().showMessageDialog(
//      "Together Exporter Error",
//      IdeDialogType.ERROR,
//      new Object[]{ 
//        new JLabel("Illegal navigable association:"),
//        new JLabel(
//          ex.toString("No qualifier defined for navigable association end '%{end}' with multiplicity '%{multiplicity}'.")
//        ),
//        new JLabel("Navigable associations with no qualifier must have multiplicity '0..1' or '1..1'. If a navigable association has a different multiplicity a qualifier is required."),
//        new JLabel("Either define a qualifier or set the multiplicity appropriately.")
//      }
//    );
    } else if (ex.getExceptionCode()== ModelExceptions.COMPOSITE_AGGREGATION_NOT_NAVIGABLE) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Composite aggregation is not navigable:"),
          new JLabel(
            ex.toString("The composite aggregation end '%{end}' is not navigable.")
          ),
          new JLabel("Association ends with aggregation 'composite' should be navigable."),
        }
      );
    } else if (ex.getExceptionCode()== ModelExceptions.UNNECESSARY_QUALIFIER_FOUND) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Unnecessary qualifier found:"),
          new JLabel(
            ex.toString("The association end '%{end}' is not navigable but has a qualifier specified.")
          ),
          new JLabel("Only navigable association ends need a unique identifying qualifier."),
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.UNKNOWN_CLASSIFIER) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Invalid classifier reference:"),
          new JLabel(
            ex.toString("The %{referenceType} '%{where}'")
          ),
          new JLabel(
            ex.toString("references an unknown classifier '%{unknownClassifier}'.")
          ),
          new JLabel("This classifier is neither a valid class name nor a valid primitive type.")
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.MISSING_SCOPE) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Invalid classifier reference:"),
          new JLabel(
            ex.toString("The %{referenceType} '%{where}'")
          ),
          new JLabel(
            ex.toString("references a classifier '%{name}' for which the scope is missing.")
          )
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.MISSING_ASSOCIATION_PROPERTY) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Missing association property:"),
          new JLabel(
            ex.toString("The %{missing property} of an aggregation or a composition (name=%{association name}) from client class (class=%{client class}, role=%{client role})")
          ),
          new JLabel(
            ex.toString("to supplier class (class=%{supplier class}, role=%{supplier role}) cannot be null.")
          )
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.QUALIFIER_SYNTAX_ERROR) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Syntax error in qualifier expression:"),
          new JLabel("Found syntax error in qualifier expression for aggregation or composition"),
          new JLabel(
            ex.toString("'%{where}'")
          ),
          new JLabel(
            ex.toString("%{error message}.")
          )
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.WRONG_NAMESPACE_PROPERTY) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Wrong setting for property \'namespace\':"),
          new JLabel(
            ex.toString("The property \'namespace\' of class %{class} does NOT match the package containment!")
          ),
          new JLabel("Set the property \'namespace\' of this class according to the package structure.")
        }
      );
    } else if (ex.getExceptionCode()== TogetherImporterErrorCodes.INVALID_SUPPLIER_CLASS) {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("Invalid attribute type:"),
          new JLabel(
            ex.toString("The type of attribute '%{attribute}' in class '%{class}' is invalid!")
          ),
          new JLabel("Verify the type declaration for this attribute (in the source code).")
        }
      );
    } else {
      IdeWindowManagerAccess.getWindowManager().showMessageDialog(
        "Together Exporter Error",
        IdeDialogType.ERROR,
        new Object[]{ 
          new JLabel("An unknown system exception occurred:"), 
          new JScrollPane(new JTextArea(ex.toString()))
        }
      );
    }
  }

  //---------------------------------------------------------------------------  
  private File chooseFile(
    IdeWindowManager windowManager
   ) {

    IdeFileChooser fileChooser = windowManager.createFileChooser(ExporterPlugin.lastPathName);
    fileChooser.setDialogType(IdeFileChooserConstant.SAVE_DIALOG);
    fileChooser.setDialogTitle("Save generated JAR file as");
    fileChooser.setFileFilter(new JarFilter());
    
    File selectedFile = fileChooser.showDialog(null) == IdeFileChooserConstant.APPROVE_OPTION
      ? fileChooser.getSelectedFile()
      : null;
    if(selectedFile != null) {
      ExporterPlugin.lastPathName= selectedFile.getPath();
    }
    return selectedFile;
  }

  //---------------------------------------------------------------------------  
  
  private static String lastPathName = "c:\\";
  private static String lastModelName = "";
  private static Model_1Accessor modelAccessor = null;
  private static TogetherImporter_1 modelImporter = null;
    
}

//--- End of File -----------------------------------------------------------
