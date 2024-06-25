from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/sqshq/piggymetrics

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "sqshq_piggymetrics"



"""
Components:
    - configuration server (Spring Cloud Config), "config", port 8888 open
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/java/com/piggymetrics/config/ConfigApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableConfigServer
And file: https://github.com/sqshq/piggymetrics/blob/master/config/Dockerfile
Artifact (line 9):
    EXPOSE 8888
And file: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/application.yml
Artifact (lines[9:11]):
    security:
      user:
        password: ${CONFIG_SERVICE_PASSWORD}
And file: https://github.com/sqshq/piggymetrics/blob/master/.env
Artifact (line 1):
    CONFIG_SERVICE_PASSWORD=password

CSRF:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/java/com/piggymetrics/config/SecurityConfig.java
Artifact (line 15):
    http.csrf().disable();

Basic authentication:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/java/com/piggymetrics/config/SecurityConfig.java
Artifact (line 21):
    .httpBasic()
"""

config = CClass(service, "config", stereotype_instances = [configuration_server, plaintext_credentials, infrastructural, csrf_disabled, basic_authentication], tagged_values = {'Port': 8888, 'Configuration Server': "Spring Cloud Config", 'Username': "user", 'Password': "password"})



"""
Components: Service discovery (Eureka), "registry", port 8761 open
File: https://github.com/sqshq/piggymetrics/blob/master/registry/src/main/java/com/piggymetrics/registry/RegistryApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableEurekaServer
And file: https://github.com/sqshq/piggymetrics/blob/master/registry/Dockerfile
Artifact (line 7):
    EXPOSE 8761
"""

registry = CClass(service, "registry", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component: connection registry to config
File: https://github.com/sqshq/piggymetrics/blob/master/registry/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      [...]
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: registry}, stereotype_instances = [restful_http, plaintext_credentials_link])



"""
Component: monitoring dashboard (Hystrix), "monitoring", port 8080 open
File: https://github.com/sqshq/piggymetrics/blob/master/monitoring/src/main/java/com/piggymetrics/monitoring/MonitoringApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableHystrixDashboard
And file: https://github.com/sqshq/piggymetrics/blob/master/monitoring/Dockerfile
Artifact (line 7):
    EXPOSE 8080
"""

monitoring = CClass(service, "monitoring", stereotype_instances = [monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Dashboard': "Hystrix", 'Port': 8080})



"""
Component: connection monitoring to config
File: https://github.com/sqshq/piggymetrics/blob/master/monitoring/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      fail-fast: [...]
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: monitoring}, stereotype_instances = [restful_http, plaintext_credentials_link])



"""
Components:
    - monitoring server (Turbine), "turbine-stream-service", port 8989 open
    - connection to registry
File: https://github.com/sqshq/piggymetrics/blob/master/turbine-stream-service/src/main/java/com/piggymetrics/turbine/TurbineStreamServiceApplication.java
Artifact (lines[8:10]):
    @SpringBootApplication
    @EnableTurbineStream
    @EnableDiscoveryClient
And file: https://github.com/sqshq/piggymetrics/blob/master/turbine-stream-service/Dockerfile
Artifact (line 7):
    EXPOSE 8989
"""

turbine_stream_service = CClass(service, "turbine-stream-service", stereotype_instances = [monitoring_server, infrastructural], tagged_values = {'Monitoring Server': "Turbine", 'Port': 8989})

add_links({turbine_stream_service: registry}, stereotype_instances = restful_http)



"""
Component: connection turbine-stream-service to config
File: https://github.com/sqshq/piggymetrics/blob/master/turbine-stream-service/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      fail-fast: true
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: turbine_stream_service}, stereotype_instances = [restful_http, plaintext_credentials_link])



"""
Component: connection turbine-stream-service to monitoring
Artifact: implicit when both are used
"""

add_links({turbine_stream_service: monitoring}, stereotype_instances = restful_http)



"""
Components:
    - message broker server (RabbitMQ), "rabbitmq", port 15672 open
File: https://github.com/sqshq/piggymetrics/blob/master/docker-compose.yml
Artifact (lines[3:7])
    rabbitmq:
        image: rabbitmq:3-management
        restart: always
        ports:
            - 15672:15672
And file: https://github.com/sqshq/piggymetrics
Artifact:
"In this project configuration, each microservice with Hystrix on board pushes metrics to Turbine via Spring Cloud Bus (with AMQP broker)"
"""

