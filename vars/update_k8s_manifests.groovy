#!/usr/bin/env groovy


// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description : Replace all image tags (but not names) in all *.yaml files.
// -----------------------------------------------------------------------------


def call(Map config = [:]) {
    def imageTag       = config.imageTag       ?: error("[ERROR] imageTag is required")
    def manifestsPath  = config.manifestsPath  ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName    = config.gitUserName    ?: 'Jenkins CI'
    def gitUserEmail   = config.gitUserEmail   ?: 'jenkins@example.com'

    echo "[INFO] Replacing all image tags with: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        // Git config
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        sh """
            echo "[INFO] Updating image tags in ${manifestsPath}..."
            find ${manifestsPath} -type f -name "*.yaml" -exec sed -i -E 's|(image:\\s+[\\w/\\.-]+):[\\w\\.-]+|\\1:${imageTag}|g' {} +
        """

        // Check if anything actually changed
        def hasChanges = sh(script: "git diff --quiet || echo changed", returnStdout: true).trim()
        if (hasChanges == "changed") {
            echo "[INFO] Git changes detected. Committing and pushing..."
            sh """
                git add ${manifestsPath}/*.yaml
                git commit -m "[INFO] Update Docker image tags to ${imageTag} "
                git remote set-url origin https://\$GIT_USERNAME:\$GIT_PASSWORD@\$(git config --get remote.origin.url | sed 's|https://||')
                git push origin HEAD
"""

        } else {
            echo "[SUCCESS] No image tag updates were needed. Git clean."
        }
    }
}
