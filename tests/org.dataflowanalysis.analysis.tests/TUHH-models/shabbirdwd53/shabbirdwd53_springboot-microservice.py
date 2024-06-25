from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/shabbirdwd53/Springboot-Microservice

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "shabbirdwd53_springboot-microservice"



"""
Components:
    - service discovery (Eureka), port 8761
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/service-registry/src/main/java/com/dailycodebuffer/service/registry/ServiceRegistryApplication.java
Artifact (line 8):
    @EnableEurekaServer
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/service-registry/pom.xml
Artifact (line 14):
    <name>service-registry</name>
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/service-registry/src/main/resources/application.yml
Artifact (lines [1:2]):
    server:
        port: 8761
"""

service_registry = CClass(service, "service-registry", stereotype_instances = [infrastructural, service_discovery], tagged_values = {'Port': 8761, 'Service Discovery': "Eureka"})



"""
Components:
    - configuration service (Spring Cloud config), "CONFIG_SERVER", port 9296
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-config-server/src/main/java/com/dailycodebuffer/cloud/CloudConfigServerApplication.java
Artifact (line 10):
    @EnableConfigServer
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-config-server/src/main/resources/application.yml
Artifact (lines [5:6]):
    application:
        name: CONFIG-SERVER
And artifact (lines [1:2]):
    server:
        port: 9296
"""

config_server = CClass(service, "CONFIG-SERVER", stereotype_instances = [infrastructural, configuration_server], tagged_values = {'Port': 9296, 'Configuration Server': "Spring Cloud Config"})



"""
Components:
    - external configuration repository "https://github.com/shabbirdwd53/config-server"
    - connection github repository to config-server
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-config-server/src/main/resources/application.yml
Artifact (lines [7:11]):
    cloud:
        config:
            server:
                git:
                    uri: https://github.com/shabbirdwd53/config-server
"""

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/shabbirdwd53/config-server"})

