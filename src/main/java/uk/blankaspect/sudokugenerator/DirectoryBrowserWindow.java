/*====================================================================*\

DirectoryBrowserWindow.java

Class: directory-browser window.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;

import javafx.css.PseudoClass;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;

import javafx.scene.control.ScrollPane.ScrollBarPolicy;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Shape;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.filesystem.DirectoryUtils;
import uk.blankaspect.common.filesystem.PathnameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.message.MessageConstants;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.ImageButton;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleProgressDialog;

import uk.blankaspect.ui.jfx.exec.ExecUtils;

import uk.blankaspect.ui.jfx.filler.FillerUtils;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.label.Labels;
import uk.blankaspect.ui.jfx.label.OverlayLabel;

import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.scrollpane.ScrollPaneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.window.WindowDims;
import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: DIRECTORY-BROWSER WINDOW


class DirectoryBrowserWindow
	extends Stage
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** A map from system-property keys to the default values of the corresponding delays (in milliseconds) in the
		<i>WINDOW_SHOWN</i> event handler of the directory-browser window. */
	private static final	Map<String, Integer>	DIR_BROWSER_WINDOW_DELAYS	= Map.of
	(
		SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_SIZE,     100,
		SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_LOCATION,  25,
		SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_OPACITY,   25
	);

	/** The minimum width of the window. */
	private static final	double	MIN_WIDTH	= 120.0;

	/** The minimum height of the window. */
	private static final	double	MIN_HEIGHT	= 96.0;

	/** The margins that are applied to the visual bounds of each screen when determining whether the saved location
		of a window is within a screen. */
	private static final	Insets	SCREEN_MARGINS	= new Insets(0.0, 32.0, 32.0, 0.0);

	/** The minimum number of columns of the multi-puzzle pane. */
	private static final	int		MIN_NUM_COLUMNS		= 1;

	/** The maximum number of columns of the multi-puzzle pane. */
	private static final	int		MAX_NUM_COLUMNS		= 12;

	/** The default number of columns of the multi-puzzle pane. */
	private static final	int		DEFAULT_NUM_COLUMNS	= 3;

	/** The default width of the window. */
	private static final	double	DEFAULT_WIDTH	= 480.0;

	/** The default height of the window. */
	private static final	double	DEFAULT_HEIGHT	= 640.0;

	/** The padding around a name label. */
	private static final	Insets	NAME_LABEL_PADDING	= new Insets(1.0, 6.0, 1.0, 6.0);

	/** The padding around the <i>number of selected files</i> label. */
	private static final	Insets	NUM_SELECTED_LABEL_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	/** The padding around the button pane. */
	private static final	Insets	BUTTON_PANE_PADDING	= new Insets(2.0, 4.0, 2.0, 4.0);

	/** The horizontal gap between adjacent components in a container. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent components in a container. */
	private static final	double	CONTROL_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	/** The factor by which the size of the default font is multiplied to give the size of the font of the placeholder
		label. */
	private static final	double	PLACEHOLDER_LABEL_FONT_SIZE_FACTOR	= 1.25;

	/** The pseudo-class that is associated with the <i>selected</i> state. */
	private static final	PseudoClass	PSEUDO_CLASS_SELECTED	=
			PseudoClass.getPseudoClass(FxPseudoClass.SELECTED);

	/** The default initial directory of the directory chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= SystemUtils.userHomeDirectory();

	/** The key combination that fires the <i>show controls</i> button. */
	private static final	KeyCombination	KEY_COMBO_SHOW_CONTROLS		= new KeyCodeCombination(KeyCode.F1);

	/** The key combination that fires the <i>choose directory</i> button. */
	private static final	KeyCombination	KEY_COMBO_CHOOSE_DIRECTORY	= new KeyCodeCombination(KeyCode.F12);

	/** The key combination that fires the <i>refresh</i> button. */
	private static final	KeyCombination	KEY_COMBO_REFRESH			= new KeyCodeCombination(KeyCode.F5);

	/** The key combination that increments the number of columns. */
	private static final	KeyCombination	KEY_COMBO_INC_NUM_COLUMNS	=
			new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN);

	/** The key combination that decrements the number of columns. */
	private static final	KeyCombination	KEY_COMBO_DEC_NUM_COLUMNS	=
			new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN);

	/** The separator of the <i>number of selected files</i> label. */
	private static final	String	NUM_SELECTED_SEPARATOR	= " / ";

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR			= "...";
	private static final	String	NO_MATCHING_FILES_STR	= "No matching files";
	private static final	String	SHOW_CONTROLS_STR		= "Show controls (F1)";
	private static final	String	REFRESH_FILES_STR		= "Refresh files (F5)";
	private static final	String	NUM_SELECTED_STR		= "Number of selected files";
	private static final	String	SELECT_ALL_STR			= "Select all files";
	private static final	String	DESELECT_ALL_STR		= "Deselect all files";
	private static final	String	CHOOSE_DIRECTORY1_STR	= "Choose directory (F12)";
	private static final	String	CHOOSE_DIRECTORY2_STR	= "Choose directory containing puzzle files";
	private static final	String	READ_FILES_STR			= "Read files";
	private static final	String	SELECT_FILE_STR			= "Select file";
	private static final	String	DESELECT_FILE_STR		= "Deselect file";
	private static final	String	OPEN_FILE_STR			= "Open file";
	private static final	String	OPEN_SELECTED_STR		= "Open selected files";
	private static final	String	EXPORT_SELECTED_STR		= "Export selected puzzles to HTML file";
	private static final	String	READING_STR				= "Reading";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.NAME_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NAME_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NAME_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NAME_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NAME_LABEL_BACKGROUND_SELECTED,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.FILE_PANE).pseudo(FxPseudoClass.SELECTED)
					.desc(StyleClass.NAME_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NAME_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NAME_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NAME_LABEL_BORDER_SELECTED,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.FILE_PANE).pseudo(FxPseudoClass.SELECTED)
					.desc(StyleClass.NAME_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.NUM_SELECTED_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NUM_SELECTED_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.NUM_SELECTED_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NUM_SELECTED_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.NUM_SELECTED_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.NUM_SELECTED_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.PLACEHOLDER_TEXT,
			CssSelector.builder()
					.cls(StyleClass.MULTI_PUZZLE_WINDOW_ROOT)
					.desc(StyleClass.PLACEHOLDER_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.CONTROL_DIALOG_MAIN_PANE_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.CONTROL_DIALOG_MAIN_PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.CONTROL_DIALOG_CLOSE_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.desc(StyleClass.CLOSE_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.CONTROL_DIALOG_CLOSE_LABEL_BACKGROUND_HOVERED,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.desc(StyleClass.CLOSE_LABEL).pseudo(FxPseudoClass.HOVERED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.CONTROL_DIALOG_CLOSE_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.desc(StyleClass.CLOSE_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.CONTROL_DIALOG_CROSS_ICON,
			CssSelector.builder()
					.cls(StyleClass.CONTROL_DIALOG_ROOT)
					.desc(StyleClass.CROSS_ICON)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.CONTROL_DIALOG_ROOT)
						.desc(StyleClass.CLOSE_LABEL)
						.build())
				.borders(Side.LEFT)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	CONTROL_DIALOG_ROOT			= StyleConstants.APP_CLASS_PREFIX + "control-dialog-root";
		String	MULTI_PUZZLE_WINDOW_ROOT	= StyleConstants.APP_CLASS_PREFIX + "multi-puzzle-window-root";

		String	CLOSE_LABEL			= StyleConstants.CLASS_PREFIX + "close-label";
		String	CROSS_ICON			= StyleConstants.CLASS_PREFIX + "cross-icon";
		String	FILE_PANE			= StyleConstants.CLASS_PREFIX + "file-pane";
		String	NAME_LABEL			= StyleConstants.CLASS_PREFIX + "name-label";
		String	NUM_SELECTED_LABEL	= StyleConstants.CLASS_PREFIX + "num-selected-label";
		String	PLACEHOLDER_LABEL	= StyleConstants.CLASS_PREFIX + "placeholder-label";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	CONTROL_DIALOG_CLOSE_LABEL_BACKGROUND =
				PREFIX + "controlDialog.closeLabel.background";
		String	CONTROL_DIALOG_CLOSE_LABEL_BACKGROUND_HOVERED =
				PREFIX + "controlDialog.closeLabel.background.hovered";
		String	CONTROL_DIALOG_CLOSE_LABEL_BORDER =
				PREFIX + "controlDialog.closeLabel.border";
		String	CONTROL_DIALOG_CROSS_ICON =
				PREFIX + "controlDialog.crossIcon";
		String	CONTROL_DIALOG_MAIN_PANE_BACKGROUND =
				PREFIX + "controlDialog.mainPane.background";
		String	CONTROL_DIALOG_MAIN_PANE_BORDER =
				PREFIX + "controlDialog.mainPane.border";
		String	NAME_LABEL_BACKGROUND =
				PREFIX + "nameLabel.background";
		String	NAME_LABEL_BACKGROUND_SELECTED =
				PREFIX + "nameLabel.background.selected";
		String	NAME_LABEL_BORDER =
				PREFIX + "nameLabel.border";
		String	NAME_LABEL_BORDER_SELECTED =
				PREFIX + "nameLabel.border.selected";
		String	NAME_LABEL_TEXT =
				PREFIX + "nameLabel.text";
		String	NUM_SELECTED_LABEL_BACKGROUND =
				PREFIX + "numSelectedLabel.background";
		String	NUM_SELECTED_LABEL_BORDER =
				PREFIX + "numSelectedLabel.border";
		String	NUM_SELECTED_LABEL_TEXT =
				PREFIX + "numSelectedLabel.text";
		String	PLACEHOLDER_TEXT =
				PREFIX + "placeholder.text";
	}

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	DIR_BROWSER_WINDOW_DELAY_LOCATION	= "dirBrowserWindowDelay.location";
		String	DIR_BROWSER_WINDOW_DELAY_OPACITY	= "dirBrowserWindowDelay.opacity";
		String	DIR_BROWSER_WINDOW_DELAY_SIZE		= "dirBrowserWindowDelay.size";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State			state				= new State();
	private static	LocationChooser	directoryChooser;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Path								directory;
	private	IProcedure1<List<PuzzleDocument>>	openAction;
	private	IProcedure1<List<Puzzle>>			exportAction;
	private	Filter								filter;
	private	int									numFiles;
	private	Label								numSelectedLabel;
	private	HBox								buttonPane;
	private	Label								placeholderLabel;
	private	MultiPuzzlePane						multiPuzzlePane;
	private	ScrollPane							scrollPane;
	private	ControlDialog						controlDialog;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(DirectoryBrowserWindow.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DirectoryBrowserWindow(
		Window								owner,
		Path								directory,
		IProcedure1<List<PuzzleDocument>>	openAction,
		IProcedure1<List<Puzzle>>			exportAction)
	{
		// Initialise instance variables
		this.directory = directory;
		this.openAction = openAction;
		this.exportAction = exportAction;
		filter = state.filter;
		numFiles = -1;

		// Set properties
		initOwner(owner);
		setTitle(PathUtils.absString(directory));

		// Make window invisible until it is displayed
		setOpacity(0.0);

		// Button: show controls
		ImageButton showControlsButton = Images.imageButton(Images.ImageId.ARROWHEAD_DOWN, SHOW_CONTROLS_STR);
		showControlsButton.setOnAction(event ->
		{
			// Disable 'show controls' button
			showControlsButton.setDisable(true);

			// Create control dialog
			controlDialog = new ControlDialog();

			// Enable 'show controls' button when control dialog is closed
			controlDialog.setOnHiding(event0 -> showControlsButton.setDisable(false));

			// Show control dialog
			Bounds bounds = buttonPane.localToScreen(buttonPane.getLayoutBounds());
			controlDialog.setX(bounds.getMinX() + 4.0);
			controlDialog.setY(bounds.getMaxY());
			controlDialog.show();
		});

		// Button: choose directory
		ImageButton chooseDirectoryButton = Images.imageButton(Images.ImageId.DIRECTORY, CHOOSE_DIRECTORY1_STR);
		chooseDirectoryButton.setOnAction(event -> onChooseDirectory());

		// Button: refresh
		ImageButton refreshButton = Images.imageButton(Images.ImageId.REFRESH, REFRESH_FILES_STR);
		refreshButton.setOnAction(event -> readFiles());

		// Create left button pane
		HBox leftButtonPane = new HBox(2.0, showControlsButton, chooseDirectoryButton, refreshButton);
		leftButtonPane.setAlignment(Pos.CENTER_LEFT);

		// Label: number selected
		numSelectedLabel = Labels.hNoShrink();
		numSelectedLabel.setMaxWidth(Double.MAX_VALUE);
		numSelectedLabel.setAlignment(Pos.CENTER_RIGHT);
		numSelectedLabel.setPadding(NUM_SELECTED_LABEL_PADDING);
		numSelectedLabel.setTextFill(getColour(ColourKey.NUM_SELECTED_LABEL_TEXT));
		numSelectedLabel.setBackground(SceneUtils
				.createColouredBackground(getColour(ColourKey.NUM_SELECTED_LABEL_BACKGROUND)));
		numSelectedLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NUM_SELECTED_LABEL_BORDER)));
		numSelectedLabel.getStyleClass().add(StyleClass.NUM_SELECTED_LABEL);
		HBox.setMargin(numSelectedLabel, new Insets(0.0, 4.0, 0.0, 4.0));

		// Button: select all
		ImageButton selectAllButton = Images.imageButton(Images.ImageId.SELECT_ALL, SELECT_ALL_STR);
		selectAllButton.setOnAction(event ->
		{
			selectAll(true);
			updateNumSelected();
		});

		// Button: deselect all
		ImageButton deselectAllButton = Images.imageButton(Images.ImageId.DESELECT_ALL, DESELECT_ALL_STR);
		deselectAllButton.setOnAction(event ->
		{
			selectAll(false);
			updateNumSelected();
		});

		// Create right button pane
		HBox rightButtonPane =
				new HBox(2.0, Labels.hNoShrink(NUM_SELECTED_STR), numSelectedLabel, selectAllButton, deselectAllButton);
		rightButtonPane.setAlignment(Pos.CENTER_RIGHT);

		// Create button pane
		buttonPane = new HBox(leftButtonPane, FillerUtils.hBoxFiller(20.0), rightButtonPane);
		buttonPane.setAlignment(Pos.CENTER);
		buttonPane.setPadding(BUTTON_PANE_PADDING);

		// Label: placeholder
		placeholderLabel = Labels.expansive(NO_MATCHING_FILES_STR);
		placeholderLabel.setFont(FontUtils.defaultFont(PLACEHOLDER_LABEL_FONT_SIZE_FACTOR));
		placeholderLabel.getStyleClass().add(StyleClass.PLACEHOLDER_LABEL);

		// Create multi-puzzle pane
		multiPuzzlePane = new MultiPuzzlePane(state.numColumns);

		// Create scroll pane
		scrollPane = new ScrollPane(multiPuzzlePane);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVisible(false);

		// Give multi-puzzle pane access to scroll pane
		multiPuzzlePane.scrollPane = scrollPane;

		// Redraw multi-puzzle pane when viewport bounds change
		scrollPane.viewportBoundsProperty().addListener(observable ->
		{
			multiPuzzlePane.layout();
			multiPuzzlePane.needsLayout();
		});

		// Redraw multi-puzzle pane when it is scrolled vertically
		scrollPane.vvalueProperty().addListener(observable -> multiPuzzlePane.requestLayout());

		// If scroll wheel is scrolled with Control down, increment/decrement number of columns
		scrollPane.addEventFilter(ScrollEvent.SCROLL, event ->
		{
			if (event.isControlDown())
			{
				double delta = event.getDeltaY();
				if (delta != 0.0)
				{
					if (delta < 0.0)
						multiPuzzlePane.incrementNumColumns(-1);
					else
						multiPuzzlePane.incrementNumColumns(1);
				}
				event.consume();
			}
		});

		// Handle 'key pressed' events
		scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			// Ignore key press if modifier key was also pressed
			if (event.isShiftDown() || event.isControlDown() || event.isAltDown())
				return;

			// Assume key event was handled
			boolean handled = true;

			// Handle block scrolling keys
			ScrollBar sb = multiPuzzlePane.vScrollBar();
			switch (event.getCode())
			{
				case PAGE_UP   -> sb.setValue(Math.max(sb.getMin(), sb.getValue() - sb.getBlockIncrement()));
				case PAGE_DOWN -> sb.setValue(Math.min(sb.getValue() + sb.getBlockIncrement(), sb.getMax()));
				default        -> handled = false;
			}

			// If key event was handled, consume it
			if (handled)
				event.consume();
		});

		// Initialise 'number selected' label
		updateNumSelected();

		// Create content pane
		StackPane contentPane = new StackPane(placeholderLabel, scrollPane);
		VBox.setVgrow(contentPane, Priority.ALWAYS);

		// Create main pane
		VBox mainPane = new VBox(buttonPane, contentPane);

		// Set scene
		setScene(new Scene(mainPane));
		sizeToScene();

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.MULTI_PUZZLE_WINDOW_ROOT);

		// Add style sheet to scene
		StyleManager.INSTANCE.addStyleSheet(getScene());

		// Add key combinations to scene
		getScene().getAccelerators().put(KEY_COMBO_SHOW_CONTROLS,    showControlsButton::fire);
		getScene().getAccelerators().put(KEY_COMBO_CHOOSE_DIRECTORY, chooseDirectoryButton::fire);
		getScene().getAccelerators().put(KEY_COMBO_REFRESH,          refreshButton::fire);
		getScene().getAccelerators().put(KEY_COMBO_INC_NUM_COLUMNS,  () -> multiPuzzlePane.incrementNumColumns(1));
		getScene().getAccelerators().put(KEY_COMBO_DEC_NUM_COLUMNS,  () -> multiPuzzlePane.incrementNumColumns(-1));

		// When window is shown, set its size and location after a delay
		addEventHandler(WindowEvent.WINDOW_SHOWN, event ->
		{
			// Get dimensions of window
			WindowDims dims = new WindowDims(this);

			// Set size of window after a delay
			ExecUtils.afterDelay(getDelay(SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_SIZE), () ->
			{
				// Update dimensions
				dims.update(false);

				// Temporarily set minimum dimensions to prevent window from shrinking (Linux/GNOME)
				dims.setMin(MIN_WIDTH, MIN_HEIGHT);

				// Get size of window from saved state
				Dimension2D size = state.getSize();

				// Set width of window
				double width = (size == null) ? DEFAULT_WIDTH : size.getWidth();
				setMinWidth(width);
				setWidth(width);

				// Set height of window
				double height = (size == null) ? DEFAULT_HEIGHT : size.getHeight();
				setMinHeight(height);
				setHeight(height);

				// Set location of window after a delay
				ExecUtils.afterDelay(getDelay(SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_LOCATION), () ->
				{
					// Get location of window from saved state
					Point2D location = state.getLocation();

					// Invalidate location if top centre of window is not within a screen
					if ((location != null) && !SceneUtils.isWithinScreen(location.getX() + 0.5 * getWidth(),
																		 location.getY(), SCREEN_MARGINS))
						location = null;

					// If there is no location, locate window relative to owner
					if (location == null)
					{
						location = SceneUtils.getRelativeLocation(getWidth(), getHeight(), owner.getX(), owner.getY(),
																  owner.getWidth(), owner.getHeight());
					}

					// Set location of window
					setX(location.getX());
					setY(location.getY());

					// Perform remaining initialisation after a delay
					ExecUtils.afterDelay(getDelay(SystemPropertyKey.DIR_BROWSER_WINDOW_DELAY_OPACITY), () ->
					{
						// Set minimum dimensions of window
						setMinWidth(MIN_WIDTH);
						setMinHeight(MIN_HEIGHT);

						// Make window visible
						setOpacity(1.0);

						// Read puzzles
						readFiles();
					});
				});
			});
		});

		// Save state of window when window is closed
		addEventHandler(WindowEvent.WINDOW_HIDING, event ->
		{
			state.restoreAndUpdate(this, true);
			state.directory = directory;
			state.numColumns = multiPuzzlePane.numColumns;
			state.filter = filter;
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

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

	public static Path chooseDirectory(
		Window	owner,
		Path	initialDirectory)
	{
		// Initialise directory chooser
		if (directoryChooser == null)
		{
			directoryChooser = LocationChooser.forDirectories();
			directoryChooser.setDialogTitle(CHOOSE_DIRECTORY2_STR);
			directoryChooser.initDirectory(DEFAULT_DIRECTORY);
		}
		directoryChooser.setDialogStateKey(owner.getClass().getCanonicalName());

		// If no initial directory was specified, use directory from saved state
		if (initialDirectory == null)
			initialDirectory = state.directory;

		// Set initial directory and filename of directory chooser
		if (initialDirectory != null)
		{
			directoryChooser.initDirectoryWithParent(initialDirectory);
			directoryChooser.setInitialFilename(initialDirectory.getFileName().toString());
		}

		// Display directory chooser and return chosen directory
		return directoryChooser.showSelectDialog(owner);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the delay (in milliseconds) that is defined the system property with the specified key.
	 *
	 * @param  key
	 *           the key of the system property.
	 * @return the delay (in milliseconds) that is defined the system property whose key is {@code key}, or a default
	 *         value if there is no such property or the property value is not a valid integer.
	 */

	private static int getDelay(
		String	key)
	{
		int delay = DIR_BROWSER_WINDOW_DELAYS.get(key);
		String value = System.getProperty(key);
		if (value != null)
		{
			try
			{
				delay = Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		return delay;
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
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Filter filter()
	{
		return filter;
	}

	//------------------------------------------------------------------

	private void readFiles()
	{
		// Remove all children of multi-puzzle pane
		multiPuzzlePane.getChildren().clear();

		// Update 'number selected' label
		updateNumSelected();

		// Show placeholder
		scrollPane.setVisible(false);
		placeholderLabel.setVisible(true);

		// Create container for local variables
		class Vars
		{
			List<Path> files;
		}
		Vars vars = new Vars();

		// Create sorted list of files in directory
		try
		{
			vars.files = DirectoryUtils.listFiles(directory, file ->
					PathnameUtils.suffixMatches(file, FileKind.ALL_FILENAME_EXTENSIONS));
			vars.files.sort(null);
		}
		catch (FileException e)
		{
			ErrorDialog.show(this, READ_FILES_STR, e);
			return;
		}

		// Create task to read puzzles
		Window window = this;
		int numFiles = vars.files.size();
		Task<List<Puzzle.FileInfo>> task = new Task<>()
		{
			{
				updateTitle(READ_FILES_STR);
				updateProgress(0, numFiles);
			}

			@Override
			protected List<Puzzle.FileInfo> call()
				throws Exception
			{
				List<Puzzle.FileInfo> puzzles = new ArrayList<>();
				for (int i = 0; i < numFiles; i++)
				{
					// Get next file
					Path file = vars.files.get(i);

					// Infer kind of file from filename extension
					FileKind fileKind = FileKind.forLocation(file);

					// Read puzzle file
					if (fileKind != null)
					{
						// Update task status
						updateMessage(READING_STR + MessageConstants.SPACE_SEPARATOR + PathUtils.abs(file));

						// Read file
						Puzzle.FileInfo fileInfo = switch (fileKind)
						{
							case PUZZLE, TEMPLATE -> Puzzle.fromJson(file, fileKind);
							case TEXT             -> Puzzle.fromText(file);
						};

						// If puzzle is accepted by filter, add it to list
						if (fileInfo.template() ? filter.acceptsTemplates() : filter.acceptsPuzzles())
						{
							fileInfo.puzzle().editable(false);
							puzzles.add(fileInfo);
						}
					}

					// Update progress
					updateProgress(i + 1, numFiles);
				}
				return puzzles;
			}

			@Override
			protected void succeeded()
			{
				// Get list of puzzles
				List<Puzzle.FileInfo> fileInfos = getValue();
				if (fileInfos.isEmpty())
					return;

				// Create document for each puzzle
				List<PuzzleDocument> documents = new ArrayList<>();
				for (Puzzle.FileInfo fileInfo : fileInfos)
				{
					Path file = fileInfo.file();
					try
					{
						documents.add(new PuzzleDocument(fileInfo.puzzle(), file, FileKind.forLocation(file)));
					}
					catch (FileException e)
					{
						ErrorDialog.show(window, getTitle(), e);
					}
				}

				// Create panes for puzzles
				for (PuzzleDocument document : documents)
					multiPuzzlePane.getChildren().add(new FilePane(document));

				// Lay out multi-puzzle pane
				multiPuzzlePane.layout();

				// Update 'number selected' label
				updateNumSelected();

				// Make scroll pane visible
				scrollPane.setVisible(true);
				placeholderLabel.setVisible(false);

				// Request focus on scroll pane
				scrollPane.requestFocus();
			}

			@Override
			protected void failed()
			{
				ErrorDialog.show(window, getTitle(), getException());
			}
		};

		// Create progress dialog for task
		new SimpleProgressDialog(this, task);

		// Execute task on background thread
		SudokuGeneratorApp.instance().executeTask(task);
	}

	//------------------------------------------------------------------

	private void selectAll(
		boolean	selected)
	{
		for (Node child : multiPuzzlePane.getChildren())
		{
			if (child instanceof FilePane filePane)
				filePane.selected(selected);
		}
	}

	//------------------------------------------------------------------

	private void updateNumSelected()
	{
		int numFiles = multiPuzzlePane.getChildren().size();
		if (this.numFiles != numFiles)
		{
			// Update instance variable
			this.numFiles = numFiles;

			// Update preferred width of label
			String prototypeText =
					"0".repeat(NumberUtils.getNumDecDigitsInt(numFiles)) + NUM_SELECTED_SEPARATOR + numFiles;
			double textWidth = TextUtils.textWidth(numSelectedLabel.getFont(), prototypeText);
			Insets insets = numSelectedLabel.getInsets();
			numSelectedLabel.setPrefWidth(Math.ceil(textWidth + insets.getLeft() + insets.getRight()));
		}

		// Update text of 'number selected' label
		numSelectedLabel.setText(multiPuzzlePane.numSelectedFiles() + NUM_SELECTED_SEPARATOR + numFiles);
	}

	//------------------------------------------------------------------

	private void onChooseDirectory()
	{
		Path directory = chooseDirectory(this, this.directory);
		if (directory != null)
		{
			this.directory = directory;
			setTitle(PathUtils.absString(directory));
			readFiles();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: FILTER


	public enum Filter
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PUZZLES
		(
			"Puzzles"
		),

		TEMPLATES
		(
			"Templates"
		),

		PUZZLES_AND_TEMPLATES
		(
			"Puzzles and templates"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Filter(
			String	text)
		{
			// Initialise instance variables
			key = StringUtils.toCamelCase(name());
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public String key()
		{
			return key;
		}

		//--------------------------------------------------------------

		public boolean acceptsPuzzles()
		{
			return (this == PUZZLES) || (this == PUZZLES_AND_TEMPLATES);
		}

		//--------------------------------------------------------------

		public boolean acceptsTemplates()
		{
			return (this == TEMPLATES) || (this == PUZZLES_AND_TEMPLATES);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: WINDOW STATE


	private static class State
		extends WindowState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Filter	DEFAULT_FILTER	= Filter.PUZZLES;

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	DIRECTORY	= "directory";
			String	FILTER		= "filter";
			String	NUM_COLUMNS	= "numColumns";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Path	directory;
		private	int		numColumns;
		private	Filter	filter;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			numColumns = DEFAULT_NUM_COLUMNS;
			filter = DEFAULT_FILTER;
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

			// Encode directory
			if (directory != null)
				rootNode.addString(PropertyKey.DIRECTORY, PathUtils.absStringStd(directory));

			// Encode number of columns
			rootNode.addInt(PropertyKey.NUM_COLUMNS, numColumns);

			// Encode filter
			rootNode.addString(PropertyKey.FILTER, filter.key);

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

			// Decode directory
			String key = PropertyKey.DIRECTORY;
			if (rootNode.hasString(key))
				directory = Path.of(rootNode.getString(key));

			// Decode number of columns
			numColumns = rootNode.getInt(PropertyKey.NUM_COLUMNS, DEFAULT_NUM_COLUMNS);

			// Decode filter
			filter = rootNode.getEnumValue(Filter.class, PropertyKey.FILTER, Filter::key, DEFAULT_FILTER);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: MULTIPLE-PUZZLE PANE


	private static class MultiPuzzlePane
		extends Pane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	MARGIN	= 6.0;
		private static final	double	GAP	= 6.0;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int			numColumns;
		private	ScrollPane	scrollPane;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MultiPuzzlePane(
			int	numColumns)
		{
			// Initialise instance variables
			this.numColumns = numColumns;

			// Set properties
			setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static double labelHeight()
		{
			return TextUtils.textHeightCeil() + NAME_LABEL_PADDING.getTop() + NAME_LABEL_PADDING.getBottom() + 2.0;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Orientation getContentBias()
		{
			return Orientation.HORIZONTAL;
		}

		//--------------------------------------------------------------

		@Override
		protected double computePrefWidth(
			double	height)
		{
			return scrollPane.getViewportBounds().getWidth();
		}

		//--------------------------------------------------------------

		@Override
		protected double computePrefHeight(
			double	width)
		{
			// Get number of children
			int numChildren = getChildren().size();

			// If there are no children or the width is not positive, the height is the vertical margins
			if ((numChildren == 0) || (width <= 0.0))
				return 2.0 * MARGIN;

			// Calculate width of a file pane
			double paneWidth = Math.floor((width - 2.0 * MARGIN - (double)(numColumns - 1) * GAP) / (double)numColumns);
			paneWidth = Math.max(0.0, paneWidth);

			// Calculate number of rows
			int numRows = (numChildren + numColumns - 1) / numColumns;

			// Calculate height of this pane and return it
			return 2.0 * MARGIN + (double)numRows * (labelHeight() + FilePane.GAP + paneWidth)
					+ (double)(numRows - 1) * GAP;
		}

		//--------------------------------------------------------------

		@Override
		protected void layoutChildren()
		{
			// Get number of children
			int numChildren = getChildren().size();

			// If there are no children, there's nothing to do
			if (numChildren == 0)
				return;

			// Get width of are in which children should be laid out
			double width = getWidth();
			Bounds viewportBounds = scrollPane.getViewportBounds();
			width = Math.min(width, viewportBounds.getWidth());

			// Calculate width of a file pane
			double paneWidth = Math.floor((width - 2.0 * MARGIN - (double)(numColumns - 1) * GAP) / (double)numColumns);
			paneWidth = Math.max(0.0, paneWidth);

			// Calculate height of a file pane
			double paneHeight = labelHeight() + FilePane.GAP + paneWidth;

			// Calculate x coordinate of first column of file panes
			double x0 = Math.floor(0.5 * (width - (double)numColumns * paneWidth - (double)(numColumns - 1) * GAP));
			x0 = Math.max(0.0, x0);

			// Get upper and lower y coordinates of viewport
			double viewportY1 = -viewportBounds.getMinY();
			double viewportY2 = viewportY1 + viewportBounds.getHeight();

			// Set location and size of each file pane
			int index = 0;
			double y = MARGIN;
			int numRows = (numChildren + numColumns - 1) / numColumns;
			for (int i = 0; i < numRows; i++)
			{
				double x = x0;
				for (int j = 0; j < numColumns; j++)
				{
					if ((index < numChildren) && (getChildren().get(index++) instanceof FilePane filePane))
					{
						filePane.updateContent((y < viewportY2) && (y + paneHeight > viewportY1));
						filePane.resize(paneWidth, paneHeight);
						filePane.relocate(x, y);
					}
					x += paneWidth + GAP;
				}
				y += paneHeight + GAP;
			}

			// Set unit and block increments of vertical scroll bar
			ScrollBar scrollBar = vScrollBar();
			double rows = (double)numRows - viewportBounds.getHeight() / (paneHeight + GAP);
			double blockIncrement = (rows > 0.0) ? 1.0 / rows : 0.0;
			scrollBar.setUnitIncrement(0.125 * blockIncrement);
			scrollBar.setBlockIncrement(blockIncrement);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private ScrollBar vScrollBar()
		{
			return ScrollPaneUtils.getVScrollBar(scrollPane);
		}

		//--------------------------------------------------------------

		private void incrementNumColumns(
			int	increment)
		{
			int numColumns = this.numColumns + increment;
			if ((numColumns >= MIN_NUM_COLUMNS) && (numColumns <= MAX_NUM_COLUMNS))
				numColumns(numColumns);
		}

		//--------------------------------------------------------------

		private void numColumns(
			int	numColumns)
		{
			this.numColumns = numColumns;
			requestLayout();
		}

		//--------------------------------------------------------------

		private int numSelectedFiles()
		{
			int count = 0;
			List<Node> children = getChildren();
			int numFiles = children.size();
			for (int i = 0; i < numFiles; i++)
			{
				if ((children.get(i) instanceof FilePane filePane) && filePane.selected)
					++count;
			}
			return count;
		}

		//--------------------------------------------------------------

		private List<PuzzleDocument> selectedDocuments()
		{
			return getChildren().stream()
					.filter(child -> (child instanceof FilePane filePane) && filePane.selected)
					.map(child -> ((FilePane)child).document)
					.toList();
		}

		//--------------------------------------------------------------

		private void needsLayout()
		{
			setNeedsLayout(true);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: FILE PANE


	private class FilePane
		extends VBox
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	GAP	= 1.0;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PuzzleDocument	document;
		private	boolean			selected;
		private	OverlayLabel	nameLabel;
		private	StackPane		contentPane;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FilePane(
			PuzzleDocument	document)
		{
			// Initialise instance variables
			this.document = document;

			// Set properties
			setSpacing(GAP);
			setAlignment(Pos.TOP_CENTER);
			getStyleClass().add(StyleClass.FILE_PANE);

			// Create name label
			nameLabel = new OverlayLabel.Primary(document.displayName());
			nameLabel.setMaxWidth(Double.MAX_VALUE);
			nameLabel.setPadding(NAME_LABEL_PADDING);
			nameLabel.setTextFill(getColour(ColourKey.NAME_LABEL_TEXT));
			nameLabel.setBackground(SceneUtils
					.createColouredBackground(getColour(ColourKey.NAME_LABEL_BACKGROUND)));
			nameLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.NAME_LABEL_BORDER)));
			nameLabel.setPopUpPadding(NAME_LABEL_PADDING);
			nameLabel.getStyleClass().add(StyleClass.NAME_LABEL);

			// If mouse is double-clicked on name label, select file or toggle its 'selected' state
			nameLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if (event.getButton() == MouseButton.PRIMARY)
				{
					if (event.isControlDown())
						selected(!selected);
					else
					{
						selectAll(false);
						selected(true);
					}
				}
			});

			// Create content pane
			contentPane = new StackPane();
			setVgrow(contentPane, Priority.ALWAYS);

			// Add children to this pane
			getChildren().addAll(nameLabel, contentPane);

			// If mouse is double-clicked on this pane, open associated file
			addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 2) && (openAction != null))
					openAction.invoke(List.of(document));
			});

			// Display context menu in response to request
			setOnContextMenuRequested(event ->
			{
				// Get documents of selected files
				List<PuzzleDocument> documents = multiPuzzlePane.selectedDocuments();

				// Create context menu
				ContextMenu menu = new ContextMenu();

				// Menu item: select/deselect file
				MenuItem menuItem = new MenuItem(selected ? DESELECT_FILE_STR : SELECT_FILE_STR);
				menuItem.setOnAction(event0 -> selected(!selected));
				menu.getItems().add(menuItem);

				// Separator
				menu.getItems().add(new SeparatorMenuItem());

				// Menu item: open file
				menuItem = new MenuItem(OPEN_FILE_STR);
				menuItem.setDisable(openAction == null);
				menuItem.setOnAction(event0 ->
				{
					if (openAction != null)
						openAction.invoke(List.of(document));
				});
				menu.getItems().add(menuItem);

				// Menu item: open selected files
				menuItem = new MenuItem(OPEN_SELECTED_STR);
				menuItem.setDisable((openAction == null) || documents.isEmpty());
				menuItem.setOnAction(event0 ->
				{
					if (openAction != null)
						openAction.invoke(documents);
				});
				menu.getItems().add(menuItem);

				// Separator
				menu.getItems().add(new SeparatorMenuItem());

				// Menu item: export selected files
				menuItem = new MenuItem(EXPORT_SELECTED_STR + ELLIPSIS_STR);
				menuItem.setDisable((exportAction == null) || documents.isEmpty());
				menuItem.setOnAction(event0 ->
				{
					if (exportAction != null)
						exportAction.invoke(documents.stream().map(doc -> doc.puzzle()).toList());
				});
				menu.getItems().add(menuItem);

				// Display context menu at location of event
				menu.show(DirectoryBrowserWindow.this, event.getScreenX(), event.getScreenY());
			});
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void selected(
			boolean	selected)
		{
			// Update instance variable
			this.selected = selected;

			// Update pseudo-class state
			pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);

			// Update background colour of name label
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				nameLabel.setBackground(SceneUtils.createColouredBackground(getColour(selected
						? ColourKey.NAME_LABEL_BACKGROUND_SELECTED
						: ColourKey.NAME_LABEL_BACKGROUND)));
			}

			// Update 'number selected' label
			updateNumSelected();
		}

		//--------------------------------------------------------------

		private void updateContent(
			boolean	hasPuzzlePane)
		{
			List<Node> children = contentPane.getChildren();
			if (hasPuzzlePane)
			{
				if (children.isEmpty())
					children.add(new PuzzlePane(document));
			}
			else
				children.clear();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: CONTROL DIALOG


	private class ControlDialog
		extends Stage
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The logical size of the <i>cross</i> icon. */
		private static final	double	CROSS_ICON_SIZE	= 0.85 * TextUtils.textHeight();

		/** The padding around the <i>close</i> lacbel. */
		private static final	Insets	CLOSE_LABEL_PADDING	= new Insets(5.0);

		/** The delay (in milliseconds) before making the window visible by restoring its opacity. */
		private static final	int		WINDOW_VISIBLE_DELAY	= 50;

		/** Miscellaneous strings. */
		private static final	String	NUM_COLUMNS_STR	= "Number of columns";
		private static final	String	FILTER_STR		= "Filter";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ControlDialog()
		{
			// Call superclass constructor
			super(StageStyle.TRANSPARENT);

			// Set properties
			initModality(Modality.APPLICATION_MODAL);
			initOwner(DirectoryBrowserWindow.this);
			setResizable(false);

			// Make window invisible until it is displayed
			setOpacity(0.0);

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

			// Spinner: number of columns
			Spinner<Integer> numColumnsSpinner =
					SpinnerFactory.integerSpinner(MIN_NUM_COLUMNS, MAX_NUM_COLUMNS, multiPuzzlePane.numColumns,
												  NumberUtils.getNumDecDigitsInt(MAX_NUM_COLUMNS));
			numColumnsSpinner.valueProperty().addListener((observable, oldNumColumns, numColumns) ->
					multiPuzzlePane.numColumns(numColumns));
			controlPane.addRow(row++, new Label(NUM_COLUMNS_STR), numColumnsSpinner);

			// Spinner: filter
			CollectionSpinner<Filter> filterSpinner =
					CollectionSpinner.leftRightH(HPos.CENTER, true, Filter.class, filter, null, null);
			filterSpinner.itemProperty().addListener((observable, oldFilter, newFilter) ->
			{
				filter = newFilter;
				readFiles();
			});
			controlPane.addRow(row++, new Label(FILTER_STR), filterSpinner);

			// Create icon for 'close dialog' label
			Shape crossIcon = Shapes.cross01(CROSS_ICON_SIZE);
			crossIcon.setStroke(getColour(ColourKey.CONTROL_DIALOG_CROSS_ICON));
			crossIcon.getStyleClass().add(StyleClass.CROSS_ICON);

			// Label: close dialog
			Label closeLabel = new Label(null, Shapes.tile(crossIcon));
			closeLabel.setMaxHeight(Double.MAX_VALUE);
			closeLabel.setPadding(CLOSE_LABEL_PADDING);
			closeLabel.setBackground(SceneUtils
					.createColouredBackground(getColour(ColourKey.CONTROL_DIALOG_CLOSE_LABEL_BACKGROUND)));
			closeLabel.setBorder(SceneUtils
					.createSolidBorder(getColour(ColourKey.CONTROL_DIALOG_CLOSE_LABEL_BORDER), Side.LEFT));
			closeLabel.getStyleClass().add(StyleClass.CLOSE_LABEL);
			closeLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if (event.getButton() == MouseButton.PRIMARY)
					hide();
			});

			// Create main pane
			HBox mainPane = new HBox(controlPane, closeLabel);
			mainPane.setAlignment(Pos.CENTER);
			mainPane.setBackground(SceneUtils
					.createColouredBackground(getColour(ColourKey.CONTROL_DIALOG_MAIN_PANE_BACKGROUND)));
			mainPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.CONTROL_DIALOG_MAIN_PANE_BORDER)));
			mainPane.getStyleClass().add(StyleClass.CONTROL_DIALOG_ROOT);

			// Create scene
			Scene scene = new Scene(mainPane);

			// Add style sheet to scene
			StyleManager.INSTANCE.addStyleSheet(scene);

			// Set scene on this window
			setScene(scene);
			sizeToScene();

			// Close dialog if Escape is pressed
			addEventFilter(KeyEvent.KEY_PRESSED, event ->
			{
				if (event.getCode() == KeyCode.ESCAPE)
				{
					hide();
					event.consume();
				}
			});

			// Update UI after window is displayed
			addEventHandler(WindowEvent.WINDOW_SHOWN, event ->
					ExecUtils.afterDelay(WINDOW_VISIBLE_DELAY, () -> setOpacity(1.0)));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
