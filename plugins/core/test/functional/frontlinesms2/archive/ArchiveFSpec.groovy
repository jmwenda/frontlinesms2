package frontlinesms2.archive

import frontlinesms2.*

@Mixin(frontlinesms2.utils.GebUtil)
class ArchiveFSpec extends ArchiveBaseSpec {
	def 'archived folder list is displayed'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			def folder = Folder.findByName('Work')
			folder.archive()
			folder.save(flush:true, failOnError:true)
			to PageArchiveFolder
		then:
			folderNames*.text() == ["Work"]
	}
	
	def 'should show list of remaining messages when a message is deleted'() {
		given:
			createTestMessages2()
		when:
			go "archive/inbox/show/${Fmessage.findBySrc('Max').id}?viewingArchive=true"
		then:
			$("#message-list td.message-sender-cell")*.text().sort() == ['Jane', 'Max']
		when:
			def btnDelete = $("#delete-msg")
			btnDelete.click()
		then:
			$("#message-list td.message-sender-cell")*.text() == ['Jane']
	}
	
	def '"Archive All" button does not appear in archive section'() {
		given:
			createTestMessages2()
		when:
			go "archive/inbox/show/${Fmessage.findBySrc('Max').id}?viewingArchive=true"
			$(".message-select")[0].click()
		then:
			!$("#btn_archive_all").displayed
		when:
			$(".message-select")[1].click()
			$(".message-select")[2].click()
		then:
			!$("#btn_archive_all").displayed
		
	}
	
	def '"Delete All" button appears when multiple messages are selected in an archived activity'() {
		given:
			def poll = new Poll(name:'thingy')
			poll.editResponses(choiceA:'One', choiceB:'Other')
			poll.save(failOnError:true, flush:true)
			def messages = [Fmessage.build(src:'Max', text:'I will be late', date:TEST_DATE-4),
					Fmessage.build(src:'Max', text:'I will be late', date:TEST_DATE-4)] 
			println messages
			poll.addToMessages(messages[0])
			poll.addToMessages(messages[1])
			poll.save(failOnError:true, flush:true)
			poll.archive()
			poll.save(flush:true)
			poll.refresh()
			assert poll.activityMessages.list().every { it.archived }
		when:
			go "archive/activity/${poll.id}/show/${messages[0].id}?messageSection=activity&viewingMessages=true"
			$(".message-select-cell #message-select-all").click()
		then:
			waitFor { $("#btn_delete_all").displayed }
	}
}
