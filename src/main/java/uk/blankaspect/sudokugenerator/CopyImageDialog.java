/*====================================================================*\

CopyImageDialog.java

Class: 'copy image' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.text.DecimalFormat;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

//----------------------------------------------------------------------


// CLASS: 'COPY IMAGE' DIALOG


class CopyImageDialog
	extends SimpleModalDialog<CopyImageDialog.Result>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		MIN_IMAGE_SIZE		= 40;
	private static final	int		MAX_IMAGE_SIZE		= 2000;
	private static final	int		DEFAULT_IMAGE_SIZE	= 360;

	private static final	DecimalFormat	SCALE_FORMATTER	= new DecimalFormat("0.###");

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 10.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(6.0);

	/** Miscellaneous strings. */
	private static final	String	COPY_IMAGE_STR			= "Copy image to clipboard";
	private static final	String	IMAGE_SIZE_STR			= "Image size";
	private static final	String	APPLY_DISPLAY_SCALE_STR	= "Apply display scale";
	private static final	String	PIXELS_STR				= "pixels";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	APPLY_DISPLAY_SCALE	= "applyDisplayScale";
		String	IMAGE_SIZE			= "imageSize";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int		imageSize			= DEFAULT_IMAGE_SIZE;
	private static	boolean	applyDisplayScale;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Result	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CopyImageDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, COPY_IMAGE_STR);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_H_GAP);
		controlPane.setVgap(CONTROL_V_GAP);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Spinner: image size
		Spinner<Integer> imageSizeSpinner =
				SpinnerFactory.integerSpinner(MIN_IMAGE_SIZE, MAX_IMAGE_SIZE, imageSize,
											  NumberUtils.getNumDecDigitsInt(MAX_IMAGE_SIZE));

		// Pane: image size
		HBox imageSizePane = new HBox(CONTROL_H_GAP, imageSizeSpinner, Labels.hNoShrink(PIXELS_STR));
		imageSizePane.setAlignment(Pos.CENTER_LEFT);
		controlPane.addRow(row++, new Label(IMAGE_SIZE_STR), imageSizePane);

		// Check box: apply display scale
		double scaleX = owner.getOutputScaleX();
		double scaleY = owner.getOutputScaleY();
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(APPLY_DISPLAY_SCALE_STR).append(" (\u00D7").append(SCALE_FORMATTER.format(scaleX));
		if (scaleY != scaleX)
			buffer.append(", \u00D7").append(SCALE_FORMATTER.format(scaleY));
		buffer.append(')');
		CheckBox applyDisplayScaleCheckBox = new CheckBox(buffer.toString());
		if ((scaleX == 1.0) && (scaleY == 1.0))
			applyDisplayScaleCheckBox.setDisable(true);
		else
			applyDisplayScaleCheckBox.setSelected(applyDisplayScale);
		controlPane.add(applyDisplayScaleCheckBox, 1, row++);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Update dialog state
			imageSize = imageSizeSpinner.getValue();
			applyDisplayScale = applyDisplayScaleCheckBox.isSelected();

			// Set result
			result = new Result(imageSize, applyDisplayScale);

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

		// Fire 'request to close window' event if Escape is pressed
		setRequestCloseOnEscape();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Result show(
		Window	owner)
	{
		return new CopyImageDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

	public static MapNode encodeState()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode image size
		rootNode.addInt(PropertyKey.IMAGE_SIZE, imageSize);

		// Encode 'apply display scale' flag
		rootNode.addBoolean(PropertyKey.APPLY_DISPLAY_SCALE, applyDisplayScale);

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		// Decode image size
		String key = PropertyKey.IMAGE_SIZE;
		if (rootNode.hasInt(key))
			imageSize = rootNode.getInt(key);

		// Decode 'apply display scale' flag
		key = PropertyKey.APPLY_DISPLAY_SCALE;
		if (rootNode.hasBoolean(key))
			applyDisplayScale = rootNode.getBoolean(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Result getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: RESULT OF 'COPY IMAGE' DIALOG


	record Result(
		int		imageSize,
		boolean	applyDisplayScale)
	{ }

	//==================================================================

}

//----------------------------------------------------------------------
