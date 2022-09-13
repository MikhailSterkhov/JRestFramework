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

**FULL DOCUMENTATION LINK: [[Wiki Page]](https://github.com/ItzStonlex/JExecutionLib/wiki)**

---

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

A simple example of REST-service structure:

```java
import com.itzstonlex.restframework.api.*;
import lombok.NonNull;

@RestService
@RestStructure(url = "https://api.agify.io", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @RestRequest(method = "GET", context = "/")
    Userdata getUserdata(@RestParam("name") String name);

    /**
     * And this function returns a direct HTTP result after
     * executing the request with all the native data
     *
     * @param name - Name of user.
     */
    @RestRequest(method = "GET", context = "/", timeout = 1000)
    RestResponse getUserdataResponse(@RestParam("name") String name);

    /**
     * The request body can be created using the 
     * {@link com.itzstonlex.restframework.api.RestRequestMessage} factory, 
     * as shown in this example
     */
    @RestRequest(method = "POST", context = "/add", useSignature = false)
    RestResponse addUserdata(@NonNull RestRequestMessage postMessage);
}
```

_P.S.: And also no one forbids not using the method signature at all_

Tests REST-service structure:
```java
TestRestService testRestService = restStorage.get(TestRestService.class);

// Get an user-datas.
Userdata meelad = testRestService.getUserdata("meelad");
RestResponse meeladNative = testRestService.getUserdataResponse("meelad");

// Print tests in console.
System.out.println(meelad);
System.out.println(meeladNative);

// Add new user-data.
RestResponse postResponse = testRestService.addUserdata(
        RestRequestMessage.asJson(new Userdata("itzstonlex", 18, 5)));

if (postResponse.getResponseCode() == 200) {
    // Success POST request execution logic.
}
```
Console Output Example:
```shell
Userdata(name=meelad, age=29, count=21)
RestResponse(responseCode=200, responseMessage=OK, url=https://api.agify.io/?name=meelad, body={"name":"meelad","age":29,"count":21}, method=GET)
```

---

## PLEASE, SUPPORT ME


By clicking on this link, you can support me as a 
developer and motivate me to develop new open-source projects!

<a href="https://www.buymeacoffee.com/itzstonlex" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
