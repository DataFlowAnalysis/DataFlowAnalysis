from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/yidongnan/spring-cloud-netflix-example

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "yidongnan_spring-cloud-netflix-example"



"""
Component:
    - Admin server (Spring Boot), "admin-dashboard", port 8040
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/admin-dashboard/src/main/java/net/devh/AdminDashboardApplication.java
Artifact (line 19):
    @EnableAdminServer
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/admin-dashboard/src/main/resources/bootstrap.yml
Artifact (lines[1:2]):
    server:
        port: 8040

CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/admin-dashboard/src/main/java/net/devh/AdminDashboardApplication.java
Artifact (line 27):
    .and().csrf().disable();
"""

admin_dashboard = CClass(service, "admin-dashboard", stereotype_instances = [administration_server, infrastructural, csrf_disabled], tagged_values = {'Administration Server': "Spring Boot Admin", 'Port': 8040})



"""
Components:
    - Service Discovery (Eureka), "eureka-server", port 8761
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/eureka-server/src/main/java/net/devh/EurekaServeApplication.java
Artifact (line 10):
    @EnableEurekaServer
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/eureka-server/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: eureka-server
And artifact (lines [1:2]):
    server:
        port: 8761

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/eureka-server/src/main/java/net/devh/EurekaServeApplication.java
Artifact (lines [18:20]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();
"""

eureka_server = CClass(service, "eureka-server", stereotype_instances = [service_discovery, infrastructural, csrf_disabled], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component:
    - Message Broker (RabbitMQ), "rabbitmq", port 4369
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [1:4]):
    rabbitmq:
        image: rabbitmq:3-management
        ports:
            - "4369:4369"
"""

rabbitmq = CClass(service, "rabbitmq", stereotype_instances = [message_broker, infrastructural], tagged_values = {'Message Broker': "RabbitMQ", 'Port': 4369})



"""
Component:
    - config-server (Spring Cloud Config), "config-server", Port 8100
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/java/net/devh/ConfigServeApplication.java
Artifact (line 18):
    @EnableConfigServer
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/resources/bootstrap.yml
Artifact (lines [7:8]):
    application:
        name: config-server
