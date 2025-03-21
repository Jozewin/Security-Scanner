document.addEventListener('DOMContentLoaded', function() {
    const loadingContainer = document.getElementById('loadingContainer');
    const resultsContainer = document.getElementById('resultsContainer');
    const securityScore = document.getElementById('securityScore');
    const scoreCircle = document.getElementById('scoreCircle');
    const highCount = document.getElementById('highCount');
    const mediumCount = document.getElementById('mediumCount');
    const lowCount = document.getElementById('lowCount');
    const appInfoGrid = document.getElementById('appInfoGrid');
    const highVulnerabilities = document.getElementById('highVulnerabilities');
    const mediumVulnerabilities = document.getElementById('mediumVulnerabilities');
    const lowVulnerabilities = document.getElementById('lowVulnerabilities');
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    // Load report data
    fetch(`/report?hash=${appHash}&type=${appType}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error fetching report');
            }
            return response.json();
        })
        .then(data => {
            // Process and display the data
            displayResults(data);

            // Hide loading, show results
            loadingContainer.style.display = 'none';
            resultsContainer.style.display = 'block';
        })
        .catch(error => {
            console.error('Error:', error);
            loadingContainer.innerHTML = `
                <p>Error loading results: ${error.message}</p>
                <a href="/" class="back-link">Back to Scanner</a>
            `;
        });

    // Tab switching
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all buttons and contents
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Add active class to clicked button and corresponding content
            button.classList.add('active');
            const tabId = `${button.dataset.tab}Tab`;
            document.getElementById(tabId).classList.add('active');
        });
    });

    function displayResults(data) {
        // Set security score
        const score = calculateSecurityScore(data);
        securityScore.textContent = score;

        // Set score circle color based on score
        if (score >= 80) {
            scoreCircle.style.boxShadow = 'inset 0 0 0 10px var(--success-color)';
        } else if (score >= 50) {
            scoreCircle.style.boxShadow = 'inset 0 0 0 10px var(--warning-color)';
        } else {
            scoreCircle.style.boxShadow = 'inset 0 0 0 10px var(--danger-color)';
        }

        // Count vulnerabilities by severity
        const vulnerabilities = extractVulnerabilities(data);
        const high = vulnerabilities.filter(v => v.severity === 'high').length;
        const medium = vulnerabilities.filter(v => v.severity === 'medium').length;
        const low = vulnerabilities.filter(v => v.severity === 'low').length;

        highCount.textContent = high;
        mediumCount.textContent = medium;
        lowCount.textContent = low;

        // Display app info
        displayAppInfo(data);

        // Display vulnerabilities
        displayVulnerabilities(vulnerabilities);
    }

    function calculateSecurityScore(data) {
        // This is a simplified implementation
        // In a real app, you would use MobSF's actual scoring algorithm
        const vulnerabilities = extractVulnerabilities(data);
        const high = vulnerabilities.filter(v => v.severity === 'high').length;
        const medium = vulnerabilities.filter(v => v.severity === 'medium').length;
        const low = vulnerabilities.filter(v => v.severity === 'low').length;

        // Calculate score (higher is better)
        const totalIssues = high + medium + low;
        if (totalIssues === 0) return 100;

        // Weighted calculation
        const score = 100 - ((high * 10) + (medium * 5) + (low * 2));
        return Math.max(0, Math.min(100, score));
    }

    function extractVulnerabilities(data) {
        // This function extracts vulnerabilities from MobSF report
        // The actual implementation depends on MobSF's response structure
        const vulnerabilities = [];

        // Example extraction from security_issues if it exists
        if (data.security_issues) {
            Object.entries(data.security_issues).forEach(([key, issue]) => {
                vulnerabilities.push({
                    id: key,
                    title: issue.title || 'Unknown Issue',
                    severity: issue.severity || 'medium',
                    description: issue.description || '',
                    reference: issue.reference || '',
                    metadata: issue.metadata || {}
                });
            });
        }

        // For demonstration, add some more example vulnerabilities if none found
        if (vulnerabilities.length === 0) {
            // This is just placeholder data for UI demonstration
            const demoVulnerabilities = [
                {
                    id: 'demo1',
                    title: 'Insecure Data Storage',
                    severity: 'high',
                    description: 'Application stores sensitive data in shared preferences without encryption',
                    reference: 'https://owasp.org/www-project-mobile-top-10/2016-risks/m2-insecure-data-storage'
                },
                {
                    id: 'demo2',
                    title: 'Weak Cryptography',
                    severity: 'medium',
                    description: 'Application uses weak cryptographic algorithms (MD5/SHA1)',
                    reference: 'https://owasp.org/www-project-mobile-top-10/2016-risks/m5-insufficient-cryptography'
                },
                {
                    id: 'demo3',
                    title: 'Exported Content Providers',
                    severity: 'low',
                    description: 'Application has content providers that are exported and accessible by other apps',
                    reference: 'https://owasp.org/www-project-mobile-top-10/2016-risks/m1-improper-platform-usage'
                }
            ];

            vulnerabilities.push(...demoVulnerabilities);
        }

        return vulnerabilities;
    }

    function displayAppInfo(data) {
        // Extract and display app information
        const appInfo = extractAppInfo(data);

        appInfoGrid.innerHTML = '';
        Object.entries(appInfo).forEach(([key, value]) => {
            const infoItem = document.createElement('div');
            infoItem.className = 'app-info-item';
            infoItem.innerHTML = `
                <div class="app-info-label">${key}</div>
                <div class="app-info-value">${value}</div>
            `;
            appInfoGrid.appendChild(infoItem);
        });
    }

    function extractAppInfo(data) {
        // Extract app info from MobSF response
        const appInfo = {};

        // Try to extract common app info fields
        if (data.app_name) appInfo['App Name'] = data.app_name;
        if (data.package_name) appInfo['Package Name'] = data.package_name;
        if (data.version_name) appInfo['Version'] = data.version_name;
        if (data.md5) appInfo['MD5'] = data.md5;

        // Add more fields based on the actual MobSF response structure

        // For demonstration, add some placeholder info if none found
        if (Object.keys(appInfo).length === 0) {
            appInfo['App Name'] = 'Demo App';
            appInfo['Package Name'] = 'com.example.demoapp';
            appInfo['Version'] = '1.0.0';
            appInfo['Platform'] = 'Android';
            appInfo['Size'] = '15.7 MB';
            appInfo['Min SDK'] = '21 (Android 5.0)';
            appInfo['Target SDK'] = '30 (Android 11.0)';
        }

        return appInfo;
    }

    function displayVulnerabilities(vulnerabilities) {
        // Display vulnerabilities by severity
        highVulnerabilities.innerHTML = '';
        mediumVulnerabilities.innerHTML = '';
        lowVulnerabilities.innerHTML = '';

        vulnerabilities.forEach(vuln => {
            const vulnItem = createVulnerabilityItem(vuln);

            if (vuln.severity === 'high') {
                highVulnerabilities.appendChild(vulnItem);
            } else if (vuln.severity === 'medium') {
                mediumVulnerabilities.appendChild(vulnItem);
            } else {
                lowVulnerabilities.appendChild(vulnItem);
            }
        });

        // Display message if no vulnerabilities found
        if (highVulnerabilities.children.length === 0) {
            highVulnerabilities.innerHTML = '<p>No high risk vulnerabilities found.</p>';
        }
        if (mediumVulnerabilities.children.length === 0) {
            mediumVulnerabilities.innerHTML = '<p>No medium risk vulnerabilities found.</p>';
        }
        if (lowVulnerabilities.children.length === 0) {
            lowVulnerabilities.innerHTML = '<p>No low risk vulnerabilities found.</p>';
        }
    }

    function createVulnerabilityItem(vuln) {
        const vulnItem = document.createElement('div');
        vulnItem.className = `vulnerability-item ${vuln.severity}`;

        vulnItem.innerHTML = `
            <h3 class="vulnerability-title">${vuln.title}</h3>
            <p class="vulnerability-description">${vuln.description}</p>
            ${vuln.reference ? `<p><a href="${vuln.reference}" target="_blank">Reference</a></p>` : ''}
            ${vuln.metadata ? createVulnerabilityDetails(vuln.metadata) : ''}
        `;

        return vulnItem;
    }

    function createVulnerabilityDetails(metadata) {
        if (typeof metadata !== 'object' || !metadata) return '';

        let detailsHtml = '<div class="vulnerability-details">';
        detailsHtml += '<h4>Details</h4>';

        // Add code snippet if available
        if (metadata.code) {
            detailsHtml += `<pre>${escapeHtml(metadata.code)}</pre>`;
        }

        // Add file location if available
        if (metadata.file) {
            detailsHtml += `<p>File: ${metadata.file}</p>`;
        }

        detailsHtml += '</div>';
        return detailsHtml;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});