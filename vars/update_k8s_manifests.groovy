// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Update the Kubernetes manifests with the new image tag.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def imageTag       = config.imageTag       ?: error("[ERROR] imageTag is required")
    def manifestsPath  = config.manifestsPath  ?: 'k8s'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName    = config.gitUserName    ?: 'Jenkins CI'
    def gitUserEmail   = config.gitUserEmail   ?: 'jenkins@example.com'

    echo "[INFO] Updating Kubernetes image tags to: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        sh """
            echo "[INFO] Replacing image tags in ${manifestsPath}/*.yaml..."
            find ${manifestsPath} -type f -name "*.yaml" -exec sed -i -E 's|(image:\\s+[\\w./-]+):[^\\s"]+|\\1:${imageTag}|g' {} +
        """

        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()
        if (hasChanges == "changed") {
            echo "[INFO] Git changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[INFO] Update Docker image tags to ${imageTag} [ci skip]"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@`git config --get remote.origin.url | sed 's|https://||'`
                git push origin HEAD
            """
        } else {
            echo "[SUCCESS] No changes to commit. Image tags already up-to-date."
        }
    }
}
