define({
	createElement: function (parent, name, properties) {
		var element = document.createElement(name);
		for(var propertyName in properties){
		    element[propertyName] = properties[propertyName];
		}
		parent.appendChild(element);
		return element;
	},

	removeChildren: function(parent) {
		while (parent.firstChild) {
			parent.removeChild(parent.firstChild);
		}
	}
});
