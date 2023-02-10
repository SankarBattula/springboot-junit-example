# springboot-junit-example

@SpringBootApplication is a convenience annotation that adds all of the following:

    @Configuration: Tags the class as a source of bean definitions for the application context.
    @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
    @EnableWebMvc: Flags the application as a web application and activates key behaviors, such as setting up a DispatcherServlet. Spring Boot adds it automatically when it sees spring-webmvc on the classpath.
    @ComponentScan: Tells Spring to look for other components, configurations, and services in the the com.example.testingweb package, letting it find the HelloController class.


Test the Application:
----------------------
The first thing you can do is write a simple sanity check test that will fail if the application context cannot start.

@SpringBootTest
public class TestingWebApplicationTests {

	@Test
		public void contextLoads() {
	}

}

---------------------
The @SpringBootTest annotation tells Spring Boot to look for a main configuration class (one with @SpringBootApplication, for instance) and use that to start a Spring application context. 
You can run this test in your IDE or on the command line (by running ./mvnw test or ./gradlew test), and it should pass.
To convince yourself that the context is creating your controller, you could add an assertion, as the following example

@SpringBootTest
public class SmokeTest {

	@Autowired
	private HomeController controller;

	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}
}

Spring interprets the @Autowired annotation, and the controller is injected before the test methods are run. 
We use AssertJ (which provides assertThat() and other methods) to express the test assertions.

A nice feature of the Spring Test support is that the application context is cached between tests. 
That way, if you have multiple methods in a test case or multiple test cases with the same configuration, they incur the cost of starting the application only once. 
You can control the cache by using the @DirtiesContext annotation.

------------------------------
It is nice to have a sanity check, but you should also write some tests that assert the behavior of your application. 
To do that, you could start the application and listen for a connection (as it would do in production) and then send an HTTP request and assert the response. 
The following listing

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {

	@Value(value="${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void greetingShouldReturnDefaultMessage() throws Exception {
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/",
				String.class)).contains("Hello, World");
	}
}

webEnvironment=RANDOM_PORT to start the server with a random port (useful to avoid conflicts in test environments) and the injection of the port with @LocalServerPort
Also, note that Spring Boot has automatically provided a TestRestTemplate for you. All you have to do is add @Autowired to it.

------------------------------------------------------------------------------------
Another useful approach is to not start the server at all but to test only the layer below that, where Spring handles the incoming HTTP request and hands it off to your controller. 
That way, almost of the full stack is used, and your code will be called in exactly the same way as if it were processing a real HTTP request but without the cost of starting the server. 
To do that, use Spring’s MockMvc and ask for that to be injected for you by using the @AutoConfigureMockMvc annotation on the test case. 
The following listing shows how to do so:

@SpringBootTest
@AutoConfigureMockMvc
public class TestingWebApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void shouldReturnDefaultMessage() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, World")));
	}
}

--------------------------------------------------------------------------------------
In this test, the full Spring application context is started but without the server. 
We can narrow the tests to only the web layer by using @WebMvcTest, as the following listing shows:
@WebMvcTest
public class WebLayerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void shouldReturnDefaultMessage() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, World")));
	}
}

----------------------------------------------------------------------------------------

The test assertion is the same as in the previous case. However, in this test, Spring Boot instantiates only the web layer rather than the whole context. 
In an application with multiple controllers, you can even ask for only one to be instantiated by using, for example, @WebMvcTest(HomeController.class).

So far, our HomeController is simple and has no dependencies. We could make it more realistic by introducing an extra component to store the greeting 
(perhaps in a new controller). The following example shows how to do so:

@WebMvcTest(HomeController.class)
public class WebMockTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private GreetingService service;

	@Test
	public void greetingShouldReturnMessageFromService() throws Exception {
		when(service.greet()).thenReturn("Hello, Mock");
		this.mockMvc.perform(get("/greeting")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, Mock")));
	}
}
We use @MockBean to create and inject a mock for the GreetingService (if you do not do so, the application context cannot start), and we set its expectations using Mockito.
