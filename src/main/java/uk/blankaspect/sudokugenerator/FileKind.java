/*====================================================================*\

FileKind.java

Enumeration: kinds of file supported by the application.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Path;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;
import uk.blankaspect.ui.jfx.locationchooser.LocationMatcher;

//----------------------------------------------------------------------


// ENUMERATION: KINDS OF FILE SUPPORTED BY THE APPLICATION


enum FileKind
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	PUZZLE
	(
		"Puzzle",
		"Sudoku puzzle",
		"Sudoku puzzle files",
		".sgp",
		FileAssoc.KEY_PREFIX + ".puzzle",
		"Sudoku puzzle file",
		"&Open with " + SudokuGeneratorApp.SHORT_NAME
	),

	TEMPLATE
	(
		"Template",
		"Sudoku template",
		"Sudoku template files",
		".sgt",
		FileAssoc.KEY_PREFIX + ".template",
		"Sudoku template file",
		"&Open with " + SudokuGeneratorApp.SHORT_NAME
	),

	TEXT
	(
		"Text",
		"Sudoku puzzle",
		"Text files",
		".txt",
		null,
		null,
		null
	);

	public static final	Set<FileKind>	PRIMARY_FILE_KINDS	= EnumSet.of(PUZZLE, TEMPLATE);

	public static final	List<String>	ALL_FILENAME_EXTENSIONS	=
			Arrays.stream(values()).map(kind -> kind.filenameExtension).toList();

	private interface FileAssoc
	{
		String	KEY_PREFIX	= "BlankAspect." + SudokuGeneratorApp.SHORT_NAME;
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String		key;
	private	String		text;
	private	String		description;
	private	String		filenameExtension;
	private	FileMatcher	matcher;
	private	String		fileAssocFileKindKey;
	private	String		fileAssocFileKindText;
	private	String		fileAssocFileOpenText;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileKind(
		String	text,
		String	description,
		String	chooserDescription,
		String	filenameExtension,
		String	fileAssocFileKindKey,
		String	fileAssocFileKindText,
		String	fileAssocFileOpenText)
	{
		// Initialise instance variables
		key = name().toLowerCase();
		this.text = text;
		this.description = description;
		this.filenameExtension = filenameExtension;
		matcher = FileMatcher.from(chooserDescription, filenameExtension);
		this.fileAssocFileKindKey = fileAssocFileKindKey;
		this.fileAssocFileKindText = fileAssocFileKindText;
		this.fileAssocFileOpenText = fileAssocFileOpenText;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FileKind forLocation(
		Path	location)
	{
		return Arrays.stream(values()).filter(kind -> kind.matches(location)).findFirst().orElse(null);
	}

	//------------------------------------------------------------------

	public static FileKind from(
		LocationChooser	fileChooser)
	{
		if (fileChooser != null)
		{
			LocationMatcher matcher = fileChooser.getFinalFilter();
			return Arrays.stream(values()).filter(kind -> kind.matcher == matcher).findFirst().orElse(null);
		}
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String key()
	{
		return key;
	}

	//------------------------------------------------------------------

	public String description()
	{
		return description;
	}

	//------------------------------------------------------------------

	public String filenameExtension()
	{
		return filenameExtension;
	}

	//------------------------------------------------------------------

	public FileMatcher matcher()
	{
		return matcher;
	}

	//------------------------------------------------------------------

	public boolean matches(
		Path	location)
	{
		return matcher.matches(location);
	}

	//------------------------------------------------------------------

	public void addFileAssocParams(
		FileAssociations	fileAssociations)
	{
		fileAssociations.addParams(fileAssocFileKindKey, fileAssocFileKindText, fileAssocFileOpenText,
								   filenameExtension);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
