// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Update the Kubernetes manifests with the new image tag.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def replacements = config.replacements ?: error("[ERROR] 'replacements' map is required")
    def manifestsPath = config.manifestsPath ?: 'k8s'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Jenkins CI'
    def gitUserEmail = config.gitUserEmail ?: 'jenkins@example.com'

    echo "[INFO] Updating Kubernetes image tags with multiple values: ${replacements}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        // Loop through each replacement and update manifests
        replacements.each { imageName, newTag ->
            echo "[INFO] Replacing tag for image: ${imageName} → ${newTag}"

            // Use find + sed to replace only in relevant files
            sh """
                find ${manifestsPath} -type f -name '*.yaml' -exec \\
                    sed -i -E 's|(image:\\s*${imageName}):[^ ]+|\\1:${newTag}|g' {} +
            """
        }

        // Check if any changes were made
        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()

        if (hasChanges == "changed") {
            echo "[INFO] Changes detected. Committing and pushing..."

            dir(manifestsPath) {
                sh """
                    git add .
                    git commit -m "[AUTO] Updated Kubernetes image tags"
                    git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                    git push origin HEAD:main
                """
            }
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}