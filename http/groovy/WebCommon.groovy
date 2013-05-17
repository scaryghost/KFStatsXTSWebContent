import com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class WebCommon {
    public static def partialQuery(reader, queryValues, records) {
        def pageSize= queryValues[Queries.rows].toInteger()
        def start= queryValues[Queries.page].toInteger() * pageSize
        def end= start + pageSize
        def order= Order.NONE, group

        if (queryValues[Queries.group] != Queries.defaults[Queries.group]) {
            order= Order.valueOf(Order.class, queryValues[Queries.order].toUpperCase())
            group= queryValues[Queries.group]
        }
        if (records) {
            return reader.getRecords(group, order, start, end)
        }
        return reader.getSessions(queryValues[Queries.steamid64], group, order, start, end)
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

    public static def chartJs= """
        function drawChart(data, title, divId, chartType) {
            var chart= new google.visualization.ChartWrapper({'chartType': chartType, 'containerId': divId, 'options': {
                'chartArea': {height: '90%'},
                'vAxis': {textStyle: {fontSize: 15}},
                'allowHtml': true
            }});
            chart.setDataTable(data);
            chart.setOption('title', title);
            chart.setOption('height', Math.max(chart.getDataTable().getNumberOfRows() * 25, document.getElementById(divId).offsetHeight * 0.975));
            chart.setOption('width', document.getElementById(divId).offsetWidth * 0.985);
            chart.draw();
        }
    """

    public static def replaceHtml= """
        function replaceHtml(html, divId) {
            document.getElementById(divId).innerHTML= html;
        }
    """

    public static def scrollingJs= """
        //Div scrolling js taken from http://gazpo.com/2012/03/horizontal-content-scroll/
        function goto(id){   
            //animate to the div id.
            \$(".contentbox-wrapper").animate({"left": -(\$(id).position().left)}, 600);
        }
    """
}
