#Set JNODE_HOME
#JNODE_HOME=

if [ -z "${JNODE_HOME}" ]
then
echo "Please set JNODE_HOME before running this script."
exit -1
fi

CRON_HOME=${JNODE_HOME}/all/cron
BUILD_LOG=${CRON_HOME}/build.log

cd ${JNODE_HOME}
cp ${CRON_HOME}/go.png ${CRON_HOME}/status.png
date &>${BUILD_LOG}
svn up 1>>${BUILD_LOG} 2>&1
./build.sh clean cd-x86-lite cd-x86_64-lite javadoc-small javadoc document-plugins 1>>${BUILD_LOG} 2>&1
res=`grep "BUILD SUCCESSFUL" ${BUILD_LOG}`
if [ -z "$res" ]
then
cp ${CRON_HOME}/no.png ${CRON_HOME}/status.png
else
cp ${CRON_HOME}/ok.png ${CRON_HOME}/status.png
fi
