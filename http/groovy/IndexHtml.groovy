import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class IndexHtml implements Resource {
    protected def htmlDiv, nav
    protected def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']
    protected static def jsFiles= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js', 
            'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}']
    protected static def stylesheets= ['http/css/kfstatsxHtml.css']
    protected static def jsChartCommon= """
        google.setOnLoadCallback(visualizationCallback);

        function visualizationCallback() {
"""

    public IndexHtml() {
        htmlDiv= new HashSet()
        htmlDiv << "totals"
        nav= ["totals", "difficulties", "levels"]
    }

    protected String generateCommonJs(def parameters) {
        def chartCalls= ""
        parameters.each {param ->
            def chartType= param[3] == null ? 'Table' : param[3]
            if (param[1] == null) {
                chartCalls+= "            replaceHtml(\"${param[0]}\", '${param[2]}');\n"
            } else {
                chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', '${chartType}');\n"
            }
        }
        return """
            ${WebCommon.replaceHtml}
            ${WebCommon.chartJs}
            google.setOnLoadCallback(visualizationCallback);

            function visualizationCallback() {
                $chartCalls
            }
        """
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def dataJson= new DataJson()
        def dataHtml= new DataHtml()
        def nav= nav.plus(reader.getAggregateCategories())
        
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
                script(type:'text/javascript', WebCommon.scrollingJs)

                def stndChartsParams= []
                nav.each {navItem ->
                    queries["table"]= navItem
                    if (htmlDiv.contains(navItem)) {
                        stndChartsParams << [dataHtml.generatePage(reader, queries), null, "${navItem}_div", null]
                    } else {
                        stndChartsParams << [dataJson.generatePage(reader, queries), navItem, "${navItem}_div", chartTypes[navItem]]
                    }
                }
                script(type: 'text/javascript') {
                    mkp.yieldUnescaped(generateCommonJs(stndChartsParams))
                }
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav') {
                        h3("Navigation") {
                            select(onchange:'goto(this.options[this.selectedIndex].value); return false') {
                                nav.each {item ->
                                    def attr= [value: "#${item}_div"]
                                    if (item == nav.first()) {
                                        attr["selected"]= "selected"
                                    }
                                    option(attr, item)
                                }
                            }
                        }
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            nav.each {item ->
                                div(id: item + '_div', class:'contentbox', '')
                            }
                        }
                    }
                }
            }
        }
        return "<!DOCTYPE HTML>\n$writer"
    }
}
