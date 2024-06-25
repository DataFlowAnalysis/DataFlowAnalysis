from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Source: https://github.com/mudigal-technologies/microservices-sample

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "mudigal-technologies_microservices-sample"


"""
Components:
    - discovery service instance 1 (Consul), "consul", port 8500
    - discovery service instance 2 (Consul), "consul2", port 8500
    - discovery service instance 3 (Consul), "consul3", port 8500
    - connections between the instances
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[149:186]):
    consul:
        image: consul:1.7.3
        [...]
        command: consul agent -server -client 0.0.0.0 -ui -bootstrap-expect=3 -data-dir=/consul/data -retry-join=consul2 -retry-join=consul3 -datacenter=blr
        ports:
          - "8500:8500"
          - "8600:8600"
        networks:
          - backend
      consul2:
        image: consul:1.7.3
        [...]
        expose:
          - "8500"
          - "8600"
        command: consul agent -server -data-dir=/consul/data -retry-join=consul -retry-join=consul3 -datacenter=blr
        links:
          - consul
        networks:
          - backend
      consul3:
        image: consul:1.7.3
        [...]
        expose:
          - "8500"
          - "8600"
        command: consul agent -server -data-dir=/consul/data -retry-join=consul -retry-join=consul2 -datacenter=blr
        links:
          - consul
          - consul2
        networks:
          - backend
"""

