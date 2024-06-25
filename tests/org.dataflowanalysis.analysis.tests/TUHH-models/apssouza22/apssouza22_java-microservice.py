from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/apssouza22/java-microservice

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "apssouza22_java-microservice"


"""
Components:
    - Config server (Spring Cloud Config), "config-server", port 8888
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/java/com/apssouza/config/Application.java
Artifact (line 8):
    @EnableConfigServer
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/application.yml
Artifact (lines[2:3]):
    spring.application.name: config-server
    server.port: 8888
"""

config_server = CClass(service, "config-server", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 8888})



"""
Component: Admin-server (Spring Boot Admin), "admin", port 8026
File: https://github.com/apssouza22/java-microservice/blob/master/admin-server/src/main/java/de/codecentric/boot/admin/SpringBootAdminApplication.java
Artifact (line):
    @EnableAdminServer
And file: https://github.com/apssouza22/java-microservice/blob/master/admin-server/src/main/resources/application.yml
Artifact (lines [1:3]):
    spring:
      application:
        name: admin
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/admin.yml
Artifact (lines [1:2]):
    server:
      port: 8026
"""

admin = CClass(service, "admin", stereotype_instances = [administration_server, infrastructural], tagged_values = {'Administration Server': "Spring Boot Admin", 'Port': 8026})



"""
Component: connection admin to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/admin-server/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({admin: config_server}, stereotype_instances = restful_http)



"""
Component: service discovery (Eureka), "eureka-server", ports 8010, 8011, 8012, 8013
File: https://github.com/apssouza22/java-microservice/blob/master/eureka-server/src/main/java/com/apssouza/discovery/Application.java
Artifact (line 8):
    @EnableEurekaServer
And file: https://github.com/apssouza22/java-microservice/blob/master/eureka-server/src/main/resources/application.yml
Artifact (lines[2:4]):
    spring:
      application:
        name: eureka-server
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/eureka-server.yml
Artifact (lines [9:10;33:34;43:44;53:54]):
    server:
        port: 8010

    server:
        port: 8011

    server:
        port: 8012

    server:
        port: 8013
"""

eureka_server = CClass(service, "eureka-server", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8010})



"""
Component: config-server to eureka-server
File: https://github.com/apssouza22/java-microservice/blob/master/eureka-server/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: eureka_server}, stereotype_instances = restful_http)



"""
Component: connection eureka-server to admin
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/eureka-server.yml
Artifact (lines [3:6]):
    spring:
      boot:
        admin:
          url: http://admin:8026,http://localhost:8026
"""

add_links({eureka_server: admin}, stereotype_instances = restful_http)



"""
Components:
    - user-service (internal), port 8016
    - local logging
File: https://github.com/apssouza22/java-microservice/blob/master/user-service/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name = user
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/user.properties
Artifact (line 1):
    server.port = 8016

Local logging:
File: https://github.com/apssouza22/java-microservice/blob/master/user-service/src/main/java/com/apssouza/bootstrap/UserLoader.java
Artifact (lines[3;21;38]):
    import org.apache.log4j.Logger;

    private final Logger log = Logger.getLogger(this.getClass().getCanonicalName());

    log.info("Created the todos.");
"""

user_service = CClass(service, "user", stereotype_instances = internal, tagged_values = {'Port': 8016})



"""
Component: admin-server to connection user-service
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/user.properties
Artfiact (line 15):
    spring.boot.admin.url=http://admin:8026,http://localhost:8026
"""

add_links({admin: user_service}, stereotype_instances = restful_http)



"""
Component: connection user-service to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/user-service/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: user_service}, stereotype_instances = restful_http)



"""
Component: Message broker (Kafka) where topic "todo-mail" has 1 partition and 1 replica, "kafka", port 9092
File: https://github.com/apssouza22/java-microservice/blob/master/docker-compose.yml
Artifact (lines[134:143]):
    kafka:
        image: wurstmeister/kafka
        ports:
          - 9092
        [...]
        environment:
          KAFKA_ADVERTISED_HOST_NAME: 172.19.0.1
          KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
          KAFKA_CREATE_TOPICS: "todo-mail:1:1"
