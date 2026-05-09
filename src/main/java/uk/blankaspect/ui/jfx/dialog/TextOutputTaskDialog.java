/*====================================================================*\

TextOutputTaskDialog.java

Class: text-output task dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.dialog;

//----------------------------------------------------------------------


// IMPORTS


import javafx.concurrent.Task;

import javafx.geometry.HPos;
import javafx.geometry.Insets;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.common.exception2.BaseException;

import uk.blankaspect.common.misc.IProcessOutputWriter;

import uk.blankaspect.ui.jfx.button.AlternativeTextButton;
import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.font.Fonts;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

//----------------------------------------------------------------------


// CLASS: TEXT-OUTPUT TASK DIALOG


public class TextOutputTaskDialog
	extends SimpleModalDialog<Void>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The initial width of a text area. */
	private static final	double	TEXT_AREA_WIDTH		= 640.0;

	/** The initial height of a text area. */
	private static final	double	TEXT_AREA_HEIGHT	= 400.0;

	/** The padding around a button. */
	private static final	Insets	BUTTON_PADDING	= new Insets(3.0, 16.0, 3.0, 16.0);

	/** Miscellaneous strings. */
	private static final	String	COPY_STR				= "Copy";
	private static final	String	COPY_TO_CLIPBOARD_STR	= "Copy to clipboard";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean							showDialog;
	private	boolean							closed;
	private	IProcessOutputWriter			textWriter;
	private	Task<?>							task;
	private	TextArea						textArea;
	private	AlternativeTextButton<String>	closeButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public TextOutputTaskDialog(
		Window	owner,
		String	boundsKey,
		String	title)
	{
		// Call superclass constructor
		super(owner, boundsKey, title);

		// Initialise instance variables
		showDialog = true;
		textWriter = new IProcessOutputWriter()
		{
			@Override
			public boolean isOpen()
			{
				return !closed;
			}

			@Override
			public void write(
				String	str)
			{
				if (isOpen())
					SceneUtils.runOnFxApplicationThread(() -> textArea.appendText(str));
			}

			@Override
			public void close()
			{
				closed = true;
			}
		};

		// Allow dialog to be resized
		setResizable(true);

		// Create text area
		textArea = new TextArea();
		textArea.setPrefSize(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		textArea.setFont(Fonts.monoFont());

		// Set text area as content
		setContent(textArea);

		// Button: copy
		Button copyButton = Buttons.hExpansive(COPY_STR);
		copyButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		copyButton.setPadding(BUTTON_PADDING);
		copyButton.setOnAction(event ->
		{
			// Get text that is selected in text area
			String text = textArea.getSelectedText();

			// If no text is selected, use entire contents of text area
			if (text.isEmpty())
			{
				text = textArea.getText();
				if (!text.endsWith("\n"))
					 text += "\n";
			}

			// Put text on clipboard
			try
			{
				ClipboardUtils.putTextThrow(text);
			}
			catch (BaseException e)
			{
				ErrorDialog.show(this, COPY_TO_CLIPBOARD_STR, e);
			}
		});
		addButton(copyButton, HPos.LEFT);

		// Button: close
		closeButton = new AlternativeTextButton<>(CLOSE_STR, CANCEL_STR);
		closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP2);
		closeButton.setPadding(BUTTON_PADDING);
		closeButton.setOnAction(event ->
		{
			if ((task != null) && task.isRunning())
			{
				closeButton.setDisable(true);
				task.cancel();
			}
			else
				hide();
		});
		addButton(closeButton, HPos.RIGHT);

		// Cancel task when dialog is closed
		addEventHandler(WindowEvent.WINDOW_HIDING, event ->
		{
			closed = true;
			if (task != null)
				task.cancel();
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public IProcessOutputWriter writer()
	{
		return textWriter;
	}

	//------------------------------------------------------------------

	public void task(
		Task<?>	task)
	{
		// Test whether task has already been set
		if (this.task != null)
			throw new IllegalStateException("Task already set");

		// Update instance variable
		this.task = task;

		// Update text of 'close' button
		closeButton.selectItem(CANCEL_STR);

		// Handle changes of task state
		task.stateProperty().addListener((observable, oldState, state) ->
		{
			switch (state)
			{
				case CANCELLED, FAILED, SUCCEEDED:
					showDialog = false;
					SceneUtils.runOnFxApplicationThread(this::onTaskEnded);
					break;

				case SCHEDULED:
					if (showDialog)
					{
						showDialog = false;
						SceneUtils.runOnFxApplicationThread(this::show);
					}
					break;

				default:
					break;
			}
		});
	}

	//------------------------------------------------------------------

	private void onTaskEnded()
	{
		if (textArea.getLength() == 0)
			hide();
		else
		{
			closeButton.setDisable(false);
			closeButton.selectItem(CLOSE_STR);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
