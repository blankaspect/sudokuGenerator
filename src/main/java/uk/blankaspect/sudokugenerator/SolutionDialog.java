/*====================================================================*\

SolutionDialog.java

Class: solution dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.text.DecimalFormat;

import java.util.List;

import java.util.concurrent.atomic.AtomicLong;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.concurrent.Task;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import javafx.util.Duration;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.ui.jfx.button.AlternativeTextButton;
import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.task.AbstractSoftCancelTask;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: SOLUTION DIALOG


/**
 * This class implements a modal dialog in which a search for solutions to a specified puzzle may be performed.
 */

class SolutionDialog
	extends SimpleModalDialog<List<Puzzle>>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_MAX_NUM_SOLUTIONS		= 1;
	public static final		int		MAX_MAX_NUM_SOLUTIONS		= 99_999;
	public static final		int		DEFAULT_MAX_NUM_SOLUTIONS	= 1;

	private static final	int		MAX_NUM_SOLUTIONS_NUM_DIGITS	=
			NumberUtils.getNumDecDigitsInt(MAX_MAX_NUM_SOLUTIONS);

	private static final	int		UPDATE_TIMER_INTERVAL	= 250;

	private static final	String	NUM_SOLUTIONS_PROTOTYPE_TEXT	= "0".repeat(MAX_NUM_SOLUTIONS_NUM_DIGITS) + ",";

	private static final	Insets	NUM_SOLUTIONS_LABEL_PADDING	= new Insets(3.0, 6.0, 3.0, 6.0);

	/** The margins around a check box. */
	private static final	Insets	CHECK_BOX_MARGINS	= new Insets(2.0, 0.0, 2.0, 0.0);

	/** The horizontal gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The vertical gap between adjacent components of the control pane. */
	private static final	double	CONTROL_PANE_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	private static final	DecimalFormat	INTEGER_FORMATTER;

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	SEARCH_FOR_SOLUTIONS_STR	= "Search for solutions";
	private static final	String	MAX_NUM_SOLUTIONS_STR		= "Maximum number of solutions";
	private static final	String	RANDOMISE_SEARCH_STR		= "Randomise search";
	private static final	String	NUM_SOLUTIONS_STR			= "Number solutions found";
	private static final	String	SEARCH_STR					= "Search";
	private static final	String	STOP_STR					= "Stop";
	private static final	String	SEARCHING_FOR_SOLUTIONS_STR	= "Searching for solutions";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.NUM_SOLUTIONS_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.SOLUTION_DIALOG_ROOT)
					.desc(StyleClass.NUM_SOLUTIONS_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NUM_SOLUTIONS_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.SOLUTION_DIALOG_ROOT)
					.desc(StyleClass.NUM_SOLUTIONS_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NUM_SOLUTIONS_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SOLUTION_DIALOG_ROOT)
					.desc(StyleClass.NUM_SOLUTIONS_LABEL)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	SOLUTION_DIALOG_ROOT	= StyleConstants.APP_CLASS_PREFIX + "solution-dialog-root";

		String	NUM_SOLUTIONS_LABEL	= StyleConstants.CLASS_PREFIX + "num-solutions-label";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	NUM_SOLUTIONS_LABEL_BACKGROUND	= PREFIX + "numSolutionsLabel.background";
		String	NUM_SOLUTIONS_LABEL_BORDER		= PREFIX + "numSolutionsLabel.border";
		String	NUM_SOLUTIONS_LABEL_TEXT		= PREFIX + "numSolutionsLabel.text";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Puzzle>					result;
	private	AtomicLong						solutionsCount;
	private	long							solutionsCountLastUpdate;
	private	Timeline						updateTimer;
	private	Task<List<Puzzle>>				task;
	private	Label							numSolutionsLabel;
	private	AlternativeTextButton<String>	cancelStopButton;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(SolutionDialog.class, COLOUR_PROPERTIES);

		// Initialise integer formatter
		INTEGER_FORMATTER = new DecimalFormat();
		INTEGER_FORMATTER.setGroupingSize(3);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private SolutionDialog(
		Window	owner,
		Puzzle	puzzle)
	{
		// Call superclass constructor
		super(owner, SEARCH_FOR_SOLUTIONS_STR, state.locator(), null);

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.SOLUTION_DIALOG_ROOT);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_PANE_H_GAP);
		controlPane.setVgap(CONTROL_PANE_V_GAP);
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

		// Spinner: maximum number of solutions
		Spinner<Integer> maxNumSolutionsSpinner =
				SpinnerFactory.integerSpinner(MIN_MAX_NUM_SOLUTIONS, MAX_MAX_NUM_SOLUTIONS, state.maxNumSolutions,
											  MAX_NUM_SOLUTIONS_NUM_DIGITS);
		controlPane.addRow(row++, new Label(MAX_NUM_SOLUTIONS_STR), maxNumSolutionsSpinner);

		// Check box: randomise search
		CheckBox randomiseSearchCheckBox = new CheckBox(RANDOMISE_SEARCH_STR);
		randomiseSearchCheckBox.setSelected(state.randomiseSearch);
		GridPane.setMargin(randomiseSearchCheckBox, CHECK_BOX_MARGINS);
		controlPane.add(randomiseSearchCheckBox, 1, row++);

		// Create list of components to disable while solving
		List<Node> componentsToDisable = List.copyOf(controlPane.getChildren());

		// Label: number of solutions
		numSolutionsLabel = new Label();
		numSolutionsLabel.setMinWidth(Region.USE_PREF_SIZE);
		numSolutionsLabel.setAlignment(Pos.CENTER_RIGHT);
		numSolutionsLabel.setPadding(NUM_SOLUTIONS_LABEL_PADDING);
		numSolutionsLabel.setTextFill(getColour(ColourKey.NUM_SOLUTIONS_LABEL_TEXT));
		numSolutionsLabel.setBackground(SceneUtils.createColouredBackground(
				getColour(ColourKey.NUM_SOLUTIONS_LABEL_BACKGROUND)));
		numSolutionsLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NUM_SOLUTIONS_LABEL_BORDER)));
		Insets insets = numSolutionsLabel.getInsets();
		numSolutionsLabel.setPrefWidth(TextUtils.textWidthCeil(NUM_SOLUTIONS_PROTOTYPE_TEXT) + insets.getLeft()
				+ insets.getRight());
		numSolutionsLabel.getStyleClass().add(StyleClass.NUM_SOLUTIONS_LABEL);
		controlPane.addRow(row++, new Label(NUM_SOLUTIONS_STR), numSolutionsLabel);

		// Add control pane to content pane
		addContent(controlPane);

		// Button: search
		Button searchButton = Buttons.hNoShrink(SEARCH_STR);
		searchButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		searchButton.setOnAction(event ->
		{
			// Disable components
			componentsToDisable.stream().forEach(node -> node.setDisable(true));
			searchButton.setDisable(true);

			// Update text of 'cancel/stop' button
			cancelStopButton.selectItem(STOP_STR);

			// Search for solutions to puzzle
			solve(puzzle, maxNumSolutionsSpinner.getValue(), randomiseSearchCheckBox.isSelected(), () ->
			{
				// Enable components that were disabled during search
				componentsToDisable.stream().forEach(node -> node.setDisable(false));
				searchButton.setDisable(false);
				cancelStopButton.setDisable(false);

				// Update text of 'cancel/stop' button
				cancelStopButton.selectItem(CANCEL_STR);
			});
		});
		addButton(searchButton, HPos.RIGHT);

		// Button: cancel/stop
		cancelStopButton = new AlternativeTextButton<>(CANCEL_STR, STOP_STR);
		cancelStopButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelStopButton.setMinWidth(Region.USE_PREF_SIZE);
		cancelStopButton.setOnAction(event ->
		{
			if ((task != null) && task.isRunning())
			{
				task.cancel();
				cancelStopButton.setDisable(true);
			}
			else
				requestClose();
		});
		addButton(cancelStopButton, HPos.RIGHT);

		// Fire 'cancel/stop' button if Escape key is pressed; fire 'search' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelStopButton, searchButton);

		// Test whether window can be closed
		setOnCloseRequest(event ->
		{
			if ((task != null) && task.isRunning())
			{
				task.cancel();
				cancelStopButton.setDisable(true);
				event.consume();
			}
		});

		// When dialog is closed, stop update timer and save dialog state
		setOnHiding(event ->
		{
			// Stop update timer
			if (updateTimer != null)
				updateTimer.stop();

			// Save dialog state
			state.restoreAndUpdate(this, true);
			state.maxNumSolutions = maxNumSolutionsSpinner.getValue();
			state.randomiseSearch = randomiseSearchCheckBox.isSelected();
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<Puzzle> show(
		Window	owner,
		Puzzle	puzzle)
	{
		return new SolutionDialog(owner, puzzle).showDialog();
	}

	//------------------------------------------------------------------

	public static MapNode encodeState()
	{
		return state.encodeTree();
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	rootNode)
	{
		state.decodeTree(rootNode);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the colour that is associated with the specified key in the colour map of the current theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the current theme of the style
	 *         manager, or {@link StyleManager#DEFAULT_COLOUR} if there is no such colour.
	 */

	private static Color getColour(
		String	key)
	{
		return StyleManager.INSTANCE.getColourOrDefault(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected List<Puzzle> getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void solve(
		Puzzle		puzzle,
		int			maxNumSolutions,
		boolean		randomiseSearch,
		IProcedure0	onCancelled)
	{
		// Start timer to update 'number of solutions' label periodically
		solutionsCount = new AtomicLong();
		solutionsCountLastUpdate = 0;
		updateNumSolutions();
		updateTimer = new Timeline(new KeyFrame(Duration.millis((double)UPDATE_TIMER_INTERVAL), event ->
		{
			long count = solutionsCount.get();
			if (solutionsCountLastUpdate < count)
			{
				solutionsCountLastUpdate = count;
				updateNumSolutions();
			}
		}));
		updateTimer.setCycleCount(Animation.INDEFINITE);
		updateTimer.play();

		// Create task to search for solutions
		task = new AbstractSoftCancelTask<>()
		{
			{
				updateTitle(SEARCH_FOR_SOLUTIONS_STR);
				updateMessage(SEARCHING_FOR_SOLUTIONS_STR + " " + ELLIPSIS_STR);
				updateProgress(-1, 1);
			}

			@Override
			protected List<Puzzle> call()
				throws Exception
			{
				return puzzle.clone().solve(maxNumSolutions, randomiseSearch, solutionsCount, this::isCancelled);
			}

			@Override
			protected void succeeded()
			{
				result = getValue();
				hide();
			}

			@Override
			protected void failed()
			{
				if (isCancelled())
					onCancelled.invoke();
				else
				{
					ErrorDialog.show(SolutionDialog.this, getTitle(), getException());
					hide();
				}
			}
		};

		// Execute task on background thread
		SudokuGeneratorApp.instance().executeTask(task);
	}

	//------------------------------------------------------------------

	private void updateNumSolutions()
	{
		numSolutionsLabel.setText(INTEGER_FORMATTER.format(solutionsCountLastUpdate));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: DIALOG STATE


	private static class State
		extends DialogState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	boolean	DEFAULT_RANDOMISE_SEARCH	= true;

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	RANDOMISE_SEARCH	= "randomiseSearch";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		maxNumSolutions;
		private	boolean	randomiseSearch;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, false);

			// Initialise instance variables
			maxNumSolutions = DEFAULT_MAX_NUM_SOLUTIONS;
			randomiseSearch = DEFAULT_RANDOMISE_SEARCH;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public MapNode encodeTree()
		{
			// Call superclass method
			MapNode rootNode = super.encodeTree();

			// Encode 'randomise search' flag
			rootNode.addBoolean(PropertyKey.RANDOMISE_SEARCH, randomiseSearch);

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decodeTree(
			MapNode	rootNode)
		{
			// Call superclass method
			super.decodeTree(rootNode);

			// Decode 'randomise search' flag
			randomiseSearch = rootNode.getBoolean(PropertyKey.RANDOMISE_SEARCH, DEFAULT_RANDOMISE_SEARCH);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
