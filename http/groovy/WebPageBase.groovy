import com.github.etsai.kfsxtrackingserver.ReaderWrapper
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class WebPageBase extends Resource {
    public static def defaultJs= """
        //Div scrolling js taken from http://gazpo.com/2012/03/horizontal-content-scroll/
        function goto(id){   
            //animate to the div id.
            \$(".contentbox-wrapper").animate({"left": -(\$(id).position().left)}, 600);
        }

        function getXml() {
            if (window.location.search != "") {
                window.location.href= window.location.href + "&xml=1";
            } else {
                window.location.href= window.location.href + "?xml=1";
            }
        }
    """

    protected def htmlDiv, navigation, categoryMthd, dataDispatcher
    protected def stylesheets= ['css/kfstatsxHtml.css']
    protected def jsFiles= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js',
            'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}']

    public WebPageBase() {
        htmlDiv= new HashSet()
        dataDispatcher= new DataDispatcher()
        navigation= []
    }

    public String getPageTitle() {
        return "KFStatsX"
    }
    protected abstract void buildXml(def builder)
    protected abstract void fillHeader(def builder)
    protected abstract void fillVisualizationJS(def builder)
    protected abstract void fillContentBoxes(def builder)

    public void setDataReader(ReaderWrapper reader) {
        super.setDataReader(reader);
        dataDispatcher.setDataReader(reader);
    }
    public void setQueries(Map<String, String> queries) {
        super.setQueries(queries);
        dataDispatcher.setQueries(queries);
    }
    public String generatePage() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        if (queries.xml != null) {
            buildXml(htmlBuilder)
            return writer
        }
        if (categoryMthd != null) {
            navigation= navigation.plus(reader.executeQuery(categoryMthd))
        }
        htmlBuilder.html() {
            htmlBuilder.head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title(getPageTitle())
                
                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
                link(rel:'shortcut icon', href: 'ico/favicon.ico')
                
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                script(type:'text/javascript') {
                    mkp.yieldUnescaped(defaultJs)
                }
                fillVisualizationJS(htmlBuilder)
            }
            body() {
                div(id:'wrap') {
                    div(id: 'header','') {
                        fillHeader(htmlBuilder)
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            fillContentBoxes(htmlBuilder)
                        }
                    }
                    div(id:"footer") {
                        a(class: 'footer-link', id:'opener', href:'javascript:void(0)', onClick:'javascript:getXml()','View as xml')
                    }
                }
            }
        }
        return String.format("<!DOCTYPE HTML>%n%s", writer)
    }
}
