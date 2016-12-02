# HappyBrackets #

HappyBrackets is an audio-focused library and toolkit for creative coding for the internet of things. Write Java code on your computer and deploy it straight to multiple remote devices, such as Raspberry Pis, CHIPs, or BeagleBones.

You are looking at the git repository for the entire HappyBrackets source code. If you are a creative coder and you want to make beautiful projects with HappyBrackets, you don’t need to be here. Head over to the getting started documentation on the Github Wiki [here](https://github.com/orsjb/HappyBrackets/wiki/Getting-Started). If you want to get under the hood and do evil mad science, then read on.

## Developing on HappyBrackets core libraries ##

The core HappyBrackets project lives inside the folder HappyBrackets next to this file. There are two other IntelliJ projects, one for the IntelliJ Plugin development, and one set of tutorials and code tasks for users of HappyBrackets, located in the Distribution folder. The following getting started instructions refer to the main HappyBrackets project. Navigate there first.

Happy brackets builds with Gradle. All you need to do to get started is call:
* ```./gradlew idea``` on Unix like platforms such as linux or OS X
* ```gradlew idea``` on Windows

This will give you a ready to go IDEA project configured with builds.

Once you've built your IDEA project you can open it up.

You should be prompted that there is an unlinked Gradle project. Say yes to linking this project. I recommend linking to the root project (the root directory). Now you can add the Gradle build task to you launch configuration. This will ensure that all dependencies are setup and build our project.

Calling `gradle deploy` will do a full build of the project, including copying generated compiled files and docs to the other project folders: the IntelliJ Plugin development project folder and to various locations within the Distribution folder. You can rebuild the IntelliJ Plugin from within that project. 

## Credits ##

HappyBrackets was originally conceptualised by Ollie Bown and has been developed by Ollie Bown, Sam Ferguson, Sam Gillespie and Oliver Coleman. This project builds on various open source libraries, like:

* NetUtil.
* pi4j.
* nanohttpd.
* Beads.

