package frontlinesms2.controller

import frontlinesms2.*

class ExportControllerISpec extends grails.plugin.spock.IntegrationSpec {
	def controller

	def setup() {
		createTestMessages()
		controller = new ExportController()
	}

	def "can export messages from a poll"() {
		given:
			Poll.createPoll(title: 'Football Teams', choiceA: 'manchester', choiceB:'barcelona').save(flush: true)
			[PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('Bob')),
				PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('Alice'))]*.save(failOnError:true, flush:true)
			controller.params.messageSection = "poll"
			controller.params.ownerId = Poll.findByTitle("Football Teams").id
		when:
			def result = controller.downloadMessageReport()
		then:
			result['messageInstanceList'].size() == 2
	}


	def "can export messages from a folder"() {
		given:
			createTestFolders()
			controller.params.messageSection = "folder"
			controller.params.ownerId = Folder.findByName("Work").id
		when:
			def result = controller.downloadMessageReport()
		then:
			result['messageInstanceList'].size() == 2
	}
	
	def "can export all contacts"() {
		given:
			createTestContacts()
		when:
			def result = controller.downloadContactReport()
		then:
			result['contactInstanceList'].size() == 3
	}


	def createTestMessages() {
		[new Fmessage(src:'Bob', dst:'+254987654', text:'I like manchester', dateReceived: new Date() - 4, starred: true),
			new Fmessage(src:'Alice', dst:'+2541234567', text:'go manchester', dateReceived: new Date() - 3)].each {
					it.inbound = true
					it.save(failOnError:true, flush:true)
			}
	}

	def createTestFolders() {
		//FIXME: Need to remove.Test fails without this line.
		Folder.list()

		def workFolder = new Folder(name: 'Work')
		workFolder.addToMessages(new Fmessage(src: "Bob", dst: "dst"))
		workFolder.addToMessages(new Fmessage(src: "Alice", dst: "dst"))
		workFolder.save(flush: true)
	}

	def createTestContacts() {
		[new Contact(name:'Gimli'),
			new Contact(name: "Borthon"),
			new Contact(name: "Legolas")].each {
				it.save(failOnError: true, flush: true)
		}
	}
	
	def createTestGroups() {
		def dwarves = new Group(name: 'Dwarves').save(flush: true)
		dwarves.addToMembers(Contact.findByName('Gimli'))
		dwarves.addToMembers(Contact.findByName('Borthon'))
		dwarves.save(failOnError: true, flush: true)
	}
}
