@Library('esdk-jenkins-lib@master') _

def version = ""
String errorMessage = null

timestamps {
	ansiColor('xterm') {
		node {
			try {
				properties([parameters([
						string(name: 'ESDK_VERSION', defaultValue: '', description: 'Version of ESDK to use (if not same as project version, project version will be updated as well)'),
						string(name: 'BUILD_USER_PARAM', defaultValue: 'anonymous', description: 'User who triggered the build implicitly (through a commit in another project)'),
						string(name: 'ERP_VERSION', defaultValue: '2016r4n13', description: 'abas Essentials version')
					])
				])
				stage('Setup') {
					timeout(1) {
						checkout scm
						sh "git reset --hard origin/$BRANCH_NAME"
						sh "git clean -fd"
					}
					prepareEnv()
					rmDirInMavenLocal '​de/abas/esdk'
					currentBuild.description = "ERP Version: ${params.ERP_VERSION}"
					initGradleProps()
				}
				stage('Set version') {
					updateEssentialsAppVersion(params.ESDK_VERSION, 'gradle.properties.template', params.BUILD_USER_PARAM, 'github.com/Tschasmine/trainingApp.git')
					initGradleProps()
				}
				stage('Preparation') { // for display purposes
					withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
							passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
						shDocker('login intra.registry.abas.sh -u $MAVENUSER -p $MAVENPASSWORD')
					}
					withEnv(["ERP_VERSION=${params.ERP_VERSION}"]) {
						shDockerComposeUp()
					}
					waitForNexus()
				}
				stage('Installation') {
					shGradle("checkPreconditions")
					shGradle("fullInstall")
				}
				stage('Verify') {
					shGradle("verify")
				}
				onMaster {
					stage('Publish') {
						shGradle("publish -x fullInstall")
					}
					stage('Upload') {
						shGradle("packAbasApp -x createAppJar")
						if (!version.endsWith("SNAPSHOT")) {
							withAWS(credentials: '07d490a3-c053-4108-960f-458307e91742', region: "us-east-1") {
								s3Upload(
										bucket: "abas-app-releases",
										file: "build/abas-app/trainingApp-abasApp-${version}.zip",
										path: "trainingApp-abasApp-${version}.zip",
										pathStyleAccessEnabled: true,
										cacheControl: 'max-age=0',
										acl: 'Private'
								)
							}
						}
					}
				}
				currentBuild.description = currentBuild.description + " => successful"
			} catch (any) {
				any.printStackTrace()
				errorMessage = any.message
				currentBuild.result = 'FAILURE'
				currentBuild.description = currentBuild.description + " => failed"
				throw any
			} finally {
				shDockerComposeCleanUp()

				junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
				archiveArtifacts 'build/reports/**'
			}
		}
		try {
			onMaster {
				if (!version.endsWith("SNAPSHOT")) {
					stage('Test') {
						build job: 'esdk/abasAppTestBucketScan', parameters: [string(name: 'INSTALLER_VERSION', value: "$version")], wait: true
					}
				}
			}
		} catch (any) {
			any.printStackTrace()
			errorMessage = any.message
			currentBuild.result = 'FAILURE'
			currentBuild.description = currentBuild.description + " => failed"
			throw any
		} finally {
			String message = "ESDK version: '${params.ESDK_VERSION}'\nabas version: '${params.ERP_VERSION}'"
			if (null != errorMessage) {
				message += "\n${errorMessage}"
			}
			slackNotify(currentBuild.result, "esdk-bot", message)
		}
	}
}
