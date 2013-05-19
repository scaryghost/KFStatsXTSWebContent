import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class IndexHtml extends WebPageBase {
    protected def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']

    public IndexHtml() {
        super()
        htmlDiv << "totals"
        navigation= ["totals", "difficulties", "levels"]
        categoryMthd= "getAggregateCategories"
    }

    protected void fillNav(def builder) {
        builder.select(onchange:'goto(this.options[this.selectedIndex].value); return false') {
            navigation.each {item ->
                def attr= [value: "#${item}_div"]
                if (item == navigation.first()) {
                    attr["selected"]= "selected"
                }
                option(attr, item)
            }
        }
    }

    protected void fillVisualizationJS(def builder) {
        def stndChartsParams= []

        navigation.each {navItem ->
            queries.table= navItem
            if (htmlDiv.contains(navItem)) {
                stndChartsParams << [dataHtml.generatePage(reader, queries), null, "${navItem}_div", null]
            } else {
                stndChartsParams << [dataJson.generatePage(reader, queries), navItem, "${navItem}_div", chartTypes[navItem]]
            }
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(generateCommonJs(stndChartsParams))
        }
    }
    protected void fillContentBoxes(def builder) {
        builder.div(id: 'dialog', title:'Levels', '')
        navigation.each {item ->
            builder.div(id: item + '_div', class:'contentbox', '')
        }
    }

    protected String generateCommonJs(def parameters) {
        def chartCalls= ""
        parameters.each {param ->
            def chartType= param[3] == null ? 'Table' : param[3]
            if (param[1] == null) {
                chartCalls+= "            replaceHtml(\"${param[0]}\", '${param[2]}');\n"
            } else {
                chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', '${chartType}');\n"
            }
        }
        return """ 
            \$(function() {
                \$( "#dialog" ).dialog({
                    autoOpen: false,
                    position: {my: "left+15%", at: "left top+15%"},
                    modal: true,
                    width: document.getElementById('levels_div').offsetWidth * 0.985
                });
            });
            function open(map) {
                \$( "#dialog" ).dialog( "option", "title", map );
                \$( "#dialog" ).dialog( "open" );
                var data= \$.ajax({url: "data.json?table=leveldata&name=" + map, dataType:"json", async: false}).responseText;
                drawChart(data, 'Difficulties', 'dialog', 'Table');
            }
            ${WebCommon.replaceHtml}
            ${WebCommon.chartJs}
            google.setOnLoadCallback(visualizationCallback);

            function visualizationCallback() {
                $chartCalls
            }
        """
    }

}
