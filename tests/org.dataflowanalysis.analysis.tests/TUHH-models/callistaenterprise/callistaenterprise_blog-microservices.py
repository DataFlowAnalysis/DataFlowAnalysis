from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/callistaenterprise/blog-microservices

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "callistaenterprise_blog-microservices"



"""
Components:
    - search engine (Elasticsearch), "elasticsearch", port 9200
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [5:8]):
    elasticsearch:
        image: docker.elastic.co/elasticsearch/elasticsearch:5.2.2
        ports:
          - "9200:9200"
"""

elasticsearch = CClass(service, "elasticsearch", stereotype_instances = [infrastructural, search_engine], tagged_values = {'Port': 9200, 'Search Engine': "Elasticsearch"})



"""
Components:
    - monitoring dashboard (Kibana), "kibana", port 5601
    - connection to elasticsearch
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [18:21]):
    kibana:
        image: docker.elastic.co/kibana/kibana:5.2.2
        ports:
          - "5601:5601"

Connection:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [24:25]):
    depends_on:
        elasticsearch:
"""

kibana = CClass(service, "kibana", stereotype_instances = [infrastructural, monitoring_dashboard], tagged_values = {'Port': 5601, 'Monitoring Dashboard': "Kibana"})

add_links({elasticsearch: kibana}, stereotype_instances = restful_http)



"""
Components:
    - logging server (Logstash), "logstash", port 25826
    - connection to elasticsearch
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [28:31]):
    logstash:
        image: docker.elastic.co/logstash/logstash:5.2.2
        ports:
          - "25826:25826"

Connection:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [35:36]):
    depends_on:
        elasticsearch:
"""

logstash = CClass(service, "logstash", stereotype_instances = [infrastructural, logging_server], tagged_values = {'Port': 25826, 'Logging Server': "Logstash"})

add_links({logstash: elasticsearch}, stereotype_instances = restful_http)



"""
Components:
    - message broker (RabbitMQ), "rabbitmq", port 15672
File: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [44:45;49]):
  rabbitmq:
    image: rabbitmq:3-management

      - "15672:15672"
"""

rabbitmq = CClass(service, "rabbitmq", stereotype_instances = [infrastructural, message_broker], tagged_values = {'Port': 15672, 'Message Broker': "RabbitMQ"})



"""
Components:
    - service discovery (Eureka), "discovery-server", ports 8761 and 8762
    - plaintext credentials
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/discovery-server/src/main/java/se/callista/microservises/support/discovery/EurekaApplication.java
Artifact (line 9):
    @EnableEurekaServer
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/discovery-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: discovery-server
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/discovery-server/src/main/resources/application.yml
Artifact (lines [1:2]):
    server:
      port: 8761

Plaintext credentials:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/discovery-server/src/main/resources/application.yml
Artifact (lines [4:6]):
    security:
        user:
            password: ${eureka.password} # Don't use a default password in a real app
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/setup-env.sh
Artifact (line 5):
    export MY_CONFIG_PWD=config_client_pwd
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artivact (line 78):
    - SECURITY_USER_PASSWORD=${MY_CONFIG_PWD}
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/discovery-server/src/main/resources/application.yml
Artifact (lines [12;35]):
    eureka:

      password: ${SECURITY_USER_PASSWORD:password}
"""

discovery_server = CClass(service, "discovery-server", stereotype_instances = [infrastructural, service_discovery, plaintext_credentials], tagged_values = {'Port': 8761, 'Port': 8762, 'Service Discovery': "Eureka", 'Username': "user", 'Password': "password"})



"""
Components:
    - configuration server (Spring Cloud Config), "config-server", port 8888
    - local logging
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/java/se/callista/microservises/support/ConfigServerApplication.java
Artifact (line 15):
    @EnableConfigServer
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/resources/bootstrap.yml
Artifact (line 1):
    spring.application.name: config-server
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/resources/application.yml
Artifact (lines [1:2]):
    server:
        port: 8888

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/java/se/callista/microservises/support/ConfigServerApplication.java
Artifact (lines [20;25]):
    private static final Logger LOG = LoggerFactory.getLogger(ConfigServerApplication.class);

        LOG.info("Connected to RabbitMQ at: {}", ctx.getEnvironment().getProperty("spring.rabbitmq.host"));

HTTPS:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/resources/server.jks
Artifact (lines 0)
"""

