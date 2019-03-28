# READ ME
This project contains sample code for an article on implementing Web APIs with JAX-RS 2.1, which was published in Java Magazin 05.19.

Disclaimer: This code is for demonstration purposes only and not meant to be used in production systems.

## Requirements:

- Java SE 11
- Maven
- A Google API key (see https://developers.google.com/maps/documentation/geocoding/get-api-key)

Once you have obtained a Google API key, copy it into the configuration file **/resources/config.properties**. 
The name of the relevant configuration parameter is **integration.google.apikey**.


## What's included:

- /src/main/java: Sample code for custom thread pools, injection of configuration and DTOs for communication with the Google APIs.

- /src/test/java: Three test classes demonstrating the differences between the old JAX-RS 2.0 client API (**JaxRs20Client**)
and the new reactive client API in JAX-RS 2.1 (**ReactiveClient** and **ReactiveClient2**)

**JaxRsClient20** demonstrates how subsequent asynchrounous requests using the JAX-RS 2.0 API result in nested **InvocationCallback**s 
and thus in code that is very hard to read and maintain. In contrast, **ReactiveClient** uses the reactive client API introduced in 
JAX-RS 2.1 that facilitates **java.util.concurrent.CompletionStage** to define multiple subsequent stages, or HTTP requests in this case. 
In class **ReactiveClient2** this approach is further refined by moving the code required for a given stage into separate methods, 
resulting in code that is easily readable and maintainable. A method like **testOneCompletionStage** allows a developer to understand 
very quickly what steps (or stages) are required to complete the task at hand. Finally, **testTwoCompletionStages** demonstrates how
two tasks, each consisting of multiple subsequent steps or stages, can be run concurrently while the code is still much better readable
than with JAX-RS 2.0.


## How to run:

To see the code in action, run one of the test classes **JaxRs20Client**, **ReactiveClient** or **ReactiveClient2**.