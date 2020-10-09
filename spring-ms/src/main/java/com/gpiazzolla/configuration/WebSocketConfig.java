package com.gpiazzolla.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${spring.rabbitmq.username:pino}")
	private String userName;
	@Value("${spring.rabbitmq.password:pino}")
	private String password;
	@Value("${spring.rabbitmq.host:localhost}")
	private String host;
	@Value("${spring.rabbitmq.port:61613}")
	private int port;

	/**
	 * this is the HTTP URL for the endpoint to which a WebSocket (or SockJS) client
	 * needs to connect for the WebSocket handshake.
	 */
	@Value("${endpoint:/ws-socket-alert}")
	private String endpoint;

	/**
	 * STOMP messages whose destination header begins with /app are routed to
	 * 
	 * @MessageMapping methods in @Controller classes.
	 */
	@Value("${destination.prefix:/app}")
	private String destinationPrefix;

	/**
	 * TOPIC o QUEUE non significano niente a livello di STOMP, e' rabbitMQ che gli
	 * da un significato https://www.rabbitmq.com/stomp.html#
	 */
	@Value("${stomp.broker.relay:/topic}")
	private String stompBrokerRelay;

	protected Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {

		config.setApplicationDestinationPrefixes(destinationPrefix);

		/**
		 * Stiamo usando un broker smtp esterno (RabbitMQ con STOMP supporto abilitato)
		 * 
		 * Establishes TCP connections to the broker, forwards all messages to it, and
		 * then forwards all messages received from the broker to clients through their
		 * WebSocket sessions. Essentially, it acts as a relay that forwards messages in
		 * both directions.
		 **/
		config.enableStompBrokerRelay(stompBrokerRelay)

		.setRelayHost(host).setRelayPort(port)
		.setSystemLogin(userName).setSystemPasscode(password)
		.setClientLogin(userName).setClientPasscode(password);

	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {

		registry.addEndpoint(endpoint).setAllowedOrigins("*").withSockJS();

	}

	
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
         
		registration.interceptors(new ChannelInterceptor() {

			/**
			 * If PRE-SEND method returns {@code null} then the actual send invocation will not occur.
			 */
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {

				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				logger.info("### ChannelInterceptor execution");
				logger.info("### Received StompCommand." + accessor.getCommand());
				
				return handleAuthentication(message, accessor.getNativeHeader("authorization"));
				
			}

			/**
			 * return null if the token is invalid
			 */
			private Message<?> handleAuthentication(Message<?> message, List<String> authenticationHeaders) {
				
				if (!CollectionUtils.isEmpty(authenticationHeaders)) {
			
					String token = authenticationHeaders.get(0);
					
					//TODO call jwt utils library
					if(token.equals("jwt-token"))
					{
						logger.info("valid token for ws connection");
						return message;
					}
				}
				
				logger.info("invalid token for ws connection");
				return null;
				
			}		
			
		});
	}
	
}