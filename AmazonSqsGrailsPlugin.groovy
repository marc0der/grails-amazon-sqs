class AmazonSqsGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/",
		    "web-app",
		    "test",
		    "scripts"
    ]

    def author = "Marco Vermeulen, Frederic Occedat"
    def authorEmail = "vermeulen.mp@gmail.com"
    def title = "Amazon SQS"
    def description = '''\\
Provides a simple service that allows all aspects of queue management and message brokering.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/amazon-sqs"

    def doWithWebDescriptor = { xml -> }

    def doWithSpring = { }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext -> }

    def onChange = { event -> }

    def onConfigChange = { event -> }
}
