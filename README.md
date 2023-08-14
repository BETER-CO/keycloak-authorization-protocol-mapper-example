# keycloak-authorization-protocol-mapper-example / customize JWT tokens
This repository is inspired by [Meinert Schwartau](https://github.com/mschwartau/keycloak-custom-protocol-mapper-example.git).

Per default [Keycloak](https://www.keycloak.org/) writes a lot of things into the [JWT tokens](https://tools.ietf.org/html/rfc7519),
e.g. the preferred username. If that is not enough, a lot of additional built in protocol mappers can be added to customize
the [JWT token](https://tools.ietf.org/html/rfc7519) created by [Keycloak](https://www.keycloak.org/) even further. They can be added in the client
section via the mappers tab (see the [documentation](https://www.keycloak.org/docs/latest/server_admin/index.html#_protocol-mappers)). But sometimes the build
in protocol mappers are not enough. If this is the case, an own protocol mapper can be added to [Keycloak](https://www.keycloak.org/) via an (not yet)
official [service provider API](https://www.baeldung.com/java-spi). This project shows how this can be done.

## Entrypoints into this project

1. [protocol-mapper](protocol-mapper): Contains the protocol mapper code. The resulting jar file will be deployed to [Keycloak](https://www.keycloak.org/). I
   tried to explain things needed in comments in the [protocol-mapper project](protocol-mapper)
2. [docker-compose](docker-compose.yml): Adds the jar file containing the [protocol mapper](protocol-mapper/src/main/java/beter/AggregatedPermissionsMapper.java), created
   by the [protocol-mapper project](protocol-mapper), to the keycloak instance.

## Try it out

### Prerequisites

1. [Java](https://www.java.com/en/) + [Maven](https://maven.apache.org/)
2. [Docker](https://www.docker.com/)

### Running the keycloak

1. If you have already started this project and changed something, execute `docker-compose down -v` so
   that the volumes and so on are destroyed. Otherwise the old keycloak postgres
   database might be reused or you might not see your changed data.
2. Start build and start/restart keycloak using docker: `sh run.sh`.
3. Now you can open the [Keycloak admin console](http://localhost:8080/admin/master/console/) and login with username / password: admin / admin.
   This initial password for the admin user were configured in our [docker-compose](docker-compose.yml) file.

### Configuration of keycloak (once)
1. Create client `example-client`
   ![Create client 1 step screenshot](images/create_client_1.PNG?raw=true "Create client 1 step screenshot")
   ![Create client 2 step screenshot](images/create_client_2.PNG?raw=true "Create client 2 step screenshot")
   ![Create client 3 step screenshot](images/create_client_3.PNG?raw=true "Create client 3 step screenshot")
   ![Create client 4 step screenshot](images/create_client_4.PNG?raw=true "Create client 4 step screenshot")
2. Add protocol mapper to `example-client`
   ![Add protocol mapper to client 1 step screenshot](images/add_protocol_mapper_1.PNG?raw=true "Add protocol mapper to client 1 step screenshot")
   ![Add protocol mapper to client 2 step screenshot](images/add_protocol_mapper_2.PNG?raw=true "Add protocol mapper to client 2 step screenshot")
   ![Add protocol mapper to client 3 step screenshot](images/add_protocol_mapper_3.PNG?raw=true "Add protocol mapper to client 3 step screenshot")
   ![Add protocol mapper to client 4 step screenshot](images/add_protocol_mapper_4.PNG?raw=true "Add protocol mapper to client 4 step screenshot")
3. Add role `resources-allowed-role` the client `example-client`
4. Add group `example-group` to the realm `master`. Add user `admin` role `resources-allowed-role` to this group
5. Add authorization scopes `view` and `edit` to the `exmple-client`
   ![Add authz scope to client 1 step screenshot](images/add_authz_scope_1.PNG?raw=true "Add authz scope to client 1 step screenshot")
   ![Add authz scope to client 2 step screenshot](images/add_authz_scope_2.PNG?raw=true "Add authz scope to client 2 step screenshot")
6. Add resources `resource1` and `resource2` of type `resource` with scopes `view` and `edit` to the client `example-client`.
   ![Add resources to client 1 step screenshot](images/add_resources_1.PNG?raw=true "Add resources to client 1 step screenshot") 
   ![Add resources to client 2 step screenshot](images/add_resources_2.PNG?raw=true "Add resources to client 2 step screenshot") 
7. Add authorization policy `allowed-by-role-policy` to the client `example-client`. 
   ![Add policy to client 1 step screenshot](images/add_policy_1.PNG?raw=true "Add policy to client 1 step screenshot") 
   ![Add policy to client 2 step screenshot](images/add_policy_2.PNG?raw=true "Add policy to client 2 step screenshot") 
8. Add scope-based permission to the client `example-client`. 
   ![Add permission to client 1 step screenshot](images/add_permission_1.PNG?raw=true "Add permission to client 1 step screenshot") 
   ![Add permission to client 2 step screenshot](images/add_permission_2.PNG?raw=true "Add permission to client 2 step screenshot") 

Now [Keycloak](https://www.keycloak.org/) is configured. As a next step we want to check the token.

### Updating the mapper
1. Start build and restart keycloak using docker: `sh run.sh`.

### Checking the access token

To check the token, we need to login. To get the tokens using the direct flow (not recommended for production usage, just for easy demo purposes. See
this [page](https://auth0.com/docs/api-auth/which-oauth-flow-to-use)) execute the following curl command:
    
    curl -X POST -k -H 'Content-Type: application/x-www-form-urlencoded' -i 'http://localhost:8080/realms/master/protocol/openid-connect/token' --data 'username=admin&password=admin&client_id=example-client&grant_type=password&client_secret=<client_secret_from_keycloak_ui>'
![Copy client secret screenshot](images/client_secret.PNG?raw=true "Copy client secret screenshot") 

Note that using the direct flow is only possible because we configured keycloak to allow it in
the client's settings.
Response should be like:

    {
        "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJydkM1bHNGRmxqaEFqWVoweGYzdlQtZklMdFpwMkdLRUF6ZXYtT3cwcWFvIn0.eyJleHAiOjE2OTIwMDUzMzUsImlhdCI6MTY5MjAwNTI3NSwianRpIjoiZWU5NGQ4MmItNTRjMy00OWZkLTlkYjEtNjNiMmFhZmRiNGIwIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOlsibWFzdGVyLXJlYWxtIiwiYWNjb3VudCJdLCJzdWIiOiJjNTA4YWZkOC03ZWRhLTQ2MzQtOTU2My01ZDc4Y2ZiMGIwYmEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJleGFtcGxlLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiJjMmIxMzJkZS0yMjc4LTRmZGMtYTQ5OC1kNTJkZDc3Y2U5MWIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImV4YW1wbGUtY2xpZW50Ijp7InJvbGVzIjpbInJlc291cmNlcy1hbGxvd2VkLXJvbGUiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJxdWVyeS1yZWFsbXMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJxdWVyeS1jbGllbnRzIiwicXVlcnktdXNlcnMiLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiYzJiMTMyZGUtMjI3OC00ZmRjLWE0OTgtZDUyZGQ3N2NlOTFiIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwZXJtaXNzaW9ucyI6WyJyZXNvdXJjZTI6dmlldyIsInJlc291cmNlMjplZGl0IiwicmVzb3VyY2UxOnZpZXciLCJyZXNvdXJjZTE6ZWRpdCJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiJ9.S0MUvsvAukCqI1rh_q6W0Y3pNR8gnfp_I16FUTnHP6DA9ytd7fdl69gfb3rGA1H_DihflO1NymhPlOL7U5IhKR6l-BdMUSV329KbBB8k4wqCK6VHQ2K1w95ktjhhalvPncGCkyF4k-oehKyG3aGEe1iwhpKbkRsuw-tZrZbuAx2E0jgdYGzx0awXOieefjYInB7Q99uL7dS1B0uMnExnT7t5J7qopBg7Sp610qoejtpr1MvuAW-PwOIFc3Vbkzs2I6aZ_s_alc9zVGuDnJ6P3kH80_18W1rKJ0hYKyQ5yn9NFUZh3ltpQCs5fEjkRXrRcNtvrFxb0hXi_AVGx64qJA",
        "expires_in":60,
        "refresh_expires_in":1800,
        "refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5MDUzNzI4OS00NzM3LTQyMjctYTRmYi0wOTQ4ZDkxZDkyY2UifQ.eyJleHAiOjE2OTIwMDcwNzUsImlhdCI6MTY5MjAwNTI3NSwianRpIjoiMzQxZTFmZjAtNTZjNy00MTU5LWE3OTYtYjVlMWEzOTA3MGI4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvcmVhbG1zL21hc3RlciIsInN1YiI6ImM1MDhhZmQ4LTdlZGEtNDYzNC05NTYzLTVkNzhjZmIwYjBiYSIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJleGFtcGxlLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiJjMmIxMzJkZS0yMjc4LTRmZGMtYTQ5OC1kNTJkZDc3Y2U5MWIiLCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiJjMmIxMzJkZS0yMjc4LTRmZGMtYTQ5OC1kNTJkZDc3Y2U5MWIifQ._OzAUXP_EiIsO32A2z7tuiQpEOd2Si4qn9YD4gxS178",
        "token_type":"Bearer",
        "not-before-policy":0,
        "session_state":"c2b132de-2278-4fdc-a498-d52dd77ce91b",
        "scope":"profile email"
    }

Then copy the `access_token` value and decode it, e.g. by using [jwt.io](https://jwt.io/). You'll
get something like the following:

    {
        "exp": 1692005335,
        "iat": 1692005275,
        "jti": "ee94d82b-54c3-49fd-9db1-63b2aafdb4b0",
        "iss": "http://localhost:8080/realms/master",
        "aud": [
            "master-realm",
            "account"
        ],
        "sub": "c508afd8-7eda-4634-9563-5d78cfb0b0ba",
        "typ": "Bearer",
        "azp": "example-client",
        "session_state": "c2b132de-2278-4fdc-a498-d52dd77ce91b",
        "acr": "1",
        "allowed-origins": [
            "/*"
        ],
        "realm_access": {
            "roles": [
            "create-realm",
            "default-roles-master",
            "offline_access",
            "admin",
            "uma_authorization"
            ]
        },
        "resource_access": {
            "example-client": {
            "roles": [
                "resources-allowed-role"
            ]
            },
            "master-realm": {
            "roles": [
                "view-realm",
                "view-identity-providers",
                "manage-identity-providers",
                "impersonation",
                "create-client",
                "manage-users",
                "query-realms",
                "view-authorization",
                "query-clients",
                "query-users",
                "manage-events",
                "manage-realm",
                "view-events",
                "view-users",
                "view-clients",
                "manage-authorization",
                "manage-clients",
                "query-groups"
            ]
            },
            "account": {
            "roles": [
                "manage-account",
                "manage-account-links",
                "view-profile"
            ]
            }
        },
        "scope": "profile email",
        "sid": "c2b132de-2278-4fdc-a498-d52dd77ce91b",
        "email_verified": false,
        "permissions": [
            "resource2:view",
            "resource2:edit",
            "resource1:view",
            "resource1:edit"
        ],
        "preferred_username": "admin"
    }

The value of our own [Aggregated Permissions Token mapper](protocol-mapper/src/main/java/beter/AggregatedPermissionsMapper.java) got added to the token because
the array of permissions appears in the `permissions` field.

## Acknowledgements

- Examples for [Keycloak](https://www.keycloak.org/): https://github.com/keycloak/keycloak/tree/master/examples


## Links

- To use keycloak with an angular app, I found this example app to be helpful: https://github.com/manfredsteyer/angular-oauth2-oidc
- Login Page for the users: Login Url: [http://localhost:8080/realms/master/account](http://localhost:8080/realms/master/account)


