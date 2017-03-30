define(['dom'], function(dom) {
	return {
		formatResult: function (type, response) {
			var currentdate = new Date();
			var resultText = 
			    + currentdate.getHours() + ":"  
	            + currentdate.getMinutes() + "." 
	            + currentdate.getSeconds() + " - " + type + " succeeded";
	            
	        // Issues
	        resultText = resultText + "; "
	        + response.issues + " issues read";
	        
	        // Duplicate keys
	        if(response.duplicateKeys>0) {
	        	resultText = resultText + " ("+ response.duplicateKeys+" duplicate keys)";
	        }
	        
	        // Matched issues
	        resultText = resultText + ", " + response.matchedIssues + " issues have been matched";
	        
	        // Transitioned issues
	        if(response.transitionedIssues > 0) {
	        	resultText = resultText + ", " + response.transitionedIssues + " issues " + (preview ? "would " : "") + " have been resolved";
	        }
	        
	        // Assigned issues
	        if(response.assignedIssues > 0) {
	        	resultText = resultText + ", " + response.assignedIssues + " issues " + (preview ? "would " : "") + " have been assigned";
	        }
	        
	        // Commented issues
	        if(response.commentedIssues > 0) {
	        	resultText = resultText + ", " + response.commentedIssues + " issues " + (preview ? "would " : "") + " have been commented";
	        }      
	        
	        resultText = resultText + ".";
	        return document.createTextNode(resultText);
		},
	
		formatError: function(type, error) {
			var currentdate = new Date();
			var resultText = 
			    + currentdate.getHours() + ":"  
	            + currentdate.getMinutes() + "." 
	            + currentdate.getSeconds() + " - "+type+" failed";
			
			 resultText = resultText + "; " + error;
			return document.createTextNode(resultText);
		}
	};
});
