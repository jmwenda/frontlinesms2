package frontlinesms2.message

import frontlinesms2.*

class MessageActionSpec extends frontlinesms2.poll.PollGebSpec {
	def 'message actions menu is displayed for all individual messages'() {
		given:
			createTestPolls()
			createTestMessages()
			createTestFolders()
		when:
			to PollMessageViewPage
			def actions = $('#move-actions').children()*.text()
		then:
			actions[1] == "Inbox"
			actions[2] == "Shampoo Brands"
			!actions.contains("Football Teams")
		when:
			go "message/inbox/show/${Fmessage.findBySrc("Bob").id}"
			def inboxActions = $('#move-actions').children()*.text()
		then:
			inboxActions[1] == "Football Teams"
			inboxActions.every {it != "Inbox"}
	}

	def "move to inbox option should be displayed for folder messages and should work"() {
		given:
			createTestFolders()
			Folder.findByName("Work").addToMessages(new Fmessage(src: "src", dst: "dst")).save(flush: true)
		when:
			go "message/folder/${Folder.findByName("Work").id}"
			$('#move-actions').jquery.val('inbox') // bug selecting option - seems to be solved by using jquery...
			$('#move-actions').jquery.trigger('change') // again this should not be necessary, but works around apparent bugs
		then:
			waitFor { $("div.flash").displayed }
		when:
			$("a", text: "Inbox").click()
			waitFor {title == "Inbox"}
		then:
			$("tbody tr").size() == 1
	}
	
	def 'clicking on poll moves multiple messages to that poll and removes it from the previous poll or inbox'() {
		given:
			createTestPolls()
			createTestMessages()
			def bob = Fmessage.findBySrc('Bob')
			def alice = Fmessage.findBySrc('Alice')
			def shampooPoll = Poll.findByTitle('Shampoo Brands')
			def footballPoll = Poll.findByTitle('Football Teams')
		when:
			to PollMessageViewPage
			messagesSelect[0].click()
		then:
			waitFor { $('#move-actions').size() == 1 } // not sure why this should decrease to 1...
		when:
			setMoveActionsValue(shampooPoll.id.toString())
		then:
			waitFor { $('#no-messages').displayed }
			footballPoll.getPollMessages().count() == 0
			shampooPoll.getPollMessages().count() == 3
	}

	def "archive action should not be available for messages that belongs to a message owner  such as activities"() {
		setup:
			createTestPolls()
			createTestMessages()
		when:
			to PollMessageViewPage
		then:
			!$("#message-archive").displayed
	}

	def "should move poll messages to inbox"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			to PollMessageViewPage
			messagesSelect[0].click()
		then:
			waitFor { $('#move-actions').size() == 1 } // not sure why this should decrease to 1...
		when:
			setMoveActionsValue('inbox')
		then:
			waitFor { $("div.flash").text() }
		when:
			$("a", text: "Inbox").click()
		then:	
			waitFor { title == "Inbox" }
			$("tbody tr").size() == 3
	}
	
	private def setMoveActionsValue(value) {
		$('#move-actions').jquery.val(value) // bug selecting option - seems to be solved by using jquery...
		$('#move-actions').jquery.trigger('change') // again this should not be necessary, but works around apparent bugs
	}
}

class PollMessageViewPage extends geb.Page {
 	static getUrl() { "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc("Bob").id}" }
	static content = {
		messagesSelect { $(".message-select") }
	}
}