rabbitmq = CClass(service, "rabbitmq", stereotype_instances = [message_broker, infrastructural], tagged_values = {'Message Broker': "RabbitMQ", 'Port': 15672})

add_links({rabbitmq: turbine_stream_service}, stereotype_instances = restful_http)



"""
Components:
    - authorization service (Spring OAuth2), "auth-service", port 5000 open
    - connection to registry
    - pre-authorized method
    - local logging
    - encryption (Spring Security BCryptPasswordEncoder)
    - token server (Spring OAUth)
    - resource server
Service, connection to registry:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/AuthApplication.java
Artifact (lines[9:12]):
    @SpringBootApplication
    @EnableResourceServer
    @EnableDiscoveryClient
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/config/OAuth2AuthorizationConfig.java
Artifact (line 22):
    @EnableAuthorizationServer
And file: https://github.com/sqshq/piggymetrics/blob/master/auth-service/Dockerfile
Artifact (line 7):
    EXPOSE 5000

Local logging:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/service/UserServiceImpl.java
Artifact (lines[6;17;35]):
import org.slf4j.LoggerFactory;

	private final Logger log = LoggerFactory.getLogger(getClass());

    		log.info("new user has been created: {}", user.getUsername());

Encryption:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/service/UserServiceImpl.java
Artifact (lines[19;30]):
	private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    		String hash = encoder.encode(user.getPassword());

Pre-authorized method:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/controller/UserController.java
Artifact (line 27):
	@PreAuthorize("#oauth2.hasScope('server')")

Token Server:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/account-service.yml
Artifact (line 6):
    accessTokenUri: http://auth-service:5000/uaa/oauth/token

CSRF:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/config/WebSecurityConfig.java
Artifact (line 28):
    .csrf().disable();

Authentication:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/config/WebSecurityConfig.java
Artifact (line 26):
    .authorizeRequests().anyRequest().authenticated()

Endpoints:
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/java/com/piggymetrics/auth/controller/UserController.java
Artifact (line 16):
    @RequestMapping("/users")
And artifact (line 22):
    @RequestMapping(value = "/current", method = RequestMethod.GET)
"""

auth_service = CClass(service, "auth-service", stereotype_instances = [authorization_server, pre_authorized_endpoints, infrastructural, token_server, encryption, local_logging, resource_server, csrf_disabled, authentication_scope_all_requests], tagged_values = {'Authorization Server': "Spring OAuth2", 'Port': 5000, 'Pre-authorized Endpoints': ['/users'], 'Endpoints': "[\'/users\', \'/users/current\']"})

add_links({auth_service: registry}, stereotype_instances = restful_http)



"""
Components:
    - connection auth-service to config
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/auth-service/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
    cloud:
        config:
            uri: http://config:8888
            fail-fast: true
            password: ${CONFIG_SERVICE_PASSWORD}
            username: user
"""

