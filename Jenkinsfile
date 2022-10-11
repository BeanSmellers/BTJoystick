pipeline {
    agent any
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
    }

    post {
        always {
            archiveArtifacts 'app/build/outputs/apk/**/*.apk'
            archiveArtifacts 'app/build/reports/tests/testDebugUnitTest/**/*.*'
        }
    }
}