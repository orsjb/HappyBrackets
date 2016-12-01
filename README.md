# HappyBrackets #
Write Java code on your computer and deploy it straight to multiple remote devices, such as Raspberry Pis, CHIPs, or BeagleBones.

## Overview ##


## Using HappyBrackets in your project ##



## Developing on HappyBrackets core libraries ##

### Getting Setup ###
Happy brackets builds with Gradle. All you need to do to get started is call:
* ```./gradlew idea``` on Unix like platforms such as linux or OS X
* ```gradlew idea``` on Windows

This will give you a ready to go IDEA project configured with builds.

Once you've built your IDEA project you can open it up :)

You should be prompted that there is an unlinked Gradle project. Say yes to linking this project. I recommend linking to the root project (the root directory). Now you can add the Gradle build task to you launch configuration. This will ensure that all dependencies are setup and build our project.