"""

kafka = CClass(service, "kafka", stereotype_instances = [message_broker, infrastructural], tagged_values = {'Message Broker': "Kafka", 'Port': 9092})



"""
Components:
    - mailer (internal), port 8020
    - local logging
File: https://github.com/apssouza22/java-microservice/blob/master/mail-service/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name=mailer
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/mailer.properties
Artifact (line 11):
    server.port=8020

Local logging:
File: https://github.com/apssouza22/java-microservice/blob/master/mail-service/src/main/java/com/apssouza/mailservice/integration/reminder/EventInput.java
Artifact (lines [9;24;38]):
    import org.apache.log4j.Logger;

        Logger LOG = Logger.getLogger(EventInput.class);

            LOG.info("Todo created");
"""

mailer = CClass(service, "mailer", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8020})



"""
Component: connection mailer to admin
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/mailer.properties
Artifact (line 8):
    spring.boot.admin.url=http://admin:8026,http://localhost:8026
"""

add_links({admin: mailer}, stereotype_instances = restful_http)



"""
Component: connection mailer to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/mail-service/src/main/resources/bootstrap.yml
Artifact (lines[2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: mailer}, stereotype_instances = restful_http)



"""
Component: connection mailer to eureka
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/mailer.properties
Artifact (line 5):
    eureka.client.serviceUrl.defaultZone = http://eureka:8010/eureka/,http://localhost:8011/eureka/
"""

add_links({mailer: eureka_server}, stereotype_instances = restful_http)



"""
Component: connection kafka to mailer, topic "todo-mail"
File: https://github.com/apssouza22/java-microservice/blob/master/mail-service/src/main/java/com/apssouza/mailservice/integration/reminder/EventInput.java
Artifact (lines 33):
    @StreamListener
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/mailer.properties
Artifact (line 16):
    spring.cloud.stream.bindings.input.destination=todo-mail
And artifact (line 19):
    spring.cloud.stream.kafka.binder.brokers=kafka
"""

add_links({kafka: mailer}, stereotype_instances = [restful_http, message_consumer_kafka], tagged_values = {'Consumer Topic': "todo-mail"})



"""
Comopnents:
    - reminder service (internal), port 8015
    - local logging
File: https://github.com/apssouza22/java-microservice/blob/master/remainder-service/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name = reminder
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/reminder.properties
Artifact (line 1):
    server.port = 8015

Local logging:
File: https://github.com/apssouza22/java-microservice/blob/master/remainder-service/src/main/java/com/apssouza/bootstrap/TodoLoader.java
Artifact (lines [9;24;42]):
    import org.apache.log4j.Logger;

        private final Logger log = Logger.getLogger(this.getClass().getCanonicalName());

            log.info("Created the to-dos.");
"""

reminder = CClass(service, "reminder", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8015})



"""
Component: connection reminder to eureka
File: https://github.com/apssouza22/java-microservice/blob/master/remainder-service/src/main/java/com/apssouza/configuration/ServiceDiscoveryConfiguration.java
Artifact (line 12):
    @EnableDiscoveryClient
"""

add_links({reminder: eureka_server}, stereotype_instances = restful_http)



"""
Component: connection reminder to admin-server
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/reminder.properties
Artifact (line 14):
    spring.boot.admin.url=http://admin:8026,http://localhost:8026
"""

add_links({admin: reminder}, stereotype_instances = restful_http)



"""
Component: connection reminder to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/remainder-service/src/main/resources/bootstrap.yml
Artifact (lines[2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: reminder}, stereotype_instances = restful_http)



"""
Component: connection reminder to kafka
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/remainder-service/src/main/java/com/apssouza/integrations/socket/TodoSocketController.java
Artifact (line 117):
    @SendTo("/topic/todos")
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/reminder.properties
Artifact (line 21):
    spring.cloud.stream.bindings.output.destination=todo-mail
