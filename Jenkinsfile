@Library('esdk-jenkins-lib@master') _

def version = ""
node {
	timestamps {
		ansiColor('xterm') {
			try {
				properties([parameters([
						string(defaultValue: '', description: 'Version of ESDK to use (if not same as project version, project version will be updated as well)', name: 'ESDK_VERSION'),
						string(defaultValue: 'anonymous', description: 'User who triggered the build implicitly (through a commit in another project)', name: 'BUILD_USER_PARAM')
					])
				])
				stage('Setup') {
					prepareEnv()
					checkout scm
					sh "git clean -f"
					sh "git reset --hard origin/$BRANCH_NAME"
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
					println("version: $version")
					println("esdkVersion: $params.ESDK_VERSION")
					if (params.ESDK_VERSION.matches("[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?") && (version != params.ESDK_VERSION)) {
						println("Builduser: ${params.BUILD_USER_PARAM}")
						justReplace(version, params.ESDK_VERSION, "gradle.properties.template")
						if (ESDK_VERSION.endsWith("-SNAPSHOT")) {
							shGitCommitSnapshot("gradle.properties.template", params.ESDK_VERSION, params.BUILD_USER_PARAM)
						} else {
							shGitCommitRelease("gradle.properties.template", params.ESDK_VERSION, params.BUILD_USER_PARAM, env.BUILD_ID)
							sh 'git branch --force release HEAD'
						}
						withCredentials([usernamePassword(credentialsId: '44e7bb41-f9fc-483f-9e66-9751c0163d37', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USER')]) {
							shGitPushIntoMaster("https://$GIT_USER:$GIT_PASSWORD@github.com/Tschasmine/trainingApp.git", params.ESDK_VERSION)
						}
					}
				}
				stage('Installation') {
					shGradle("checkPreconditions -x importKeys")
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
