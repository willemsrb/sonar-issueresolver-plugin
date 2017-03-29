define(['dom'], function(dom) {
	return {
		create: function(parent) {
			// Setup the tabs
			var divLayout = dom.createElement(parent, 'div', { className: 'settings-layout' });
			var divSide = dom.createElement(divLayout, 'div', { className: 'settings-side'});
			var divMain = dom.createElement(divLayout, 'div', { className: 'settings-main'});
			var ulMenu = dom.createElement(divSide, 'ul', { className: 'settings-menu'});		

			// Return the tabs object
			return {
				tabParent: divMain,
				menuParent: ulMenu,
				links: [],
				show: function(name){
					dom.removeChildren(this.tabParent);
					this.links.forEach(function(item, index) {
						if(item.name == name) {
							item.link.className = 'active';
							item.tab.show(divMain);
						} else {
							item.link.className = '';
						}
					});
				},
				tab: function(name, tab) {
					var li = dom.createElement(this.menuParent, 'li', {});
					var a = dom.createElement(li, 'a', { textContent: name, href: '#' });
					var thisObject = this;
						
					a.onclick = function() {
						thisObject.show(name);
					}
					
					this.links.push({ name: name, link: a, tab: tab });
				},
			};
		},
	}
});
