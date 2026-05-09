/*====================================================================*\

IProcedure2.java

Interface: procedure with two parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: PROCEDURE WITH TWO PARAMETERS


/**
 * This functional interface defines the method that must be implemented by a <i>procedure</i> (a function that has no
 * return value) with two parameters.  A procedure acts only through its side effects.
 *
 * @param <T1>
 *          the type of the first parameter.
 * @param <T2>
 *          the type of the second parameter.
 */

@FunctionalInterface
public interface IProcedure2<T1, T2>
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this procedure with the specified arguments.
	 *
	 * @param arg1
	 *          the first argument.
	 * @param arg2
	 *          the second argument.
	 */

	void invoke(
		T1	arg1,
		T2	arg2);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
