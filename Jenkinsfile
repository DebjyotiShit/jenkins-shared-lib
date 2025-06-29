@Library('shared-lib') _

pipeline {
    agent any

    parameters {
        string(name: 'BACKEND_IMAGE_TAG', defaultValue: '', description: 'Tag for backend Docker image')
        string(name: 'FRONTEND_IMAGE_TAG', defaultValue: '', description: 'Tag for frontend Docker image')
    }

    stages {
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/DebjyotiShit/ClearCut.git',
                     branch: 'master',
                     credentialsId: 'gitCredentials'
            }
        }

        stage('Update K8s Image Tags') {
            steps {
                script {
                    def imageReplacements = [
                        "debjyoti08/clearcut_backend": params.BACKEND_IMAGE_TAG,
                        "debjyoti08/clearcut_frontend": params.FRONTEND_IMAGE_TAG
                    ]

                    update_k8s_manifests(
                        replacements: imageReplacements,
                        manifestsPath: 'k8s',
                        gitCredentials: 'gitCredentials',
                        gitUserName: 'DebjyotiShit',
                        gitUserEmail: 'debjyotishit8@gmail.com'
                    )
                }
            }
        }
    }

    post {
        success {
            echo "[SUCCESS] Image tags updated and pushed to GitHub."
        }
        failure {
            echo "[FAILURE] Failed to update image tags."
        }
    }
}