And artifact (line 25):
    spring.cloud.stream.kafka.binder.brokers=kafka
"""

add_links({reminder: kafka}, stereotype_instances = [restful_http, message_producer_kafka], tagged_values = {'Producer Topic': "todo-mail"})



"""
Components:
    - "api-gateway", internal, port 8018
    - circuit breaker (Hystrix)
    - Resource server
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name = api-gateway
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/api-gateway.properties
Artifact (line 1):
    server.port = 8018

Hystrix
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/java/com/apssouza/BasicApplication.java
Artifact (line10):
    @EnableHystrix

Resource Server:
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/java/com/apssouza/configuration/OAuth2ResourceServerConfiguration.java
Artifact (line 19):
    @EnableResourceServer

CSRF:
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/java/com/apssouza/configuration/OAuth2ResourceServerConfiguration.java
Artifact (lines [24:25]):
    http
        .csrf().disable()
"""

api_gateway = CClass(service, "api-gateway", stereotype_instances = [gateway, circuit_breaker, resource_server, internal, csrf_disabled], tagged_values = {'Circuit Breaker': "Hystrix", 'Port': 8018})



"""
Component: connection api-gateway to eureka-server
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/java/com/apssouza/configuration/ServiceDiscoveryConfiguration.java
Artifact (line 12):
    @EnableDiscoveryClient
"""

add_links({eureka_server: api_gateway}, stereotype_instances = restful_http)



"""
Component: connection api-gateway to admin-server
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/api-gateway.properties
Artifact (line 15):
    spring.boot.admin.url=http://admin:8026,http://localhost:8026
