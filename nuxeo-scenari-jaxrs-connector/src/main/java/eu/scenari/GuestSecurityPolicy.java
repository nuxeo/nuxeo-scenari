package eu.scenari;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class GuestSecurityPolicy extends AbstractSecurityPolicy {

    protected String anonymousId = null;

    protected static List<String> PERMISSIONS = Arrays.asList(READ,
            READ_CHILDREN, READ_VERSION, READ_LIFE_CYCLE, BROWSE);

    @Override
    public Access checkPermission(Document document, ACP acp,
            Principal principal, String permission,
            String[] resolvedPermissions, String[] additionalPrincipals) {
        if (!principal.getName().equals(anonymousId())) {
            return null;
        }

        if (!document.isVersion()) {
            return null;
        }

        return PERMISSIONS.contains(permission) ? Access.GRANT : Access.DENY;
    }

    protected String anonymousId() {
        try {
            if (anonymousId == null) {
                anonymousId = Framework.getLocalService(UserManager.class).getAnonymousUserId();
            }
            return anonymousId;
        } catch (ClientException e) {
            return null;
        }
    }
}
