#!/bin/bash

##
## Script for downloading and installing
## dependencies that don't have a remote
## repository and we need to get them manually
##
## Requires Git and Maven
##

##
## Remove previous dependencies code
##
rm -rf deps

##
## Clone function, clones the given repository
## from the given GitHub user using the specified
## commit as target.
##
## Parameters: GitHub User, Repository, Commit, Install command
##
function install {
    git clone "https://github.com/$1/$2.git" "deps/$2"
    cd "deps/$2" || exit 1
    git checkout -b buildDep "$3"

    ## Check if it's a Maven project and replace HTTP by HTTPS
    if [[ -f "pom.xml" ]]; then
        sed -i "s/http:\/\//https:\/\//" "pom.xml"
    fi

    $4 # install
    cd "../.." || exit 1 # go back
}

# EzChat 2.5.0 dependency: ddownload 0.1.0
install unnamed ddownload faac4eec9dc3b410688903f443309fda06ff86d0 "mvn clean install"
# EzChat 2.5.0
install FixedDev EzChat 48faa1c1464bc039ee378055b3e3947a94836087 "mvn clean install"

