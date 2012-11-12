#!-*- sh -*- 
# 
# Abs: multiplier_setup.bash is a bash source script to set the runtime environment for
# execution of the multiplier example of an EPICS V4 client/server interaction.  
#
# Usage: This script should be "sourced" prior to execution of both the 
# client and server sides the multiplier Service, "multiplierServerRunner" and
# "multiplierClientRunner". 
#
# Dependencies: pvDataJava, pvAccessJava, EasyPVA
#
# Remarks: This script only actually exports MULTIPLIER, PVSERVICE and CLASSPATH (plus
# whatever is exported in pvCommon_setup.bash). Everything
# else in here is just getting to right values for those. 
#  
# Ref: MULTIPLIERSERVICE_README.txt
#
# ----------------------------------------------------------------------------
# Auth: 14-Sep-2012, Greg White SLAC/PSI, (greg@slac.stanford.edu).
# Mod:  
# ============================================================================

# Set the antecedent dependency locations, used to 
# set the classpath, and in startup to find setup data like xmls.
#
WORKSPACE=$HOME/Development/epicsV4/workspace_hg_beta2
EXAMPLES=${WORKSPACE}/exampleJava

PVDATA=${WORKSPACE}/pvDataJava
PVACCESS=${WORKSPACE}/pvAccessJava
EASYPVA=${WORKSPACE}/alphaJava/easyPVA

# Set the CLASSPATH. Classpath requires classes or jars for all the antecedent
# dependencies of multiplier: pvData, pvAccess. As written here
# it assumes exampleJava, pvDataJava and pvAccessJava were built
# by Eclipse (delivering classes to their respective /bin dirs) but you may 
# need to edit this if you build a different way.
#
CLASSPATH=${EXAMPLES}/target/classes
CLASSPATH=${CLASSPATH}:${PVDATA}/bin
CLASSPATH=${CLASSPATH}:${PVACCESS}/bin
CLASSPATH=${CLASSPATH}:${EASYPVA}/bin

# Export the variables actually used by callers
#
export CLASSPATH

# printenv CLASSPATH | tr : '\n'

