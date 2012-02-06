/*
 * Copyright 2007 Peter Ledbrook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 */
package org.codehaus.groovy.grails.plugins.gwt;

import java.io.IOException;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet filter that ensures that the *.nocache.js files generated
 * by GWT are not cached by the browser or a proxy. The source has been
 * taken from <a href="http://seewah.blogspot.com/2009/02/gwt-tips-2-nocachejs-getting-cached-in.html">
 * this blog</a>.
 */
public class GwtCacheControlFilter implements Filter {
    public void destroy() {}
    public void init(FilterConfig config) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.contains(".nocache.")) {
            long now = new Date().getTime();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setDateHeader("Date", now);
            httpResponse.setDateHeader("Expires", now - 86400000L);  // One day ago.
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        } else if (requestURI.contains(".cache.")) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Cache-Control", "public");
            httpResponse.setHeader("Cache-Control", "max-age=29030400");
            long oneYearFromNow = System.currentTimeMillis() + 31536000000l;
            httpResponse.setDateHeader("Expires", oneYearFromNow);
            httpResponse.setDateHeader("max-age", oneYearFromNow);
        }
        filterChain.doFilter(request, response);
    }
}
