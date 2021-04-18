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

##### Successful docker-compose build  
![alt-text](https://github.com/DivineBee/MessageBroker/blob/master/src/main/resources/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-04-16%20163526.jpg?raw=true)  
##### Subscriber's output which subscribed to the topics tweet and user  
![alt-text](https://github.com/DivineBee/MessageBroker/blob/master/src/main/resources/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202021-04-17%20135846.jpg?raw=true)  

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
    ObjectInputStream inputStream = new ObjectInputStream(msg.getInputStream());

    //  transform obtained data to processable object
    Map<String, Object> incomingData = (Map<String, Object>) inputStream.readObject();

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
            List<String> topicList = getListFromObj(incomingData.get(CustomStringTopic.TOPICS_TO_SUB));

            //  if there is no such topic yet, create one for sub and wait for info to come
            for (String topic : topicList) {
                if (!subscribersToTopics.containsKey(topic))
                    subscribersToTopics.put(topic, new ArrayList<>());
                subscribersToTopics.get(topic).add((Integer) incomingData.get("port"));
            }

            //  establish connection for sending data to sub
            TcpClient tcpClient = new TcpClient((String) incomingData.get(CustomStringTopic.IP), (Integer) incomingData.get("port"));
            ActorFactory.createActor("TcpClient_" + incomingData.get("port"), tcpClient.getClientBehaviour());

            return false;
        }

        if(incomingData.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.PUBLISHING))
            for (String topic : getListFromObj(incomingData.get(CustomStringTopic.TOPIC_TO_PUBLISH)))
                if (!subscribersToTopics.containsKey(topic))
                    subscribersToTopics.put(topic, new ArrayList<>());

        //  publish message to all submitted for this topic subscribers
        if (incomingData.get(CustomStringTopic.TOPIC) != null) {
            List<Integer> listOfSubs = subscribersToTopics.get(incomingData.get(CustomStringTopic.TOPIC));
            publish(listOfSubs, incomingData);
        } else {
            System.err.println("Message has no topic attached");
        }
                    * * *
```
MessageBroker also has 2 helper classes - CustomStringTopic and CustomSubtopic. In the first one are the main available topics for communication with Broker. The second one is responsible for sub-topics where are the keys for extracting data from hashmap. So the first one is the topic itself, but the second one is for ease of use in using the hashmap keys.  
Now I will talk about why we need to have this broker at all - Publishers and Subscribers. In the role of the publisher I chose to be Sink, because it already has all the data beautifully batched together. Inside it I prepared a method which is responsible for sending data. So, here for each topic a separate map is made and the data is inserted to. Then this data is prepared and sent.  
```java
 public void prepareAndSendData(List<DataWithAnalytics> recordsToSend) throws DeadException {
    // form transmittable record for each found piece of data
    for (DataWithAnalytics currentRecord : recordsToSend) {
        HashMap<String, Object> tweetRecord = new HashMap<>();
        HashMap<String, Object> userRecord = new HashMap<>();

        tweetRecord.put(CustomStringTopic.TOPIC, CustomStringTopic.TWEET);
        tweetRecord.put(CustomSubtopic.ID, currentRecord.getId());
        tweetRecord.put(CustomSubtopic.TWEET_TEXT, currentRecord.getTweet());
        tweetRecord.put(CustomSubtopic.EMOTION_RATIO, currentRecord.getEmotionRatio());
        tweetRecord.put(CustomSubtopic.EMOTION_SCORE, currentRecord.getEmotionScore());
        Supervisor.sendMessage("TcpClient", tweetRecord);

        userRecord.put(CustomStringTopic.TOPIC, CustomStringTopic.USER);
        userRecord.put(CustomSubtopic.ID, currentRecord.getId());
        userRecord.put(CustomSubtopic.USERNAME, currentRecord.getUser());
        userRecord.put(CustomSubtopic.USER_RATIO, currentRecord.getUserRatio());
        Supervisor.sendMessage("TcpClient", userRecord);
    }
}
```  
If the publisher was already defined and I just added some lines of code then the Subscriber had to be add. In the Subscriber first thing was to define the needed fields which were of course the port and ip to which the subscriber will connect. In the main method is initializing the client that will send request for connection, then will inform the broker where to send the data with the handshake(establishing connection between the two). And then after establishing connection for request from the broker the subscription can be performed. In the handshake method, the subscriber performs the handshake with broker, setting topics to which it wants to subscribe. It is done in few steps, first it initializes the handshake message that will inform about to which topics is required subscription then the subscriber points to which topics he wants to subscribe and then informs the broker about the port where to send data. Subscriber has its definition of behaviour, the behaviour of joining messages and recording incomplete ones for their future finishing. First it receives the input stream from the broker and reads this incoming message of the specific type where it is getting the record info about the topics to which it has subscribed and then shows the record if it has value. Then the buffer is cleared for the next iteration.  
```java
public static void main(String[] args) throws IOException, DeadException {
    // init client that will send request for connection
    TcpClient tcpClient = new TcpClient("127.0.0.1", 3000);
    ActorFactory.createActor("TcpClient", tcpClient.getClientBehaviour());

    // inform broker where to send data
    handShake();

    // define port that will listen for incoming requests and messages from broker
    TcpServer tcpServer = new TcpServer(3002);
    Socket socket = null;

    // perform subscription
    subscribe(socket, tcpServer);
}
```  
Now short about the Docker. In order to make the docker-compose I had to make separate Dockerfiles for containers which I wanted to put in. After some tries and many fails because my classes had lots of dependencies I came to the conclusion that  I need to make a fat jar file which will contain all the dependencies needed to run the container properly. So I made this fat jar file and in the Dockerfile wrote the following:  
```dockerfile
# Prerequisites.
FROM picoded/ubuntu-base

