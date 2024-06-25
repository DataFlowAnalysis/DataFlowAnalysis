from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/spring-petclinic/spring-petclinic-microservices

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "spring-petclinic_spring-petclinic-microservices"



"""
Components:
    - configuration server (Spring Config), "config-server", port 8888
    - GitHub repository https://github.com/spring-petclinic/spring-petclinic-microservices-config
    - connection config server to GitHub repository
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-config-server/src/main/java/org/springframework/samples/petclinic/config/ConfigServerApplication.java
Artifact (lines[25:26]):
    @EnableConfigServer
    @SpringBootApplication
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-config-server/src/main/resources/application.yml
Artifact (lines[1:11]):
    server.port: 8888
    spring:
      cloud:
        config:
          server:
            git:
              uri: https://github.com/spring-petclinic/spring-petclinic-microservices-config
              default-label: main
            # Use the File System Backend to avoid git pulling. Enable "native" profile in the Config Server.
            native:
              searchLocations: file:///${GIT_REPO}
"""

config_server = CClass(service, "config-server", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 8888})

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/spring-petclinic/spring-petclinic-microservices-config"})

add_links({github_repository: config_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - discovery erver (Eureka), "discovery-server", port 8761
    - connection to config server
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-discovery-server/src/main/java/org/springframework/samples/petclinic/discovery/DiscoveryServerApplication.java
Artifact (lines[25:26]):
    @SpringBootApplication
    @EnableEurekaServer
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-discovery-server/src/main/resources/application.yml
Artifact (lines[1:5]):
    spring:
      application:
        name: discovery-server
      config:
        import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
And file: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/discovery-server.yml
Artifact (lines[1:2]):
    server:
      port: 8761
"""

discovery_server = CClass(service, "discovery-server", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})

add_links({config_server: discovery_server}, stereotype_instances = restful_http)



"""
Component:
    - tracing server (Zipkin), "tracing-server", port 9411
    - connection to config server
    - connection to discovery service
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/docker-compose.yml
Artifact (lines[65:72]):
    tracing-server:
        image: openzipkin/zipkin
        container_name: tracing-server
        ports:
         - 9411:9411
And file: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/tracing-server.yml
Artifact (lines[1:2]):
    server:
      port: 9411
And artifact (lines[17:20]):
    eureka:
      client:
        serviceUrl:
          defaultZone: http://discovery-server:8761/eureka/
"""

tracing_server = CClass(service, "tracing-server", stereotype_instances = [tracing_server, infrastructural], tagged_values = {'Tracing Server': "Zipkin", 'Port': 9411})

add_links({tracing_server: discovery_server}, stereotype_instances = restful_http)
add_links({config_server: tracing_server}, stereotype_instances = restful_http)



"""
Components:
    - admin server (Spring Boot Admin), "admin-server", port 9090
    - connection to discovery service
    - connection to config server
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-admin-server/src/main/java/org/springframework/samples/petclinic/admin/SpringBootAdminApplication.java
Artifact (lines[23:25]):
    @SpringBootApplication
    @EnableAdminServer
    @EnableDiscoveryClient
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-admin-server/src/main/resources/application.yml
Artifact (lines[1:5]):
    spring:
      application:
        name: admin-server
      config:
        import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-admin-server/pom.xml
Artifact (line 20):
    <docker.image.exposed.port>9090</docker.image.exposed.port>
"""

admin_server = CClass(service, "admin-server", stereotype_instances = [administration_server, infrastructural], tagged_values = {'Administration Server': "Spring Boot Admin", 'Port': 9090})

add_links({admin_server: discovery_server}, stereotype_instances = restful_http)
add_links({config_server: admin_server}, stereotype_instances = restful_http)



"""
Component: metrics server (Prometheus), "prometheus-server", hostport 9091, containerport 9090
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[94:99]):
    prometheus-server:
        build: ./docker/prometheus
        container_name: prometheus-server
        [...]
        ports:
        - 9091:9090
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/Dockerfile
Artifact (lines[1:2]):
    FROM prom/prometheus:v2.4.2
    ADD prometheus.yml /etc/prometheus/
"""

prometheus_server = CClass(service, "prometheus-server", stereotype_instances = [metrics_server, infrastructural], tagged_values = {'Metrics Server': "Prometheus", 'Port': 9090})



"""
Note on connection prometheus to prometheus (monitoring itself):
Although job-name is prometheus, localhost:9090 is not clear. Admin and prometheus both have port 9090 open
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/prometheus.yml
Artifact (lines[9:12]):
    scrape_configs:
    - job_name: prometheus
      static_configs:
      - targets: ['localhost:9090']
"""



"""
Component: monitoring dashboard (Grafana), "grafana-server", port 3000
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[87:92]):
    grafana-server:
        build: ./docker/grafana
        container_name: grafana-server
        [...]
        ports:
        - 3000:3000
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/grafana/Dockerfile
Artifact (lines[1:4]):
    FROM grafana/grafana:5.2.4
    ADD ./provisioning /etc/grafana/provisioning
    ADD ./grafana.ini /etc/grafana/grafana.ini
    ADD ./dashboards /var/lib/grafana/dashboards
