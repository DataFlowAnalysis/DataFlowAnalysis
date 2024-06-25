from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/rohitghatol/spring-boot-microservices

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "rohitghatol_spring-boot-microservices"



"""
Component:
    - config server (Spring Cloud Config), "configserver", port 8888
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/config-server/src/main/java/com/rohitghatol/microservices/config/Application.java
Artifact (line 26):
    @EnableConfigServer
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/config-server/src/main/resources/application.yml
Artifact ([2:3]):
    application:
        name: configserver
And artifact (lines [14:15]):
    server:
        port: 8888
"""

configserver = CClass(service, "configserver", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 8888})



"""
Component:
    - external GitHub repository "https://github.com/anilallewar/sample-config"
    - connection external repository to config service
    - plaintext credentials
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/config-server/src/main/resources/application.yml
Artifact (lines [3:6]):
    config:
        server:
            git:
                uri: https://github.com/anilallewar/sample-config

Plaintext credentials:
File: https://github.com/anilallewar/sample-config/blob/master/api-gateway.yml
Artifact (lines [47:51]):
    client:
          accessTokenUri: http://${authserver.hostname}:${authserver.port}/${authserver.contextPath}/oauth/token
          userAuthorizationUri: http://${authserver.hostname}:${authserver.port}/${authserver.contextPath}/oauth/authorize
          clientId: client
          clientSecret: secret
And artifact (lines [65:66]):
    username: root
    password: password
"""

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/anilallewar/sample-config"})

