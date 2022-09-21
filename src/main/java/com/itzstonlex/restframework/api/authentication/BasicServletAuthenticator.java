package com.itzstonlex.restframework.api.authentication;

import com.itzstonlex.restframework.util.RestUtilities;
import com.sun.net.httpserver.BasicAuthenticator;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true)
public class BasicServletAuthenticator extends BasicAuthenticator {

    private RestAuthentication authentication;
    
    /**
     * Creates a BasicAuthenticator for the given HTTP realm
     *
     * @param realm The HTTP Basic authentication realm
     * @throws NullPointerException if the realm is an empty string
     */
    public BasicServletAuthenticator(RestAuthentication authentication, String realm) {
        super(realm);
        this.authentication = authentication;
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        return RestUtilities.parseSystemProperties(authentication.username()).equals(username)
                && RestUtilities.parseSystemProperties(authentication.password()).equals(password);
    }
}
