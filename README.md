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

A simple example of initializing your<br>
project for the requirements of this library:

```java
import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestFrameworkStorage;

public class Bootstrap {

    public static void main(String[] args) {
        RestFrameworkStorage restStorage = RestFrameworkBootstrap.runServices(Bootstrap.class);
    }
}
```

---

### REST CLIENT

A simple example of REST-client structure:

```java
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.request.RestRequestMessage;
import com.itzstonlex.restframework.api.response.RestResponse;
import lombok.NonNull;

@RestService
@RestClient(url = "http://localhost:8082/api")
@RestFlag(RestFlag.Type.DISALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestClient {

    /**
     * Handling of exceptions.
     *
     * @param exception - Thrown exception.
     */
    @RestExceptionHandler
    default void handle(IOException exception) {
        System.out.println("EXCEPTION HANDLER!");

        exception.printStackTrace();
    }

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @Get(context = "/user", timeout = 1000)
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
    RestResponse getUserdataResponse(@RestParam("name") String name);

    /**
     * The requestMethod body can be created using the
     * {@link com.itzstonlex.restframework.api.request.RestRequestMessage}
     * factory, as shown in this example
     */
    @Post(context = "/adduser", useSignature = false)
    RestResponse addUserdata(@NonNull RestRequestMessage postMessage);
}
```

_P.S.: And also no one forbids not using the method signature at all_

Tests REST-client structure:
```java
restStorage.initServer(RestServerTest.class, new ArrayList<>());

// await for server bind.
Thread.sleep(1500);

// test client connection.
RestClientTest restClient = restStorage.get(RestClientTest.class);

restClient.addUserdata(
RestRequestMessage.asJsonObject(new Userdata("itzstonlex", 18, 3)));

Userdata itzstonlex = restClient.getUserdata("itzstonlex");
RestResponse itzstonlexResponse = restClient.getUserdataResponse("itzstonlex");

System.out.println("[Test] " + itzstonlex);
System.out.println("[Test] " + itzstonlexResponse);
System.out.println("[Test] " + restClient.getCachedUserdataList(2));
```
Console Output Example:
```shell
[Test] Userdata(name=itzstonlex, age=18, count=3)
[Test] RestResponse(responseCode=200, responseMessage=OK, url=http://localhost:8082/api/user?name=itzstonlex, body={"name":"itzstonlex","age":18,"count":3}, method=GET)
[Test] [{name=itzstonlex, age=18.0, count=3.0}]
```
---

### REST SERVER

A simple example of REST-server structure:

```java
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.request.RestRequestMessage;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.test.Userdata;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestService
@RestServer(host = "localhost", port = 8082, bindTimeout = 1000, defaultContext = "/api")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestServerTest {

    private List<Userdata> userdataList;

    @RestExceptionHandler
    public void onExceptionThrow(IOException exception) {
        exception.printStackTrace();
    }

    @Get(context = "/users")
    public RestResponse onUsersGet() {
        return RestResponse.createOnlyBody(200, userdataList);
    }

    @Get(context = "/users")
    public RestResponse onLimitedUsersGet(@RestParam("limit") long limit) {
        return RestResponse.createOnlyBody(200, userdataList.stream().limit(limit).collect(Collectors.toList()));
    }

    @Post(context = "/adduser")
    public RestResponse onUserAdd(@NonNull RestRequestMessage message) {
        Userdata newUserdata = message.getMessageAsJsonObject(Userdata.class);

        userdataList.add(newUserdata);

        message.setMessage("Successfully added");
        return RestResponse.createOnlyBody(200, message);
    }
}
```
---

## PLEASE, SUPPORT ME


By clicking on this link, you can support me as a 
developer and motivate me to develop new open-source projects!

<a href="https://www.buymeacoffee.com/itzstonlex" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
