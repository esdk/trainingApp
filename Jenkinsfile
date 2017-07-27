@Library('esdk-jenkins-lib@master') _

def version = ""
node {
	timestamps {
		ansiColor('xterm') {
			try {
				properties([parameters([string(defaultValue: '""', description: 'Version of ESDK to use (if not same as project version, project version will be updated as well)', name: 'ESDK_VERSION')])])
				stage('Setup') {
					prepareEnv()
					checkout scm
					shInstallDockerCompose()
					initGradleProps()
					showGradleProps()
				}
				stage('Preparation') { // for display purposes
					withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
							passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
						shDocker('login intra.registry.abas.sh -u $MAVENUSER -p $MAVENPASSWORD')
					}
					shDockerComposeUp()
					sleep 30
				}
				stage('Set version') {
					shGradle("--version")
					version = readVersion()
					String esdkVersion = (String) ESDK_VERSION
					println(esdkVersion)
					if (!esdkVersion.empty() || version != esdkVersion) {
						wrap([$class: 'BuildUser']) {
							println(BUILD_USER)
							justReplace(version, esdkVersion, "gradle.properties")
							if (ESDK_VERSION.endsWith("-SNAPSHOT")) {
								shGitCommitSnapshot("gradle.properties", esdkVersion, "$BUILD_USER")
							} else {
								shGitCommitRelease("gradle.properties", esdkVersion, "$BUILD_USER", BUILD_ID)
							}
						}
					}
				}
				stage('Installation') {
					shGradle("checkPreconditions")
					shGradle("publishHomeDirJars")
					shGradle("fullInstall")
				}
				stage('Build') {
					shGradle("verify")
				}
				onMaster {
					stage('Publish') {
						shGradle("createAppJar")
						shGradle("publish")
					}
				}
			} catch (any) {
				any.printStackTrace()
				currentBuild.result = 'FAILURE'
				throw any
			} finally {
				shDockerComposeCleanUp()

				archiveArtifacts 'build/reports/**'

				slackNotify(currentBuild.result)
			}
		}
	}
}
