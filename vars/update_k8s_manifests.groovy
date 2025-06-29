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

    echo "[INFO] Updating Kubernetes image tags: ${replacements}"

    withCredentials([usernamePassword(credentialsId: gitCredentials)]) {
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        replacements.each { imageName, newTag ->
            echo "[DEBUG] Looking for image: ${imageName}"
            sh """
                echo "[DEBUG] Searching for files containing '${imageName}':"
                find ${manifestsPath} -type f -name '*.yaml' -exec grep -l '${imageName}' {} \\;
            """

            echo "[INFO] Replacing tag for image: ${imageName} → ${newTag}"
            sh """
                find ${manifestsPath} -type f -name '*.yaml' -exec \\
                    sed -i -E 's|(image:\\s*)(${imageName}):([^ /]+)|\\1\\2:${newTag}|g' {} +
            """
        }

        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()
        if (hasChanges == "changed") {
            echo "[INFO] Changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[CI/CD] Updated image tags"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                git push origin HEAD:master
            """
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}