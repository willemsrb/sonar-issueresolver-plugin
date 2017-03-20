window.registerExtension('issueresolver/issueresolver_page', function (options) {
	options.el.id='issueresolver-page';
	options.el.className='page page-limited';
	
	var header = document.createElement('header');
	header.className = 'page-header';
	options.el.appendChild(header);
	
	var title = document.createElement('h1');
	title.className='page-title';
	title.textContent='Issue resolver';
	header.appendChild(title);
	
	var description = document.createElement('div');
	description.className = 'page-description';
	description.textContent = 'Allows you to export and import issues that are resolved with false positive and won\'t fix.';
	header.appendChild(description);
	
	// Export
	var divExport = document.createElement('div');
	divExport.className = 'panel';
	options.el.appendChild(divExport);
	
    var headerExport = document.createElement('h2');
	headerExport.className = 'issueresolver-header';
    headerExport.textContent = 'Export';
    divExport.appendChild(headerExport);

	var descriptionExport = document.createElement('div');
	descriptionExport.className = 'issueresolver-description big-spacer-bottom';
	descriptionExport.textContent = 'Export issues that are resolved as false positive or won\'t fix';
	divExport.appendChild(descriptionExport);

	// Export - form
	var formExport = document.createElement('form');
	formExport.id = 'issueresolver-export-form';
	divExport.appendChild(formExport);
	
	// Export - form - project key
	var formExportProjectKey = document.createElement('div');
	formExportProjectKey .className = 'modal-field';
	formExport.appendChild(formExportProjectKey);
	
	var formExportProjectKeyLabel = document.createElement('label');
	formExportProjectKeyLabel.for = 'issueresolver-export-projectkey';
	formExportProjectKey.appendChild(formExportProjectKeyLabel);

	formExportProjectKeyLabel.appendChild(document.createTextNode('Project'));
	var formExportProjectKeyLabelMandatory = document.createElement('em');
	formExportProjectKeyLabelMandatory.className='mandatory';
	formExportProjectKeyLabelMandatory.textContent = '*';
	formExportProjectKeyLabel.appendChild(formExportProjectKeyLabelMandatory);
	
	var formExportProjectKeyInput = document.createElement('select');
	formExportProjectKeyInput.id= 'issueresolver-export-projectkey';
	formExportProjectKeyInput.name='projectKey';
	formExportProjectKey.appendChild(formExportProjectKeyInput);
	
	var formExportProjectKeyDescription = document.createElement('div');
	formExportProjectKeyDescription.className='modal-field-description';
	formExportProjectKeyDescription.textContent = 'The project to export issues for';
	formExportProjectKey.appendChild(formExportProjectKeyDescription);

	// Export - form - button
	var formExportButton = document.createElement('div');
	formExportButton.className = 'modal-field';
	formExport.appendChild(formExportButton);
	
	var formExportButtonButton = document.createElement('button');
	formExportButtonButton.textContent ='Export';
	formExportButton.appendChild(formExportButtonButton);

	// Export - form - onsubmit
	formExport.onsubmit = function() {
		console.log('submitting export');
		window.location = '/api/issueresolver/export?projectKey=' + encodeURI(formExportProjectKeyInput.value);
		return false;
	};
	
	// Import
	var divImport = document.createElement('div');
	divImport.className = 'panel';
	options.el.appendChild(divImport);
	
	var divImportResult = document.createElement('div');
	divImportResult.className = 'panel';
	divImportResult.style.display = 'none';
	options.el.appendChild(divImportResult);
	
	 var headerImportResult = document.createElement('h2');
	headerImportResult.className = 'issueresolver-header';
    headerImportResult.textContent = 'Import result';
    divImportResult.appendChild(headerImportResult);
	
	
    var headerImport = document.createElement('h2');
	headerImport.className = 'issueresolver-header';
    headerImport.textContent = 'Import';
    divImport.appendChild(headerImport);

	var descriptionImport = document.createElement('div');
	descriptionImport.className = 'issueresolver-description big-spacer-bottom';
	descriptionImport.textContent = 'Import (resolve) previously export issues, that will be matched to current issues using rule key, component and location.';
	divImport.appendChild(descriptionImport);

	// Import - form
	var formImport = document.createElement('form');
	formImport.id = 'issueresolver-import-form';
	divImport.appendChild(formImport);
	
	// Import - form - project key
	var formImportProjectKey = document.createElement('div');
	formImportProjectKey.className = 'modal-field';
	formImport.appendChild(formImportProjectKey);
	
	var formImportProjectKeyLabel = document.createElement('label');
	formImportProjectKeyLabel.for = 'issueresolver-import-projectkey';
	formImportProjectKey.appendChild(formImportProjectKeyLabel);

	formImportProjectKeyLabel.appendChild(document.createTextNode('Project'));
	var formImportProjectKeyLabelMandatory = document.createElement('em');
	formImportProjectKeyLabelMandatory.className='mandatory';
	formImportProjectKeyLabelMandatory.textContent = '*';
	formImportProjectKeyLabel.appendChild(formImportProjectKeyLabelMandatory);

	var formImportProjectKeyInput = document.createElement('select');
	formImportProjectKeyInput.id= 'issueresolver-import-projectkey';
	formImportProjectKeyInput.name='projectKey';
	formImportProjectKey.appendChild(formImportProjectKeyInput);

	var formImportProjectKeyDescription = document.createElement('div');
	formImportProjectKeyDescription.className='modal-field-description';
	formImportProjectKeyDescription.textContent = 'The project to import issues for';
	formImportProjectKey.appendChild(formImportProjectKeyDescription);

	// Import - form - preview
	var formImportPreview = document.createElement('div');
	formImportPreview.className = 'modal-field';
	formImport.appendChild(formImportPreview);
	
	var formImportPreviewLabel = document.createElement('label');
	formImportPreviewLabel.for = 'issueresolver-import-preview';
	formImportPreview.appendChild(formImportPreviewLabel);

	formImportPreviewLabel.appendChild(document.createTextNode('Preview'));
//	var formImportPreviewLabelMandatory = document.createElement('em');
//	formImportPreviewLabelMandatory.className='mandatory';
//	formImportPreviewLabelMandatory.textContent = '*';
//	formImportPreviewLabel.appendChild(formImportPreviewLabelMandatory);

	var formImportPreviewInput = document.createElement('input');
	formImportPreviewInput.id= 'issueresolver-import-preview';
	formImportPreviewInput.type='checkbox';
	formImportPreviewInput.name='preview';
	formImportPreviewInput.value='true';
	formImportPreview.appendChild(formImportPreviewInput);

	var formImportPreviewDescription = document.createElement('div');
	formImportPreviewDescription.className='modal-field-description';
	formImportPreviewDescription.textContent = 'If set, issue are not actually resolved, but only matched and checked, no changes are made';
	formImportPreview.appendChild(formImportPreviewDescription);

	// Import - form - data
	var formImportData = document.createElement('div');
	formImportData.className = 'modal-field';
	formImport.appendChild(formImportData);
	
	var formImportDataLabel = document.createElement('label');
	formImportDataLabel.for = 'issueresolver-import-data';
	formImportData.appendChild(formImportDataLabel);

	formImportDataLabel.appendChild(document.createTextNode('Data'));
	var formImportDataLabelMandatory = document.createElement('em');
	formImportDataLabelMandatory.className='mandatory';
	formImportDataLabelMandatory.textContent = '*';
	formImportDataLabel.appendChild(formImportDataLabelMandatory )
	
	var formImportDataInput = document.createElement('input');
	formImportDataInput.id= 'issueresolver-import-data';
	formImportDataInput.type='file';
	formImportDataInput.name='data';
	formImportData.appendChild(formImportDataInput);

	var formImportDataDescription = document.createElement('div');
	formImportDataDescription.className='modal-field-description';
	formImportDataDescription.textContent = 'The exported resolved issue data';
	formImportData.appendChild(formImportDataDescription);

	// Import - form - button
	var formImportButton = document.createElement('div');
	formImportButton.className = 'modal-field';
	formImport.appendChild(formImportButton);
	
	var formImportButtonButton = document.createElement('button');
	formImportButtonButton.textContent ='Import';
	formImportButton.appendChild(formImportButtonButton);
	
	// Import - form - onsubmit
	formImport.onsubmit = function() {
		console.log('submitting import');
		
		formImportButtonButton.disabled=true;
				
		window.SonarRequest.postJSON(
		    '/api/issueresolver/import',
		    new FormData(formImport)
		).then(function(response) {
			var okResult = document.createElement('div');
			
			var currentdate = new Date();
			var resultText = 
			    + currentdate.getHours() + ":"  
                + currentdate.getMinutes() + "." 
                + currentdate.getSeconds() + " - Import succeeded";
                
            // Issues
            resultText = resultText + "; "
            + response.issues + " issues read";
            
            // Duplicate keys
            if(response.duplicateKeys>0) {
            	resultText = resultText + " ("+ response.duplicateKeys+" duplicate keys)";
            }
            
            // Unmatched issues
            if(response.unmatchedIssues > 0) {
            resultText = resultText + ", " + response.unmatchedIssues + " issues could not be matched";
            }
            
            // Unresolved issues
            if(response.unresolvedIssues > 0) {
            resultText = resultText + ", " + response.unresolvedIssues + " issues could not be resolved";
            }
            
            // Resolved issues
            resultText = resultText + ", " + response.resolvedIssues + " issues ";
            if(response.preview) {
            	resultText = resultText + "would ";
            }
            
            resultText = resultText + "have been resolved.";

			//            
			okResult.textContent=resultText;
			divImportResult.appendChild(okResult);
			divImportResult.style.display='block';
			formImportButtonButton.disabled=false;
			
		}).catch(function (error) {
			var errorResult = document.createElement('div');
			
			var currentdate = new Date();
			var resultText = 
			    + currentdate.getHours() + ":"  
                + currentdate.getMinutes() + "." 
                + currentdate.getSeconds() + " - Import failed";
			
			errorResult.textContent=resultText;
			divImportResult.appendChild(errorResult);
			divImportResult.style.display='block';
			formImportButtonButton.disabled=false;

		});
		return false;
	};
	
	// Populate drop down lists
	window.SonarRequest.postJSON(
		'/api/components/search',
		{ 'ps':999999,'qualifiers':'TRK'}		
	).then(function(response) {
		for(componentIndex = 0; componentIndex < response.components.length; componentIndex++) {
			var component = response.components[componentIndex];
			console.log(component);
		
			var formImportProjectKeyOption = document.createElement('option');
			formImportProjectKeyOption.value = component.key;
			formImportProjectKeyOption.textContent = component.name;
			formImportProjectKeyInput.appendChild(formImportProjectKeyOption);
			
			var formExportProjectKeyOption = document.createElement('option');
			formExportProjectKeyOption.value = component.key;
			formExportProjectKeyOption.textContent = component.name;
			formExportProjectKeyInput.appendChild(formExportProjectKeyOption);
			
		}
	}).catch(function(error) {
	});
	 
 	return function () {
  	};
});