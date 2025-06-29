// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Run the SonarQube Scan.
// -----------------------------------------------------------------------------
def call(String sonarInstance, String projectName, String projectKey) {
    echo "[INFO] Running SonarQube Scan for project: ${projectName}"

    withSonarQubeEnv(sonarInstance) {
        sh """
            sonar-scanner \
                -Dsonar.projectKey=${projectKey} \
                -Dsonar.projectName=${projectName} \
                -Dsonar.sources=. \
                -X
        """
    }

    echo "[SUCCESS] SonarQube scan completed."
}
