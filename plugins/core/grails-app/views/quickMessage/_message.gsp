<div id="tabs-1" class="${configureTabs.contains('tabs-1') ? '' : 'hide'}">
	<label class="header" for="messageText">Enter message:</label><br />
	<g:textArea name="messageText" value="${messageText}" rows="5" cols="40"/>
	<span id="send-message-stats" class="character-count">Characters remaining 160 (1 SMS message)</span> 
</div>
<g:javascript>
	$("#messageText").live("keyup", updateCount);
	$("#messageText").live("blur", function() {
		var value = $(this).val();
		if(value) {
			$("#confirm-message-text").html(value);
		} else {
			$("#confirm-message-text").html("none");
		}
	})
</g:javascript>