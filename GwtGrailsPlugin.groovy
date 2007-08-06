import org.codehaus.groovy.grails.plugins.support.GrailsPluginUtils

class GwtGrailsPlugin {
    def version = '0.1'
    def author = 'Peter Ledbrook'
    def authorEmail = 'peter@cacoethes.co.uk'
    def title = 'The Google Web Toolkit for Grails.'
    def description = '''\
Incorporates GWT into Grails. In particular, GWT host pages can be
GSPs and standard Grails services can be used to handle client RPC
requests.
'''
    def documentation = 'http://grails.codehaus.org/GWT+Plugin'

    def grailsVersion = GrailsPluginUtils.grailsVersion
    def dependsOn = [:]

    def doWithSpring = {
    }   

    def doWithApplicationContext = { applicationContext ->
    }

    def doWithWebDescriptor = { xml ->
        xml.servlet[0] + {
            servlet {
                'servlet-name'('GwtRpcServlet')
                'servlet-class'('org.codehaus.groovy.grails.plugins.gwt.GrailsRemoteServiceServlet')
            }
        }

        xml.'servlet-mapping'[0] + {
            'servlet-mapping' {
                'servlet-name'('GwtRpcServlet')
                'url-pattern'('/gwt/rpc')
            }
        }
    }                                      

    def doWithDynamicMethods = { ctx ->
    }

    def onChange = { event ->
    }                                                                                  

    def onApplicationChange = { event ->
    }
}
