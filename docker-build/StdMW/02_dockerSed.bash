#!/bin/bash

echo "START 02_dockerSedStdMW"

echo "BEA_VERSION_TIMESTAMP"

set -e

STD_DIR=$1

# release version, such as BEA_VERSION_TIMESTAMP
RELEASE=${2}
# user id, such as 2002
USER_ID=${3}
# outside port for Tomcat, such as 8080
OUTSIDE_PORT=${4}
# subnet to use for compose stack, such as 128.1.1.1/24. Use 0 for "no subnet"
SUBNET=${5}
# environment, usually dvlp, stag, or prod
ENVIRON=${6}
START_SCRIPT=/deploy-dir/${RELEASE}/10p_startPROD.bash
STOP_SCRIPT=/deploy-dir/${RELEASE}/20p_stopPROD.bash
UPCHECK_SCRIPT=/deploy-dir/${RELEASE}/30p_checkPROD.bash
if [ "${ENVIRON}" == "stag" ]; then
	START_SCRIPT=/deploy-dir/${RELEASE}/10s_startSTAG.bash
	STOP_SCRIPT=/deploy-dir/${RELEASE}/20s_stopSTAG.bash
	UPCHECK_SCRIPT=/deploy-dir/${RELEASE}/30s_checkSTAG.bash
fi
if [ "${ENVIRON}" == "dvlp" ]; then
	START_SCRIPT=/deploy-dir/${RELEASE}/10d_startDVLP.bash
	STOP_SCRIPT=/deploy-dir/${RELEASE}/20d_stopDVLP.bash
	UPCHECK_SCRIPT=/deploy-dir/${RELEASE}/30d_checkDVLP.bash
fi
if [ "${ENVIRON}" == "hub" ]; then
	START_SCRIPT=-
	STOP_SCRIPT=-
	UPCHECK_SCRIPT=-
fi
# outside path for Tomcat log files
OUTSIDE_LOGPATH=${7}
# output config location for StdMW, should contain MW_CACHE directory
OUTSIDE_CONFIGPATH=${8}
# URL and tag to use as image name, such as mdabcb/smw_image:DAP_BEA_VERSION_TIMESTAMP
IMAGE_URL=${9}
# path for making ZIPs
ZIPTMPPATH=${10}
# group id, such as 2002
GROUP_ID=${11}

# if SUBNET is not equal to 0, then also remove the #SUBNET comments so the IPAM gets used
IPAM_COMMENT=#SUBNET
if [ "${SUBNET}" != "0" ]; then
	# replace comment with nothing, to activate IPAM settings
	IPAM_COMMENT=
fi

echo "create Dockerfile from Dockerfile_template"

rm -f ${STD_DIR}/Dockerfile
sed -e "s|<RELEASE_VERSION>|SMW_${RELEASE}|g" \
    -e "s|<USERID>|${USER_ID}|g" \
    -e "s|<GROUPID>|${GROUP_ID}|g" \
    -e "s|<LOG_DIR>|${OUTSIDE_LOGPATH}|g" \
    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
    ${STD_DIR}/Dockerfile_template > ${STD_DIR}/Dockerfile

echo "create docker-compose.yml from docker-compose_template.yml"

rm -f ${STD_DIR}/docker-compose.yml
sed -e "s|<OUTSIDE_PORT>|${OUTSIDE_PORT}|g" \
    -e "s|<SUBNET>|${SUBNET}|g" \
    -e "s|<ENVIRON>|${ENVIRON}|g" \
    -e "s|<LOGPATH>|${OUTSIDE_LOGPATH}|g" \
    -e "s|#SUBNET|${IPAM_COMMENT}|g" \
    -e "s|<IMAGEURL>|${IMAGE_URL}|g" \
    -e "s|<CONFIGPATH>|${OUTSIDE_CONFIGPATH}|g" \
    -e "s|<ZIPTMPPATH>|${ZIPTMPPATH}|g" \
    ${STD_DIR}/docker-compose_template.yml > ${STD_DIR}/docker-compose.yml

# then build with docker compose -f docker-compose.yml build --force-rm --no-cache

echo "FINISH 02_dockerSedStdMW"

