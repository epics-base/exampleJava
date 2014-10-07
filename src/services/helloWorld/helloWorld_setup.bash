#!-*- sh -*- 
# 
# Abs: helloWorld_setup.bash is a bash source script to set the runtime environment for
# execution of the helloWorld example of an EPICS V4 client/server interaction.  
#
# Usage: This script should be "sourced" prior to execution of both the 
# client and server sides the hello Service, "helloServerRunner" and
# "helloClientRunner". 
#
# Sources:  pvCommon_setup.bash
#
# Remarks: This script only actually exports only CLASSPATH. Everything
# else in here is just getting to right values for those. 
#  
# Ref: HELLOWORLD_README.txt
#
# ----------------------------------------------------------------------------
# Auth: 14-Sep-2011, Greg White (greg@slac.stanford.edu).
# Mod:  12-Nov-2012, Greg White (greg@slac.stanford.edu),
#       Removed reference to pvCommon_setup
# ============================================================================

# Set the antecedent dependency locations, used to 
# set the classpath, and in startup to find setup data like xmls.
#
#WORKSPACE=$HOME/Development/epicsV4/workspace_hg_beta3
WORKSPACE=/home/hg
EXAMPLES=${WORKSPACE}/exampleJava

PVDATA=${WORKSPACE}/pvDataJava
PVACCESS=${WORKSPACE}/pvAccessJava

# Set the CLASSPATH. Classpath requires classes or jars for all the antecedent
# dependencies of helloWorld: pvData, pvAccess. As written here
# it assumes exampleJava, pvDataJava and pvAccessJava were built
# by Eclipse (delivering classes to their respective /bin dirs) but you may 
# need to edit this if you build a different way.
#
CLASSPATH=${EXAMPLES}/target/classes
CLASSPATH=${CLASSPATH}:${PVDATA}/bin
CLASSPATH=${CLASSPATH}:${PVACCESS}/bin

# Export the variables actually used at runtime.
#
export CLASSPATH

# Uncomment to debug. 
# printenv CLASSPATH | tr : '\n'

