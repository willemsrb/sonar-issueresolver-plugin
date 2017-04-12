define(['config', 'dom'], function(config, dom) {
	return {
		create: function(projectKey) {
			return {
				projectKey: projectKey,
				show: function(parent) {
					dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Export'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Export issues that are resolved as false positive or won\'t fix as a data file.'});
					
					// Export - form
					var formExport = dom.createElement( parent, 'form', { id: 'issueresolver-export-form' });

					// Export - form - button
					var formExportButton = dom.createElement(formExport, 'div', { className: 'modal-field'});
					dom.createElement(formExportButton, 'button', { textContent: 'Export'});

					// Export - form - onsubmit
					formExport.onsubmit = function() {
						window.location = config.basename + 'api/issueresolver/export?projectKey=' + encodeURI(projectKey);
						return false;
					};
				}
			};
		}
	};	
});
