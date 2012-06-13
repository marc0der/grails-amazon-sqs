import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.auth.BasicAWSCredentials

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
An Amazon SQS integration plugin that provides functionality allowing queue management and message brokering.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/amazon-sqs"

    def doWithWebDescriptor = { xml -> }

    def doWithSpring = {
        basicAWSCredentials(
			BasicAWSCredentials, 
			application.config.grails.plugins.amazon.sqs.accessKey, 
			application.config.grails.plugins.amazon.sqs.secretKey
		)

        amazonSQS(AmazonSQSClient, basicAWSCredentials) {
            endpoint = application.config.grails.plugins.amazon.sqs.region
        }
    }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext -> }

    def onChange = { event -> }

    def onConfigChange = { event -> }
}
