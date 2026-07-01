/*====================================================================*\

SudokuGeneratorApp.java

Class: application for generating sudoku puzzles.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.nio.file.attribute.FileTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Predicate;

import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.collections.FXCollections;

import javafx.concurrent.Task;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Group;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;

import javafx.scene.image.WritableImage;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Shape;

import javafx.scene.text.Font;

import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.NodeMessage;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.config.AppAuxDirectory;
import uk.blankaspect.common.config.AppConfig;
import uk.blankaspect.common.config.PortNumber;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.FileSystemUtils;
import uk.blankaspect.common.filesystem.PathnameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IFunction2;
import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure1;
import uk.blankaspect.common.function.IProcedure2;

import uk.blankaspect.common.geometry.VHDirection;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.message.MessageConstants;

import uk.blankaspect.common.misc.DataTxChannel;
import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.thread.DaemonFactory;

import uk.blankaspect.ui.jfx.button.Buttons;
import uk.blankaspect.ui.jfx.button.GraphicButton;
import uk.blankaspect.ui.jfx.button.ImageDataButton;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.container.PaneStyle;

import uk.blankaspect.ui.jfx.dialog.ButtonInfo;
import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleProgressDialog;
import uk.blankaspect.ui.jfx.dialog.TextOutputTaskDialog;

import uk.blankaspect.ui.jfx.exec.ExecUtils;

import uk.blankaspect.ui.jfx.icon.Icons;

import uk.blankaspect.ui.jfx.image.ImageData;
import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.io.IOUtils;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.platform.windows.FileAssociationDialog;

import uk.blankaspect.ui.jfx.popup.CopyPopUp;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.toolbar.ChangeIndicatorTitleBar;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

import uk.blankaspect.ui.jfx.window.WindowDims;
import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: APPLICATION FOR GENERATING SUDOKU PUZZLES


public class SudokuGeneratorApp
	extends Application
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The short name of the application. */
	public static final		String	SHORT_NAME	= "SudokuGenerator";

	/** The long name of the application. */
	private static final	String	LONG_NAME	= "Sudoku generator";

	/** The name of the application when used as a key. */
	private static final	String	NAME_KEY	= StringUtils.firstCharToLowerCase(SHORT_NAME);

	/** The name of the file that contains the build properties of the application. */
	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	/** The filename of the CSS style sheet. */
	private static final	String	STYLE_SHEET_FILENAME	= NAME_KEY + "-%02d.css";

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= SystemUtils.userHomeDirectory();

	/** The interval (in milliseconds) between successive periodic file tasks. */
	private static final	int		PERIODIC_FILE_TASK_INTERVAL	= 500;

	/** The suffix of the name of a thread on which a periodic file task is performed. */
	private static final	String	PERIODIC_FILE_TASK_THREAD_NAME_SUFFIX	= "periodicFileTask";

	/** A map from system-property keys to the default values of the corresponding delays (in milliseconds) in the
		<i>WINDOW_SHOWN</i> event handler of the main window. */
	private static final	Map<String, Integer>	MAIN_WINDOW_DELAYS	= Map.of
	(
		SystemPropertyKey.MAIN_WINDOW_DELAY_SIZE,     100,
		SystemPropertyKey.MAIN_WINDOW_DELAY_LOCATION,  25,
		SystemPropertyKey.MAIN_WINDOW_DELAY_OPACITY,   25
	);

	/** The minimum width of the main window. */
	private static final	double	MAIN_WINDOW_MIN_WIDTH	= 240.0;

	/** The minimum height of the main window. */
	private static final	double	MAIN_WINDOW_MIN_HEIGHT	= 160.0;

	/** The margins that are applied to the visual bounds of each screen when determining whether the saved location of
		the main window is within a screen. */
	private static final	Insets	SCREEN_MARGINS	= new Insets(0.0, 32.0, 32.0, 0.0);

	/** The prototype text for the puzzle spinner. */
	private static final	String	PUZZLE_SPINNER_PROTOTYPE_TEXT	= "0".repeat(32);

	/** The padding around the puzzle-selection pane. */
	private static final	Insets	PUZZLE_SELECTION_PANE_PADDING	= new Insets(4.0, 6.0, 4.0, 6.0);

	/** The padding around the status pane. */
	private static final	Insets	STATUS_PANE_PADDING	= new Insets(2.0, 8.0, 2.0, 8.0);

	/** The width of the scene. */
	private static final	double	SCENE_WIDTH		= 400.0;

	/** The height of the scene. */
	private static final	double	SCENE_HEIGHT	= 400.0;

	/** The prefix of the name of the directory to which a Windows file-association script is written. */
	private static final	String	FILE_ASSOC_SCRIPT_DIR_NAME_PREFIX	= NAME_KEY + "_";

	/** The filename stem of a Windows file-association script. */
	private static final	String	FILE_ASSOC_SCRIPT_FILENAME_STEM	= NAME_KEY + "Associations";

	/** The first filename extension of an HTML file. */
	private static final	String	HTML_FILENAME_EXTENSION1	= ".html";

	/** The second filename extension of an HTML file. */
	private static final	String	HTML_FILENAME_EXTENSION2	= ".htm";

	/** The filename extension of an XHTML file. */
	private static final	String	XHTML_FILENAME_EXTENSION	= ".xhtml";

	/** The filter for HTML files. */
	private static final	FileMatcher	HTML_FILES_FILTER	=
			FileMatcher.from("HTML files", HTML_FILENAME_EXTENSION1, HTML_FILENAME_EXTENSION2,
							 XHTML_FILENAME_EXTENSION);

	/** The file-system location filter for drag-and-drop actions. */
	private static final	Predicate<Path>	DRAG_AND_DROP_FILTER	= location ->
			Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS)
				&& PathnameUtils.suffixMatches(location, FileKind.ALL_FILENAME_EXTENSIONS);

	/** The key combination that selects the previous puzzle. */
	private static final	KeyCombination	KEY_COMBO_PREVIOUS_PUZZLE	=
			new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN);

	/** The key combination that selects the next puzzle. */
	private static final	KeyCombination	KEY_COMBO_NEXT_PUZZLE	=
			new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	CONFIG_ERROR_STR			= "Configuration error";
	private static final	String	LOCATION_STR				= "Location";
	private static final	String	FILE_STR					= "File";
	private static final	String	EDIT_STR					= "Edit";
	private static final	String	VIEW_STR					= "View";
	private static final	String	PUZZLE_STR					= "Puzzle";
	private static final	String	PLATFORM_STR				= "Platform";
	private static final	String	NO_PUZZLES_STR				= "No puzzles";
	private static final	String	OPEN_PUZZLE_STR				= "Open puzzle";
	private static final	String	OPEN_TEMPLATE_STR			= "Open template";
	private static final	String	OPEN_FILE_STR				= "Open file";
	private static final	String	READ_FILE_STR				= "Read file";
	private static final	String	READING_STR					= "Reading";
	private static final	String	CLOSE_PUZZLE1_STR			= "Close puzzle";
	private static final	String	CLOSE_PUZZLE2_STR			= "close the puzzle";
	private static final	String	CLOSE_STR					= "Close";
	private static final	String	CLOSE_DELETE1_STR			= "Close puzzle and delete file";
	private static final	String	CLOSE_DELETE2_STR			= "close the puzzle and delete the associated file";
	private static final	String	WANT_STR					= "Do you want to %s?";
	private static final	String	SAVE_PUZZLE_STR				= "Save puzzle";
	private static final	String	SAVE_TEMPLATE_STR			= "Save template";
	private static final	String	SAVE_FILE_STR				= "Save file";
	private static final	String	SAVE_AS_PUZZLE_STR			= "Save as puzzle";
	private static final	String	SAVE_AS_TEMPLATE_STR		= "Save as template";
	private static final	String	WRITE_PUZZLE_FILE_STR		= "Write puzzle file";
	public static final		String	SUDOKU_STR					= "Sudoku";
	private static final	String	EXPORT_HTML_STR				= "Export puzzle as HTML file";
	private static final	String	WRITE_HTML_FILE_STR			= "Write HTML file";
	private static final	String	WRITING_STR					= "Writing";
	private static final	String	FILE_WRITTEN_STR			= "The file was written successfully.";
	private static final	String	HOMOGENISE_TEMPLATE_STR		= "Homogenise template entries";
	private static final	String	HOMOGENISE_PROMPT_STR		=
			"Do you want to homogenise the entries of the template?";
	private static final	String	YES_STR						= "Yes";
	private static final	String	NO_STR						= "No";
	private static final	String	CONFIRM_RELOAD_STR			=
			"Do you want discard the changes to the current puzzle and reload the original file?";
	private static final	String	RELOAD_FILE1_STR			= "Reload file";
	private static final	String	RELOAD_FILE2_STR			= "reload the file";
	private static final	String	RELOAD_STR					= "Reload";
	private static final	String	MODIFIED_FILE_STR			= "Modified file";
	private static final	String	MODIFIED_RELOAD_STR			=
			"The file has been modified externally.\nDo you want to reload the modified file?";
	private static final	String	UNSAVED_CHANGES_STR			= "Unsaved changes";
	private static final	String	UNSAVED_PROMPT_STR			= """
		The puzzle has changes that have not been saved.
		If you proceed, the changes will be lost.
		Do you want to %s?""";
	private static final	String	IMAGE_COPIED_STR			=
			"An image of %d\u00D7%d pixels was copied to the clipboard.";
	private static final	String	WINDOWS_STR					= "Windows";
	private static final	String	FILE_ASSOCIATION_STR		= "File association";
	private static final	String	FA_SCRIPT_OP_SUCCEEDED_STR	=
			"The file-association script operation completed successfully.";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.PUZZLE_SELECTION_PANE_BACKGROUND,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(StyleClass.PUZZLE_SELECTION_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.PUZZLE_SELECTION_PANE_BORDER,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(StyleClass.PUZZLE_SELECTION_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.PUZZLE_SELECTION_PANE_CLEAR_ICON_BACKGROUND,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(Icons.StyleClass.CLEAR01_BACKGROUND)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.PUZZLE_SELECTION_PANE_CLEAR_ICON_FOREGROUND,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(Icons.StyleClass.CLEAR01_FOREGROUND)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.STATUS_PANE_BACKGROUND,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(StyleClass.STATUS_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.STATUS_PANE_BORDER,
			CssSelector.builder()
					.id(StyleConstants.NodeId.APP_MAIN_ROOT)
					.desc(StyleClass.STATUS_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.SOLUTION_BUTTON_ICON,
			CssSelector.builder()
					.cls(StyleClass.SOLUTION_INFO_PANE)
					.desc(StyleClass.SOLUTION_BUTTON_ICON)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.SOLUTION_BUTTON_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SOLUTION_INFO_PANE)
					.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.id(StyleConstants.NodeId.APP_MAIN_ROOT)
						.desc(StyleClass.PUZZLE_SELECTION_PANE)
						.build())
				.borders(Side.BOTTOM)
				.build(),
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.id(StyleConstants.NodeId.APP_MAIN_ROOT)
						.desc(StyleClass.STATUS_PANE)
						.build())
				.borders(Side.TOP)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	PUZZLE_SELECTION_PANE	= StyleConstants.CLASS_PREFIX + "puzzle-selection-pane";
		String	SOLUTION_BUTTON_ICON	= StyleConstants.CLASS_PREFIX + "solution-button-icon";
		String	SOLUTION_INFO_PANE		= StyleConstants.CLASS_PREFIX + "solution-info-pane";
		String	STATUS_PANE				= StyleConstants.CLASS_PREFIX + "status-pane";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	PUZZLE_SELECTION_PANE_BACKGROUND			= PREFIX + "puzzleSelectionPane.background";
		String	PUZZLE_SELECTION_PANE_BORDER				= PREFIX + "puzzleSelectionPane.border";
		String	PUZZLE_SELECTION_PANE_CLEAR_ICON_BACKGROUND	= PREFIX + "puzzleSelectionPane.clearIcon.background";
		String	PUZZLE_SELECTION_PANE_CLEAR_ICON_FOREGROUND	= PREFIX + "puzzleSelectionPane.clearIcon.foreground";
		String	SOLUTION_BUTTON_BORDER						= PREFIX + "solutionButton.border";
		String	SOLUTION_BUTTON_ICON						= PREFIX + "solutionButton.icon";
		String	STATUS_PANE_BACKGROUND						= PREFIX + "statusPane.background";
		String	STATUS_PANE_BORDER							= PREFIX + "statusPane.border";
	}

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	APPEARANCE					= "appearance";
		String	CLEAR_ON_SAVE				= "clearOnSave";
		String	COPY_IMAGE_DIALOG			= "copyImageDialog";
		String	COPY_TEXT_DIALOG			= "copyTextDialog";
		String	DIRECTORY_BROWSER_WINDOW	= "directoryBrowserWindow";
		String	EDIT_HISTORY				= "editHistory";
		String	EXPORT_HTML_DIALOG			= "exportHtmlDialog";
		String	EXPORT_HTML_DIRECTORY		= "exportHtmlDirectory";
		String	GENERATION_DIALOG			= "generationDialog";
		String	GRID						= "grid";
		String	GRID_FONT					= "gridFont";
		String	HOMOGENISE_ENTRIES_DIALOG	= "homogeniseEntriesDialog";
		String	IGNORE_CASE_WHEN_TYPING		= "ignoreCaseWhenTyping";
		String	MAIN_WINDOW					= "mainWindow";
		String	MAX_SIZE					= "maxSize";
		String	OPEN_PUZZLE_DIRECTORY		= "openPuzzleDirectory";
		String	OPEN_PUZZLE_FILE_KIND		= "openPuzzleFileKind";
		String	OPEN_TEMPLATE_DIRECTORY		= "openTemplateDirectory";
		String	OPEN_TEMPLATE_FILE_KIND		= "openTemplateFileKind";
		String	PUZZLE						= "puzzle";
		String	SAVE_PUZZLE_DIRECTORY		= "savePuzzleDirectory";
		String	SAVE_PUZZLE_FILE_KIND		= "savePuzzleFileKind";
		String	SAVE_TEMPLATE_DIRECTORY		= "saveTemplateDirectory";
		String	SAVE_TEMPLATE_FILE_KIND		= "saveTemplateFileKind";
		String	SOLUTION_DIALOG				= "solutionDialog";
		String	SYMBOLS						= "symbols";
		String	TEMPLATE_DIALOG				= "templateDialog";
		String	THEME						= "theme";
	}

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	INDEPENDENT_INSTANCE		= "independentInstance";
		String	MAIN_WINDOW_DELAY_LOCATION	= "mainWindowDelay.location";
		String	MAIN_WINDOW_DELAY_OPACITY	= "mainWindowDelay.opacity";
		String	MAIN_WINDOW_DELAY_SIZE		= "mainWindowDelay.size";
		String	OS_NAME						= "os.name";
		String	USE_STYLE_SHEET_FILE		= "useStyleSheetFile";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	ERROR_ACCESSING_FILE =
				"An error occurred when accessing the file.";

		String	FAILED_TO_DELETE_FILE =
				"Failed to delete the file.";

		String	NO_AUXILIARY_DIRECTORY =
				"The location of the auxiliary directory could not be determined.";

		String	FONT_NOT_AVAILABLE =
				"Font: %s\nThe font is not available.";

		String	UNSUPPORTED_FILE_KIND =
				"The file must have one of the following filename extensions:";

		String	NO_TEXT_ON_CLIPBOARD =
				"There is no text on the clipboard.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	/** The single instance of this class. */
	private static	SudokuGeneratorApp	instance;

	/** The index of the last thread that was created for a background task. */
	private static	int					threadIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The properties of the build of this application. */
	private	ResourceProperties				buildProperties;

	/** The string representation of the version of this application. */
	private	String							versionStr;

	/** User preferences. */
	private	Preferences						preferences;

	/** A list of files that are received from other instances of this application. */
	private	List<Path>						receivedFiles;

	/** The state of the main window. */
	private	WindowState						mainWindowState;

	/** A list of puzzle documents and their associated view states. */
	private	List<DocumentInfo>				documentInfos;

	/** The selected document and its associated view state. */
	private	DocumentInfo					selectedDocumentInfo;

	/** The directory that is associated with {@link #openPuzzleFileChooser}. */
	private	Path							openPuzzleDirectory;

	/** The directory that is associated with {@link #openTemplateFileChooser}. */
	private	Path							openTemplateDirectory;

	/** The kind of file that corresponds to the initial filter of {@link #openPuzzleFileChooser}. */
	private	FileKind						openPuzzleFileKind;

	/** The kind of file that corresponds to the initial filter of {@link #openTemplateFileChooser}. */
	private	FileKind						openTemplateFileKind;

	/** The directory that is associated with {@link #savePuzzleFileChooser}. */
	private	Path							savePuzzleDirectory;

	/** The directory that is associated with {@link #saveTemplateFileChooser}. */
	private	Path							saveTemplateDirectory;

	/** The kind of file that corresponds to the initial filter of {@link #savePuzzleFileChooser}. */
	private	FileKind						savePuzzleFileKind;

	/** The kind of file that corresponds to the initial filter of {@link #saveTemplateFileChooser}. */
	private	FileKind						saveTemplateFileKind;

	/** The directory that is associated with {@link #exportHtmlFileChooser}. */
	private	Path							exportHtmlDirectory;

	/** The main window. */
	private	Stage							primaryStage;

	/** The <i>file</i> menu. */
	private	Menu							fileMenu;

	/** The <i>edit</i> menu. */
	private	Menu							editMenu;

	/** The <i>view</i> menu. */
	private	Menu							viewMenu;

	/** The <i>puzzle</i> menu. */
	private	Menu							puzzleMenu;

	/** The <i>platform</i> menu. */
	private	Menu							platformMenu;

	/** The bar in which the pathname of the selected puzzle document is displayed. */
	private	ChangeIndicatorTitleBar			pathnameBar;

	/** The spinner for selecting a puzzle document. */
	private	CollectionSpinner<DocumentInfo>	puzzleSpinner;

	/** The button for closing the selected puzzle. */
	private	GraphicButton					closeButton;

	/** The puzzle view. */
	private	PuzzleView						puzzleView;

	/** The status label. */
	private	Label							statusLabel;

	/** The solution-information pane. */
	private	SolutionInfoPane				solutionInfoPane;

	/** The status pane. */
	private	StackPane						statusPane;

	/** The window in which the puzzle files of a chosen directory are displayed. */
	private	DirectoryBrowserWindow			directoryBrowserWindow;

	/** The file chooser for opening a puzzle file. */
	private	LocationChooser					openPuzzleFileChooser;

	/** The file chooser for opening a template file. */
	private	LocationChooser					openTemplateFileChooser;

	/** The file chooser for saving a puzzle file. */
	private	LocationChooser					savePuzzleFileChooser;

	/** The file chooser for saving a puzzle file. */
	private	LocationChooser					saveTemplateFileChooser;

	/** The file chooser for exporting a puzzle as an HTML file. */
	private	LocationChooser					exportHtmlFileChooser;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SudokuGeneratorApp()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void main(
		String[]	args)
	{
		launch(args);
	}

	//------------------------------------------------------------------

	public static SudokuGeneratorApp instance()
	{
		return instance;
	}

	//------------------------------------------------------------------

	private static String messageToString(
		NodeMessage	message)
	{
		// Create buffer
		StringBuilder buffer = new StringBuilder(64);

		// Append path of node
		String path = NodeMessage.nodeToPathString(message.getNode());
		if (!path.isEmpty())
			buffer.append(LOCATION_STR).append(": ").append(path).append('\n');

		// Append text
		buffer.append(message.getText());

		// Return string
		return buffer.toString();
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
		int delay = MAIN_WINDOW_DELAYS.get(key);
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
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void init()
	{
		instance = this;
	}

	//------------------------------------------------------------------

	@Override
	public void start(
		Stage	primaryStage)
	{
		// Make main window invisible until it is shown
		primaryStage.setOpacity(0.0);

		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					ErrorLogger.INSTANCE.write(exception);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}

		// Initialise instance variables
		preferences = new Preferences();
		receivedFiles = new ArrayList<>();
		mainWindowState = new WindowState(false, true);
		documentInfos = new ArrayList<>();
		openPuzzleFileKind = FileKind.PUZZLE;
		openTemplateFileKind = FileKind.TEMPLATE;
		savePuzzleFileKind = FileKind.PUZZLE;
		saveTemplateFileKind = FileKind.TEMPLATE;
		this.primaryStage = primaryStage;

		// Read build properties
		try
		{
			buildProperties =
					new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Create list of files from command-line arguments
		List<Path> inFiles = new ArrayList<>();
		List<String> args = getParameters().getRaw();
		for (String arg : args)
		{
			Path file = Path.of(PathnameUtils.parsePathname(arg));
			if (Arrays.stream(FileKind.values()).anyMatch(kind -> kind.matches(file)))
				inFiles.add(file);
		}

		// If multiple instances of this application are not allowed, try to transmit list of command-line files to
		// another instance
		if (!Boolean.getBoolean(SystemPropertyKey.INDEPENDENT_INSTANCE))
		{
			// Read TX port number from file
			int txPort = PortNumber.getValue(NAME_KEY);

			// Seek another running instance of this application; if one is found, transmit list of command-line files
			// to it
			if (txPort >= 0)
			{
				String txId = getClass().getSimpleName() + "." + DataTxChannel.getIdSuffix();
				DataTxChannel txChannel = new DataTxChannel(txId);
				List<String> pathnames = inFiles.stream().map(file -> PathUtils.absString(file) + "\n").toList();
				if (txChannel.transmit(txPort, NAME_KEY, pathnames))
					System.exit(0);
			}

			// Open a channel for receiving data from other instances of this application
			DataTxChannel rxChannel = new DataTxChannel(NAME_KEY);
			int rxPort = rxChannel.openReceiver();

			// Listen for lists of pathnames from other instances of this application
			if (rxPort >= 0)
			{
				// Listen for data on RX port
				rxChannel.listen(data ->
				{
					Platform.runLater(() ->
					{
						// Add pathnames to list of received files
						List<String> pathnames = StringUtils.split(data, '\n');
						if (!pathnames.isEmpty())
						{
							receivedFiles.addAll(pathnames.stream()
									.filter(pathname -> !pathname.isEmpty())
									.map(Path::of)
									.toList());
						}
					});
				});

				// On shutdown, invalidate port-number file
				Runtime.getRuntime().addShutdownHook(new Thread(() -> PortNumber.setValue(NAME_KEY, -1)));

				// Write port number to file
				PortNumber.setValue(NAME_KEY, rxPort);
			}
		}

		// Create container for local variables
		class Vars
		{
			Configuration	config;
			BaseException 	configException;
		}
		Vars vars = new Vars();

		// Read configuration file and decode configuration
		NodeMessage.List configErrorMessages = new NodeMessage.List();
		try
		{
			// Initialise configuration
			vars.config = new Configuration();

			// Read and decode configuration
			if (!AppConfig.noConfigFile())
			{
				// Read configuration file
				vars.config.read();

				// Decode configuration
				decodeConfig(vars.config.getConfig(), configErrorMessages);
			}
		}
		catch (BaseException e)
		{
			vars.configException = e;
		}

		// Get style manager
		StyleManager styleManager = StyleManager.INSTANCE;

		// Select theme from system property
		String themeId = System.getProperty(StyleManager.SystemPropertyKey.THEME);
		if (!StringUtils.isNullOrEmpty(themeId))
			styleManager.selectThemeOrDefault(themeId);

		// Set ID and style-sheet filename on style manager
		if (Boolean.getBoolean(SystemPropertyKey.USE_STYLE_SHEET_FILE))
		{
			styleManager.setId(getClass().getSimpleName());
			styleManager.setStyleSheetFilename(STYLE_SHEET_FILENAME);
		}

		// Register the style properties of this class and its dependencies with the style manager
		styleManager.register(getClass(), COLOUR_PROPERTIES, RULE_SETS, PaneStyle.class, SimpleTextListView.class);

		// Create pathname bar
		pathnameBar = new ChangeIndicatorTitleBar();
		VBox.setMargin(pathnameBar, new Insets(-1.0, 0.0, 0.0, 0.0));

		// Spinner: puzzle
		puzzleSpinner = new CollectionSpinner<DocumentInfo>(CollectionSpinner.ButtonPos.LEFT_RIGHT,
															Orientation.HORIZONTAL, HPos.CENTER, true)
				.itemConverter(DocumentInfo::displayName)
				.emptyItemsText(NO_PUZZLES_STR)
				.items(documentInfos, PUZZLE_SPINNER_PROTOTYPE_TEXT);
		puzzleSpinner.itemProperty().addListener((observable, oldDocumentInfo, documentInfo) ->
		{
			if (!Objects.equals(documentInfo, selectedDocumentInfo))
				selectDocument(documentInfo);
		});

		// Button: close puzzle
		Group clearIcon = Icons.clear01(getColour(ColourKey.PUZZLE_SELECTION_PANE_CLEAR_ICON_BACKGROUND),
										getColour(ColourKey.PUZZLE_SELECTION_PANE_CLEAR_ICON_FOREGROUND));
		closeButton = new GraphicButton(clearIcon, CLOSE_PUZZLE1_STR);
		closeButton.setDisable(true);
		closeButton.setOnAction(event -> onClosePuzzle());

		// Create spacer
		Region spacer = new Region();
		spacer.prefWidthProperty().bind(closeButton.widthProperty());

		// Create puzzle-selection pane
		HBox puzzleSelectionPane = new HBox(6.0, spacer, puzzleSpinner, closeButton);
		puzzleSelectionPane.setAlignment(Pos.CENTER);
		puzzleSelectionPane.setPadding(PUZZLE_SELECTION_PANE_PADDING);
		puzzleSelectionPane.setBorder(
				SceneUtils.createSolidBorder(getColour(ColourKey.PUZZLE_SELECTION_PANE_BORDER), Side.BOTTOM));
		puzzleSelectionPane.getStyleClass().add(StyleClass.PUZZLE_SELECTION_PANE);

		// Create puzzle view
		puzzleView = new PuzzleView();
		puzzleView.setVisible(false);

		// Create pane for puzzle view
		StackPane puzzleViewPane = new StackPane(puzzleView);
		puzzleViewPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
		{
			if (puzzleView.isVisible())
				puzzleView.requestFocus();
		});
		VBox.setVgrow(puzzleViewPane, Priority.ALWAYS);

		// Label: status
		statusLabel = new Label("");
		statusLabel.setAlignment(Pos.CENTER_LEFT);

		// Create solution-information pane
		solutionInfoPane = new SolutionInfoPane();
		solutionInfoPane.setVisible(false);

		// Create status pane
		statusPane = new StackPane(statusLabel, solutionInfoPane);
		statusPane.setAlignment(Pos.CENTER_LEFT);
		statusPane.setPadding(STATUS_PANE_PADDING);
		statusPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.STATUS_PANE_BORDER), Side.TOP));
		statusPane.getStyleClass().add(StyleClass.STATUS_PANE);

		// Display context menu for status pane on request
		statusPane.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
		{
			if (statusLabel.isVisible())
			{
				// Get text of status label
				String text = statusLabel.getText();

				// If text is not blank, display 'copy text' pop-up
				if (!StringUtils.isNullOrBlank(text))
				{
					// Create pop-up for 'copy text' action
					CopyPopUp popUp = CopyPopUp.text(primaryStage, () -> text);

					// Display pop-up
					popUp.show(primaryStage, event.getScreenX(), event.getScreenY());
				}
			}
		});

		// Create main pane
		VBox mainPane = new VBox(createMenuBar(), pathnameBar, puzzleSelectionPane, puzzleViewPane, statusPane);
		mainPane.setId(StyleConstants.NodeId.APP_MAIN_ROOT);
		mainPane.setAlignment(Pos.TOP_CENTER);

		// Create scene
		Scene scene = new Scene(mainPane, SCENE_WIDTH, SCENE_HEIGHT);

		// Add style sheet to scene
		styleManager.addStyleSheet(scene);

		// Update images of image buttons when theme changes
		StyleManager.INSTANCE.themeProperty().addListener(observable ->
		{
			ImageData.updateImages();
			ImageDataButton.updateButtons();
		});

		// Handle 'key pressed' events
		scene.addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			if (KEY_COMBO_PREVIOUS_PUZZLE.match(event))
			{
				SudokuGeneratorApp.instance().selectPreviousDocument();
				event.consume();
			}
			else if (KEY_COMBO_NEXT_PUZZLE.match(event))
			{
				SudokuGeneratorApp.instance().selectNextDocument();
				event.consume();
			}
		});

		// Handle 'drag over' events
		scene.setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a file of a supported kind
			if (ClipboardUtils.locationMatches(event.getDragboard(), DRAG_AND_DROP_FILTER))
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Handle 'drag dropped' events
		scene.setOnDragDropped(event ->
		{
			// Get locations of supported kinds of file from dragboard
			List<Path> files = ClipboardUtils.matchingLocations(event.getDragboard(), DRAG_AND_DROP_FILTER);

			// Indicate that drag-and-drop is complete
			event.setDropCompleted(true);

			// Process files
			if (!files.isEmpty())
			{
				Platform.runLater(() ->
				{
					for (Path file : files)
						openFile(file, null);
				});
			}

			// Consume event
			event.consume();
		});

		// Set properties of main window
		primaryStage.setTitle(LONG_NAME + " " + versionStr);
		primaryStage.getIcons().addAll(Images.APP_ICON_IMAGES);

		// Set scene on main window
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();

		// When main window is shown, set its size and location after a delay
		primaryStage.setOnShown(event ->
		{
			// Get dimensions of window
			WindowDims dims = new WindowDims(primaryStage);

			// Set size of main window after a delay
			ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_SIZE), () ->
			{
				// Update dimensions
				dims.update(false);

				// Temporarily set minimum dimensions to prevent window from shrinking (Linux/GNOME)
				dims.setMin(MAIN_WINDOW_MIN_WIDTH, MAIN_WINDOW_MIN_HEIGHT);

				// Get size of window from saved state
				Dimension2D size = mainWindowState.getSize();

				// Set size of window
				if (size != null)
				{
					// Set width
					double width = size.getWidth();
					if (width <= 0.0)
						width = Math.max(MAIN_WINDOW_MIN_WIDTH, dims.w());
					primaryStage.setMinWidth(width);
					primaryStage.setWidth(width);

					// Set height
					double height = size.getHeight();
					if (height <= 0.0)
						height = Math.max(MAIN_WINDOW_MIN_HEIGHT, dims.h());
					primaryStage.setMinHeight(height);
					primaryStage.setHeight(height);
				}

				// Set location of main window after a delay
				ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_LOCATION), () ->
				{
					// Get location of window from saved state
					Point2D location = mainWindowState.getLocation();

					// Invalidate location if top centre of window is not within a screen
					double width = primaryStage.getWidth();
					if ((location != null) && !SceneUtils.isWithinScreen(location.getX() + 0.5 * width, location.getY(),
																		 SCREEN_MARGINS))
						location = null;

					// If there is no location, centre window within primary screen
					if (location == null)
						location = SceneUtils.centreInScreen(width, primaryStage.getHeight());

					// Set location of window
					primaryStage.setX(location.getX());
					primaryStage.setY(location.getY());

					// Perform remaining initialisation after a delay
					ExecUtils.afterDelay(getDelay(SystemPropertyKey.MAIN_WINDOW_DELAY_OPACITY), () ->
					{
						// Set minimum dimensions of window
						primaryStage.setMinWidth(MAIN_WINDOW_MIN_WIDTH);
						primaryStage.setMinHeight(MAIN_WINDOW_MIN_HEIGHT);

						// Make window visible
						primaryStage.setOpacity(1.0);

						// Report any configuration error
						if (vars.configException != null)
							ErrorDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, vars.configException);

						// Display any configuration error messages
						showMessages(configErrorMessages);

						// Perform remaining initialisation
						initialise(inFiles);
					});
				});
			});
		});

		// Test whether window can be closed
		primaryStage.setOnCloseRequest(event ->
		{
			// Choose documents with unsaved changes that will be saved
			List<DocumentInfo> documentsToSave = chooseDocumentsToSave();

			// If selection dialog was cancelled, prevent this application from closing ...
			if (documentsToSave == null)
				event.consume();

			// ... otherwise, save documents
			else
			{
				for (DocumentInfo docInfo : documentsToSave)
				{
					// Select document
					selectDocument(docInfo);

					// Save document
					onSaveFile();
				}
			}
		});

		// Write configuration file when main window is closed
		if (vars.config != null)
		{
			primaryStage.setOnHiding(event ->
			{
				// Close directory-browser window
				if (directoryBrowserWindow != null)
					directoryBrowserWindow.hide();

				// Update state of main window
				mainWindowState.restoreAndUpdate(primaryStage);

				// Write configuration
				if (vars.config.canWrite())
				{
					try
					{
						// Encode configuration
						encodeConfig(vars.config.getConfig());

						// Write configuration file
						vars.config.write();
					}
					catch (FileException e)
					{
						ErrorDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, e);
					}
				}
			});
		}

		// Display main window
		primaryStage.show();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Preferences preferences()
	{
		return preferences;
	}

	//------------------------------------------------------------------

	public void updateUI()
	{
		updateChanged();
		updateMenuItems();
		updateStatus();
	}

	//------------------------------------------------------------------

	public void updateMenuItems()
	{
		// Update menu items
		updateMenuItems(fileMenu.getItems());
		updateMenuItems(editMenu.getItems());
		updateMenuItems(viewMenu.getItems());
		updateMenuItems(puzzleMenu.getItems());
		updateMenuItems(platformMenu.getItems());

		// Update menus
		viewMenu.setDisable(documentInfos.isEmpty());
		puzzleMenu.setDisable(documentInfos.isEmpty());
	}

	//------------------------------------------------------------------

	/**
	 * Enables or disables the specified menu items according to the user data of each item.
	 *
	 * @param menuItems
	 *          the menu items that will be updated.
	 */

	public void updateMenuItems(
		Iterable<? extends MenuItem>	menuItems)
	{
		PuzzleDocument document = selectedDocument();
		boolean noDocument = (document == null);

		for (MenuItem menuItem : menuItems)
		{
			// Get user data of menu item
			Object userData = menuItem.getUserData();

			// Case: submenu
			if (menuItem instanceof Menu submenu)
			{
				if (userData instanceof PuzzleDocument.SubmenuId id)
				{
					if (noDocument)
						submenu.setDisable(true);
					else
						document.updateSubmenu(submenu, id);
				}
				updateMenuItems(submenu.getItems());
			}

			// Case: leaf
			else
			{
				if (userData instanceof Command command)
				{
					menuItem.setDisable(switch (command)
					{
						case BROWSE_DIRECTORY         -> (directoryBrowserWindow != null);
						case CLOSE,
							 COPY_IMAGE,
							 DUPLICATE,
							 EXPORT_HTML,
							 SAVE_AS_PUZZLE,
							 SAVE_AS_TEMPLATE         -> noDocument;
						case CLOSE_ALL                -> documentInfos.isEmpty();
						case CLOSE_AND_DELETE         -> noDocument || (document.file() == null);
						case MANAGE_FILE_ASSOCIATIONS ->
								!System.getProperty(SystemPropertyKey.OS_NAME, "").contains(WINDOWS_STR);
						case RELOAD                   ->
								noDocument || !document.isChanged() || (document.file() == null);
						case SAVE                     -> noDocument || !document.isChanged();
						default                       -> false;
					});
				}
				else if (userData instanceof PuzzleDocument.Command command)
				{
					if (noDocument)
						menuItem.setDisable(true);
					else
						document.updateMenuItem(menuItem, command);
				}
			}
		}
	}

	//------------------------------------------------------------------

	public void selectPreviousDocument()
	{
		int index = documentInfos.indexOf(selectedDocumentInfo);
		if (index > 0)
			selectDocument(documentInfos.get(--index));
	}

	//------------------------------------------------------------------

	public void selectNextDocument()
	{
		int index = documentInfos.indexOf(selectedDocumentInfo);
		if (index < documentInfos.size() - 1)
			selectDocument(documentInfos.get(++index));
	}

	//------------------------------------------------------------------

	public void addDocument(
		PuzzleDocument	document)
	{
		// Create pairing of document and view state and add it to list
		DocumentInfo documentInfo = new DocumentInfo(document);
		documentInfos.add(documentInfo);

		// Update items of puzzle spinner
		updatePuzzleSpinnerItems();

		// Enable 'close puzzle' button
		closeButton.setDisable(false);

		// Select document
		selectDocument(documentInfo);

		// Request focus on puzzle view
		puzzleView.requestFocus();
	}

	//------------------------------------------------------------------

	/**
	 * Executes the specified task on a background thread.
	 *
	 * @param task
	 *          the task that will be executed.
	 */

	public void executeTask(
		Task<?>	task)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor(runnable ->
				new Thread(runnable, getClass().getSimpleName() + "-" + ++threadIndex));
		executor.execute(task);
		executor.shutdown();
	}

	//------------------------------------------------------------------

	private void showMessages(
		NodeMessage.List	messageList)
	{
		for (NodeMessage message : messageList.getMessages())
		{
			MessageIcon32 icon = switch (message.getKind())
			{
				case DEBUG, UNDEFINED -> MessageIcon32.ALERT;
				case INFO             -> MessageIcon32.INFORMATION;
				case WARNING          -> MessageIcon32.WARNING;
				case ERROR, FATAL     -> MessageIcon32.ERROR;
			};
			NotificationDialog.show(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, icon.get(),
									messageToString(message));
		}
	}

	//------------------------------------------------------------------

	/**
	 * Encodes the configuration of this application to the specified root node.
	 *
	 * @param rootNode
	 *          the root node of the configuration.
	 */

	private void encodeConfig(
		MapNode	rootNode)
	{
		// Clear properties
		rootNode.clear();

		// Create appearance node
		MapNode appearanceNode = new MapNode();

		// Encode theme ID
		String themeId = StyleManager.INSTANCE.getThemeId();
		if (themeId != null)
			appearanceNode.addString(PropertyKey.THEME, themeId);

		// Encode grid font
		FontInfo gridFont = preferences.gridFont();
		if (gridFont != null)
			appearanceNode.add(PropertyKey.GRID_FONT, gridFont.encode());

		// Add appearance node to root node
		if (!appearanceNode.isEmpty())
			rootNode.add(PropertyKey.APPEARANCE, appearanceNode);

		// Encode state of main window
		MapNode windowStateNode = mainWindowState.encodeTree();
		if (!windowStateNode.isEmpty())
			rootNode.add(PropertyKey.MAIN_WINDOW, windowStateNode);

		// Encode state of directory-browser window
		windowStateNode = DirectoryBrowserWindow.encodeState();
		if (!windowStateNode.isEmpty())
			rootNode.add(PropertyKey.DIRECTORY_BROWSER_WINDOW, windowStateNode);

		// Create puzzle node
		MapNode puzzleNode = new MapNode();

		// Encode 'ignore case when typing' flag
		MapNode gridNode = puzzleNode.addMap(PropertyKey.GRID);
		gridNode.addBoolean(PropertyKey.IGNORE_CASE_WHEN_TYPING, preferences.ignoreCaseInGrid());

		// Encode symbols
		if (!preferences.symbols().isEmpty())
		{
			MapNode node = puzzleNode.addMap(PropertyKey.SYMBOLS);
			for (Puzzle.Order order : preferences.symbols().keySet())
				node.addString(order.key(), new String(preferences.symbols().get(order)));
		}

		// Create 'edit history' node
		MapNode editHistoryNode = puzzleNode.addMap(PropertyKey.EDIT_HISTORY);

		// Encode maximum size of edit history
		editHistoryNode.addInt(PropertyKey.MAX_SIZE, preferences.editHistoryMaxSize());

		// Encode 'clear edit history on save' flag
		editHistoryNode.addBoolean(PropertyKey.CLEAR_ON_SAVE, preferences.editHistoryClearOnSave());

		// Add puzzle node
		rootNode.add(PropertyKey.PUZZLE, puzzleNode);

		// Create procedure to encode file-system location
		IProcedure2<String, Path> encodeLocation = (key, location) ->
		{
			if (location != null)
				rootNode.addString(key, PathUtils.absStringStd(location));
		};

		// Encode initial directory of 'open puzzle' file chooser
		if (openPuzzleDirectory != null)
			encodeLocation.invoke(PropertyKey.OPEN_PUZZLE_DIRECTORY, openPuzzleDirectory);

		// Encode initial kind of file for 'open puzzle' file chooser
		FileKind fileKind = FileKind.from(openPuzzleFileChooser);
		if (fileKind == null)
			fileKind = openPuzzleFileKind;
		rootNode.addString(PropertyKey.OPEN_PUZZLE_FILE_KIND, fileKind.key());

		// Encode initial directory of 'open template' file chooser
		if (openTemplateDirectory != null)
			encodeLocation.invoke(PropertyKey.OPEN_TEMPLATE_DIRECTORY, openTemplateDirectory);

		// Encode initial kind of file for 'open template' file chooser
		fileKind = FileKind.from(openTemplateFileChooser);
		if (fileKind == null)
			fileKind = openTemplateFileKind;
		rootNode.addString(PropertyKey.OPEN_TEMPLATE_FILE_KIND, fileKind.key());

		// Encode initial directory of 'save puzzle' file chooser for puzzles
		if (savePuzzleDirectory != null)
			encodeLocation.invoke(PropertyKey.SAVE_PUZZLE_DIRECTORY, savePuzzleDirectory);

		// Encode initial kind of file for 'save puzzle' file chooser
		fileKind = FileKind.from(savePuzzleFileChooser);
		if (fileKind == null)
			fileKind = savePuzzleFileKind;
		rootNode.addString(PropertyKey.SAVE_PUZZLE_FILE_KIND, fileKind.key());

		// Encode initial directory of 'save template' file chooser for puzzles
		if (saveTemplateDirectory != null)
			encodeLocation.invoke(PropertyKey.SAVE_TEMPLATE_DIRECTORY, saveTemplateDirectory);

		// Encode initial kind of file for 'save template' file chooser
		fileKind = FileKind.from(saveTemplateFileChooser);
		if (fileKind == null)
			fileKind = saveTemplateFileKind;
		rootNode.addString(PropertyKey.SAVE_TEMPLATE_FILE_KIND, fileKind.key());

		// Encode initial directory of 'export HTML' file chooser
		if (exportHtmlDirectory != null)
			encodeLocation.invoke(PropertyKey.EXPORT_HTML_DIRECTORY, exportHtmlDirectory);

		// Encode state of 'copy image' dialog
		MapNode dialogNode = CopyImageDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.COPY_IMAGE_DIALOG, dialogNode);

		// Encode state of 'copy text' dialog
		dialogNode = CopyTextDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.COPY_TEXT_DIALOG, dialogNode);

		// Encode state of 'export HTML' dialog
		dialogNode = ExportHtmlDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.EXPORT_HTML_DIALOG, dialogNode);

		// Encode state of generation dialog
		dialogNode = GenerationDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.GENERATION_DIALOG, dialogNode);

		// Encode state of 'homogenise entries' dialog
		dialogNode = HomogeniseEntriesDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.HOMOGENISE_ENTRIES_DIALOG, dialogNode);

		// Encode state of solution dialog
		dialogNode = SolutionDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.SOLUTION_DIALOG, dialogNode);

		// Encode state of template dialog
		dialogNode = TemplateDialog.encodeState();
		if (!dialogNode.isEmpty())
			rootNode.add(PropertyKey.TEMPLATE_DIALOG, dialogNode);
	}

	//------------------------------------------------------------------

	/**
	 * Decodes the configuration of this application from the specified root node.
	 *
	 * @param rootNode
	 *          the root node of the configuration.
	 * @param messages
	 *          a list to which this method may append messages that relate to errors that occurred when decoding the
	 *          configuration.
	 */

	private void decodeConfig(
		MapNode				rootNode,
		NodeMessage.List	messages)
	{
		// Decode appearance node
		String key = PropertyKey.APPEARANCE;
		if (rootNode.hasMap(key))
		{
			MapNode appearanceNode = rootNode.getMapNode(key);

			// Decode theme ID
			String themeId = appearanceNode.getString(PropertyKey.THEME, StyleManager.DEFAULT_THEME_ID);
			StyleManager.INSTANCE.selectThemeOrDefault(themeId);

			// Decode grid font
			key = PropertyKey.GRID_FONT;
			if (appearanceNode.hasMap(key))
			{
				MapNode gridFontNode = appearanceNode.getMapNode(key);
				FontInfo fontInfo = FontInfo.decode(gridFontNode);
				String name = fontInfo.name();
				if (StringUtils.isNullOrBlank(name))
					fontInfo = null;
				else if (!Font.getFamilies().contains(name))
				{
					fontInfo = null;
					messages.add(gridFontNode, NodeMessage.Kind.WARNING, ErrorMsg.FONT_NOT_AVAILABLE, name);
				}
				preferences.gridFont(fontInfo);
			}
		}

		// Decode state of main window
		key = PropertyKey.MAIN_WINDOW;
		if (rootNode.hasMap(key))
			mainWindowState.decodeTree(rootNode.getMapNode(key));

		// Decode state of directory-browser window
		key = PropertyKey.DIRECTORY_BROWSER_WINDOW;
		if (rootNode.hasMap(key))
			DirectoryBrowserWindow.decodeState(rootNode.getMapNode(key));

		// Decode puzzle node
		key = PropertyKey.PUZZLE;
		if (rootNode.hasMap(key))
		{
			MapNode puzzleNode = rootNode.getMapNode(key);

			// Decode 'ignore case when typing' flag
			key = PropertyKey.GRID;
			if (puzzleNode.hasMap(key))
			{
				MapNode gridNode = puzzleNode.getMapNode(key);
				key = PropertyKey.IGNORE_CASE_WHEN_TYPING;
				if (gridNode.hasBoolean(key))
					preferences.ignoreCaseInGrid(gridNode.getBoolean(key));
			}

			// Decode symbols
			key = PropertyKey.SYMBOLS;
			if (puzzleNode.hasMap(key))
			{
				Map<Puzzle.Order, char[]> symbols = new EnumMap<>(preferences.symbols());
				MapNode symbolsNode = puzzleNode.getMapNode(key);
				for (Puzzle.Order order : Puzzle.Order.values())
				{
					key = order.key();
					if (symbolsNode.hasString(key))
						symbols.put(order, symbolsNode.getString(key).toCharArray());
				}
				preferences.symbols(symbols);
			}

			// Decode 'edit history' node
			key = PropertyKey.EDIT_HISTORY;
			if (puzzleNode.hasMap(key))
			{
				MapNode editHistoryNode = puzzleNode.getMapNode(key);

				// Decode maximum size of edit history
				key = PropertyKey.MAX_SIZE;
				if (editHistoryNode.hasInt(key))
					preferences.editHistoryMaxSize(editHistoryNode.getInt(key));

				// Decode 'clear edit history on save' flag
				key = PropertyKey.CLEAR_ON_SAVE;
				if (editHistoryNode.hasBoolean(key))
					preferences.editHistoryClearOnSave(editHistoryNode.getBoolean(key));
			}
		}

		// Decode initial directory of 'open puzzle' file chooser
		key = PropertyKey.OPEN_PUZZLE_DIRECTORY;
		if (rootNode.hasString(key))
			openPuzzleDirectory = Path.of(rootNode.getString(key));

		// Decode initial kind of file for 'open puzzle' file chooser
		openPuzzleFileKind = rootNode.getEnumValue(FileKind.class, PropertyKey.OPEN_PUZZLE_FILE_KIND, FileKind::key,
												   FileKind.PUZZLE);

		// Decode initial directory of 'open template' file chooser
		key = PropertyKey.OPEN_TEMPLATE_DIRECTORY;
		if (rootNode.hasString(key))
			openTemplateDirectory = Path.of(rootNode.getString(key));

		// Decode initial kind of file for 'open template' file chooser
		openTemplateFileKind = rootNode.getEnumValue(FileKind.class, PropertyKey.OPEN_TEMPLATE_FILE_KIND, FileKind::key,
													 FileKind.TEMPLATE);

		// Decode initial directory of 'save puzzle' file chooser
		key = PropertyKey.SAVE_PUZZLE_DIRECTORY;
		if (rootNode.hasString(key))
			savePuzzleDirectory = Path.of(rootNode.getString(key));

		// Decode initial kind of file for 'save puzzle' file chooser
		savePuzzleFileKind = rootNode.getEnumValue(FileKind.class, PropertyKey.SAVE_PUZZLE_FILE_KIND, FileKind::key,
												   FileKind.PUZZLE);

		// Decode initial directory of 'save template' file chooser
		key = PropertyKey.SAVE_TEMPLATE_DIRECTORY;
		if (rootNode.hasString(key))
			saveTemplateDirectory = Path.of(rootNode.getString(key));

		// Decode initial kind of file for 'save template' file chooser
		saveTemplateFileKind = rootNode.getEnumValue(FileKind.class, PropertyKey.SAVE_TEMPLATE_FILE_KIND, FileKind::key,
													 FileKind.TEMPLATE);

		// Decode initial directory of 'export HTML' file chooser
		key = PropertyKey.EXPORT_HTML_DIRECTORY;
		if (rootNode.hasString(key))
			exportHtmlDirectory = Path.of(rootNode.getString(key));

		// Decode state of 'copy image' dialog
		key = PropertyKey.COPY_IMAGE_DIALOG;
		if (rootNode.hasMap(key))
			CopyImageDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of 'copy text' dialog
		key = PropertyKey.COPY_TEXT_DIALOG;
		if (rootNode.hasMap(key))
			CopyTextDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of 'export HTML' dialog
		key = PropertyKey.EXPORT_HTML_DIALOG;
		if (rootNode.hasMap(key))
			ExportHtmlDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of generation dialog
		key = PropertyKey.GENERATION_DIALOG;
		if (rootNode.hasMap(key))
			GenerationDialog.decodeState(rootNode.getMapNode(key));

		// Encode state of 'homogenise entries' dialog
		key = PropertyKey.HOMOGENISE_ENTRIES_DIALOG;
		if (rootNode.hasMap(key))
			HomogeniseEntriesDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of solution dialog
		key = PropertyKey.SOLUTION_DIALOG;
		if (rootNode.hasMap(key))
			SolutionDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of template dialog
		key = PropertyKey.TEMPLATE_DIALOG;
		if (rootNode.hasMap(key))
			TemplateDialog.decodeState(rootNode.getMapNode(key));
	}

	//------------------------------------------------------------------

	private void initialise(
		List<Path>	inFiles)
	{
		// Initialise font of puzzle pane
		PuzzlePane.setFont(preferences.gridFont());

		// Open files that were specified on command line
		for (Path file : inFiles)
			openFile(file, null);

		// Start periodic file task that opens received files and checks for modified files
		AtomicBoolean locked = new AtomicBoolean();
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable ->
				DaemonFactory.create(NAME_KEY + "-" + PERIODIC_FILE_TASK_THREAD_NAME_SUFFIX, runnable));
		executor.scheduleWithFixedDelay(() ->
		{
			// Prevent re-entry to this task
			if (locked.getAndSet(true))
				return;

			// Open received files and check for modified files
			try
			{
				// Open received files
				Platform.runLater(() ->
				{
					if (!receivedFiles.isEmpty())
					{
						List<Path> files = new ArrayList<>(receivedFiles);
						receivedFiles.clear();
						for (Path file : files)
							openFile(file, null);
					}
				});

				// Check for modified files
				Platform.runLater(() ->
				{
					for (DocumentInfo documentInfo : documentInfos)
					{
						// If reload is pending, skip document
						if (documentInfo.reloadRequired)
							continue;

						// Get next document
						PuzzleDocument document = documentInfo.document;

						// If document is not associated with a file or document's lock cannot be acquired, skip
						// document
						Path file = document.file();
						if ((file == null) || !document.lock().tryLock())
							continue;

						// Get timestamp that was last associated with document
						FileTime oldTimestamp = document.timestamp();

						// Get timestamp of file; reload file if timestamp is different from stored value
						try
						{
							// Get timestamp of file
							FileTime timestamp = Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);

							// If timestamp is different from stored value, reload file
							if (!timestamp.equals(oldTimestamp))
							{
								// Update stored timestamp
								document.timestamp(timestamp);

								// If no timestamp was stored previously, don't reload file
								if (oldTimestamp == null)
									continue;

								// Mark document for reloading
								documentInfo.reloadRequired = true;

								// If document is selected, reload file after confirmation
								if (documentInfo == selectedDocumentInfo)
									conditionalReloadFile(documentInfo);
							}
						}
						catch (IOException e)
						{
							// ignore
						}
						finally
						{
							// Release lock on document after stored timestamp has been updated
							document.lock().unlock();
						}
					}
				});
			}
			finally
			{
				// Allow this task to run
				locked.set(false);
			}
		},
		PERIODIC_FILE_TASK_INTERVAL, PERIODIC_FILE_TASK_INTERVAL, TimeUnit.MILLISECONDS);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a menu bar for the main window of this application.
	 *
	 * @return a menu bar for the main window of this application.
	 */

	private MenuBar createMenuBar()
	{
		// Create menu bar
		MenuBar menuBar = new MenuBar();
		menuBar.setPadding(Insets.EMPTY);

		// Create menu: file
		fileMenu = new Menu(FILE_STR);
		menuBar.getMenus().add(fileMenu);

		// Add items to 'file' menu
		fileMenu.getItems().addAll(
			Command.NEW.newMenuItem(),
			Command.OPEN_PUZZLE.newMenuItem(),
			Command.OPEN_TEMPLATE.newMenuItem(),
			Command.RELOAD.newMenuItem(),
			new SeparatorMenuItem(),
			Command.CLOSE.newMenuItem(),
			Command.CLOSE_ALL.newMenuItem(),
			Command.CLOSE_AND_DELETE.newMenuItem(),
			new SeparatorMenuItem(),
			Command.SAVE.newMenuItem(),
			Command.SAVE_AS_PUZZLE.newMenuItem(),
			Command.SAVE_AS_TEMPLATE.newMenuItem(),
			new SeparatorMenuItem(),
			Command.EXPORT_HTML.newMenuItem(),
			new SeparatorMenuItem(),
			Command.BROWSE_DIRECTORY.newMenuItem(),
			new SeparatorMenuItem(),
			Command.EXIT.newMenuItem()
		);

		// Enable/disable items of 'file' menu when menu is displayed
		fileMenu.setOnShowing(event -> updateMenuItems(fileMenu.getItems()));

		// Create menu: edit
		editMenu = new Menu(EDIT_STR);
		menuBar.getMenus().add(editMenu);

		// Add items to 'edit' menu
		editMenu.getItems().addAll(
			PuzzleDocument.Command.UNDO.newMenuItem(),
			PuzzleDocument.Command.REDO.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.COPY_TEXT.newMenuItem(),
			Command.PASTE.newMenuItem(),
			new SeparatorMenuItem(),
			Command.EDIT_PREFRENCES.newMenuItem()
		);

		// Enable/disable items of 'edit' menu when menu is displayed
		editMenu.setOnShowing(event -> updateMenuItems(editMenu.getItems()));

		// Create menu: view
		viewMenu = new Menu(VIEW_STR);
		menuBar.getMenus().add(viewMenu);

		// Add items to 'view' menu
		viewMenu.getItems().addAll(
			Command.COPY_IMAGE.newMenuItem()
		);

		// Enable/disable items of 'view' menu when menu is displayed
		viewMenu.setOnShowing(event -> updateMenuItems(viewMenu.getItems()));

		// Create menu: puzzle
		puzzleMenu = new Menu(PUZZLE_STR);
		menuBar.getMenus().add(puzzleMenu);

		// Add items to 'puzzle' menu
		puzzleMenu.getItems().addAll(
			PuzzleDocument.Command.CHOOSE_SYMBOL.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.FIND_ENTRY.newMenuItem(),
			PuzzleDocument.Command.SOLVE.newMenuItem(),
			PuzzleDocument.Command.CLOSE_SOLUTIONS.newMenuItem(),
			new SeparatorMenuItem(),
			Command.DUPLICATE.newMenuItem(),
			PuzzleDocument.Command.GENERATE.newMenuItem(),
			PuzzleDocument.Command.CREATE_TEMPLATE.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.COUNT_ENTRIES.newMenuItem(),
			PuzzleDocument.Command.CHECK.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.createTransformSubmenu(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.HOMOGENISE_ENTRIES.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.CLEAR.newMenuItem(),
			PuzzleDocument.Command.DELETE_PROPERTIES.newMenuItem(),
			new SeparatorMenuItem(),
			PuzzleDocument.Command.EDIT_SYMBOLS.newMenuItem()
		);

		// Create menu: platform
		platformMenu = new Menu(PLATFORM_STR);
		menuBar.getMenus().add(platformMenu);

		// Add items to 'platform' menu
		platformMenu.getItems().addAll(
			Command.MANAGE_FILE_ASSOCIATIONS.newMenuItem()
		);

		// Enable/disable items of 'puzzle' menu when menu is displayed
		puzzleMenu.setOnShowing(event -> updateMenuItems(puzzleMenu.getItems()));

		// Enable/disable menu items
		updateMenuItems();

		// Return menu bar
		return menuBar;
	}

	//------------------------------------------------------------------

	private void updateChanged()
	{
		PuzzleDocument document = selectedDocument();
		pathnameBar.label().setChanged((document != null) && document.isChanged());
	}

	//------------------------------------------------------------------

	private void updatePuzzleSpinnerItems()
	{
		puzzleSpinner.setItems(documentInfos, PUZZLE_SPINNER_PROTOTYPE_TEXT);
	}

	//------------------------------------------------------------------

	private void updateStatus()
	{
		PuzzleDocument document = selectedDocument();
		if ((document == null) || !document.hasSolutions())
		{
			statusLabel.setText((document == null) ? "" : document.statusText());
			statusLabel.setVisible(true);
			solutionInfoPane.setVisible(false);
		}
		else
		{
			solutionInfoPane.update();
			statusLabel.setVisible(false);
			solutionInfoPane.setVisible(true);
		}
	}

	//------------------------------------------------------------------

	private PuzzleDocument selectedDocument()
	{
		return (selectedDocumentInfo == null) ? null : selectedDocumentInfo.document;
	}

	//------------------------------------------------------------------

	private void selectDocument(
		DocumentInfo	documentInfo)
	{
		// Save view state that is associated with old selected document
		if ((selectedDocumentInfo != null) && (selectedDocumentInfo != documentInfo))
			selectedDocumentInfo.viewState = puzzleView.state();

		// Update instance variable
		selectedDocumentInfo = documentInfo;

		// Update puzzle view
		if (documentInfo == null)
			puzzleView.document();
		else
			puzzleView.document(documentInfo.document, documentInfo.viewState);

		// Show puzzle view if there is a document
		puzzleView.setVisible(documentInfo != null);

		// Update pathname bar
		PuzzleDocument document = selectedDocument();
		pathnameBar.text((document == null) ? null : document.pathame());

		// Update puzzle spinner
		puzzleSpinner.setItem(documentInfo);

		// Update UI
		updateUI();

		// Reload file if necessary
		if (documentInfo != null)
			conditionalReloadFile(documentInfo);
	}

	//------------------------------------------------------------------

	private void removeDocument()
	{
		removeDocument(documentInfos.indexOf(selectedDocumentInfo));
	}

	//------------------------------------------------------------------

	private void removeDocument(
		int	index)
	{
		// Remove document from list
		documentInfos.remove(index);

		// Update items of puzzle spinner
		updatePuzzleSpinnerItems();

		// Get index of new document
		index = Math.min(index, documentInfos.size() - 1);

		// Disable 'close puzzle' button
		if (index < 0)
			closeButton.setDisable(true);

		// Select document
		selectDocument((index < 0) ? null : documentInfos.get(index));
	}

	//------------------------------------------------------------------

	private void updateDocument(
		PuzzleDocument	document,
		Path			file,
		FileKind		fileKind,
		String			dialogTitle)
	{
		try
		{
			document.updateFile(file, fileKind);
		}
		catch (FileException e)
		{
			ErrorDialog.show(primaryStage, dialogTitle, e);
		}
		finally
		{
			// Select document
			documentInfos.stream().filter(di -> di.document == document).findFirst().ifPresent(this::selectDocument);
		}
	}

	//------------------------------------------------------------------

	private void openFile(
		Path		file,
		FileKind	fileKind)
	{
		// Get title for kind of file
		String title = (fileKind == null) ? OPEN_FILE_STR : switch (fileKind)
		{
			case PUZZLE   -> OPEN_PUZZLE_STR;
			case TEMPLATE -> OPEN_TEMPLATE_STR;
			default       -> OPEN_FILE_STR;
		};

		// Test for file
		if (!IOUtils.isExistingFile(file, primaryStage, title))
			return;

		// Find document that corresponds to file
		DocumentInfo documentInfo = null;
		for (DocumentInfo docInfo : documentInfos)
		{
			Path diFile = docInfo.document.file();
			if (diFile != null)
			{
				try
				{
					if (Files.isSameFile(file, diFile))
					{
						documentInfo = docInfo;
						break;
					}
				}
				catch (Exception e)
				{
					ErrorDialog.show(primaryStage, title, ErrorMsg.ERROR_ACCESSING_FILE, e);
				}
			}
		}

		// If document was not found, read file and add document to list ...
		if (documentInfo == null)
			readFile(file, fileKind, this::addDocument);

		// ... otherwise, reload file
		else
		{
			PuzzleDocument document = documentInfo.document;
			boolean changed = document.isChanged();
			if (!changed)
			{
				try
				{
					changed = !document.timestamp().equals(PuzzleDocument.getTimestamp(file));
				}
				catch (FileException e)
				{
					ErrorDialog.show(primaryStage, title, e);
					changed = true;
				}
			}
			if (!changed
					|| warnUnsavedChanges(document.displayName(), document.pathame(), RELOAD_FILE2_STR, RELOAD_STR))
				reloadFile(documentInfo);
		}
	}

	//------------------------------------------------------------------

	private void reloadFile(
		DocumentInfo	documentInfo)
	{
		// Get index of document info in list
		int index = documentInfos.indexOf(documentInfo);
		if (index < 0)
			return;

		// Test for file
		Path file = documentInfo.document.file();
		if (!IOUtils.isExistingFile(file, primaryStage, RELOAD_FILE1_STR))
			return;

		// Read file; replace current document and update view
		readFile(file, documentInfo.document.fileKind(), document ->
		{
			// Update document info
			documentInfo.document = document;
			documentInfo.viewState = PuzzlePane.newState();

			// Select document
			selectDocument(documentInfo);

			// Request focus on puzzle view
			puzzleView.requestFocus();
		});
	}

	//------------------------------------------------------------------

	private void readFile(
		Path						file,
		FileKind					fileKind,
		IProcedure1<PuzzleDocument>	onRead)
	{
		// Find kind of puzzle file for filename extension
		if (fileKind == null)
		{
			fileKind = FileKind.forLocation(file);
			if (fileKind == null)
			{
				String separator = "\n    ";
				String extensions = FileKind.ALL_FILENAME_EXTENSIONS.stream()
						.collect(Collectors.joining(separator, separator, ""));
				String message = PathUtils.abs(file) + MessageConstants.LABEL_SEPARATOR
						+ ErrorMsg.UNSUPPORTED_FILE_KIND + extensions;
				ErrorDialog.show(primaryStage, READ_FILE_STR, message);
				return;
			}
		}
		FileKind expectedFileKind = fileKind;

		// Create task to read file
		Task<PuzzleDocument> task = new Task<>()
		{
			{
				updateTitle(READ_FILE_STR);
				updateMessage(READING_STR + MessageConstants.SPACE_SEPARATOR + PathUtils.abs(file));
				updateProgress(-1, 1);
			}

			@Override
			protected PuzzleDocument call()
				throws Exception
			{
				Puzzle.FileInfo fileInfo = switch (expectedFileKind)
				{
					case PUZZLE, TEMPLATE -> Puzzle.fromJson(file, expectedFileKind);
					case TEXT             -> Puzzle.fromText(file);
				};
				return new PuzzleDocument(fileInfo.puzzle(), file, expectedFileKind);
			}

			@Override
			protected void succeeded()
			{
				onRead.invoke(getValue());
			}

			@Override
			protected void failed()
			{
				ErrorDialog.show(primaryStage, getTitle(), getException());
			}
		};

		// Create progress dialog for task
		new SimpleProgressDialog(primaryStage, task);

		// Execute task on background thread
		executeTask(task);
	}

	//------------------------------------------------------------------

	private void writeFile(
		PuzzleDocument	document,
		FileKind		fileKind,
		Path			file,
		IProcedure0		onWritten)
	{
		// Get puzzle
		Puzzle puzzle = document.puzzle();

		// If template is to be written, prompt to homogenise puzzle entries
		if ((fileKind == FileKind.TEMPLATE) && puzzle.hasHeterogeneousEntries())
		{
			switch (MessageDialog.show(primaryStage, HOMOGENISE_TEMPLATE_STR, MessageIcon32.QUESTION.get(),
									   HOMOGENISE_PROMPT_STR,
									   ButtonInfo.allRight(YES_STR, NO_STR, SimpleDialog.CANCEL_STR)))
			{
				case 0:
					if (!document.homogeniseEntries())
						return;
					break;

				case 1:
					// do nothing
					break;

				default:
					return;
			}
		}

		// Create task to write file
		Task<Void> task = new Task<>()
		{
			{
				updateTitle(WRITE_PUZZLE_FILE_STR);
				updateMessage(WRITING_STR + MessageConstants.SPACE_SEPARATOR + PathUtils.abs(file));
				updateProgress(-1, 1);
			}

			@Override
			protected Void call()
				throws Exception
			{
				try
				{
					// Wait until lock for document has been acquired
					while (!document.lock().tryLock())
						Thread.sleep(100);

					// Write puzzle to file
					switch (fileKind)
					{
						case PUZZLE, TEMPLATE -> puzzle.writeJson(file, fileKind);
						case TEXT             -> puzzle.writeText(file);
					}

					// Update timestamp of document for periodic external-modification check
					document.timestamp(Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS));
				}
				finally
				{
					// Release lock
					document.lock().unlock();
				}
				return null;
			}

			@Override
			protected void succeeded()
			{
				onWritten.invoke();
			}

			@Override
			protected void failed()
			{
				ErrorDialog.show(primaryStage, getTitle(), getException());
			}
		};

		// Create progress dialog for task
		new SimpleProgressDialog(primaryStage, task);

		// Execute task on background thread
		executeTask(task);
	}

	//------------------------------------------------------------------

	private void exportPuzzles(
		Window				window,
		List<Puzzle>		puzzles,
		Path				file,
		IProcedure1<Path>	onSuccess)
	{
		// Display dialog to choose colour scheme and font size
		ExportHtmlDialog.Result result = ExportHtmlDialog.show(window, puzzles.size());
		if (result == null)
			return;

		// Initialise file chooser
		if (exportHtmlFileChooser == null)
		{
			exportHtmlFileChooser = LocationChooser.forFiles();
			exportHtmlFileChooser.setDialogTitle(EXPORT_HTML_STR);
			exportHtmlFileChooser.setDialogStateKey();
			exportHtmlFileChooser.addFilters(HTML_FILES_FILTER, FileMatcher.ANY_FILE);
			exportHtmlFileChooser.setInitialFilter(0);
		}

		// Set initial directory of file chooser
		exportHtmlFileChooser.setInitialDirectory(
				(file == null)
						? (exportHtmlDirectory == null)
								? DEFAULT_DIRECTORY
								: exportHtmlDirectory
						: PathUtils.absParent(file));

		// Set initial filename of file chooser
		exportHtmlFileChooser.setInitialFilename((file == null) ? null : file.getFileName().toString());

		// Display file chooser
		file = exportHtmlFileChooser.showSaveDialog(window);

		// Write file
		if ((file != null) && IOUtils.replaceExistingFile(file, window, EXPORT_HTML_STR))
		{
			// Append filename extension, if filename doesn't have one
			Path outFile = exportHtmlFileChooser.appendFilenameSuffix(file);

			// Update directory
			exportHtmlDirectory = PathUtils.absParent(outFile);

			// Create task to write file
			Task<Void> task = new Task<>()
			{
				{
					updateTitle(WRITE_HTML_FILE_STR);
					updateMessage(WRITING_STR + MessageConstants.SPACE_SEPARATOR + PathUtils.abs(outFile));
					updateProgress(-1, 1);
				}

				@Override
				protected Void call()
					throws Exception
				{
					PuzzleIO.writeHtml(outFile, puzzles, SUDOKU_STR, result.colourScheme(), result.fontSize(),
									   result.numColumns());
					return null;
				}

				@Override
				protected void succeeded()
				{
					// Invoke 'on success' procedure
					if (onSuccess != null)
						onSuccess.invoke(outFile);

					// Report success
					String message = PathUtils.abs(outFile) + MessageConstants.LABEL_SEPARATOR + FILE_WRITTEN_STR;
					NotificationDialog.show(window, getTitle(), MessageIcon32.INFORMATION.get(), message);
				}

				@Override
				protected void failed()
				{
					ErrorDialog.show(window, getTitle(), getException());
				}
			};

			// Create progress dialog for task
			new SimpleProgressDialog(window, task);

			// Execute task on background thread
			executeTask(task);
		}
	}

	//------------------------------------------------------------------

	private boolean warnUnsavedChanges(
		String	name,
		String	longName,
		String	actionStr,
		String	acceptStr)
	{
		String message = ((longName == null) ? "" : longName + MessageConstants.LABEL_SEPARATOR)
							+ String.format(UNSAVED_PROMPT_STR, actionStr);
		return ConfirmationDialog.show(primaryStage, UNSAVED_CHANGES_STR + " : " + name, MessageIcon32.WARNING.get(),
									   message, acceptStr);
	}

	//------------------------------------------------------------------

	private List<DocumentInfo> chooseDocumentsToSave()
	{
		// Create list of documents whose documents have unsaved changes
		List<DocumentInfo> changedDocuments = documentInfos.stream().filter(di -> di.document.isChanged()).toList();

		// If any documents have unsaved changes, display dialog for selecting documents to be saved; return list of
		// documents
		return changedDocuments.isEmpty()
				? Collections.emptyList()
				: new UnsavedChangesDialog(changedDocuments).showDialog();
	}

	//------------------------------------------------------------------

	private void conditionalReloadFile(
		DocumentInfo	documentInfo)
	{
		if (documentInfo.reloadRequired)
		{
			try
			{
				Path file = documentInfo.document.file();
				if (file != null)
				{
					String message = PathUtils.abs(file) + MessageConstants.LABEL_SEPARATOR + MODIFIED_RELOAD_STR;
					if (ConfirmationDialog.show(primaryStage, MODIFIED_FILE_STR, MessageIcon32.QUESTION.get(), message,
												RELOAD_STR))
						reloadFile(documentInfo);
				}
			}
			finally
			{
				documentInfo.reloadRequired = false;
			}
		}
	}

	//------------------------------------------------------------------

	private void onNewPuzzle()
	{
		// Display dialog for selecting order of puzzle
		Puzzle.Order order = OrderDialog.show(primaryStage);

		// If order was selected, create new document and add it to list
		if (order != null)
			addDocument(new PuzzleDocument(order));
	}

	//------------------------------------------------------------------

	private void onOpenPuzzle()
	{
		// Initialise file chooser
		if (openPuzzleFileChooser == null)
		{
			openPuzzleFileChooser = LocationChooser.forFiles();
			openPuzzleFileChooser.setDialogTitle(OPEN_PUZZLE_STR);
			openPuzzleFileChooser.setDialogStateKey();
		}

		// Set filters of file chooser
		openPuzzleFileChooser.clearFilters();
		openPuzzleFileChooser.addFilter(FileKind.PUZZLE.matcher());
		openPuzzleFileChooser.addFilter(FileKind.TEXT.matcher());
		openPuzzleFileChooser.addFilter(FileMatcher.ANY_FILE);
		openPuzzleFileChooser.setInitialFilter((openPuzzleFileKind == FileKind.PUZZLE) ? 0 : 1);

		// Set initial directory of file chooser
		openPuzzleFileChooser.initDirectory(openPuzzleDirectory, DEFAULT_DIRECTORY);

		// Display file chooser
		Path file = openPuzzleFileChooser.showOpenDialog(primaryStage);

		// Open file
		if (file != null)
		{
			// Update directory
			openPuzzleDirectory = PathUtils.absParent(file);

			// Update kind of file
			FileKind fileKind = FileKind.from(openPuzzleFileChooser);
			if (fileKind != null)
				openPuzzleFileKind = fileKind;

			// Open puzzle file
			openFile(file, fileKind);
		}
	}

	//------------------------------------------------------------------

	private void onOpenTemplate()
	{
		// Initialise file chooser
		if (openTemplateFileChooser == null)
		{
			openTemplateFileChooser = LocationChooser.forFiles();
			openTemplateFileChooser.setDialogTitle(OPEN_TEMPLATE_STR);
			openTemplateFileChooser.setDialogStateKey();
		}

		// Set filters of file chooser
		openTemplateFileChooser.clearFilters();
		openTemplateFileChooser.addFilter(FileKind.TEMPLATE.matcher());
		openTemplateFileChooser.addFilter(FileKind.TEXT.matcher());
		openTemplateFileChooser.addFilter(FileMatcher.ANY_FILE);
		openTemplateFileChooser.setInitialFilter((openTemplateFileKind == FileKind.TEMPLATE) ? 0 : 1);

		// Set initial directory of file chooser
		openTemplateFileChooser.initDirectory(openTemplateDirectory, DEFAULT_DIRECTORY);

		// Display file chooser
		Path file = openTemplateFileChooser.showOpenDialog(primaryStage);

		// Open file
		if (file != null)
		{
			// Update directory
			openTemplateDirectory = PathUtils.absParent(file);

			// Update kind of file
			FileKind fileKind = FileKind.from(openTemplateFileChooser);
			if (fileKind != null)
				openTemplateFileKind = fileKind;

			// Open template file
			openFile(file, fileKind);
		}
	}

	//------------------------------------------------------------------

	private void onReloadFile()
	{
		PuzzleDocument document = selectedDocument();
		if ((document != null) && document.isChanged())
		{
			Path file = document.file();
			if (file != null)
			{
				if (ConfirmationDialog.show(primaryStage, RELOAD_FILE1_STR, MessageIcon32.QUESTION.get(),
											PathUtils.abs(file) + MessageConstants.LABEL_SEPARATOR + CONFIRM_RELOAD_STR,
											RELOAD_STR))
					reloadFile(selectedDocumentInfo);
			}
		}
	}

	//------------------------------------------------------------------

	private void onDuplicatePuzzle()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Get puzzle from selected document
		Puzzle puzzle = document.puzzle();

		// Create new document
		PuzzleDocument newDocument = new PuzzleDocument(puzzle.puzzleOrder());

		// Copy puzzle from selected document to new document
		newDocument.copyPuzzle(puzzle);

		// Add new document to list
		addDocument(newDocument);
	}

	//------------------------------------------------------------------

	private void onClosePuzzle()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// If document has not changed or user confirms closure, remove document from list
		if (!document.isChanged()
				|| warnUnsavedChanges(document.displayName(), document.pathame(), CLOSE_PUZZLE2_STR, CLOSE_STR))
			removeDocument();
	}

	//------------------------------------------------------------------

	private void onCloseAll()
	{
		// Test for documents
		if (documentInfos.isEmpty())
			return;

		// Allow user to choose which documents with unsaved changes to save
		List<DocumentInfo> documentsToSave = chooseDocumentsToSave();
		if (documentsToSave == null)
			return;

		// Save chosen documents with unsaved changes
		for (DocumentInfo docInfo : documentsToSave)
		{
			// Select document
			selectDocument(docInfo);

			// Save document
			onSaveFile();
		}

		// Remove all items from list of documents
		for (int i = documentInfos.size() - 1; i >= 0; i--)
			removeDocument(i);
	}

	//------------------------------------------------------------------

	private void onCloseAndDelete()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Get file
		Path file = document.file();
		if (file == null)
			return;

		// Display dialog to seek confirmation for closing puzzle and deleting file
		String message = String.format(document.isChanged() ? UNSAVED_PROMPT_STR : WANT_STR, CLOSE_DELETE2_STR);
		if (ConfirmationDialog.show(primaryStage, CLOSE_DELETE1_STR, MessageIcon32.QUESTION.get(), message,
									Command.CLOSE_AND_DELETE.text))
		{
			// Remove selected document from list of documents
			removeDocument();

			// Delete file
			try
			{
				FileSystemUtils.deleteWithRetries(file, 3);
			}
			catch (IOException e)
			{
				ErrorDialog.show(primaryStage, CLOSE_DELETE1_STR, ErrorMsg.FAILED_TO_DELETE_FILE, e);
			}
		}
	}

	//------------------------------------------------------------------

	private void onSaveFile()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if ((document == null) || !document.isChanged())
			return;

		// Get location of document file
		Path file = document.file();

		// If there is no document file, choose kind of file ...
		if (file == null)
		{
			FileKind fileKind = FileKindDialog.show(primaryStage);
			if (fileKind == null)
				return;
			switch (fileKind)
			{
				case PUZZLE   -> onSaveAsPuzzle();
				case TEMPLATE -> onSaveAsTemplate();
				default       -> { }
			}
		}

		// ... otherwise, write file
		else
		{
			// Hide solutions
			document.onCloseSolutions();

			// Write file
			FileKind fileKind = document.fileKind();
			String title = (fileKind == null) ? SAVE_FILE_STR : switch (fileKind)
			{
				case PUZZLE   -> SAVE_PUZZLE_STR;
				case TEMPLATE -> SAVE_TEMPLATE_STR;
				default       -> SAVE_FILE_STR;
			};
			writeFile(document, fileKind, file, () -> updateDocument(document, file, fileKind, title));
		}
	}

	//------------------------------------------------------------------

	private void onSaveAsPuzzle()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Hide solutions
		document.onCloseSolutions();

		// Initialise file chooser
		if (savePuzzleFileChooser == null)
		{
			savePuzzleFileChooser = LocationChooser.forFiles();
			savePuzzleFileChooser.setDialogTitle(SAVE_AS_PUZZLE_STR);
			savePuzzleFileChooser.setDialogStateKey();
			savePuzzleFileChooser.addFilter(FileKind.PUZZLE.matcher());
			savePuzzleFileChooser.addFilter(FileKind.TEXT.matcher());
			savePuzzleFileChooser.setInitialFilter((savePuzzleFileKind == FileKind.PUZZLE) ? 0 : 1);
		}

		// Set initial directory of file chooser
		Path file = document.file();
		savePuzzleFileChooser.setInitialDirectory(
				(savePuzzleDirectory == null)
						? (file == null)
								? DEFAULT_DIRECTORY
								: PathUtils.absParent(file)
						: savePuzzleDirectory);

		// Set initial filename of file chooser
		savePuzzleFileChooser.setInitialFilename((file == null)
				? null
				: StringUtils.getPrefixLast(file.getFileName().toString(), '.')
						+ FileKind.PUZZLE.filenameExtension());

		// Display file chooser
		file = savePuzzleFileChooser.showSaveDialog(primaryStage);

		// Write file
		if ((file != null) && IOUtils.replaceExistingFile(file, primaryStage, SAVE_AS_PUZZLE_STR))
		{
			// Append filename extension, if filename doesn't have one
			Path outFile = savePuzzleFileChooser.appendFilenameSuffix(file);

			// Update directory
			savePuzzleDirectory = PathUtils.absParent(outFile);

			// Update kind of file
			FileKind fileKind = FileKind.from(savePuzzleFileChooser);
			if (fileKind != null)
				savePuzzleFileKind = fileKind;

			// Write puzzle file
			writeFile(document, fileKind, outFile, () ->
					updateDocument(document, outFile, fileKind, SAVE_AS_PUZZLE_STR));
		}
	}

	//------------------------------------------------------------------

	private void onSaveAsTemplate()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Hide solutions
		document.onCloseSolutions();

		// Initialise file chooser
		if (saveTemplateFileChooser == null)
		{
			saveTemplateFileChooser = LocationChooser.forFiles();
			saveTemplateFileChooser.setDialogTitle(SAVE_AS_TEMPLATE_STR);
			saveTemplateFileChooser.setDialogStateKey();
			saveTemplateFileChooser.addFilter(FileKind.TEMPLATE.matcher());
			saveTemplateFileChooser.addFilter(FileKind.TEXT.matcher());
			saveTemplateFileChooser.setInitialFilter((saveTemplateFileKind == FileKind.TEMPLATE) ? 0 : 1);
		}

		// Set initial directory of file chooser
		Path file = document.file();
		saveTemplateFileChooser.setInitialDirectory(
				(saveTemplateDirectory == null)
						? (file == null)
								? DEFAULT_DIRECTORY
								: PathUtils.absParent(file)
						: saveTemplateDirectory);

		// Set initial filename of file chooser
		saveTemplateFileChooser.setInitialFilename((file == null)
				? null
				: StringUtils.getPrefixLast(file.getFileName().toString(), '.')
						+ FileKind.TEMPLATE.filenameExtension());

		// Display file chooser
		file = saveTemplateFileChooser.showSaveDialog(primaryStage);

		// Write file
		if ((file != null) && IOUtils.replaceExistingFile(file, primaryStage, SAVE_AS_TEMPLATE_STR))
		{
			// Append filename extension, if filename doesn't have one
			Path outFile = saveTemplateFileChooser.appendFilenameSuffix(file);

			// Update directory
			saveTemplateDirectory = PathUtils.absParent(outFile);

			// Update kind of file
			FileKind fileKind = FileKind.from(saveTemplateFileChooser);
			if (fileKind != null)
				saveTemplateFileKind = fileKind;

			// Write template file
			writeFile(document, fileKind, outFile, () ->
					updateDocument(document, outFile, fileKind, SAVE_AS_TEMPLATE_STR));
		}
	}

	//------------------------------------------------------------------

	private void onExportHtmlFile()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Hide solutions
		document.onCloseSolutions();

		// If there is no saved location of HTML file, derive location of HTML file from location of document
		Path file = document.exportHtmlFile();
		if (file == null)
		{
			file = document.file();
			if (file != null)
			{
				String name = StringUtils.getPrefixLast(file.getFileName().toString(), '.') + HTML_FILENAME_EXTENSION1;
				file = file.resolveSibling(name);
			}
		}

		// Export puzzle
		exportPuzzles(primaryStage, List.of(document.puzzle()), file, outFile -> document.exportHtmlFile(outFile));
	}

	//------------------------------------------------------------------

	private void onBrowseDirectory()
	{
		// Ignore command if directory-browser window is displayed
		if (directoryBrowserWindow != null)
			return;

		// Choose directory
		Path directory = DirectoryBrowserWindow.chooseDirectory(primaryStage, null);
		if (directory == null)
			return;

		// Display directory-browser window
		directoryBrowserWindow = new DirectoryBrowserWindow(primaryStage, directory,
		documents ->
		{
			for (PuzzleDocument document : documents)
				openFile(document.file(), document.fileKind());
		},
		puzzles -> exportPuzzles(directoryBrowserWindow, puzzles, null, null));

		directoryBrowserWindow.setOnHidden(event ->
		{
			directoryBrowserWindow = null;
			updateMenuItems();
		});
		directoryBrowserWindow.show();
		updateMenuItems();
	}

	//------------------------------------------------------------------

	private void onExit()
	{
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	//------------------------------------------------------------------

	private void onPaste()
	{
		// Get system clipboard
		Clipboard clipboard = Clipboard.getSystemClipboard();

		// If there is text on the clipboard, create document from it ...
		try
		{
			// Test for text on clipboard
			if (!clipboard.hasString())
				throw new BaseException(ErrorMsg.NO_TEXT_ON_CLIPBOARD);

			// Get text from clipboard
			String text = clipboard.getString();
			if (text == null)
				throw new BaseException(ErrorMsg.NO_TEXT_ON_CLIPBOARD);

			// Create document and add it to list
			addDocument(new PuzzleDocument(Puzzle.parseText(text), true));
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, Command.PASTE.text, e);
		}
	}

	//------------------------------------------------------------------

	private void onEditPreferences()
	{
		Preferences result = PreferencesDialog.show(primaryStage, preferences);
		if (result != null)
		{
			// Update instance variable
			preferences = result;

			// Update grid font
			PuzzlePane.setFont(preferences.gridFont());

			// Redraw puzzle view
			puzzleView.invalidateFontSizes();
			puzzleView.redraw();
		}
	}

	//------------------------------------------------------------------

	private void onCopyImage()
	{
		// Get selected document
		PuzzleDocument document = selectedDocument();
		if (document == null)
			return;

		// Hide solutions
		document.onCloseSolutions();

		// Display dialog for size of image
		CopyImageDialog.Result result = CopyImageDialog.show(primaryStage);
		if (result == null)
			return;

		// Create pane for current puzzle
		PuzzlePane puzzlePane = new PuzzlePane(document);

		// Add puzzle pane to temporary scene
		double imageSize = (double)result.imageSize();
		new Scene(puzzlePane, imageSize, imageSize);

		// Create image of puzzle grid and put it on system clipbaord
		try
		{
			// Create image of puzzle grid
			double scaleX = result.applyDisplayScale() ? primaryStage.getOutputScaleX() : 1.0;
			double scaleY = result.applyDisplayScale() ? primaryStage.getOutputScaleY() : 1.0;
			WritableImage image = puzzlePane.gridImage(scaleX, scaleY);

			// Put image on clipboard
			ClipboardUtils.putImageThrow(image);

			// Report success
			NotificationDialog.show(primaryStage, Command.COPY_IMAGE.text, MessageIcon32.INFORMATION.get(),
									String.format(IMAGE_COPIED_STR, (int)image.getWidth(), (int)image.getHeight()));
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, Command.COPY_IMAGE.text, e);
		}
	}

	//------------------------------------------------------------------

	private void onManageFileAssociations()
	{
		// Display file-association dialog
		FileAssociationDialog.Result result = FileAssociationDialog.show(primaryStage);
		if (result == null)
			return;

		// Initialise file-association object
		FileAssociations fileAssociations = new FileAssociations();
		for (FileKind fileKind : FileKind.PRIMARY_FILE_KINDS)
			fileKind.addFileAssocParams(fileAssociations);

		// Create dialog for text output from script
		TextOutputTaskDialog dialog =
				new TextOutputTaskDialog(primaryStage, getClass().getName(), FILE_ASSOCIATION_STR);

		// Create task to write file-association script
		Task<Void> task = new Task<>()
		{
			{
				updateTitle(FILE_ASSOCIATION_STR);
				updateProgress(-1, 1);
			}

			@Override
			protected Void call()
				throws Exception
			{
				fileAssociations.executeScript(SHORT_NAME, result.javaLauncherPathname(), result.jarPathname(),
											   result.iconPathname(), FILE_ASSOC_SCRIPT_DIR_NAME_PREFIX,
											   FILE_ASSOC_SCRIPT_FILENAME_STEM, result.removeEntries(),
											   result.scriptLifeCycle(), dialog.writer(), this::isCancelled);
				return null;
			}

			@Override
			protected void succeeded()
			{
				NotificationDialog.show(primaryStage, getTitle(), MessageIcon32.INFORMATION.get(),
										FA_SCRIPT_OP_SUCCEEDED_STR);
			}

			@Override
			protected void failed()
			{
				ErrorDialog.show(primaryStage, getTitle(), getException());
			}
		};

		// Set task on dialog
		dialog.task(task);

		// Execute task on background thread
		executeTask(task);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMAND


	public enum Command
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		BROWSE_DIRECTORY
		(
			"Browse directory",
			true,
			new KeyCodeCombination(KeyCode.F12),
			SudokuGeneratorApp.instance::onBrowseDirectory
		),

		CLOSE
		(
			"Close",
			false,
			new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onClosePuzzle
		),

		CLOSE_ALL
		(
			"Close all",
			false,
			null,
			SudokuGeneratorApp.instance::onCloseAll
		),

		CLOSE_AND_DELETE
		(
			"Close and delete",
			false,
			null,
			SudokuGeneratorApp.instance::onCloseAndDelete
		),

		COPY_IMAGE
		(
			"Copy image",
			true,
			null,
			SudokuGeneratorApp.instance::onCopyImage
		),

		DUPLICATE
		(
			"Duplicate",
			false,
			new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onDuplicatePuzzle
		),

		EDIT_PREFRENCES
		(
			"Preferences",
			false,
			null,
			SudokuGeneratorApp.instance::onEditPreferences
		),

		EXIT
		(
			"Exit",
			false,
			null,
			SudokuGeneratorApp.instance::onExit
		),

		EXPORT_HTML
		(
			"Export HTML file",
			true,
			new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onExportHtmlFile
		),

		MANAGE_FILE_ASSOCIATIONS
		(
			"Manage Windows file associations",
			false,
			null,
			SudokuGeneratorApp.instance::onManageFileAssociations
		),

		NEW
		(
			"New",
			false,
			new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onNewPuzzle
		),

		OPEN_PUZZLE
		(
			"Open puzzle",
			true,
			new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onOpenPuzzle
		),

		OPEN_TEMPLATE
		(
			"Open template",
			true,
			new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
			SudokuGeneratorApp.instance::onOpenTemplate
		),

		PASTE
		(
			"Paste",
			false,
			new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onPaste
		),

		RELOAD
		(
			"Reload",
			false,
			null,
			SudokuGeneratorApp.instance::onReloadFile
		),

		SAVE
		(
			"Save",
			false,
			new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
			SudokuGeneratorApp.instance::onSaveFile
		),

		SAVE_AS_PUZZLE
		(
			"Save as puzzle",
			true,
			null,
			SudokuGeneratorApp.instance::onSaveAsPuzzle
		),

		SAVE_AS_TEMPLATE
		(
			"Save as template",
			true,
			null,
			SudokuGeneratorApp.instance::onSaveAsTemplate
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this command. */
		private	String						text;

		/** Flag: if {@code true}, the text of a menu item has a trailing ellipsis. */
		private	boolean						ellipsis;

		/** The key combination that invokes this command. */
		private	KeyCombination				keyCombo;

		/** The handler for action events that invoke this command. */
		private	EventHandler<ActionEvent>	eventHandler;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a command.
		 *
		 * @param text
		 *          the text that will represent the command.
		 * @param ellipsis
		 *          if {@code true}, the text of a menu item will have a trailing ellipsis.
		 * @param keyCombo
		 *          the key combination that will invoke the command.
		 * @param action
		 *          the action that will be performed by the command.
		 */

		private Command(
			String			text,
			boolean			ellipsis,
			KeyCombination	keyCombo,
			Runnable		action)
		{
			// Initialise instance variables
			this.text = text;
			this.ellipsis = ellipsis;
			this.keyCombo = keyCombo;
			eventHandler = event -> action.run();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates and returns a new instance of a menu item for this command.
		 *
		 * @return a new instance of a menu item for this command.
		 */

		public MenuItem newMenuItem()
		{
			MenuItem menuItem = new MenuItem(ellipsis ? text + ELLIPSIS_STR : text);
			menuItem.setUserData(this);
			if (keyCombo != null)
				menuItem.setAccelerator(keyCombo);
			menuItem.setOnAction(eventHandler);
			return menuItem;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CONFIGURATION


	/**
	 * This class implements the configuration of the application.
	 */

	private static class Configuration
		extends AppConfig
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The identifier of a configuration file. */
		private static final	String	ID	= "P9GURXV3OWJ84MA61MDP76F9J";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of the configuration of the application.
		 *
		 * @throws BaseException
		 *           if the configuration directory could not be determined.
		 */

		private Configuration()
			throws BaseException
		{
			// Call superclass constructor
			super(ID, NAME_KEY, SHORT_NAME, LONG_NAME);

			// Determine location of config file
			if (!noConfigFile())
			{
				// Get location of parent directory of config file
				AppAuxDirectory.Directory directory =
						AppAuxDirectory.getDirectory(NAME_KEY, getClass().getEnclosingClass());
				if (directory == null)
					throw new BaseException(ErrorMsg.NO_AUXILIARY_DIRECTORY);

				// Set parent directory of config file
				setDirectory(directory.location());
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PAIRING OF PUZZLE DOCUMENT AND VIEW STATE


	private static class DocumentInfo
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PuzzleDocument		document;
		private	PuzzlePane.State	viewState;
		private	boolean				reloadRequired;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DocumentInfo(
			PuzzleDocument	document)
		{
			// Initialise instance variables
			this.document = document;
			viewState = PuzzlePane.newState();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof DocumentInfo other) && (document == other.document);
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return System.identityHashCode(document);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String displayName()
		{
			return document.displayName();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: SOLUTION-INFORMATION PANE


	private class SolutionInfoPane
		extends HBox
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	PREVIOUS_SOLUTION_STR	= "Previous solution";
		private static final	String	NEXT_SOLUTION_STR		= "Next solution";
		private static final	String	SOLUTION_STR			= "Solution";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	GraphicButton	previousButton;
		private	GraphicButton	nextButton;
		private	Label			label;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SolutionInfoPane()
		{
			// Set properties
			setSpacing(6.0);
			setAlignment(Pos.CENTER_LEFT);
			getStyleClass().add(StyleClass.SOLUTION_INFO_PANE);

			// Create factory for buttons
			double iconHeight = 0.8 * TextUtils.textHeight();
			IFunction2<GraphicButton, Shape, String> buttonFactory = (arrowhead, text) ->
			{
				// Set properties of icon
				arrowhead.setFill(getColour(ColourKey.SOLUTION_BUTTON_ICON));
				arrowhead.getStyleClass().add(StyleClass.SOLUTION_BUTTON_ICON);

				// Create button
				GraphicButton button = new GraphicButton(Shapes.tile(arrowhead, Math.ceil(iconHeight)));
				button.setBorderColour(getColour(ColourKey.SOLUTION_BUTTON_BORDER));
				TooltipDecorator.addTooltip(button, text);
				return button;
			};

			// Button: previous solution
			previousButton = buttonFactory.invoke(Shapes.arrowhead01(VHDirection.LEFT, iconHeight),
												  PREVIOUS_SOLUTION_STR);
			previousButton.setOnAction(event ->
			{
				PuzzleDocument document = selectedDocument();
				if (document != null)
					document.showPreviousSolution();
			});

			// Button: next solution
			nextButton = buttonFactory.invoke(Shapes.arrowhead01(VHDirection.RIGHT, iconHeight), NEXT_SOLUTION_STR);
			nextButton.setOnAction(event ->
			{
				PuzzleDocument document = selectedDocument();
				if (document != null)
					document.showNextSolution();
			});

			// Create label
			label = new Label();
			setMargin(label, new Insets(0.0, 0.0, 0.0, 2.0));

			// Add children to this pane
			getChildren().addAll(previousButton, nextButton, label);

			// Update buttons and label
			update();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void update()
		{
			PuzzleDocument document = selectedDocument();
			if (document == null)
			{
				previousButton.setDisable(true);
				nextButton.setDisable(true);
				label.setText(SOLUTION_STR + " 0 / 0");
			}
			else
			{
				PuzzleDocument.SolutionInfo	solutionInfo = document.solutionInfo();
				int solutionIndex = solutionInfo.index();
				int numSolutions = solutionInfo.numSolutions();
				previousButton.setDisable(solutionIndex <= 0);
				nextButton.setDisable(solutionIndex >= numSolutions - 1);
				label.setText(SOLUTION_STR + " " + (solutionIndex + 1) + " / " + numSolutions);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'UNSAVED CHANGES' DIALOG


	/**
	 * This class implements a modal dialog in which the pathnames of puzzle documents that have unsaved changes are
	 * displayed in a list view.  Documents to be saved may be selected from the list view.
	 */

	private class UnsavedChangesDialog
		extends SimpleModalDialog<List<DocumentInfo>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The width of the list view of documents. */
		private static final	double	LIST_VIEW_WIDTH		= 440.0;

		/** The height of the list view of documents. */
		private static final	double	LIST_VIEW_HEIGHT	= 184.0;

		/** The padding around a button. */
		private static final	Insets	BUTTON_PADDING	= new Insets(3.0, 12.0, 3.0, 12.0);

		/** Miscellaneous strings. */
		private static final	String	DOCUMENTS_STR			=
				"The following documents have changes that have not been saved:";
		private static final	String	SELECT_ALL_STR			= "Select all";
		private static final	String	SAVE_SELECTED_STR		= "Save selected";
		private static final	String	DISCARD_SELECTED_STR	= "Discard selected";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The result of this dialog. */
		private	List<DocumentInfo>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private UnsavedChangesDialog(
			List<DocumentInfo>	documentInfos)
		{
			// Call superclass constructor
			super(primaryStage, MethodHandles.lookup().lookupClass().getCanonicalName(), null, UNSAVED_CHANGES_STR);

			// Allow dialog to be resized
			setResizable(true);

			// Create message pane
			HBox messagePane = new HBox(8.0, MessageIcon32.WARNING.get(), Labels.hNoShrink(DOCUMENTS_STR));
			messagePane.setAlignment(Pos.CENTER_LEFT);

			// Create list view of documents
			SimpleTextListView<DocumentInfo> listView = new SimpleTextListView<>(di -> di.document.displayName());
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
			listView.setItems(FXCollections.observableArrayList(documentInfos));
			VBox.setVgrow(listView, Priority.ALWAYS);

			// Create content pane
			VBox contentPane = new VBox(6.0, messagePane, listView);
			contentPane.setAlignment(Pos.CENTER);

			// Add content pane to container
			addContent(contentPane);

			// Create button: select all
			Button selectAllButton = Buttons.hNoShrink(SELECT_ALL_STR);
			selectAllButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			selectAllButton.setOnAction(event -> listView.getSelectionModel().selectAll());
			addButton(selectAllButton, HPos.LEFT);

			// Create button: save selected
			Button saveSelectedButton = Buttons.hNoShrink(SAVE_SELECTED_STR);
			saveSelectedButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP2);
			saveSelectedButton.setOnAction(event ->
			{
				result = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
				requestClose();
			});
			addButton(saveSelectedButton, HPos.CENTER);

			// Create button: discard selected
			Button discardSelectedButton = Buttons.hNoShrink(DISCARD_SELECTED_STR);
			discardSelectedButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP2);
			discardSelectedButton.setOnAction(event ->
			{
				listView.getItems().removeAll(listView.getSelectionModel().getSelectedItems());
				if (listView.getItems().isEmpty())
				{
					result = Collections.emptyList();
					requestClose();
				}
				listView.getSelectionModel().select(0);
				listView.requestFocus();
			});
			addButton(discardSelectedButton, HPos.CENTER);

			// Create button: cancel
			Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP3);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'request to close window' event if Escape is pressed
			setRequestCloseOnEscape();

			// Update buttons and select tab when selection in list view changes
			listView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) observable ->
			{
				// Update buttons
				boolean noSelection = listView.getSelectionModel().isEmpty();
				saveSelectedButton.setDisable(noSelection);
				discardSelectedButton.setDisable(noSelection);

				// Select document
				List<DocumentInfo> docInfos = listView.getSelectionModel().getSelectedItems();
				if (!docInfos.isEmpty())
					selectDocument(docInfos.get(0));
			});

			// Select first item in list view
			listView.getSelectionModel().select(0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		protected Insets getButtonPadding()
		{
			return BUTTON_PADDING;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		protected List<DocumentInfo> getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
