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
        // Set Git user info
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        // Loop through each replacement and update manifests
        replacements.each { imageName, newTag ->
            echo "[INFO] Replacing tag for image: ${imageName} → ${newTag}"

            // Print matching files
            sh """
                echo "[DEBUG] Files containing ${imageName}:"
                find ${manifestsPath} -type f -name '*.yaml' -exec grep -l '${imageName}' {} \\;
            """

            // Replace image tag
            sh """
                find ${manifestsPath} -type f -name '*.yaml' -exec \\
                    sed -i -E 's|(image:\\s*)(${imageName}):([^ /]+)|\\1\\2:${newTag}|g' {} +
            """
        }

        // Show diff for debugging
        sh "git diff"

        // Check for changes
        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()

        if (hasChanges == "changed") {
            echo "[INFO] Changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[CI/CD] Updated Kubernetes image tags"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                git push origin HEAD:master
            """
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}