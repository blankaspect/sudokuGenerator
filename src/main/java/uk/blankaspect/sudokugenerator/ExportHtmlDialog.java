/*====================================================================*\

ExportHtmlDialog.java

Class: 'export HTML' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
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

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

//----------------------------------------------------------------------


// CLASS: 'EXPORT HTML' DIALOG


class ExportHtmlDialog
	extends SimpleModalDialog<ExportHtmlDialog.Result>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	PuzzleIO.ColourScheme	DEFAULT_COLOUR_SCHEME	= PuzzleIO.ColourScheme.AUTOMATIC;

	private static final	int		MIN_FONT_SIZE		= 6;
	private static final	int		MAX_FONT_SIZE		= 48;
	private static final	int		DEFAULT_FONT_SIZE	= 14;

	private static final	int		MIN_NUM_COLUMNS		= 1;
	private static final	int		MAX_NUM_COLUMNS		= 12;
	private static final	int		DEFAULT_NUM_COLUMNS	= 1;

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	/** Miscellaneous strings. */
	private static final	String	EXPORT_HTML_STR		= "Export %s to HTML file";
	private static final	String	PUZZLE_STR			= "puzzle";
	private static final	String	PUZZLES_STR			= "puzzles";
	private static final	String	COLOUR_SCHEME_STR	= "Colour scheme";
	private static final	String	FONT_SIZE_STR		= "Font size";
	private static final	String	PT_STR				= "pt";
	private static final	String	NUM_COLUMNS_STR		= "Number of columns";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	COLOUR_SCHEME	= "colourScheme";
		String	FONT_SIZE		= "fontSize";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	PuzzleIO.ColourScheme	colourScheme	= DEFAULT_COLOUR_SCHEME;
	private static	int						fontSize		= DEFAULT_FONT_SIZE;
	private static	int						numColumns		= DEFAULT_NUM_COLUMNS;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Result	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ExportHtmlDialog(
		Window	owner,
		int		numPuzzles)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getCanonicalName() + "-" + owner.getClass().getSimpleName(),
			  null, String.format(EXPORT_HTML_STR, (numPuzzles < 2) ? PUZZLE_STR : PUZZLES_STR));

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

		// Spinner: colour scheme
		CollectionSpinner<PuzzleIO.ColourScheme> colourSchemeSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, PuzzleIO.ColourScheme.class, colourScheme, null, null);
		controlPane.addRow(row++, new Label(COLOUR_SCHEME_STR), colourSchemeSpinner);

		// Spinner: font size
		Spinner<Integer> fontSizeSpinner = SpinnerFactory.integerSpinner(MIN_FONT_SIZE, MAX_FONT_SIZE, fontSize,
																		 NumberUtils.getNumDecDigitsInt(MAX_FONT_SIZE));

		// Pane: font size
		HBox fontSizePane = new HBox(CONTROL_H_GAP, fontSizeSpinner, Labels.hNoShrink(PT_STR));
		fontSizePane.setAlignment(Pos.CENTER_LEFT);
		controlPane.addRow(row++, new Label(FONT_SIZE_STR), fontSizePane);

		// Label: number of columns
		Label numColumnsLabel = new Label(NUM_COLUMNS_STR);
		numColumnsLabel.setDisable(numPuzzles < 2);

		// Spinner: number of columns
		Spinner<Integer> numColumnsSpinner =
				SpinnerFactory.integerSpinner(MIN_NUM_COLUMNS, Math.min(numPuzzles, MAX_NUM_COLUMNS),
											  Math.min(numPuzzles, numColumns),
											  NumberUtils.getNumDecDigitsInt(MAX_NUM_COLUMNS));
		numColumnsSpinner.setDisable(numPuzzles < 2);
		controlPane.addRow(row++, numColumnsLabel, numColumnsSpinner);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Update dialog state
			colourScheme = colourSchemeSpinner.getItem();
			fontSize = fontSizeSpinner.getValue();
			numColumns = numColumnsSpinner.getValue();

			// Set result
			result = new Result(colourScheme, fontSize, numColumns);

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

		// Request focus when dialog is shown
		setOnShown(event -> colourSchemeSpinner.textBox().requestFocus());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Result show(
		Window	owner,
		int		numPuzzles)
	{
		return new ExportHtmlDialog(owner, numPuzzles).showDialog();
	}

	//------------------------------------------------------------------

	public static MapNode encodeState()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode colour scheme
		rootNode.addString(PropertyKey.COLOUR_SCHEME, colourScheme.key());

		// Encode font size
		rootNode.addInt(PropertyKey.FONT_SIZE, fontSize);

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		// Decode colour scheme
		String key = PropertyKey.COLOUR_SCHEME;
		if (rootNode.hasString(key))
		{
			colourScheme = rootNode.getEnumValue(PuzzleIO.ColourScheme.class, key, PuzzleIO.ColourScheme::key,
												 DEFAULT_COLOUR_SCHEME);
		}

		// Decode font size
		key = PropertyKey.FONT_SIZE;
		if (rootNode.hasInt(key))
			fontSize = rootNode.getInt(key);
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


	// RECORD: RESULT OF 'EXPORT HTML' DIALOG


	record Result(
		PuzzleIO.ColourScheme	colourScheme,
		int						fontSize,
		int						numColumns)
	{ }

	//==================================================================

}

//----------------------------------------------------------------------
