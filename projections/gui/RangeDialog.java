package projections.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;

/**
 *  RangeDialog
 *  written by Chee Wai Lee
 *  6/28/2002
 *
 *  This class is the base dialog class expected to be called from a
 *  subclass of ProjectionsWindow in order to present a range input
 *  dialog box to the user.
 *
 *  This dialog box is designed to maintain old information as long
 *  as it was not destroyed and garbage collected.
 *
 */
public class RangeDialog extends JDialog
    implements ActionListener, KeyListener, FocusListener
{
    ProjectionsWindow parentWindow;

    // inheritable GUI objects
    JPanel mainPanel, buttonPanel;

    JSelectField processorsField;
    JTimeTextField startTimeField;
    JTimeTextField endTimeField;

    JPanel timePanel, processorsPanel;
    JButton bOK, bUpdate, bCancel;

    // private GUI objects
    private JLabel startTextLabel, endTextLabel, totalTimeTextLabel,
	processorTextLabel;
    private JLabel totalTimeLabel, validTimeRangeLabel, validProcessorsLabel;

    // Data definitions
    long totalTime;
    long startTime;
    long endTime;

    long totalValidTime;
    String validProcessors;
    int numProcessors;

    // flags
    boolean layoutComplete = false;

    /**
     *  Constructor. Creation of the dialog object should be separate from
     *  the GUI layout. This allows for the proper inheritance from this
     *  base class.
     */
    public RangeDialog(ProjectionsWindow parentWindow, 
		       String titleString)
    {
	super((JFrame)parentWindow, titleString, true);

	this.parentWindow = parentWindow;
	startTime = 0;
	endTime = Analysis.getTotalTime();
	totalValidTime = endTime;
	validProcessors = Analysis.getValidProcessorString();
	numProcessors = Analysis.getNumProcessors();
	totalTime = endTime - startTime;
    }   

    /**
     *  INHERITANCE NOTE:
     *  The subclass should call super.actionPerformed AFTER its own 
     *  actionPerformed routine to ensure the proper behaviour of events 
     *  with respect to parent class fields.
     */
    public void actionPerformed(ActionEvent evt)
    {
	if(evt.getSource() instanceof JButton) {
	    JButton b = (JButton) evt.getSource();
	  
	    if(b == bOK) {
		// point user to an inconsistent field.
		JTextField someField = checkConsistent();
		if (someField != null) {
		    someField.selectAll();
		    someField.requestFocus();
		    return;
		} else {
		    setAllData();
		    parentWindow.dialogCancelled(false);
		}
	    } else if (b == bUpdate) {
		// update all text fields.
		updateData(processorsField);
		updateData(startTimeField);
		updateData(endTimeField);
		return;
	    }else if (b == bCancel){
		parentWindow.dialogCancelled(true);
	    }
	    setVisible(false);
	    dispose();
	} else if (evt.getSource() instanceof JTextField) {
	    // perform an update in response to an "Enter" action event
	    // since the Enter key is typically hit after some keyboard
	    // input.
	    updateData((JTextField)evt.getSource());
	    bOK.setEnabled(true);
	}     
    }   
    
    public void focusGained(FocusEvent evt) {
	// do nothing
    }

    public void focusLost(FocusEvent evt) {
	// when keyboard focus is lost from a text field, it is assumed
	// that the user has confirmed the data. Hence, perform an update and
	// enable the OK button.
	if (evt.getComponent() instanceof JTextField) {
	    updateData((JTextField)evt.getComponent());
	    bOK.setEnabled(true);
	}
    }

    public void keyPressed(KeyEvent evt) {
	// do nothing
    }

    public void keyReleased(KeyEvent evt) {
	// do nothing
    }

    /**
     *  Look for changes in input to text fields.
     *
     *  INHERITANCE NOTE:
     *  If additional behavior is desired for subclass, super.keyTyped
     *  should be call after processing the event in the subclass.
     */
    public void keyTyped(KeyEvent evt) {
	int keycode = evt.getKeyCode();
	if (evt.getComponent() instanceof JTextField) {
	    JTextField field = (JTextField)evt.getComponent();
	    
	    switch (keycode) {
	    case KeyEvent.VK_ENTER:
		// hitting enter is typical of input confirmation. Update the
		// data and enable the OK button.
		updateData(field);
		bOK.setEnabled(true);
		break;
	    case KeyEvent.VK_TAB:
		// leaving keyboard focus for that field. Update the data and
		// enable the OK button.
		updateData(field);
		bOK.setEnabled(true);
		break;
	    default:
		// any other key assumed to be input. Hence disable OK button.
		// enable the Update button to alert user to the change.
		bOK.setEnabled(false);
		break;
	    }
	}
    }

    /**
     *  displayDialog is the public interface by which the parent window
     *  may display the dialog box.
     *
     *  INHERITANCE NOTE:
     *  This method should be OVERRIDDEN by the subclass if a different
     *  layout format is desired. Otherwise, leave it alone.
     */
    public void displayDialog() {
    
	if (layoutComplete) {
	    setVisible(true);
	    return;
	}

	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    parentWindow.dialogCancelled(true);
		    setVisible(false);
		    dispose();
		}
	    });
	
	mainPanel = createMainLayout();
	buttonPanel = createButtonLayout();

	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add(mainPanel, BorderLayout.CENTER);
	this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	layoutComplete = true;
	
	pack();
	setResizable(false);
	setVisible(true);
    }

    /**
     *  createMainLayout creates the layout for basic time and processor
     *  range specification.
     *
     *  INHERITANCE NOTE:
     *  Subclasses should call super.createMainLayout to acquire the 
     *  default panels generated for the basic GUI. These panels can then
     *  be integrated into the panel being constructed by the subclass's
     *  createMainLayout method.
     *
     *  However, if the desire is to construct a brand new layout, this
     *  method should be OVERRIDDEN. The programmer would then have to
     *  use the inheritable references to the fields and his/her own 
     *  label objects to construct the layout.
     */
    JPanel createMainLayout() {

	JPanel inputPanel = new JPanel();

	// Standard Layout behavior for all subcomponents
	GridBagLayout      gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.insets = new Insets(2, 2, 2, 2);
	  
	// Default processor range layout
	processorsPanel = new JPanel();
	processorsPanel.setLayout(gbl);
	validProcessorsLabel = new JLabel("Valid Processors = " + 
					 validProcessors, JLabel.LEFT);
	processorTextLabel = new JLabel("Processors :", JLabel.LEFT);
	processorsField = new JSelectField(validProcessors, 12);
	// set listeners
	processorsField.addActionListener(this);
	// layout
	Util.gblAdd(processorsPanel, validProcessorsLabel, 
		    gbc, 0,0, 2,1, 1,1);
	Util.gblAdd(processorsPanel, processorTextLabel,
		    gbc, 0,1, 1,1, 1,1);
	Util.gblAdd(processorsPanel, processorsField,
		    gbc, 1,1, 1,1, 1,1);

	// Default time range layout
	timePanel = new JPanel();
	timePanel.setLayout(gbl);
	validTimeRangeLabel = new JLabel("Valid Time Range = " +
					U.t(startTime) + " to " +
					U.t(endTime), JLabel.LEFT);
	startTextLabel = new JLabel("Start Time :", JLabel.LEFT);
	startTimeField = new JTimeTextField(startTime, 12);
	endTextLabel = new JLabel("End Time :", JLabel.LEFT);
	endTimeField = new JTimeTextField(endTime, 12);
	totalTimeTextLabel = new JLabel("Total Time selected :", JLabel.LEFT);
	totalTimeLabel = new JLabel(U.t(totalTime), JLabel.LEFT);
	// set listeners
	startTimeField.addActionListener(this);
	endTimeField.addActionListener(this);
	startTimeField.addKeyListener(this);
	endTimeField.addKeyListener(this);
	startTimeField.addFocusListener(this);
	endTimeField.addFocusListener(this);
	// layout
	Util.gblAdd(timePanel, validTimeRangeLabel,
		    gbc, 0,0, 4,1, 1,1);
	Util.gblAdd(timePanel, startTextLabel, 
		    gbc, 0,1, 1,1, 1,1);
	Util.gblAdd(timePanel, startTimeField,
		    gbc, 1,1, 1,1, 1,1);
	Util.gblAdd(timePanel, endTextLabel,
		    gbc, 2,1, 1,1, 1,1);
	Util.gblAdd(timePanel, endTimeField,
		    gbc, 3,1, 1,1, 1,1);
	Util.gblAdd(timePanel, totalTimeTextLabel,
		    gbc, 0,2, 1,1, 1,1);
	Util.gblAdd(timePanel, totalTimeLabel,
		    gbc, 1,2, 3,1, 1,1);

	inputPanel.setLayout(gbl);
	Util.gblAdd(inputPanel, processorsPanel,
		    gbc, 0,0, 1,1, 1,1);
	Util.gblAdd(inputPanel, timePanel,
		    gbc, 0,1, 1,1, 1,1);
	
	return inputPanel;
    }

    /**
     *  createButtonLayout creates the layout for basic control buttons.
     *
     *  INHERITANCE NOTE:
     *  Subclasses should call super.createButtonLayout to acquire the 
     *  default panels generated for the basic GUI. These panels can then
     *  be integrated into the panel being constructed by the subclass's
     *  createButtonLayout method.
     *
     *  However, if the desire is to construct a brand new layout, this
     *  method should be OVERRIDDEN. The programmer would then have to
     *  use the inheritable references to the buttons and his/her own 
     *  button objects to construct the layout.
     */
    JPanel createButtonLayout() {
	JPanel buttonPanel = new JPanel();

	bOK     = new JButton("OK");
	bUpdate = new JButton("Update");
	bCancel = new JButton("Cancel");
	
        buttonPanel.add(bOK);
	buttonPanel.add(bUpdate);
	buttonPanel.add(bCancel);
	
	bOK.addActionListener    (this);
	bUpdate.addActionListener(this);
	bCancel.addActionListener(this);

	return buttonPanel;
    }

    /**
     *  updateData stores the data values from text into the variables.
     *  It also attempts to make the various data fields consistent by
     *  updating the values of other data items which are dependent on 
     *  the changes to the current data item.

     *  INHERITANCE NOTE:
     *  Inheriting subclasses should make a superclass call after doing
     *  its own field updates.
     */
    void updateData(JTextField field) {
	if (field instanceof JTimeTextField) {
	    // if the field is a time-based field
	    if (field == startTimeField) {
		startTime = startTimeField.getValue();
		totalTime = endTime - startTime;
		totalTimeLabel.setText(U.t(totalTime));
	    } else if (field == endTimeField) {
		endTime = endTimeField.getValue();
		totalTime = endTime - startTime;
		totalTimeLabel.setText(U.t(totalTime));
	    }
	} else if (field instanceof JSelectField) {
	    // if the field is a range selection field
	    if (field == processorsField) {
		// if the data is okay, keep it. Otherwise, replace it
		// with the old data.

		// do nothing for now.
	    }
	}
    }

    /**
     *  checkConsistent verifies the consistency of the various data fields
     *  and returns any one of the fields that are in violation.
     *
     *  INHERITANCE NOTE:
     *  Inheriting subclasses should make a superclass call after doing
     *  its own consistency checks.
     */
    JTextField checkConsistent() {
	// start time cannot be greater or equal to end time
	if (startTime >= endTime) {
	    return startTimeField;
	}
	// starting time cannot be less than zero
	if (startTime < 0) {
	    return startTimeField;
	}
	// ending time cannot be greater than total time
	if (endTime > totalValidTime) {
	    return endTimeField;
	}
	return null;
    }

    /**
     *  setAllData sets the data fields via API provided in the parent
     *  window. 
     *
     *  INHERITANCE NOTE:
     *  Inheriting subclasses should make a superclass call
     *  after doing its own API calls.
     */
    void setAllData() {
	parentWindow.setProcessorRange(processorsField.getValue(numProcessors));
	parentWindow.setStartTime(startTime);
	parentWindow.setEndTime(endTime);
    }
}