config_server = CClass(service, "config-server", stereotype_instances = [infrastructural, configuration_server, local_logging], tagged_values = {'Port': 8888, 'Configuration Server': "Spring Cloud Config"})



"""
Components:
    - external github repository "https://github.com/callistaenterprise/blog-microservices-config"
    - connection external-repository to config-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/resources/application.yml
Artifact (line 12):
    spring.cloud.config.server.git.uri: file:///${PWD}/../../../../blog-microservices-config/
"""

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/callistaenterprise/blog-microservices-config"})

add_links({github_repository: config_server}, stereotype_instances = restful_http)



"""
Components:
    - connection config-server to discovery-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/java/se/callista/microservises/support/ConfigServerApplication.java
Artifact (line 16):
    @EnableDiscoveryClient
"""

add_links({config_server: discovery_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection rabbitmq to config-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/config-server/src/main/resources/application.yml
Artifact (lines [16:18]):
    spring.rabbitmq:
      host: localhost
      port: 5672
"""

add_links({config_server: rabbitmq}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - authorization server (Spring OAuth2), "auth-server", port 9999
    - resource server
    - endpoints ["/user"]
    - local logging
    - plaintext credentials
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/java/se/callista/microservises/support/oauth/AuthserverApplication.java
Artifact (line 17):
    @EnableAuthorizationServer
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: auth-server
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines [92:93]):
    ports:
      - "9999:9999"

Resource server:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/java/se/callista/microservises/support/oauth/AuthserverApplication.java
Artifact (line 16):
    @EnableResourceServer

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/java/se/callista/microservises/support/oauth/AuthserverApplication.java
Artifact (lines [21;30]):
    private static final Logger LOG = LoggerFactory.getLogger(AuthserverApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/java/se/callista/microservises/support/oauth/AuthserverApplication.java
Artifact (line 23):
    @RequestMapping("/user")

Plaintext credentials:
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/auth-server.yml
Artifact (line 10):
    security.user.password: password
And artifact (lines [12:14]):
    security.oauth2.client:
      clientId: acme
      clientSecret: acmesecret

HTTPS:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/resources/truststore.jks
Artifact (lines 0)
"""

auth_server = CClass(service, "auth-server", stereotype_instances = [infrastructural, authorization_server, resource_server, local_logging, plaintext_credentials], tagged_values = {'Port': 9999, 'Authorization Server': "Spring OAuth2", 'Endpoints': "[\'/user\']", 'Username': "acme", 'Password': "acmesecret"})



"""
Components:
    - connection config-server to auth-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
        config:

            uri: https://localhost:8888
"""

add_links({config_server: auth_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - monitoring dashboard (Hystrix), "monitor-dashboard", port 7979
    - local logging
    - endpoints ["/"]
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/src/main/java/se/callista/microservises/support/monitordashboard/HystrixDashboardApplication.java
Artifact (line 16):
    @EnableHystrixDashboard
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: monitor-dashboard
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/docker-compose-with-elk.yml
Artifact (lines 174:175):
    ports:
      - "7979:7979"

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/src/main/java/se/callista/microservises/support/monitordashboard/HystrixDashboardApplication.java
Artifact (lines [19;23]):
    private static final Logger LOG = LoggerFactory.getLogger(HystrixDashboardApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/src/main/java/se/callista/microservises/support/monitordashboard/HystrixDashboardApplication.java
Artifact (line 27):
    @RequestMapping("/")
"""

monitor_dashboard = CClass(service, "monitor-dashboard", stereotype_instances = [infrastructural, monitoring_dashboard, local_logging], tagged_values = {'Port': 7979, 'Monitoring Dashboard': "Hystrix", 'Endpoints': "[\'/\']"})



"""
Components:
    - connection config-server to monitor-dashboard
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
        config:

            uri: https://localhost:8888
"""

add_links({config_server: monitor_dashboard}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - connection monitor-dashboard to discovery-server
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/monitor-dashboard.yml
Artifact (lines [52;55:57]):
    eureka:

      client:
        serviceUrl:
          defaultZone: http://discovery:8761/eureka/
"""

add_links({monitor_dashboard: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - monitoring server (Turbine), "turbine-server", port 8989
    - local logging
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/turbine/src/main/java/se/callista/microservises/support/turbine/TurbineApplication.java
Artifact (line 18):
    @EnableTurbineStream
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/turbine/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: turbine-server
And file:
Artifact (lines [1:2]):
    server:
        port: 8989

Local logging:
File:
Artifact (lines [22;26]):
    private static final Logger LOG = LoggerFactory.getLogger(TurbineApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");
"""

turbine_server = CClass(service, "turbine-server", stereotype_instances = [infrastructural, monitoring_server, local_logging], tagged_values = {'Port': 8989, 'Monitoring Server': "Turbine"})



"""
Components:
    - connection rabbitmq to turbine-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/turbine/src/main/resources/application.yml
Artifact (lines [33:35]):
    spring.rabbitmq:
      host: localhost
      port: 5672
"""

add_links({rabbitmq: turbine_server}, stereotype_instances = restful_http)



"""
Components:
    - connection turbine-server to discovery-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/turbine/src/main/java/se/callista/microservises/support/turbine/TurbineApplication.java
Artifact (line 19):
    @EnableDiscoveryClient
"""

add_links({turbine_server: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - connection config-server to turbine-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/turbine/src/main/resources/bootstrap.yml
Artifact (lines [4:5;15]):
    cloud:
        config:

          uri: https://localhost:8888
"""

add_links({config_server: turbine_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})


"""
Components:
    - connection turbine-server to monitor-dashboard
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/monitor-dashboard/build.gradle
Artifact (line 40):
    compile('org.springframework.cloud:spring-cloud-starter-bus-amqp')
"""

add_links({turbine_server: monitor_dashboard}, stereotype_instances = restful_http)



"""
Components:
    - tracing server (Zipkin), "zipkin-server", port 9411
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/zipkin-server/src/main/java/se/callista/microservises/workshop/zipkin/ZipkinServerApplication.java
Artifact (line 7):
    @EnableZipkinStreamServer
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/zipkin-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: zipkin-server
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/zipkin-server/src/main/resources/application.yml
Artifact (line 1):
    server.port: 9411
"""

zipkin_server = CClass(service, "zipkin-server", stereotype_instances = [infrastructural, tracing_server], tagged_values = {'Port': 9411, 'Tracing Server': "Zipkin"})



"""
Components:
    - connection zipkin-server to rabbitmq
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/zipkin-server/src/main/resources/application.yml
Artifact (lines [3:4]):
    spring.rabbitmq.host: localhost
    spring.rabbitmq.port: 5672
"""

add_links({rabbitmq: zipkin_server}, stereotype_instances = restful_http)



"""
Components:
    - internal service "product-service", port 8080
    - local logging
    - endpoints ["/product/{productId}", "/set-processing-time"]
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/java/se/callista/microservices/core/product/ProductServiceApplication.java
Artifact (line 14):
    @SpringBootApplication
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: product-service
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/java/se/callista/microservices/core/product/ProductServiceApplication.java
Artifact (lines 19;23):
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/java/se/callista/microservices/core/product/service/ProductService.java
Artifact (line 47):
    @RequestMapping("/product/{productId}")
And artifact (line 77):
    @RequestMapping("/set-processing-time")
"""

product_service = CClass(service, "product-service", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8080, 'Endpoints': "[\'/product/{productId}\', \'/set-processing-time\']"})



"""
Components:
    - connection config-server to product-service
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
        config:

          uri: https://localhost:8888
"""

add_links({config_server: product_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection product-service to discover-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/product-service/src/main/java/se/callista/microservices/core/product/ProductServiceApplication.java
Artifact (line 15):
    @EnableDiscoveryClien
"""

add_links({product_service: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - internal service "recommendation-service", port 8080
    - local logging
    - endpoints ["/recommendation", "/set-processing-time"]
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/java/se/callista/microservices/core/recommendation/RecommendationServiceApplication.java
Artifact (line 13):
    @SpringBootApplication
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: recommendation-service
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/java/se/callista/microservices/core/recommendation/RecommendationServiceApplication.java
Artifact (lines 18;22):
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/java/se/callista/microservices/core/recommendation/service/RecommendationService.java
Artifact (line 58):
    @RequestMapping("/recommendation")
And artiact (line 95):
    @RequestMapping("/set-processing-time")
"""

recommendation_service = CClass(service, "recommendation-service", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8080, 'Endpoints': "[\'/recommendation\', \'/set-processing-time\']"})



"""
Components:
    - connection config-server to recommendation-service
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
  cloud:
    config:

      uri: https://localhost:8888
"""

add_links({config_server: recommendation_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection recommendation-service to discovery-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/recommendation-service/src/main/java/se/callista/microservices/core/recommendation/RecommendationServiceApplication.java
Artifact (line 14):
    @EnableDiscoveryClient
"""

add_links({recommendation_service: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - internal service "review-service", port 8080
    - local logging
    - endpoints ["/review", "/set-processing-time"]
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/java/se/callista/microservices/core/review/ReviewServiceApplication.java
Artifact (line 18):
    @SpringBootApplication
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: review-service
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/java/se/callista/microservices/core/review/ReviewServiceApplication.java
Artifact (lines 23;27):
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/java/se/callista/microservices/core/review/service/ReviewService.java
Artifact (line 64):
    @RequestMapping("/review")
And artifact (line 103):
    @RequestMapping("/set-processing-time")
"""

review_service = CClass(service, "review-service", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8080, 'Endpoints': "[\'/review\', \'/set-processing-time\']"})



"""
Components:
    - connection config-server to review-service
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
      config:

        uri: https://localhost:8888
"""

add_links({config_server: review_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection review-service to discovery-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/core/review-service/src/main/java/se/callista/microservices/core/review/ReviewServiceApplication.java
Artifact (line 19):
    @EnableDiscoveryClient
"""

add_links({review_service: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - internal service "composite-service", port 8080
    - resource server
    - circuit breaker
    - load balancer
    - local logging
    - endpoints ["/", "/{productId}"]
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (line 18):
    @SpringBootApplication
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: composite-service
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Resource server:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (line 21):
    @EnableResourceServer

Circuit breaker:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (line 19):
    @EnableCircuitBreaker

Load balancer:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (line 34):
    @LoadBalanced

Local logging:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (lines [25;29]):
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Endpoints:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/service/ProductCompositeService.java
Artifact (line 47):
    @RequestMapping("/")
And artifact (line 52):
    @RequestMapping("/{productId}")
"""

composite_service = CClass(service, "composite-service", stereotype_instances = [internal, local_logging, load_balancer, circuit_breaker, resource_server], tagged_values = {'Port': 8080, 'Endpoints': "[\'/\', \'/{productId}\']", 'Load Balancer': "Spring Cloud"})



"""
Components:
    - connection config-server to composite-service
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
        config:

            uri: https://localhost:8888
"""

add_links({config_server: composite_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - connection auth-server to composite-service
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/composite-service.yml
Artifact (lines [12:15]):
    security:
      oauth2:
        resource:
          userInfoUri: https://localhost:9999/uaa/user
"""

add_links({auth_server: composite_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection composite-service to discovery-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/ProductCompositeServiceApplication.java
Artifact (line 20):
    @EnableDiscoveryClient
"""

add_links({composite_service: discovery_server}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link])



"""
Components:
    - connection composite-service to rabbitmq
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/service/ProductCompositeIntegration.java
Artifact (line 45):
    @HystrixCommand(fallbackMethod = "defaultProduct")
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/build.gradle
Artifact (line 49):
    compile("org.springframework.cloud:spring-cloud-starter-bus-amqp")
"""

add_links({composite_service: rabbitmq}, stereotype_instances = restful_http)



"""
Components:
    - connection composite-service to product-service
    - load balanced
    - circuit breaker
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/service/ProductCompositeIntegration.java
Artifact (lines [50;53]):
    String url = "http://product-service/product/" + productId;

    ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
"""

add_links({composite_service: product_service}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - connection composite-service to recommendation-service
    - load balanced
    - circuit breaker
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/service/ProductCompositeIntegration.java
Artifact (lines [85;88]):
    String url = "http://recommendation-service/recommendation?productId=" + productId;

    ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
"""

add_links({composite_service: recommendation_service}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - connection composite-service to review-service
    - load balanced
    - circuit breaker
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/composite/product-composite-service/src/main/java/se/callista/microservices/composite/product/service/ProductCompositeIntegration.java
Artifact (lines [125;128]):
    String url = "http://review-service/review?productId=" + productId;

    ResponseEntity<String> resultStr = restTemplate.getForEntity(url, String.class);
"""

add_links({composite_service: review_service}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - API gateway (Zuul), "edge-server", port 8765
    - resource server
    - local logging
    - load balancer (Zuul)
    - circuit breaker
    - user
    - connections bwteen user and gateway
    - HTTPS enabled
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/edge-server/src/main/java/se/callista/microservises/support/edge/ZuulApplication.java
Artifact (line 18):
    @EnableZuulProxy
And file: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/edge-server/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: edge-server
And file: https://github.com/callistaenterprise/blog-microservices-config/blob/master/monitor-dashboard.yml
Artifact (line 2):
    port: 8765

Resource server:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/edge-server/src/main/java/se/callista/microservises/support/edge/ZuulApplication.java
Artifact (line 19):
    @EnableResourceServer

Local logging:
File:
Artifact (lines [22;26]):
    private static final Logger LOG = LoggerFactory.getLogger(ZuulApplication.class);

        LOG.warn("Will now disable hostname check in SSL, only to be used during development");

Circuit breaker:
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/edge-server.yml
Artifact (lines [20;29:31]):
hystrix:

  command:
    default:
      circuitBreaker:

HTTPS:
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/edge-server/src/main/resources/truststore.jks
Artifact (lines 0)
"""

edge_server = CClass(service, "edge-server", stereotype_instances = [infrastructural, gateway, resource_server, local_logging, circuit_breaker, load_balancer], tagged_values = {'Port': 8765, 'Gateway': "Zuul", 'Load Balancer': "Ribbon"})

user = CClass(external_component, "user", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({edge_server: user}, stereotype_instances = restful_http)

add_links({user: edge_server}, stereotype_instances = restful_http)



"""
Component:
    - connection edge-server to composite-service
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/edge-server.yml
Artifact (lines [60:64]):
    zuul:
      ignoredServices: "*"
      prefix: /api
      routes:
        composite-service: /product/**
"""

add_links({edge_server: composite_service}, stereotype_instances = [restful_http, circuit_breaker_link], tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - connection discover-server to edge-server
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/edge-server.yml
Artifact (lines [100;103:105]):
    eureka:

      client:
        serviceUrl:
          defaultZone: http://discovery:8761/eureka/
"""

add_links({discovery_server: edge_server}, stereotype_instances = restful_http)



"""
Components:
    - connection config-server to edge-server
File: https://github.com/callistaenterprise/blog-microservices/blob/master/microservices/support/edge-server/src/main/resources/bootstrap.yml
Artifact (lines [7:8;18]):
    cloud:
        config:

            uri: https://localhost:8888

"""

add_links({config_server: edge_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - connection edge-server to auth-server
File: https://github.com/callistaenterprise/blog-microservices-config/blob/master/edge-server.yml
Artifact (lines [15:18]):
    security:
      oauth2:
        resource:
          userInfoUri: https://localhost:9999/uaa/user
"""

add_links({edge_server: auth_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection discovery-server to logstash
    - connection config-server to logstash
    - connection auth-server to logstash
    - connection product-service to logstash
    - connection recommendation-service to logstash
    - connection review-service to logstash
    - connection composite-service to logstash
    - connection monitor-dashboard to logstash
    - connection edge-server to logstash
    - connection zipkin-server to logstash
File: https://github.com/callistaenterprise/blog-microservices/blob/1681bdb6b1b0c64147d059f2c2e04726bce8e2f4/docker-compose-with-elk.yml
Artifact (lines [58:61;81:84;98:101;118:121;133:136;148:151;163:166;180:183;197:200;214:217]):
    logging:
      driver: syslog
      options:
        syslog-address: "tcp://127.0.0.1:25826"

    ... (always the same)
"""

add_links({discovery_server: logstash}, stereotype_instances = restful_http)

add_links({config_server: logstash}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})

add_links({auth_server: logstash}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})

add_links({product_service: logstash}, stereotype_instances = restful_http)

add_links({recommendation_service: logstash}, stereotype_instances = restful_http)

add_links({review_service: logstash}, stereotype_instances = restful_http)

add_links({composite_service: logstash}, stereotype_instances = restful_http)

add_links({monitor_dashboard: logstash}, stereotype_instances = restful_http)

add_links({edge_server: logstash}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})

add_links({zipkin_server: logstash}, stereotype_instances = restful_http)




##### Create model
model = CBundle(model_name, elements = discovery_server.class_object.get_connected_elements())

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
