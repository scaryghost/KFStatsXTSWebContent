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
    

    public IndexHtml() {
        visualizations= [:]
        visualizations["totals"]= {queries, category, type -> replaceHtml(queries, category)}
        visualizations["records"]= {queries, category, type -> pagedTable(queries, category)}
        visualizations["weapons"]= {queries, category, type -> resizedVisualization(queries, category, type)}
        visualizations["kills"]= visualizations["weapons"]
        visualizations["deaths"]= visualizations["weapons"]
        visualizations["perks"]= {queries, category, type -> simpleVisualization(queries, category, type)}

        navLeft= ["totals", "difficulties", "levels"]
        navRight= ["records"]
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

    protected String simpleVisualization(def queries, def category, def type) {
        """
        google.setOnLoadCallback(draw${category});
        function draw${category}() {
            var data= new google.visualization.DataTable(${dataJsonObj.generatePage(reader, queries)});
            var chart= new google.visualization.ChartWrapper({'chartType': '$type', 'containerId': '${category}_div', 'options': {allowHtml: true}});
            chart.setDataTable(data);
            chart.setOption('title', '${category}');
            chart.setOption('height', document.getElementById('${category}_div').offsetHeight * 0.975);
            chart.setOption('width', document.getElementById('${category}_div').offsetWidth * 0.985);
            chart.draw();
        }
    """
    }

    protected String resizedVisualization(def queries, def category, def type) {
        """
        google.setOnLoadCallback(draw${category});
        function draw${category}() {
            var data= new google.visualization.DataTable(${dataJsonObj.generatePage(reader, queries)});
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

    protected String pagedTable(def queries, def category) {
        def ajaxQueries= ["table=$category"]

        queries.each {key, value ->
            ajaxQueries << "$key=$value"
        }
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

                nav.each {
                    def js
                    queries["table"]= it
                    if (visualizations[it] != null) {
                        if (chartTypes[it] != null) {
                            js= visualizations[it](queries, it, chartTypes[it])
                        } else {
                            js= visualizations[it](queries, it, 'Table')
                        }
                    } else {
                        js= simpleVisualization(queries, it, 'Table')
                    }
                    script(type: 'text/javascript') {
                        mkp.yieldUnescaped(js)
                    }
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
                                    } else if (item == nav.last()) {
                                        attr["value"]+= "_outer"
                                    }
                                    option(attr, item)
                                }
                            }
                        }
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            nav.each {item ->
                                if (item == nav.last()) {
                                    div(id:item + '_div_outer', class:'contentbox') {
                                        form(action:'', 'Number of rows:') {
                                            select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                                                option(selected:"selected", value:'25', '25')
                                                option(value:'50', '50')
                                                option(value:'100', '100')
                                                option(value:'250', '250')
                                            }
                                        }
                                        if (queries["steamid64"] == null) {
                                            form(action:'profile.html', method:'get', style:'text-align:left') {
                                            mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64: </a>")
                                            input(type:'text', name:'steamid64')
                                            input(type:'submit', value:'Search Player')
                                            }
                                        }
                                        div(id: item + '_div', '')
                                    }
                                } else {
                                    div(id: item + '_div', class:'contentbox', '')
                                }
                            }
                        }
                    }
                }
            }
        }
        return "<!DOCTYPE HTML>\n$writer"
    }
}