# This is in accordance to : https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-get-on-ubuntu-16-04
RUN apt-get update && \
	apt-get install -y openjdk-8-jdk && \
	apt-get install -y ant && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

# Fix certificate issues, found as of
# https://bugs.launchpad.net/ubuntu/+source/ca-certificates-java/+bug/983302
RUN apt-get update && \
	apt-get install -y ca-certificates-java && \
	apt-get clean && \
	update-ca-certificates -f && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

# Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# App
USER daemon

# This copies to local fat jar inside the image
ADD msgbroker-jar-with-dependencies.jar appname.jar

# What to run when the container starts
ENTRYPOINT [ "java", "-jar", "./appname.jar" ]

# Ports used by the app
EXPOSE 5000
```  
In order to have java run I had to use a OS, for which I chose Ubuntu. On it the installation of java 8 is performed and fixing of possible certification issues, also all the stuff with setting up such as environment variables and so on. Then the fat jar is inserted here with new name appname.jar because the initial one was too big. After that I defined the entrypoint and the port on which the container will start. Also in order to make the fat jar I had to use Maven via command line and pass some commands for building that fat jar. Then after making such Dockerfiles for each component I brought them together in the final docker-compose which collects them and run together.  
```dockerfile
version: "3.8"
services:
  mongodb:
    container_name: mongodb
    image: mongo
    ports:
      - 27017:27017
    environment:
      - MONGO_INITDB_ROOT_USERNAME=admin
      - MONGO_INITDB_ROOT_PASSWORD=admin

  rtpserver:
    image: alexburlacu/rtp-server:faf18x
    ports:
      - "4000:4000"
    healthcheck:
      test: [ "CMD", "curl", "-f", "http://rtpserver:4000" ]
      interval: 5s # 10s
      timeout: 5s  # 10s
      retries: 1  #2

  message-broker:
    container_name: message-broker
    build: target/
    depends_on:
      - rtpserver
    ports:
      - "5000:5000"
```  
First is the container for the MongoDB, next is the rtpserver used in the all 3 labs and last one is the container with my laboratory work. 

## Technologies
* Java 11
* Maven for Jackson and MongoDB dependencies
* MongoDB
* Docker

## Status
Project Status _Adding functionalities_
