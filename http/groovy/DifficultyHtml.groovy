import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class DifficultyHtml extends IndexHtml {
    protected static def jsFiles= ['http/js/jquery-1.9.1.min.js', 
        'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}']

    public DifficultyHtml() {
        super()
    }

    protected String dashboardVisualization(def queries) {
        """
        google.setOnLoadCallback(drawDashboard);
        function drawDashboard() {
            var dashboard= new google.visualization.Dashboard(document.getElementById('dashboard_div'));
            var data= new google.visualization.DataTable(${dataJsonObj.generatePage(reader, queries)});

            var donutRangeSlider= new google.visualization.ControlWrapper({
                'controlType': 'CategoryFilter',
                'containerId': 'filter_div',
                'options': {
                  'filterColumnLabel': 'Wave',
                  'ui': {
                    'labelStacking': 'horizontal',
                    'allowTyping': false,
                    'allowMultiple': true
                    }
                }
            });
            var pieChart= new google.visualization.ChartWrapper({
                'chartType': 'LineChart',
                'containerId': 'chart_div',
                'options': {
                  'width': 1680,
                  'height': 1050,
                  'legend': 'right',
                  'pointSize': 5
                }
            });
            dashboard.bind(donutRangeSlider, pieChart);
            dashboard.draw(data);
        }
    """
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        this.reader= reader

        htmlBuilder.html() {
            htmlBuilder.head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title("KFStatsX")

                link(rel:'shortcut icon', href: 'http/ico/favicon.ico')
                
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                htmlBuilder.script(type: 'text/javascript') {
                    mkp.yieldUnescaped(dashboardVisualization(queries))
                }
                
            }
            body() {
                div(id:'dashboard_div') {
                    div(id:'filter_div', '')
                    div(id:'chart_div', '')
                }
            }
        }
        return "<!DOCTYPE HTML>\n$writer"
    }
}