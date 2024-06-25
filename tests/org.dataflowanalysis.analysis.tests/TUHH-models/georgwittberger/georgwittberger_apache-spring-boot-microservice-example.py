from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/georgwittberger/apache-spring-boot-microservice-example

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "georgwittberger_apache-spring-boot-microservice-example"


"""
Components:
    - Web Server (Apache httpd)
    - user and connection to apache-configuration (implicit for web server)
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/README.adoc
Artifact:
"Copy the file apache-configuration/httpd-microservice-example.conf from this project to your Apache conf directory.
[...]
Start the web server or reload the configuration if it is already running."
"""

apache_server = CClass(service, "apache-server", stereotype_instances = [web_server, infrastructural], tagged_values = {'Web Server': "Apache httpd"})

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: apache_server}, stereotype_instances = restful_http)
add_links({apache_server: user}, stereotype_instances = restful_http)



"""
Component: content-service (internal), port 11080
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/content-service/src/main/java/io/github/georgwittberger/microserviceexample/contentservice/Application.java
Artifact (line 6):
    @SpringBootApplication
And file: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/content-service/src/main/resources/application.yml
Artifact (lines[1:2]):
server:
  port: 11080
"""

content_service = CClass(service, "content-service", stereotype_instances = internal, tagged_values = {'Port': 11080})



"""
Component: product-service (internal), port 11081
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/product-service/src/main/java/io/github/georgwittberger/microserviceexample/productservice/Application.java
Artifact (line 6):
    @SpringBootApplication
And file: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/product-service/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
      port: 11081
"""

product_service = CClass(service, "product-service", stereotype_instances = internal, tagged_values = {'Port': 11081})



"""
Component: cart-service (internal), port 11082
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/cart-service/src/main/java/io/github/georgwittberger/microserviceexample/cartservice/Application.java
Artifact (line 8):
    @SpringBootApplication
And file: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/cart-service/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
      port: 11082
"""

cart_service = CClass(service, "cart-service", stereotype_instances = internal, tagged_values = {'Port': 11082})



"""
Component: connection cart-service to product-service
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/cart-service/src/main/java/io/github/georgwittberger/microserviceexample/cartservice/cart/CartService.java
Artifact (line 26):
        Product product = restTemplate.getForObject(productServiceURL + productSeoName, Product.class);
And artifact (lines[17:18]):
    @Value("${service.product.url}")
    private String productServiceURL;
And file: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/cart-service/src/main/resources/application.yml
Artifact (lines[9:11]):
    service:
      product:
        url: http://localhost:11081
"""

add_links({cart_service: product_service}, stereotype_instances = restful_http)



"""
Components:
    - connection web server to product-service
    - connection web server to cart-service
    - connection web server to content-service
File: https://github.com/georgwittberger/apache-spring-boot-microservice-example/blob/master/apache-configuration/httpd-microservice-example.conf
Artifact lines([26:36]):
    # Pass requests to the product service.
    ProxyPass "${PRODUCT_SERVICE_ROUTE}" "http://localhost:11081"
    ProxyPassReverse "${PRODUCT_SERVICE_ROUTE}" "http://localhost:11081"

    # Pass requests to the cart service.
    ProxyPass "${CART_SERVICE_ROUTE}" "http://localhost:11082"
    ProxyPassReverse "${CART_SERVICE_ROUTE}" "http://localhost:11082"

    # Pass any other requests to the content service (must be last).
    ProxyPass "/" "http://localhost:11080/"
    ProxyPassReverse "/" "http://localhost:11080/"
"""

add_links({apache_server: cart_service}, stereotype_instances = restful_http)

add_links({apache_server: product_service}, stereotype_instances = restful_http)

add_links({apache_server: content_service}, stereotype_instances = restful_http)





##### Create model
model = CBundle(model_name, elements = apache_server.class_object.get_connected_elements())


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
