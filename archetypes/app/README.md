# Installation

## Java Developers

Create a project using the Maven archetype:

    mvn archetype:generate -B \
        -DarchetypeRepository=https://artifactory.psdops.com/public \
        -DarchetypeGroupId=com.psddev \
        -DarchetypeArtifactId=cms-app-archetype \
        -DarchetypeVersion=3.1-SNAPSHOT \
        -DgroupId=com.example \
        -DartifactId=demo
        
Change into the newly created project directory:

    cd demo

Prepare the project:

    mvn generate-resources

(Optional) Install Brightspot Base and any other libraries:

    target/bin/grunt install-library --endpoint perfectsense/brightspot-base#master

Build and run the project:

    mvn -P run clean package cargo:run
    
(Optional) Run Brightspot Styleguide:

    target/bin/styleguide

## Front End Developers

Create a project using the archetype:

    mvn archetype:generate -B \
        -DarchetypeRepository=https://artifactory.psdops.com/public \
        -DarchetypeGroupId=com.psddev \
        -DarchetypeArtifactId=cms-app-archetype \
        -DarchetypeVersion=3.1-SNAPSHOT \
        -DgroupId=com.example \
        -DartifactId=demo

Change into the newly created project directory:

    cd demo
    
Prepare the project:

    npm install

(Optional) Install Brightspot Base and any other libraries:

    npm run grunt -- install-library --endpoint perfectsense/brightspot-base#master
    
Build the project:

    npm run grunt

Run Brightspot Styleguide:

    npm run styleguide