add_links({github_repository: configserver}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - service discovery (Eureka), "webservice-registry", port 8761
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/webservice-registry/src/main/java/com/rohitghatol/microservices/registry/Application.java
Artifact (line 24):
    @EnableEurekaServer
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/webservice-registry/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: webservice-registry

Port:
File: https://github.com/anilallewar/sample-config/blob/master/webservice-registry.yml
Artifact (lines [1:2]):
    server:
        port: 8761
"""

webservice_registry = CClass(service, "webservice-registry", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component:
    - connection configserver to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/webservice-registry/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: webservice_registry}, stereotype_instances = restful_http)



"""
Component:
    - authorization server (Spring OAuth2), "auth-server", port 8899
    - endpoint ["/me"]
    - encryption (BCryptPasswordEncoder)
    - tokenstore
    - plaintext credentials
    - resource server
    - authorization
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/OAuthConfiguration.java
Artifact (line 29):
    @EnableAuthorizationServer
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: auth-server

Port:
File: https://github.com/anilallewar/sample-config/blob/master/auth-server.yml
Artifact (lines [3:4]):
    server:
        port: 8899

Endpoint:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/api/AuthUserController.java
Artifact(line 24):
    @RequestMapping("/me")

Encryption:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/OAuthConfiguration.java
Artifact (line 38):
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
And artifact (line 88):
    .passwordEncoder(passwordEncoder)

Tokenstore:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/OAuthConfiguration.java
Artifact (lines [48:49]):
    public JdbcTokenStore tokenStore() {
    		return new JdbcTokenStore(dataSource);

Plaintext credentials:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/OAuthConfiguration.java
Artifact (lines [121:124]):
	auth.jdbcAuthentication().dataSource(dataSource).withUser("dave")
			.password("secret").roles("USER");
	auth.jdbcAuthentication().dataSource(dataSource).withUser("anil")
			.password("password").roles("ADMIN");
And file: https://github.com/anilallewar/sample-config/blob/master/auth-server.yml
Artifact (lines [13:14]):
    user:
        password: password

Resource server:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/ResourceServerConfiguration.java
Artifact (line 22):
    @EnableResourceServer

Authorization:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/config/ResourceServerConfiguration.java
Atifact (lines [38:42]):
    http
    .requestMatchers().antMatchers("/me")
    .and()
    .authorizeRequests()
    .antMatchers("/me").access("#oauth2.hasScope('read')");
"""

auth_server = CClass(service, "auth-server", stereotype_instances = [authorization_server, infrastructural, encryption, tokenstore, plaintext_credentials, resource_server, authorization_scope_all_requests], tagged_values = {'Authorization Server': "Spring OAuth2", 'Port': 8899, 'Endpoints': "[\'/me\', \'/\']", 'Username': "dave", 'Password': "secret"})



"""
Component:
    - external database (MySQL), port 3306
    - connection external database to auth-server
    - plaintext credentials
File: https://github.com/anilallewar/sample-config/blob/master/auth-server.yml
Artifact (lines [19:22]):
    datasource:
        url: jdbc:mysql://localhost:3306/auth
        username: root
        password: password
"""

database_auth_server = CClass(external_component, "database-auth-server", stereotype_instances = [external_database, plaintext_credentials, entrypoint, exitpoint], tagged_values = {'Database': "MySQL", 'Port': 3306, 'Username': "root", 'Password': "password"})

add_links({database_auth_server: auth_server}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "root", 'Password': "password"})



"""
Component:
    - connection configserver to auth-server
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    config:
        uri: http://localhost:8888
"""

add_links({configserver: auth_server}, stereotype_instances = restful_http)



"""
Component:
    - connection auth-server to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/auth-server/src/main/java/com/rohitghatol/microservice/auth/Application.java
Artifact (line 26):
    @EnableEurekaClient
"""

add_links({auth_server: webservice_registry}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - internal service "web-portal", port 8090
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/web-portal/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: web-portal

Port:
File: https://github.com/anilallewar/sample-config/blob/master/web-portal.yml
Artifact (lines [1:2]):
    server:
        port: 8090
And artifact (lines [24:25]):
    server:
        port: 8080
"""

web_portal = CClass(service, "web-portal", stereotype_instances = [internal], tagged_values = {'Port': 8090, 'Port': 8080})



"""
Component:
    - connetion configserver to web-portal
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/web-portal/src/main/resources/bootstrap.yml
Artifact (lines [4:6]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({configserver: web_portal}, stereotype_instances = restful_http)



"""
Component:
    - connection web-portal to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/web-portal/src/main/java/com/rohitghatol/microservices/portal/Application.java
Artifact (line 33):
    @EnableEurekaClient
"""

add_links({web_portal: webservice_registry}, stereotype_instances = restful_http)



"""
Component:
    - internal service "user-webservice", port 8081
    - endpoints ["/", "/{userName}"]
    - resource server
    - authoriztion
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/resources/bootstrap.yml
Artifact (lines 2:5):
    application:
    # Name of the service that is using with Zuul routes to forward specific requests to this service
        name: user-webservice

Endpoints:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/java/com/rohitghatol/microservices/user/apis/UserController.java
Artifact (line 21):
    @RequestMapping("/")
And artifact (line 47):
    @RequestMapping(value = "{userName}", method = RequestMethod.GET, headers = "Accept=application/json")

Resource server:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/java/com/rohitghatol/microservices/user/config/UserConfiguration.java
Artifact (line 20):
    @EnableResourceServer

Authorization:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/java/com/rohitghatol/microservices/user/config/UserConfiguration.java
Artifact (lines [30:35]):
    http.requestMatchers()
        .antMatchers("/**")
    .and()
        .authorizeRequests()
            .anyRequest()
                .authenticated()

Port:
File: https://github.com/anilallewar/sample-config/blob/master/user-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8081
And artifact (lines [43:44]):
    server:
        port: 8080
"""

user_webservice = CClass(service, "user-webservice", stereotype_instances = [internal, resource_server, authentication_scope_all_requests], tagged_values = {'Port': 8081, 'Port': 8080, 'Endpoints': "[\'/\', \'/{userName}\']"})



"""
Component:
    - connection configserver to user-webservice
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/resources/bootstrap.yml
Artifact (lines [6:11]):
    cloud:
        config:
    # Define the URL from where this service would pick up it's external configuration. Note that it is
    # pointing to the config-server aplication
        uri: http://localhost:8888
"""

add_links({configserver: user_webservice}, stereotype_instances = restful_http)



"""
Component:
    - connection user-webservice to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/user-webservice/src/main/java/com/rohitghatol/microservices/user/Application.java
Artifact (line 39):
    @EnableEurekaClient
"""

add_links({user_webservice: webservice_registry}, stereotype_instances = restful_http)



"""
Component:
    - connection auth-server to user-webservice
File: https://github.com/anilallewar/sample-config/blob/master/user-webservice.yml
Artifact (lines [10:13]):
    authserver:
        hostname: localhost
        port: 8899
        contextPath: userauth
"""

add_links({auth_server: user_webservice}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - internal service "comments-webservice", port 8083
    - resource server
    - endpoints ["/comments", "/comments/{taskId}"]
    - authorization
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    application:

    # Name of the service that is using with Zuul routes to forward specific requests to this service
        name: comments-webservice

Resource server:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/java/com/rohitghatol/microservices/comments/config/CommentsConfiguration.java
Atifact (line 20):
    @EnableResourceServer

Endpoints:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/java/com/rohitghatol/microservices/comments/apis/CommentsController.java
Artifact (line 28):
    @RequestMapping("/comments")
And artifact (line 55):
        @RequestMapping(value = "/{taskId}", method = RequestMethod.GET, headers = "Accept=application/json")

Authorization:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/java/com/rohitghatol/microservices/comments/config/CommentsConfiguration.java
Artifact (lines [30:35]):
    http.requestMatchers()
        .antMatchers("/**")
	.and()
        .authorizeRequests()
            .anyRequest()
                .authenticated()

Port:
File: https://github.com/anilallewar/sample-config/blob/master/comments-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8083
And artifact (lines [43:44]):
    server:
        port: 8080
"""

comments_webservice = CClass(service, "comments-webservice", stereotype_instances = [internal, resource_server, authentication_scope_all_requests], tagged_values = {'Port': 8083, 'Port': 8080, 'Endpoints': "[\'/comments\', \'/comments/{taskId}\']"})



"""
Component:
    - connection configserver to comments-webservice
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/resources/bootstrap.yml
Artifact (lines [7:11]):
    config:

    # Define the URL from where this service would pick up it's external configuration. Note that it is
    # pointing to the config-server aplication
        uri: http://localhost:8888
"""

add_links({configserver: comments_webservice}, stereotype_instances = restful_http)



"""
Component:
    - connection comments-webservice to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/comments-webservice/src/main/java/com/rohitghatol/microservices/comments/Application.java
Artifact (line 40):
    @EnableEurekaClient
"""

add_links({comments_webservice: webservice_registry}, stereotype_instances = restful_http)



"""
Component:
    - connection auth-server to comments-webservice
File: https://github.com/anilallewar/sample-config/blob/master/comments-webservice.yml
Artifact (lines [10:13]):
    authserver:
        hostname: localhost
        port: 8899
        contextPath: userauth
"""

add_links({auth_server: comments_webservice}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - internal service "task-webservice", port 8082
    - circuit breaker for connections (Hystrix)
    - endpoints ["/", "/{taskId}", "/usertask/{userName}"]
    - resource server
    - authorization
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    application:

    # Name of the service that is using with Zuul routes to forward specific requests to this service
        name: task-webservice

Circuit breaker:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/Application.java
Artifact (line 48):
    @EnableCircuitBreaker
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/build.gradle
Artifact (line 38):
    compile("org.springframework.cloud:spring-cloud-starter-hystrix:${project.cloudVersion}"

Endpoints:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/apis/TaskController.java
Artifact (line 22):
    @RequestMapping("/")
And artifact (line 48):
    @RequestMapping(value = "{taskId}", method = RequestMethod.GET, headers = "Accept=application/json")
And artifact (line 70):
    @RequestMapping(value = "/usertask/{userName}", method = RequestMethod.GET, headers = "Accept=application/json")

Resource server:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/config/TaskConfiguration.java
Artifact (line 20):
    @EnableResourceServer

Authorization:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/config/TaskConfiguration.java
Artifact (lines [30:35]):
    http.requestMatchers()
        .antMatchers("/**")
    .and()
        .authorizeRequests()
            .anyRequest()
                .authenticated()

Port:
File: https://github.com/anilallewar/sample-config/blob/master/task-webservice.yml
Artifact (lines [1:2]):
    server:
        port: 8082
And artifact (lines [47:48]):
    server:
        port: 8080
"""

task_webservice = CClass(service, "task-webservice", stereotype_instances = [internal, circuit_breaker, resource_server, authentication_scope_all_requests], tagged_values = {'Port': 8082, 'Port': 8080, 'Endpoints': "[\'/\', \'/{taskId}\', \'/usertask/{userName}\']"})



"""
Component:
    - connection task-webservice to webservice-registry
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/Application.java
Artifact (line 46):
    @EnableEurekaClient
"""

add_links({task_webservice: webservice_registry}, stereotype_instances = [restful_http, circuit_breaker_link])



"""
Component:
    - connection auth-server to task-webservice
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/config/OAuthClientConfiguration.java
Artifact (line 21):
    @EnableOAuth2Client
"""

add_links({auth_server: task_webservice}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - connection configserver to task-webservice
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/resources/bootstrap.yml
Artifact (lines [7:11]):
    config:

    # Define the URL from where this service would pick up it's external configuration. Note that it is
    # pointing to the config-server aplication
        uri: http://localhost:8888
"""

add_links({configserver: task_webservice}, stereotype_instances = restful_http)



"""
Component:
    - connection task-webservice to comments-webservice
    - circuit breaker
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/task-webservice/src/main/java/com/rohitghatol/microservices/task/apis/CommentsService.java
Artifact (line 71):
    return restTemplate.getForObject(String.format("http://comments-webservice/comments/%s", taskId),
"""

add_links({task_webservice: comments_webservice}, stereotype_instances = [restful_http, circuit_breaker_link])



"""
Component:
    - API gateway (Zuul), "api-gateway", port 8765
    - authentication for all connections
    - load balancer (Zuul)
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/java/com/rohitghatol/microservice/gateway/Application.java
Artifact (line 51):
    @EnableZuulProxy
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: api-gateway

Authentication:
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/java/com/rohitghatol/microservice/gateway/Application.java
Artifact (line 53):
    @EnableOAuth2Sso
And file: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/java/com/rohitghatol/microservice/gateway/config/OAuthConfiguration.java
Artifact (lines [46:50]):
    http
	.authorizeRequests()
	//Allow access to all static resources without authentication
	.antMatchers("/","/**/*.html").permitAll()
	.anyRequest().authenticated()

Port:
File: https://github.com/anilallewar/sample-config/blob/master/api-gateway.yml
Artifact (lines [2:3]):
    server:
        port: 8765
And artifact (lines [96:97]):
    server:
        port: 8080
"""

api_gateway = CClass(service, "api-gateway", stereotype_instances = [gateway, infrastructural, authentication_scope_all_requests, load_balancer], tagged_values = {'Gateway': "Zuul", 'Port': 8765, 'Port': 8080, 'Load Balancer': "Ribbon"})



"""
Components:
    - user
    - connections between user and api-gateway
Implicit with gateway
"""


user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: api_gateway}, role_name = "target", stereotype_instances = restful_http)
add_links({api_gateway: user}, role_name = "target", stereotype_instances = restful_http)



"""
Component:
    - connection api-gateway to auth-server
File: https://github.com/anilallewar/sample-config/blob/master/api-gateway.yml
Artifact (lines [6:8]):
    authserver:
        hostname: localhost
        port: 8899
"""

add_links({api_gateway: auth_server}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - connection external database to api-gateway
    - plaintext credentials
File: https://github.com/anilallewar/sample-config/blob/master/api-gateway.yml
Artifact (lines [63:66]):
    datasource:
        url: jdbc:mysql://localhost:3306/auth
        username: root
        password: password
"""

add_links({database_auth_server: api_gateway}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "root", 'Password': "password"})



"""
Component:
    - connection api-gateway to user-webservice
    - connection api-gateway to task-webservice
    - connection api-gateway to web-portal
File: https://github.com/anilallewar/sample-config/blob/master/api-gateway.yml
Artifact (lines [18:24]):
    zuul:
        routes:
            user-webservice: /api/user/**
            task-webservice: /api/task/**
            web-portal: /**
            user:
                path: /api/loggedinuser/**
"""

add_links({api_gateway: user_webservice}, stereotype_instances = restful_http)

add_links({api_gateway: task_webservice}, stereotype_instances = restful_http)

add_links({api_gateway: web_portal}, stereotype_instances = restful_http)



"""
Component:
    - connection configserver to api-gateway
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    config:
        uri: http://localhost:8888
"""

add_links({configserver: api_gateway}, stereotype_instances = restful_http)



"""
Component:
    - connection webservice-registry to api-gateway
File: https://github.com/rohitghatol/spring-boot-microservices/blob/master/api-gateway/src/main/java/com/rohitghatol/microservice/gateway/Application.java
Artifact (line 52):
    @EnableEurekaClient
"""

add_links({webservice_registry: api_gateway}, stereotype_instances = restful_http)






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
