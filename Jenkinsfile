pipeline {
    agent { label 'jenkins_agent' }

    tools {
        git 'Default'
    }


    environment {
        // Определите здесь переменные окружения, которые могут понадобиться для сборки
        GRADLE_OPTS = "-Xmx1024m"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/develop']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/your-repo/your-project.git']]])
            }
        }

        stage('Build') {
            steps {
                sh '''
                    ./gradlew build -x test
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    ./gradlew test
                '''
            }
        }

        stage('Deploy') {
            // Добавьте шаги для деплоя, если это необходимо
            // Например:
            // sshPublisher(publisher: [allowAllHosts: true], recipients: '', source: '.', target: 'build/libs/your-artifact.jar')
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}