"""

add_links({admin: api_gateway}, stereotype_instances = restful_http)



"""
Component: connection api-gateway to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/resources/bootstrap.yml
Artifact (lines[2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - Connection api-gateway to user-service

File: https://github.com/apssouza22/java-microservice/blob/master/api-gateway/src/main/java/com/apssouza/clients/UserClient.java
Artifact (line 16):
    @FeignClient("user")
"""

add_links({api_gateway: user_service}, stereotype_instances = [restful_http, load_balanced_link, feign_connection], tagged_values = {'Load Balancer': "Ribbon"})



"""
Components:
    - authorization server (Spring OAuth), "oauth", port 8017
    - circuit breaker (Hystrix)
    - tokenstore
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/java/com/apssouza/configuration/OAuth2ServerConfiguration.java
Artifact (line 24):
    @EnableAuthorizationServer
And file: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name = oauth
And file: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/oauth.properties
Artifact (line 1):
    server.port = 8017

Hystrix:
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/java/com/apssouza/BasicApplication.java
Artifact (line 9):
    @EnableHystrix

Tokenstore:
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/java/com/apssouza/configuration/JwtServerConfiguration.java
Artifact (line 22):
    public TokenStore tokenStore() {
"""

oauth = CClass(service, "oauth", stereotype_instances = [authorization_server, tokenstore, infrastructural, circuit_breaker], tagged_values = {'Authorization Server': "Spring OAuth2", 'Port': 8017, 'Circuit Breaker': "Hystrix"})



"""
Component: connection oauth to config-server
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
      cloud:
        config:
          uri: http://config:8888
"""

add_links({config_server: oauth}, stereotype_instances = restful_http)



"""
Component: connection oauth to user-service
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/java/com/apssouza/clients/UserClient.java
Artifact (line 15):
    @FeignClient("user")
"""

add_links({oauth: user_service}, stereotype_instances = [restful_http, auth_provider, load_balanced_link, feign_connection], tagged_values = {'Load Balancer': "Ribbon"})



"""
Component: connection oauth to eureka-server:
File: https://github.com/apssouza22/java-microservice/blob/master/oauth-server/src/main/java/com/apssouza/configuration/ServiceDiscoveryConfiguration.java
Artifact (line 11):
    @EnableDiscoveryClient
"""

add_links({oauth: eureka_server}, stereotype_instances = restful_http)



"""
Component: connection oauth to admin-server
File: https://github.com/apssouza22/java-microservice/blob/master/config-server/src/main/resources/offline-repository/oauth.properties
Artifact (line 15):
    spring.boot.admin.url=http://admin:8026,http://localhost:8026
"""

add_links({admin: oauth}, stereotype_instances = restful_http)



"""
Components:
    - web-app (Nginx), "proxy", port 80
    - user and connection to it (implicit)
File: https://github.com/apssouza22/java-microservice/blob/master/proxy/Dockerfile
Artifact (line 3):
    FROM nginx:latest
And file: https://github.com/apssouza22/java-microservice/blob/master/proxy/default.conf
Artifact (line 8):
    listen        80;
"""

proxy = CClass(service, "proxy", stereotype_instances = [web_application, infrastructural], tagged_values = {'Web Application': "Nginx", 'Port': 80})

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: proxy}, stereotype_instances = restful_http)

add_links({proxy: user}, stereotype_instances = restful_http)



"""
Component: connection proxy to gateway
File: https://github.com/apssouza22/java-microservice/blob/master/proxy/default.conf
Artifact (line 4):
    server gateway:8018;
"""

add_links({proxy: api_gateway}, stereotype_instances = restful_http)



"""
Component: Config Server (Apache ZooKeeper), zookeeper, port 2181
File: https://github.com/apssouza22/java-microservice/blob/master/docker-compose.yml
Artifact (lines[125:126]):
    zookeeper:
        image: wurstmeister/zookeeper
"""

zookeeper = CClass(service, "zookeeper", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "ZooKeeper", 'Port': 2181})



"""
Component: connection from kafka to ZooKeeper
File: https://github.com/apssouza22/java-microservice/blob/master/docker-compose.yml
Artifact (lines[138:139]):
    depends_on:
      - zookeeper
And: implicit; Kafka always needs ZooKeeper to store topics, partitions, etc.
"""

add_links({zookeeper: kafka}, stereotype_instances = restful_http)



"""
Components:
    - external logging server, port 5044
    - connections from proxy, user_service, reminder, eureka_server, gateway, oauth, admin, config_server, and mailer to external logging server
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/proxy/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/user-service/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/remainder-service/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/eureka-server/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/api-gateway/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/oauth-server/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/admin-server/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/config-server/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
File: https://github.com/apssouza22/java-microservice/blob/9274eeb9189dfea0722ac0263814822b94b83ecc/mail-service/src/main/resources/docker/filebeat.yml
Artifact (lines [1:5]):
    output:
      logstash:
        enabled: true
        hosts:
          - elk:5044
"""

logstash = CClass(external_component, "logstash", stereotype_instances = [logging_server, exitpoint], tagged_values = {'Logging Server': "Logstash", 'Port': 5044})

add_links({proxy: logstash}, stereotype_instances = restful_http)

add_links({user_service: logstash}, stereotype_instances = restful_http)

add_links({reminder: logstash}, stereotype_instances = restful_http)

add_links({eureka_server: logstash}, stereotype_instances = restful_http)

add_links({api_gateway: logstash}, stereotype_instances = restful_http)

add_links({oauth: logstash}, stereotype_instances = restful_http)

add_links({admin: logstash}, stereotype_instances = restful_http)

add_links({config_server: logstash}, stereotype_instances = restful_http)

add_links({mailer: logstash}, stereotype_instances = restful_http)



"""
Component: jmx_monitoring (internal)
File: https://github.com/apssouza22/java-microservice/blob/master/jmx-monitoring/pom.xml
"""

jmx_monitoring = CClass(service, "jmx-monitoring", stereotype_instances = internal)



"""
Component: todo_infra (internal)
File: https://github.com/apssouza22/java-microservice/blob/master/todo-infra/pom.xml
"""

todo_infra = CClass(service, "todo-infra", stereotype_instances = internal)


##### Create model
model = CBundle(model_name, elements = admin.class_object.get_connected_elements())

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
