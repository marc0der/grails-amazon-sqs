package org.grails.plugins.amazon.sqs

import spock.lang.*
import grails.plugin.spock.*
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException

@Stepwise
class SqsServiceIntegrationSpec extends IntegrationSpec {

	def sqsService

	static final queueName = "myqueue"
	static final String messageBody = "This is a message"
	static queueUrl
	static messageId
	static Message message

    def "service should contain initialised SQS client"() {
	    expect:
	    sqsService.amazonSQS
    }

	def "service should allow creation of a queue"() {
		when:
		queueUrl = sqsService.createQueue(queueName)

		then:
		queueUrl.startsWith "https://sqs.us-west-2.amazonaws.com/"
		queueUrl.endsWith queueName
	}

	def "service should list all queues created"() {
		when:
		def urls = sqsService.listQueues()

		then:
		urls.contains queueUrl
	}

	def "service should allow sending of a message"() {
		given:

		def message = new Message(queueUrl: queueUrl, body: messageBody)
		
		when:
		messageId = sqsService.sendMessage(message)

		then:
		messageId
	}

	def "service should allow retrieval of a sent message"() {
		when:
		message = sqsService.receiveMessage(queueUrl)

		then:
		message.queueUrl == queueUrl
		message.body == messageBody
	}

	def "service should allow deletion of a sent message"() {
		when:
		def success = sqsService.deleteMessage(queueUrl, message.receiptHandle)

		then:
		success
	}

	def "service should allow deletion of a queue"() {
		when:
		def success = sqsService.deleteQueue(queueUrl)

		then:
		notThrown AmazonClientException
		notThrown AmazonServiceException
		success
	}

}
