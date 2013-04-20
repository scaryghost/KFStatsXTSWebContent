import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class IndexJs implements Resource {

    private String simpleVisualization(def category, def type) {
"""google.setOnLoadCallback(draw${category});
function draw${category}() {
    var data= new google.visualization.DataTable(${new DataJson().generatePage(reader, [table: '${category}']});
    var chart= new google.visualization.ChartWrapper({'chartType': '$type', 'containerId': '${category}_div', 'options': {allowHtml: true}});
    chart.setDataTable(data);
    chart.setOption('title', '${category}');
    chart.setOption('height', document.getElementById('${category}_div').offsetHeight * 0.975);
    chart.setOption('width', document.getElementById('${category}_div').offsetWidth * 0.985);
    chart.draw();
}"""
    }

    private String resizedVisualization(def category, def type, def options) {
"""google.setOnLoadCallback(draw${category});
function draw${category}() {
    var data= new google.visualization.DataTable(${new DataJson().generatePage(reader, [table: '${category}']});
    var chart= new google.visualization.ChartWrapper({'chartType': '$type', 'containerId': '${category}_div', 'options': {
        'legend': {position: 'none'},
        'chartArea': {height: '90%'},
        'vAxis': {textStyle: {fontSize: 15}}
    }});
    chart.setDataTable(data);
    chart.setOption('title', '$category');
    chart.setOption('height', chart.getDataTable().getNumberOfRows() * 25);
    chart.setOption('width', document.getElementById('${category}_div').offsetWidth * 0.985);
    chart.draw();
}"""

    }
    public String generatePage(DataReader reader, Map<String, String> queries) {
        """document.getElementById('totals_div').innerHTML= ${new DataHtml().generatePage(reader, [table: 'totals']};
${simpleVisualization('difficulties', 'Table')}
${simpleVisualization('levels', 'Table')}
${simpleVisualization('levels', 'BarChart')}
"""
    
    }
}
