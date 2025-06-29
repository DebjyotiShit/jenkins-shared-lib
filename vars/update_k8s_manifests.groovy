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
            echo "[INFO] Replacing tag for image: ${imageName} → ${imageName}:${newTag}"
            sh """
                find ${manifestsPath} -type f -name '*.yaml' -exec \\
                    sed -i -E 's|(image:\\s*${imageName}):[[:alnum:]\\._\\-]+|\\1:${newTag}|g' {} +
            """
        }

        // Check if any changes were made
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
                git commit -m "[AUTO] Update multiple image tags: ${replacements.collect{ k,v -> "$k:$v" }.join(', ')}"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                git push origin HEAD:main
            """
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}