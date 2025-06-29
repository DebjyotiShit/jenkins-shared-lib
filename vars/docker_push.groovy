// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Push the docker image to the remote registry.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def imageName   = config.imageName ?: error("[ERROR] 'imageName' is required.")
    def imageTag    = config.imageTag ?: 'latest'
    def credentials = config.credentials ?: 'docker-hub-credentials'

    echo "[INFO] Preparing to push Docker image..."
    echo "[INFO] Image: ${imageName}:${imageTag}"
    echo "[INFO] Credentials ID: ${credentials}"

    try {
        withCredentials([usernamePassword(
            credentialsId: credentials,
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sh """
                echo "[INFO] Logging in to Docker registry..."
                echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USERNAME" --password-stdin

                echo "[INFO] Pushing ${imageName}:${imageTag}..."
                docker push ${imageName}:${imageTag}

                echo "[INFO] Pushing ${imageName}:latest..."
                docker push ${imageName}:latest
            """
        }

        echo "[SUCCESS] Docker image pushed successfully."

    } catch (error) {
        echo "[ERROR] Failed to push Docker image: ${error.message}"
        currentBuild.result = 'FAILURE'
        throw error
    }
}
