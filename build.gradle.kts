buildscript {
    val esdkSnapshotURL: String by project
    val esdkReleaseURL: String by project
    val nexusUser: String by project
    val nexusPassword: String by project
    if ((version as String).endsWith("-SNAPSHOT")) {
        repositories {
            mavenLocal()
            maven {
                url = uri(esdkSnapshotURL)
                credentials {
                    username = nexusUser
                    password = nexusPassword
                }
            }
            maven {
                url = uri(esdkReleaseURL)
                credentials {
                    username = nexusUser
                    password = nexusPassword
                }
            }
        }
        dependencies {
            classpath("de.abas.esdk:gradlePlugin:$version")
        }
    } else {
        repositories {
            maven {
                url = uri("https://plugins.gradle.org/m2/")
            }
        }
        dependencies {
            classpath("esdk:gradlePlugin:$version")
        }
    }
}

apply(plugin = "de.abas.esdk")

plugins {
    java
    `maven-publish`
}

val esdkSnapshotURL: String by project
val esdkReleaseURL: String by project
val publicReleaseURL: String by project
val nexusUser: String by project
val nexusPassword: String by project

val NEXUS_HOST: String by project
val NEXUS_PORT: String by project
val NEXUS_NAME: String by project

fun after2018(): Boolean {
    val erpVersion = System.getenv("ERP_VERSION")
    if (erpVersion == null || erpVersion == "") {
        return false
    }
    val majorVersion = Integer.parseInt(erpVersion.substring(0, 4))
    return majorVersion >= 2018
}


repositories {
    mavenLocal()
    maven {
        url = uri("http://$NEXUS_HOST:$NEXUS_PORT/nexus/content/repositories/$NEXUS_NAME")
        isAllowInsecureProtocol = true
    }
    maven {
        url = uri("http://$NEXUS_HOST:$NEXUS_PORT/nexus/content/repositories/$NEXUS_NAME-SNAPSHOT")
        isAllowInsecureProtocol = true
    }
    mavenCentral()
    maven {
        url = uri(esdkSnapshotURL)
        withCredentials()
    }
    maven {
        url = uri(esdkReleaseURL)
        withCredentials()
    }
    maven { url = uri(publicReleaseURL) }
}

gradle.taskGraph.whenReady {
    tasks {
        "esdkAppDocumentation"(org.asciidoctor.gradle.jvm.AsciidoctorTask::class) {
            attributes(mapOf("productVersion" to (version as String).split("-")[0]))
        }
    }
}

val provided by configurations
val integTestImplementation by configurations

val installer by configurations.creating {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

group = "de.abas.esdk.app"
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "de.abas.esdk.app"
            artifactId = "trainingApp"
            artifact(tasks.getByName("createAppJar"))
        }
    }
    repositories {
        if (project.hasProperty("esdkSnapshotURL") && project.hasProperty("esdkReleaseURL")
            && project.hasProperty("nexusUser") && project.hasProperty("nexusPassword")
        ) {
            if ((version as String).endsWith("-SNAPSHOT")) {
                maven {
                    url = uri(esdkSnapshotURL)
                    withCredentials()
                }
            } else {
                maven {
                    url = uri(esdkReleaseURL)
                    withCredentials()
                }
            }
        }
    }
}

tasks.register<Copy>("downloadInstaller") {
    dependsOn(installer)
    from(installer.singleFile)
    into(file("${project.buildDir}/installer/"))
}

dependencies {
    installer(group = "de.abas.esdk", name = "installer", version = (version as String), classifier = "", ext = "zip") {
        isChanging = true
    }

    provided("de.abas.homedir:log4j:1.0.0")
    provided("de.abas.homedir:jedp:1.0.0")
    provided("de.abas.homedir:abas-db-base:1.0.0")
    provided("de.abas.homedir:abas-jfop-runtime-api:1.0.0")
    provided("de.abas.homedir:abas-erp-common:1.0.0")

    implementation("de.abas.homedir:abas-axi2:1.0.0")
    implementation("de.abas.homedir:abas-axi:1.0.0")
    implementation("de.abas.homedir:abas-db-internal:1.0.0")
    implementation("de.abas.clientdir:abas-db:1.0.0-SNAPSHOT")
    implementation("de.abas.clientdir:abas-db-infosys:1.0.0-SNAPSHOT")
    if (after2018()) {
        implementation("de.abas.clientdir:abas-db-index:1.0.0-SNAPSHOT")
    }

    implementation("de.abas.esdk:client-api:0.0.11:all")

    implementation("de.abas.homedir:commons-collections:1.0.0")
    runtimeOnly("de.abas.homedir:abas-jfop-base:1.0.0")

    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-all:1.3")

    integTestImplementation("de.abas.homedir:abas-db-util:1.0.0")
    integTestImplementation("de.abas.homedir:abas-enums:1.0.0")
    integTestImplementation("de.abas.esdk.test.util:esdk-test-utils:0.0.4")
    integTestImplementation("org.slf4j:slf4j-log4j12:1.7.30")

}

fun MavenArtifactRepository.withCredentials() {
    credentials {
        username = nexusUser
        password = nexusPassword
    }
}
