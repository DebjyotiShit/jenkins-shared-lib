// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Generates and archives a structured build report for the pipeline.
// -----------------------------------------------------------------------------

def call(Map config = [:]) {
    def projectName = config.projectName ?: 'Project'
    def imageName   = config.imageName ?: 'N/A'
    def imageTag    = config.imageTag ?: 'latest'
    def buildStatus = currentBuild.result ?: 'SUCCESS'
    
    echo "[INFO] Generating build report for ${projectName}..."

    // Create report directory
    sh 'mkdir -p reports'

    // Build the report content
    sh """
        {
            echo "==============================="
            echo "       ${projectName} Build Report"
            echo "==============================="
            echo "Generated On : \$(date)"
            echo ""
            echo "Build Number : ${env.BUILD_NUMBER}"
            echo "Docker Image  : ${imageName}"
            echo "Image Tag     : ${imageTag}"
            echo "Build Status  : ${buildStatus}"
            echo "Build URL     : ${env.BUILD_URL}"
        } > reports/build-report.txt
    """

    echo "[INFO] Archiving build report..."
    archiveArtifacts artifacts: 'reports/*', allowEmptyArchive: true

    echo "[SUCCESS] Build report generated and archived successfully."
}
