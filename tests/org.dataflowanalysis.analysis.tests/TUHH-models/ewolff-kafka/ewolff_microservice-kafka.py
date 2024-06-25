from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/ewolff/microservice-kafka

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "ewolff_microservice-kafka"



"""
Component: Config Server (Apache ZooKeeper), zookeeper
File: https://github.com/ewolff/microservice-kafka/blob/6fd9cc6aba3109f0e9c031ce4555b1a144b7a0d0/docker/docker-compose.yml
Artifact (lines[2:4]):
    services:
      zookeeper:
        image: wurstmeister/zookeeper:3.4.6
"""

zookeeper = CClass(service, "zookeeper", stereotype_instances = [configuration_server, infrastructural], tagged_values = {'Configuration Server': "ZooKeeper"})



"""
Component: Message broker (Kafka), "kafka", port 9092
File: https://github.com/ewolff/microservice-kafka/blob/6fd9cc6aba3109f0e9c031ce4555b1a144b7a0d0/docker/docker-compose.yml
Artifact (lines[5:11]):
      kafka:
        image: wurstmeister/kafka:2.12-2.5.0
        [...]
        environment:
          KAFKA_ADVERTISED_HOST_NAME: kafka
          KAFKA_ADVERTISED_PORT: 9092
"""

kafka = CClass(service, "kafka", stereotype_instances = [message_broker, infrastructural], tagged_values = {'Message Broker': "Kafka", 'Port': 9092})



"""
Component: connection from kafka to ZooKeeper
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/docker-compose.yml
Artifact (lines[5:8]):
  kafka:
    [...]
    links:
     - zookeeper
And: implicit; Kafka always needs ZooKeeper to store topics, partitions, etc.
"""

add_links({zookeeper: kafka}, stereotype_instances = restful_http)



"""
Component: order service (internal), port 8080
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/src/main/java/com/ewolff/microservice/order/OrderApp.java
Artifact (line 6):
    @SpringBootApplication
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Endpoints:
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/src/main/java/com/ewolff/microservice/order/logic/OrderRepository.java
Artifact (line 9):
    @RepositoryRestResource(collectionResourceRel = "order", path = "order")
"""

order = CClass(service, "order", stereotype_instances = internal, tagged_values = {'Port': 8080, 'Endpoints': "[\'/order\']"})



"""
Component: connection order to kafka (sending to topic "order")
File: https://github.com/ewolff/microservice-kafka/blob/6fd9cc6aba3109f0e9c031ce4555b1a144b7a0d0/microservice-kafka/microservice-kafka-order/src/main/java/com/ewolff/microservice/order/logic/OrderService.java
Artifact (line 14):
	private KafkaTemplate<String, Order> kafkaTemplate;
And artifact (line 34)
		kafkaTemplate.send("order", order.getId() + "created", order);
"""

add_links({order: kafka}, stereotype_instances = [restful_http, message_producer_kafka], tagged_values = {'Producer Topic': "order"})



"""
Components:
    - invoicing service (internal), port 8080
    - local logging
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/java/com/ewolff/microservice/invoicing/InvoiceApp.java
Artifact (line 6):
    @SpringBootApplication
And file:
Artifact (line 4):
    EXPOSE 8080

Local logging:
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/java/com/ewolff/microservice/invoicing/InvoiceService.java
Artifact (lines[4;12;25]):
    import org.slf4j.LoggerFactory;

    	private final Logger log = LoggerFactory.getLogger(InvoiceService.class);

        			log.info("Invoice id {} already exists - ignored", invoice.getId());
"""

invoicing = CClass(service, "invoicing", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8080})



"""
Component: connection invoicing to kafka (listening to topic "order")
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/java/com/ewolff/microservice/invoicing/events/OrderKafkaListener.java
Artifact (line 24):
    @KafkaListener(topics = "order")
"""

add_links({kafka: invoicing}, stereotype_instances = [restful_http, message_consumer_kafka], tagged_values = {'Consumer Topic': "order"})



"""
Components:
    - shipping service (internal), port 8080
    - local logging
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/java/com/ewolff/microservice/shipping/ShippingApp.java
Artifact (line 6):
    @SpringBootApplication
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/Dockerfile
Artifact (line 4):
    EXPOSE 8080

Local logging:
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/java/com/ewolff/microservice/shipping/ShipmentService.java
Artifact (lines[4;12;25]):
    import org.slf4j.LoggerFactory;

    	private final Logger log = LoggerFactory.getLogger(ShipmentService.class);

        			log.info("Shipment id {} already exists - ignored", shipment.
"""

shipping = CClass(service, "shipping", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8080})



