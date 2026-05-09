/*====================================================================*\

Symmetry.java

Class: kinds of symmetry of a sudoku puzzle.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.sudokugenerator;

//----------------------------------------------------------------------


// ENUMERATION: KINDS OF SYMMETRY OF A SUDOKU PUZZLE


/**
 * This is an enumeration of the kinds of symmetry that may be applied when creating a template for a sudoku puzzle.
 */

enum Symmetry
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * No symmetry.
	 */
	NONE
	(
		"none",
		"None"
	),

	/**
	 * Reflection in the vertical axis.
	 */
	REFLECTION_V
	(
		"reflectionV",
		"Reflection in vertical axis"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions(gridSize, (gridSize + 1) / 2);
		}
	},

	/**
	 * Reflection in the horizontal axis.
	 */
	REFLECTION_H
	(
		"reflectionH",
		"Reflection in horizontal axis"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions((gridSize + 1) / 2, gridSize);
		}
	},

	/**
	 * Reflection in the vertical and horizontal axes.
	 */
	REFLECTION_VH
	(
		"reflectionVH",
		"Reflection in V and H axes"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions((gridSize + 1) / 2, (gridSize + 1) / 2);
		}
	},

	/**
	 * Reflection in the diagonal from top-left to bottom-right.
	 */
	REFLECTION_DIAGONAL_TL
	(
		"reflectionDiagonalTL",
		"Reflection in top-left diagonal : \u27CD"
	),

	/**
	 * Reflection in the diagonal from top-right to bottom-left.
	 */
	REFLECTION_DIAGONAL_TR
	(
		"reflectionDiagonalTR",
		"Reflection in top-right diagonal : \u27CB"
	),

	/**
	 * Reflection in both diagonals.
	 */
	REFLECTION_DIAGONALS
	(
		"reflectionDiagonals",
		"Reflection in both diagonals"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions((gridSize + 1) / 2, gridSize);
		}
	},

	/**
	 * Two-fold rotation.
	 */
	ROTATION_2
	(
		"rotation2",
		"2-fold rotation"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions((gridSize + 1) / 2, gridSize);
		}
	},

	/**
	 * Four-fold rotation.
	 */
	ROTATION_4
	(
		"rotation4",
		"4-fold rotation"
	)
	{
		@Override
		public Dimensions principalDimensions(
			int	gridSize)
		{
			return new Dimensions((gridSize + 1) / 2, (gridSize + 1) / 2);
		}
	};

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The key that is associated with this kind of symmetry. */
	private	String	key;

	/** The string representation of this kind of symmetry. */
	private	String	text;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an enumeration constant for a kind of symmetry.
	 *
	 * @param key
	 *          the key that will be associated with the kind of symmetry.
	 * @param text
	 *          the string representation of the kind of symmetry.
	 */

	private Symmetry(
		String	key,
		String	text)
	{
		// Initialise instance variables
		this.key = key;
		this.text = text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the key that is associated with this kind of symmetry.
	 *
	 * @return the key that is associated with this kind of symmetry.
	 */

	public String key()
	{
		return key;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the number of rows and columns of the principal region of a grid of the specified size (number of rows
	 * and columns) for this kind of symmetry.  The dimensions of the principal region are used when creating a template
	 * for a puzzle.
	 *
	 * @param  gridSize
	 *           the number of rows and columns of the grid.
	 * @return the number of rows and columns of the principal region of a grid of size {@code gridSize} for this kind
	 *         of symmetry.
	 */

	public Dimensions principalDimensions(
		int	gridSize)
	{
		return new Dimensions(gridSize, gridSize);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: PAIRING OF A NUMBER OF ROWS AND A NUMBER OF COLUMNS


	record Dimensions(
		int	numRows,
		int	numColumns)
	{ }

	//==================================================================

}

//----------------------------------------------------------------------
