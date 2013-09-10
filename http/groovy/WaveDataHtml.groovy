import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class WaveDataHtml extends WebPageBase {
    public WaveDataHtml() {
        super()
        categoryMthd= "getWaveDataCategories"
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css'
        jsFiles << 'http://code.jquery.com/ui/1.10.3/jquery-ui.js'
    }
    public String getPageTitle() {
        return "${super.getPageTitle()} - Wave Data"
    }

    protected void fillHeader(def builder) {
        builder.ul(id: "menu") {
            li() {
                a("Navigation")
                ul() {
                    if (queries.level == null) {
                        li() {
                            a(href: "javascript:goto(levels_div)", 'Levels')
                        }
                    }
                    navigation.each {item ->
                        def divName= "${item}_dashboard_div"
                        li() {
                            a(href: "javascript:goto(" + divName + ")", item.capitalize())
                        }
                    }
                }
            }
        }
    }
    protected void fillVisualizationJS(def builder) {
        def parameters= []
        queries.table= "wave"
        navigation.each {item ->
            queries.group= item
            parameters << [dataDispatcher.generatePage(), item]
        }
        if (queries.level == null) {
            queries.table= "difficultydata"
            parameters << [dataDispatcher.generatePage(), 'Levels', 'levels_div']
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(dashboardVisualization(parameters))
        }
    }
    protected void fillContentBoxes(def builder) {
        if (queries.level == null) {
            builder.div(id: 'levels_div', class: 'contentbox', '')
        }
        navigation.each {item ->
            builder.div(id: item + '_dashboard_div', class:'contentbox', '') {
                div(id: item + "_dashboard_filter1_div", '')
                div(id: item + "_dashboard_filter2_div", '')
                div(id: item + "_dashboard_chart_div", '')
            }
        }
    }

    protected static def dashboardJs= """
        \$(function() {
            \$( "#menu" ).menu();
        });
        function drawDashboard(data, category, title) {
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
                  'curveType': 'function',
                  'title': title,
                  'pointSize': 5,
                  'hAxis': {title: 'Wave', titleTextStyle: {color: 'red'}},
                  'vAxis': {title: 'Frequency', titleTextStyle: {color: 'red'}}
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
                chartCalls+= "            drawDashboard(${param[0]}, '${param[1]}', '${param[1].capitalize()}');\n"
            } else {
                chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', 'Table')\n"
            }
        }
        return "${WebCommon.chartJs}\n$dashboardJs$chartCalls        }\n    "
    }

    protected void buildXml(def builder) {
        builder.kfstatsx() {
            def waveDataAttr= [difficulty: queries.difficulty, length: queries.length]

            if (queries.level != null) {
                waveDataAttr.level= queries.level
            }
            builder.'wave-data'(waveDataAttr) {
                if (queries.level == null) {
                    'stats'(category: 'levels') {
                        reader.getDifficultyData(queries.difficulty, queries.length).each {data ->
                            def dataAttr= [name: data.level, wins: data.wins, losses: data.losses, time:data.time, 
                                "avg-wave": String.format("%.2f", WebCommon.computeAvgWave(data))]
                            'entry'(dataAttr) {
                                'formatted-time'(Time.secToStr(data.time))
                            }
                        }
                    }
                }
                reader.getWaveDataCategories().each {category ->
                    builder.'stats'(category: category) {
                        def waves= [:]
                        def rows= queries.level == null ? reader.getWaveData(queries.difficulty, queries.length, category) : 
                            reader.getWaveData(queries.level, queries.difficulty, queries.length, category)
                        rows.each {row ->
                            if (waves[row.wave] == null) {
                                waves[row.wave]= []
                            }
                            waves[row.wave] << row
                        }
                        waves.each {wave, stats ->
                            builder.'wave'(num: wave) {
                                stats.each {stat ->
                                    def statAttr= [stat: stat.stat, value: stat.value]
                                    'entry'(statAttr)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
