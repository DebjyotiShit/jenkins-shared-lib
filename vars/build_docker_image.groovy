// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Build the docker image.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def imageName = config.imageName ?: error("[ERROR] 'imageName' is required.")
    def imageTag = config.imageTag ?: 'latest'
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context = config.context ?: '.'

    echo "[INFO] Building Docker image..."
    echo "[INFO] Image Name   : ${imageName}"
    echo "[INFO] Image Tag    : ${imageTag}"
    echo "[INFO] Dockerfile   : ${dockerfile}"
    echo "[INFO] Build Context: ${context}"

    try {
        sh """
            docker build -t ${imageName}:${imageTag} -t ${imageName}:latest -f ${dockerfile} ${context}
        """
        echo "[SUCCESS] Docker image '${imageName}:${imageTag}' built successfully."
    } catch (error) {
        echo "[ERROR] Docker build failed: ${error.message}"
        currentBuild.result = 'FAILURE'
        throw error
    }
}
