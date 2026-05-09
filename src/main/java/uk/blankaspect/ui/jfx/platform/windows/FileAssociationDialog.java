/*====================================================================*\

FileAssociationDialog.java

Class: Windows file-association dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.platform.windows;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javafx.stage.Window;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.common.property.PropertyString;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.container.PathnamePane;

import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.dropdownlist.SimpleDropDownList;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.textfield.PathnameField;

//----------------------------------------------------------------------


// CLASS: WINDOWS FILE-ASSOCIATION DIALOG


public class FileAssociationDialog
	extends SimpleModalDialog<FileAssociationDialog.Result>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The relative location of the Windows Java launcher in the {@code JAVA_HOME} directory. */
	private static final	String	JAVA_LAUNCHER_PATHNAME	= "bin\\javaw.exe";

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= SystemUtils.workingDirectory();

	/** The filename extension of a Windows executable file. */
	private static final	String	EXE_FILENAME_EXTENSION	= ".exe";

	/** The filename extension of a Windows icon file. */
	private static final	String	ICON_FILENAME_EXTENSION	= ".ico";

	/** The filename extension of a JAR file. */
	private static final	String	JAR_FILENAME_EXTENSION	= ".jar";

	/** The filter for Windows executable files. */
	private static final	FileMatcher	EXE_FILES_FILTER	=
			FileMatcher.from("Windows executable files", EXE_FILENAME_EXTENSION);

	/** The filter for Windows icon files. */
	private static final	FileMatcher	ICON_FILES_FILTER	=
			FileMatcher.from("Windows icon files", ICON_FILENAME_EXTENSION);

	/** The filter for JAR files. */
	private static final	FileMatcher	JAR_FILES_FILTER	=
			FileMatcher.from("JAR files", JAR_FILENAME_EXTENSION);

	/** The margins around a check box. */
	private static final	Insets	CHECK_BOX_MARGINS	= new Insets(2.0, 0.0, 2.0, 0.0);

	/** The horizontal gap between adjacent columns of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The vertical gap between adjacent rows of the control pane. */
	private static final	double	CONTROL_PANE_V_GAP	= 8.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(2.0);

	/** The number of columns of a pathname field. */
	private static final	int		PATHNAME_FIELD_NUM_COLUMNS	= 40;

	/** The padding around a <i>default</i> button. */
	private static final	Insets	DEFAULT_BUTTON_PADDING	= new Insets(3.0, 8.0, 3.0, 8.0);

	/** Miscellaneous strings */
	private static final	String	FILE_ASSOCIATIONS_STR	= "Windows file associations";
	private static final	String	ACTION_STR				= "Action";
	private static final	String	JAVA_LAUNCHER_STR		= "Java launcher";
	private static final	String	JAR_STR					= "JAR";
	private static final	String	ICON_STR				= "Icon";
	private static final	String	FILES_MUST_EXIST_STR	= "Files must exist";
	private static final	String	SCRIPT_LIFE_CYCLE_STR	= "Script life cycle";
	private static final	String	DEFAULT_STR				= "Default";
	private static final	String	JAVA_LAUNCHER_FILE_STR	= "Java-launcher file";
	private static final	String	JAR_FILE_STR			= "JAR file";
	private static final	String	ICON_FILE_STR			= "Icon file";

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	JAVA_HOME_DIR	= "java.home";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	NO_JAVA_LAUNCHER_FILE_LOCATION =
				"The location of the Java-launcher file was not specified.";

		String	NO_JAR_FILE_LOCATION =
				"The location of the JAR file was not specified.";

		String	NO_ICON_FILE_LOCATION =
				"The location of the icon file was not specified.";

		String	NOT_A_FILE =
				"The location does not denote a regular file.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state							= new State();
	private static	Path	defaultJavaLauncherLocation;
	private static	Path	defaultJarLocation;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Result	result;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Get location of Java launcher
		String pathname = System.getProperty(SystemPropertyKey.JAVA_HOME_DIR);
		if (pathname != null)
		{
			try
			{
				Path location = Path.of(pathname, JAVA_LAUNCHER_PATHNAME);
				if (Files.exists(location))
					defaultJavaLauncherLocation = PathUtils.abs(location);
			}
			catch (Exception e)
			{
				// ignore
			}
		}

		// Get location of JAR
		try
		{
			Path location =
					Path.of(FileAssociationDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS)
					&& PathnameUtils.suffixMatches(location, JAR_FILENAME_EXTENSION))
				defaultJarLocation = PathUtils.abs(location);
		}
		catch (Exception e)
		{
			// ignore
		}
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileAssociationDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, FILE_ASSOCIATIONS_STR, state.locator(), null);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_PANE_H_GAP);
		controlPane.setVgap(CONTROL_PANE_V_GAP);
		controlPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Drop-down list: action
		SimpleDropDownList<Action> actionList = new SimpleDropDownList<>(Action.values());
		actionList.item(state.action);
		controlPane.addRow(row++, new Label(ACTION_STR), actionList);

		// Get start index of components that are required by 'add' action
		int addActionStartIndex = controlPane.getChildren().size();

		// File chooser: Java launcher
		LocationChooser javaLauncherFileChooser = LocationChooser.forFiles();
		javaLauncherFileChooser.setDialogTitle(JAVA_LAUNCHER_FILE_STR);
		javaLauncherFileChooser.setDialogStateKey();
		javaLauncherFileChooser.addFilters(EXE_FILES_FILTER, FileMatcher.ANY_FILE);
		javaLauncherFileChooser.setInitialFilter(0);

		// Pathname field: Java launcher
		PathnameField javaLauncherField = new PathnameField(state.javaLauncherPathname, PATHNAME_FIELD_NUM_COLUMNS);
		javaLauncherField.setShowInvalidPathnameError(true);
		javaLauncherField.setLocationMatcher(EXE_FILES_FILTER::matches);
		javaLauncherField.textProperty().addListener(observable -> javaLauncherField.getLocation());

		// Pathname pane: Java launcher
		PathnamePane javaLauncherPane = new PathnamePane(javaLauncherField, true, event ->
		{
			// Set initial directory and filename of file chooser
			javaLauncherField.initChooser(javaLauncherFileChooser, DEFAULT_DIRECTORY);

			// Display file chooser
			Path file = javaLauncherFileChooser.showSelectDialog(this);

			// Update pathname field
			if (file != null)
				javaLauncherField.setLocation(file);
		});

		// Button: default Java launcher
		Button javaLauncherDefaultButton = new Button(DEFAULT_STR);
		javaLauncherDefaultButton.setPadding(DEFAULT_BUTTON_PADDING);
		javaLauncherDefaultButton.setOnAction(event ->
				javaLauncherField.setText(defaultJavaLauncherLocation.toString()));
		controlPane.addRow(row++, new Label(JAVA_LAUNCHER_STR), javaLauncherPane, javaLauncherDefaultButton);

		// File chooser: JAR
		LocationChooser jarFileChooser = LocationChooser.forFiles();
		jarFileChooser.setDialogTitle(JAR_FILE_STR);
		jarFileChooser.setDialogStateKey();
		jarFileChooser.addFilters(JAR_FILES_FILTER, FileMatcher.ANY_FILE);
		jarFileChooser.setInitialFilter(0);

		// Pathname field: JAR
		PathnameField jarField = new PathnameField(state.jarPathname, PATHNAME_FIELD_NUM_COLUMNS);
		jarField.setShowInvalidPathnameError(true);
		jarField.setLocationMatcher(JAR_FILES_FILTER::matches);
		jarField.textProperty().addListener(observable -> jarField.getLocation());

		// Pathname pane: JAR
		PathnamePane jarPane = new PathnamePane(jarField, true, event ->
		{
			// Set initial directory and filename of file chooser
			jarField.initChooser(jarFileChooser, DEFAULT_DIRECTORY);

			// Display file chooser
			Path file = jarFileChooser.showSelectDialog(this);

			// Update pathname field
			if (file != null)
				jarField.setLocation(file);
		});

		// Button: default JAR
		Button jarDefaultButton = new Button(DEFAULT_STR);
		jarDefaultButton.setPadding(DEFAULT_BUTTON_PADDING);
		jarDefaultButton.setOnAction(event -> jarField.setText(defaultJarLocation.toString()));
		controlPane.addRow(row++, new Label(JAR_STR), jarPane, jarDefaultButton);

		// File chooser: icon
		LocationChooser iconFileChooser = LocationChooser.forFiles();
		iconFileChooser.setDialogTitle(ICON_FILE_STR);
		iconFileChooser.setDialogStateKey();
		iconFileChooser.addFilters(ICON_FILES_FILTER, FileMatcher.ANY_FILE);
		iconFileChooser.setInitialFilter(0);

		// Pathname field: icon
		PathnameField iconField = new PathnameField(state.iconPathname, PATHNAME_FIELD_NUM_COLUMNS);
		iconField.setShowInvalidPathnameError(true);
		iconField.setLocationMatcher(ICON_FILES_FILTER::matches);
		iconField.textProperty().addListener(observable -> iconField.getLocation());

		// Pathname pane: icon
		PathnamePane iconPane = new PathnamePane(iconField, true, event ->
		{
			// Set initial directory and filename of file chooser
			iconField.initChooser(iconFileChooser, DEFAULT_DIRECTORY);

			// Display file chooser
			Path file = iconFileChooser.showSelectDialog(this);

			// Update pathname field
			if (file != null)
				iconField.setLocation(file);
		});
		controlPane.addRow(row++, new Label(ICON_STR), iconPane);

		// Check box: files must exist
		CheckBox filesMustExistCheckBox = new CheckBox(FILES_MUST_EXIST_STR);
		filesMustExistCheckBox.setSelected(state.filesMustExist);
		GridPane.setMargin(filesMustExistCheckBox, CHECK_BOX_MARGINS);
		controlPane.add(filesMustExistCheckBox, 1, row++);

		// Get end index of components that are required by 'add' action
		int addActionEndIndex = controlPane.getChildren().size();

		// Create procedure to update components
		IProcedure0 updateComponents = () ->
		{
			// Enable/disable components that relate to action
			boolean disabled = (actionList.item() == Action.REMOVE);
			for (int i = addActionStartIndex; i < addActionEndIndex; i++)
				controlPane.getChildren().get(i).setDisable(disabled);

			// Enable/disable 'default' buttons
			if (!disabled)
			{
				javaLauncherDefaultButton.setDisable(defaultJavaLauncherLocation == null);
				jarDefaultButton.setDisable(defaultJarLocation == null);
			}
		};

		// Update components when selected action changes
		actionList.itemProperty().addListener(observable -> updateComponents.invoke());

		// Drop-down list: script life cycle
		SimpleDropDownList<FileAssociations.ScriptLifeCycle> scriptLifeCycleList =
				new SimpleDropDownList<>(FileAssociations.ScriptLifeCycle.values());
		scriptLifeCycleList.item(state.scriptLifeCycle);
		controlPane.addRow(row++, new Label(SCRIPT_LIFE_CYCLE_STR), scriptLifeCycleList);

		// Update components
		updateComponents.invoke();

		// Add control pane to content pane
		addContent(controlPane);

		// Create procedure to update state of dialog
		IProcedure0 updateState = () ->
		{
			state.restoreAndUpdate(this, true);
			state.action = actionList.item();
			state.javaLauncherPathname = javaLauncherField.getText();
			state.jarPathname = jarField.getText();
			state.iconPathname = iconField.getText();
			state.filesMustExist = filesMustExistCheckBox.isSelected();
			state.scriptLifeCycle = scriptLifeCycleList.item();
		};

		// Create button: OK
		Button okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Validate locations of files
			if (actionList.item() == Action.ADD)
			{
				try
				{
					// Get 'files must exist' flag
					boolean mustExist = filesMustExistCheckBox.isSelected();

					// Validate location of Java-launcher file
					validateLocation(javaLauncherField, mustExist, ErrorMsg.NO_JAVA_LAUNCHER_FILE_LOCATION);

					// Validate location of JAR file
					validateLocation(jarField, mustExist, ErrorMsg.NO_JAR_FILE_LOCATION);

					// Validate location of icon file
					validateLocation(iconField, mustExist, ErrorMsg.NO_ICON_FILE_LOCATION);
				}
				catch (BaseException e)
				{
					ErrorDialog.show(this, FILE_ASSOCIATIONS_STR, e);
					return;
				}
			}

			// Update dialog state
			updateState.invoke();

			// Set result
			result = state.result();

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

		// When dialog is closed, save dialog state
		setOnHiding(event ->
		{
			if (result == null)
				updateState.invoke();
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Result show(
		Window	owner)
	{
		return new FileAssociationDialog(owner).showDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Result getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void validateLocation(
		PathnameField	field,
		boolean			mustExist,
		String			errorMessage)
		throws BaseException
	{
		try
		{
			if (field.isEmpty())
				throw new BaseException(errorMessage);
			if (mustExist)
			{
				Path file = field.getLocation();
				if (file != null)
				{
					if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
						throw new FileException(ErrorMsg.NOT_A_FILE, file);
				}
			}
		}
		catch (BaseException e)
		{
			field.requestFocus();
			throw e;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ACTION


	public enum Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ADD
		(
			"Add"
		),

		REMOVE
		(
			"Remove"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Action(
			String	text)
		{
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

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: RESULT


	public record Result(
		String								javaLauncherPathname,
		String								jarPathname,
		String								iconPathname,
		boolean								removeEntries,
		FileAssociations.ScriptLifeCycle	scriptLifeCycle)
	{ }

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

		private static final	char	ENV_VAR_PREFIX	= '%';
		private static final	char	ENV_VAR_SUFFIX	= '%';

		private static final	char	UNIX_FILE_SEPARATOR		= '/';
		private static final	char	WINDOWS_FILE_SEPARATOR	= '\\';

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private Action								action;
		private String								javaLauncherPathname;
		private String								jarPathname;
		private String								iconPathname;
		private boolean								filesMustExist;
		private FileAssociations.ScriptLifeCycle	scriptLifeCycle;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, false);

			// Initialise instance variablesv
			action = Action.ADD;
			filesMustExist = true;
			scriptLifeCycle = FileAssociations.ScriptLifeCycle.WRITE_EXECUTE_DELETE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static String processPathname(
			String	pathname)
		{
			StringBuilder buffer = new StringBuilder(256);
			for (PropertyString.Span span : PropertyString.getSpans(pathname))
			{
				if (span.text() != null)
				{
					if (span.kind() == PropertyString.SpanKind.ENVIRONMENT)
					{
						buffer.append(ENV_VAR_PREFIX);
						buffer.append(span.key());
						buffer.append(ENV_VAR_SUFFIX);
					}
					else
						buffer.append(span.text().replace(UNIX_FILE_SEPARATOR, WINDOWS_FILE_SEPARATOR));
				}
			}
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Result result()
		{
			return new Result(processPathname(javaLauncherPathname), processPathname(jarPathname),
							  processPathname(iconPathname), action == Action.REMOVE, scriptLifeCycle);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
