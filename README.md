# JRestFramework
Create a REST project services structure with a JRestFramework

## WARNING

This project is no longer supported!
A new version is available in a separate repository: [jrest2](https://github.com/MikhailSterkhov/jrest2)

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
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.context.RestBody;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.context.response.RestResponse;

@RestService
@RestClient(url = "http://localhost:8082/api")
@RestAuthentication(username = "admin", password = "${system.rest.auth.password}")
@RestOption(RestOption.Type.ASYNCHRONOUS)
@RestOption(RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)
public interface RestClientTest {

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @Get(context = "/user")
    @Header(name = "Content-Type", value = "application/json")
    Userdata getUserdata(@RestParam("name") String name);

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (List)
     *
     * @param limit - Limit of users list size
     */
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
     * The request body can be created using the
     * {@link com.itzstonlex.restframework.api.context.RestBody}
     * factory, as shown in this example
     */
    @Post(context = "/adduser", useSignature = false)
    @Header(name = "Content-Type", value = "application/json")
    @Header(name = "Auth-Token", value = "${system.rest.auth.token}", operate = Header.Operation.ADD)
    RestResponse addUserdata(@RestParam RestBody body);
}
```

_P.S.: And also no one forbids not using the method signature at all_

---

### REST SERVER

A simple example of REST-server structure:

```java
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.authentication.RestAuthResult;
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.authentication.RestAuthenticationResult;
import com.itzstonlex.restframework.api.context.RestBody;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.context.request.RestRequestContext;
import com.itzstonlex.restframework.api.context.response.Responses;
import com.itzstonlex.restframework.api.context.response.RestResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestService
@RestServer(host = "localhost", port = 8082, defaultContext = "/api")
@RestAuthentication(username = "admin", password = "${system.rest.auth.password}")
@RestOption(RestOption.Type.ASYNCHRONOUS)
@RestOption(RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)
public class RestServerTest {

    private static final int NOT_FOUND_ERR = (Responses.BAD_REQUEST + 4);

    private static final String TOKEN_HEADER = "Auth-Token";

    private final List<Userdata> userdataList;

    public RestServerTest(List<Userdata> userdataList) {
        this.userdataList = userdataList;
    }

    @Get(context = "/users", timeout = 200)
    public RestResponse onUsersGet() {
        return Responses.ofJSON(Responses.OK, userdataList);
    }

    @Get(context = "/users")
    public RestResponse onLimitedUsersGet(@RestParam("limit") long limit) {
        return Responses.ofJSON(Responses.OK, userdataList.stream().limit(limit).collect(Collectors.toList()));
    }

    @Get(context = "/user")
    public RestResponse onUserGet(@RestParam("name") String name) {

        Userdata userdata = userdataList.stream()
                .filter(cached -> cached.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (userdata == null) {
            return Responses.ofText(NOT_FOUND_ERR, "Userdata is not found");
        }

        return Responses.ofJSON(Responses.OK, userdata);
    }

    @Post(context = "/adduser", timeout = 250)
    public RestResponse onUserAdd(@RestParam RestRequestContext context) {
        String tokenHeader = context.getFirstHeader(TOKEN_HEADER);

        if (tokenHeader == null || !tokenHeader.equals(System.getProperty("rest.auth.token"))) {
            throw new IllegalArgumentException(TOKEN_HEADER);
        }

        RestBody body = context.getBody();

        Userdata newUserdata = body.convert(Userdata.class);
        userdataList.add(newUserdata);

        return Responses.ofJSONMessage(Responses.OK, "Successfully added");
    }

    @RestExceptionHandler
    public void onExceptionThrow(IllegalArgumentException exception) {
        System.out.println("Wrong authentication token!");
    }

    @RestAuthenticationResult
    public void onAuthResult(RestAuthResult result) {
        switch (result) {

            case SUCCESS: {
                System.out.println("[Server] Success authorized!");
                break;
            }

            case FAILURE: {
                System.out.println("[Server] Forbidden");
                break;
            }
        }
    }
}
```

---

### TESTING REST-SERVICES

A simple example of initializing your<br>
project for the requirements of this library:

```java
import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestServicePublicManager;

public class Bootstrap {

    public static void main(String[] args) {
        RestServicePublicManager rest = RestFrameworkBootstrap.runServices(Bootstrap.class);
    }
}
```

Example REST services tests:
```java
// Bind a REST-server
rest.bind(RestServerTest.class, new ArrayList<>());

// Get initialized REST-client
RestClientTest restClient = rest.get(RestClientTest.class);

// Add user & print response.
RestBody adduserBody = RestBody.fromConvertedObject(new Userdata("itzstonlex", 18, 3)); 
System.out.println("[Test] " + restClient.addUserdata(adduserBody));

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
[Test] RestResponse(statusCode=200, statusMessage=OK, body={"message":"Successfully added"})
[Test] Userdata(name=itzstonlex, age=18, count=3)
[Test] RestResponse(statusCode=200, statusMessage=OK, body={"name":"itzstonlex","age":18,"count":3})
[Test] [{name=itzstonlex, age=18.0, count=3.0}]
```
---

## PLEASE, SUPPORT ME


By clicking on this link, you can support me as a 
developer and motivate me to develop new open-source projects!

<a href="https://www.buymeacoffee.com/itzstonlex" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
