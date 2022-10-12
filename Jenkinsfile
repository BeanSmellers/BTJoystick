def NEXT_VER = ''
def MSG_JSON

pipeline {
    agent { label 'android' }
    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Release') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def gradleVersion = sh(returnStdout: true, script: './gradlew -q printVersion 2>/dev/null || echo 0.0').trim()
                    def (confMaj, confMin) = gradleVersion.tokenize('.').collect { it.toInteger() }


                    // Gets the latest tag following the format M.m.?, if none found, return M.m.0
                    def currVersion = sh(returnStdout: true, script: """git describe --tags --abbrev=0 --match $confMaj.$confMin.* 2>/dev/null || echo $confMaj.$confMin.0""").trim()
                    def (major, minor, patch) = currVersion.tokenize('.').collect { it.toInteger() }
                    patch += 1  // Auto increment patch ver


                    NEXT_VER = """$major.$minor.$patch"""
                }

                echo "Creating release version ${NEXT_VER}"

                // Get current latest release
                withCredentials([string(credentialsId: 'github-token', variable: 'TOKEN')]) {
                    script {
                        def output = sh(returnStdout: true, script: 'curl https://api.github.com/repos/BeanSmellers/BTJoystick/releases/latest')
                        def latestJSON = readJSON text: output
                        def lastRelVer = latestJSON['tag_name']

                        echo """Current latest release version is: $lastRelVer"""

                        echo "Auto-generating release message..."
                        def body = """{
                                            "tag_name": "${NEXT_VER}",
                                            "target_commitish": "main",
                                            "previous_tag_name": "${lastRelVer}"
                                            }"""
                        def msgOut = httpRequest httpMode: 'POST', requestBody: body,
                                        customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                        url: "https://api.github.com/repos/BeanSmellers/BTJoystick/releases/generate-notes"
                        MSG_JSON = readJSON text: msgOut.content
                    }
                }

                echo "Creating release..."
                withCredentials([string(credentialsId: 'github-token', variable: 'TOKEN')]) {
                    script {
                        def body = """{
                                    "tag_name": "${NEXT_VER}"
                                    "target_commitish": "main",
                                    "name": "${MSG_JSON['name']}",
                                    "body": "${escaped_msg}"
                                    }"""
                        httpRequest httpMode: 'POST', requestBody: body,
                                customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                    url: "https://api.github.com/repos/BeanSmellers/BTJoystick/releases"
                    }
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts 'app/build/outputs/apk/**/*.apk'
        }
    }
}