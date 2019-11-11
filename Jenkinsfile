@Library('esdk-jenkins-lib@master') _

def version = ""
String errorMessage = null
String defaultErpVersion = "2017r4n16p27"

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
					stage('Preparation') { // for display purposes
						withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
								passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
							shDocker('login sdp.registry.abas.sh -u $MAVENUSER -p $MAVENPASSWORD')
						}
						withEnv(["ERP_VERSION=${params.ERP_VERSION}"]) {
							shDockerComposeUp()
						}
						setupHybridTenant("d72216db-346d-499f-97f7-19b589c412bd", 6569, 2214)
						waitForNexus(2, "localhost", "8090", 10, 10, "admin", "admin123")
					}
					stage('Installation') {
						shGradle("checkPreconditions")
						shGradle("createAppJar")
						registerAppDevVersion('trainingApp', 'train', version)
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
							if (!version.endsWith("SNAPSHOT")) {
								releaseAppVersion("trainingApp", "train", version)
								def esdkApp = sh returnStdout: true, script: "ls build/esdk-app/ | grep 'esdkApp-$version'"
								esdkApp = esdkApp.trim()
								withAWS(credentials: 'e4ec24aa-35e1-4650-a4bd-6d9b06654e9b', region: "us-east-1") {
									s3Upload(
											bucket: "abas-apps",
											file: "build/esdk-app/$esdkApp",
											path: "trainingApp-esdkApp-${version}.zip",
											pathStyleAccessEnabled: true,
											cacheControl: 'max-age=0',
											acl: 'Private'
									)
								}
								build job: 'esdk/esdkAppTestBucketScan', parameters: [string(name: 'INSTALLER_VERSION', value: "$version")], wait: false
							}
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
				stopHybridTenant()
				shDockerComposeCleanUp()
				dockerPruneWhenSpaceLessThan(10)

				slackNotify(currentBuild.result, 'esdk-bot', currentBuild.description)
			}
		}
	}
}
