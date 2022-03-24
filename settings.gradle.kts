pluginManagement {
    val esdkSnapshotURL: String by settings
    val nexusUser: String by settings
    val nexusPassword: String by settings

    repositories {
        maven {
            url = uri("https://artifactory.abas.sh/artifactory/abas.maven-public/")
            content {
                includeGroupByRegex("de\\.abas\\..*")
                excludeGroup("de.abas.homedir")
                excludeGroup("de.abas.clientdir")
            }
        }
        maven {
            url = uri(esdkSnapshotURL)
            credentials {
                username = nexusUser
                password = nexusPassword
            }
            content {
                includeGroup("esdk")
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "trainingApp"
