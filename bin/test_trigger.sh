#!/bin/bash -x

if [ -z $1 ] ; then 
  echo "Missing variable suffix. eg use $0 MAIN file"
  exit 4;
fi

if [ -z $2 ] ; then 
  echo "Missing webhook payload. eg use $0 MAIN file"
  exit 5;
fi

SUFFIX=$1
TEAMCITY_BASE_URL=TEAMCITY_BASE_URL_$SUFFIX
TEAMCITY_TOKEN=TEAMCITY_TOKEN_$SUFFIX

FILE=$2

if [ -z ${!TEAMCITY_BASE_URL} ] ; then
  echo "No value found for TEAMCITY_BASE_URL_$SUFFIX"
  exit 2;
fi

if [ -z ${!TEAMCITY_TOKEN} ] ; then
  echo "No value found for TEAMCITY_TOKEN_$SUFFIX"
  exit 3;
fi


  echo "Sending trigger with payload from: $FILE"
  curl -H "Accept: application/json" \
       -H "Authorization: Bearer ${!TEAMCITY_TOKEN}" \
       -H "Content-Type: application/json" \
       -d @${FILE} \
       -vv \
       ${!TEAMCITY_BASE_URL}/app/rest/webhook-trigger/TcPlugins_TcWebHookTriggerTest