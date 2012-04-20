package frontlinesms2

import java.text.DateFormat;
import java.text.SimpleDateFormat

import au.com.bytecode.opencsv.CSVWriter

class ImportController {
	private static final def MESSAGE_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
	
	def exportService
	def failedContactsFile = new File("failedContacts.txt")
	
	def importData = {
		if (params.data == 'contacts') importContacts()
		else importMessages()
	}
	
	def importContacts = {
		def savedCount = 0
		def uploadedCSVFile = request.getFile('importCsvFile')
		
		if(uploadedCSVFile) {
			def headers
			def failedLines = []
			def standardFields = ['Name':'name', 'Mobile Number':'mobile',
					'E-mail Address':'email', 'Notes':'notes']
			uploadedCSVFile.inputStream.toCsvReader([escapeChar:'�']).eachLine { tokens ->
				if(!headers) headers = tokens
				else try {
					Contact c = new Contact()
					def groups
					headers.eachWithIndex { key, i ->
						def value = tokens[i]
						if(key in standardFields) {
							c."${standardFields[key]}" = value
						} else if(key == 'Group(s)') {
							def groupNames = getGroupNames(value)
							println "Group names: $groupNames"
							groups = getGroups(groupNames)
						} else {
							new CustomField(name:key, value:value, contact:c)
						}
					}
					c.save(failOnError:true)
					if(groups) groups.each { c.addToGroup(it) }
					++savedCount
				} catch(Exception ex) {
					log.info "Encountered saving contact ", ex
					failedLines << tokens
				}		
			}

			if(failedLines) {
				def writer
				try {
					writer = new CSVWriter(new OutputStreamWriter(failedContactsFile.newOutputStream(), 'UTF-8'))
					writer.writeNext(headers)
					failedLines.each { writer.writeNext(it) }
				} finally { try { writer.close() } catch(Exception ex) {} }
			}
			
			flash.message = "$savedCount contacts were imported; ${failedLines.size()} failed${failedLines? ('. ' + g.link(action:'exportFailedContacts', absolute:'true', 'Create CSV of failed contacts')): ''}" 
			
			redirect controller: "settings", action: 'general'
		} else throw new RuntimeException("File upload has failed for some reason.")
	}

	def exportFailedContacts = { 
		response.setHeader("Content-disposition", "attachment; filename=failedContacts.csv")
		failedContactsFile.eachLine {response.outputStream  << "$it\n"}
		response.outputStream.flush()
		failedContactsFile.deleteOnExit()
	}
	
	def importMessages = {
		def savedCount = 0
		def failedCount = 0
		def uploadedCSVFile = request.getFile('importCsvFile')
		if(uploadedCSVFile) {
			def headers
			def standardFields = ['Message Content':'text', 'Sender Number':'src']
			def dispatchStatuses = [Failed:DispatchStatus.FAILED,
					Pending:DispatchStatus.PENDING,
					Outbox:DispatchStatus.SENT,
					Sent:DispatchStatus.SENT]
			uploadedCSVFile.inputStream.toCsvReader([escapeChar:'�']).eachLine { tokens ->
				println "Processing: $tokens"
				if(!headers) headers = tokens 
				else try {
					Fmessage fm = new Fmessage()
					def dispatchStatus
					headers.eachWithIndex { key, i ->
						def value = tokens[i]
						if (key in standardFields) {
							fm[standardFields[key]] = value
						} else if (key == 'Message Date') {
							fm.date = MESSAGE_DATE.parse(value)
						} else if (key == 'Recipient Number') {
							fm.addToDispatches(new Dispatch(dst:value))
						} else if(key == 'Message Type') {
							fm.inbound = (value == 'Received')
						} else if(key == 'Message Status') {
							dispatchStatus = dispatchStatuses[value]
						}
					}
					if (fm.inbound) fm.dispatches = []
					else fm.dispatches.findAll{
						it.status = dispatchStatus
						if (dispatchStatus==DispatchStatus.SENT) it.dateSent = fm.date
					}
					fm.save(failOnError:true)
					++savedCount
					getMessageFolder("messages from v1").addToMessages(fm)
				} catch(Exception ex) {
					ex.printStackTrace()
					log.info "Encountered saving message ", ex
					++failedCount
				}
			}
			println "savedCount: $savedCount; failedCount: $failedCount"
			flash.message = "$savedCount messages were imported; $failedCount failed" 
			redirect controller: "settings", action: 'general'
		}
	}
	
	def getMessageFolder(name) {
		Folder.findByName(name)?: new Folder(name: name).save(failOnError:true)
	}

	def getGroupNames(csvValue) {
		println "getGroupNames() : csvValue=$csvValue"
		Set csvGroups = []
		csvValue.split("\\\\").each { gName ->
			def longName
			gName.split("/").each { shortName ->
				csvGroups << shortName
				longName = longName? "$longName-$shortName": shortName
				csvGroups << longName
			}
		}
		println "getGroupNames() : ${csvGroups - ''}"
		return csvGroups - ''
	}
	
	def getGroups(groupNames) {
		println "ImportController.getGroups() : $groupNames"
		groupNames.collect { name ->
			Group.findByName(name)?: new Group(name:name).save(failOnError:true)
		}
	}
}
