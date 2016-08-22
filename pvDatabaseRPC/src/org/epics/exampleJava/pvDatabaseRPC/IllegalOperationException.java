/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author Dave Hickin
 *
 */

package org.epics.exampleJava.pvDatabaseRPC;

public class IllegalOperationException extends RuntimeException
{
    IllegalOperationException(String message)
    {
        super(message);
    }
}
