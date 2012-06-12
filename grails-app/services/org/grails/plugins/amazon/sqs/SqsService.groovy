package org.grails.plugins.amazon.sqs

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.DeleteQueueRequest
import com.amazonaws.AmazonClientException
import com.amazonaws.services.sqs.model.InvalidIdFormatException
import com.amazonaws.services.sqs.model.ReceiptHandleIsInvalidException

class SqsService {

	AmazonSQS amazonSQS

    static transactional = true

	/**
	 * Create a new SQS Queue.
	 * @param queueName The name of the new queue.
	 * @return The queue URL.
	 */
	String createQueue(String queueName){
		def request = new CreateQueueRequest(queueName)
		def result = amazonSQS.createQueue(request)
		return result.queueUrl
	}

	/**
	 * Delete an existing SQS Queue.
	 * @param queueUrl The queue url to delete.
	 * @return Success.
	 */
	boolean deleteQueue(String queueUrl){
		def request = new DeleteQueueRequest(queueUrl: queueUrl)
		try{
			amazonSQS.deleteQueue(request)

		} catch(AmazonClientException ace){
			log.error "Internal error: $ace.message"
			return false
		}

		true
	}

	/**
	 * Get a complete listing of SQS Queues.
	 * @return A list of queue URLs.
	 */
    List<String> listQueues() {
	    def listQueueResult = amazonSQS.listQueues()
	    listQueueResult.queueUrls
    }

	/**
	 * Send a message to a particular SQS Queue.
	 * @param message The Message object containing queueUrl and messageBody.
	 * @return The messageId.
	 */
	String sendMessage(Message message){
		def request = new SendMessageRequest(queueUrl: message.queueUrl, messageBody: message.body)
		def result = amazonSQS.sendMessage(request)
		result.messageId
	}

	/**
	 * Receive a single Message.
	 * @param queueUrl The SQS queue URL.
	 * @return The Message.
	 */
	Message receiveMessage(String queueUrl) {
		def result = receiveMessages(queueUrl, 1)
		if(! result) {
			throw new QueueEmptyException("Nothing found on queue $queueUrl to process.")
		} else {
			result.first()
		}
	}

	/**
	 * Receive a bounded list of messages.
	 * @param queueUrl The SQS queue URL.
	 * @param maxNumberOfMessages The message list size.
	 * @return The Message List.
	 */
	List receiveMessages(String queueUrl, Integer maxNumberOfMessages) {
		def messages = []

		def request = new ReceiveMessageRequest(queueUrl: queueUrl, maxNumberOfMessages: maxNumberOfMessages)
		def result = amazonSQS.receiveMessage(request)
		result.messages?.each { message ->
			messages << new Message(id: message.messageId, body: message.body, queueUrl: queueUrl, receiptHandle: message.receiptHandle)
		}
		messages
	}

	/**
	 * Delete a message from an SQS queue.
	 * @param queueUrl The SQS queue URL.
	 * @param receiptHandle The message receipt handle.
	 * @return Success.
	 */
	boolean deleteMessage(String queueUrl, String receiptHandle) {
		def request = new DeleteMessageRequest(queueUrl: queueUrl, receiptHandle: receiptHandle)
		try {
			amazonSQS.deleteMessage(request)

		} catch(InvalidIdFormatException iife){
			log.error "Invalid ID format: ${iife.message}"
			return false
		} catch(ReceiptHandleIsInvalidException rhiie){
			log.error "Invalid Receipt Handle: ${rhiie.message}"
			return false
		} catch(AmazonClientException ace){
			log.error "Internal error: ${ace.message}"
			return false
		}
		true
	}
}
