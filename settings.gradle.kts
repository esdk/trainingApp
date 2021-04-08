pluginManagement {
    repositories {
        maven {
            url = uri("https://artifactory.abas.sh/artifactory/abas.maven-public/")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "trainingApp"
