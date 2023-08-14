package beter;

import org.junit.jupiter.api.Test;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AggregatedPermissionsMapperTest {

    static final String CLAIM_NAME = "haandlerIdClaimNameExample";

    @Test
    public void shouldTokenMapperDisplayCategory() {
        final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
        assertThat(new AggregatedPermissionsMapper().getDisplayCategory()).isEqualTo(tokenMapperDisplayCategory);
    }

    @Test
    public void shouldHaveDisplayType() {
        assertThat(new AggregatedPermissionsMapper().getDisplayType()).isNotBlank();
    }

    @Test
    public void shouldHaveHelpText() {
        assertThat(new AggregatedPermissionsMapper().getHelpText()).isNotBlank();
    }

    @Test
    public void shouldHaveIdId() {
        assertThat(new AggregatedPermissionsMapper().getId()).isNotBlank();
    }

    @Test
    public void shouldHaveProperties() {
        final List<String> configPropertyNames = new AggregatedPermissionsMapper().getConfigProperties().stream()
                .map(ProviderConfigProperty::getName)
                .collect(Collectors.toList());
        assertThat(configPropertyNames).containsExactly(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO);
    }

    @Test
    public void shouldAddClaim() {
        final UserSessionModel session = givenUserSession();

        final AccessToken accessToken = transformAccessToken(session);

        assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo(null);
    }

    private UserSessionModel givenUserSession() {
        UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
        UserModel user = Mockito.mock(UserModel.class);
        when(userSession.getUser()).thenReturn(user);
        return userSession;
    }


    private AccessToken transformAccessToken(UserSessionModel userSessionModel) {
        final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
        mappingModel.setConfig(createConfig());
        return new AggregatedPermissionsMapper().transformAccessToken(new AccessToken(), mappingModel, null, userSessionModel, null);
    }

    private Map<String, String> createConfig() {
        final Map<String, String> result = new HashMap<>();
        result.put("access.token.claim", "true");
        result.put("claim.name", CLAIM_NAME);
        return result;
    }
}