from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/anilallewar/microservices-basics-spring-boot

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "anilallewar_microservices-basics-spring-boot"



"""
Components:
    - configuration server (Spring Cloud config), "configserver", port 8888
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/config-server/src/main/java/com/anilallewar/microservices/config/ConfigApplication.java
Artifact (line 21):
    @EnableConfigServer
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/config-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: configserver
And file:
Artifact (lines [17:18]):
    server:
        port: 8888
"""

configserver = CClass(service, "configserver", stereotype_instances = [infrastructural, configuration_server], tagged_values = {'Port': 8888, 'Configuration Server': "Spring Cloud Config"})



"""
Components:
    - external configuration repository "https://github.com/anilallewar/microservices-basics-cloud-config"
    - connection from github repository to configserver
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/config-server/src/main/resources/application.yml
Artifact (lines [2:6]):
    cloud:
        config:
            server:
                git:
                    uri: https://github.com/anilallewar/microservices-basics-cloud-config
"""

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/anilallewar/microservices-basics-cloud-config"})

add_links({github_repository: configserver}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - service discover (Eureka), "webservice-registry", port 8761
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/webservice-registry/src/main/java/com/anilallewar/microservices/registry/RegistryApplication.java
Artifact (line 19):
    @EnableEurekaServer
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/webservice-registry/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: webservice-registry
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/webservice-registry.yml
Artifact (lines [1:2]):
    server:
        port: 8761
"""

webservice_registry = CClass(service, "webservice-registry", stereotype_instances = [infrastructural, service_discovery], tagged_values = {'Port': 8761, 'Service Discovery': "Eureka"})



"""
Components:
    - connection configserver to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/webservice-registry/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - tracing server (Zipkin), "zipkin-tracing", port 9411
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/zipkin-server/src/main/java/com/anilallewar/microservices/tracing/ZipkinTracingApplication.java
Artifact (line 15):
    @EnableZipkinServer
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/zipkin-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: zipkin-tracing
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/zipkin-tracing.yml
Artifact (lines [1:2]):
    server:
        port: 9411
"""

zipkin_tracing = CClass(service, "zipkin-tracing", stereotype_instances = [infrastructural, tracing_server], tagged_values = {'Port': 9411, 'Tracing Server': "Zipkin"})



"""
Components:
    - connection configserver to zipkin-tracing
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/zipkin-server/src/main/resources/bootstrap.yml
Artifact (lines [4;8:9]):
    cloud:
        config:

            uri: http://localhost:8888
"""

add_links({configserver: zipkin_tracing}, stereotype_instances = restful_http)



"""
Components:
    - MySQL database "mysqldb", port 3306
    - plaintext crdentials
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/docker-orchestration/docker-compose/docker-compose.yml
Artifact (lines [3:4]):
    mysqldb:
        image: mysql:latest
And artifact (lines [12:13]):
    ports:
      - "3306/tcp"

Plaintext credentials:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/docker-orchestration/docker-compose/docker-compose.yml
Artifact (lines [15:16]):
    MYSQL_ROOT_PASSWORD: password
    MYSQL_DATABASE: auth
"""

mysqldb = CClass(service, "mysqldb", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Port': 3306, 'Database': "MySQL", 'Password': "password"})



"""
Components:
    - authorization server (Spring Cloud OAuth2), "auth-server", port 8899
    - plaintext credentials
    - authentication scope all requests
    - resource server
    - endpoints ["/me"]
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/java/com/anilallewar/microservices/auth/config/OAuthServerConfiguration.java
Artifact (line 52):
    @EnableAuthorizationServer
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: auth-server
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/auth-server.yml
Artifact (lines [3:4]):
    server:
        port: 8899

Plaintext credentials:
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/auth-server.yml
Artifact (lines [12:14]):
    security:
        user:
            password: password

Authentication:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/java/com/anilallewar/microservices/auth/config/OAuthWebFormConfiguration.java
Artifact (liens [34:47]):
    http
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .formLogin()
                .loginPage("/login")
                    .permitAll()
        .and()
            .requestMatchers()
                .antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access")
        .and()
            .authorizeRequests()
                .anyRequest()
                    .authenticated();

Resource server:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/java/com/anilallewar/microservices/auth/AuthServerApplication.java
Artifact (line 22):
    @EnableResourceServer

Endpoints:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/java/com/anilallewar/microservices/auth/api/AuthUserController.java
Artifact (line 31):
    @RequestMapping(path = "/me", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
"""

auth_server = CClass(service, "auth-server", stereotype_instances = [infrastructural, authorization_server, resource_server, authentication_scope_all_requests, plaintext_credentials], tagged_values = {'Port': 8899, 'Authorization Server': "Spring OAuth2", 'Endpoints': "[\'/me\']", 'Username': "user", 'Password': "password"})



"""
Components:
    - connection configserver to auth-server
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: auth_server}, stereotype_instances = restful_http)



