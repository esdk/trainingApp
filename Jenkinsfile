@Library('esdk-jenkins-lib@master') _

String version = ""
String errorMessage = null
String defaultErpVersion = "2017r4n16p39"

timestamps {
	ansiColor('xterm') {
		node {
			try {
				timeout(activity: true, time: 20) {
					properties([parameters([
							string(name: 'ESDK_VERSION', defaultValue: '', description: 'Version of ESDK to use (if not same as project version, project version will be updated as well)'),
							string(name: 'BUILD_USER_PARAM', defaultValue: 'anonymous', description: 'User who triggered the build implicitly (through a commit in another project)'),
							string(name: 'ERP_VERSION', defaultValue: defaultErpVersion, description: 'abas Essentials version')
					])
					])
					stage('Setup') {
						timeout(1) {
							checkout scm
							sh returnStatus: true, script: "sudo rm -rf logs"
							sh "git reset --hard origin/$BRANCH_NAME"
							sh 'git clean -fd'
							sh 'mkdir logs'
						}
						gitSetUser()
						prepareEnv()
						rmDirInMavenLocal 'de/abas/esdk'
						currentBuild.description = "ERP version: ${params.ERP_VERSION}"
						initGradleProps()
					}
					stage('Set version') {
						updateEssentialsAppVersion(params.ESDK_VERSION, 'gradle.properties.template', params.BUILD_USER_PARAM, 'git@github.com:esdk/trainingApp.git')
						initGradleProps()
						version = readVersion()
						currentBuild.description += ", ESDK version: $version"
					}
					if (currentVersionIsReleaseButNoNewVersionGiven(version, params.ESDK_VERSION)) {
						echo "It seems a release build is currently in progress. Skipping this build."
						currentBuild.result = 'ABORTED'
						return
					}
					stage('Preparation') { // for display purposes
						setProperCommandForDockerCompose(params.ERP_VERSION)
						withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
								passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
							shDocker('login sdp.registry.abas.sh -u $MAVENUSER -p $MAVENPASSWORD')
						}
						withEnv(["ERP_VERSION=${params.ERP_VERSION}"]) {
							shDockerCompose("up -d --build")
						}
						waitForNexus(2, "localhost", "8090", 10, 10, "admin", "admin123")
					}
					stage('Installation') {
						shGradle("checkPreconditions")
						shGradle("createAppJar")
					}
					stage('Verify') {
						try {
							shGradle("verify")
						} finally {
							junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
							archiveArtifacts 'build/reports/**'
						}
					}
					onMaster {
						stage('Upload') {
							shGradle("packEsdkApp -x checkForSnapshot")
							shGradle("publish -x createAppJar")
						}
					}
					currentBuild.description = currentBuild.description + " => successful"
				}
			} catch (any) {
				any.printStackTrace()
				errorMessage = any.message
				currentBuild.result = 'FAILURE'
				currentBuild.description = currentBuild.description + " => failed"
				throw any
			} finally {
				for (line in shReturnStdoutTrimmed("docker exec -t erp-train sh -c 'ls -1 /abas/erp/*.FEHL'").split("\n")) {
					archiveFileFromContainers("/abas/erp", line.trim())
				}
				shDockerComposeCleanUp()
				dockerPruneWhenSpaceLessThan(10)

				slackNotify(currentBuild.result, 'esdk-bot', currentBuild.description)
			}
		}
	}
}

def currentVersionIsReleaseButNoNewVersionGiven(String currentVersion, String newVersion) {
	return !currentVersion.endsWith("SNAPSHOT") && (null == newVersion || "" == newVersion)
}

private void setProperCommandForDockerCompose(String image) {
	String[] notUpdatedImages = ["2018r4n03", "2018r4n04", "2018r4n04p01", "2018r4n05", "2018r4n06", "2018r4n07", "2018r4n08", "2018r4n09", "2018r4n10", "2018r4n11", "2018r4n12", "2018r4n13"]
	if (notUpdatedImages.contains(image)) {
		env.RUNCOMMAND_PATH="/data/"
	}
}
