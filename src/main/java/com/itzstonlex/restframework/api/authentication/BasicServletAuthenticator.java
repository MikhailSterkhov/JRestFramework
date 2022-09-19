package com.itzstonlex.restframework.api.authentication;

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
        return authentication.username().equals(username) && authentication.password().equals(password);
    }
}
