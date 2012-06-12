import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.auth.BasicAWSCredentials

def config = ConfigurationHolder.config

beans = {

	basicAWSCredentials(BasicAWSCredentials, config.grails.plugins.amazon.sqs.accessKey, config.grails.plugins.amazon.sqs.secretKey)

	amazonSQS(AmazonSQSClient, basicAWSCredentials) {
		endpoint = config.grails.plugins.amazon.sqs.region
	}

}