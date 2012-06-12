package org.grails.plugins.amazon.sqs

import grails.plugin.spock.*
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.ListQueuesResult
import com.amazonaws.services.sqs.model.SendMessageResult
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.amazonaws.services.sqs.model.CreateQueueResult
import com.amazonaws.services.sqs.model.DeleteQueueRequest
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.sqs.model.InvalidIdFormatException
import com.amazonaws.services.sqs.model.ReceiptHandleIsInvalidException

class SqsServiceSpec extends UnitSpec {

	SqsService service

	AmazonSQS amazonSQS = Mock()

	def setup(){
		mockLogging SqsService
		service = new SqsService(amazonSQS: amazonSQS)
	}

	def "create queue should enforce a queue name"() {
		given:
		def url = "queueUrl"
		def result = new CreateQueueResult().withQueueUrl(url)

		when:
		service.createQueue("x")

		then:
		amazonSQS.createQueue(_) >> result
		true
	}

	def "create queue should create a new queue on amazon SQS"() {
		given:
		def queueName = "myQueue"
		def result = new CreateQueueResult()

		when:
		service.createQueue(queueName)

		then:
		1 * amazonSQS.createQueue( { it.queueName == queueName } ) >> result
	}

	def "create queue should return the url of the newly created queue"() {
		given:
		def queueUrl = "http://myqueue.xxxxx"
		def result = new CreateQueueResult().withQueueUrl(queueUrl)


		when:
		def resultUrl = service.createQueue("")

		then:
		amazonSQS.createQueue(_) >> result

		and:
		resultUrl == queueUrl
	}

	def "delete queue should acknowledge successfully deletion of a queue"() {
		given:
		def queueUrl = "myurl"
		def request = new DeleteQueueRequest(queueUrl: queueUrl)

		when:
		def success = service.deleteQueue(queueUrl)

		then:
		1 * amazonSQS.deleteQueue(request)

		and:
		success
	}

	def "delete queue should report client failure of queue deletion"() {
		when:
		def success = service.deleteQueue("")

		then:
		amazonSQS.deleteQueue(_) >> { throw new AmazonClientException("boom") }

		and:
		notThrown AmazonClientException
		! success
	}

	def "list queues should retrieve a list queues result from amazon SQS"() {
		given:
		def listQueuesResult = new ListQueuesResult()

		when:
		service.listQueues()

		then:
		1 * amazonSQS.listQueues() >> listQueuesResult
	}

	def "list queue should return urls contained in list queues result"() {
		given:
		def queueUrl = "aQueueUrl"
		def listQueuesResult = new ListQueuesResult()
		listQueuesResult.setQueueUrls([queueUrl])

		when:
		def queuesUrls = service.listQueues()

		then:
		amazonSQS.listQueues() >> listQueuesResult

		and:
		queuesUrls instanceof List<String>
		queuesUrls.first() == queueUrl
	}

	def "send message should send a message to the given queue"(){
		given:
		def queueUrl = "queueUrl"
		def messageBody = "message"
		def message = new Message(queueUrl: queueUrl, body: messageBody)
		def result = new SendMessageResult()

		when:
		service.sendMessage(message)

		then:
		1 * amazonSQS.sendMessage( { it.queueUrl == queueUrl && it.messageBody == messageBody } ) >> result
	}

	def "send message should return a message id"(){
		given:
		def messageId = "1234"
		def result = new SendMessageResult(messageId: messageId)

		when:
		def response = service.sendMessage(new Message())

		then:
		amazonSQS.sendMessage(_) >> result

		and:
		response == messageId
	}

	def "receive message retrieves a message from a given queue"() {
		given:
		def queueUrl = "aQueueUrl"
		def message = new com.amazonaws.services.sqs.model.Message()
		def result = new ReceiveMessageResult(messages: [message])

		when:
		service.receiveMessage(queueUrl)

		then:
		1 * amazonSQS.receiveMessage( { it.queueUrl == queueUrl && it.maxNumberOfMessages == 1} ) >> result
	}

	def "receive message should return a message object with id, body, receipt handle and the queue url"() {
		given:
		def queueUrl = "aQueueUrl"
		def messageId = "1234"
		def messageBody = "a body"
		def receiptHandle = "213aas1"
		def message = new com.amazonaws.services.sqs.model.Message(messageId: messageId, body: messageBody, receiptHandle: receiptHandle)
		def result = new ReceiveMessageResult(messages: [message])

		when:
		def response = service.receiveMessage(queueUrl)

		then:
		amazonSQS.receiveMessage(_) >> result

		and:
		response.id == messageId
		response.body == messageBody
		response.queueUrl == queueUrl
		response.receiptHandle == receiptHandle
	}

	def "receive message should throw an exception if queue is empty"() {
		given:
		def queueUrl = "aQueueUrl"
		def result = new ReceiveMessageResult(messages: [])

		when:
		def response = service.receiveMessage(queueUrl)

		then:
		amazonSQS.receiveMessage(_) >> result

		and:
		thrown QueueEmptyException
	}

	def "receive messages should return a bounded list of message objects"() {
		given:
		def maxMessages = 10
		def queueUrl = "aQueueUrl"
		def message1 = new com.amazonaws.services.sqs.model.Message(messageId: "1")
		def message2 = new com.amazonaws.services.sqs.model.Message(messageId: "2")

		def result = new ReceiveMessageResult(messages: [message1, message2])

		when:
		def response = service.receiveMessages(queueUrl, maxMessages)

		then:
		amazonSQS.receiveMessage( { it.queueUrl == queueUrl && it.maxNumberOfMessages == maxMessages} ) >> result

		and:
		response instanceof List
		response[0].id == message1.messageId
		response[1].id == message2.messageId
	}

	def "receive messages should return an empty list when the queue is empty"(){
		given:
		def receiveMessageResult = new ReceiveMessageResult(messages: [])

		when:
		def result = service.receiveMessages("", 10)

		then:
		amazonSQS.receiveMessage(_) >> receiveMessageResult

		and:
		result instanceof List
		result.size() == 0
	}

	def "delete message should send a receipt handle and a queueUrl to delete a message"() {
		given:
		def queueUrl = "aQueueUrl"
		def receiptHandle = "654sdfds6"

		when:
		service.deleteMessage(queueUrl, receiptHandle)

		then:
		1 * amazonSQS.deleteMessage( { it.queueUrl == queueUrl && it.receiptHandle == receiptHandle} )
	}

	def "delete message should affirm the deletion of a message"() {
		when:
		def success = service.deleteMessage("", "")

		then:
		success
	}

	def "delete message should report an invalid id format"() {
		when:
		def success = service.deleteMessage("", "")

		then:
		amazonSQS.deleteMessage(_) >> { throw new InvalidIdFormatException("pow") }
		notThrown InvalidIdFormatException
		! success
	}

	def "delete message should report an invalid receipt handle"() {
		when:
		def success = service.deleteMessage("", "")

		then:
		amazonSQS.deleteMessage(_) >> { throw new ReceiptHandleIsInvalidException("pow") }
		notThrown ReceiptHandleIsInvalidException
		! success
	}

	def "delete message should report a client error"() {
		when:
		def success = service.deleteMessage("", "")

		then:
		amazonSQS.deleteMessage(_) >> { throw new AmazonClientException("pow") }
		notThrown AmazonClientException
		! success
	}

}
