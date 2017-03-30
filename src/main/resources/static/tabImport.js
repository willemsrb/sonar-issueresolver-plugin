define(['dom', 'result'], function(dom, result) {
	return {
		create: function(projectKey) {	
			return {
				projectKey: projectKey,
				show: function(parent) {
					// Header and description
					dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Import'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Import a datafile with issues (created using export), that will be matched to current issues using rule key, component and location.'});
					
					// Import - form
					var formImport = dom.createElement(parent, 'form', { id: 'issueresolver-import-form' });
					
					// Import - form - projectKey (hidden)
					dom.createElement(formImport, 'input', { id: 'issueresolver-import-projectKey', type:'hidden', name: 'projectKey', value: projectKey });

					// Import - form - data
					var formImportData = dom.createElement(formImport, 'div', { className: 'modal-field'});
					var formImportDataLabel = dom.createElement(formImportData, 'label', { for: 'issueresolver-import-data'});
					formImportDataLabel.appendChild(document.createTextNode('Data'));
					dom.createElement(formImportDataLabel, 'em', { className:'mandatory',textContent: '*'});
					dom.createElement(formImportData, 'input', { id: 'issueresolver-import-data', type:'file', name:'data'});
					dom.createElement(formImportData, 'div', { className:'modal-field-description', textContent: 'The exported issue data'});

					// Import - form - preview (checkbox, optional)
					var formImportPreview = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportPreviewLabel = dom.createElement(formImportPreview, 'label', { for: 'issueresolver-import-preview' });
					formImportPreviewLabel.appendChild(document.createTextNode('Preview'));
					dom.createElement(formImportPreview, 'input', { id: 'issueresolver-import-preview', type: 'checkbox', name: 'preview', value: 'true'});
					dom.createElement(formImportPreview, 'div', { className: 'modal-field-description', textContent: 'If set, issues are not actually resolved, but only matched and checked, no changes are made' });

					// Import - form - skipAssign (checkbox, optional)
					var formImportSkipAssign = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportSkipAssignLabel = dom.createElement(formImportSkipAssign, 'label', { for: 'issueresolver-import-skipassign' });
					formImportSkipAssignLabel.appendChild(document.createTextNode('Skip assignments'));
					dom.createElement(formImportSkipAssign, 'input', { id: 'issueresolver-import-skipassign', type: 'checkbox', name: 'skipAssign', value: 'true'});
					dom.createElement(formImportSkipAssign, 'div', { className: 'modal-field-description', textContent: 'If set, issue assignments are skipped' });

					// Import - form - skipComments (checkbox, optional)
					var formImportSkipComments = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportSkipCommentsLabel = dom.createElement(formImportSkipComments, 'label', { for: 'issueresolver-import-skipcomments' });
					formImportSkipCommentsLabel.appendChild(document.createTextNode('Skip comments'));
					dom.createElement(formImportSkipComments, 'input', { id: 'issueresolver-import-skipcomments', type: 'checkbox', name: 'skipComments', value: 'true'});
					dom.createElement(formImportSkipComments, 'div', { className: 'modal-field-description', textContent: 'If set, issue comments are skipped' });
				
					// Import - form - button
					var formImportButton = dom.createElement(formImport, 'div', { className: 'modal-field' });
					var formImportButtonButton = dom.createElement(formImportButton, 'button', { textContent: 'Import' });

					// Result placeholder
					var divImportResult = dom.createElement(parent, 'div', {});
					divImportResult.style.display = 'none';
					dom.createElement(divImportResult, 'h2', { className: 'issueresolver-header', textContent: 'Import result'});
					
					// Import - form - onsubmit
					formImport.onsubmit = function() {
						formImportButtonButton.disabled=true;
								
						window.SonarRequest.postJSON(
						    '/api/issueresolver/import',
						    new FormData(formImport)
						).then(function(response) {
							divImportResult.appendChild(result.formatResult('Import', response));
							divImportResult.style.display='block';
							formImportButtonButton.disabled=false;
						}).catch(function (error) {
							divImportResult.appendChild(result.formatError('Import', error));
							divImportResult.style.display='block';
							formImportButtonButton.disabled=false;
						});
						
						return false;
					};
				}
			};
		}
	};
});