add_links({github_repository: config_server}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Components:
    - connection config-server to service-registry
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-config-server/src/main/java/com/dailycodebuffer/cloud/CloudConfigServerApplication.java
Artifact (line 9):
    @EnableEurekaClient
"""

add_links({config_server: service_registry}, stereotype_instances = restful_http)



"""
Components:
    - tracing server (Zipkin), port 9411
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/pom.xml
Artifact (line 47):
    <artifactId>spring-cloud-starter-zipkin</artifactId>
"""

zipkin_server = CClass(service, "zipkin-server", stereotype_instances = [infrastructural, tracing_server], tagged_values = {'Port': 9411, 'Tracing Server': "Zipkin"})



"""
Components:
    - internal service "DEPARTMENT-SERVICE", port 9001
    - local logging
    - endpoints ["/departments", "/departments/{id}"]
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/java/com/dailycodebuffer/department/DepartmentServiceApplication.java
Artifact (line 7):
    @SpringBootApplication
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/resources/application.yml
Artifact(lines [5:6]):
    application:
        name: DEPARTMENT-SERVICE
And artifact (lines [1:2]):
    server:
        port: 9001

Local logging:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/java/com/dailycodebuffer/department/controller/DepartmentController.java
Artifact (lines [11;19])
    @Slf4j

        log.info("Inside saveDepartment method of DepartmentController");

Endpoints:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/java/com/dailycodebuffer/department/controller/DepartmentController.java
Artifact (line 10):
    @RequestMapping("/departments")
And artifact (line 23):
    @GetMapping("/{id}")
"""

department_service = CClass(service, "DEPARTMENT-SERVICE", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 9001, 'Endpoints': "[\'/departments\', \'/departments/{id}\']", 'Logging Technology': "Lombok"})



"""
Components:
    - connection DEPARTMENT-SERVICE to zipkin-server
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/resources/application.yml
Artifact (lines [7:8]):
    zipkin:
        base-url: http://127.0.0.1:9411/
"""

add_links({department_service: zipkin_server}, stereotype_instances = restful_http)



"""
Components:
    - connection CONFIG-SERVER to DEPARTMENT-SERVICE
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/resources/bootstrap.yml
Artifact (lines [1:5]):
    spring:
        cloud:
            config:
                enabled: true
                uri: http://localhost:9296
"""

add_links({config_server: department_service}, stereotype_instances = restful_http)



"""
Components:
    - connection DEPARTMENT-SERVICE to service-registry
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/department-service/src/main/java/com/dailycodebuffer/department/DepartmentServiceApplication.java
Artifact (line 8):
    @EnableEurekaClient
"""

add_links({department_service: service_registry}, stereotype_instances = restful_http)



"""
Components:
    - internal service "USER-SRVICE", port 9002
    - local logging
    - load balancer
    - endpoints ["/users", "/users/{id}"]
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/UserServiceApplication.java
Artifact (line 10):
    @SpringBootApplication
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/resources/application.yml
Artifact (lines [5:6]):
    application:
        name: USER-SERVICE
And artifact (lines [1:2]):
    server:
        port: 9002

Local logging:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/controller/UserController.java
Artifact (lines [13;21]):
    @Slf4j

        log.info("Inside saveUser of UserController");

Load balancer:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/UserServiceApplication.java
Artifact (line 19):
    @LoadBalanced

Endpoints:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/controller/UserController.java
Artifact (line 12):
    @RequestMapping("/users")
And artifact (line 25):
    @GetMapping("/{id}")
"""

user_service = CClass(service, "USER-SERVICE", stereotype_instances = [internal, local_logging, load_balancer], tagged_values = {'Port': 9002, 'Endpoints': "[\'/users\', \'/users/{id}\']", 'Load Balancer': "Spring Cloud", 'Logging Technology': "Lombok"})



"""
Components:
    - connection USER-SERVICE to zipkin_server
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/resources/application.yml
Artifact (lines [7:8]):
    zipkin:
        base-url: http://127.0.0.1:9411/
"""

add_links({user_service: zipkin_server}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - connection CONFIG-SERVER to USER-SERVICE
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/resources/bootstrap.yml
Artifact (lines [1:5]):
    spring:
        cloud:
            config:
                enabled: true
                uri: http://localhost:9296
"""

add_links({config_server: user_service}, stereotype_instances = restful_http)



"""
Components:
    - connection USER-SERVICE to service-registry
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/UserServiceApplication.java
Artifact (line 11):
    @EnableEurekaClient
"""

add_links({user_service: service_registry}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - connection USER-SERVICE to DEPARTMENT-SERVICE
    - load balanced
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/user-service/src/main/java/com/dailycodebuffer/user/service/UserService.java
Artifact (line 33):
    restTemplate.getForObject("http://DEPARTMENT-SERVICE/departments/" + user.getDepartmentId()
"""

add_links({user_service: department_service}, stereotype_instances = [restful_http, load_balanced_link])



"""
Components:
    - monitoring dashboard (Hystrix), "HYSTRIX-DASHBOARD", port 9295
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/hystrix-dashboard/src/main/java/com/dailycodebuffer/hystrix/dashboard/HystrixDashboardApplication.java
Artifact (line 9):
    @EnableHystrixDashboard
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/hystrix-dashboard/src/main/resources/application.yml
Artifact (lines [5:6]):
    application:
        name: HYSTRIX-DASHBOARD
And artifact (lines [1:2]):
    server:
        port: 9295
"""

hystrix_dashboard = CClass(service, "HYSTRIX-DASHBOARD", stereotype_instances = [infrastructural, monitoring_dashboard], tagged_values = {'Port': 9295, 'Monitoring Dashboard': "Hystrix"})



"""
Components:
    - connection CONFIG-SERVER to HYSTRIX-DASHBOARD
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/hystrix-dashboard/src/main/resources/bootstrap.yml
Artifact (lines [1:5]):
    spring:
        cloud:
            config:
                enabled: true
                uri: http://localhost:9296
"""

add_links({config_server: hystrix_dashboard}, stereotype_instances = restful_http)



"""
Components:
    - connection HYSTRIX-DASHBOARD to service-registry
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/hystrix-dashboard/src/main/java/com/dailycodebuffer/hystrix/dashboard/HystrixDashboardApplication.java
Artifact (line 10):
    @EnableEurekaClient
"""

add_links({hystrix_dashboard: service_registry}, stereotype_instances = restful_http)



"""
Components:
    - api gateway (Spring Cloud Gateway), "API-GATEWAY", port 9191
    - circuit breaker (Hystrix)
    - endpoints ["/userServiceFallBack", "/departmentServiceFallBack"]
    - user (implicit)
    - connections between user and api-gateway (implicit)
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/java/com/dailycodebuffer/cloud/gateway/CloudGatewayApplication.java
Artifact (line 8):
    @SpringBootApplication
And file: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/resources/application.yml
Artifact (lines [5:6]):
    application:
        name: API-GATEWAY
And artifact (lines [1:2]):
    server:
        port: 9191

Circuit breaker:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/java/com/dailycodebuffer/cloud/gateway/CloudGatewayApplication.java
Artifact (line 10):
    @EnableHystrix

Endpoints:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/java/com/dailycodebuffer/cloud/gateway/FallBackMethodController.java
Artifact (line 9):
    @GetMapping("/userServiceFallBack")
And artifact (line 15):
    @GetMapping("/departmentServiceFallBack")

Load Balancer:
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/resources/application.yml
Artifact (lines [11;20]):
    uri: lb://USER-SERVICE

    uri: lb://DEPARTMENT-SERVICE
"""

api_gateway = CClass(service, "API-GATEWAY", stereotype_instances = [infrastructural, gateway, circuit_breaker, load_balancer], tagged_values = {'Port': 9191, 'Gateway': "Spring Cloud Gateway", 'Circuit Breaker': "Hystrix", 'Endpoints': "[\'/userServiceFallBack\', \'/departmentServiceFallBack\']"})

user = CClass(external_component, "user", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({api_gateway: user}, stereotype_instances = restful_http)

add_links({user: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection service-registry to API-GATEWAY
File: inkedin.com/
Artifact (line 9):
    @EnableEurekaClient
"""

add_links({service_registry: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection CONFIG-SERVER to API-GATEWAY
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/resources/bootstrap.yml
Artifact (lines [1:5]):
    spring:
        cloud:
            config:
                enabled: true
                uri: http://localhost:9296
"""

add_links({config_server: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - connection API-GATEWAY to USER-SERVICE
    - connection API-GATEWAY to DEPARTMENT-SERVICE
File: https://github.com/shabbirdwd53/Springboot-Microservice/blob/main/cloud-gateway/src/main/resources/application.yml
Artifact (lines [7:11]):
    cloud:
        gateway:
            routes:
                - id: USER-SERVICE
                  uri: lb://USER-SERVICE
And artifact (lines [19:20]):
    - id: DEPARTMENT-SERVICE
      uri: lb://DEPARTMENT-SERVICE
"""

add_links({api_gateway: user_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link])

add_links({api_gateway: department_service}, stereotype_instances = [restful_http, circuit_breaker_link, load_balanced_link])



"""
Components:
    - connection zipkin-server to HYSTRIX-DASHBOARD
Implicit when both are used
"""

add_links({zipkin_server: hystrix_dashboard}, stereotype_instances = restful_http)



##### Create model
model = CBundle(model_name, elements = service_registry.class_object.get_connected_elements())

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