consul = CClass(service, "consul", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Consul", 'Port': 8500})

consul2 = CClass(service, "consul2", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Consul", 'Port': 8500})

consul3 = CClass(service, "consul3", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Consul", 'Port': 8500})

add_links({consul2: consul}, stereotype_instances = restful_http)

add_links({consul3: consul}, stereotype_instances = restful_http)

add_links({consul3: consul2}, stereotype_instances = restful_http)



"""
Component: search engine (Elasticsearch), "elasticsearch", port 9200
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[225:245]):
  elasticsearch:
    image: elasticsearch:7.7.0
    [...]
    ports:
      - "9200:9200"
      - "9300:9300"
    expose:
      - "9200"
"""

elasticsearch = CClass(service, "elasticsearch", stereotype_instances = [search_engine, infrastructural], tagged_values = {'Search Engine': "Elasticsearch", 'Port': 9200})



"""
Components:
    - logging server (Logstash), "logstash", port 5000
    - connection to elasticsearch
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[252:265]):
  logstash:
    image: logstash:7.7.0
    [...]
    ports:
      - "5000:5000"
    expose:
      - "5000"
    [...]
    command: >
      logstash --debug -e 'input { tcp { port => 5000 codec => json { charset => "UTF-8" } } } output { elasticsearch { hosts => "elasticsearch:9200" } }'
    depends_on:
      - elasticsearch
"""

logstash = CClass(service, "logstash", stereotype_instances = [logging_server, infrastructural], tagged_values = {'Logging Server': "Logstash", 'Port': 5000})

add_links({logstash: elasticsearch}, stereotype_instances = restful_http)



"""
Components:
    - monitoring dashboard (Kibana), "kibana", port 5601
    - connetion to elasticsearch
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[207:218]):
  kibana:
    image: kibana:7.7.0
    [...]
    ports:
      - "5601:5601"
    expose:
      - "5601"
    environment:
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    links:
      - elasticsearch
"""

kibana = CClass(service, "kibana", stereotype_instances = [monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Dashboard': "Kibana", 'Port': 5601})

add_links({elasticsearch: kibana}, stereotype_instances = restful_http)



"""
Components:
    - message broker (RabbitMQ), "rabbit", port 15672
    - plaintext_credentials
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[123:137]):
  rabbit:
    image: rabbitmq:3.8-management-alpine
    [...]
    ports:
      - "5672:5672"
      - "15672:15672"
    expose:
      - "15672"
    environment:
      [...]
      - RABBITMQ_DEFAULT_USER=mudigal
      - RABBITMQ_DEFAULT_PASS=mudigal
"""

rabbit = CClass(service, "rabbit", stereotype_instances = [message_broker, plaintext_credentials, infrastructural], tagged_values = {'Message Broker': "RabbitMQ", 'Port': 15672, 'Username': "mudigal", 'Password': "mudigal"})



"""
Components:
    - service-one (internal), port 8082
    - connection to service discovery
    - local logging
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/java/com/mudigal/one/ServiceOneApplication.java
Artifact (lines[20:22]):
    @EnableScheduling
    @SpringBootApplication
    @EnableDiscoveryClient
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/docker/Dockerfile
Artifact (line 7):
    EXPOSE 8082
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/resources/application.yml
Artifact (lines[50:52]):
    consul:
      host: consul
      port: 8500

Local logging:
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/java/com/mudigal/one/service/impl/NameValueServiceImpl.java
Artifact (lines [6;27;47]):
    import org.slf4j.LoggerFactory;

        private Logger logger = LoggerFactory.getLogger(NameValueServiceImpl.class);

            logger.info("Sending data to RabbitMQ's queue. Data: " + dataForRabbit);
"""

service_one = CClass(service, "service-one", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8082})

add_links({consul: service_one}, stereotype_instances = restful_http)



"""
Component: connection service-one to logstash
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/resources/application.yml
Artifact (line 67):
    logstash.servers: logstash:5000
"""

add_links({service_one: logstash}, stereotype_instances = restful_http)



"""
Component: connection service-one to rabbit, exchange "com.mudigal.microservices-sample.services-exchange", queue "com.mudigal.microservices-sample.service-one", routing key "com.mudigal.microservices-sample.service-*"
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/resources/application.yml
Artifact (lines[60:64]):
    rabbitmq:
        host: rabbit
        port: 5672
        username: mudigal
        password: mudigal
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/java/com/mudigal/one/component/queue/ServiceOneRabbitMQBean.java
Artifact (line 44):
    return BindingBuilder.bind(queue).to(exchange).with(routingKeyName);
And artifact (lines[28:30]):
    public final static String queueName = "com.mudigal.microservices-sample.service-one";
    public final static String exchangeName = "com.mudigal.microservices-sample.services-exchange";
    public final static String routingKeyName = "com.mudigal.microservices-sample.service-*";
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/java/com/mudigal/one/service/impl/ServiceOneRabbitMessageProducer.java
Artifact (lines[35:36]):
	this.rabbitTemplate.convertAndSend(ServiceOneRabbitMQBean.exchangeName,
			ServiceOneRabbitMQBean.routingKeyName, new ObjectMapper().writeValueAsString(data));
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/java/com/mudigal/one/service/impl/ServiceOneRabbitMessageConsumer.java
Artifact (line 37):
    @RabbitListener(queues = ServiceOneRabbitMQBean.queueName)
"""

add_links({service_one: rabbit}, stereotype_instances = [message_producer_rabbitmq, plaintext_credentials_link, restful_http], tagged_values = {'Producer Exchange': "com.mudigal.microservices-sample.services-exchange", 'Routing Key': "com.mudigal.microservices-sample.service-*"})

add_links({rabbit: service_one}, stereotype_instances = [message_consumer_rabbitmq, restful_http], tagged_values = {'Queue': "com.mudigal.microservices-sample.service-one"})



"""
Components:
    - service-two (internal), port 8084
    - connection to service discovery
    - local logging
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/java/com/mudigal/two/ServiceTwoApplication.java
Artifact (lines[16:17]):
    @EnableDiscoveryClient
    @SpringBootApplication
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/docker/Dockerfile
Artifact (line 7):
    EXPOSE 8084
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/resources/application.yml
Artifact (lines[61:63]):
    consul:
      host: consul
      port: 8500

Local Logging:
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/java/com/mudigal/two/service/impl/NameValueServiceImpl.java
Artifact (lines [13;26;50]):
    import org.apache.log4j.Logger;

        private Logger logger = Logger.getLogger(NameValueServiceImpl.class);

            logger.info("Updated: " + nameValueTO);
"""

service_two = CClass(service, "service-two", stereotype_instances = [internal, local_logging], tagged_values = {'Port': 8084})

add_links({consul: service_two}, stereotype_instances = restful_http)



"""
Component: connection service-one to logstash
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/resources/application.yml
Artifact (lines[88:89]):
    logstash:
        servers: logstash:5000
"""

add_links({service_two: logstash}, stereotype_instances = restful_http)



"""
Component: connection service-two to rabbit, exchange "com.mudigal.microservices-sample.services-exchange", queue "com.mudigal.microservices-sample.service-two", routing key "com.mudigal.microservices-sample.service-*"
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/resources/application.yml
Artifact (lines[81:85]):
    rabbitmq:
        host: rabbit
        port: 5672
        username: mudigal
        password: mudigal
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/java/com/mudigal/two/component/queue/ServiceTwoRabbitMQBean.java
Artifact (line 41):
    return BindingBuilder.bind(queue).to(exchange).with(routingKeyName);
And artifact (lines[25:27]):
    public final static String queueName = "com.mudigal.microservices-sample.service-two";
    public final static String exchangeName = "com.mudigal.microservices-sample.services-exchange";
    public final static String routingKeyName = "com.mudigal.microservices-sample.service-*";
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/java/com/mudigal/two/service/impl/ServiceTwoRabbitMessageProducer.java
Artifact (lines[37:38]):
    this.rabbitTemplate.convertAndSend(ServiceTwoRabbitMQBean.exchangeName,
        ServiceTwoRabbitMQBean.routingKeyName, new ObjectMapper().writeValueAsString(message));
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/java/com/mudigal/two/service/impl/ServiceTwoRabbitMessageConsumer.java
Artifact (line 35):
    @RabbitListener(queues = ServiceTwoRabbitMQBean.queueName)
"""

add_links({service_two: rabbit}, stereotype_instances = [message_producer_rabbitmq, restful_http, plaintext_credentials_link], tagged_values = {'Producer Exchange': "com.mudigal.microservices-sample.services-exchange", 'Routing Key': "com.mudigal.microservices-sample.service-*"})

add_links({rabbit: service_two}, stereotype_instances = [message_consumer_rabbitmq, restful_http], tagged_values = {'Queue': "com.mudigal.microservices-sample.service-two"})



"""
Components:
    - database (MongoDB), "service-one-db", port 27017
    - plaintext credentials
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[45:59]):
  service-one-db:
    [...]
    image: "mongo:3.7"
    environment:
      - MONGODB_USER="service-one"
      - MONGODB_PASS="service-one"
      [...]
    ports:
      - "27017:27017"
    expose:
      - "27017"
"""

service_one_db = CClass(database_component, "service-one-db", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MongoDB", 'Username': "service-one", 'Password': "service-one", 'Port': 27017})



"""
Component: connection service-one to service-one-db
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-one/src/main/resources/application.yml
Artifact (lines[58:59]):
    data:
        mongodb.uri: mongodb://service-one-db/service-one
"""

add_links({service_one_db: service_one}, stereotype_instances = jdbc)



"""
Components:
    - database (MySQL), "service-two-db", port 3310
    - plaintext_credentials
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[87:99]):
  service-two-db:
    [...]
    image: "mysql/mysql-server:5.7"
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=service-two
      - MYSQL_USER=service-two
      - MYSQL_PASSWORD=service-two
    ports:
      - "3310:3306"
    expose:
      - "3310"
"""

service_two_db = CClass(database_component, "service-two-db", stereotype_instances = [database, plaintext_credentials], tagged_values = {'Database': "MySQL", 'Username': "service-two", 'Password': "service-two", 'Port': 3310})



"""
Component: connection service-two to service-two-db, plaintext  credentials
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/service-two/src/main/resources/application.yml
Artifact (lines[70:74]):
    datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://service-two-db/service-two?createDatabaseIfNotExist=true
        username: service-two
        password: service-two
"""

add_links({service_two_db: service_two}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Username': "service-two", 'Password': "service-two"})



"""
Component:
    - API gateway (Zuul), "api-gateway", port 8080
    - load balancer (built in with Zuul)
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/api-gateway/src/main/java/com/mudigal/ApiGatewayApplication.java
Artifact (lines[17:18]):
    @EnableZuulProxy
    @SpringBootApplication
And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/api-gateway/src/main/docker/Dockerfile
Artifact (line 7):
    EXPOSE 8080
"""

api_gateway = CClass(service, "api-gateway", stereotype_instances = [gateway, infrastructural, load_balancer], tagged_values = {'Gateway': "Zuul", 'Load Balancer': "Ribbon", 'Port': 8080})



"""
Components:
    - connection api-gateway to service-one
    - connection api-gateway to service-two
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/api-gateway/src/main/resources/application.yml
Artifact (lines[21:32]):
    zuul:
      ignoredServices: '*'
      routes:
        one:
          path: /service-one/**
          serviceId: Service-One
        two:
          path: /service-two/**
          serviceId: Service-Two
        three:
          path: /service-three/**
          serviceId: Service-Three
*Note*: service-three seems to be a residue, no such service exists
"""

add_links({api_gateway: service_one}, stereotype_instances = restful_http)

add_links({api_gateway: service_two}, stereotype_instances = restful_http)



"""
Component: connection api-gateway to consul
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/api-gateway/src/main/resources/application.yml
Artifact (lines[39:41]):
    consul:
      host: consul
      port: 8500
"""

add_links({consul: api_gateway}, stereotype_instances = restful_http)



"""
Components:
    - web app (Nginx), "web-application", port 4200
    - user (implicit with web app)
    - connection user to web app
File: https://github.com/mudigal-technologies/microservices-sample/tree/master/web-application

And file: https://github.com/mudigal-technologies/microservices-sample/blob/master/web-application/docker/Dockerfile
Artifact (line 23):
    EXPOSE 4200
"""

web_application = CClass(service, "web-application", stereotype_instances = [web_application, infrastructural], tagged_values = {'Web Application': "Nginx", 'Port': 4200})

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])

add_links({user: web_application}, stereotype_instances = restful_http)
add_links({web_application: user}, stereotype_instances = restful_http)


"""
Component: connection web-application to api-gateway
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/web-application/src/app/service/gateway/gateway.service.ts
Artifact (line 13):
    return this.httpClient.get<DataResponse>('http://' + window.location.hostname + ':8080/' + service);
"""

add_links({web_application: api_gateway}, stereotype_instances = restful_http)



"""
Component: infrastructure visualization (Weave Scope), "scope", port 4040
File: https://github.com/mudigal-technologies/microservices-sample/blob/master/build/docker/docker-compose.yml
Artifact (lines[184:193]):
  scope:
    image: weaveworks/scope:1.13.1
    [...]
    ports:
      - "4040:4040"
    expose:
      - "4040"
"""

scope = CClass(service, "scope", stereotype_instances = [monitoring_dashboard, infrastructural], tagged_values = {'Monitoring Dashboard': "Weave Scope", 'Port': 4040})



"""
Components: connections of weavescope to all other services
Artifact: implicit; weavescope detects all other Docker containers automatically
"""

add_links({api_gateway: scope}, stereotype_instances = restful_http)

add_links({service_one: scope}, stereotype_instances = restful_http)

add_links({service_one_db: scope}, stereotype_instances = restful_http)

add_links({service_two: scope}, stereotype_instances = restful_http)

add_links({service_two_db: scope}, stereotype_instances = restful_http)

add_links({web_application: scope}, stereotype_instances = restful_http)

add_links({rabbit: scope}, stereotype_instances = restful_http)

add_links({consul: scope}, stereotype_instances = restful_http)

add_links({consul2: scope}, stereotype_instances = restful_http)

add_links({consul3: scope}, stereotype_instances = restful_http)

add_links({kibana: scope}, stereotype_instances = restful_http)

add_links({elasticsearch: scope}, stereotype_instances = restful_http)

add_links({logstash: scope}, stereotype_instances = restful_http)


##### Create model
model = CBundle(model_name, elements = api_gateway.class_object.get_connected_elements())


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
