import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class DifficultyHtml extends IndexHtml {
    public DifficultyHtml() {
        super()
    }
    protected static def dasboardJs= """
        function drawDashboard(data, category) {
            var divName= category + "_dashboard";
            var dashboard=  new google.visualization.Dashboard(document.getElementById(divName + '_div'))
            var dataTable= new google.visualization.DataTable(data);

            var columnsTable = new google.visualization.DataTable();
            columnsTable.addColumn('number', 'colIndex');
            columnsTable.addColumn('string', 'colLabel');
            var initState= {selectedValues: []};
            // put the columns into this data table (skip column 0)
            for (var i = 1; i < dataTable.getNumberOfColumns(); i++) {
                columnsTable.addRow([i, dataTable.getColumnLabel(i)]);
                initState.selectedValues.push(dataTable.getColumnLabel(i));
            }
            var donutRangeSlider= new google.visualization.ControlWrapper({
                'controlType': 'CategoryFilter',
                'containerId': divName + '_filter1_div',
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
                containerId: divName + '_filter2_div',
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
            
            var chart= new google.visualization.ChartWrapper({
                'chartType': 'LineChart',
                'containerId': divName + '_chart_div',
                'options': {
                  'height': document.getElementById(divName + '_div').offsetHeight * 0.9,
                  'width': document.getElementById(divName + '_div').offsetWidth * 0.975,
                  'legend': 'right',
                  'pointSize': 5
                }
            });
            dashboard.bind(donutRangeSlider, chart);
            dashboard.draw(dataTable);

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
                chart.setView({columns: columnIndices});
                chart.draw();
            });
    
            columnFilter.draw();

        }
        //Column filtering taken from: http://jsfiddle.net/asgallant/WaUu2/
        google.setOnLoadCallback(visualizationCallback);
        function visualizationCallback() {
"""

    protected String dashboardVisualization(def parameters) {
        def chartCalls= ""
        parameters.each {param ->
            chartCalls+= "            drawDashboard(${param[0]}, '${param[1]}');\n"
        }
        return "$dasboardJs$chartCalls        }\n    "
    }

    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        def nav= reader.getWaveDataCategories()
        def dataJson= new DataJson()

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
                def parameters= []
                nav.each {item ->
                    queries.group= item
                    parameters << [dataJson.generatePage(reader, queries), item]
                }
                script(type: 'text/javascript') {
                    mkp.yieldUnescaped(dashboardVisualization(parameters))
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
