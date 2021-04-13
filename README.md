## Implementing of Message Broker

> Laboratory work No3 at Real-Time Programming  
> University: Technical University of Moldova  
> Faculty: Software Engineering  
> Teacher: Burlacu Alexandru  
> Group: FAF -182  
> Student: Vizant Beatrice  
> Task: Message Broker  

## Table of contents
- [Requirements](#requirements)
- [Output example](#output-example)
- [Explanation](#explanation)
- [Technologies](#technologies)
- [Status](#status)

## Requirements
* Develop a message broker with support for multiple topics. The message broker should be a dedicated async TCP/UDP server. You must connect the second lab to it so that the lab will publish messages on the message broker which can be subscribed to using a tool like telnet or netcat. Given that you will have at least 2 separated applications, docker-compose is mandatory.  
* Messages in your code are represented as structures/classes and not as just maps or strings. Of course, this implies you will need to serialize these messages at some point, to send them via network.  
* Ensure reliable message delivery using Persistent Messages with subscriber acknowledgments and Durable queues.   
OPTIONAL:  
* CDC: Change Data Capture (1.5-2 points), thus making the publisher the database from lab2 rather than a dedicated actor.  
* Cluster Mode (2 points) discuss with me  
* Message priority + Delivery Guarantee using producer acknowledgments (1 point)  
* MQTT-style assured delivery (1p)  
* Polyglot API (UDP/TCP + MQTT/XMPP/AMQP) (0.5p) if using also HTTP then (1p) with Protocol Negotiation like HTTP's content negotiation  

## Output example
![alt-text]()

## Explanation


## Technologies
Java 12 and Maven for Jackson and MongoDB dependencies

## Status
Project Status _in progress_
