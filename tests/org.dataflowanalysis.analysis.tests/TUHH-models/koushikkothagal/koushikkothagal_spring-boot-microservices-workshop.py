from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# source: https://github.com/koushikkothagal/spring-boot-microservices-workshop

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "koushikkothagal_spring-boot-microservices-workshop"



"""
Component:
    - service discovery (Eureka), "discovery-server", port 8761
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/discovery-server/src/main/java/io/javabrains/discoveryserver/DiscoveryServerApplication.java
Artifact (line 8):
    @EnableEurekaServer
And file: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/discovery-server/pom.xml
Artifact (line 12):
    <artifactId>discovery-server</artifactId>
And file: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/discovery-server/src/main/resources/application.properties
Artifact (line 1):
    server.port=8761
"""

discovery_server = CClass(service, "discovery-server", stereotype_instances = [infrastructural, service_discovery], tagged_values = {'Port': 8761, 'Service Discovery': "Eureka"})



"""
Component:
    - internal service "ratings-data-service", port 8083
    - endpoints ["/ratingsdata/movies/{movieId}", "/ratingsdata/user/{userId}"]
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/ratings-data-service/src/main/java/io/javabrains/ratingsdataservice/RatingsDataServiceApplication.java
Artifact (line 7):
    @SpringBootApplication
And file:
Artifact (line 1):
    spring.application.name=ratings-data-service
And artifact (line 2):
    server.port=8083

Endpoints:
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/ratings-data-service/src/main/java/io/javabrains/ratingsdataservice/resources/RatingsResource.java
Artifact (line 10):
    @RequestMapping("/ratingsdata")
And artifact (line 13):
    @RequestMapping("/movies/{movieId}")
And artifact (line 18):
    @RequestMapping("/user/{userId}")
"""

ratings_data_service = CClass(service, "ratings-data-service", stereotype_instances = [internal], tagged_values = {'Port': 8083, 'Endpoints': "[\'/ratingsdata\', \'/ratingsdata/movies/{movieId}\', \'/ratingsdata/user/{userId}\']"})



"""
Component:
    - connection ratings-data-service to discovery-server
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/ratings-data-service/src/main/java/io/javabrains/ratingsdataservice/RatingsDataServiceApplication.java
Artifact (line 8):
    @EnableEurekaClient
"""

add_links({ratings_data_service: discovery_server}, stereotype_instances = restful_http)



"""
Component:
    - internal service "movie-info-service", port 8082
    - endpoints ["/movies", "/movies/{movieId}"]
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-info-service/src/main/java/io/javabrains/movieinfoservice/MovieInfoServiceApplication.java
Artifact (line 9):
    @SpringBootApplication
And file: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-info-service/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name=movie-info-service
And artifact (line 2):
    server.port=8082

Endpoints:
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-info-service/src/main/java/io/javabrains/movieinfoservice/resources/MovieResource.java
Artifact (line 13):
    @RequestMapping("/movies")
And artifact (line 22):
    @RequestMapping("/{movieId}")
"""

movie_info_service = CClass(service, "movie-info-service", stereotype_instances = [internal], tagged_values = {'Port': 8082, 'Endpoints': "[\'/movies\', \'/movies/{movieId}\']"})



"""
Component:
    - connection movie-info-service to discovery-server
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-info-service/src/main/java/io/javabrains/movieinfoservice/MovieInfoServiceApplication.java
Artifact (line 10):
    @EnableEurekaClient
"""

add_links({movie_info_service: discovery_server}, stereotype_instances = restful_http)



"""
Component:
    - external website "https://api.themoviedb.org"
    - connection external website to movie-info-service
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-info-service/src/main/java/io/javabrains/movieinfoservice/resources/MovieResource.java
Artifact (line 24):
    MovieSummary movieSummary = restTemplate.getForObject("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" +  apiKey, MovieSummary.class);
"""

external_website = CClass(external_component, "external-website", stereotype_instances = [external_website], tagged_values = {'URL': "https://api.themoviedb.org"})

add_links({external_website: movie_info_service}, stereotype_instances = restful_http)



"""
Component:
    - internal service "movie-catalog-service", port 8081
    - load balancer (Spring Cloud)
    - endpoints ["/catalog", "/catalog/{userId}"]
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/MovieCatalogServiceApplication.java
Artifact (line 10):
    @SpringBootApplication
And file: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/resources/application.properties
Artifact (line 1):
    spring.application.name=movie-catalog-service
And artifact (line 2):
    server.port=8081

Load Balancer:
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/MovieCatalogServiceApplication.java
Artifact (line 18):
    @LoadBalanced

Endpoints:
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/resources/CatalogResource.java
Artifact (line 20):
    @RequestMapping("/catalog")
And artifact (line 29):
    @RequestMapping("/{userId}")
"""

movie_catalog_service = CClass(service, "movie-catalog-service", stereotype_instances = [internal, load_balancer], tagged_values = {'Port': 8081, 'Load Balancer': "Spring Cloud", 'Endpoints': "[\'/catalog\', \'/catalog/{userId}\']"})



"""
Component:
    - connection movie-catalog-service to discovery-server
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/MovieCatalogServiceApplication.java
Artifact (line 11):
    @EnableEurekaClient
"""

add_links({movie_catalog_service: discovery_server}, stereotype_instances = [restful_http, load_balanced_link])



"""
Component:
    - connection movie-catalog-service to ratings-data-service
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/resources/CatalogResource.java
Artifact (line 32):
    UserRating userRating = restTemplate.getForObject("http://ratings-data-service/ratingsdata/user/" + userId, UserRating.class);
"""

add_links({movie_catalog_service: ratings_data_service}, stereotype_instances = [restful_http, load_balanced_link])



"""
Component:
    - connection movie-catalog-service to movie-info-service
File: https://github.com/koushikkothagal/spring-boot-microservices-workshop/blob/master/movie-catalog-service/src/main/java/io/javabrains/moviecatalogservice/resources/CatalogResource.java
Artifact (line 36):
    Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
"""

add_links({movie_catalog_service: movie_info_service}, stereotype_instances = [restful_http, load_balanced_link])




##### Create model
model = CBundle(model_name, elements = discovery_server.class_object.get_connected_elements())

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
