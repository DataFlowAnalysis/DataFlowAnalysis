from codeable_models import CClass, CBundle, add_links, CStereotype, CMetaclass, CEnum, CAttribute
from metamodels.microservice_dfds_metamodel import *
from plant_uml_renderer import PlantUMLGenerator

# Sources:
#   - https://github.com/piomin/sample-spring-oauth2-microservices/tree/with_database
#   - https://piotrminkowski.wordpress.com/2017/12/01/part-2-microservices-security-with-oauth2/

plantuml_path = "../../plantuml.jar"
output_directory = "."
model_name = "piomin_sample-spring-oauth2-microservices"


"""
Component: Discovery Server (Eureka), "discovery", port 8761
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/discovery/src/main/java/pl/piomin/services/discovery/DiscoveryServer.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableEurekaServer
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/discovery/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
      port: ${PORT:8761}
"""

discovery_server = CClass(service, "discovery-server", stereotype_instances = [service_discovery, infrastructural], tagged_values = {'Service Discovery': "Eureka", 'Port': 8761})



"""
Component:
    - API Gateway (Zuul), "gateway", port 8765
    - gateway database
    - connection gateway to gateway-database
    - connection to service discovery
    - in-memory authentication, plain-text credentials
    - load balancer (built in with Zuul)
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/master/gateway/src/main/java/pl/piomin/services/gateway/GatewayServer.java
Artifact (lines[14:1830]):
    @SpringBootApplication
    @EnableZuulProxy
    @EnableOAuth2Sso
    @EnableDiscoveryClient
    @EnableJdbcHttpSession
And artifact (lines[27:28]):
	return DataSourceBuilder.create().url("jdbc:mysql://192.168.99.100:33306/default?useSSL=false")
			.username("default").password("default").driverClassName("com.mysql.jdbc.Driver").build();
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/gateway/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
        port: ${PORT:8765}
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/gateway/src/main/java/pl/piomin/services/gateway/SecurityConfig.java
Artifact (lines[33:37]):
    @Override
    	public void configure(AuthenticationManagerBuilder auth) throws Exception {
            [...]
    		auth.inMemoryAuthentication().withUser("root").password("password").roles("USER");
    	}
"""

gateway_server = CClass(service, "gateway-server", stereotype_instances = [gateway, in_memory_authentication, plaintext_credentials, infrastructural, load_balancer], tagged_values = {'Gateway': "Zuul", 'Username': "root", 'Password': "password", 'Port': 8765, 'Load Balancer': "Ribbon"})

add_links({discovery_server: gateway_server}, stereotype_instances = restful_http)

database_gateway_server = CClass(external_component, "database-gateway-server", stereotype_instances = [external_database, entrypoint, exitpoint, plaintext_credentials], tagged_values = {'Database': "MySQL", 'Password': "default", 'Username': "default"})

add_links({database_gateway_server: gateway_server}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {'Password': "default", 'Username': "default"})



"""
Component: User
Implicit through Gateway
"""

user = CClass(external_component, "User", stereotype_instances = [user_stereotype, entrypoint, exitpoint])



"""
Component: connection user to gateway, authenticated
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/gateway/src/main/java/pl/piomin/services/gateway/SecurityConfig.java
Artifact (lines[21:31]):
    @Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
			.anyRequest().authenticated()
			.and()
		.formLogin()
			.loginPage("/login")
			.permitAll()
			.and().httpBasic().disable();
	}

"""

add_links({user: gateway_server}, stereotype_instances = restful_http)

add_links({gateway_server: user}, stereotype_instances = restful_http)



