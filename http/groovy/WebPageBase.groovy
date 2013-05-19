import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class WebPageBase implements Resource {
    protected def drawNav
    protected def htmlDiv, navigation, categoryMthd, dataHtml, dataJson, queries, reader
    protected static def stylesheets= ['http/css/kfstatsxHtml.css', 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css']
    protected static def jsFiles= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js', 'http://code.jquery.com/ui/1.10.3/jquery-ui.js',
            'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}']

    public WebPageBase() {
        htmlDiv= new HashSet()
        dataHtml= new DataHtml()
        dataJson= new DataJson()
        drawNav= true
        navigation= []
    }

    protected abstract void fillNav(def builder)
    protected abstract void fillVisualizationJS(def builder)
    protected abstract void fillContentBoxes(def builder)

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        this.queries= queries
        this.reader= reader

        if (categoryMthd != null) {
            navigation= navigation.plus(reader.class.getDeclaredMethod(categoryMthd).invoke(reader))
        }
        htmlBuilder.html() {
            htmlBuilder.head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title("KFStatsX")
                
                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
                link(rel:'shortcut icon', href: 'http/ico/favicon.ico')
                
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                script(type:'text/javascript', WebCommon.scrollingJs)
                fillVisualizationJS(htmlBuilder)
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav','') {
                        if (drawNav) {
                            h3("Navigation") {
                                fillNav(htmlBuilder)
                            }
                        }
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            fillContentBoxes(htmlBuilder)
                        }
                    }
                    div(id:"footer") {
                        a(id:'opener', href:'javascript:void(0)', 'Click me')
                    }
                }
            }
        }
        return "<!DOCTYPE HTML>\n$writer"
    }
}
