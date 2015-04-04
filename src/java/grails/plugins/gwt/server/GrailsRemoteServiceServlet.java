package grails.plugins.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

/**
 * Used to overcome the problem with grails resource plugin conflict with GWT
 *
 * GWT will use the URL to find resources, but the url has /static in it.
 *
 * issue#62 https://github.com/simplicityitself/grails-gwt/issues/62
 *
 * Created by ryan on 15-04-04.
 */
public class GrailsRemoteServiceServlet extends RemoteServiceServlet {
    Logger logger = LoggerFactory.getLogger(GrailsRemoteServiceServlet.class);
    /**
     * Strip off the /static from the beginning of a module url, then pass to the super's implementation
     *
     * issue#62
     *
     * Does not affect other uses of static
     *
     * Borrow's from loadSerializationPolicy
     *
     * @param request
     * @param moduleBaseURL
     * @param strongName
     * @return
     */
    @Override
    protected SerializationPolicy doGetSerializationPolicy(
            HttpServletRequest request, String moduleBaseURL, String strongName) {
        //moduleBaseURL.replace("/static", "") is so much easy(lazy)
        String returnURL = moduleBaseURL;
        logger.debug("doGetSerializationPolicy url:" + moduleBaseURL);
        try{
            URL url = new URL(moduleBaseURL);
            String host = url.getHost();
            int port = url.getPort();
            String protocol = url.getProtocol();
            String basePath = url.getPath();
            logger.debug("basePath:" + basePath);
            if(basePath.startsWith("/static")){
                logger.debug("found static!");
                basePath = basePath.replaceFirst("/static", "");
                URL newUrl = new URL(protocol, host, port, basePath);
                returnURL = newUrl.toString();
            }

        } catch(Exception e) { // default implementation will handle this
            logger.error("Exception in GrailsRemoteService Servlet parsing url" + e.getMessage());
        }
        return super.doGetSerializationPolicy(request, returnURL, strongName);

    }
}
