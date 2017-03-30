define(['dom'], function(dom) {
	return {
		formatFailures: function(type, failures) {
			var divFailures = document.createElement('div');
			divFailures.style = "paddding-top: 0.5em;"
			dom.createElement(divFailures, 'span', { style: 'font-weight: bold; font-style: italic;', textContent: type+':'});
			var ulFailures = dom.createElement(divFailures, 'ul', {});
			ulFailures.style = "list-style-type: disc; padding-left: 1.5em;"
			failures.forEach(function(item) {
				dom.createElement(ulFailures, 'li', { textContent: item });
			});
			
			return divFailures;
		},
		formatIssuesWouldHaveBeen: function(preview, size) {
			var result = ", " + size + " issue";
			if(size > 1) {
				result = result + "s";
			}
			result = result + " ";
			
			if(preview) {
				result = result + "would have";
			} else if(size == 1) {
				result = result + "has";
			} else {
				result = result + "have";
			}
			
			result = result + " been ";
			return result;
		},
		formatIssues: function(type, response) {
			var currentDate = new Date();
			var resultText = ("0" + currentDate.getHours()).slice(-2) + ":"  
	            + ("0" + currentDate.getMinutes()).slice(-2) + "." 
	            + ("0" + currentDate.getSeconds()).slice(-2) + " - " + type + " succeeded";
	            
	        // Issues
	        resultText = resultText + "; "
	        + response.issues + " issue" + (response.issues > 1 ? "s": "") + " read";
	        
	        // Duplicate keys
	        if(response.duplicateKeys>0) {
	        	resultText = resultText + " ("+ response.duplicateKeys+" duplicate keys)";
	        }
	        
	        // Matched issues
	        resultText = resultText + this.formatIssuesWouldHaveBeen(false, response.matchedIssues) + "matched";
	        
	        // Transitioned issues
	        if(response.transitionedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.transitionedIssues) + "resolved";
	        }
	        
	        // Assigned issues
	        if(response.assignedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.assignedIssues) + "assigned";
	        }
	        
	        // Commented issues
	        if(response.commentedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.commentedIssues) + "commented";
	        }      
	        
	        resultText = resultText + ".";
	        return resultText;
		},
		formatResult: function (type, response) {
	        var divResult = document.createElement('div');

	        // Base result
			var baseResult = this.formatIssues(type, response);
	        dom.createElement(divResult, 'span', { style: 'font-weight:bold;', textContent: baseResult });
	        
	        // Match failures
	        if(response.matchFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Matching failures', response.matchFailures));
	        }
	        
	        // Transition failures
	        if(response.transitionFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Transition failures', response.transitionFailures));
	        }
	        
	        // Assign failures
	        if(response.assignFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Assign failures', response.assignFailures));
	        }
	        
	        // Comment failures
	        if(response.commentFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Comment failures', response.commentFailures));
	        }
	        
	        return divResult;
		},
	
		formatError: function(type, error) {
			var currentDate = new Date();
			var resultText = ("0" + currentDate.getHours()).slice(-2) + ":"  
            	+ ("0" + currentDate.getMinutes()).slice(-2) + "." 
            	+ ("0" + currentDate.getSeconds()).slice(-2) + " - "+type+" failed";
			
			 resultText = resultText + "; " + error;
			return document.createTextNode(resultText);
		}
	};
});
