from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/jferrater/Tap-And-Eat-MicroServices

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "jferrater_tap-and-eat-microservices"



"""
Component:
    - Service Discovery (Eureka), "discovery-service", port 8888
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/DiscoveryService/src/main/java/com/github/joffry/ferrater/discoveryservice/DiscoveryServiceApplication.java
Artifact (line 13):
    @EnableEurekaServer
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/DiscoveryService/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: discovery-service
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/DiscoveryService/src/main/resources/application.yml
Artifact (lines [2:3]):
    server:
        port:  8888
"""

discovery_service = CClass(service, "discovery-service", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8888})



"""
Component:
    - Configuration Server (Spring Cloud Config), "config-service", port 8888
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ConfigService/src/main/java/com/github/joffry/ferrater/configservice/ConfigServiceApplication.java
Artifact (line 14):
    @EnableConfigServer
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ConfigService/src/main/resources/bootstrap.yml
Artifact (lines [2:3]):
    application:
        name: config-service
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ConfigService/src/main/docker/Dockerfile
Artifact (line 5):
    EXPOSE 8888
"""

config_service = CClass(service, "config-service", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "Spring Cloud Config", 'Port': 8888})



"""
Component:
    - external configuration repository "https://github.com/jferrater/ConfigData"
    - connection config-service to external configuration repository
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ConfigService/src/main/resources/application.yml
Artifact (lines [7:12]):
    spring:
        cloud:
            config:
                server:
                    git:
                        uri: https://github.com/jferrater/ConfigData
"""

github_repository = CClass(external_component, "github-repository", stereotype_instances = [github_repository, entrypoint], tagged_values = {'URL': "https://github.com/jferrater/ConfigData"})

add_links({github_repository: config_service}, stereotype_instances = restful_http, tagged_values = {'Protocol': "HTTPS"})



"""
Component:
    - connection config-service to eureka
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ConfigService/src/main/java/com/github/joffry/ferrater/configservice/ConfigServiceApplication.java
Artifact (line 15):
    @EnableDiscoveryClient
"""

