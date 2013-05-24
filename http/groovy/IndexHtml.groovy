import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time

public class IndexHtml extends WebPageBase {
    protected def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']

    public IndexHtml() {
        super()
        htmlDiv << "totals"
        navigation= ["totals", "difficulties", "levels"]
        categoryMthd= "getAggregateCategories"
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css'
        jsFiles << 'http://code.jquery.com/ui/1.10.3/jquery-ui.js'
    }

    protected void fillHeader(def builder) {
        builder.h3("Navigation") {
            select(onchange:'goto(this.options[this.selectedIndex].value); return false') {
                navigation.each {item ->
                    def divName= chartTypes[item] == null ? "#${item}_div" : "#${item}_dashboard_div" 
                    def attr= [value: divName]
                    if (item == navigation.first()) {
                        attr["selected"]= "selected"
                    }
                    option(attr, item.capitalize())
                }
            }
        }
    }

    protected void fillVisualizationJS(def builder) {
        def stndChartsParams= []

        navigation.each {navItem ->
            def divName= chartTypes[navItem] == null ? "${navItem}_div" : navItem
            queries.table= navItem
            if (htmlDiv.contains(navItem)) {
                stndChartsParams << [dataHtml.generatePage(), null, divName, null]
            } else {
                stndChartsParams << [dataJson.generatePage(), navItem, divName, chartTypes[navItem]]
            }
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(generateCommonJs(stndChartsParams))
        }
    }
    protected void fillContentBoxes(def builder) {
        builder.div(id: 'dialog', title:'Levels', '')
        navigation.each {item ->
            if (chartTypes[item] == null) {
                builder.div(id: item + "_div", class:'contentbox', '') 
            } else {
                builder.div(id: item + '_dashboard_div', class:'contentbox') {
                    div(id: item + '_filter_div', '')
                    div(id: item + '_chart_div', '')
                }
            }
        }
    }

    protected String generateCommonJs(def parameters) {
        def chartCalls= ""
        parameters.each {param ->
            def chartType= param[3] == null ? 'Table' : param[3]
            if (param[1] == null) {
                chartCalls+= "            replaceHtml(\"${param[0]}\", '${param[2]}');\n"
            } else if (chartTypes[param[1]] == null) {
                chartCalls+= "            drawChart(${param[0]}, '${param[1]}', '${param[2]}', '${chartType}');\n"
            } else {
                chartCalls+= "            drawFilteredChart(${param[0]}, '${param[1].capitalize()}', '${param[2]}', '${chartType}');\n"
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
            var data= \$.ajax({url: "data.json?table=leveldata&level=" + map, dataType:"json", async: false}).responseText;
            drawChart(data, 'Difficulties', 'dialog', 'Table');
        }
            ${WebCommon.filterChartJs}
            ${WebCommon.replaceHtml}
            ${WebCommon.chartJs}
        google.setOnLoadCallback(visualizationCallback);

        function visualizationCallback() {
$chartCalls
        }
        """
    }

    protected String toXml(def builder) {
        builder.kfstatsx() {
            builder.'stats'(category:"totals") {
                WebCommon.generateSummary(reader).each {attr ->
                    'entry'(attr)
                }
            }
            
            builder.'stats'(category:"difficulties") {
                def accum= [wins: 0, losses: 0, time: 0]
                reader.getDifficulties().each {row ->
                    def result= row
                    accum.keySet().each {key ->
                        accum[key]+= row[key]
                    }

                    result["avg-wave"]= String.format("%.2f", result.waveaccum / (row.wins + row.losses))
                    result["formatted-time"]= Time.secToStr(row.time)
                    result.remove("waveaccum")
                    'entry'(result)
                }
                
                accum.name= "Total"
                accum.length= ""
                accum["avg-wave"]= ""
                accum["formatted-time"]= Time.secToStr(accum.time)
                total(accum)
            }
            builder.'stats'(category:"levels") {
                def accum= [wins: 0, losses: 0, time: 0]
                reader.getLevels().each {row ->
                    def result= row
                    accum.keySet().each {key ->
                        accum[key]+= row[key]
                    }
                    
                    result["formatted-time"]= Time.secToStr(result.time)
                    'entry'(result) {
                        reader.getLevelData(result.level).each {difficulty ->
                            accum.keySet().each {key ->
                                accum[key]+= row[key]
                            }

                            difficulty["avg-wave"]= String.format("%.2f", difficulty.waveaccum / (row.wins + row.losses))
                            difficulty["formatted-time"]= Time.secToStr(row.time)
                            difficulty.remove("waveaccum")
                            builder.'difficulty'(difficulty)
                        }
                    }
                }
                accum.name= "Total"
                accum["formatted-time"]= Time.secToStr(accum.time)
                'total'(accum)
            }
            reader.getAggregateCategories().each {category ->
                builder.'stats'(category: category) {
                    reader.getAggregateData(category).each {row ->
                        def attr= row
                        if (category == "perks" || attr.stat.toLowerCase().contains("time")) {
                            attr["formatted"]= Time.secToStr(attr["value"])
                        }
                        attr.remove("category")
                        'entry'(attr)
                    }
                }
            }
        }
    }

}
