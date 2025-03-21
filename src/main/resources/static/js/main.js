document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const fileInput = document.getElementById('file');
    const fileName = document.getElementById('fileName');
    const submitBtn = document.querySelector('.submit-btn');
    const btnText = document.querySelector('.btn-text');
    const spinner = document.querySelector('.spinner');
    const progressContainer = document.getElementById('progressContainer');
    const progressBar = document.getElementById('progressBar');
    const statusMessage = document.getElementById('statusMessage');
    const uploadStep = document.getElementById('uploadStep');
    const scanStep = document.getElementById('scanStep');
    const completeStep = document.getElementById('completeStep');

    fileInput.addEventListener('change', function() {
        if (this.files && this.files.length > 0) {
            fileName.textContent = this.files[0].name;
        } else {
            fileName.textContent = 'No file selected';
        }
    });

    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();

        if (!fileInput.files || fileInput.files.length === 0) {
            alert('Please select a file to scan');
            return;
        }

        // Show loading state
        btnText.textContent = 'Uploading...';
        spinner.classList.remove('hidden');
        submitBtn.disabled = true;
        // Show progress indicators
               progressContainer.classList.remove('hidden');

               const formData = new FormData();
               formData.append('file', fileInput.files[0]);

               // Set progress to uploading (33%)
               progressBar.style.width = '33%';

               // Make API request to scan endpoint
               fetch('/scan', {
                   method: 'POST',
                   body: formData
               })
               .then(response => {
                   if (!response.ok) {
                       throw new Error('Error scanning app');
                   }
                   return response.json();
               })
               .then(data => {
                   // Update progress to scanning (66%)
                   uploadStep.classList.remove('active');
                   uploadStep.classList.add('completed');
                   scanStep.classList.add('active');
                   progressBar.style.width = '66%';
                   statusMessage.textContent = 'Analyzing app for vulnerabilities...';

                   if (data.hash && data.scan_type) {
                       // Update progress to complete (100%)
                       setTimeout(() => {
                           scanStep.classList.remove('active');
                           scanStep.classList.add('completed');
                           completeStep.classList.add('active');
                           progressBar.style.width = '100%';
                           statusMessage.textContent = 'Scan complete! Redirecting to results...';

                           // Redirect to results page
                           setTimeout(() => {
                               window.location.href = `/results?hash=${data.hash}&type=${data.scan_type}`;
                           }, 1000);
                       }, 1500);
                   } else {
                       throw new Error('Invalid response from server');
                   }
               })
               .catch(error => {
                   console.error('Error:', error);
                   statusMessage.textContent = `Error: ${error.message}`;
                   btnText.textContent = 'Scan App';
                   spinner.classList.add('hidden');
                   submitBtn.disabled = false;
               });
           });
        });