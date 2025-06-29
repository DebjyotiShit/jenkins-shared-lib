// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Run the trivy scan on the current workspace and output to trivyfs.txt
// -----------------------------------------------------------------------------

def call() {
    echo "[INFO] Starting Trivy filesystem scan..."
    
    sh """
        mkdir -p reports
        trivy fs . > reports/trivyfs.txt
    """

    echo "[SUCCESS] Trivy scan completed. Report saved to reports/trivyfs.txt"
    
    archiveArtifacts artifacts: 'reports/trivyfs.txt', allowEmptyArchive: true
}
