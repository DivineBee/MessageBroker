FROM openjdk:8
COPY . /subscribers
WORKDIR /subscribers
RUN javac ClassicSubscriber.java
CMD ["java", "ClassicSubscriber"]