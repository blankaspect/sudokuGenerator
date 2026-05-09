/*====================================================================*\

IProcedure3.java

Interface: procedure with three parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: PROCEDURE WITH THREE PARAMETERS


/**
 * This functional interface defines the method that must be implemented by a <i>procedure</i> (a function that has no
 * return value) with three parameters.  A procedure acts only through its side effects.
 *
 * @param <T1>
 *          the type of the first parameter.
 * @param <T2>
 *          the type of the second parameter.
 * @param <T3>
 *          the type of the third parameter.
 */

@FunctionalInterface
public interface IProcedure3<T1, T2, T3>
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
	 * @param arg3
	 *          the third argument.
	 */

	void invoke(
		T1	arg1,
		T2	arg2,
		T3	arg3);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
