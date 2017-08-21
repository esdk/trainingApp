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
					if (params.ESDK_VERSION.matches("[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?") && version != params.ESDK_VERSION) {
						wrap([$class: 'BuildUser']) {
							println(BUILD_USER)
							justReplace(version, params.ESDK_VERSION, "gradle.properties.template")
							if (ESDK_VERSION.endsWith("-SNAPSHOT")) {
								shGitCommitSnapshot("gradle.properties.template", params.ESDK_VERSION, "$BUILD_USER")
							} else {
								shGitCommitRelease("gradle.properties.template", params.ESDK_VERSION, "$BUILD_USER", BUILD_ID)
								sh 'git branch --force release HEAD'
							}
							withCredentials([usernamePassword(credentialsId: '44e7bb41-f9fc-483f-9e66-9751c0163d37', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USER')]) {
								shGitPushIntoMaster("https://$GIT_USER:$GIT_PASSWORD@github.com/Tschasmine/trainingApp.git", params.ESDK_VERSION)
							}
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
					stage('IntegTest') {
						shDockerComposeDown()
						shDockerComposeUp()
						shDocker("cp build/installer/installer-${version}.zip erp:/abas/erp1")
						shDocker("cp build/libs/trainingApp-standalone-app.jar erp:/abas/erp1")
						shDocker("exec --user root -t erp unzip -o /abas/erp1/installer-${version}.zip -d /abas/erp1")
						shDocker("exec --user root -t erp chown -R s3 /abas/erp1/installer-${version}")
						shDocker("exec -t erp sh -c 'cd /abas/erp1 && eval \$(sh denv.sh) && cd /abas/erp1/esdk-installer-${version}/bin && ./esdk-installer -a /abas/erp1/trainingApp-standalone-app.jar -p sy")
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