"""

grafana_server = CClass(service, "grafana-server", stereotype_instances = [monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Dashboard': "Grafana", 'Port': 3000})



"""
Component: connection prometheus to grafana
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/grafana/provisioning/datasources/all.yml
Artifact (lines[5:10]):
    datasources:
    - name: Prometheus
      type: prometheus
      [...]
      url: http://prometheus-server:9090
"""

add_links({prometheus_server: grafana_server}, stereotype_instances = restful_http)



"""
Components:
    - customers-service (internal), port 8081
    - connection to discovery service
    - in-memory data store
    - local logging
Service, connection to discovery service:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/src/main/java/org/springframework/samples/petclinic/customers/CustomersServiceApplication.java
Artifact (lines[25:26]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[21;29:30]):
    customers-service:
        ports:
        - 8081:8081
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/src/main/resources/application.yml
Atrifact (lines[1:3]):
    spring:
      application:
        name: customers-service

In-Memory datastore:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/pom.xml
Artifact (lines[67:69]):
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
And file: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/application.yml
Artifact (lines[10:13]):
    spring:
      datasource:
        schema: classpath*:db/hsqldb/schema.sql
        data: classpath*:db/hsqldb/data.sql

Local logging:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/pom.xml
Artifact (lines[77:78]):
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/src/main/java/org/springframework/samples/petclinic/customers/web/OwnerResource.java
Artifact (lines[41;86]):
    @Slf4j
    [...]
    log.info("Saving owner {}", ownerModel);

Endpoints:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-customers-service/src/main/java/org/springframework/samples/petclinic/customers/web/OwnerResource.java
Artifact (line 38):
    @RequestMapping("/owners")
And artifact (line 59):
    @GetMapping(value = "/{ownerId}")
And artifact (line 75):
    @PutMapping(value = "/{ownerId}")
"""

customers_service = CClass(service, "customers-service", stereotype_instances = [in_memory_data_store, local_logging, internal], tagged_values = {'Port': 8081, 'In-Memory Data Store': "HSQLDB", 'Logging Technology': "Lombok", 'Endpoints': "[\'/owners\', \'/owners/{ownerId}\']"})

add_links({customers_service: discovery_server}, stereotype_instances = restful_http)



"""
Component: connection customer-service to config service
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-customers-service/src/main/resources/application.yml
Artifact (lines[1:5]):
    spring:
      [...]
      config:
        import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
"""

add_links({config_server: customers_service}, stereotype_instances = restful_http)



"""
Component: connection customer-service to tracing
File: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/customers-service.yml
Artifact (lines[13:14]):
    zipkin:
        baseUrl: http://tracing-server:9411
"""

add_links({customers_service: tracing_server}, stereotype_instances = restful_http)



"""
Component: connection customers-service to prometheus
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/prometheus.yml
Artifact (lines[9;19:22]):
    scrape_configs:
    - job_name: customers-service
      metrics_path: /actuator/prometheus
      static_configs:
      - targets: ['customers-service:8081']
"""

add_links({customers_service: prometheus_server}, stereotype_instances = restful_http)



"""
Components:
    - vets-service (internal), port 8083
    - connection to discovery service
    - in-memory datastore
Service and connection to discovry service:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-vets-service/src/main/java/org/springframework/samples/petclinic/vets/VetsServiceApplication.java
Artifact (lines[27:28]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[43;51:52]):
    vets-service:
        ports:
         - 8083:8083
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-vets-service/src/main/resources/application.yml
Artifct (lines[1:3]):
    spring:
      application:
        name: vets-service

