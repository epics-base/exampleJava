# Example Application `arrayPerformance`

This example application demonstrates the performance of large arrays.

Specific explanations and instructions can be found in the documentation of the parent 
module `exampleJava`.

## Building

This application is a Maven project. It can be built using an IDE (e.g. Eclipse,
NetBeans) or from the command line using

    mvn install

## Running

There are wrapper scripts for Windows and Linux in the `scripts` directory.
From the current directory (where this file is located), run

on Linux:

    ./scripts/<specificName>

on Windows:

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_25   (or where your Java is)
    .\scripts\<specificName>.bat
