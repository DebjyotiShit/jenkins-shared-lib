#!/usr/bin/env groovy
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
            find ${manifestsPath} -type f -name '*.yaml' -exec sed -i -E 's|(image:\\s+[\\w./-]+):[\\w.-]+|\\1:${imageTag}|g' {} +
        """
    
        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()
        if (hasChanges == "changed") {
            echo "[INFO] Changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[AUTO] Updated all image tags to ${imageTag}"
                git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/DebjyotiShit/ClearCut.git
                git push origin HEAD
            """
        } else {
            echo "[INFO] No changes to commit. All image tags are already up-to-date."
        }
    }
}
