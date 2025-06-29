// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Update the Kubernetes manifests with the new image tag.
// -----------------------------------------------------------------------------    
def call(Map config = [:]) {
    def imageTag       = config.imageTag       ?: error("Image tag is required")
    def manifestsPath  = config.manifestsPath  ?: 'k8s'
    def gitCredentials = config.gitCredentials ?: 'gitCredentials'
    def gitUserName    = config.gitUserName    ?: 'DebjyotiShit'
    def gitUserEmail   = config.gitUserEmail   ?: 'debjyotishit8@gmail.com'

    echo "[INFO] Updating image tags to ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,  // Credentials ID for the Git repository
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        sh """
            echo "[INFO] Replacing image tags in ${manifestsPath}..."
            find ${manifestsPath} -type f -name "*.yaml" -exec sed -i -E 's|(image:\\s+[\\w./-]+):[\\w.-]+|\\1:${imageTag}|g' {} +
        """

        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()
        if (hasChanges == "changed") {
            echo "[INFO] Git changes found. Committing..."
            sh """
                git add ${manifestsPath}/*.yaml                                                                                                                     ─╯
                git commit -m "[INFO] Update Docker image tags to ${imageTag}"
                git remote set-url origin https://\$GIT_USERNAME:\$GIT_PASSWORD@\$\(git config --get remote.origin.url | sed 's|https://||')
                git push origin HEAD
            """
        } else {
            echo "[SUCCESS] No image changes to commit."
        }
    }
}