"""
Components:
    - authorization server (Spring OAuth2), "auth", port 9999
    - connection to discovery service
    - auth-database (MySQL), "ouath2"
    - connection auth to auth-database
    - plaintext credentials
    - tokenstore in auth-database
    - encryption
    - local logging
    - resource server
Authorization server:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/java/pl/piomin/services/auth/AuthServer.java
Artifact (lines[11:13]):
    @SpringBootApplication
    @RestController
    @EnableDiscoveryClient
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/java/pl/piomin/services/auth/OAuth2Config.java
Artifact (lines[17:19]):
    @Configuration
    @EnableAuthorizationServer
    public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

Port:
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
        port: ${PORT:9999}

Database:
File: https://piotrminkowski.wordpress.com/2017/12/01/part-2-microservices-security-with-oauth2/
Artifact:
    "All the authentication credentials and tokens are stored in MySQL database. So, the first step is to start MySQL. The most comfortable way to achieve it is through a Docker container. The command visible below in addition to starting database also creates schema and user oauth2.
    docker run -d --name mysql -e MYSQL_DATABASE=oauth2 -e MYSQL_USER=oauth2 -e MYSQL_PASSWORD=oauth2 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -p 33306:3306 mysql"
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/resources/application.yml
Artifact (lines[7:10]):
    datasource:
        url: jdbc:mysql://192.168.99.100:33306/oauth2?useSSL=false
        username: oauth2
        password: oauth2

Tokenstore in database:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/java/pl/piomin/services/auth/OAuth2Config.java
Artifact (lines[21:22]):
    @Autowired
	private DataSource dataSource;
And artifact (lines[47:50]):
    @Bean
	public JdbcTokenStore tokenStore() {
		return new JdbcTokenStore(dataSource);
	}

Encryption:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/java/pl/piomin/services/auth/SecurityConfig.java
Artifact (lines[7:8;24:25;32]):
    import org.springframework.security.authentication.encoding.PasswordEncoder;
    import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

        public PasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder(256);

                	.passwordEncoder(passwordEncoder());

Local logging:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/auth/src/main/java/pl/piomin/services/auth/security/UserDetailsServiceImpl.java
Artifact (lines[7;24;33]):
    import org.slf4j.LoggerFactory;

        private final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

                log.debug("Authenticating {}", login);

Resource Server:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/05f390ee4351247a9c5803098059238424b58bae/auth/src/main/java/pl/piomin/services/auth/AuthServer.java
Artifact (line 21):
    @EnableResourceServer

Logging:
File: https://github.com/blaugmail/clone-sample-spring-oauth2-microservices/blob/main/customer/src/main/java/pl/piomin/services/customer/api/CustomerController.java
Artifact (line 17):
    private final static Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);
"""

auth_server = CClass(service, "auth-server", stereotype_instances = [authorization_server, encryption, infrastructural, resource_server, token_server, local_logging], tagged_values = {'Authorization Server': "Spring OAuth2", 'Port': 9999})

add_links({auth_server: discovery_server}, stereotype_instances = restful_http)

database_auth_server = CClass(external_component, "database-auth-server", stereotype_instances = [external_database, entrypoint, exitpoint, plaintext_credentials, tokenstore], tagged_values = {'Database': "MySQL", "Username": "oauth2", "Password": "oauth2"})

add_links({database_auth_server: auth_server}, stereotype_instances = [jdbc, plaintext_credentials_link], tagged_values = {"Username": "oauth2", "Password": "oauth2"})



"""
Component: connection gateway to authorization server
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/gateway/src/main/resources/application.yml
Artifact (lines[37:44]):
    oauth2:
        client:
            accessTokenUri: http://localhost:8765/uua/oauth/token
            userAuthorizationUri: http://localhost:8765/uua/oauth/authorize
            clientAuthenticationScheme: form
        resource:
            userInfoUri: http://localhost:8765/uaa/user
            preferTokenInfo: false
And artifact (lines[16:21]):
    zuul:
        routes:
            uaa:
                path: /uaa/**
                sensitiveHeaders:
                serviceId: auth-server
"""

add_links({gateway_server: auth_server}, stereotype_instances = [restful_http, auth_provider])



"""
Component:
    - account-service (internal), port 8082
    - connection to discovery service
    - pre-authorized method
    - connection to auth
    - resource server

Service and connection to discovery service:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/java/pl/piomin/services/account/AccountService.java
Artifact (lines[7:8]):
    @SpringBootApplication
    @EnableDiscoveryClient
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/resources/application.yml
Artifact (lines[1:2]):
    server:
        port: ${PORT:8082}

Pre-authorized method:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/java/pl/piomin/services/account/OAuth2ResourceServerConfig.java
Artifact (lines[10:12]):
    @Configuration
    @EnableResourceServer
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/java/pl/piomin/services/account/api/AccountController.java
Artifact (lines[16:27]):
	@GetMapping("/{id}")
	@PreAuthorize("#oauth2.hasScope('read')")
	public Account findAccount(@PathVariable("id") Integer id) {
		return new Account(id, 1, "123456789", 1234);
	}

	@GetMapping("/")
	@PreAuthorize("#oauth2.hasScope('read')")
	public List<Account> findAccounts() {
		return Arrays.asList(new Account(1, 1, "123456789", 1234), new Account(2, 1, "123456780", 2500),
				new Account(3, 1, "123456781", 10000));
	}

Connection to auth:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/resources/application.yml
Artifact (liens[15:23]):
    oauth2:
        client:
            client-id: account-service
            client-secret: secret
            scope: read
            access-token-uri: http://localhost:9999/oauth/token
            user-authorization-uri: http://localhost:9999/oauth/authorize
        resource:
            token-info-uri: http://localhost:9999/oauth/check_token

Resource Server:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/05f390ee4351247a9c5803098059238424b58bae/account/src/main/java/pl/piomin/services/account/AccountService.java
Artifact (line 10):
    @EnableResourceServer
"""

