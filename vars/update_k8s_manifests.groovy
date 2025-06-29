// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Update the Kubernetes manifests with the new image tag.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def imageTag = config.imageTag ?: error("[ERROR] imageTag is required")
    def manifestsPath = config.manifestsPath ?: 'k8s'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Jenkins CI'
    def gitUserEmail = config.gitUserEmail ?: 'jenkins@example.com'

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
            find ${manifestsPath} -type f -name '*.yaml' -exec \\
                sed -i -E 's|(image:\\s+[[:alnum:]_\\./\\-]+):[[:alnum:]\\.\\-_]+|\\1:${imageTag}|g' {} +
        """

        def hasChanges = sh(script: """
            if git diff --quiet; then
                echo "no_changes"
            else
                echo "changes_detected"
            fi
        """, returnStdout: true).trim()

        if (hasChanges == "changes_detected") {
            echo "[INFO] Changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[AUTO] Update backend image tag to ${imageTag}"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                git push origin HEAD:master
            """
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}