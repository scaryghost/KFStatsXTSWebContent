import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class LevelDataHtml extends IndexHtml {
    public LevelDataHtml() {
        super()
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

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
                script(type: 'text/javascript') {
                    mkp.yieldUnescaped(generateCommonJs([[new DataJson().generatePage(reader, queries), 'Level Data', "leveldata_div", null]]))
                }
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav', '')
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            div(id: 'leveldata_div', class:'contentbox', '')
                        }
                    }
                }
            }
        }
        return "<!DOCTYPE HTML>\n$writer"
    }
}
