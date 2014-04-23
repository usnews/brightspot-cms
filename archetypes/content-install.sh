#!/bin/bash

USAGE="$0 [-g <groupId>],[-a <artifactId>],[-c <class prefix>],[-n <namespaceUri>],[-h prints this message]"
WHO=`whoami`
BASE_DIR=`pwd`

#######################################################
# Get commandline arguments
#######################################################
while getopts ":g:a:c:n:h:" OPT
do
    case ${OPT} in
        g ) 
            GROUP_ID="${OPTARG}"
    ;;
        a ) 
            ARTIFACT_ID="${OPTARG}"
    ;;
        c ) 
            CLASS_PREFIX="${OPTARG}"
    ;;
        n ) 
            NAMESPACE_URI="${OPTARG}"
    ;;
        h ) 
            echo "${USAGE}"
            exit 1
    ;;
        * ) 
        echo "ERROR: Unknown option $OPT"
        echo "${USAGE}"
        exit 1
    ;;
    esac
done

if [[ -z $GROUP_ID ]]; then
    echo "Enter a groupId (ex: com.psddev)"
    read GROUP_ID
fi

if [[ -z $ARTIFACT_ID ]]; then
    echo "Enter an artifactId (ex: helloWorld)"
    read ARTIFACT_ID
fi

if [[ -z $CLASS_PREFIX ]]; then
    echo "Enter a classPrefix (ex: HelloWorld)"
    read CLASS_PREFIX
fi

if [[ -z $NAMESPACE_URI ]]; then
    echo "Enter a namespace URI (ex: helloWorld.psddev.com)"
    read NAMESPACE_URI
fi


# always end in a forward-slash
if [[ $BASE_DIR != */ ]]; then
    BASE_DIR=$BASE_DIR"/"
fi

#######################################################
# Run Maven Archetypes in order
#######################################################

# Brightspot CMS
mvn archetype:generate -B \
-DarchetypeRepository=http://public.psddev.com/maven \
-DarchetypeGroupId=com.psddev \
-DarchetypeArtifactId=cms-app-archetype \
-DarchetypeVersion=2.4-SNAPSHOT \
-DgroupId=$GROUP_ID \
-DartifactId=$ARTIFACT_ID

# Content Common
mvn archetype:generate \
  -DarchetypeGroupId=com.psddev \
  -DarchetypeArtifactId=cms-content-common-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -DclassPrefix=$CLASS_PREFIX \
  -Dversion=1.0-SNAPSHOT \
  -DnamespaceUri=$NAMESPACE_URI \
  -DinteractiveMode=false


// Content Article
mvn archetype:generate \
  -DarchetypeGroupId=com.psddev \
  -DarchetypeArtifactId=cms-content-article-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=$GROUP_ID \
  -DartifactId=$ARTIFACT_ID \
  -Ddari.version=2.4-SNAPSHOT \
  -DinteractiveMode=false