add_links({config: auth_service}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Components:
    - account-service (internal), port 6000 open
    - connection to registry
    - pre-authorized method
    - local logging
    - resource server
    - circuit breaker
Service, connection to registry:
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/AccountApplication.java
Artifact (lines[11:16]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableOAuth2Client
    @EnableFeignClients
    @EnableCircuitBreaker
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/sqshq/piggymetrics/blob/master/account-service/Dockerfile
Artifact (line 7):
    EXPOSE 6000

Pre-authorized method:
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java
Artifact (line 19):
	@PreAuthorize("#oauth2.hasScope('server') or #name.equals('demo')")

Local logging:
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/service/security/CustomUserInfoTokenServices.java
Artifact (line 4):
    import org.apache.commons.logging.LogFactory;
And artifact (line 31):
	   protected final Log logger = LogFactory.getLog(getClass());
And artifact (line 68):
			this.logger.debug("userinfo returned error: " + map.get("error"));

Resource Server:
File: https://github.com/sqshq/piggymetrics/blob/fd5ee3c555ea9cd6067eacf3f2a3e8b85fe4fe77/account-service/src/main/java/com/piggymetrics/account/config/ResourceServerConfig.java
Artifact (line 23):
    @EnableResourceServer

Circuit Breaker:
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/pom.xml
Artifact (line 61):
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>

Endpoints:
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java
Artifact (line 20):
    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
And artifact (line 25):
    @RequestMapping(path = "/current", method = RequestMethod.GET)
And artifact (line 35):
    @RequestMapping(path = "/", method = RequestMethod.POST)
And file: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java
Artifact (line 12):
    @RequestMapping(method = RequestMethod.POST, value = "/uaa/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
And file: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java
Artifact (line 13):
    @RequestMapping(method = RequestMethod.PUT, value = "/statistics/{accountName}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
"""

account_service = CClass(service, "account-service", stereotype_instances = [internal, pre_authorized_endpoints, local_logging, resource_server, circuit_breaker], tagged_values = {'Port': 6000, 'Pre-authorized Endpoints': ['/{name}'], 'Circuit Breaker': "Hystrix", 'Endpoints': "[\'/{name}\', \'/\', \'/uaa/users\', \'/statistics/{accountName}\', \'/current\']"})

add_links({account_service: registry}, stereotype_instances = [restful_http, circuit_breaker_link], tagged_values = {'Circuit Breaker': "Hystrix"})



"""
Components:
    - connection account-service to auth-service
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/account-service.yml
Artifact (lines[1:8]):
    security:
        oauth2:
            client:
                clientId: account-service
                clientSecret: ${ACCOUNT_SERVICE_PASSWORD}
                accessTokenUri: http://auth-service:5000/uaa/oauth/token
                grant-type: client_credentials
                scope: server
And file: https://github.com/sqshq/piggymetrics/blob/master/.env
Artifact (line 4):
    ACCOUNT_SERVICE_PASSWORD=password
"""

add_links({auth_service: account_service}, stereotype_instances = [restful_http, plaintext_credentials_link, auth_provider, authentication_with_plaintext_credentials])



"""
Components:
    - connection account-service to auth-service
    - load balancer (Ribbon)
File: https://github.com/sqshq/piggymetrics/blob/fd5ee3c555ea9cd6067eacf3f2a3e8b85fe4fe77/account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java
Artifact (line 9):
    @FeignClient(name = "auth-service")
"""

add_links({account_service: auth_service}, stereotype_instances = [restful_http, feign_connection, load_balanced_link], tagged_values = {'Load Balancer': "Ribbon"})



"""
Component: connection account-service to config
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      fail-fast: true
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: account_service}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Component: connection account-service to rabbitmq (to turbine-stream-service)
File: https://github.com/sqshq/piggymetrics/blob/fd5ee3c555ea9cd6067eacf3f2a3e8b85fe4fe77/account-service/pom.xml
Artifact (lines[63:66]):
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
	</dependency>
"""

add_links({account_service: rabbitmq}, stereotype_instances = [restful_http, circuit_breaker_link])



"""
Components:
    - notification-service (internal), port 8000 open
    - connection to registry
    - local logging
    - resource server
Service, connection to registry,
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/NotificationServiceApplication.java
Artifact (lines[18:22]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableOAuth2Client
    @EnableFeignClients
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/sqshq/piggymetrics/blob/master/notification-service/Dockerfile
Artifact (line 7):
    EXPOSE 8000

Local logging:
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/service/EmailServiceImpl.java
Artifact (lines[6;25;52]):
    import org.slf4j.LoggerFactory;
    	private final Logger log = LoggerFactory.getLogger(getClass());
        		log.info("{} email notification has been send to {}", type, recipient.

Resource server:
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/config/ResourceServerConfig.java
Artifact (line 18):
    @EnableResourceServer

OAuth:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/notification-service.yml
Artifact (lines[1:8]):
    security:
      oauth2:
        client:
          clientId: notification-service
          clientSecret: ${NOTIFICATION_SERVICE_PASSWORD}
          accessTokenUri: http://auth-service:5000/uaa/oauth/token
          grant-type: client_credentials
          scope: server

Endpoints:
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/client/AccountServiceClient.java
Artifact (line 12):
    @RequestMapping(method = RequestMethod.GET, value = "/accounts/{accountName}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
And file: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/controller/RecipientController.java
Artifact (line 15):
    @RequestMapping("/recipients")
And artifact (line 21):
    @RequestMapping(path = "/current", method = RequestMethod.GET)
"""

notification_service = CClass(service, "notification-service", stereotype_instances = [internal, local_logging, resource_server], tagged_values = {'Port': 8000, 'Endpoints': "[\'/accounts/{accountName}\', \'/recipients/current\', \'/recipients\']"})

add_links({notification_service: registry}, stereotype_instances = restful_http)



"""
Components:
    - connection notification-service to auth-service
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/notification-service.yml
Artifact (lines[1:8]):
    security:
        oauth2:
            client:
                clientId: notification-service
                clientSecret: ${NOTIFICATION_SERVICE_PASSWORD}
                accessTokenUri: http://auth-service:5000/uaa/oauth/token
                grant-type: client_credentials
                scope: server
And file: https://github.com/sqshq/piggymetrics/blob/master/.env
Artifact (line 2):
    NOTIFICATION_SERVICE_PASSWORD=password
"""

add_links({auth_service: notification_service}, stereotype_instances = [restful_http, plaintext_credentials_link, auth_provider, authentication_with_plaintext_credentials])



"""
Component: connection notification-service to config
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      fail-fast: true
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: notification_service}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Component: connection notification-service to rabbitmq (to turbine-stream-service)
File: https://github.com/sqshq/piggymetrics/blob/fd5ee3c555ea9cd6067eacf3f2a3e8b85fe4fe77/notification-service/pom.xml
Artifact (lines[59:62]):
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
	</dependency>
"""

add_links({notification_service: rabbitmq}, stereotype_instances = restful_http)



"""
Components:
    - external mail server gmail
    - connection notification-service to gmail
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/notification-service.yml
Artifact (lines[36:44]):
  mail:
    host: smtp.gmail.com
    port: 465
    username: dev-user
    password: dev-password
    properties:
      mail:
        smtp:
          auth: true

"""

mail_server = CClass(external_component, "mail-server", stereotype_instances = [mail_server, plaintext_credentials, exitpoint, entrypoint], tagged_values = {'Mail Server': "Gmail", 'Host': "smtp.gmail.com", 'Port': 465, 'Username': "dev-user", 'Password': "dev-password"})

add_links({notification_service: mail_server}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Components:
    - statistics-service (internal), port 7000 open
    - connection to registry
    - local logging
    - Pre-authorized method
    - resource server
File: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/StatisticsApplication.java
Artifact (lines[21:25]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableOAuth2Client
    @EnableFeignClients
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/Dockerfile
Artifact (line 7):
    EXPOSE 7000

Local logging:
File:
Artifact (lines[11;30;75]):
    import org.slf4j.LoggerFactory;
    	private final Logger log = LoggerFactory.getLogger(getClass());
        		log.debug("new datapoint has been created: {}", pointId);

Pre-authorized method:
File: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/controller/StatisticsController.java
Artfiact (line 25):
	@PreAuthorize("#oauth2.hasScope('server') or #accountName.equals('demo')")

Resource Server:
File: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/config/ResourceServerConfig.java
Artifact (line 15):
    @EnableResourceServer

OAuth:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/statistics-service.yml
Artifact (lines[1:8]):
    security:
      oauth2:
        client:
          clientId: statistics-service
          clientSecret: ${STATISTICS_SERVICE_PASSWORD}
          accessTokenUri: http://auth-service:5000/uaa/oauth/token
          grant-type: client_credentials
          scope: server

Endpoints:
File: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/controller/StatisticsController.java
Artifact (line 20):
    @RequestMapping(value = "/current", method = RequestMethod.GET)
And artifact (line 26):
    @RequestMapping(value = "/{accountName}", method = RequestMethod.GET)
And file: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/client/ExchangeRatesClient.java
Artifact (line 13):
    @RequestMapping(method = RequestMethod.GET, value = "/latest")
"""

statistics_service = CClass(service, "statistics-service", stereotype_instances = [internal, local_logging, pre_authorized_endpoints, resource_server], tagged_values = {'Port': 7000, 'Pre-authorized Endpoints': ['/{accountName}'], 'Endpoints': "[\'/latest\', \'/current\', \'/{accountName}\']"})

add_links({statistics_service: registry}, stereotype_instances = restful_http)



"""
Components:
    - connection statistics-service to auth-service
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/statistics-service.yml
Artifact (lines[1:8]):
    security:
        oauth2:
            client:
                clientId: statistics-service
                clientSecret: ${STATISTICS_SERVICE_PASSWORD}
                accessTokenUri: http://auth-service:5000/uaa/oauth/token
                grant-type: client_credentials
                scope: server
And file: https://github.com/sqshq/piggymetrics/blob/master/.env
Artifact (line 3):
    STATISTICS_SERVICE_PASSWORD=password
"""

add_links({auth_service: statistics_service}, stereotype_instances = [restful_http, plaintext_credentials_link, auth_provider, authentication_with_plaintext_credentials])



"""
Components:
    - connection statistics-service to config
    - plaintext_credentials
File: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
  cloud:
    config:
      uri: http://config:8888
      fail-fast: true
      password: ${CONFIG_SERVICE_PASSWORD}
      username: user
"""

add_links({config: statistics_service}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Component: connection statistics-service to rabbitmq (to turbine-stream-service)
File: https://github.com/sqshq/piggymetrics/blob/fd5ee3c555ea9cd6067eacf3f2a3e8b85fe4fe77/statistics-service/pom.xml
Artifact (lines[59:62]):
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
	</dependency>
"""

add_links({statistics_service: rabbitmq}, stereotype_instances = restful_http)



"""
Components:
    - external website "exchangeratesapi"
    - connection statistics-service to external website
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/statistics-service.yml
Artifact (lines[24:25]):
    rates:
      url: https://api.exchangeratesapi.io
And file: https://github.com/sqshq/piggymetrics/blob/master/statistics-service/src/main/java/com/piggymetrics/statistics/client/ExchangeRatesClient.java
Artifact (line 10):
    @FeignClient(url = "${rates.url}", name = "rates-client", fallback = ExchangeRatesClientFallback.class)
"""

external_website = CClass(external_component, "external-website", stereotype_instances = [external_website, entrypoint, exitpoint], tagged_values = {'URL': "https://api.exchangeratesapi.io"})

add_links({external_website: statistics_service}, stereotype_instances = restful_http)



"""
Components:
    - connection account-service to statistics-service
    - circuit breaker
    - load balanced (Ribbon)
File: https://github.com/sqshq/piggymetrics/blob/master/account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java
Artifact (line 10):
    @FeignClient(name = "statistics-service", fallback = StatisticsServiceClientFallback.class)
And file: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/account-service.yml
Artifact (lines[24:26]):
    feign:
      hystrix:
        enabled: true
"""

add_links({account_service: statistics_service}, stereotype_instances = [restful_http, feign_connection, circuit_breaker_link, load_balanced_link], tagged_values = {'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon"})



"""
Component:
    - connection notification-service to account-service
    - load balanced (Ribbon)
File: https://github.com/sqshq/piggymetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/client/AccountServiceClient.java
Artifact (line 9):
    @FeignClient(name = "account-service")
"""

add_links({notification_service: account_service}, stereotype_instances = [restful_http, feign_connection, load_balanced_link], tagged_values = {'Load Balancer': "Ribbon"})



"""
Components:
    - auth database (MongoDB), "auth-mongodb"
    - account database (MongoDB), "account-mongodb"
    - statistics database (MongoDB), "statistics-mongodb"
    - notification database (MongoDB), "notification-mongodb"
    - plaintext credentials for all these
File: https://github.com/sqshq/piggymetrics/blob/master/.env
Artifact (line 5):
    MONGODB_PASSWORD=password
And file: https://github.com/sqshq/piggymetrics/blob/master/mongodb/Dockerfile
Artifact (line 1):
    FROM mongo:3
And file: https://github.com/sqshq/piggymetrics/blob/master/docker-compose.yml
Artifact (lines[70:73]):
    auth-mongodb:
        environment:
            MONGODB_PASSWORD: $MONGODB_PASSWORD
        image: sqshq/piggymetrics-mongodb
And artifact (lines[95:99]):
    account-mongodb:
        environment:
            INIT_DUMP: account-service-dump.js
            MONGODB_PASSWORD: $MONGODB_PASSWORD
        image: sqshq/piggymetrics-mongodb
And artifact (lines[121:124]):
    statistics-mongodb:
        environment:
            MONGODB_PASSWORD: $MONGODB_PASSWORD
        image: sqshq/piggymetrics-mongodb
And artifact (lines[146:150]):
    notification-mongodb:
        image: sqshq/piggymetrics-mongodb
        [...]
        environment:
            MONGODB_PASSWORD: $MONGODB_PASSWORD
"""

auth_mongodb = CClass(database_component, "auth-mongodb", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MongoDB", 'Username': "user", 'Password': "password"})

account_mongodb = CClass(database_component, "account-mongodb", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MongoDB", 'Username': "user", 'Password': "password"})

statistics_mongodb = CClass(database_component, "statistics-mongodb", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MongoDB", 'Username': "user", 'Password': "password"})

notification_mongodb = CClass(database_component, "notification-mongodb", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MongoDB", 'Username': "user", 'Password': "password"})



"""
Component: connection account-service to account-mongodb
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/account-service.yml
Artifact (lines[10:17]):
    spring:
      data:
        mongodb:
          host: account-mongodb
          username: user
          password: ${MONGODB_PASSWORD}
          database: piggymetrics
          port: 27017
"""

add_links({account_mongodb: account_service}, stereotype_instances = [jdbc, plaintext_credentials_link])



"""
Component: connection notification-service to notification-mongodb
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/notification-service.yml
Artifact (lines[28:35]):
    spring:
      data:
        mongodb:
          host: notification-mongodb
          username: user
          password: ${MONGODB_PASSWORD}
          database: piggymetrics
          port: 27017
"""

add_links({notification_mongodb: notification_service}, stereotype_instances = [jdbc, plaintext_credentials_link])



"""
Component: connection statistics-service to statistics-mongodb
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/statistics-service.yml
Artifact (lines[10:17]):
    spring:
      data:
        mongodb:
          host: statistics-mongodb
          username: user
          password: ${MONGODB_PASSWORD}
          database: piggymetrics
          port: 27017
"""

add_links({statistics_mongodb: statistics_service}, stereotype_instances = [jdbc, plaintext_credentials_link])



"""
Component: connection auth-service to auth-mongodb
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/auth-service.yml
Artifact (lines[1:8]):
    spring:
      data:
        mongodb:
          host: auth-mongodb
          username: user
          password: ${MONGODB_PASSWORD}
          database: piggymetrics
          port: 27017
"""

add_links({auth_mongodb: auth_service}, stereotype_instances = [jdbc, plaintext_credentials_link])



"""
Components:
    - API-Gateway (Zuul), "gateway", port 4000 open
    - load balancer (built in for Zuul)
    - connection to registry
    - user (implicit with gateway)
    - connection user to gateway
File: https://github.com/sqshq/piggymetrics/blob/master/gateway/src/main/java/com/piggymetrics/gateway/GatewayApplication.java
Artifact (lines[8:10]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableZuulProxy
And file: https://github.com/sqshq/piggymetrics/blob/master/gateway/Dockerfile
Artifact (line 7):
    EXPOSE 4000
"""

gateway = CClass(service, "gateway", stereotype_instances = [gateway, infrastructural, load_balancer], tagged_values = {'Gateway': "Zuul", 'Port': 4000, 'Load Balancer': "Ribbon"})

add_links({registry: gateway}, stereotype_instances = restful_http)

user = CClass(external_component, "user", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: gateway}, stereotype_instances = restful_http)

add_links({gateway: user}, stereotype_instances = restful_http)


"""
Components:
    - connection gateway to config
    - plaintext credentials
File: https://github.com/sqshq/piggymetrics/blob/master/gateway/src/main/resources/bootstrap.yml
Artifact (lines[4:9]):
    cloud:
        config:
            uri: http://config:8888
            fail-fast: true
            password: ${CONFIG_SERVICE_PASSWORD}
            username: user
"""

add_links({config: gateway}, stereotype_instances = [plaintext_credentials_link, restful_http])



"""
Components:
    - connection gateway to auth-service
    - connection gateway to account-service
    - connection gateway to statistics-service
    - connection gateway to notification-service
    - circuit breaker for these links
    - load balancer or these links
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/gateway.yml
Artifact (lines[13:40]):
zuul:
  ignoredServices: '*'
  [...]
  routes:
    auth-service:
        path: /uaa/**
        url: http://auth-service:5000
        [...]
    account-service:
        path: /accounts/**
        serviceId: account-service
        [...]
    statistics-service:
        path: /statistics/**
        serviceId: statistics-service
        [...]
    notification-service:
        path: /notifications/**
        serviceId: notification-service

Circuit breaker:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/gateway.yml
Artifact (lines[1:7]):
    hystrix:
        command:
            default:
                execution:
                    isolation:
                        thread:
                            timeoutInMilliseconds: 20000

Load balancer:
File: https://github.com/sqshq/piggymetrics/blob/master/config/src/main/resources/shared/gateway.yml
Artifact (lines 9:11):
    ribbon:
        ReadTimeout: 20000
        ConnectTimeout: 20000
"""

add_links({gateway: account_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})

add_links({gateway: statistics_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})

add_links({gateway: notification_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})

add_links({gateway: auth_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link, auth_provider], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})



##### Create model
model = CBundle(model_name, elements = gateway.class_object.get_connected_elements())


def run():

    generator = PlantUMLGenerator()
    generator.plant_uml_jar_path = plantuml_path
    generator.directory = output_directory
    generator.object_model_renderer.left_to_right = True
    generator.generate_object_models(model_name, [model, {}])

    print(f"Generated models in {generator.directory!s}/" + model_name)


if __name__ == "__main__":
    run()




#
