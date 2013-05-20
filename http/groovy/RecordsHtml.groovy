import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class RecordsHtml extends WebPageBase {
    protected final def category

    public RecordsHtml() {
        this("records")
    }

    protected RecordsHtml(def category) {
        super()
        this.category= category
    }

    public String getPageTitle() {
        return "${super.getPageTitle()} - Player Records"
    }

    protected void fillHeader(def builder) {
        builder.h3("Player Records")
    }

    protected void fillVisualizationJS(def builder) {
        def ajaxQueries= ["table=$category"]

        queries.each {key, value ->
            ajaxQueries << "$key=$value"
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(pagedTable(ajaxQueries))
        }
    }

    protected void fillContentBoxes(def builder) {
        builder.div(id:category + '_div_outer', class:'contentbox') {
            form(action:'', 'Number of rows:') {
                select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                    option(selected:"selected", value:'25', '25')
                    option(value:'50', '50')
                    option(value:'100', '100')
                    option(value:'250', '250')
                }
            }
            form(action:'profile.html', method:'get', style:'text-align:left') {
                mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64: </a>")
                input(type:'text', name:'steamid64')
                input(type:'submit', value:'Search Player')
            }
            div(id: category + '_div', '')
        }
    }

    protected String pagedTable(def ajaxQueries) {
        """
        var page= 0, pageSize= 25, group="none", order= "ASC";
        var data, chart;

        function buildQuery() {
            return ["page=" + page, "rows=" + pageSize, "group=" + group, "order=" + order].join('&');
        }
        function buildDataTable() {
            return new google.visualization.DataTable(\$.ajax({url: "data.json?${ajaxQueries.join('&')}&" + buildQuery(), dataType:"json", async: false}).responseText);
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
}
