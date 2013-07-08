import com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.DataReader.Record
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class WebCommon {
    public static double computeAvgWave(Record record) {
        def avg
        
        try {
            avg= record.wave_sum/(record.wins + record.losses)
        } catch (ArithmeticException ex) {
            avg= 0.0
        }
        return avg
    }
    public static def aggregateCombineMatchHistory(def matchHistory, def orderByLevel) {
        def aggregateData= [:]

        matchHistory.each {row ->
            def key
            def diffKey= [row.difficulty, row.length]

            key= orderByLevel ? row.level : diffKey
            if (aggregateData[key] == null) {
                aggregateData[key]= [:]
                aggregateData[key]["time"]= 0
            }
            if (aggregateData[key][row.result] == null) {
                aggregateData[key][row.result]= 0
            }
            aggregateData[key][row.result]++
            aggregateData[key]["time"]+= row.duration
        }
        return aggregateData
    }

    public static def aggregateMatchHistory(def matchHistory, def orderByLevel) {
        def aggregateData= [:]
        matchHistory.each {row ->
            def key1, key2
            def diffKey= [row.difficulty, row.length]

            key1= orderByLevel ? row.level : diffKey
            key2= orderByLevel ? diffKey : row.level
            
            if (aggregateData[key1] == null) {
                aggregateData[key1]= [:]
            }
            if (aggregateData[key1][key2] == null) {
                aggregateData[key1][key2]= [:]
                aggregateData[key1][key2]["time"]= 0
            }
            if (aggregateData[key1][key2][row.result] == null) {
                aggregateData[key1][key2][row.result]= 0
            }
            aggregateData[key1][key2][row.result]++
            aggregateData[key1][key2]["time"]+= row.duration
        }
        return aggregateData
    }

    public static def partialQuery(reader, queries, records) {
        def defaults= [page: 0, rows: 25, order: "asc", group: "none"]
        defaults.each {key, value ->
            if (queries[key] == null) {
                queries[key]= value
            }
        }

        def pageSize= queries.rows.toInteger()
        def start= queries.page.toInteger() * pageSize
        def end= start + pageSize
        def order= Order.NONE, group

        if (queries.group != defaults.group) {
            order= Order.valueOf(Order.class, queries.order.toUpperCase())
            group= queries.group
        }
        if (records) {
            return reader.getRecords(group, order, start, end)
        }
        return reader.getMatchHistory(queries.steamid64, group, order, start, end)
    }

    public static def generateSummary(reader) {
        def games= 0, playTime= 0, playerCount
        reader.getDifficulties().each {row ->
                games+= row.wins + row.losses
                playTime+= row.time
        }
        playerCount= reader.getNumRecords()

        return [["Games", games], ["Play Time", Time.secToStr(playTime)], ["Player Count", playerCount]].collect {
            [name: it[0], value: it[1]]
        }
    }

    public static def filterChartJs= """
        function drawFilteredChart(data, title, divId, chartType) {
            var datatable= new google.visualization.DataTable(data);
            var dashboard= new google.visualization.Dashboard(document.getElementById(divId + '_dashboard_div'));
            var chartController = new google.visualization.ControlWrapper({
                'controlType': 'CategoryFilter',
                'containerId': divId+'_filter_div',
                'options': {
                    'filterColumnIndex': 0
                }
            });
            var chart= new google.visualization.ChartWrapper({'chartType': chartType, 'containerId': divId+"_chart_div", 'options': {
                'chartArea': {height: '90%'},
                'vAxis': {title: 'Name', titleTextStyle: {color: 'red'}, textStyle: {fontSize: 15}},
                'hAxis': {title: 'Frequency', titleTextStyle: {color: 'red'}},
                'allowHtml': true,
                'title': title,
                'height': document.getElementById(divId+"_dashboard_div").offsetHeight * 0.925,
                'width': document.getElementById(divId+"_dashboard_div").offsetWidth * 0.985
            }});
            //taken from https://groups.google.com/forum/?fromgroups#!topic/google-visualization-api/VcgHvrCrCNM
            google.visualization.events.addListener(dashboard, 'ready', function() {
                var numRows= chart.getDataTable().getNumberOfRows();
                var expectedHeight = Math.max(numRows * 25, document.getElementById(divId+"_dashboard_div").offsetHeight * 0.925);
                if (parseInt(chart.getOption('height'), 10) != expectedHeight) {
                    chart.setOption('height', expectedHeight);
                    chart.draw();
                }
            });
            if (chartType != 'PieChart') {
                chart.setOption('legend', {position: 'none'});
            }
            dashboard.bind(chartController, chart);
            dashboard.draw(datatable);
        }

    """
    public static def chartJs= """
        function drawChart(data, title, divId, chartType) {
            var chart= new google.visualization.ChartWrapper({'chartType': chartType, 'containerId': divId, 'options': {
                'chartArea': {height: '90%'},
                'vAxis': {textStyle: {fontSize: 15}},
                'allowHtml': true,
                'title': title,
                'height': document.getElementById(divId).offsetHeight * 0.975,
                'width': document.getElementById(divId).offsetWidth * 0.985
            }});
            chart.setDataTable(data);
            chart.draw();
        }
    """

    public static def replaceHtml= """
        function replaceHtml(html, divId) {
            document.getElementById(divId).innerHTML= html;
        }
    """

}
