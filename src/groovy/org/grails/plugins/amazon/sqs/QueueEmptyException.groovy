package org.grails.plugins.amazon.sqs

/**
 * Created with IntelliJ IDEA.
 * User: marco
 * Date: 12/06/2012
 * Time: 10:20
 */
class QueueEmptyException extends Throwable {

	public QueueEmptyException(String message){
		super(message)
	}

}
