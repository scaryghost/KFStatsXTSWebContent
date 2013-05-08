import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class IndexHtml implements Resource {
    protected def visualizations, navLeft, navRight, dataJsonObj, reader
    protected def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']
    protected static def jsFiles= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js', 
            'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}']
    protected static def stylesheets= ['http/css/kfstatsxHtml.css']
    protected static def scrollingJs= """
        //Div scrolling js taken from http://gazpo.com/2012/03/horizontal-content-scroll/
        function goto(id){   
            //animate to the div id.
            \$(".contentbox-wrapper").animate({"left": -(\$(id).position().left)}, 600);
        }
    """
    protected static def jsChartCommon= """
        google.setOnLoadCallback(visualizationCallback);

        function drawChart(data, title, divId, chartType) {
            var chart= new google.visualization.ChartWrapper({'chartType': chartType, 'containerId': divId, 'options': {
                'chartArea': {height: '90%'},
                'vAxis': {textStyle: {fontSize: 15}},
                'allowHtml': true
            }});
            chart.setDataTable(data);
            chart.setOption('title', title);
            chart.setOption('height', Math.max(chart.getDataTable().getNumberOfRows() * 25, document.getElementById(divId).offsetHeight * 0.975));
            chart.setOption('width', document.getElementById(divId).offsetWidth * 0.985);
            chart.draw();
        }
        function visualizationCallback() {
"""

    public IndexHtml() {
        visualizations= [:]
        visualizations["totals"]= {queries, category, type -> replaceHtml(queries, category)}
        navLeft= ["totals", "difficulties", "levels"]
        navRight= []
        dataJsonObj= new DataJson()
    }

    protected String replaceHtml(def queries, def category) {
        """
        google.setOnLoadCallback(draw${category});
        function draw${category}() {
            document.getElementById('${category}_div').innerHTML= "${new DataHtml().generatePage(reader, queries)}";
        }
    """
    }

    protected String generateCommonJs(def parameters) {
        def chartCalls= ""
        parameters.each {param ->
            def chartType= param[3] == null ? 'Table' : param[3]
            chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', '${chartType}');\n"
        }
        return "$jsChartCommon$chartCalls        }\n    "
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def nav= navLeft.plus(reader.getAggregateCategories()).plus(navRight)
        this.reader= reader
        
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
                script(type:'text/javascript', scrollingJs)

                def stndChartsParams= []
                nav.each {navItem ->
                    queries["table"]= navItem
                    if (visualizations[navItem] != null) {
                        script(type: 'text/javascript') {
                            mkp.yieldUnescaped(visualizations[navItem](queries, navItem, chartTypes[navItem]))
                        }
                    } else {
                        stndChartsParams << [dataJsonObj.generatePage(reader, queries), navItem, "${navItem}_div", chartTypes[navItem]]
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
