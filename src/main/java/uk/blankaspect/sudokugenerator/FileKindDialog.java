/*====================================================================*\

FileKindDialog.java

Class: file-kind dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;

import javafx.scene.control.Button;

import javafx.stage.Window;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.container.RadioButtonPane;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

//----------------------------------------------------------------------


// CLASS: FILE-KIND DIALOG


class FileKindDialog
	extends SimpleModalDialog<FileKind>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The padding around the file-kind pane. */
	private static final	Insets	FILE_KIND_PANE_PADDING	= new Insets(2.0, 40.0, 2.0, 40.0);

	/** The available kinds of file. */
	private static final	List<FileKind>	FILE_KINDS	= List.of(FileKind.PUZZLE, FileKind.TEMPLATE);

	/** Miscellaneous strings. */
	private static final	String	CHOOSE_FILE_KIND_STR	= "Choose kind of file";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	FileKind	fileKind	= FileKind.PUZZLE;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FileKind	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileKindDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, CHOOSE_FILE_KIND_STR);

		RadioButtonPane<FileKind> fileKindPane = new RadioButtonPane<>(0, FILE_KINDS, fileKind.ordinal(), null);
		fileKindPane.setPadding(FILE_KIND_PANE_PADDING);

		// Add file-kind pane to content pane
		addContent(fileKindPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Update dialog state
			fileKind = FILE_KINDS.get((Integer)fileKindPane.selectedButton().getUserData());

			// Set result
			result = fileKind;

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FileKind show(
		Window	owner)
	{
		return new FileKindDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected FileKind getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
