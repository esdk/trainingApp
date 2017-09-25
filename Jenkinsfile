@Library('esdk-jenkins-lib@ESDK-319-store-abas-essentials-version-c') _

def version = ""
node {
	timestamps {
		ansiColor('xterm') {
			try {
				properties([parameters([
						string(name: 'ESDK_VERSION', defaultValue: '', description: 'Version of ESDK to use (if not same as project version, project version will be updated as well)'),
						string(name: 'BUILD_USER_PARAM', defaultValue: 'anonymous', description: 'User who triggered the build implicitly (through a commit in another project)'),
						string(name: 'ERP_VERSION', defaultValue: '2016r4n13', description: 'abas Essentials version')
					])
				])
				stage('Setup') {
					checkout scm
					sh "git clean -fd"
					sh "git reset --hard origin/$BRANCH_NAME"
					prepareEnv()
					rmDirInMavenLocal '​de/abas/esdk'
					shInstallDockerCompose()
					currentBuild.description = "ERP Version: ${params.ERP_VERSION}"
					initGradleProps()
					showGradleProps()
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
					sleep 30
				}
				stage('Installation') {
					shGradle("checkPreconditions -x importKeys")
					shGradle("publishHomeDirJars")
					shGradle("fullInstall")
				}
				stage('Verify') {
					shGradle("verify")
				}
				onMaster {
					stage('Publish') {
						shGradle("createAppJar")
						shGradle("publish")
					}
				}
				currentBuild.description = currentBuild.description + " => successful"
			} catch (any) {
				any.printStackTrace()
				currentBuild.result = 'FAILURE'
				currentBuild.description = currentBuild.description + " => failed"
				throw any
			} finally {
				shDockerComposeCleanUp()

				junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
				archiveArtifacts 'build/reports/**'

				slackNotify(currentBuild.result)
			}
		}
	}
}
