package eu.scenari;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.ui.web.auth.interfaces.NuxeoAuthenticationPlugin;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class NotAuthorized implements NuxeoAuthenticationPlugin {
    @Override
    public Boolean handleLoginPrompt(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s) {
        httpServletResponse.setStatus(401);
        return true;
    }

    @Override
    public UserIdentificationInfo handleRetrieveIdentity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return null;
    }

    @Override
    public Boolean needLoginPrompt(HttpServletRequest httpServletRequest) {
        return true;
    }

    @Override
    public void initPlugin(Map<String, String> stringStringMap) {

    }

    @Override
    public List<String> getUnAuthenticatedURLPrefix() {
        return null;
    }
}
