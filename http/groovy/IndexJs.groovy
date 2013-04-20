import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class IndexJs implements Resource {
    private def visualizations= [records: {reader, category, type -> pagedTable(reader, category)}, weapons: {reader, category, type -> resizedVisualization(reader, category, type)}, 
        kills: {reader, category, type -> resizedVisualization(reader, category, type)}, deaths: {reader, category, type -> resizedVisualization(reader, category, type)}, 
        perks: {reader, category, type -> simpleVisualization(reader, category, type)}]
    private def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']

    private String simpleVisualization(def reader, def category, def type) {
"""google.setOnLoadCallback(draw${category});
function draw${category}() {
    var data= new google.visualization.DataTable(${new DataJson().generatePage(reader, [table: category])});
    var chart= new google.visualization.ChartWrapper({'chartType': '$type', 'containerId': '${category}_div', 'options': {allowHtml: true}});
    chart.setDataTable(data);
    chart.setOption('title', '${category}');
    chart.setOption('height', document.getElementById('${category}_div').offsetHeight * 0.975);
    chart.setOption('width', document.getElementById('${category}_div').offsetWidth * 0.985);
    chart.draw();
}
"""
    }

    private String resizedVisualization(def reader, def category, def type) {
"""google.setOnLoadCallback(draw${category});
function draw${category}() {
    var data= new google.visualization.DataTable(${new DataJson().generatePage(reader, [table: category])});
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
}
"""
    }

    private String pagedTable(def reader, def category) {
        def queries= ["table=$category"]
"""
var page= 0, pageSize= 25, group="none", order= "ASC";
var data, chart;

function buildQuery() {
    return ["page=" + page, "rows=" + pageSize, "group=" + group, "order=" + order].join('&');
}
function buildDataTable() {
    return new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}&" + buildQuery(), dataType:"json", async: false}).responseText);
}
google.setOnLoadCallback(drawVisualization);
function drawVisualization() {
    data= buildDataTable();
    chart= new google.visualization.ChartWrapper({'chartType': 'Table', 'containerId': '${category}_div', 
        'options': {
            'page': 'event',
            'sort': 'event',
            'pageSize': pageSize,
            'pagingButtonsConfiguration': 'both',
            'showRowNumber': true,
            'allowHtml': true,
            'height': document.getElementById('${category}_div_outer').offsetHeight * 0.925,
            'width': document.getElementById('${category}_div_outer').offsetWidth * 0.985
        }
    });

    google.visualization.events.addListener(chart, 'ready', onReady);
    chart.setDataTable(data);
    chart.draw();

    function onReady() {
        google.visualization.events.addListener(chart.getChart(), 'page', function(properties) {
            page+= parseInt(properties['page'], 10);
            if (page < 0) {
                page= 0;
            }

            data= buildDataTable();
            if (data.getNumberOfRows() == 0) {
                page--;
            } else {
                chart.setOption('firstRowNumber', pageSize * page + 1);
                chart.setDataTable(data);
                chart.draw();
            }
        });
        google.visualization.events.addListener(chart.getChart(), 'sort', function(properties) {
            order= properties["ascending"] ? "asc" : "desc";
            group= data.getColumnLabel(properties["column"]);
            data= buildDataTable();
            chart.setOption('sortColumn', properties["column"]);
            chart.setOption('sortAscending', properties["ascending"]);
            chart.setDataTable(data);
            chart.draw();
        });
    }
}
function updatePageSize(newSize) {
    pageSize= newSize;
    data= buildDataTable();
    chart.setDataTable(data);
    chart.setOption('pageSize', pageSize);
    chart.draw();
}
"""

    }
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def nav= ["totals", "difficulties", "levels", "deaths"].plus(reader.getAggregateCategories()) << "records"
        def js= ""

        nav.each {
            if (visualizations[it] != null) {
                if (chartTypes[it] != null) {
                    js+= visualizations[it](reader, it, chartTypes[it])
                } else {
                    js+= visualizations[it](reader, it, 'Table')
                }
            } else {
                js+= simpleVisualization(reader, it, 'Table')
            }
            js+= "\n"
        }

        return js
    }
}
