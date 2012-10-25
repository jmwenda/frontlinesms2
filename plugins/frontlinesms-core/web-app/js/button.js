var FsmsButton = function() {
	var
		_trigger = function() {
			// Trigger clicking of the button when the anchor is clicked.
			var button = $(this);
			if(button.hasClass("disabled")) { return; }
			button.prev().click();
		},
		_apply = function(original) {
			var a, buttonText, copyAttributes, displayNone, i, newController;
			// replace a button with an anchor
			// find the original text
			original = $(original);
			if(original.hasClass("fsms-button-replaced")) { return; }
			original.addClass("fsms-button-replaced");
			buttonText = original.val() || original.text();
			// create the new control
			newController = $("<a>" + buttonText + "</a>");
			displayNone = original.css("display") === "none";
			if(displayNone) { newController.css("display", "none"); }
			copyAttributes = ["class"];
			for(i=copyAttributes.length-1; i>=0; --i) {
				a=copyAttributes[i];
				newController.attr(a, original.attr(a));
			}
			// Only IE seems to support "disabled" attribute on <a/> tags
			if(original.attr("disabled")) {
				newController.addClass("disabled");
			}
			newController.click(_trigger);
			
			// add the new control next to original
			original.after(newController);
			// hide the current control
			original.hide();
		},
		_findAndApply = function(selecter, parent) {
			var found;
			if(parent) {
				found = parent.find(selecter);
			} else { found = $(selecter); }
			found.each(function(i, e) { _apply(e); });
		},
		_find = function(selecter) {
			var found = $(selecter);
			if(found.hasClass("fsms-button-replaced")) {
				return found.next();
			}
			return found;
		};
	return { apply:_apply, trigger:_trigger, findAndApply:_findAndApply, find:_find };
};

