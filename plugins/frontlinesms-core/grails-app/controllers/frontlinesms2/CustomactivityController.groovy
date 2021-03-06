package frontlinesms2

class CustomactivityController extends ActivityController {
	def customActivityService 

	def save() {
		withCustomActivity { customActivity ->
			doSave('customactivity', customActivityService, customActivity)
		}
	}

	private def withCustomActivity = withDomainObject CustomActivity, { params.ownerId }
}
