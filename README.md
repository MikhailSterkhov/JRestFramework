# JRestFramework
Create a REST project services structure with a JRestFramework

---

This library provides many tools for configurability 
and initialization flexibility to run your code or project.

---

## FEEDBACK

- My Discord Server: **[Link](https://discord.gg/GmT9pUy8af)**
- My VKontakte Page: **[Link](https://vk.com/itzstonlex)**

---

## HOW TO USE?

### REST CLIENT

A simple example of REST-client structure:

```java
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.RestBody;
import com.itzstonlex.restframework.api.response.RestResponse;
import lombok.NonNull;

@RestService
@RestClient(url = "http://localhost:8082/api")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestClient {

    /**
     * Handling of exceptions.
     *
     * @param exception - Thrown exception.
     */
    @RestExceptionHandler
    void handle(IOException exception);

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @RestHeader(name = "Content-Type", value = "application/json")
    @Get(context = "/user")
    Userdata getUserdata(@RestParam("name") String name);

    @Get(context = "/users")
    List<Userdata> getCachedUserdataList(@RestParam("limit") long limit);

    /**
     * And this function returns a direct HTTP result after
     * executing the request with all the native data
     *
     * @param name - Name of user.
     */
    @Get(context = "/user")
    RestResponse getUserdataAsResponse(@RestParam("name") String name);

    /**
     * The requestMethod body can be created using the
     * {@link com.itzstonlex.restframework.api.RestBody}
     * factory, as shown in this example
     */
    @RestHeader(name = "Auth-Token", value = "TestToken123")
    @Post(context = "/adduser", useSignature = false)
    RestResponse addUserdata(@NonNull RestBody postMessage);
}
```

_P.S.: And also no one forbids not using the method signature at all_

---

### REST SERVER

A simple example of REST-server structure:

```java
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.RestBody;
import com.itzstonlex.restframework.api.request.RestRequestContext;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.test.Userdata;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.itzstonlex.restframework.api.response.RestResponse.CLIENT_ERROR;
import static com.itzstonlex.restframework.api.response.RestResponse.SUCCESS;

@RestService
@RestServer(host = "localhost", port = 8082, defaultContext = "/api")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestServerTest {

    private static final String AUTH_TOKEN = "Auth-Token";

    private List<Userdata> userdataList;

    @RestExceptionHandler
    public void onExceptionThrow(IOException exception) {
        exception.printStackTrace();
    }

    @RestExceptionHandler
    public void onExceptionThrow(IllegalArgumentException exception) {
        System.out.println("Wrong authentication token!");
    }

    @Get(context = "/users")
    public RestResponse onUsersGet() {
        return RestResponse.createOnlyBody(SUCCESS, userdataList);
    }

    @Get(context = "/users")
    public RestResponse onLimitedUsersGet(@RestParam("limit") long limit) {
        return RestResponse.createOnlyBody(SUCCESS, userdataList.stream().limit(limit).collect(Collectors.toList()));
    }

    @Get(context = "/user")
    public RestResponse onUserGet(@RestParam("name") String name) {
        Userdata userdata = userdataList.stream()
                .filter(cached -> cached.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (userdata == null) {
            return RestResponse.createOnlyBody(CLIENT_ERROR + 4, RestBody.asText("Userdata is not found"));
        }

        return RestResponse.createOnlyBody(SUCCESS, userdata);
    }

    @Post(context = "/adduser")
    public RestResponse onUserAdd(@RestParam RestRequestContext context) {
        if (context.getFirstHeader(AUTH_TOKEN).equals("TestToken123")) {
            throw new IllegalArgumentException(AUTH_TOKEN);
        }

        RestBody message = context.getBody();

        Userdata newUserdata = message.getBodyAsJsonObject(Userdata.class);

        userdataList.add(newUserdata);

        message.setValue("Successfully added");
        return RestResponse.createOnlyBody(SUCCESS, message);
    }
}
```

---

### TESTING REST-SERVICES

A simple example of initializing your<br>
project for the requirements of this library:

```java
import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestFrameworkStorage;

public class Bootstrap {

    public static void main(String[] args) {
        RestFrameworkStorage rest = RestFrameworkBootstrap.runServices(Bootstrap.class);
    }
}
```

Example REST services tests:
```java
// Initial REST-server
rest.initServer(RestServerTest.class, new ArrayList<>());

// Get initialized REST-client
RestClientTest restClient = rest.get(RestClientTest.class);

// Add user & print response,
System.out.println("[Test] " + restClient.addUserdata(
        RestRequestMessage.asJsonObject(new Userdata("itzstonlex", 18, 3)))
);

// Get response-data at variables.
Userdata itzstonlex = restClient.getUserdata("itzstonlex");
RestResponse itzstonlexResponse = restClient.getUserdataAsResponse("itzstonlex");

// Print responses.
System.out.println("[Test] " + itzstonlex);
System.out.println("[Test] " + itzstonlexResponse);
System.out.println("[Test] " + restClient.getCachedUserdataList(2));
```
Console Output Example:
```shell
[Test] RestResponse(responseCode=200, responseMessage=OK, url=http://localhost:8082/api/adduser, body={"message":"Successfully added"}, method=POST)
[Test] Userdata(name=itzstonlex, age=18, count=3)
[Test] RestResponse(responseCode=200, responseMessage=OK, url=http://localhost:8082/api/user?name=itzstonlex, body={"name":"itzstonlex","age":18,"count":3}, method=GET)
[Test] [{name=itzstonlex, age=18.0, count=3.0}]
```
---

## PLEASE, SUPPORT ME


By clicking on this link, you can support me as a 
developer and motivate me to develop new open-source projects!

<a href="https://www.buymeacoffee.com/itzstonlex" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
