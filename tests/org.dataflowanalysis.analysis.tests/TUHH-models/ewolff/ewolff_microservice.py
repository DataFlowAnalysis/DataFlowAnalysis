from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/ewolff/microservice

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "ewolff_microservice"


"""
Component:
    - Service discovery (Eureka), "eureka", port 8761
File: https://github.com/ewolff/microservice/blob/99d9d07d1b68285d2ceefd8a811f31788ee43d75/microservice-demo/microservice-demo-eureka-server/src/main/java/com/ewolff/microservice/eurekaserver/EurekaApplication.java
Artifact (lines [7:8]):
    @SpringBootApplication
    @EnableEurekaServer
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-eureka-server/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
        port: 8761
"""

eureka = CClass(service, "eureka", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Components:
    - API gateway (Zuul), "zuul", port 8080
    - connection to service discovery server (Eureka)
    - user and connection to zuul (implicit for gateway)
Gateway:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-zuul-server/src/main/java/com/ewolff/microservice/zuulserver/ZuulApplication.java
Artifact (lines [7:8]):
    @SpringBootApplication
    @EnableZuulProxy
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-zuul-server/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
        port: 8080

Connection:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-zuul-server/pom.xml
Artifact (line 21):
    		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
"""

zuul = CClass(service, "zuul", stereotype_instances = [gateway, load_balancer, infrastructural], tagged_values = {'Gateway': "Zuul", 'Load Balancer': "Ribbon", 'Port': 8080})

add_links({eureka: zuul}, stereotype_instances = restful_http)

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: zuul}, stereotype_instances = restful_http)
add_links({zuul: user}, stereotype_instances = restful_http)



"""
Components:
    - monitoring Server (Turbine), "turbine", port 8989
    - connection to eureka
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-turbine-server/src/main/java/com/ewolff/microservice/turbine/TurbineApplication.java
Artifact (lines[10:11]):
    @EnableTurbine
    @EnableEurekaClient
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-turbine-server/src/main/resources/application.yml
Artfiact (lines[1:2]):
    server:
        port: 8989
"""

turbine = CClass(service, "turbine", stereotype_instances = [monitoring_server, monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Server': "Turbine", 'Monitoring Dashboard': "Hystrix", 'Port': 8989})

add_links({turbine: eureka}, stereotype_instances = restful_http)



"""
Components:
    - catalog service (internal), "catalog", port 8080
    - connection to eureka
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-catalog/src/main/java/com/ewolff/microservice/catalog/CatalogApp.java
Artifact (lines[14:15]):
    @EnableDiscoveryClient
    @Component
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-catalog/src/main/resources/application.properties
Artifact (lines [1;5]):
    server.port=8080
    spring.application.name=catalog

Endpoints:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-catalog/src/main/java/com/ewolff/microservice/catalog/ItemRepository.java
Artifact (line 9):
    @RepositoryRestResource(collectionResourceRel = "catalog", path = "catalog")
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-catalog/src/main/java/com/ewolff/microservice/catalog/web/CatalogController.java
Artifact (line 25):
    @RequestMapping(value = "/{id}.html", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
And artifact (line 30):
    @RequestMapping(value = "/list.html", method = RequestMethod.GET
And artifact (line 35):
    @RequestMapping(value = "/form.html", method = RequestMethod.GET
And artifact (line 53):
    @RequestMapping(value = "/searchForm.html", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
And artifact (line 58):
    @RequestMapping(value = "/searchByName.html", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
"""

catalog = CClass(service, "catalog", stereotype_instances = internal, tagged_values = {'Port': 8080, 'Endpoints': "[\'/catalog\', \'/{id}.html\', \'/list.html\', \'/form.html\', \'/searchForm.html\', \'/searchByName.html\']"})

add_links({catalog: eureka}, stereotype_instances = restful_http)



"""
Components:
    - customer service (internal), "customer", port 8080
    - connection to eureka
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-customer/src/main/java/com/ewolff/microservice/customer/CustomerApp.java
Artifact (lines[14:15]):
    @EnableDiscoveryClient
    @Component
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-customer/src/main/resources/application.properties
Artifact (lines[1;5]):
    server.port=8080
    spring.application.name=customer
Endpoints:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-customer/src/main/java/com/ewolff/microservice/customer/CustomerRepository.java
Artifact (line 9):
    @RepositoryRestResource(collectionResourceRel = "customer", path = "customer")
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-customer/src/main/java/com/ewolff/microservice/customer/web/CustomerController.java
Artifact (line 26):
    @RequestMapping(value = "/{id}.html", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
And artifact (line 31):
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
And artifact (line 36):
    @RequestMapping(value = "/form.html", method = RequestMethod.GET)
"""

customer = CClass(service, "customer", stereotype_instances = internal, tagged_values = {'Port': 8080, 'Endpoints': "[\'/form.html\', \'/list.html\', \'/{id}.html\', \'/customer\']"})

add_links({customer: eureka}, stereotype_instances = restful_http)



"""
Components:
    - order service (internal), "order", port 8080
    - connection to eureka
    - client-side load balancer (Ribbon)
    - circuit breaker (Hystrix)
    - local logging
Service:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/OrderApp.java
Artifact (lines [9:12]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableCircuitBreaker
    @RibbonClient("order")
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/resources/application.properties
Arifact (lines[1;5]):
    server.port=8080
    spring.application.name=order

Circuit breaker:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/pom.xml
Artifact (line 27):
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>

Connection to catalog service:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/clients/CatalogClient.java
Artifact (line 112):
    	return restTemplate.getForObject(catalogURL() + itemId, Item.class);

And artifact (lines [97:107]):
    private String catalogURL() {
    	String url;
    	if (useRibbon) {
    		ServiceInstance instance = loadBalancer.choose("CATALOG");
    		url = String.format("http://%s:%s/catalog/", instance.getHost(), instance.getPort());
    	} else {
    		url = String.format("http://%s:%s/catalog/", catalogServiceHost, catalogServicePort);
    	}
    	log.trace("Catalog: URL {} ", url);
    	return url;
    }

And artifact (lines[44:45]):
	@Value("${catalog.service.host:catalog}") String catalogServiceHost,
	@Value("${catalog.service.port:8080}") long catalogServicePort,

Connection to customer service:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/clients/CustomerClient.java
Artifact (lines[107:108]):
		return restTemplate.getForObject(customerURL() + customerId,
				Customer.class);

And artifact (lines[93:104]):
    private String customerURL() {
		String url;
		if (useRibbon) {
			ServiceInstance instance = loadBalancer.choose("CUSTOMER");
			url = String.format("http://%s:%s/customer/", instance.getHost(), instance.getPort());
		} else {
			url = String.format("http://%s:%s/customer/", customerServiceHost, customerServicePort);
		}
		log.trace("Customer: URL {} ", url);
		return url;

	}

And artifact (lines[43:44]):
	@Value("${customer.service.host:customer}") String customerServiceHost,
	@Value("${customer.service.port:8080}") long customerServicePort,

Connection to turbine:
File: https://github.com/ewolff/microservice/blob/99d9d07d1b68285d2ceefd8a811f31788ee43d75/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/clients/CatalogClient.java
Artifact (lines[73:74]):
    @HystrixCommand(fallbackMethod = "priceCache", commandProperties = {
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "2") })

Local logging:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/clients/CatalogClient.java
Artifact (lines[8;29;105]):
    import org.slf4j.LoggerFactory;

    	private final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    		log.trace("Catalog: URL {} ", url);

Endpoints:
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/logic/OrderRepository.java
Artifact (line 6):
    @RepositoryRestResource(collectionResourceRel = "order", path = "order")
And file: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-order/src/main/java/com/ewolff/microservice/order/logic/OrderController.java
Artifact (line 48):
    @RequestMapping(value = "/", method = RequestMethod.GET)
And artifact (line 53):
    @RequestMapping(value = "/form.html", method = RequestMethod.GET)
And artifact (line 58):
    @RequestMapping(value = "/line", method = RequestMethod.POST)
And artifact (line 64):
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
"""

order = CClass(service, "order", stereotype_instances = [internal, load_balancer, circuit_breaker, local_logging], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix", 'Port': 8080, 'Endpoints': "[\'/\', \'/form.html\', \'/{id}\', \'/line\', \'/order\']"})

add_links({order: eureka}, stereotype_instances = restful_http)

add_links({order: catalog}, stereotype_instances = restful_http)

add_links({order: customer}, stereotype_instances = restful_http)

add_links({order: turbine}, stereotype_instances = restful_http)



"""
Components:
    - connection zuul to customer
    - connection zuul to catalog
    - connection zuul to order
File: https://github.com/ewolff/microservice/blob/master/microservice-demo/microservice-demo-zuul-server/src/main/resources/static/index.html
Artifact (lines[21;27;39;66]):
    <a href="/customer/list.html">Customer</a>

    <a href="/catalog/list.html">Catalog</a>

    <a href="/order/">Order</a>
"""

add_links({zuul: customer}, stereotype_instances = restful_http)

add_links({zuul: catalog}, stereotype_instances = restful_http)

add_links({zuul: order}, stereotype_instances = restful_http)

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
