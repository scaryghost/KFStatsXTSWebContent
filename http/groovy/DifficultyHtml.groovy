import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class DifficultyHtml extends IndexHtml {
    public DifficultyHtml() {
        super()
    }

    protected String dashboardVisualization(def queries, def category) {
        queries["group"]= category
        """
        //Column filtering taken from: http://jsfiddle.net/asgallant/WaUu2/
        google.setOnLoadCallback(drawDashboard${category});
        function drawDashboard${category}() {
            var dashboard= new google.visualization.Dashboard(document.getElementById('${category}_dashboard_div'));
            var data= new google.visualization.DataTable(${dataJsonObj.generatePage(reader, queries)});

            var columnsTable = new google.visualization.DataTable();
            columnsTable.addColumn('number', 'colIndex');
            columnsTable.addColumn('string', 'colLabel');
            var initState= {selectedValues: []};
            // put the columns into this data table (skip column 0)
            for (var i = 1; i < data.getNumberOfColumns(); i++) {
                columnsTable.addRow([i, data.getColumnLabel(i)]);
                initState.selectedValues.push(data.getColumnLabel(i));
            }

            var donutRangeSlider= new google.visualization.ControlWrapper({
                'controlType': 'CategoryFilter',
                'containerId': '${category}_dashboard_filter1_div',
                'options': {
                  'filterColumnLabel': 'Wave',
                  'ui': {
                    'labelStacking': 'horizontal',
                    'allowTyping': false,
                    'allowMultiple': true
                    }
                }
            });
            var columnFilter = new google.visualization.ControlWrapper({
                controlType: 'CategoryFilter',
                containerId: '${category}_dashboard_filter2_div',
                dataTable: columnsTable,
                options: {
                    filterColumnLabel: 'colLabel',
                    ui: {
                        label: 'Columns',
                        allowTyping: false,
                        allowMultiple: true,
                        selectedValuesLayout: 'horizontal'
                    }
                },
                state: initState
            });
            
            var pieChart= new google.visualization.ChartWrapper({
                'chartType': 'LineChart',
                'containerId': '${category}_dashboard_chart_div',
                'options': {
                  'height': document.getElementById('${category}_dashboard_div').offsetHeight * 0.9,
                  'width': document.getElementById('${category}_dashboard_div').offsetWidth * 0.975,
                  'legend': 'right',
                  'pointSize': 5
                }
            });
            dashboard.bind(donutRangeSlider, pieChart);
            dashboard.draw(data);

            google.visualization.events.addListener(columnFilter, 'statechange', function () {
                var state = columnFilter.getState();
                var row;
                var columnIndices = [0];
                for (var i = 0; i < state.selectedValues.length; i++) {
                    row = columnsTable.getFilteredRows([{column: 1, value: state.selectedValues[i]}])[0];
                    columnIndices.push(columnsTable.getValue(row, 0));
                }
                // sort the indices into their original order
                columnIndices.sort(function (a, b) {
                    return (a - b);
                });
                pieChart.setView({columns: columnIndices});
                pieChart.draw();
            });
    
            columnFilter.draw();

        }
    """
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        def nav= reader.getWaveDataCategories()
        this.reader= reader

        htmlBuilder.html() {
            htmlBuilder.head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title("KFStatsX")

                link(rel:'shortcut icon', href: 'http/ico/favicon.ico')
                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
                
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                script(type:'text/javascript', scrollingJs)
                htmlBuilder.script(type: 'text/javascript') {
                    mkp.yieldUnescaped(dashboardVisualization(queries))
                }
                nav.each {item ->
                    script(type: 'text/javascript') {
                        mkp.yieldUnescaped(dashboardVisualization(queries, item))
                    }
                }
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav') {
                        h3("Navigation") {
                            select(onchange:'goto(this.options[this.selectedIndex].value); return false') {
                                nav.each {item ->
                                    def attr= [value: "#${item}_dashboard_div"]
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
                                div(id: item + '_dashboard_div', class:'contentbox', '') {
                                    div(id: item + "_dashboard_filter1_div", '')
                                    div(id: item + "_dashboard_filter2_div", '')
                                    div(id: item + "_dashboard_chart_div", '')
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
