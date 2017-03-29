define({
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
        return resultText;
	},

	formatError: function(type, error) {
		var currentdate = new Date();
		var resultText = 
		    + currentdate.getHours() + ":"  
            + currentdate.getMinutes() + "." 
            + currentdate.getSeconds() + " - "+type+" failed";
		
		 resultText = resultText + "; " + error;
		
		return resultText;
	}
});
