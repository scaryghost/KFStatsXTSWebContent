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
        builder.div(id: 'dialog', title:'Basic dialog') {
            p("This is an animated dialog which is useful for displaying information. The dialog window can be moved, resized and closed with the 'x' icon.")
        }
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
                    show: {
                        effect: "blind",
                        duration: 1000
                    },
                    hide: {
                        effect: "explode",
                        duration: 1000
                    },
                    modal: true
                });
            });
            function open(map) {
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
