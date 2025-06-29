// -----------------------------------------------------------------------------
// Maintained by: Debjyoti Shit
// Description: Run the OWASP Dependency Check.
// -----------------------------------------------------------------------------

def call() {
    echo "[INFO] Running OWASP Dependency Check..."

    dependencyCheck additionalArguments: '--scan ./', odcInstallation: 'OWASP'

    echo "[INFO] Publishing Dependency Check report..."
    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'

    echo "[SUCCESS] Dependency Check completed and report published."
}