"""
Components:
    - connection mysqldb to auth-server
    - plaintext credentials
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/auth-server.yml
Artifact (lines [22:26]):
    spring:
        datasource:
            url: jdbc:mysql://localhost:3306/auth
            username: root
            password: password
"""

add_links({mysqldb: auth_server}, stereotype_instances = [restful_http, plaintext_credentials_link], tagged_values = {'Username': "root", 'Password': "password"})



"""
Components:
    - connection auth-server to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/auth-server/src/main/java/com/anilallewar/microservices/auth/AuthServerApplication.java
Artifact (line 21):
    @EnableEurekaClient
"""

add_links({auth_server: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - monitoring dashboard (Hystrix), "web-portal", port 8090
    - monitoring server (Turbine)
    - authentication scope all requests
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/web-portal/src/main/java/com/anilallewar/microservices/portal/PortalApplication.java
Artifact (line 31):
    @EnableHystrixDashboard
And artifact (line 32):
    @EnableTurbine
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/web-portal.yml
Artifact (lines [3:4]):
    server:
        port: 8090

Authentication:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/web-portal/src/main/java/com/anilallewar/microservices/portal/PortalApplication.java
Artifact (lines [42:43]):
    http.logout().and().authorizeRequests().antMatchers("/**/*.html", "/", "/login","/hystrix/**","/turbine.stream").permitAll().anyRequest()
        .authenticated();
"""

web_portal = CClass(service, "web-portal", stereotype_instances = [infrastructural, monitoring_dashboard, monitoring_server, authentication_scope_all_requests], tagged_values = {'Port': 8090, 'Monitoring Server': "Turbine", 'Monitoring Dashboard': "Hystrix"})



"""
Components:
    - connection web-portal to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/web-portal/src/main/java/com/anilallewar/microservices/portal/PortalApplication.java
Artifact (line 30):
    @EnableEurekaClient
"""

add_links({web_portal: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - connection configserver to web-portal
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/web-portal/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: web_portal}, stereotype_instances = restful_http)



"""
Components:
    - internal service "user-webservice", port 8091
    - resource server
    - local logging
    - endpoints ["/", "/{userName}"]
    - authentication scope all requests
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/UserApplication.java
Artifact (line 30):
    @SpringBootApplication
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/resources/bootstrap.yml
Artifact (lines [2;4]):
    application:

        name: user-webservice
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/user-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8091

Resource server:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/UserApplication.java
Artifact (line 32):
    @EnableResourceServer

Local logging:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/api/UserController.java
Artifact (lines [26;58]):
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

        LOGGER.info(String.format("Found matching user: %s", userDtoToReturn.toString()));

Endpoints:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/api/UserController.java
Artifact (line 23):
    @RequestMapping("/")
And artifact (line 51):
    @RequestMapping(value = "{userName}", method = RequestMethod.GET, headers = "Accept=application/json")

Authentication:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/config/security/ResourceServerConfiguration.java
Artifact (lines [40:46]):
    http
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
                .anyRequest()
                    .authenticated();
"""

user_webservice = CClass(service, "user-webservice", stereotype_instances = [internal, local_logging, resource_server, authentication_scope_all_requests], tagged_values = {'Port': 8091, 'Endpoints': "[\'/\', \'/{userName}\']"})



"""
Components:
    - connection user-webservice to zipkin-tracing
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/user-webservice.yml
Artifact (lines [11:12]):
    zipkin:
        baseUrl: http://localhost:9411/
"""

add_links({user_webservice: zipkin_tracing}, stereotype_instances = restful_http)



"""
Components:
    - connection configserver to user-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/resources/bootstrap.yml
Artifact (lines [5:6;9]):
    cloud:
        config:

            uri: http://localhost:8888
"""

add_links({configserver: user_webservice}, stereotype_instances = restful_http)



"""
Components:
    - connection user-webservice to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/UserApplication.java
Artifact (line 31):
    @EnableEurekaClient
"""

add_links({user_webservice: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - connection auth-server to user-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/user-webservice/src/main/java/com/anilallewar/microservices/user/UserApplication.java
Artifact (line 34):
    @EnableOAuth2Client
"""

add_links({auth_server: user_webservice}, stereotype_instances = restful_http)



"""
Components:
    - internal service "comments-webservice", port 8083
    - resource server
    - local logging
    - endpoints ["/comments", "/comments/{taskId}"]
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/java/com/anilallewar/microservices/comments/CommentsApplication.java
Artifact (line 28):
    @SpringBootApplication
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/resources/bootstrap.yml
Artifact (lines [2;5]):
    application:

        name: comments-webservice
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/comments-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8083

Resource Server:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/java/com/anilallewar/microservices/comments/CommentsApplication.java
Artifact (line 30):
    @EnableResourceServer

Local logging:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/java/com/anilallewar/microservices/comments/apis/CommentsController.java
Artifact (lines [33;65])
    private static final Logger LOGGER = Logger.getLogger(CommentsController.class.getName());

        LOGGER.info(String.format("Found matching comments for task [%s] with comment [%s]", taskId,

Endpoints:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/java/com/anilallewar/microservices/comments/apis/CommentsController.java
Artifact (line 30):
    @RequestMapping("/comments")
And artifact (line 59):
    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET, headers = "Accept=application/json")
"""

comments_webservice = CClass(service, "comments-webservice", stereotype_instances = [internal, local_logging, resource_server], tagged_values = {'Port': 8083, 'Endpoints': "[\'/comments\', \'/comments/{taskId}\']"})



"""
Components:
    - connection comments-webservice to zipkin-server
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/comments-webservice.yml
Artifact (lines [10:11]):
    zipkin:
        baseUrl: http://localhost:9411/
"""

add_links({comments_webservice: zipkin_tracing}, stereotype_instances = restful_http)



"""
Components:
    - connection configserver to comments-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/resources/bootstrap.yml
Artifact (lines [17:19]):
    cloud:
        config:
            uri: http://configserver:8888
"""

add_links({configserver: comments_webservice}, stereotype_instances = restful_http)



"""
Components:
    - connection comments-webservice to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/comments-webservice/src/main/java/com/anilallewar/microservices/comments/CommentsApplication.java
Artifact (line 29):
    @EnableEurekaClient
"""

add_links({comments_webservice: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - internal service "task-webservice", port 8082
    - circuit breaker
    - authentication scope all requests
    - local logging
    - endpoints ["/", "/{taskId}", "/usertask/{userName}"]
    - load balancer
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/TaskApplication.java
Artifact (line 38):
    @SpringBootApplication
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/resources/bootstrap.yml
Artifact (lines [2;5]):
  application:

    name: task-webservice
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/task-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8082

Circuit breaker:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/TaskApplication.java
Artifact (line 40):
    @EnableCircuitBreaker

Resource Server:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/TaskApplication.java
Artifact (line 41):
    @EnableResourceServer

Authentication:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/config/security/WebSecurityConfiguration.java
Artifact (lines [18:21]):
    http
        .authorizeRequests()
            .anyRequest()
                .authenticated();

Local logging:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/apis/CommentsService.java
Artifact (lines [31;91]):
    private static final Logger LOGGER = Logger.getLogger(CommentsService.class.getName());

        LOGGER.info(String.format("Checking comments for taskId [%s]", taskId));

Endpoints:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/apis/TaskController.java
Artifact (line 22):
    @RequestMapping("/")
And artifact (line 48):
    @RequestMapping(value = "{taskId}", method = RequestMethod.GET, headers = "Accept=application/json")
And artifact (line 70):
    @RequestMapping(value = "/usertask/{userName}", method = RequestMethod.GET, headers = "Accept=application/json")

Load Balancer:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/config/OAuthClientConfiguration.java
Artifact (line 30):
    @LoadBalanced
"""

task_webservice = CClass(service, "task-webservice", stereotype_instances = [internal, local_logging, authentication_scope_all_requests, resource_server, circuit_breaker, load_balancer], tagged_values = {'Port': 8082, 'Circuit Breaker': "Hystrix", 'Endpoints': "[\'/\', \'/{taskId}\', \'/usertask/{userName}\']", 'Load Balancer': "Spring Cloud"})



"""
Components:
    - connection task-webservice to zipkin-tracing
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/task-webservice.yml
Artifact (lines [11:12]):
    zipkin:
        baseUrl: http://localhost:9411/
"""

add_links({task_webservice: zipkin_tracing}, stereotype_instances = restful_http)



"""
Components:
    - connection configserver to task-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/resources/bootstrap.yml
Artifact (lines [6:7;11]):
    cloud:
        config:

            uri: http://localhost:8888
"""

add_links({configserver: task_webservice}, stereotype_instances = restful_http)



"""
Components:
    - connection auth-server to task-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/TaskApplication.java
Artifact (line 42):
    @EnableOAuth2Client
"""

add_links({auth_server: task_webservice}, stereotype_instances = restful_http)



"""
Components:
    - connection task-webservice to webservice-registry
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/TaskApplication.java
Artifact (line 39):
    @EnableEurekaClient
"""

add_links({task_webservice: webservice_registry}, stereotype_instances = restful_http)



"""
Components:
    - connection task-webservice to zipkin-tracing
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/apis/CommentsService.java
Artifact (line 69):
    @HystrixCommand(fallbackMethod = "getFallbackCommentsForTask", commandProperties = {
"""

add_links({task_webservice: web_portal}, stereotype_instances = restful_http)



"""
Components:
    - connection task-webservice to comments-webservice
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/task-webservice/src/main/java/com/anilallewar/microservices/task/apis/CommentsService.java
Artifact (line 93):
    return restTemplate.getForObject(String.format("http://comments-webservice/comments/%s", taskId),
"""

add_links({task_webservice: comments_webservice}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link])



"""
Components:
    - api gateway (Zuul), "api-gateway", port 8765
    - load balancer (Ribbon)
    - circuit breaker (Hystrix)
    - user (implicit)
    - connections between user and api-gateway (implicit)
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/api-gateway/src/main/java/com/anilallewar/microservices/gateway/GatewayApplication.java
Artifact (line 48):
    @EnableZuulProxy
And file: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/api-gateway/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: api-gateway
And file: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (lines [59:60]):
    server:
        port: 8765

Load balancer:
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (line 114):
    ribbon:

Circuit breaker:
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (line 106):
    hystrix:

CSRF:
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/api-gateway/src/main/java/com/anilallewar/microservices/gateway/security/WebSecurityConfiguration.java
Artifact (lines 40:41):
    .csrf()
        .disable();
"""

api_gateway = CClass(service, "api-gateway", stereotype_instances = [infrastructural, gateway, load_balancer, circuit_breaker, csrf_disabled], tagged_values = {'Port': 8765, 'Gateway': "Zuul", 'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})

user = CClass(external_component, "user", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({api_gateway: user}, stereotype_instances = restful_http)

add_links({user: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection configserver to api-gateway
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/api-gateway/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection webservice-registry to api-gateway
File: https://github.com/anilallewar/microservices-basics-spring-boot/blob/master/api-gateway/src/main/java/com/anilallewar/microservices/gateway/GatewayApplication.java
Artifact (line 47):
    @EnableEurekaClient
"""

add_links({webservice_registry: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection api-gateway to zipkin-server
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (lines [10:11]):
    zipkin:
        baseUrl: http://localhost:9411/
"""

add_links({api_gateway: zipkin_tracing}, stereotype_instances = restful_http)



"""
Components:
    - connection auth-server to api-gateway
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (lines [27:29]):
    authserver:
        hostname: localhost
        port: 8899
"""

add_links({auth_server: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection api-gateway to user-webservice
    - connection api-gateway to task-webservice
File: https://github.com/anilallewar/microservices-basics-cloud-config/blob/master/api-gateway.yml
Artifact (lines [71:81]):
    routes:
        user-service:
          path: /user-service/**
          stripPrefix: false
          serviceId: user-webservice
          sensitiveHeaders:
        task-service:
          path: /task-service/**
          stripPrefix: false
          serviceId: task-webservice
          sensitiveHeaders:
"""

add_links({api_gateway: user_webservice}, stereotype_instances = [restful_http, load_balanced_link, circuit_breaker_link])

add_links({api_gateway: task_webservice}, stereotype_instances = [restful_http, load_balanced_link, circuit_breaker_link])







##### Create model
model = CBundle(model_name, elements = webservice_registry.class_object.get_connected_elements())

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