In-memory datastore:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-vets-service/pom.xml
Artifact (lines[87:89]):
	<dependency>
		<groupId>org.hsqldb</groupId>
		<artifactId>hsqldb</artifactId>
And file: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/application.yml
Artifact (lines[10:13]):
    spring:
      datasource:
        schema: classpath*:db/hsqldb/schema.sql
        data: classpath*:db/hsqldb/data.sql

Endpoints:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-vets-service/src/main/java/org/springframework/samples/petclinic/vets/web/VetResource.java
Artifact (line 35):
    @RequestMapping("/vets")
"""

vets_service = CClass(service, "vets-service", stereotype_instances = [in_memory_data_store, internal], tagged_values = {'Port': 8083, 'In-Memory Data Store': "HSQLDB", 'Endpoints': "[\'/vets\']"})

add_links({vets_service: discovery_server}, stereotype_instances = restful_http)



"""
Component: connection vets-service to config-server
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-vets-service/src/main/resources/application.yml
Artifact (lines[1;4:5]):
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
"""

add_links({config_server: vets_service}, stereotype_instances = restful_http)



"""
Component: connection vets-service to tracing
File: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/vets-service.yml
Artifact (lines[21:22]):
    zipkin:
        baseUrl: http://tracing-server:9411
"""

add_links({vets_service: tracing_server}, stereotype_instances = restful_http)



"""
Component: connection vets-service to prometheus
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/prometheus.yml
Artifact (lines[9;29:32]):
    scrape_configs:
    - job_name: vets-service
      metrics_path: /actuator/prometheus
      static_configs:
      - targets: ['vets-service:8083']
"""

add_links({vets_service: prometheus_server}, stereotype_instances = restful_http)



"""
Components:
    - visits-service (internal), port 8082
    - connection to discovery service
    - in-memory datastore
    - local logging
Service and connection to discovry service:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-visits-service/src/main/java/org/springframework/samples/petclinic/visits/VisitsServiceApplication.java
Artifact (lines[25:26]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[32;40:41]):
    visits-service:
        ports:
         - 8082:8082
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-visits-service/src/main/resources/application.yml
Artifct (lines[1:3]):
    spring:
      application:
        name: visits-service

In-memory datastore:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/0ab2e953e0e23761593a80cd17d7525e3ae32f9d/spring-petclinic-visits-service/pom.xml
Artifact (lines[66:68]):
	<dependency>
		<groupId>org.hsqldb</groupId>
		<artifactId>hsqldb</artifactId>
And file: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/application.yml
Artifact (lines[10:13]):
    spring:
      datasource:
        schema: classpath*:db/hsqldb/schema.sql
        data: classpath*:db/hsqldb/data.sql

Local logging:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-visits-service/pom.xml
Artifact (lines[61:63]):
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-visits-service/src/main/java/org/springframework/samples/petclinic/visits/web/VisitResource.java
Artifact (lines[45;58]):
    @Slf4j
    [...]
    log.info("Saving visit {}", visit);
"""

visits_service = CClass(service, "visits-service", stereotype_instances = [in_memory_data_store, local_logging, internal], tagged_values = {'Port': 8082, 'In-Memory Data Store': "HSQLDB", 'Logging Technology': "Lombok"})

add_links({visits_service: discovery_server}, stereotype_instances = restful_http)



"""
Component: connection visits-service to config-server
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-visits-service/src/main/resources/application.yml
Artifact (lines[1;4:5]):
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
"""

add_links({config_server: visits_service}, stereotype_instances = restful_http)



"""
Component: connection visits-service to tracing
File: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/visits-service.yml
Artifact (lines[13:14]):
    zipkin:
        baseUrl: http://tracing-server:9411
"""

add_links({visits_service: tracing_server}, stereotype_instances = restful_http)



"""
Component: connection visits-service to prometheus
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/prometheus.yml
Artifact (lines[9;24:27]):
    scrape_configs:
    - job_name: visits-service
      metrics_path: /actuator/prometheus
      static_configs:
      - targets: ['visits-service:8082']
