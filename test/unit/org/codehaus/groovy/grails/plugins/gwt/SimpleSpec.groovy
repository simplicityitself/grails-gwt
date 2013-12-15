package org.codehaus.groovy.grails.plugins.gwt

import spock.lang.Specification

class SimpleSpec extends Specification {


  def "My Test"() {

    expect:
    1 * 5 == ("Some" + "Text").size()

  }

}
