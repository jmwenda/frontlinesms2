package frontlinesms2

import grails.test.mixin.*
import spock.lang.*
import frontlinesms2.*
import frontlinesms2.camel.*
import org.apache.camel.Exchange
import org.apache.camel.Message

@TestFor(GenericWebconnection)
@Mock([Keyword])
class GenericWebconnectionSpec extends CamelUnitSpecification {
	private static final String TEST_NUMBER = "+2345678"
	def setup() {
		Webconnection.metaClass.static.findAllByNameIlike = { name -> GenericWebconnection.findAll().findAll { it.name == name } }
	}

	@Unroll
	def "Test constraints"() {
		when:
			def extComm = new GenericWebconnection(name:name, url:"http://www.frontlinesms.com/sync",httpMethod:Webconnection.HttpMethod.GET)
		then:
			extComm.validate() == valid
		where:
			name   | valid
			'test' | true
			'test' | true
			''     | false
			null   | false
	}

	@Unroll
	def "Test URL constraints for #url (valid? #valid)"() {
		when:
			def extComm = new GenericWebconnection(name:"URL",url:url,httpMethod:Webconnection.HttpMethod.GET)
		then:
			extComm.validate() == valid
		where:
			url                                           | valid
			'http://www.example.com/branderr/csce.html'   | true
			'https://www.example.com/index.html'          | true
			'http://127.0.0.1:8080/frontlinesms-core'     | true
			'http://127.0.0.1'                            | true
			'www.example.com/index.html'                  | false
			'http://localhost:8080/frontlinesms-core'     | false // currently fails because of http://jira.grails.org/browse/GRAILS-5509, http://jira.grails.org/browse/GRAILS-1692, https://issues.apache.org/jira/browse/VALIDATOR-248
			'http//www.example.com/index.php'             | false
			'https://http://home/frontlinesms'            | false
			'http://....home.com'                         | false
			'http:/www.example.com/index.html'            | false
			'http:/wwww.example.com/index.html'           | false
			'htpp:/www.example.com/index.html'            | false
			'htttp:/www.example.com/index.html'           | false
			'htttp:/www..example.com/index.html'          | false
			'ftp://www.example.com/home/smith/budget.wk1' | false
	}

	def 'apiProcess should pass call to service'() {
		given:
			WebconnectionService s = Mock()
			def c = new GenericWebconnection()
			c.webconnectionService = s
			def controller = [:]
		when:
			c.apiProcess(controller)
		then:
			1 * s.apiProcess(c, controller)
	}
}

