pluginManagement {
    val esdkSnapshotURL: String by settings
    val nexusUser: String by settings
    val nexusPassword: String by settings

    repositories {
        maven {
            url = uri("https://artifactory.abas.sh/artifactory/abas.maven-public/")
        }
        maven {
            url = uri(esdkSnapshotURL)
            credentials {
                username = nexusUser
                password = nexusPassword
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "trainingApp"
