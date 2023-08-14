package beter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.common.UserModelIdentity;
import org.keycloak.authorization.permission.evaluator.Evaluators;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;

/*
 * Our own authorization protocol mapper.
 */
public class AggregatedPermissionsMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    /*
     * A config which keycloak uses to display a generic dialog to configure the
     * token.
     */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our
     * data-setup project to
     * configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "oidc-aggregated-permissions-mapper";

    static {
        // The builtin protocol mapper let the user define under which claim name (key)
        // the protocol mapper writes its value. To display this option in the generic
        // dialog
        // in keycloak, execute the following method.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        // The builtin protocol mapper let the user define for which tokens the protocol
        // mapper
        // is executed (access token, id token, user info). To add the config options
        // for the different types
        // to the dialog execute the following method. Note that the following method
        // uses the interfaces
        // this token mapper implements to decide which options to add to the config. So
        // if this token
        // mapper should never be available for some sort of options, e.g. like the id
        // token, just don't
        // implement the corresponding interface.
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AggregatedPermissionsMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Aggregated Permissions Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds permissions array (resource:scope fromat) to the claim";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(final IDToken token,
            final ProtocolMapperModel mappingModel,
            final UserSessionModel userSession,
            final KeycloakSession keycloakSession,
            final ClientSessionContext clientSessionCtx) {

        var logger = Logger.getLogger(PROVIDER_ID);

        var actions = new ArrayList<String>();

        if (keycloakSession != null) { // will work at real keycloak environment only

            var context = keycloakSession.getContext();
            var client = context.getClient();
            var realm = context.getRealm();
            var user = userSession.getUser();

            logger.info("Start to search for permissions for " + user.getUsername());

            var authorization = keycloakSession.getProvider(AuthorizationProvider.class);

            var resourceServer = authorization.getStoreFactory().getResourceServerStore().findByClient(client);

            var evals = new Evaluators(authorization);
            var request = new AuthorizationRequest();
            var identity = new UserModelIdentity(realm, user);
            var evalContext = new DefaultEvaluationContext(identity, keycloakSession);
            var eval = evals.from(evalContext, resourceServer, request);
            var permissions = eval.evaluate(resourceServer, request);

            for (var permission : permissions) {
                var name = permission.getResourceName();

                for (var scope : permission.getScopes()) {
                    var action = name + ":" + scope;
                    logger.info(user.getUsername() + ": " + action);
                    actions.add(action);
                }
            }

            logger.info(user.getUsername() + " has " + actions.size() + " permissions");
        }        

        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, "true");
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, actions);
    }
}
