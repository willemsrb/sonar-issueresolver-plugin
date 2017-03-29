window.registerExtension('issueresolver/entrypoint', function (options) {
	options.el.id='issueresolver-page';
	options.el.className='page page-limited';
	
	var loader = function() {
		requirejs.config({
		    baseUrl: '/static/issueresolver'
		});
		requirejs(['main'], function(main) {
			main.main(options);
	    });
	};
		
    // Adding the script tag to the head as suggested before
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = '/static/issueresolver/require.js';

    // Then bind the event to the callback function.
    // There are several events for cross browser compatibility.
    script.onreadystatechange = loader;
    script.onload = loader;

    // Fire the loading
    options.el.appendChild(script);
	 
 	return function () {
		// No clean-up needed
  	};
});