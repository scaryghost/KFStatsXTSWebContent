import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class WaveDataHtml extends WebPageBase {
    public WaveDataHtml() {
        super()
        categoryMthd= "getWaveDataCategories"
    }

    protected void fillNav(def builder) {
        builder.select(onchange:'goto(this.options[this.selectedIndex].value); return false') {
            if (queries.level == null) {
                option(value: "#summary_div", selected: "selected", 'Summary')
            }
            navigation.each {item ->
                option(value: "#${item}_dashboard_div", item)
            }
        }
    }
    protected void fillVisualizationJS(def builder) {
        def parameters= []
        queries.table= "wave"
        navigation.each {item ->
            queries.group= item
            parameters << [dataJson.generatePage(reader, queries), item]
        }
        if (queries.level == null) {
            queries.table= "difficultydata"
            parameters << [dataJson.generatePage(reader, queries), 'Summary', 'summary_div']
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(dashboardVisualization(parameters))
        }
    }
    protected void fillContentBoxes(def builder) {
        if (queries.level == null) {
            builder.div(id: 'summary_div', class: 'contentbox', '')
        }
        navigation.each {item ->
            builder.div(id: item + '_dashboard_div', class:'contentbox', '') {
                div(id: item + "_dashboard_filter1_div", '')
                div(id: item + "_dashboard_filter2_div", '')
                div(id: item + "_dashboard_chart_div", '')
            }
        }
    }

    protected static def dasboardJs= """
        function drawDashboard(data, category) {
            var divName= category + "_dashboard";
            var dashboard= new google.visualization.Dashboard(document.getElementById(divName + '_div'))
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
            if (param.size() == 2) {
                chartCalls+= "            drawDashboard(${param[0]}, '${param[1]}');\n"
            } else {
                chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', 'Table')\n"
            }
        }
        return "${WebCommon.chartJs}\n$dasboardJs$chartCalls        }\n    "
    }
}
