pipeline {
    agent { label 'jenkins_agent' }

    tools {
        git 'Default'
    }

    environment {
        POKER_USER = credentials('poker-user')
        POKER_BASE_PASS = credentials('poker-base-pass')
        KEYCLOAK_DB_USER = credentials('keycloak-db-user')
        KEYCLOAK_DB_PASS = credentials('keycloak-db-pass')
        KEYCLOAK_ADMIN = credentials('keycloak-admin')
        KEYCLOAK_PASS = credentials('keycloak-pass')
        CLIENT_SECRET = credentials('client-secret')
        USER_CLIENT_SECRET = credentials('user-client-secret')
        KEYCLOAK_ADDRESS = credentials('keycloak-address')
        MINIO_USER = credentials('minio-user')
        MINIO_PASSWORD = credentials('minio-password')
        MINIO_ACCESS_KEY = credentials('minio-access-key')
        MINIO_SECRET_KEY = credentials('minio-secret-key')
        MINIO_ADDRESS = credentials('minio-address')
    }

    stages {
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
                sh 'chmod +x ./gradlew'
            }
        }

        stage('PreDeploy') {
            steps {
                sh """
                ls
                docker-compose up -d
                """
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build('sport_poker:latest')
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                ls
                docker-compose up -d
                """
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}