"""
Component: connection kafka to shipping (listening to topic "order")
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/java/com/ewolff/microservice/shipping/events/OrderKafkaListener.java
Artifact (line 24):
    @KafkaListener(topics = "order")
"""

add_links({kafka: shipping},  stereotype_instances = [restful_http, message_consumer_kafka], tagged_values = {'Consumer Topic': "order"})



"""
Components:
    - Web server (Apache httpd), "apache", port 80
    - connection apache to order, shipping, and invoicing
    - user and connection to apache (implicit for web server)
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/apache/Dockerfile
Artifact (lines[1;9:11;18]):
    FROM ubuntu:20.04
    [...]
    RUN apt-get install -y -qq apache2 && \
        a2enmod proxy proxy_http proxy_ajp rewrite deflate headers proxy_connect proxy_html lbmethod_byrequests && \
        mkdir /var/lock/apache2 && mkdir /var/run/apache2
    [...]
    COPY 000-default.conf  /etc/apache2/sites-enabled/000-default.conf
    [...]
    EXPOSE 80
    CMD apache2ctl -D FOREGROUND

Connection apache to order:
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/apache/000-default.conf
Artifact (lines[14:15])
    ProxyPass        /order http://order:8080/
    ProxyPassReverse /order http://order:8080/
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/src/main/resources/application.properties
Artifact (lines[3:4]):
    server.port=8080
    spring.application.name=order

Connection apache to shipping:
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/apache/000-default.conf
Artifact (lines[17:18])
	ProxyPass        /shipping http://shipping:8080/
    ProxyPassReverse /shipping http://shipping:8080/
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/resources/application.properties
Artifact (lines[3:4]):
    server.port=8080
    spring.application.name=shipping

Connection apache to invoicing:
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/apache/000-default.conf
Artifact (lines[20:21])
	ProxyPass        /invoicing http://invoicing:8080/
    ProxyPassReverse /invoicing http://invoicing:8080/
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/resources/application.properties
Artifact (lines[3:4]):
    server.port=8080
    spring.application.name=invoicing
"""

apache = CClass(service, "apache", stereotype_instances = [web_server, infrastructural], tagged_values = {'Web Server': "Apache httpd", 'Port': 80})

add_links({apache: order}, stereotype_instances = restful_http)

add_links({apache: shipping}, stereotype_instances = restful_http)

add_links({apache: invoicing}, stereotype_instances = restful_http)

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: apache}, stereotype_instances = restful_http)
add_links({apache: user}, stereotype_instances = restful_http)



"""
Components:
    - Postgres database
    - plaintext credentials
File: https://github.com/ewolff/microservice-kafka/blob/master/docker/postgres/Dockerfile
Artifact (line 1):
    FROM postgres:12.2
And file: https://github.com/ewolff/microservice-kafka/blob/master/docker/docker-compose.yml
Artifact (lines[22:26]):
    postgres:
        [...]
            POSTGRES_PASSWORD: dbpass
            POSTGRES_USER: dbuser
"""

postgres = CClass(database_component, "postgres", stereotype_instances = [database, plaintext_credentials, exitpoint], tagged_values = {'Database': "PostgreSQL", 'Username': "dbuser", 'Password': "dbpass"})



"""
Component: connection order service to database via JDBC
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/src/main/resources/application.properties
Artifact (line 9):
    spring.datasource.url=jdbc:postgresql://postgres/dborder
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-order/src/main/resources/application.properties
Artifact (lines [10:11]):
    spring.datasource.username=dbuser
    spring.datasource.password=dbpass
"""

add_links({postgres: order}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "dbuser", 'Password': "dbpass"})



"""
Component: connection shipping service to database via JDBC
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/resources/application.properties
Artifact (line 11):
    spring.datasource.url=jdbc:postgresql://postgres/dbshipping
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-shipping/src/main/resources/application.properties
Artifact (lines [12:13]):
    spring.datasource.username=dbuser
    spring.datasource.password=dbpass
"""

add_links({postgres: shipping}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "dbuser", 'Password': "dbpass"})



"""
Component: connection invoicing service to database via JDBC
File: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/resources/application.properties
Artifact (line 11):
    spring.datasource.url=jdbc:postgresql://postgres/dbinvoicing
And file: https://github.com/ewolff/microservice-kafka/blob/master/microservice-kafka/microservice-kafka-invoicing/src/main/resources/application.properties
Artifact (lines [12:13]):
    spring.datasource.username=dbuser
    spring.datasource.password=dbpass
"""

add_links({postgres: invoicing}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "dbuser", 'Password': "dbpass"})



##### Create model
model = CBundle(model_name, elements = apache.class_object.get_connected_elements())


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
