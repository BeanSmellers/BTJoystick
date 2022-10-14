def NEXT_VER = ''

pipeline {
    agent { label 'android' }
    parameters {
        booleanParam(name: 'DO_RELEASE', defaultValue: false, description: 'Create release on github')
    }

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
        stage('Pre-release') {
            steps {
                sh 'keytool -genkey -v -keystore signing-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias test-key -noprompt -storepass testing -keypass testing -dname "CN=poum.ca, OU=ID, O=poum, L=Test, S=Test, C=CA"'
                sh 'zipalign -v -p 4 ./app/build/outputs/apk/release/app-release-unsigned.apk app-aligned.apk 1>/dev/null'
                sh 'apksigner sign -ks signing-key.jks --ks-pass pass:testing --out app-signed.apk app-aligned.apk'
            }
        }
        stage('Release') {
            when {
                allOf {
                    branch 'main'
                    expression { return params.DO_RELEASE == true }
                }
            }
            steps {
                script {
                    def gradleVersion = sh(returnStdout: true, script: './gradlew -q printVersion 2>/dev/null || echo 0.0').trim()
                    def (confMaj, confMin) = gradleVersion.tokenize('.').collect { it.toInteger() }


                    // Gets the latest tag following the format M.m.?, if none found, return M.m.0
                    def currVersion = sh(returnStdout: true, script: """git fetch --tags && git describe --tags --abbrev=0 --match $confMaj.$confMin.* 2>/dev/null""").trim()
                    def (major, minor, patch) = [0, 0, 0]
                    if (currVersion) {
                        (major, minor, patch) = currVersion.tokenize('.').collect { it.toInteger() }
                        patch += 1  // Auto increment patch ver
                    } else {
                        currVersion = """${confMaj}.${confMin}.0"""
                        (major, minor, patch) = currVersion.tokenize('.').collect { it.toInteger() }
                    }
                    NEXT_VER = """${major}.${minor}.${patch}"""
                }


                echo "Creating release version ${NEXT_VER}..."
                withCredentials([string(credentialsId: 'github-token', variable: 'TOKEN')]) {
                    script {
                        def body = """{
                                    "tag_name": "${NEXT_VER}",
                                    "target_commitish": "main",
                                    "generate_release_notes": true
                                    }"""
                        def response = httpRequest httpMode: 'POST', requestBody: body,
                                customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                    url: "https://api.github.com/repos/BeanSmellers/BTJoystick/releases"
                        // Parse response json to get new release ID
                        def relJSON = readJSON text: response.content

                        echo "Uploading assets to release"
                        httpRequest contentType: 'APPLICATION_OCTETSTREAM', httpMode: 'POST',
                                customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                uploadFile: './app/build/outputs/apk/release/app-release-unsigned.apk',
                                multipartName: 'app-release.apk',
                                url: "https://uploads.github.com/repos/BeanSmellers/BTJoystick/releases/${relJSON['id']}/assets?name=app-release.apk"

                        httpRequest contentType: 'APPLICATION_OCTETSTREAM', httpMode: 'POST',
                                customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                uploadFile: './app/build/outputs/apk/debug/app-debug.apk',
                                multipartName: 'app-debug.apk',
                                url: "https://uploads.github.com/repos/BeanSmellers/BTJoystick/releases/${relJSON['id']}/assets?name=app-debug.apk"

                        // Upload signed release apk
                        httpRequest contentType: 'APPLICATION_OCTETSTREAM', httpMode: 'POST',
                                customHeaders: [[name: 'Authorization', value: "Bearer ${TOKEN}"]],
                                uploadFile: './app-signed.apk',
                                multipartName: 'app-debug.apk',
                                url: "https://uploads.github.com/repos/BeanSmellers/BTJoystick/releases/${relJSON['id']}/assets?name=app-signed.apk"
                    }
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts 'app/build/outputs/apk/**/*.apk'
            archiveArtifacts 'app-signed.apk'
        }
    }
}