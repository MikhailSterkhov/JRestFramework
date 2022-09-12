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

import java.util.UUID;

@RestService
@RestStructure(url = "localhost:8080", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    @RestRequest(method = "POST", context = "/users")
    RestResponse addUser(
            @RestParam("uuid") UUID uuid,
            @RestParam("name") String username
    );

    @RestRequest(method = "DELETE", context = "/users")
    RestResponse deleteUser(
            @RestParam("id") long id
    );

    @RestRequest(method = "GET", context = "/users", useSignature = false)
    RestResponse getUsers();
}
```

Tests REST-service structure:
```java
TestRestService testRestService = restStorage.get(TestRestService.class);

RestResponse deleteResponse = testRestService.deleteUser(1);

System.out.println(deleteResponse.getStatusCode());
System.out.println(deleteResponse.getUrl());

System.out.println(testRestService.getUsers().getUrl());
System.out.println(testRestService.addUser(UUID.randomUUID(), "itzstonlex").getUrl());
```
Console Output Example:
```shell
200
localhost:8080/users?id=1
localhost:8080/users
localhost:8080/users?uuid=5783a104-cc4c-4304-a117-a16ff73a19ce&name=itzstonlex
```

---

## PLEASE, SUPPORT ME


By clicking on this link, you can support me as a 
developer and motivate me to develop new open-source projects!

<a href="https://www.buymeacoffee.com/itzstonlex" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
