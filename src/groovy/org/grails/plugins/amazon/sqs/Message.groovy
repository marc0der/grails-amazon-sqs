package org.grails.plugins.amazon.sqs

class Message {
	String id
	String body
	String queueUrl
	String receiptHandle
}