account_service = CClass(service, "account-service", stereotype_instances = [internal, pre_authorized_endpoints, resource_server], tagged_values = {'Pre-authorized Endpoints': ["/{id}", "/"], 'Port': 8082})

add_links({account_service: discovery_server}, stereotype_instances = restful_http)
add_links({auth_server: account_service}, stereotype_instances = [restful_http, auth_provider])



"""
Component: connection gateway to account-service
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/gateway/src/main/resources/application.yml
Artifact (lines[16:25]):
    zuul:
        routes:
            [...]
            account:
                path: /account/**
                sensitiveHeaders:
                serviceId: account-service
"""

add_links({gateway_server: account_service}, stereotype_instances = restful_http)



"""
Components:
    - customer-service (internal), port 8083
    - connection to service discovery
    - pre-authorized method
    - resource server
Service and connection to service discovery:
File:
Artifact (lines[8:10]):
    @SpringBootApplication
    @EnableDiscoveryClient
    @EnableFeignClients
And file:
Artifact (lines[1:2]):
    server:
        port: ${PORT:8083}

Pre-authorized method:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/java/pl/piomin/services/customer/OAuth2ResourceServerConfig.java
Artifact (lines[10:12]):
    @Configuration
    @EnableResourceServer
    @EnableGlobalMethodSecurity(prePostEnabled = true)
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/java/pl/piomin/services/customer/api/CustomerController.java
Artifact (lines[22:23]):
    @GetMapping("/{id}")
	@PreAuthorize("#oauth2.hasScope('read')")

Resource Server:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/java/pl/piomin/services/customer/OAuth2ResourceServerConfig.java
Artifact (line 11):
    @EnableResourceServer
"""

customer_service = CClass(service, "customer-service", stereotype_instances = [internal, pre_authorized_endpoints, resource_server, local_logging], tagged_values = {'Pre-authorized Endpoints': ["/{id}"], 'Port': 8083})

add_links({customer_service: discovery_server}, stereotype_instances = restful_http)



"""
Components:
    - connection customer-service to account-service through FeignClient
    - OAuth2 for this connection
    - connection to authorizatino server
    - load balancer for this connection (Ribbon)
Connection:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/java/pl/piomin/services/customer/client/AccountClient.java
Artifact (line 10):
    @FeignClient(name = "account-service", configuration = AccountClientConfiguration.class)
And file:https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/account/src/main/java/pl/piomin/services/account/api/AccountController.java
Artifact (line 24):
    public List<Account> findAccounts() {

OAuth for Feign (using a FeignInterceptor) and connection to auth:
File: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/java/pl/piomin/services/customer/client/AccountClientConfiguration.java
Artifact (lines[26:29]):
    @Bean
	RequestInterceptor oauth2FeignRequestInterceptor() {
		return new OAuth2FeignRequestInterceptor(new DefaultOAuth2ClientContext(), resource());
	}
And artifact (lines[36:46]):
private OAuth2ProtectedResourceDetails resource() {
		ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
		resourceDetails.setUsername("piomin");
		resourceDetails.setPassword("piot123");
		resourceDetails.setAccessTokenUri(accessTokenUri);
		resourceDetails.setClientId(clientId);
		resourceDetails.setClientSecret(clientSecret);
		resourceDetails.setGrantType("password");
		resourceDetails.setScope(Arrays.asList(scope));
		return resourceDetails;
	}
And artifact (lines[17:24]):
	@Value("${security.oauth2.client.access-token-uri}")
	private String accessTokenUri;
	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	@Value("${security.oauth2.client.scope}")
	private String scope;
And file: https://github.com/piomin/sample-spring-oauth2-microservices/blob/with_database/customer/src/main/resources/application.yml
Artifact (lines[11:19]):
    oauth2:
        client:
            client-id: customer-service
            client-secret: secret
            scope: read
            access-token-uri: http://localhost:9999/oauth/token
            user-authorization-uri: http://localhost:9999/oauth/authorize
        resource:
            token-info-uri: http://localhost:9999/oauth/check_token
"""

add_links({customer_service: account_service}, stereotype_instances = [restful_http, authenticated_request, feign_connection])
add_links({auth_server: customer_service}, stereotype_instances = [restful_http, auth_provider])



##### Create model
model = CBundle(model_name, elements = gateway_server.class_object.get_connected_elements())


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
