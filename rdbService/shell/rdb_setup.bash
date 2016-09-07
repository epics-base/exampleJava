#!-*- sh -*- 
# 
# Abs: rdb_setup.bash is a bash source script to set the runtime environment for
# execution of the rdbService example of an EPICS V4 client/server system.  
#
# Usage: This script should be "sourced" prior to execution of both the 
# client and server sides of rdbSerivice, "rdbServerRunner" and
# "rdbClientRunner". 
#
# Remarks: This script only actually exports RDBXML, PVSERVICE and CLASSPATH (plus
# whatever is exported in pvCommon_setup.bash). Everything
# else in here is just getting to right values for those. 
#
# Sources:  pvCommon_setup.bash
#  
# Ref: RDBSERVICE_README.txt
#
# ----------------------------------------------------------------------------
# Auth: 14-Sep-2011, Greg White (greg@slac.stanford.edu).
# Mod:  
# ============================================================================

# "Source" file that sets up shared references. Sets JAVAIOC shared reference.
# source $HOME/Development/epicsV4/workspace_hg/common/source/pvCommon_setup.bash

# Set the antecedent dependency locations, used to 
# set the classpath, and in startup to find setup data like xmls.
#
WORKSPACE=$HOME/Development/epicsV4/workspace_hg_beta2
EXAMPLEJAVA=${WORKSPACE}/exampleJava

PVDATA=${WORKSPACE}/pvDataJava
PVACCESS=${WORKSPACE}/pvAccessJava

# Set the CLASSPATH. Classpath requires classes or jars for all the antecedent
# dependencies of helloWorld: pvIOC, pvData, pvAccess, pvService, CAJ, JCA. 
# If you're going to use the "SWTConsole" then swt.jar too. As written here
# it assumes exampleJava, pvIOCJava, pvDataJava and pvServiceJava were built
# by Eclipse (delivering classes to their respective /bin dirs) but you may 
# need to edit this if you build a different way.
#
CLASSPATH=${EXAMPLEJAVA}/target/classes
CLASSPATH=${CLASSPATH}:${PVDATA}/bin
CLASSPATH=${CLASSPATH}:${PVACCESS}/bin

# Export the variables actually used at runtime.
#
export CLASSPATH
