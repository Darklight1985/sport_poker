pipeline {
    agent { label 'jenkins_agent' }

    tools {
        git 'Default'
    }

    environment {
        GRADLE_OPTS = "-Xmx1024m"
        GIT_CREDENTIALS_ID = 'git'
    }

    stages {
//         stage('SSH Key Scanning') {
//             steps {
//                 sh 'ssh-keyscan github.com >> ~/.ssh/known_hosts'
//             }
//         }
//
//         stage('Checkout') {
//             steps {
//                 git url: "git@github.com:Darklight1985/sport_poker.git", branch: 'develop', credentialsId: "${GIT_CREDENTIALS_ID}"
//             }
//         }

stage('Debug') {
    steps {
        sh 'pwd'
        sh 'ls -la'
    }
}

        stage('Build') {
            steps {
              sh 'chmod +x ./gradlew'
              sh './gradlew build -x test'
            }
        }

        stage('Test') {
            steps {
                   sh './gradlew test'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying...'
                sh './deploy.sh'
            }
        }
    }

//    post {
  //      always {
  //          cleanWs()
 //       }
 //   }
}