import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time

public class IndexHtml extends WebPageBase {
    protected def chartTypes= [deaths: 'BarChart', perks: 'PieChart', weapons: 'BarChart', kills: 'BarChart']

    public IndexHtml() {
        super()
        htmlDiv << "totals"
        navigation= ["totals", "difficulties", "levels"]
        categoryMthd= "getStatCategories"
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css'
        jsFiles << 'http://code.jquery.com/ui/1.10.3/jquery-ui.js'
    }

    protected void fillHeader(def builder) {
        builder.ul(id: "menu") {
            li() {
                a("Navigation")
                ul() {
                    navigation.each {item ->
                        def divName= chartTypes[item] == null ? "${item}_div" : "${item}_dashboard_div"
                        li() {
                            a(href: "javascript:goto(" + divName + ")", item.capitalize())
                        }
                    }
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
                stndChartsParams << [dataDispatcher.generatePage(), null, divName, null]
            } else {
                stndChartsParams << [dataDispatcher.generatePage(), navItem, divName, chartTypes[navItem]]
            }
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped(generateCommonJs(stndChartsParams))
        }
    }

    protected void addDialogBox(def builder) {
        builder.div(id: 'dialog', title:'Levels', '')
    }
    protected void fillContentBoxes(def builder) {
        addDialogBox(builder)
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
    protected String generateDialogJS() {
        """
        function open(opts) {
            \$( "#dialog" ).dialog( "option", "title", ("title" in opts) ? opts["title"] : opts["level"]);
            \$( "#dialog" ).dialog( "open" );
            
            var query= ""; 
            var keys= Object.keys(opts);
            delete opts["title"];
            for(var index in keys) {
                if (query.length != 0) {
                    query+= "&";
                }
                query+= keys[index] + "=" + opts[keys[index]];
            }
            var data= \$.ajax({url: "datadispatcher.php?" + query, dataType:"json", async: false}).responseText;
            drawChart(data, 'Difficulties', 'dialog', 'Table');
        }
        """
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
            \$( "#menu" ).menu();
        });
            ${generateDialogJS()}
            ${WebCommon.filterChartJs}
            ${WebCommon.replaceHtml}
            ${WebCommon.chartJs}
        google.setOnLoadCallback(visualizationCallback);

        function visualizationCallback() {
$chartCalls
        }
        """
    }

    protected void buildXml(def builder) {
        builder.kfstatsx() {
            'aggregate'() {
                builder.'stats'(category:"totals") {
                    WebCommon.generateSummary(reader).each {attr ->
                        'entry'(attr)
                    }
                }
                builder.'stats'(category:"difficulties") {
                    def accum= [wins: 0, losses: 0, time: 0]
                    reader.getDifficulties().each {difficulty ->
                        def attr= [name: difficulty.name, length: difficulty.length, wins: difficulty.wins, losses: difficulty.losses, time: difficulty.time]
                        accum.keySet().each {key ->
                            accum[key]+= attr[key]
                        }
    
                        attr["avg-wave"]= String.format("%.2f", WebCommon.computeAvgWave(difficulty))
                        'entry'(attr) {
                            reader.getDifficultyData(difficulty.name, difficulty.length).each {data ->
                                def dataAttr= [name: data.level, wins: data.wins, losses: data.losses, time: data.time]
                                dataAttr["avg-wave"]= String.format("%.2f", WebCommon.computeAvgWave(data))
                                builder.'difficulty'(dataAttr) {
                                    'formatted-time'(Time.secToStr(data.time))
                                }
                            }
                            'formatted-time'(Time.secToStr(difficulty.time))
                        }
                    }
                
                    accum.name= "Total"
                    total(accum) {
                        'formatted-time'(Time.secToStr(accum.time))
                    }
                }
                builder.'stats'(category:"levels") {
                    def accum= [wins: 0, losses: 0, time: 0]
                    reader.getLevels().each {level ->
                        def levelAttr= [name: level.name, wins: level.wins, losses: level.losses, time: level.time]
                        accum.keySet().each {key ->
                            accum[key]+= levelAttr[key]
                        }
                        
                        'entry'(levelAttr) {
                            'formatted-time'(Time.secToStr(accum.time))
                            reader.getLevelData(level.name).each {data ->
                                def dataAttr= [name: data.difficulty, length: data.length, wins: data.wins, losses: data.losses, time: data.time]
                                dataAttr["avg-wave"]= String.format("%.2f", WebCommon.computeAvgWave(data))
                                builder.'difficulty'(dataAttr) {
                                    'formatted-time'(Time.secToStr(data.time))
                                }
                            }
                        }
                    }
                    accum.name= "Total"
                    'total'(accum) {
                        'formatted-time'(Time.secToStr(accum.time))
                    }
                }
                reader.getStatCategories().each {category ->
                    builder.'stats'(category: category) {
                        reader.getAggregateData(category).each {stat ->
                            def attr= [stat: stat.stat, value: stat.value]
                            'entry'(attr) {
                                if (category == "perks" || stat.stat.toLowerCase().contains("time")) {
                                    'formatted-time'(Time.secToStr(stat.value))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