And artifact (lines [1:2]):
    server:
      port: 8100

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/java/net/devh/ConfigServeApplication.java
Artifact (lines [26:28]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();
"""

config_server = CClass(service, "config-server", stereotype_instances = [configuration_server, infrastructural, csrf_disabled], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 8100})



"""
Component:
    - connection admin-dashboard to config-server
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/resources/bootstrap.yml
Artifact (lines [25:29]):
    management:
        endpoints:
            web:
                exposure:
                    include: "*"
"""

add_links({admin_dashboard: config_server}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to eureka-server
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/java/net/devh/ConfigServeApplication.java
Artifact (line 17):
    @EnableEurekaClient
"""

add_links({config_server: eureka_server}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to rabbitmq
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/3b86bf0e20a7c7da8f4e3e7e2cb15bf4cd407743/config-server/src/main/resources/bootstrap.yml
Artifact (lines [9:10]):
    rabbitmq:
        host: localhost
"""

add_links({config_server: rabbitmq}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to eureka
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/config-server/src/main/java/net/devh/ConfigServeApplication.java
Artifact (line 17):
    @EnableEurekaClient
"""

add_links({admin_dashboard: eureka_server}, stereotype_instances = restful_http)



"""
Components:
    - Monitoring Dashboard (Hystrix), "hystrix-dashboard", port 8050
    - Monitoring server (Turbine), on the same service
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/hystrix-dashboard/src/main/java/net/devh/HystrixDashboardApplication.java
Artifact (line 18):
    @EnableHystrixDashboard
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/hystrix-dashboard/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: hystrix-dashboard
And artifact (lines [1:2]):
    server:
        port: 8050

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/hystrix-dashboard/src/main/java/net/devh/HystrixDashboardApplication.java
Artifact (lines [28:29]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();
"""

hystrix_dashboard = CClass(service, "hystrix-dashboard", stereotype_instances = [monitoring_dashboard, monitoring_server, infrastructural, csrf_disabled], tagged_values = {'Monitoring Dashboard': "Hystrix", 'Monitoring Server': "Turbine", 'Port': 8050})



"""
Component:
    - connection admin-dashboard to hystrix-dashboard
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/hystrix-dashboard/src/main/resources/bootstrap.yml
Artifact (lines [19:23]):
    management:
        endpoints:
            web:
                exposure:
                    include: "*"
"""

add_links({admin_dashboard: hystrix_dashboard}, stereotype_instances = restful_http)



"""
Component:
    - connection hystrix dashboard to eureka-server
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/hystrix-dashboard/src/main/java/net/devh/HystrixDashboardApplication.java
Artifact (line 20):
    @EnableEurekaClient
"""

add_links({hystrix_dashboard: eureka_server}, stereotype_instances = restful_http)



"""
Components:
    - internal service "service-a", port 8080
    - endpoints
    - circuit breaker (Hystrix)
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (line 32):
    @SpringBootApplication
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/resources/bootstrap.yml
Artfiact (lines [5:6]):
    application:
        name: service-a
And artifact (lines [1:2]):
    server:
        port: 8080

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (lines [52:52]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();

Endpoints:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/controller/AServiceController.java
Artifact (line 35):
    @RequestMapping(value = "/", method = RequestMethod.GET)

Circuit Breaker:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (line 30):
    @EnableHystrix
"""

service_a = CClass(service, "service-a", stereotype_instances = [internal, circuit_breaker, csrf_disabled], tagged_values = {'Port': 8080, 'Endpoints': "[\'/\']", 'Circuit Breaker': "Hystrix"})



"""
Component:
    - connection admin-dashboard to service-a
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/resources/bootstrap.yml
Artifact (lines [26:30]):
    management:
        endpoints:
            web:
                exposure:
                    include: "*"
"""

add_links({admin_dashboard: service_a}, stereotype_instances = restful_http)



"""
Component:
    - connection service-a to rabbitmq
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/resources/bootstrap.yml
Artifact (lines [18:19]):
    rabbitmq:
        host: localhost
"""

add_links({service_a: rabbitmq}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to service-a
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/resources/bootstrap.yml
Artifact (lines [7:11]):
    cloud:
        config:
            discovery:
                enabled: true
                service-id: config-server
"""

add_links({config_server: service_a}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to service-a
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (line 29):
    @EnableEurekaClient
"""

add_links({service_a: eureka_server}, stereotype_instances = restful_http)



"""
Component:
    - connection service-a to hystrix
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (line 30):
    @EnableHystrix
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/hystrix/HystrixWrappedServiceBClient.java
Artifact (line 22):
    @HystrixCommand(groupKey = "helloGroup", fallbackMethod = "fallBackCall")
"""

add_links({service_a: hystrix_dashboard}, stereotype_instances = restful_http)



"""
Components:
    - internal service "service-b", port 8070
    - endpoints
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/java/net/devh/B1ServiceApplication.java
Artifact (line 28):
    @SpringBootApplication
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/resources/bootstrap.yml
Artfiact (lines [5:6]):
    application:
        name: service-b
And artifact (lines [1:2]):
    server:
        port: 8070

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/java/net/devh/B1ServiceApplication.java
Artifact (lines [49:51]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();

Endpoints:
    File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/java/net/devh/ServiceB1Controller.java
Artifact (line 31):
    @RequestMapping(value = "/", method = RequestMethod.GET)
"""

service_b = CClass(service, "service-b", stereotype_instances = [internal, csrf_disabled], tagged_values = {'Port': 8070, 'Endpoints': "[\'/\']"})



"""
Component:
    - connection admin-dashboard to service-b
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/resources/bootstrap.yml
Artifact (lines [35:39]):
    management:
        endpoints:
            web:
                exposure:
                    include: "*"
"""

add_links({admin_dashboard: service_b}, stereotype_instances = restful_http)



"""
Component:
    - connection service-b to rabbitmq
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/resources/bootstrap.yml
Artifact (lines [18:19]):
    rabbitmq:
        host: localhost
"""

add_links({service_b: rabbitmq}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to service-b
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/resources/bootstrap.yml
Artifact (lines [10:14]):
    cloud:
        config:
            discovery:
                enabled: true
                service-id: config-server
"""

add_links({config_server: service_b}, stereotype_instances = restful_http)



"""
Component:
    - connection config-server to service-b
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-b/src/main/java/net/devh/B1ServiceApplication.java
Artifact (line 27):
    @EnableEurekaClient
"""

add_links({service_b: eureka_server}, stereotype_instances = restful_http)



"""
Component:
    - connection service-a to service-b
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/A1ServiceApplication.java
Artifact (line 31):
    @EnableFeignClients
And file: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/src/main/java/net/devh/feign/ServiceBClient.java
Artifact (line 12):
    @FeignClient(name = "service-b")
"""

add_links({service_a: service_b}, stereotype_instances = [restful_http, feign_connection])



"""
Components:
    - Gateway (Zuul), "zuul", port 8060
    - load balancer (Zuul)
    - user
    - connections between user and gateway
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/zuul/src/main/java/net/devh/ZuulApplication.java
Atifact (line 17):
    @EnableZuulProxy
And file:
Artifact (lines [5:6]):
    application:
        name: zuul
And artifact (lines [1:2]):
    server:
        port: 8060

HTTPS / CSRF:
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/zuul/src/main/java/net/devh/ZuulApplication.java
Artifact (lines [24:26]):
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable();
"""

zuul = CClass(service, "zuul", stereotype_instances = [gateway, infrastructural, load_balancer, csrf_disabled], tagged_values = {'Gateway': "Zuul", 'Port': 8060, 'Load Balancer': "Ribbon"})

user = CClass(external_component, "user", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({zuul: user}, stereotype_instances = restful_http)

add_links({user: zuul}, stereotype_instances = restful_http)



"""
Component:
    - connection zuul to service-a
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/zuul/src/main/resources/bootstrap.yml
Artifact (lines [36:38]):
    zuul:
        routes:
            a-service:
"""

add_links({zuul: service_a}, stereotype_instances = restful_http)



"""
Component:
    - connection zuul to eureka-server
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/zuul/src/main/java/net/devh/ZuulApplication.java
Artifact (line 16):
    @EnableEurekaClient
"""

add_links({eureka_server: zuul}, stereotype_instances = restful_http)



"""
Component:
    - Tracing Server (Zipkin), "zipkin", port 9411
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [10:13]):
    zipkin:
        image: openzipkin/zipkin
        ports:
            - "9411:9411"
"""

zipkin = CClass(service, "zipkin", stereotype_instances = [tracing_server, infrastructural], tagged_values = {'Tracing Server': "Zipkin", 'Port': 9411})



"""
Component:
    - connection rabbitmq to zipkin
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/service-a/build.gradle
Artifact (line 12):
    compile('org.springframework.cloud:spring-cloud-starter-bus-amqp')
"""

add_links({rabbitmq: zipkin}, stereotype_instances = restful_http)



"""
Component:
    - connection service-b to zipkin
    - connection service-b to hystrix-dashoard
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [29;31;34;36]):
    service-b:

      links:

        - "hystrix-dashboard"

        - "zipkin"
"""

add_links({service_b: zipkin}, stereotype_instances = restful_http)

add_links({service_b: hystrix_dashboard}, stereotype_instances = restful_http)



"""
Component:
    - connection service-a to zipkin
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [38;40;46]):
    service-a:

      links:

        - "zipkin"
"""

add_links({service_a: zipkin}, stereotype_instances = restful_http)



"""
Component:
    - connection admin-dashboard to rabbitmq
    - connection admin-dashboard to zuul
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [48;52;54;58]):
    admin-dashboard:

      links:

        - "rabbitmq"

        - "zuul"
"""

add_links({admin_dashboard: rabbitmq}, stereotype_instances = restful_http)

add_links({admin_dashboard: zuul}, stereotype_instances = restful_http)



"""
Component:
    - connection zuul to rabbitmq
    - connection zuul to zipkin
    - connection zuul to config-server
File: https://github.com/yidongnan/spring-cloud-netflix-example/blob/master/docker-compose.yml
Artifact (lines [61;65;67;69:70]):
    zuul:

      links:

        - "config-server"

        - "rabbitmq"
        - "zipkin"
"""

add_links({zuul: rabbitmq}, stereotype_instances = restful_http)

add_links({zuul: zipkin}, stereotype_instances = restful_http)

add_links({zuul: config_server}, stereotype_instances = restful_http)





##### Create model
model = CBundle(model_name, elements = zuul.class_object.get_connected_elements())

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
