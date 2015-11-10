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

Install Brightspot Base:

    target/bin/grunt install-library --endpoint perfectsense/brightspot-base#feature/refactor

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
    npm run grunt

Install Brightspot Base:

    npm run grunt -- install-library --endpoint perfectsense/brightspot-base#feature/refactor
    
Run Brightspot Styleguide:

    npm run styleguide
