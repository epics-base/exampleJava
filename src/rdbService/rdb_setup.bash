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
# whatever is exported in pvCommon_setup.bash, which is presently JAVAIOC). Everything
# else in here is just getting to right values for those. 
#
# Sources:  pvCommon_setup.bash - for JAVAIOC
#  
# Ref: RDBSERVICE_README.txt
#
# ----------------------------------------------------------------------------
# Auth: 14-Sep-2011, Greg White (greg@slac.stanford.edu).
# Mod:  
# ============================================================================

# "Source" file that sets up shared references. Sets JAVAIOC shared reference.
source $HOME/Development/epicsV4/ev4hg/common/source/pvCommon_setup.bash

# Set the antecedent dependency locations, used to 
# set the classpath, and in startup to find setup data like xmls.
#
WORKSPACE=$HOME/Development/epicsV4/ev4hg
# Where to find service xmls:
PVSERVICE=${WORKSPACE}/pvServiceJava
# Where to find exampleJava's class files, as used in CLASSPATH:
EXAMPLES=${WORKSPACE}/exampleJava
# Where to find RDBSERVICES's own record db xml:
RDBXML=${EXAMPLES}/src/rdbService


# Set the CLASSPATH. Classpath requires classes or jars for all the antecedent
# dependencies of rdbService: pvIOC, pvData, pvAccess, pvService, CAJ, JCA. 
# If you're going to use the "SWTConsole" then swt.jar too. As written here
# it assumes exampleJava, pvIOCJava, pvDataJava and pvServiceJava were built
# by Eclipse (delivering classes to their respective /bin dirs) but you may 
# need to edit this if you build a different way.
#
CLASSPATH=${EXAMPLES}/target/classes
PVVERSION='1.1-SNAPSHOT'
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/pvIOC/${PVVERSION}/pvIOC-${PVVERSION}.jar
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/pvData/${PVVERSION}/pvData-${PVVERSION}.jar
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/pvAccess/${PVVERSION}/pvAccess-${PVVERSION}.jar
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/pvService/${PVVERSION}/pvService-${PVVERSION}.jar
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/caj/1.1.8/caj-1.1.8.jar
CLASSPATH=${CLASSPATH}:${HOME}/.m2/repository/epics/jca/2.3.5/jca-2.3.5.jar
# If you like, add swt.jar for your platform. It may be in /usr/lib (linux) or a
# subdir of the Eclipse app on a Mac.
# For linux add CLASSPATH=${CLASSPATH}:/usr/lib/eclipse/swt.jar 
# For mac add, eg, CLASSPATH=${CLASSPATH}:/Applications/eclipse\ 3.6.2\ \(IDE\ for\ EE\)\ /plugins/org.eclipse.swt.cocoa.macosx.x86_64_3.6.2.v3659b.jar

# Export the variables actually used at runtime.
#
export RDBXML
export PVSERVICE
export CLASSPATH