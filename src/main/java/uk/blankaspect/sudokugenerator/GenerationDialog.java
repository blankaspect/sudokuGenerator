/*====================================================================*\

GenerationDialog.java

Class: puzzle-generation dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.text.DecimalFormat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.atomic.AtomicLong;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.beans.InvalidationListener;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.concurrent.Task;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import javafx.util.Duration;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.random.IPrng;
import uk.blankaspect.common.random.PrngXoshiro256ss;

import uk.blankaspect.ui.jfx.button.AlternativeTextButton;
import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.IntRangeSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.task.AbstractSoftCancelTask;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.textfield.FilterFactory;
import uk.blankaspect.ui.jfx.textfield.TextFieldUtils;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

//----------------------------------------------------------------------


// CLASS: PUZZLE-GENERATION DIALOG


/**
 * This class implements a modal dialog in which a puzzle may be generated.
 */

class GenerationDialog
	extends SimpleModalDialog<Boolean>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_NUM_ENTRIES	= 4;

	private static final	int		MIN_NUM_THREADS	= 0;
	private static final	int		MAX_NUM_THREADS	= 128;
	private static final	int		NUM_THREADS_NUM_DIGITS	= NumberUtils.getNumDecDigitsInt(MAX_NUM_THREADS);

	private static final	int		UPDATE_TIMER_INTERVAL	= 250;

	private static final	String	NUM_ATTEMPTS_PROTOTYPE_TEXT	= "0".repeat(20) + ",".repeat(6);

	private static final	Insets	NUM_ATTEMPTS_LABEL_PADDING	= new Insets(3.0, 6.0, 3.0, 6.0);

	private static final	Insets	NUM_ATTEMPTS_PANE_PADDING	= new Insets(6.0, 12.0, 6.0, 12.0);

	/** The padding around the mode-page pane. */
	private static final	Insets	MODE_PAGE_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 8.0;

	/** The margins around a check box. */
	private static final	Insets	CHECK_BOX_MARGINS	= new Insets(2.0, 0.0, 2.0, 0.0);

	/** The key combination that selects the previous mode. */
	private static final	KeyCombination	KEY_COMBO_PREVIOUS_MODE	=
			new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN);

	/** The key combination that selects the next mode. */
	private static final	KeyCombination	KEY_COMBO_NEXT_MODE	=
			new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN);

	/** The key combination that fires the <i>random seed</i> button. */
	private static final	KeyCombination	KEY_COMBO_RANDOM_SEED	=
			new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);

	private static final	DecimalFormat	INTEGER_FORMATTER;

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	GENERATE_PUZZLE_STR			= "Generate puzzle";
	private static final	String	SEED_STR					= "Seed";
	private static final	String	GENERATE_RANDOM_SEED_STR	= "Generate a random seed";
	private static final	String	MODE_STR					= "Mode";
	private static final	String	NUM_ATTEMPTS_STR			= "Number of attempts";
	private static final	String	GENERATE_STR				= "Generate";
	private static final	String	STOP_STR					= "Stop";
	private static final	String	GENERATING_PUZZLES_STR		= "Generating puzzles";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.SECONDARY_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.SECONDARY_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.NUM_ATTEMPTS_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.NUM_ATTEMPTS_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NUM_ATTEMPTS_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.NUM_ATTEMPTS_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NUM_ATTEMPTS_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.NUM_ATTEMPTS_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NUM_ATTEMPTS_PANE_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.NUM_ATTEMPTS_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NUM_ATTEMPTS_PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.GENERATION_DIALOG_ROOT)
					.desc(StyleClass.NUM_ATTEMPTS_PANE)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.GENERATION_DIALOG_ROOT)
						.desc(StyleClass.NUM_ATTEMPTS_PANE)
						.build())
				.borders(Side.TOP, Side.BOTTOM)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	GENERATION_DIALOG_ROOT	= StyleConstants.APP_CLASS_PREFIX + "generation-dialog-root";

		String	NUM_ATTEMPTS_LABEL	= StyleConstants.CLASS_PREFIX + "num-attempts-label";
		String	NUM_ATTEMPTS_PANE	= StyleConstants.CLASS_PREFIX + "num-attempts-pane";
		String	SECONDARY_LABEL		= StyleConstants.CLASS_PREFIX + "secondary-label";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	NUM_ATTEMPTS_LABEL_BACKGROUND	= PREFIX + "numAttemptsLabel.background";
		String	NUM_ATTEMPTS_LABEL_BORDER		= PREFIX + "numAttemptsLabel.border";
		String	NUM_ATTEMPTS_LABEL_TEXT			= PREFIX + "numAttemptsLabel.text";
		String	NUM_ATTEMPTS_PANE_BACKGROUND	= PREFIX + "numAttemptsPane.background";
		String	NUM_ATTEMPTS_PANE_BORDER		= PREFIX + "numAttemptsPane.border";
		String	SECONDARY_LABEL_TEXT			= PREFIX + "secondaryLabel.text";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state		= new State();
	private static	IPrng	seedPrng	= new PrngXoshiro256ss();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean							result;
	private	GenerationMode					selectedMode;
	private	AtomicLong						attemptsCount;
	private	long							attemptsCountLastUpdate;
	private	Timeline						updateTimer;
	private	Task<Void>						task;
	private	Label							numAttemptsLabel;
	private	AlternativeTextButton<String>	cancelStopButton;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(GenerationDialog.class, COLOUR_PROPERTIES, RULE_SETS);

		// Initialise integer formatter
		INTEGER_FORMATTER = new DecimalFormat();
		INTEGER_FORMATTER.setGroupingSize(3);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private GenerationDialog(
		Window	owner,
		Puzzle	puzzle)
	{
		// Call superclass constructor
		super(owner, GENERATE_PUZZLE_STR, state.locator(), null);

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.GENERATION_DIALOG_ROOT);

		// Create mode-page pane
		StackPane modePagePane = new StackPane();
		modePagePane.setPadding(MODE_PAGE_PANE_PADDING);
		VBox.setVgrow(modePagePane, Priority.ALWAYS);

		// Create mode pages
		Map<GenerationMode, IModePage> modePages = new EnumMap<>(GenerationMode.class);
		for (GenerationMode mode : GenerationMode.values())
		{
			GenerationParams params = state.paramSets.get(mode);
			IModePage modePage = switch (mode)
			{
				case ADDITIVE    -> new AdditivePage(puzzle, (GenerationParams.Additive)params);
				case SUBTRACTIVE -> new SubtractivePage(puzzle, (GenerationParams.Subtractive)params);
			};
			modePages.put(mode, modePage);
			modePage.pane().setVisible(false);
			modePagePane.getChildren().add(modePage.pane());
		}

		// Label: number of attempts
		numAttemptsLabel = new Label();
		numAttemptsLabel.setMinWidth(Region.USE_PREF_SIZE);
		numAttemptsLabel.setAlignment(Pos.CENTER_RIGHT);
		numAttemptsLabel.setPadding(NUM_ATTEMPTS_LABEL_PADDING);
		numAttemptsLabel.setTextFill(getColour(ColourKey.NUM_ATTEMPTS_LABEL_TEXT));
		numAttemptsLabel.setBackground(SceneUtils.createColouredBackground(
				getColour(ColourKey.NUM_ATTEMPTS_LABEL_BACKGROUND)));
		numAttemptsLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NUM_ATTEMPTS_LABEL_BORDER)));
		Insets insets = numAttemptsLabel.getInsets();
		numAttemptsLabel.setPrefWidth(TextUtils.textWidthCeil(NUM_ATTEMPTS_PROTOTYPE_TEXT) + insets.getLeft()
				+ insets.getRight());
		numAttemptsLabel.getStyleClass().add(StyleClass.NUM_ATTEMPTS_LABEL);

		// Pane: number of attempts
		HBox numAttemptsPane = new HBox(CONTROL_H_GAP, Labels.hNoShrink(NUM_ATTEMPTS_STR), numAttemptsLabel);
		numAttemptsPane.setAlignment(Pos.CENTER);
		numAttemptsPane.setPadding(NUM_ATTEMPTS_PANE_PADDING);
		numAttemptsPane.setBackground(SceneUtils.createColouredBackground(
				getColour(ColourKey.NUM_ATTEMPTS_PANE_BACKGROUND)));
		numAttemptsPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NUM_ATTEMPTS_PANE_BORDER)));
		numAttemptsPane.getStyleClass().add(StyleClass.NUM_ATTEMPTS_PANE);

		// Create content pane
		VBox contentPane = new VBox(modePagePane, numAttemptsPane);
		contentPane.setAlignment(Pos.TOP_CENTER);

		// Set content pane
		setContent(contentPane);

		// Spinner: generation mode
		CollectionSpinner<GenerationMode> generationModeSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, GenerationMode.class, state.generationMode, null, null);

		// Pane: generation mode
		HBox generationModePane = new HBox(CONTROL_H_GAP, Labels.hNoShrink(MODE_STR), generationModeSpinner);
		generationModePane.setAlignment(Pos.CENTER_LEFT);
		addButton(generationModePane, HPos.LEFT, false);

		// Button: generate
		Button generateButton = Buttons.hNoShrink(GENERATE_STR);
		generateButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		generateButton.setOnAction(event ->
		{
			// Disable components
			modePagePane.setDisable(true);
			generationModePane.setDisable(true);
			generateButton.setDisable(true);

			// Update text of 'cancel/stop' button
			cancelStopButton.selectItem(STOP_STR);

			// Generate puzzle
			generate(puzzle, selectedMode, modePages.get(selectedMode).params(), () ->
			{
				// Enable components that were disabled during generation
				modePagePane.setDisable(false);
				generationModePane.setDisable(false);
				generateButton.setDisable(false);
				cancelStopButton.setDisable(false);

				// Update text of 'cancel/stop' button
				cancelStopButton.selectItem(CANCEL_STR);
			});
		});
		addButton(generateButton, HPos.RIGHT);

		// Create procedure to enable/disable 'generate' button according to 'can generate' state of current mode page
		IProcedure0 updateGenerateButton = () ->
				generateButton.setDisable(!modePages.get(selectedMode).canGenerate().get());

		// Create listener to enable/disable 'generate' button when mode changes
		InvalidationListener canGenerateListener = observable -> updateGenerateButton.invoke();

		// Create procedure to update mode page
		IProcedure0 updateModePage = () ->
		{
			GenerationMode newMode = generationModeSpinner.getItem();
			if (selectedMode != newMode)
			{
				// Deselect old mode page
				if (selectedMode != null)
				{
					IModePage modePage = modePages.get(selectedMode);
					modePage.onDeselecting();
					modePage.canGenerate().removeListener(canGenerateListener);
					modePage.pane().setVisible(false);
				}

				// Select new mode page
				selectedMode = newMode;
				IModePage modePage = modePages.get(selectedMode);
				modePage.onSelecting();
				modePage.canGenerate().addListener(canGenerateListener);
				modePage.pane().setVisible(true);

				// Enable/disable 'generate' button
				updateGenerateButton.invoke();
			}
		};

		// Update mode page when mode changes
		generationModeSpinner.itemProperty().addListener(observable -> updateModePage.invoke());

		// Select initial mode page
		updateModePage.invoke();

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

		// Fire 'cancel/stop' button if Escape key is pressed; fire 'generate' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelStopButton, generateButton);

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
			state.generationMode = selectedMode;
			for (GenerationMode mode : modePages.keySet())
				state.paramSets.put(mode, modePages.get(mode).params());
		});

		// Handle 'key pressed' events
		getScene().addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			if (!generationModeSpinner.isDisabled())
			{
				if (KEY_COMBO_PREVIOUS_MODE.match(event))
				{
					int index = generationModeSpinner.value();
					if (--index < generationModeSpinner.minValue())
						index = generationModeSpinner.maxValue();
					generationModeSpinner.setValue(index);
					event.consume();
				}
				else if (KEY_COMBO_NEXT_MODE.match(event))
				{
					int index = generationModeSpinner.value();
					if (++index > generationModeSpinner.maxValue())
						index = generationModeSpinner.minValue();
					generationModeSpinner.setValue(index);
					event.consume();
				}
			}
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Boolean show(
		Window	owner,
		Puzzle	puzzle)
	{
		return new GenerationDialog(owner, puzzle).showDialog();
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
	protected Boolean getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void generate(
		Puzzle				puzzle,
		GenerationMode		mode,
		GenerationParams	params,
		IProcedure0			onCancelled)
	{
		// Start timer to update 'number of attempts' label periodically
		attemptsCount = new AtomicLong();
		attemptsCountLastUpdate = 0;
		updateNumAttempts();
		updateTimer = new Timeline(new KeyFrame(Duration.millis((double)UPDATE_TIMER_INTERVAL), event ->
		{
			long count = attemptsCount.get();
			if (attemptsCountLastUpdate < count)
			{
				attemptsCountLastUpdate = count;
				updateNumAttempts();
			}
		}));
		updateTimer.setCycleCount(Animation.INDEFINITE);
		updateTimer.play();

		// Create task to generate puzzle
		task = new AbstractSoftCancelTask<>()
		{
			{
				updateTitle(GENERATE_PUZZLE_STR);
				updateMessage(GENERATING_PUZZLES_STR + " " + ELLIPSIS_STR);
				updateProgress(-1, 1);
			}

			@Override
			protected Void call()
				throws Exception
			{
				// Generate entries
				switch (mode)
				{
					case ADDITIVE:
					{
						GenerationParams.Additive p = (GenerationParams.Additive)params;
						int numEntries = puzzle.hasEntries() ? 0 : p.numEntries(puzzle.puzzleOrder());
						puzzle.generateAdditive(p.seed(), numEntries, p.randomiseVerification(), p.numThreads(),
												attemptsCount, this::isCancelled);
						break;
					}

					case SUBTRACTIVE:
					{
						GenerationParams.Subtractive p = (GenerationParams.Subtractive)params;
						int numEntries = puzzle.hasEntries() ? 0 : p.numEntries(puzzle.puzzleOrder());
						puzzle.generateSubtractive(p.seed(), numEntries, p.randomiseVerification(), p.numThreads(),
												   p.maxFillTime(), p.verifyIncrementally(), attemptsCount,
												   this::isCancelled);
						break;
					}
				}

				// Return nothing
				return null;
			}

			@Override
			protected void succeeded()
			{
				result = true;
				hide();
			}

			@Override
			protected void failed()
			{
				if (isCancelled())
					onCancelled.invoke();
				else
				{
					ErrorDialog.show(GenerationDialog.this, getTitle(), getException());
					hide();
				}
			}

			@Override
			protected void cancelled()
			{
				onCancelled.invoke();
			}
		};

		// Execute task on background thread
		SudokuGeneratorApp.instance().executeTask(task);
	}

	//------------------------------------------------------------------

	private void updateNumAttempts()
	{
		numAttemptsLabel.setText(INTEGER_FORMATTER.format(attemptsCountLastUpdate));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: MODE PAGE


	private interface IModePage
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		GenerationParams params();

		//--------------------------------------------------------------

		Pane pane();

		//--------------------------------------------------------------

		ReadOnlyBooleanProperty canGenerate();

		//--------------------------------------------------------------

		void onSelecting();

		//--------------------------------------------------------------

		void onDeselecting();

		//--------------------------------------------------------------

	}

	//==================================================================

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

		private static final	GenerationMode	DEFAULT_GENERATION_MODE	= GenerationMode.ADDITIVE;

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	MODE	= "mode";
			String	PARAMS	= "params";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	GenerationMode							generationMode;
		private Map<GenerationMode, GenerationParams>	paramSets;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, false);

			// Initialise instance variables
			generationMode = DEFAULT_GENERATION_MODE;
			paramSets = new EnumMap<>(GenerationMode.class);
			for (GenerationMode mode : GenerationMode.values())
				paramSets.put(mode, mode.newParams());
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

			// Encode generation mode
			rootNode.addString(PropertyKey.MODE, generationMode.key());

			// Encode parameter sets
			MapNode paramsNode = rootNode.addMap(PropertyKey.PARAMS);
			for (GenerationMode mode : paramSets.keySet())
				paramsNode.add(mode.key(), paramSets.get(mode).encode());

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

			// Decode generation mode
			generationMode = rootNode.getEnumValue(GenerationMode.class, PropertyKey.MODE,
												   GenerationMode::key, DEFAULT_GENERATION_MODE);

			// Decode parameter sets
			String key = PropertyKey.PARAMS;
			if (rootNode.hasMap(key))
			{
				MapNode paramsNode = rootNode.getMapNode(key);
				for (GenerationMode mode : GenerationMode.values())
				{
					key = mode.key();
					if (paramsNode.hasMap(key))
						paramSets.computeIfAbsent(mode, GenerationMode::newParams).decode(paramsNode.getMapNode(key));
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: ADDITIVE-GENERATION PAGE


	private static class AdditivePage
		implements IModePage
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	GenerationParams.Additive	params;
		private	ModePagePane				pane;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AdditivePage(
			Puzzle						puzzle,
			GenerationParams.Additive	params)
		{
			// Initialise instance variables
			this.params = params;

			// Create pane
			pane = new ModePagePane(puzzle, params);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IModePane interface
	////////////////////////////////////////////////////////////////////

		@Override
		public GenerationParams.Additive params()
		{
			Map<Puzzle.Order, Integer> numEntries = new EnumMap<>(params.numEntries());
			if (!pane.hasEntries)
				numEntries.put(pane.puzzleOrder, pane.numEntriesSpinner.getValue());

			return new GenerationParams.Additive(
					numEntries, pane.seed(), pane.randomiseVerificationCheckBox.isSelected(),
					pane.numThreadsSpinner.getValue());
		}

		//--------------------------------------------------------------

		@Override
		public GridPane pane()
		{
			return pane;
		}

		//--------------------------------------------------------------

		@Override
		public ReadOnlyBooleanProperty canGenerate()
		{
			return pane.canGenerate;
		}

		//--------------------------------------------------------------

		@Override
		public void onSelecting()
		{
			// Add key combination to fire 'random seed' button
			SceneUtils.getWindow(pane).getScene().getAccelerators()
					.put(KEY_COMBO_RANDOM_SEED, pane.randomSeedButton::fire);
		}

		//--------------------------------------------------------------

		@Override
		public void onDeselecting()
		{
			// Remove key combination
			SceneUtils.getWindow(pane).getScene().getAccelerators().remove(KEY_COMBO_RANDOM_SEED);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: SUBTRACTIVE-GENERATION PAGE


	private static class SubtractivePage
		implements IModePage
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int[]	MAX_FILL_TIMES	= { 0, -2, 1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20 };

		private static final	int		INITIAL_MAX_FILL_TIME_INDEX	= 3;

		/** Miscellaneous strings. */
		private static final	String	MAX_FILL_TIME_STR			= "Maximum fill time";
		private static final	String	NONE_STR					= "None";
		private static final	String	SECONDS_STR					= "seconds";
		private static final	String	VERIFY_INCREMENTALLY_STR	= "Verify incrementally";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	GenerationParams.Subtractive	params;
		private	IntRangeSpinner					maxFillTimeSpinner;
		private	CheckBox						verifyIncrementallyCheckBox;
		private	ModePagePane					pane;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SubtractivePage(
			Puzzle							puzzle,
			GenerationParams.Subtractive	params)
		{
			// Initialise instance variables
			this.params = params;

			// Create pane
			pane = new ModePagePane(puzzle, params);

			// Initialise row index
			int row = pane.getRowCount();

			// Spinner: maximum fill time
			maxFillTimeSpinner = IntRangeSpinner.leftRightH(HPos.CENTER, true, 0, MAX_FILL_TIMES.length - 1,
															millisecondsToIndex(params.maxFillTime()), null, index ->
			{
				int value = MAX_FILL_TIMES[index];
				return (value < 0)
							? Double.toString(1.0 / (double)-value)
							: (value == 0)
									? NONE_STR
									: Integer.toString(value);
			});

			// Label: seconds
			Label secondsLabel = Labels.hNoShrink(SECONDS_STR);
			secondsLabel.visibleProperty().bind(maxFillTimeSpinner.valueProperty().isNotEqualTo(0));

			// Pane: maximum fill time
			HBox maxFillTimePane = new HBox(6.0, maxFillTimeSpinner, secondsLabel);
			maxFillTimePane.setAlignment(Pos.CENTER_LEFT);
			pane.addRow(row++, new Label(MAX_FILL_TIME_STR), maxFillTimePane);

			// Check box: verify incrementally
			verifyIncrementallyCheckBox = new CheckBox(VERIFY_INCREMENTALLY_STR);
			verifyIncrementallyCheckBox.setSelected(params.verifyIncrementally());
			GridPane.setMargin(verifyIncrementallyCheckBox, CHECK_BOX_MARGINS);
			pane.add(verifyIncrementallyCheckBox, 1, row++);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static int indexToMillseconds(
			int	index)
		{
			int value = MAX_FILL_TIMES[index];
			return (value < 0) ? (int)Math.round(1000.0 / (double)-value) : value * 1000;
		}

		//--------------------------------------------------------------

		private static int millisecondsToIndex(
			int	milliseconds)
		{
			int value = milliseconds / 1000;
			if ((value == 0) && (milliseconds > 0))
				value = -1000 / milliseconds;
			for (int i = 0; i < MAX_FILL_TIMES.length; i++)
			{
				if (MAX_FILL_TIMES[i] == value)
					return i;
			}
			return INITIAL_MAX_FILL_TIME_INDEX;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IModePane interface
	////////////////////////////////////////////////////////////////////

		@Override
		public GenerationParams.Subtractive params()
		{
			Map<Puzzle.Order, Integer> numEntries = new EnumMap<>(params.numEntries());
			if (!pane.hasEntries)
				numEntries.put(pane.puzzleOrder, pane.numEntriesSpinner.getValue());

			return new GenerationParams.Subtractive(
					numEntries, pane.seed(), pane.randomiseVerificationCheckBox.isSelected(),
					pane.numThreadsSpinner.getValue(), indexToMillseconds(maxFillTimeSpinner.value()),
					verifyIncrementallyCheckBox.isSelected());
		}

		//--------------------------------------------------------------

		@Override
		public GridPane pane()
		{
			return pane;
		}

		//--------------------------------------------------------------

		@Override
		public ReadOnlyBooleanProperty canGenerate()
		{
			return pane.canGenerate;
		}

		//--------------------------------------------------------------

		@Override
		public void onSelecting()
		{
			// Add key combination to fire 'random seed' button
			SceneUtils.getWindow(pane).getScene().getAccelerators()
					.put(KEY_COMBO_RANDOM_SEED, pane.randomSeedButton::fire);
		}

		//--------------------------------------------------------------

		@Override
		public void onDeselecting()
		{
			// Remove key combination
			SceneUtils.getWindow(pane).getScene().getAccelerators().remove(KEY_COMBO_RANDOM_SEED);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: MODE-PAGE PANE


	private static class ModePagePane
		extends GridPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Insets	RANDOM_SEED_BUTTON_PADDING	= new Insets(3.0, 6.0, 3.0, 6.0);

		/** Miscellaneous strings. */
		private static final	String	NUM_ENTRIES_STR				= "Number of entries";
		private static final	String	RANDOM_STR					= "Random";
		private static final	String	RANDOMISE_VERIFICATION_STR	= "Randomise verification";
		private static final	String	NUM_THREADS_STR				= "Number of threads";
		private static final	String	ZERO_THREADS_STR			= " max(1, number of processors \u2212 1) = ";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Puzzle.Order			puzzleOrder;
		private	boolean					hasEntries;
		private	SimpleBooleanProperty	canGenerate;
		private	Spinner<Integer>		numEntriesSpinner;
		private	TextField				seedField;
		private	Button					randomSeedButton;
		private	CheckBox				randomiseVerificationCheckBox;
		private	Spinner<Integer>		numThreadsSpinner;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ModePagePane(
			Puzzle					puzzle,
			GenerationParams.Common	params)
		{
			// Initialise instance variables
			puzzleOrder = puzzle.puzzleOrder();
			hasEntries = puzzle.hasEntries();
			canGenerate = new SimpleBooleanProperty(false);

			// Set properties
			setHgap(CONTROL_H_GAP);
			setVgap(CONTROL_V_GAP);
			setAlignment(Pos.TOP_CENTER);

			// Initialise column constraints
			ColumnConstraints column = new ColumnConstraints();
			column.setMinWidth(Region.USE_PREF_SIZE);
			column.setHalignment(HPos.RIGHT);
			getColumnConstraints().add(column);

			column = new ColumnConstraints();
			column.setHalignment(HPos.LEFT);
			getColumnConstraints().add(column);

			// Initialise row index
			int row = 0;

			// Label: number of entries
			Label numEntriesLabel = new Label(NUM_ENTRIES_STR);
			numEntriesLabel.setDisable(hasEntries);

			// Spinner: number of entries
			int maxNumEntries = puzzle.numRows() * puzzle.numColumns() - 1;
			int numEntries = params.numEntries(puzzleOrder);
			numEntriesSpinner = SpinnerFactory.integerSpinner(MIN_NUM_ENTRIES, maxNumEntries, numEntries,
															  NumberUtils.getNumDecDigitsInt(maxNumEntries));
			numEntriesSpinner.setDisable(hasEntries);
			addRow(row++, numEntriesLabel, numEntriesSpinner);

			// Text field: seed for PRNG
			seedField = new TextField();
			int numDigits = NumberUtils.getNumDecDigitsLong(Long.MAX_VALUE);
			seedField.setTextFormatter(new TextFormatter<>(FilterFactory.decInteger(numDigits, false)));
			TextFieldUtils.setNumColumns(seedField, '0', numDigits);
			if (params.seed() != null)
				seedField.setText(params.seed().toString());
			canGenerate.bind(seedField.textProperty().isNotEmpty());

			// Button: random seed
			randomSeedButton = Buttons.hNoShrink(RANDOM_STR);
			randomSeedButton.setPadding(RANDOM_SEED_BUTTON_PADDING);
			randomSeedButton.setOnAction(event -> seedField.setText(Long.toString(seedPrng.nextLong())));
			TooltipDecorator.addTooltip(randomSeedButton,
										GENERATE_RANDOM_SEED_STR + " (" + KEY_COMBO_RANDOM_SEED.getDisplayText() + ")");

			// Pane: seed
			HBox seedPane = new HBox(CONTROL_H_GAP, seedField, randomSeedButton);
			seedPane.setAlignment(Pos.CENTER_LEFT);
			addRow(row++, new Label(SEED_STR), seedPane);

			// Check box: randomise verification
			randomiseVerificationCheckBox = new CheckBox(RANDOMISE_VERIFICATION_STR);
			randomiseVerificationCheckBox.setSelected(params.randomiseVerification());
			GridPane.setMargin(randomiseVerificationCheckBox, CHECK_BOX_MARGINS);
			add(randomiseVerificationCheckBox, 1, row++);

			// Label: number of threads
			Label numThreadsLabel = new Label(NUM_THREADS_STR);

			// Spinner: number of threads
			numThreadsSpinner = SpinnerFactory.integerSpinner(MIN_NUM_THREADS, MAX_NUM_THREADS, params.numThreads(),
															  NUM_THREADS_NUM_DIGITS);

			// Label: zero threads
			Label zeroThreadsLabel = Labels.hNoShrink(ZERO_THREADS_STR + Puzzle.actualNumThreads(0));
			zeroThreadsLabel.setTextFill(getColour(ColourKey.SECONDARY_LABEL_TEXT));
			zeroThreadsLabel.getStyleClass().add(StyleClass.SECONDARY_LABEL);
			zeroThreadsLabel.visibleProperty().bind(numThreadsSpinner.valueProperty().isEqualTo(0));

			// Pane: number of threads
			HBox numThreadsPane = new HBox(6.0, numThreadsSpinner, zeroThreadsLabel);
			numThreadsPane.setAlignment(Pos.CENTER_LEFT);
			addRow(row++, numThreadsLabel, numThreadsPane);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Long seed()
		{
			String text = seedField.getText();
			return text.isEmpty() ? null : Long.parseLong(text);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
