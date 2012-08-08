package org.openmdx.model1.poseidon.plugin;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.openmdx.kernel.log.SysLog;

import com.gentleware.poseidon.openapi.PoseidonUIConnector;
import com.gentleware.poseidon.util.SwingWorker;
import com.gentleware.services.Localizer;
import com.gentleware.services.Services;
import com.gentleware.services.swingx.SuffixFileFilter;


/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a
* for-profit company or business) then you should purchase
* a license - please visit www.cloudgarden.com for details.
*/
public class ExporterDialog
  extends javax.swing.JDialog
  implements Localizer.Listener, MouseListener
{
  /**
   * Implements <code>Seerializable</code> 
   */
  private static final long serialVersionUID = -8163057076280069662L;
  
  private JList listProblems;
  private JScrollPane scrollPaneProblems;
  private JLabel labelProblems;
  private JLabel labelProgress;
  private JSeparator jSeparator2;
  private JProgressBar progressBar;
  private JButton buttonClose;
  private JButton buttonExport;
  private JFileChooser jFileChooser2;
  private JTextField textFieldModelName;
  private JLabel labelSelectedFileName;
  private JLabel labelFileName;
  private JSeparator jSeparator1;
  private JButton buttonChooseFile;
  private JLabel labelModelName;

  public ExporterDialog(
    Frame owner,
    String resourceBundle,
    ExporterListener exporterListener
  ) {
    super(
      owner,
      false   // non modal
    );
    
    // register this dialog to get locale changed events from Poseidon
    Services.getInstance().getLocalizer().addListener(this);
    
    this.exporterListener = exporterListener;
    this.setLocationRelativeTo(owner);
    initGUI();
  }

  public void localeChanged(
  ) {
    labelModelName.setText(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "labelSelectPackageName"));
    labelFileName.setText(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "labelSelectFileName"));
    labelProblems.setText(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "labelFoundProblems"));
    buttonExport.setText(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "buttonExport"));
    buttonClose.setText(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "buttonClose"));
  }

  /**
   * Initializes the GUI.
   * Auto-generated code - any changes you make will disappear.
   */
  public void initGUI(
  ){
    try {
      preInitGUI();

      labelModelName = new JLabel();
      buttonChooseFile = new JButton();
      jSeparator1 = new JSeparator();
      labelFileName = new JLabel();
      labelSelectedFileName = new JLabel();
      textFieldModelName = new JTextField();
      jFileChooser2 = new JFileChooser();
      buttonExport = new JButton();
      buttonClose = new JButton();
      progressBar = new JProgressBar();
      jSeparator2 = new JSeparator();
      labelProgress = new JLabel();
      labelProblems = new JLabel();
      scrollPaneProblems = new JScrollPane();
      listProblems = new JList();

  		this.getContentPane().setLayout(null);
	  	this.setResizable(false);
		  this.setTitle("openMDX Exporter");
		  this.setSize(new java.awt.Dimension(546,376));

  		labelModelName.setText("Export package name:");
	  	labelModelName.setVerticalAlignment(SwingConstants.TOP);
		  labelModelName.setVerticalTextPosition(SwingConstants.CENTER);
  		labelModelName.setVisible(true);
	  	labelModelName.setPreferredSize(new java.awt.Dimension(129,16));
		  labelModelName.setBounds(new java.awt.Rectangle(24,25,129,16));
  		this.getContentPane().add(labelModelName);

  		buttonChooseFile.setToolTipText("choose export file");
	  	buttonChooseFile.setPreferredSize(new java.awt.Dimension(24,24));
		  buttonChooseFile.setBounds(new java.awt.Rectangle(490,56,24,24));
   		this.getContentPane().add(buttonChooseFile);
   		buttonChooseFile.addActionListener( new ActionListener() {
   			public void actionPerformed(ActionEvent evt) {
   				buttonChooseFileActionPerformed(evt);
   			}
   		});

   		jSeparator1.setPreferredSize(new java.awt.Dimension(490,3));
   		jSeparator1.setBounds(new java.awt.Rectangle(24,94,490,3));
   		this.getContentPane().add(jSeparator1);
    
   		labelFileName.setText("Export file name:");
   		labelFileName.setPreferredSize(new java.awt.Dimension(130,20));
   		labelFileName.setBounds(new java.awt.Rectangle(24,58,130,20));
   		this.getContentPane().add(labelFileName);
    
   		labelSelectedFileName.setPreferredSize(new java.awt.Dimension(320,20));
   		labelSelectedFileName.setBounds(new java.awt.Rectangle(160,58,320,20));
   		this.getContentPane().add(labelSelectedFileName);
    
   		textFieldModelName.setVisible(true);
   		textFieldModelName.setPreferredSize(new java.awt.Dimension(353,20));
   		textFieldModelName.setBounds(new java.awt.Rectangle(160,22,353,20));
   		this.getContentPane().add(textFieldModelName);
    
   		textFieldModelName.add(jFileChooser2);
    
   		buttonExport.setText("Export");
   		buttonExport.setPreferredSize(new java.awt.Dimension(100,24));
   		buttonExport.setBounds(new java.awt.Rectangle(305,313,100,24));
   		this.getContentPane().add(buttonExport);
   		buttonExport.addActionListener( new ActionListener() {
   			public void actionPerformed(ActionEvent evt) {
   				buttonExportActionPerformed(evt);
   			}
   		});

    	buttonClose.setText("Close");
		  buttonClose.setPreferredSize(new java.awt.Dimension(100,24));
		  buttonClose.setOpaque(false);
		  buttonClose.setSize(new java.awt.Dimension(100,24));
		  buttonClose.setBounds(new java.awt.Rectangle(415,313,100,24));
		  this.getContentPane().add(buttonClose);
		  buttonClose.addActionListener( new ActionListener() {
			  public void actionPerformed(ActionEvent evt) {
          buttonCloseActionPerformed(evt);
			  }
		  });

  		progressBar.setPreferredSize(new java.awt.Dimension(490,20));
   		progressBar.setBounds(new java.awt.Rectangle(24,127,490,20));
   		this.getContentPane().add(progressBar);
    
   		jSeparator2.setPreferredSize(new java.awt.Dimension(490,3));
   		jSeparator2.setBounds(new java.awt.Rectangle(24,306,490,3));
   		this.getContentPane().add(jSeparator2);
    
   		labelProgress.setPreferredSize(new java.awt.Dimension(493,20));
   		labelProgress.setBounds(new java.awt.Rectangle(24,101,493,20));
   		this.getContentPane().add(labelProgress);
    
   		labelProblems.setText("Found Problems:");
   		labelProblems.setPreferredSize(new java.awt.Dimension(487,20));
   		labelProblems.setBounds(new java.awt.Rectangle(24,159,487,20));
   		this.getContentPane().add(labelProblems);
    
   		scrollPaneProblems.setPreferredSize(new java.awt.Dimension(490,115));
   		scrollPaneProblems.setBounds(new java.awt.Rectangle(24,182,490,115));
   		this.getContentPane().add(scrollPaneProblems);
    
   		listProblems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   		scrollPaneProblems.add(listProblems);
   		scrollPaneProblems.setViewportView(listProblems);

      postInitGUI();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
	/** Add your pre-init code in here 	*/
	public void preInitGUI(){
	}

	/** Add your post-init code in here 	*/
	public void postInitGUI(
  ){
    getRootPane().setDefaultButton(this.buttonExport);
    this.buttonChooseFile.setIcon(chooseFileIcon);
    this.addWindowListener(this.windowClosingListener);
    getRootPane().registerKeyboardAction(this.cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    this.localeChanged();
    this.problems = new Vector();
    this.listProblems.setCellRenderer(new CustomCellRenderer());
    this.listProblems.addMouseListener(this);
  }

  public void showDialog(
    String initialModelName,
    String initialFileName
  ) {
    // set model name and file name to initial values
    this.textFieldModelName.setText(initialModelName);
    this.labelSelectedFileName.setText(initialFileName);
    
    this.resetDialog();

    this.show();
	}

  public void setProgressText(
    String text
  ) {
    this.labelProgress.setText(text);
  }
  
  public void setProgressBarValue(
    int value
  ) {
    this.progressBar.setValue(value);
  }
  
  public void addNoProblem(
    String problemText
  ) {
    this.addProblem(okIcon, problemText);
  }
  
  public void addErrorProblem(
    String problemText
  ) {
    this.addProblem(errorIcon, problemText);
  }
  
  public void addWarningProblem(
    String problemText
  ) {
    this.addProblem(warningIcon, problemText);
  }
  
  private void addProblem(
    Icon problemIcon,
    String problemText
  ) {
   JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
   panel.add(new JLabel(problemIcon));
   panel.add(new JLabel(problemText));

   this.problems.add(panel);
   this.listProblems.setListData(problems);
  }

	/** Auto-generated event handler method */
	protected void buttonChooseFileActionPerformed(
    ActionEvent evt
  ){
    JFileChooser fileChooser = new JFileChooser();    
    fileChooser.setDialogTitle(Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "selectJarFile"));            
    fileChooser.resetChoosableFileFilters();
    fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
    fileChooser.addChoosableFileFilter(
      new SuffixFileFilter(
        "jar",
        Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "jarFiles")
      )
    );
      
    if (this.labelSelectedFileName.getText() != null)
    {
      fileChooser.setSelectedFile(
        new File(this.labelSelectedFileName.getText())
      );
    }

    if (
       fileChooser.showDialog(this, Services.localize(ExporterPlugin.RESOURCE_BUNDLE_NAME, "buttonChoose")) == JFileChooser.APPROVE_OPTION
    ) {
      this.labelSelectedFileName.setText(
        fileChooser.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".jar") ?
          fileChooser.getSelectedFile().getAbsolutePath() :
          fileChooser.getSelectedFile().getAbsolutePath() + ".jar"
      );
    }
	}

	/** Auto-generated event handler method */
	protected void buttonExportActionPerformed(
    ActionEvent evt
  ){

    final SwingWorker worker = new SwingWorker(this.getTitle()) {
        public Object construct() {
          resetDialog();
          String openmdxjdoDirectoryName = null; // TODO
          exporterListener.doExport(
            textFieldModelName.getText(),
            labelSelectedFileName.getText(),
            openmdxjdoDirectoryName
          );
          return null;
        }
    };
    worker.start();
	}

  public void enableDialog(
  ) {
    this.textFieldModelName.setEnabled(true);
    this.buttonExport.setEnabled(true);
    this.buttonClose.setEnabled(true);
    this.buttonChooseFile.setEnabled(true);
  }

  public void disableDialog(
  ) {
    this.textFieldModelName.setEnabled(false);
    this.buttonExport.setEnabled(false);
    this.buttonClose.setEnabled(false);
    this.buttonChooseFile.setEnabled(false);
  }

  private void resetDialog(
  ) {
    // reset text in progress label
    this.labelProgress.setText("");
    
    // clear found problems list
    this.problems.clear();
    this.listProblems.setListData(problems);
    
    // reset progress bar
    this.progressBar.setValue(0);
    
  }

  /**
   * Returns an ImageIcon, or null if the path was invalid.
   */
  private static ImageIcon createImageIcon(
    String path, 
    String description
  ) {
    java.net.URL imgURL = ExporterDialog.class.getClassLoader().getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    } 
    SysLog.warning("could not find image icon " + path);
    return null;
  }
  
  /** Auto-generated event handler method */
	protected void buttonCloseActionPerformed(ActionEvent evt){
    this.hide();
    this.dispose();  
	}

  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}

  public void mouseClicked(
    MouseEvent event
  ) {
    if(event.getClickCount() == 2)
    {
      int index = this.listProblems.locationToIndex(event.getPoint());
      Object item = this.listProblems.getModel().getElementAt(index);
      this.listProblems.ensureIndexIsVisible(index);
      Services.logError("Double clicked on " + item);
      PoseidonUIConnector.showUrl("http://www.openmdx.org");
    }
  }

  class CustomCellRenderer
    implements ListCellRenderer
  {
    public Component getListCellRendererComponent(
      JList list, 
      Object value, 
      int index,
      boolean isSelected,
      boolean cellHasFocus
    ) {
      Component component = (Component)value;
      component.setBackground(isSelected ? Color.yellow : Color.white);
      return component;
    }
  }

  private Vector problems;
  private static final ImageIcon chooseFileIcon = createImageIcon("org/openmdx/model1/poseidon/plugin/choose_file.gif", "choose file");
  private static final ImageIcon errorIcon = createImageIcon("org/openmdx/model1/poseidon/plugin/error.gif", "error");
  private static final ImageIcon warningIcon = createImageIcon("org/openmdx/model1/poseidon/plugin/warning.gif", "warning");
  private static final ImageIcon okIcon = createImageIcon("org/openmdx/model1/poseidon/plugin/ok.gif", "ok");
  
  private ExporterListener exporterListener = null;

  private WindowListener windowClosingListener = new WindowAdapter() {
    public void windowClosing(final WindowEvent e) {
      dispose();
    }
  };

  private ActionListener cancelListener = new ActionListener() {
    public void actionPerformed(final ActionEvent e) {
      dispose();
    }
  };

}
