from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Sources:
# - https://github.com/fernandoabcampos/spring-netflix-oss-microservices
# - https://fernandoabcampos.wordpress.com

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "fernandoabcampos_spring-netflix-oss-microservices"


######### Create model components

"""
Components:
    - Config Server (Spring Cloud Config), "config-server", port 9090
    - external GitHub repo  https://github.com/fernandoabcampos/microservices-config.git
    - connection config to git repo
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/config-server/src/main/java/com/spring/netflix/oss/microservices/ConfigServerApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableConfigServer
And file:
Artifact (lines[3:12]):
    server:
      port: 9090

    spring:
      cloud:
        config:
          server:
            git:
               uri: https://github.com/fernandoabcampos/microservices-config.git
               searchPaths: MASTER
"""

config_server = CClass(service, "config-server", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 9090})

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/fernandoabcampos/microservices-config.git"})

add_links({github_repository: config_server}, stereotype_instances = restful_http, tagged_values = {'Protocol' : "HTTPS"})



"""
Component: Discovery Server (Eureka), "discovery-service", port 8761
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/discovery-service/src/main/java/com/spring/netflix/oss/microservices/DiscoveryServiceApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableEurekaServer
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/discovery-service/Dockerfile
Artifact (line 8):
    EXPOSE 8761
"""