add_links({config_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "account-service", port 8000
    - endpoints ["/accounts"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/AccountService/src/main/java/com/github/joffryferrater/accountservice/AccountServiceApplication.java
Artifact (line 19):
    @SpringBootApplication
And file:
Artifact (lines [6:7]):
    application:
        name: account-service
And artifact (lines [9:10]):
    server:
        port: 8000

Endpoints:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/AccountService/src/main/java/com/github/joffryferrater/accountservice/models/AccountRepository.java
Artifact (line 7):
    @RepositoryRestResource(path="accounts", collectionResourceRel="accounts")
"""

account_service = CClass(service, "account-service", stereotype_instances = [internal], tagged_values = {'Port': 8000, 'Endpoints': "[\'/accounts\']"})



"""
Component:
    - connection  to config-service to account-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/AccountService/src/main/resources/bootstrap.yml
Artifact (lines[2:5]):
    spring:
        cloud:
            config:
                uri: http://localhost:8888
"""

add_links({config_service: account_service}, stereotype_instances = restful_http)



"""
Component:
    - connection account-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/AccountService/src/main/java/com/github/joffryferrater/accountservice/AccountServiceApplication.java
Artifact (line 20):
    @EnableDiscoveryClient
"""

add_links({account_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "customer-service", port 8002
    - endpoint ["/customers"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/CustomerService/src/main/java/com/github/joffryferrater/customerservice/CustomerServiceApplication.java
Artifact (line 12):
    @SpringBootApplication
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/CustomerService/src/main/resources/bootstrap.yml
Artifact (lines [4:5]):
    application:
        name: customer-service
And artifact (lines [10:11]):
    server:
        port: 8002

Endpoints:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/CustomerService/src/main/java/com/github/joffryferrater/customerservice/models/CustomerRepository.java
Artifact (line 14):
    @RepositoryRestResource(path="customers", collectionResourceRel="customers")
"""

customer_service = CClass(service, "customer-service", stereotype_instances = [internal], tagged_values = {'Port': 8002, 'Endpoints': "[\'/customers\']"})



"""
Component:
    - connection config-service to customer-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/CustomerService/src/main/resources/bootstrap.yml
Artifact (lines [6:8]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({config_service: customer_service}, stereotype_instances = restful_http)



"""
Component:
    - connection customer-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/CustomerService/src/main/java/com/github/joffryferrater/customerservice/CustomerServiceApplication.java
Artifact (line 13):
    @EnableDiscoveryClient
"""

add_links({customer_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "store-service", port 8003
    - endpoints ["/stores"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/StoreService/src/main/java/com/github/joffryferrater/storeservice/StoreServiceApplication.java
Artifact (line 12):
    @SpringBootApplication
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/StoreService/src/main/resources/bootstrap.yml
Artifact (lines [2:4]):
    application:
        name: store-service
And artifact (lines [8:9]):
    server:
        port: 8003

Endpoints:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/StoreService/src/main/java/com/github/joffryferrater/storeservice/models/StoreRepository.java
Artifact (line 14):
    @RepositoryRestResource(path="stores", collectionResourceRel="stores")
"""

store_service = CClass(service, "store-service", stereotype_instances = [internal], tagged_values = {'Port': 8003, 'Endpoints': "[\'/stores\']"})



"""
Component:
    - connection config-service to store-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/StoreService/src/main/resources/bootstrap.yml
Artifact (lines [5:7]):
    cloud:
        config:
            uri: http://localhost:8888
"""

add_links({config_service: store_service}, stereotype_instances = restful_http)



"""
Component:
    - connection store-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/StoreService/src/main/java/com/github/joffryferrater/storeservice/StoreServiceApplication.java
Artifact (line 13):
    @EnableDiscoveryClient
"""

add_links({store_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "item-service", port 8004
    - endpoint ["/items"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ItemService/src/main/java/com/github/joffryferrater/itemservice/ItemServiceApplication.java
Artifact (line 21):
    @SpringBootApplication
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ItemService/src/main/resources/bootstrap.yml
Artifact (lines [7:8]):
    application:
        name: item-service
And artifact (lines [10:11]):
    server:
        port: 8004

Endpoint:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ItemService/src/main/java/com/github/joffryferrater/itemservice/models/ItemRepository.java
Artifact (line 15):
    @RepositoryRestResource(path="items", collectionResourceRel="items")
"""

item_service = CClass(service, "item-service", stereotype_instances = [internal], tagged_values = {'Port': 8004, 'Endpoints': "[\'/items\']"})



"""
Component:
    - connection config-service to item-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ItemService/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
        cloud:
            config:
                uri: http://localhost:8888
"""

add_links({config_service: item_service}, stereotype_instances = restful_http)



"""
Component:
    - connection item-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/ItemService/src/main/java/com/github/joffryferrater/itemservice/ItemServiceApplication.java
Artifact (line 22):
    @EnableDiscoveryClient
"""

add_links({item_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "price-service", port 8001
    - endpoint ["/items"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/PriceService/src/main/java/com/github/joffryferrater/priceservice/PriceServiceApplication.java
Artifact (line 13):
    @SpringBootApplication
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/PriceService/src/main/resources/bootstrap.yml
Artifact (lines [5:6]):
    application:
        name: price-service
And artifact (lines [7:8]):
    server:
        port: 8001

Endpoint:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/PriceService/src/main/java/com/github/joffryferrater/priceservice/models/PriceRepository.java
Artifact (line 15):
    @RepositoryRestResource(path="items", collectionResourceRel="items")
"""

price_service = CClass(service, "price-service", stereotype_instances = [internal], tagged_values = {'Port': 8001, 'Endpoints': "[\'/prices\']"})



"""
Component:
    - connection config-service to price-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/PriceService/src/main/resources/bootstrap.yml
Artifact (lines [1:4]):
    spring:
        cloud:
            config:
                uri: http://localhost:8888
"""

add_links({config_service: price_service}, stereotype_instances = restful_http)



"""
Component:
    - connection price-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/PriceService/src/main/java/com/github/joffryferrater/priceservice/PriceServiceApplication.java
Artifact (line 14):
    @EnableDiscoveryClient
"""

add_links({price_service: discovery_service}, stereotype_instances = restful_http)



"""
Component:
    - monitoring dashboard (Hystrix), "foodtray-service", port 8005
    - circuit breaker for all connections (Hystrix)
    - endpoints ["/foodtrays", "/foodtrays/{itemCode}", "/foodtrays/price/{itemCode}", "/foodtrays/item/{itemCode}"]
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/FoodTrayServiceApplication.java
Artifact (line 15):
    @SpringBootApplication
And file: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/resources/bootstrap.yml
Artifact (lines [7:8]):
    application:
        name: foodtray-service
And artifact (lines [18:19]):
    server:
        port: 8005

Monitoring dashboard:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/FoodTrayServiceApplication.java
Artifact (line 18):
    @EnableHystrixDashboard

Circuit breaker:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/FoodTrayServiceApplication.java
Artifact (line 17):
    @EnableCircuitBreaker

Endpoints:
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/FoodTrayController.java
Artifact (lines [24:25]):
    @RestController
    @RequestMapping("/foodtrays")
And artifact (line 35):
    @RequestMapping(value="/price/{itemCode}", method=RequestMethod.GET)
And artifact (line 40):
    @RequestMapping(value="/item/{itemCode}", method=RequestMethod.GET)
And artifact (line 45):
    @RequestMapping(value="/{itemCode}", method=RequestMethod.GET)
And artifact (line 53):
    @RequestMapping(value={"/", ""}, method=RequestMethod.POST)
And artifact (line 59):
    @RequestMapping(value={"/", ""}, method=RequestMethod.GET)
"""

foodtray_service = CClass(service, "foodtray-service", stereotype_instances = [infrastructural, monitoring_dashboard, circuit_breaker], tagged_values = {'Monitoring Dashboard': "Hystrix", 'Circuit Breaker': "Hystrix", 'Port': 8005, 'Endpoints': "[\'/foodtrays\', \'/foodtrays/{itemCode}\', \'/foodtrays/price/{itemCode}\', \'/foodtrays/item/{itemCode}\']"})



"""
Component:
    - connection config-service to foodtray-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/resources/bootstrap.yml
Artifact (lines [2:5]):
    spring:
        cloud:
            config:
                uri: http://localhost:8888
"""

add_links({config_service: foodtray_service}, stereotype_instances = [restful_http, circuit_breaker_link], tagged_values = {'Circuit Breaker': "Hystrix"})



"""
Component:
    - connection foodtray-service to discovery-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/FoodTrayServiceApplication.java
Artifact (line 16):
    @EnableDiscoveryClient
"""

add_links({foodtray_service: discovery_service}, stereotype_instances = [restful_http, circuit_breaker_link], tagged_values = {'Circuit Breaker': "Hystrix"})



"""
Component:
    - connection foodtray-service to item-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/repository/ItemServiceRepository.java
Artifact (line 15):
    @FeignClient(value="ITEM-SERVICE", fallback=ItemServiceFallback.class)
"""

add_links({foodtray_service: item_service}, stereotype_instances = [restful_http, feign_connection, load_balanced_link, circuit_breaker_link], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})



"""
Component:
    - connection foodtray-service to price-service
File: https://github.com/jferrater/Tap-And-Eat-MicroServices/blob/master/FoodTrayService/src/main/java/com/github/joffryferrater/foodtrayservice/repository/PriceServiceRepository.java
Artifact (line 15):
    @FeignClient(value="PRICE-SERVICE", fallback=PriceServiceRepository.class)
"""

add_links({foodtray_service: price_service}, stereotype_instances = [restful_http, feign_connection, load_balanced_link, circuit_breaker_link], tagged_values = {'Load Balancer': "Ribbon", 'Circuit Breaker': "Hystrix"})



##### Create model
model = CBundle(model_name, elements = discovery_service.class_object.get_connected_elements())

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
