import de.abas.esdk.gradle.EsdkConfig

buildscript {
	val esdkSnapshotURL: String by project
	val esdkReleaseURL: String by project
	val nexusUser: String by project
	val nexusPassword: String by project
	val versionString = "0.14.5-SNAPSHOT"
	if ((versionString as String).endsWith("-SNAPSHOT")) {
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
			classpath("de.abas.esdk:gradlePlugin:$versionString")
		}
	} else {
		repositories {
			maven {
				url = uri("https://plugins.gradle.org/m2/")
			}
		}
		dependencies {
			classpath("esdk:gradlePlugin:$versionString")
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
val NEXUS_PASSWORD: String by project

val ABAS_HOMEDIR: String by project
val ABAS_CLIENTDIR: String by project
val ABAS_CLIENTID: String by project

val EDP_HOST: String by project
val EDP_PORT: String by project
val EDP_USER: String by project
val EDP_PASSWORD: String by project

val SSH_HOST: String by project
val SSH_PORT: String by project
val SSH_USER: String by project
val SSH_PASSWORD: String by project
val SSH_KEY: String by project

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
	maven { url = uri("http://$NEXUS_HOST:$NEXUS_PORT/nexus/content/repositories/$NEXUS_NAME") }
	maven { url = uri("http://$NEXUS_HOST:$NEXUS_PORT/nexus/content/repositories/$NEXUS_NAME-SNAPSHOT") }
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

val esdk: EsdkConfig = extensions["esdk"] as EsdkConfig

esdk.apply {
	app.apply {
		name = "trainingApp"
		vendorId = "ag"
		appId = "train"
		shared = false
		infosystems = listOf("IS.OW1.TESTINFO")
		tables = listOf("TestDb", "Teil")
		data = listOf("data.json")
		keys = listOf("2738")
		enums = listOf("Importfileformat", "Importfileformat2", "Importfileformat3", "ThenSteps")
		namedTypes = listOf("TestRealNamedType")
		screens = mapOf("Customer:Customer" to listOf("A"), "Sales:BlanketOrder" to listOf("A"), "Operation:Operation" to listOf("A"), "77" to listOf("A"), "Pricing:Pricing" to listOf("A"), "TestDb:TestStructure" to listOf("A"))
		advancedScreens = mapOf("75" to listOf("A"))
		essentialsVersions = listOf("2017r1n00-2017r4n16", "2018r1n00-2018r4n16", "2019r1n00-2019r4n16")
		preconditions = listOf("workDirs=ow1")
		languages = "DEA"
		workdirs = listOf("ow1", "owbi")
	}
	abas.apply {
		homeDir = ABAS_HOMEDIR
		clientDir = ABAS_CLIENTDIR
		clientId = ABAS_CLIENTID
		edpHost = EDP_HOST
		edpPort = EDP_PORT.toInt()
		edpUser = EDP_USER
		edpPassword = EDP_PASSWORD
	}
	nexus.apply {
		nexusHost = NEXUS_HOST
		nexusPort = NEXUS_PORT.toInt()
		nexusRepoName = NEXUS_NAME
		nexusPassword = NEXUS_PASSWORD
	}
	ssh.apply {
		host = SSH_HOST
		port = SSH_PORT.toInt()
		user = SSH_USER
		password = SSH_PASSWORD
		key = SSH_KEY
		timeout = 15000
	}
	installType = "SSH"
}

gradle.taskGraph.whenReady {
	tasks {
		"esdkAppDocumentation"(org.asciidoctor.gradle.jvm.AsciidoctorTask::class) {
			attributes(mapOf("productVersion" to (version as String).split("-")[0]))
		}
	}
}

val provided by configurations
val licensing by configurations
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
				&& project.hasProperty("nexusUser") && project.hasProperty("nexusPassword")) {
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

	licensing("de.abas.esdk:client-api:0.0.11:all")

	runtime("de.abas.homedir:commons-collections:1.0.0")
	runtime("de.abas.homedir:abas-jfop-base:1.0.0")

	testImplementation("junit:junit:4.12")
	testImplementation("org.hamcrest:hamcrest-all:1.3")

	integTestImplementation("de.abas.homedir:abas-db-util:1.0.0")
	integTestImplementation("de.abas.homedir:abas-enums:1.0.0")
	integTestImplementation("de.abas.esdk.test.util:esdk-test-utils:0.0.2")

}

fun MavenArtifactRepository.withCredentials() {
	credentials {
		username = nexusUser
		password = nexusPassword
	}
}
