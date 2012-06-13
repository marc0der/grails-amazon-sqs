# Amazon SQS Plugin for Grails

## Description
Amazon Simple Queue Service (Amazon SQS) offers a reliable, highly scalable, hosted queue for storing messages as they travel between computers. By using Amazon SQS, developers can simply move data between distributed components of their applications that perform different tasks, without losing messages or requiring each component to be always available. Amazon SQS makes it easy to build an automated workflow, working in close conjunction with the Amazon Elastic Compute Cloud (Amazon EC2) and the other AWS infrastructure web services.

Amazon SQS works by exposing Amazon’s web-scale messaging infrastructure as a web service. Any computer on the Internet can add or read messages without any installed software or special firewall configurations. Components of applications using Amazon SQS can run independently, and do not need to be on the same network, developed with the same technologies, or running at the same time.

This plugin for Grails brings the convenience of SQS into your Grails application. It exposes a service that can be wired into your Controllers and Services allowing you to send, read and delete messages from a queue. It even allows you to create and delete queues on the Amazon infrastructure.


## Installation

    grails install-plugin amazon-sqs

## Application Configuration

Add the following lines of configuration to your Config.groovy:

    grails.plugins.amazon.sqs.accessKey = "AMAZON_ACCESS_KEY"
    grails.plugins.amazon.sqs.secretKey = "AMAZON_SECRET_KEY"
    grails.plugins.amazon.sqs.region = "AMAZON_REGION"

Where AMAZON_REGION can be one of:

*  http://sqs.us-east-1.amazonaws.com       - The US East (Northern Virginia)
*  http://sqs.us-west-2.amazonaws.com       - The US West (Oregon) end-point
*  http://sqs.us-west-1.amazonaws.com       - The US West (Northern California)
*  http://sqs.eu-west-1.amazonaws.com       - The EU(Ireland)
*  http://sqs.ap-southeast-1.amazonaws.com  - The Asia Pacific (Singapore)
*  http://sqs.ap-northeast-1.amazonaws.com  - The Asia Pacific (Tokyo)
*  http://sqs.sa-east-1.amazonaws.com       - The South America (Sao Paulo)

## The SQS Service

Simply add the following field to your Controller or Service:

    class MyController {
        def sqsService	
        
        def index = {}
    }

Then simply start using it by calling one of the following methods:

    String createQueue(String queueName);
    boolean deleteQueue(String queueUrl);
    List<String> listQueues();
    String sendMessage(Message message);
    Message receiveMessage(String queueUrl);
    List receiveMessages(String queueUrl, Integer maxNumberOfMessages);
    boolean deleteMessage(String queueUrl, String receiptHandle);

The ```receiveMessage()``` method returns an instance of ```org.grails.plugins.amazon.sqs.Message``` which looks something like this:

    class Message {
       String id
       String body
       String queueUrl
       String receiptHandle
    }


All these methods are self explanetory and are used within the context of the Amazon SQS lifecycle, which is explained next.

## Message Lifecycle

Messages that are stored in Amazon SQS have a lifecycle that is easy to manage but ensures that all messages are processed.

1.  A system that needs to send a message will find an Amazon SQS queue, and use ```sendMessage()``` to add a new message to it.
2.  A different system that processes messages needs more messages to process, so it calls ```receiveMessage()```, and this message is returned.
3.  Once a message has been returned by ```receiveMessage()```, it will not be returned by any other ```receiveMessage()``` until the visibility timeout has passed. This keeps multiple computers from processing the same message at once.
4.  If the system that processes messages successfully finishes working with this message, it calls ```deleteMessage()```, which removes the message from the queue so no one else will ever process it. If this system fails to process the message, then it will be read by another ```receiveMessage()``` call as soon as the visibility timeout passes.

## Source Code

This is available on GitHub at:

http://github.com/marcoVermeulen/grails-amazon-sqs

## In the Pipeline

This is only the first iteration of the plugin, and only supports the use of an auto-wired service for now. In the future we will be adding the future:

1.  All other methods exposed by the SQS API: 
SendMessageBatch, ChangeMessageVisibility, ChangeMessageVisibilityBatch, DeleteMessageBatch, SetQueueAttributes, GetQueueAttributes, GetQueueUrl, AddPermission, RemovePermission
2.  An easy to use DSL that will be available within Controllers and Services for performing various queue related actions.
3.  More comprehensive configuration options.
