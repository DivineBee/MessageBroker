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
For this laboratory work I started with implementation of TCP server and client which later will be responsible for reliable message delivery.  
First was implementing the TCP server which main responsibility is listening for incoming requests, It was the simplest class to implement withing this laboratory work and because of previous semester background. Then I created the TCP client which will send requests to the server. Its construcctor constructor define IP and port where to send requests, designed to be launched as an actor. Within this class a behaviour field is declared and the Behaviour methods are implemented as following:  
```java
    //  define behaviour
    clientBehaviour = new Behaviour<Object>() {

        /**
         * sends each received message as a request to the defined IP and port
         * @param self Self reference
         * @param msg Message that is sent to the actor and must be sent as request to IP and port
         * @return true if must be continued, false if not
         * @throws Exception
         */
        @Override
        public boolean onReceive(Actor<Object> self, Object msg) throws Exception {
            //  set output stream and send message as java-object
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(msg);
            } catch (SocketException e) {
                System.err.println("Subscriber disconnected");
                return false;
            }

            return true;
        }

        @Override
        public void onException(Actor<Object> self, Exception e) {
            System.out.println("Error sending message from " + self.getName());
        }
    };
```  
TCP client has one more method which is responsible for setting the destination to where to send the request:   
```java
public void setDestination(String destinationIp, int destinationPort) throws IOException {
    SocketAddress socketAddress = new InetSocketAddress(destinationIp, destinationPort);
    socket.bind(socketAddress);
    socket.connect(socketAddress);
}
```  
Next, the king of this laboratory work - Message Broker. This class is responsible for work of publishers and subscribers connection principle. Subscribers connects to the server, requesting for list of topics. This subscribers is registered conform topics that it requested. When there is new message from publisher, broker checks topic of message and sends it to all subscribers that requested for this topic. If there is not such topic, then subscriber waits until messages with such topic will appear. Publisher connects to the server, showing that it wants to publish messages. Broker opens stream for him and waits for them. When broker receives message, he checks topic of the message and sends it to all subscribers for this topic. Like in the TCP client, Message Broker has a field which is responsible for defining the behaviour of the receiving and processing messages. First it sets a stream for client input then it is serialized into processable object. After that multiple checks are performed in order for broker to know how to behave and process data, for optimization purposes first check is if the received data is not empty(null) this way momentally excluding the other next checks which put load on the system. In case there is a new subscriber, then add actor for him and set to which topics he is subscribing, in case the topic doesn't already exist then create new one and wait for information to come, after that the connection is being established in order to send the data to subscriber through the TCP client. Then publish the messages to certain topic for all submitted subscribers. The MessageBroker has three more methods which are called from within behaviour(subscribe, publish, getListFromObj). I will start from subscribe() - it opens a connection for each incoming request and sets an actor responsible for it. Inside there is a try-catch block where a waiting for connection is performed and then creation of actor in case it detected a new connection to server. The publish() method sends data to all susbcribers of certain topic in case that list of subscribers is not null it traverses the whole list of subscribers and then sending the message to it. The getListFromObj() gets list of String from incoming objects(serialization). And finally the main() where Broker is establishing connection to the server and opens new sockets to new actors waiting for data as susbscription. A part of the explanation's code below:  
```java
                    * * *
    //  set stream for input from client
    ObjectInputStream objectInputStream = new ObjectInputStream(msg.getInputStream());

    //  transform obtained data to processable object
    Map<String, Object> incomingData = (Map<String, Object>) objectInputStream.readObject();

    if (incomingData == null || incomingData.isEmpty())
        System.err.println("empty message was received");
    else {
        //  if client ends messaging, kill his actor
        if(incomingData.get("topic").equals(CustomStringTopic.END_OF_MESSAGING)) {
            self.die();
            return false;
        }

        //  if this is new subscriber, then add actor for him and set to which topics he is subscribing
        if(incomingData.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.SUBSCRIBING)) {
            List<String> themesList = getListFromObj(incomingData.get(CustomStringTopic.TOPICS_TO_SUB));

            //  if there is no such theme yet, create one for sub and wait for info to come
            for (String theme : themesList) {
                if (!subscribersToTopics.containsKey(theme))
                    subscribersToTopics.put(theme, new ArrayList<>());
                subscribersToTopics.get(theme).add((Integer) incomingData.get("port"));
            }

            //  establish connection for sending data to sub
            TcpClient tcpClient = new TcpClient((String) incomingData.get(CustomStringTopic.IP), (Integer) incomingData.get("port"));
            ActorFactory.createActor("TcpClient_" + incomingData.get("port"), tcpClient.getClientBehaviour());

            return false;
        }

        if(incomingData.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.PUBLISHING))
            for (String theme : getListFromObj(incomingData.get(CustomStringTopic.TOPIC_TO_PUBLISH)))
                if (!subscribersToTopics.containsKey(theme))
                    subscribersToTopics.put(theme, new ArrayList<>());

        //  publish message to all submitted for this topic subscribers
        if (incomingData.get(CustomStringTopic.TOPIC) != null) {
            List<Integer> listOfSubs = subscribersToTopics.get(incomingData.get(CustomStringTopic.TOPIC));
            publish(listOfSubs, incomingData);
        } else {
            System.err.println("Message has no topic attached");
        }
                    * * *
```

## Technologies
Java 12 and Maven for Jackson and MongoDB dependencies

## Status
Project Status _in progress_
