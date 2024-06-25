from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/mdeket/spring-cloud-movie-recommendation

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "mdeket_spring-cloud-movie-recommendation"


"""
Components:
    - Config Service (Spring Cloud Config), "config-service", port 8888
    - external repository https://github.com/mdeket/spring-cloud-example-config-repo.git
    - connection config service to external repo
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/config-service/src/main/java/com/example/ConfigServiceApplication.java
Artifact (lines[7:8]):
    @EnableConfigServer
    @SpringBootApplication
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/config-service/src/main/resources/application.properties
Artifact (lines[1:3]):
    server.port=8888
    spring.application.name= config-service
    spring.cloud.config.server.git.uri=https://github.com/mdeket/spring-cloud-example-config-repo.git
"""

config_service = CClass(service, "config-service", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Port': 8888, 'Configuration Server': "Spring Cloud Config"})

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/mdeket/spring-cloud-example-config-repo.git"})

add_links({github_repository: config_service}, stereotype_instances = restful_http, tagged_values: {'Protocol': "HTTPS"})



"""
Component: discovery service (Eureka), "eureka-service", port 8761
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/eureka-service/src/main/java/com/example/EurekaServiceApplication.java
Artifact (lines[7:8]):
    @EnableEurekaServer
    @SpringBootApplication
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/eureka-service-default.yml
Artifact (lines[1:2]):
    server:
        port: 8761
"""

eureka_service = CClass(service, "eureka-service", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component: connection eureka service to config service
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/eureka-service/src/main/resources/bootstrap.properties
Artifact (line 2):
    spring.cloud.config.uri=http://localhost:8888
"""

add_links({config_service: eureka_service}, stereotype_instances = restful_http)



"""
Components:
    - movie-service (internal), port 8002
    - connection to eureka server
        - use of load balancer (Hystrix) and dashboard
        - use of logging (Zipkin)
        - rabbit
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/movie-service/src/main/java/com/example/MovieServiceApplication.java
Artifact (lines[7:8]):
    @EnableEurekaClient
    @SpringBootApplication
And file:
Artifact (lines[1:2]):
    server:
        port: 8002

Endpoints:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/movie-service/src/main/java/com/example/MovieController.java
Artifact (line 25):
    @RequestMapping("/movie")
And artifact (line 39):
    @RequestMapping(value = "/list", method = RequestMethod.GET)
And artifact (line 53):
    @RequestMapping(method = RequestMethod.GET, value = "/{movieId}")
And artifact (line 58):
    @RequestMapping(method = RequestMethod.GET, value = "/dummyData")
"""

movie_service = CClass(service, "movie-service", stereotype_instances = internal, tagged_values = {'Port': 8002, 'Endpoints': "[\'/movie/dummyData\', \'/movie/list\', \'/movie/{movieId}\', \'/movie\']"})

add_links({movie_service: eureka_service}, stereotype_instances = restful_http)



"""
Component: connection movie-service to config server
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/movie-service/src/main/resources/bootstrap.properties
Artifact (lines[1:2]):
    spring.application.name=movie-service
    spring.cloud.config.uri=http://localhost:8888
"""

add_links({config_service: movie_service}, stereotype_instances = restful_http)



"""
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/README.md
Artifact:
    "Before starting this service, just be sure to have installed and a running mongodb instance on default port."
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/user-service-default.yml (in the external repository used by config server)
Artifact (lines[4:9]):
    spring:
        profiles: default
        datasource:
            url: jdbc:mysql://localhost:3306/UserSOA
            username: root
            password: root
"""

database_movie_service = CClass(external_component, "database-movie-service", stereotype_instances = [entrypoint, exitpoint, external_database])

add_links({database_movie_service: movie_service}, stereotype_instances = jdbc)



"""
Components:
    - user-service (internal), port 8001
    - connection to service discovery
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/user-service/src/main/java/com/example/UserServiceApplication.java
Artifact (lines[7:8]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/user-service-default.yml
Artifact (lines[1:2]):
    server:
        port: 8001

Endpoints:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/user-service/src/main/java/com/example/UserController.java
Artifact (line 20):
    @RequestMapping("/user")
And artifact (line 31):
    @RequestMapping(value="/{userId}", method = RequestMethod.GET
"""

user_service = CClass(service, "user-service", stereotype_instances = internal, tagged_values = {'Port': 8001, 'Endpoints': "[\'/user\', \'/user/{userId}\']"})

add_links({user_service: eureka_service}, stereotype_instances = restful_http)



"""Component: connection user-service to config-server
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/user-service/src/main/resources/bootstrap.properties
Artifact (lines[1:2]):
    spring.application.name=user-service
    spring.cloud.config.uri=http://localhost:8888
"""

add_links({config_service: user_service}, stereotype_instances = restful_http)



"""Components:
    - external user-database (MySQL), port 3306
    - connection user-service to user-database
    - plaintext credentials
File:
Artifact (lines[8:16]):
    import org.springframework.data.jpa.repository.JpaRepository;
    [...]
    public interface UserRepo extends JpaRepository<User, Long>{

    }
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/user-service/src/main/java/com/example/UserController.java
Artifact (lines[23:29]):
    @Autowired
    private UserRepo customerRepo;

    @RequestMapping(method = RequestMethod.GET)
    public Collection getAllUsers(){
        return this.customerRepo.findAll();
    }
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/README.md
Artifact:
    "Before starting user service, you must have MySQL installed and running on localhost:3306 with UserSOA database created."
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/user-service-default.yml (in the external repository used by config server)
Artifact (lines[4:9]):
    spring:
        profiles: default
        datasource:
            url: jdbc:mysql://localhost:3306/UserSOA
            username: root
            password: root
"""

database_user_service = CClass(external_component, "database-user-service", stereotype_instances = [entrypoint, exitpoint, external_database, plaintext_credentials], tagged_values = {'Port': 3306, 'Database': "MySQL", 'Username': "root", 'Password': "root"})

add_links({database_user_service: user_service}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "root", 'Password': "root"})



"""
Components:
    - recommendation-service (internal), port 8003
    - connection to discovery server
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-service/src/main/java/com/example/RecommendationServiceApplication.java
Artifact (lines[7:8]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/recommendation-service-default.yml
Artifact (lines[1:2]):
    server:
        port: 8003
Endpoints:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-service/src/main/java/com/example/RecommendationController.java
Artifact (line 23):
    @RequestMapping("/recommendation")
And artifact (line 33):
    @RequestMapping(value = "/user", method = RequestMethod.GET)
And artifact (line 42):
    @RequestMapping(value = "/movie", method = RequestMethod.GET)
And artifact (line 51):
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
And artifact (line 56):
    @RequestMapping(value = "/recommend/user/{userId}", method = RequestMethod.GET)
And artifact (line 67):
    @RequestMapping(value = "/movie/{movieId}", method = RequestMethod.GET)
And artifact (line 72):
    @RequestMapping(value = "/dummyData", method = RequestMethod.GET)
"""

recommendation_service = CClass(service, "recommendation-service", stereotype_instances = internal, tagged_values = {'Port': 8003, 'Endpoints': "[\'/recommendation\', \'/recommendation/recommend/user/{userId}\', \'/recommendation/user\', \'/recommendation/movie/{movieId}\', \'/recommendation/user/{userId}\', \'/recommendation/dummyData\', \'/recommendation/movie\']"})

add_links({recommendation_service: eureka_service}, stereotype_instances = restful_http)



"""
Component: connection recommendation-service to config service
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-service/src/main/resources/bootstrap.properties
Artifact (lines[1:2]):
    spring.application.name=recommendation-service
    spring.cloud.config.uri=http://localhost:8888
"""

add_links({config_service: recommendation_service}, stereotype_instances = restful_http)



"""
Components:
    - external recommendation-database (Neo4j), port 7474
    - connection recommendation-service to recommendation-database
    - plaintext credentials
File:
Artifact (lines[22:53]):
    @Configuration
    @EnableNeo4jRepositories(basePackages = "com.example")
    @EnableTransactionManagement
    public class MyConfiguration extends Neo4jConfiguration {

        @Bean
        public org.neo4j.ogm.config.Configuration getConfiguration() {
           org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
           config
               .driverConfiguration()
               .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
               .setURI("http://neo4j:root@localhost:7474");
           return config;
        }
        [...]
    }
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-service/src/main/java/com/example/UserRepo.java
Artifact (line 14):
    public interface UserRepo extends GraphRepository<User>{
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/5aa5ee9e2e64c33e294409e39cb9708591230e08/recommendation-service/src/main/java/com/example/MyConfiguration.java
Artifact (line 33):
    @RequestMapping(value = "/user", method = RequestMethod.GET)
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/recommendation-service-default.yml
Artifact (lines[3:6]):
    neo4j:
        uri: http://localhost:7474/
        username: neo4j
        password: root
"""

database_recommendation_service = CClass(external_component, "database-recommendation-service", stereotype_instances = [exitpoint, entrypoint, external_database, plaintext_credentials], tagged_values = {'Database': "Neo4j", 'Port': 7474, 'Username': "neo4j", 'Password': "root"})

add_links({database_recommendation_service: recommendation_service}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "neo4j", 'Password': "root"})



"""
Components:
    - API gateway (Zuul), "recommendation-client", port 9000
    - load balancer (built in for Zuul)
    - connection to discovery service
    - user (implicit for gateway)
    - connection user to recommendation-client
    - use of circuit breaker (Hystrix)
    - monitoring dashboard (Hystrix)
    - local logging
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/RecommendationClientApplication.java
Artifact (lines[11:16]):
    @SpringBootApplication
    @EnableZuulProxy
    @EnableFeignClients(basePackages = "com.example")
    @EnableDiscoveryClient
    @EnableCircuitBreaker
    @EnableHystrixDashboard
And file: https://github.com/mdeket/spring-cloud-example-config-repo/blob/master/recommendation-client-default.yml
Artifact (lines[1:2]):
    server:
        port: 9000

Local logging:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/MainController.java
Artifact (lines[21;51]):
    import java.util.logging.Logger;

            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);

Circuit Breaker:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/5aa5ee9e2e64c33e294409e39cb9708591230e08/recommendation-client/pom.xml
Artifact (line 77):
    <artifactId>spring-cloud-netflix-hystrix-dashboard</artifactId>

Endpoints:
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/service/RecommendationService.java
Artifact (line 19):
    @RequestMapping(method = RequestMethod.GET, value = "/recommendation/dummyData")
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/service/MovieService.java
Artifact (line 20):
    @RequestMapping(method = RequestMethod.GET, value = "/movie")
And artifact (line 23):
    @RequestMapping(method = RequestMethod.GET, value = "/movie/dummyData")
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/service/UserService.java
Artifact (line 22):
    @RequestMapping(method = RequestMethod.GET, value = "/user")
And artifact (line 26):
    @RequestMapping(method = RequestMethod.GET, value = "/newuser")
And artifact (line 29):
    @RequestMapping(method = RequestMethod.GET, value = "/user/{userId}")
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/MainController.java
Artifact (line 35):
    @RequestMapping("/api")
And artifact (line 45):
    @GetMapping(value ="/recommendation/user/{userId}")
And artifact (line 66):
    @GetMapping(value = "/userDetails/{userId}")
"""

recommendation_client = CClass(service, "recommendation-client", stereotype_instances = [gateway, monitoring_dashboard, local_logging, infrastructural, circuit_breaker, load_balancer], tagged_values = {'Port': 9000, 'Gateway': "Zuul", 'Monitoring Dashboard': "Hystrix", 'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon", 'Endpoints': "[\'/user/{userId}\', \'/api/userDetails/{userId}\', \'/user\', \'/api\', \'/recommendation/dummyData\', \'/movie/dummyData\', \'/newuser\', \'/api/recommendation/user/{userId}\', \'/movie\']"})

add_links({eureka_service: recommendation_client}, stereotype_instances = restful_http)

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: recommendation_client}, stereotype_instances = restful_http)
add_links({recommendation_client: user}, stereotype_instances = restful_http)



"""
Component: connection recommendation-client to config server
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/resources/bootstrap.properties
Artifact (lines[1:2]):
    spring.application.name=recommendation-client
    spring.cloud.config.uri=http://localhost:8888
"""

add_links({config_service: recommendation_client}, stereotype_instances = restful_http)



"""
Component:
    - connection recommendation-client to recommendation-service
    - circuit breaker for this connection
    - load balancer for this connection (included in FeignClient)
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/RecommendationClientApplication.java
Artifact (lines[13:15]):
    @EnableFeignClients(basePackages = "com.example")
    [...]
    @EnableCircuitBreaker
And file:
Artifact (line 16):
    @FeignClient("recommendation-service")
"""

add_links({recommendation_client: recommendation_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link, feign_connection], tagged_values = {'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon"})



"""
Component:
    - connection recommendation-client to movie-service
    - circuit breaker for this connection
    - load balancer for this connection (included in FeignClient)
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/RecommendationClientApplication.java
Artifact (lines[13:15]):
    @EnableFeignClients(basePackages = "com.example")
    [...]
    @EnableCircuitBreaker
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/service/MovieService.java
Artifact (line 17):
    @FeignClient("movie-service")
"""

add_links({recommendation_client: movie_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link, feign_connection], tagged_values = {'Circuit Breaker': "Hystrix", 'Load Balancer': "Ribbon"})



"""
Component:
    - connection recomendation-client to user-service
    - load balancer for this connection (included in FeignClient)
File: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/RecommendationClientApplication.java
Artifact (lines[13:15]):
    @EnableFeignClients(basePackages = "com.example")
    [...]
    @EnableCircuitBreaker
And file: https://github.com/mdeket/spring-cloud-movie-recommendation/blob/master/recommendation-client/src/main/java/com/example/service/UserService.java
Artifact (line 19):
    @FeignClient("user-service")
"""

add_links({recommendation_client: user_service}, stereotype_instances = [restful_http, load_balanced_link, circuit_breaker_link, feign_connection], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})



##### Create model
model = CBundle(model_name, elements = config_service.class_object.get_connected_elements())


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