"""

add_links({visits_service: prometheus_server}, stereotype_instances = restful_http)



"""
Components:
    - API-Gateway (Spring Cloud Gateway), "api-gateway", port 8080
    - connection to discovery service
    - user (implicit with gateway)
    - connection user to api-gateway
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/ApiGatewayApplication.java
Artifact (lines[45:46]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/resources/application.yml
Artifact (lines[1:3]):
    spring:
      application:
        name: api-gateway
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker-compose.yml
Artifact (lines[62:63]):
    ports:
     - 8080:8080

Endpoints:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/boundary/web/ApiGatewayController.java
Artifact (line 39):
    @RequestMapping("/api/gateway")
And artifact (line 48):
    @GetMapping(value = "owners/{ownerId}")
"""

api_gateway = CClass(service, "api-gateway", stereotype_instances = [gateway, infrastructural, load_balancer], tagged_values = {'Gateway': "Spring Cloud Gateway", 'Port':8080, 'Load Balancer': "Spring Cloud", 'Endpoints': "[\'/api/gatewayowners/{ownerId}\', \'/api/gateway\']"})

add_links({discovery_server: api_gateway}, stereotype_instances = restful_http)

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: api_gateway}, stereotype_instances = restful_http)
add_links({api_gateway: user}, stereotype_instances = [restful_http, load_balanced_link])


"""
Component: connection api-gateway to config-server
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/resources/application.yml
Artifact (lines[4:5]):
    config:
        import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
"""

add_links({config_server: api_gateway}, stereotype_instances = restful_http)



"""
Component: connection api-gateway to prometheus
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/docker/prometheus/prometheus.yml
Artifact (lines[9;14:17]):
    scrape_configs:
    - job_name: api-gateway
      metrics_path: /actuator/prometheus
      static_configs:
      - targets: ['api-gateway:8080']
"""

add_links({api_gateway: prometheus_server}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - connection api-gateway to vets-service
    - connection api-gateway to visits-service
    - connection api-gateway to customers-service
    - load balancer for link to customers and visits
    - circuit breaker for link to customers
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/resources/application.yml
Artifact (lines[7:24]):
    gateway:
      routes:
        - id: vets-service
          uri: lb://vets-service
          predicates:
            - Path=/api/vet/**
        [...]
        - id: visits-service
          uri: lb://visits-service
          predicates:
            - Path=/api/visit/**
        [...]
        - id: customers-service
          uri: lb://customers-service
          predicates:
            - Path=/api/customer/**

Load Balancer:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/ApiGatewayApplication.java
Artifact (lines[60:63]):
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/application/CustomersServiceClient.java
Artifact (lines[35:36]):
    return webClientBuilder.build().get()
        .uri("http://customers-service/owners/{ownerId}", ownerId)
And file: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/master/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/application/VisitsServiceClient.java
Artifact (lines[36;41:43]):
    private String hostname = "http://visits-service/";
    return webClientBuilder.build()
        .get()
        .uri(hostname + "pets/visits?petId={petId}", joinIds(petIds))

Circuit breaker:
File: https://github.com/spring-petclinic/spring-petclinic-microservices/blob/c47e99a4c6cd2c0c7ea7ea8e6c5f490aa3a8e250/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/boundary/web/ApiGatewayController.java
Artifact (lines[50:54]):
    return customersServiceClient.getOwner(ownerId)
        .flatMap(owner ->
            visitsServiceClient.getVisitsForPets(owner.getPetIds())
                .transform(it -> {
                    ReactiveCircuitBreaker cb = cbFactory.create("getOwnerDetails");
And artifact (line 46):
    private final ReactiveCircuitBreakerFactory cbFactory;
And file:
Artifact (lines[30:40]):
    public class CustomersServiceClient {

        private final WebClient.Builder webClientBuilder;

        public Mono<OwnerDetails> getOwner(final int ownerId) {
            return webClientBuilder.build().get()
                .uri("http://customers-service/owners/{ownerId}", ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
        }
    }
"""

add_links({api_gateway: vets_service}, stereotype_instances = [restful_http, load_balanced_link])

add_links({api_gateway: visits_service}, stereotype_instances = [restful_http, load_balanced_link])

add_links({api_gateway: customers_service}, stereotype_instances = [restful_http, load_balanced_link, circuit_breaker_link])



"""
Component: connection api-gateway to tracing-server
File: https://github.com/spring-petclinic/spring-petclinic-microservices-config/blob/main/api-gateway.yml
Artifact (liens[25:26]):
    zipkin:
        baseUrl: http://tracing-server:9411
"""

add_links({api_gateway: tracing_server}, stereotype_instances = [restful_http, load_balanced_link])



##### Create model
model = CBundle(model_name, elements = api_gateway.class_object.get_connected_elements())


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
