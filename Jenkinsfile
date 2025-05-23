pipeline {
    agent { label 'jenkins_agent' }

    tools {
        git 'Default'
    }

    environment {
        POKER_USER = credentials('poker-user') // Глобальная переменная
        POKER_BASE_PASS = credentials('poker-base-pass')
        KEYCLOAK_DB_USER = credentials('keycloak-db-user')
        KEYCLOAK_DB_PASS = credentials('keycloak-db-pass')
        KEYCLOAK_ADMIN = credentials('keycloak-admin')
        KEYCLOAK_PASS = credentials('keycloak-pass')
        CLIENT_SECRET = credentials('client-secret')
        USER_CLIENT_SECRET = credentials('user-client-secret')
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
//                 git url: "git@github.com:Darklight1985/sport_poker.git", branch: 'develop', credentialsId: "git"
//             }
//         }

          stage('Debug') {
               steps {
                   sh 'pwd'
                 sh 'ls -la'
               }
          }

                  stage('Check Docker') {
                      steps {
                          sh 'docker --version'
                          sh 'docker info'
                      }
                  }

         stage('Prepare Environment') {
                    steps {
                        script {
                            sh 'chmod +x ./gradlew'
                        }
                    }
         }

        stage('Build') {
            steps {
                                    script {
              sh './gradlew build -x test'
            }
            }
        }

//         stage('Test') {
//             steps {
//                                     script {
//                    sh './gradlew test'
//                    }
//             }
//         }

        stage('Build Docker Image') {
            steps {
                script {
                    // Убедитесь, что Docker установлен и доступен на агенте Jenkins
                    docker.build('sport_poker:latest')

                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh """
                    ls
                    docker-compose up -d
                    """
                }
            }
        }
    }

//    post {
  //      always {
  //          cleanWs()
 //       }
 //   }
}