// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Clone the repository from the specified URL and branch.
// -----------------------------------------------------------------------------

def call(String url, String branch) {
    echo "[INFO] Initiating Git checkout..."
    
    if (!url?.trim() || !branch?.trim()) {
        error "[ERROR] Git URL or branch name is missing. Aborting checkout."
    }

    try {
        git url: url, branch: branch
        echo "[SUCCESS] Checked out branch '${branch}' from '${url}'."
    } catch (error) {
        echo "[ERROR] Git checkout failed: ${error.message}"
        currentBuild.result = 'FAILURE'
        throw error
    }
}