discovery_service = CClass(service, "discovery-service", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component: connection discovery service to config server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/discovery-service/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: discovery-service
      cloud:
        config:
          uri: http://localhost:9090
"""

add_links({config_server: discovery_service}, stereotype_instances = restful_http)



"""
Component: Message Broker Server (RabbitMQ), "rabbit-mq-server", ports 5672, 15672
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/rabbit-mq-server/Dockerfile
Artifact (lines[1:3]):
    FROM rabbitmq
    EXPOSE 5672
    EXPOSE 15672
"""

rabbitmq = CClass(service, "rabbitmq", stereotype_instances = [message_broker, infrastructural], tagged_values = {'Message Broker': "RabbitMQ", 'Port': 5672})



"""
Components:
    - Monitoring Dashboard (Hystrix), "monitor-dashboard", port 8179
    - connection to service discovery
File:
Artifact (lines[10:12]):
    @SpringBootApplication
    @EnableHystrixDashboard
    @EnableDiscoveryClient
File:
Artifact (line 8):
    EXPOSE 8179
"""

monitor_dashboard = CClass(service, "monitor-dashboard", stereotype_instances = [monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Dashboard': "Hystrix", 'Port': 8179})

add_links({monitor_dashboard: discovery_service}, stereotype_instances = restful_http)



"""
Component: connection monitor dashboard to config server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/monitor-dashboard/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: monitor-dashboard
      cloud:
        config:
          uri: http://localhost:9090
"""

add_links({config_server: monitor_dashboard}, stereotype_instances = restful_http)



"""
Components:
    - monitoring server (Turbine), "turbine", port 8989
    - connection to discovery service
    - connection to rabbit-mq-server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/turbine/src/main/java/com/spring/netflix/oss/microservices/TurbineApplication.java
Artifact (lines[8:10]):
    @SpringBootApplication
    @EnableTurbineAmqp
    @EnableEurekaClient
*Note*: TurbineAmqp means, that RabbitMQ is used for communication to Hystrix clients
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/8668c4dffd9464b5d724f4d1a9547e029ee03d12/turbine/Dockerfile
Artifact (line 8):
    EXPOSE 8989
"""

turbine = CClass(service, "turbine", stereotype_instances = [monitoring_server, infrastructural], tagged_values = {'Monitoring Server': "Turbine", 'Port': 8989})

add_links({turbine: discovery_service}, stereotype_instances = restful_http)

add_links({rabbitmq: turbine}, stereotype_instances = restful_http)


"""
Component: connection turbine to config server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/turbine/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: turbine
      cloud:
        config:
          uri: http://localhost:9090
"""

add_links({config_server: turbine}, stereotype_instances = restful_http)



"""
Component: connection turbine to monitor-dashboard
Artifact: implicit when both are used
"""

add_links({turbine: monitor_dashboard}, stereotype_instances = restful_http)



"""
Components:
    - statement-service (internal) port 8080
    - connection to discovery service
File:
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableDiscoveryClient
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/statement-service/Dockerfile
Artifact (line 6):
    EXPOSE 8080
"""

statement_service = CClass(service, "statement-service", stereotype_instances = internal, tagged_values = {'Port': 8080})

add_links({statement_service: discovery_service}, stereotype_instances = restful_http)



"""
Component: connection statement service to config server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/statement-service/src/main/resources/bootstrap.yml
Artifact (lines[10:12]):
    cloud:
     config:
        uri: http://config-server:9090
"""

add_links({config_server: statement_service}, stereotype_instances = restful_http)



"""
Components:
    - "card-service", port 8080
    - connection to service discovery
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-service/src/main/java/com/spring/netflix/oss/microservices/CardServiceApplication.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableDiscoveryClient
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-service/Dockerfile
Artifact (line 6):
    EXPOSE 8080
"""

card_service = CClass(service, "card-service", stereotype_instances = internal, tagged_values = {'Port': 8080})

add_links({card_service: discovery_service}, stereotype_instances = restful_http)



"""
Component: connection card service to config service
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-service/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: card-service
      cloud:
        config:
        uri: http://localhost:9090
"""

add_links({config_server: card_service}, stereotype_instances = restful_http)



"""
Components:
    - "card-statement-composite-service", port 8080
    - use of circuit breaker (Hystrix)
    - connection to discovery service
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/java/com/spring/netflix/oss/microservices/CardStatementCompositeApplication.java#L9
Artifact (lines[9:12]):
    @SpringBootApplication
    @EnableEurekaClient
    @EnableFeignClients
    @EnableCircuitBreaker
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/Dockerfile
Artifact (line 6):
    EXPOSE 8080
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/pom.xml
Artifact (line 47):
    <artifactId>spring-cloud-starter-hystrix</artifactId>
"""

card_statement_composite = CClass(service, "card-statement-composite", stereotype_instances = [internal, circuit_breaker], tagged_values = {'Port': 8080, 'Circuit Breaker': "Hystrix"})

add_links({card_statement_composite: discovery_service}, stereotype_instances = [restful_http, circuit_breaker_link])



"""
Components:
    - connection card-statement-copmosite to card-service
    - circuit breaker
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/java/com/spring/netflix/oss/microservices/service/CardClient.java
Artifact (line 5):
    @FeignClient(name = "card-service")
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/java/com/spring/netflix/oss/microservices/CardStatementCompositeApplication.java
Artifact (line 12):
    @EnableCircuitBreaker
"""

add_links({card_statement_composite: card_service}, stereotype_instances = [restful_http, circuit_breaker_link, feign_connection, load_balanced_link], tagged_values = {'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon"})



"""
Components:
    - connection card-statement-composite to statement-service
    - cicuit breaker
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/java/com/spring/netflix/oss/microservices/service/StatementClient.java
Artifact (line 5):
    @FeignClient(name = "statement-service")
And file: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/java/com/spring/netflix/oss/microservices/CardStatementCompositeApplication.java
Artifact (line 12):
    @EnableCircuitBreaker
"""

add_links({card_statement_composite: statement_service}, stereotype_instances = [restful_http, circuit_breaker_link, feign_connection, load_balanced_link], tagged_values = {'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon"})



"""
Component: connection card-statement-composite service to config service
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/card-statement-composite/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: card-statement-composite
      cloud:
        config:
          uri: http://localhost:9090
"""

add_links({config_server: card_statement_composite}, stereotype_instances = restful_http)



"""
Components:
    - connection card-statement-composite to rabbit-mq-server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/8668c4dffd9464b5d724f4d1a9547e029ee03d12/card-statement-composite/pom.xml
Artifact (line 55):
		<artifactId>spring-cloud-netflix-hystrix-amqp</artifactId>
"""

add_links({card_statement_composite: rabbitmq}, stereotype_instances = restful_http)



"""
Components:
    - Gateway (Zuul), "edge-server", port 8765
    - Connection to discovery service
    - user and connection to edge-server (implicit for gateway)
    - load balancer (built in with Zuul)
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/edge-server/src/main/java/com/spring/netflix/oss/microservices/EdgeServerApplication.java
Artifact (lines[8:10]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableZuulProxy
And file: https://github.com/fernandoabcampos/microservices-config/blob/master/MASTER/edge-server.yml
Artifact (lines[1:2]):
    server:
        port: 8765
"""

edge_server = CClass(service, "edge-server", stereotype_instances = [gateway, infrastructural, load_balancer], tagged_values = {'Gateway': "Zuul", 'Port': 8765, 'Load Balancer': "Ribbon"})

add_links({discovery_service: edge_server}, stereotype_instances = restful_http)

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: edge_server}, stereotype_instances = restful_http)

add_links({edge_server: user}, stereotype_instances = restful_http)



"""
Component: connection edge server to config server
File: https://github.com/fernandoabcampos/spring-netflix-oss-microservices/blob/master/edge-server/src/main/resources/bootstrap.yml
Artifact (lines[1:6]):
    spring:
      application:
        name: edge-server
      cloud:
        config:
          uri: http://localhost:9090
"""

add_links({config_server: edge_server}, stereotype_instances = restful_http)



"""
Components:
    - connection edge-server to card-service
    - connection edge-server to statement-service
    - connection edge-server to card-statement-composite
File: https://github.com/fernandoabcampos/microservices-config/blob/master/MASTER/edge-server.yml
Artifact (lines[9:24]):
zuul:
  debug:
    request: true
  routes:
    card-service:
        path: /card-service/**
        serviceId: card-service
        stripPrefix: true
    statement-service:
        path: /statement-service/**
        serviceId: statement-service
        stripPrefix: true
    card-statement-composite:
        path: /card-statement-composite/**
        serviceId: card-statement-composite
        stripPrefix: true
"""

add_links({edge_server: card_service}, stereotype_instances = restful_http)

add_links({edge_server: statement_service}, stereotype_instances = restful_http)

add_links({edge_server: card_statement_composite}, stereotype_instances = restful_http)



##### Create model
model = CBundle(model_name, elements = edge_server.class_object.get_connected_elements())


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
