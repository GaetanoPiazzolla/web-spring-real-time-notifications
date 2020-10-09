# Real-time-notification in Real-enterprise-world

## Purpose 
This project is a POC / starting point / copy-paste place, to start implementing some real time notification using web socket in a real enterprise word.

**TL;DR:** checkout this [IMAGE](https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/connection.png) to know get very quick what I'm talking about.

What do I mean is that in this project I have used all the layer and component of a standard web application, eg: relaying the web socket connection trough a reverse proxy (nginx) in https, relaying trough a gateway (kong), using docker to build the application layers and expose ports, orchestrating with docker-compose etc.

Most internet tutorials do not cover all the topics here involved so I've decided to put it all together. 


## Technologies used
In this project I've used several framework and libraries:

1) STOMPjs and SockJS
2) SpringBoot + STOMP protocol libraries, 
3) RabbitMQ
4) Kong & Konga
5) Nginx
6) Docker & DockerCompose

Let's start doing a brief introduction about each of those with some useful links if you want to dig deeper.

**STOMP (Simple Text Oriented Messaging Protocol)**

Comunication protocol which is text based but can send body in binary format. Very fast and reliable. Using stomp instead of a standard websocket is like using TCP instead of HTTP 

- No need to invent a custom messaging protocol and message format.
- STOMP clients are available including a Java client in the Spring Framework.
- Message brokers such as RabbitMQ, ActiveMQ, and others can be used (optionally) to manage subscriptions and broadcast messages.

https://docs.spring.io/spring-framework/docs/4.3.x/spring-framework-reference/html/websocket.html#websocket-stomp-overview

**STOMPjs + SockJS**

STOMPjs -> STOMP JavaScript client

Sockjs -> is a browser JavaScript library that provides a WebSocket-like object, 
	con fallback per browser vecchi 
	senza oggetto websocket nel DOM
	
https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/sockjs.md.html

**RabbitMQ + Stomp PLUGIN**

RabbitMQ - message broker

We Need to use a plugin to connect using STOMP protocol

https://www.rabbitmq.com/stomp.html 

**KONG + Konga**

Api Gateway, which has a premium UI
we will use KONGA wich is an open source project that uses the kong api to configure kong objects as routes and services.

**Nginx**

web server

we will use the nginx capabilities only to provide an https ( with not valid certificate ) connection
and to reverse proxy to the kong api


## SPRING Web Socket Configuration 

The web socket configuration is all centralized in the class [WebSocketConfig.java](https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/spring-ms/src/main/java/com/gpiazzolla/configuration/WebSocketConfig.java)

Also, In this file I have configured a custom channel interceptor to intercept all calls to the web-socket and secure the connection trough JWT checking.
            
    @Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

	   StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

	   logger.info("### ChannelInterceptor execution");
	   logger.info("### Received StompCommand." + accessor.getCommand());
				
	   return handleAuthentication(message, accessor.getNativeHeader("authorization"));
				
	}

## RabbitMQ configuration

We have to configure the rabbit plugin to enable the STOMP protocol. In the docker-file of the rabbit container:

    FROM rabbitmq:management
    RUN rabbitmq-plugins enable --offline rabbitmq_stomp

## KONG Service and Route configuration 
In Kong we have to configure the SERVICE and the ROUTE that will use the service.
We can do this trough Kong Rest API; instead we will use KONGA which has a very nice interface.

After the startup of the containers, the first think to do is to connect to 

    http://localhost:1337/
    
(1337 is the exposed port in docker compose)

    konga:
       build: ./konga
       container_name: konga
       depends_on:
         konga-database:
            condition: service_healthy
       volumes:
         - ./konga/user_seed.js:/app/user_seed.js:ro
    ports:
      - 1337:1337

Then log-in with admin / adminadminadmin then create a new connection with URL http://kong:8001.
(8001 is the exposed API port in docker compose; the 8000 is the port used for the services, 8443 is for https)

    kong:
       build: ./kong
       container_name: kong
       depends_on:
         kong-database:
           condition: service_healthy
       ...
       ports:
         - 8000:8000
         - 8001:8001
         - 8443:8443
       ...


Next we have to configure a route and a service as in the image provided

1) https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/route.png

2) https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/service.png

## Frontend configuration

We will use both STOMP.js and Sock.JS which provides some fallbacks in case of using old web browser to establish web-socket connections; 
so both JS files are included in the [index.html](https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/frontend/index.html).

To establish the connection: 

    connect() {
        var socket = new SockJS('https://localhost:9999/ws-socket-alert',null);
        this.stompClient = Stomp.over(socket);  
        this.stompClient.connect({}, this._onConnected);
    }

As you can see, the path /ws-socket-alert is configured in the [nginx.conf](https://github.com/GaetanoPiazzolla/web-spring-real-time-notification/blob/master/frontend/nginx/nginx.conf) to redirect on kong gateway:

    location /ws-socket-alert {
	    proxy_pass http://kong:8000/ws-socket-alert;
	    ....
	}


## Test the think (video) 

In this video you can see as the application will connect to the web socket, and using a call to the backend spring boot ms application
the notification will be shown to the frontend users.

![](test.gif)


