window.registerExtension('issueresolver/entrypoint', function (options) {
	options.el.id='issueresolver-page';
	options.el.className='page page-limited';
	
	var location = options.router.createLocation('/static/issueresolver');
	var loader = function() {
		requirejs.config({
		    baseUrl: location.basename + location.pathname
		});
		requirejs(['config', 'main'], function(config, main) {
			config.basename = location.basename;
			main.main(options);
	    });
	};
		
    // Adding the script tag to the head as suggested before
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = location.basename + location.pathname + '/require.js';

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