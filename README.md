# trainingApp
This is the source code of the trainingApp an abas Essentials Application build on the abas Essentials SDK.

## General setup
Add a gradle.properties file to your $GRADLE_USER_HOME.

```
#If you use a proxy add it here
systemProp.http.proxyHost=webproxy.abas.de
systemProp.http.proxyPort=8000
systemProp.https.proxyHost=webproxy.abas.de
systemProp.https.proxyPort=8000

esdkSnapshotURL=https://abasartifactory.jfrog.io/artifactory/abas.esdk.snapshots/
esdkReleaseURL=https://abasartifactory.jfrog.io/artifactory/abas.esdk.releases/
nexusUser=<extranet username>
nexusPassword=<extranet password>
```

To create the common development setup for IntelliJ run
```shell
./gradlew idea
```

To create the common development setup for Eclipse run
```shell
./gradlew eclipse
```

Or just import the project as a Gradle project 

## Installation
To install the project make sure you are running the docker-compose.yml file or else change the gradle.properties file accordingly to use another erp client (you will still need a nexus server, but it can of course also be installed in your erp installation or elsewhere as long as it is configured in the gradle.properties file).

To use the project's docker-compose.yml file, in the project's root directory run:
```shell
docker login --username <extranet user> --password <extranet password> abasartifactory.jfrog.io
docker-compose up
```

Once it's up, initialize the gradle.properties with the appropriate values by running:
```shell
./initGradleProperties.sh
```

Now, you need to load all the $HOMEDIR/java/lib dependencies into the Nexus Server. This is only necessary once as long as the essentials_nexus container is not reinitialized. Run the following gradle command to upload the dependencies to the Nexus Server:
```shell
./gradlew publishHomeDirJars
```

Now you can install the project as follows:
```shell
./gradlew fullInstall
```
## Development
If you want to make changes to the project before installing you still need to run the docker-compose.yml file or at least have a Nexus Server set up to work with.

Then run
```shell
./gradlew publishHomeDirJars
```

You also need to run
```shell
./gradlew publishClientDirJars
./gradlew idea eclipse
```
to upload the $MANDANTDIR/java/lib dependencies to the Nexus Server and set eclipse up to work with the uploaded dependencies.

After that the code should compile both with gradle and in IntelliJ/Eclipse and you are set up to work on the code or resource files as you want.
