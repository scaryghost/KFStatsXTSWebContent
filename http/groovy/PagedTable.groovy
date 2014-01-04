import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class PagedTable extends WebPageBase {
    protected final def category, formUrl
    protected def dataOptions, aoColumnDefs, aoData

    public PagedTable(def category, def formUrl) {
        super()

        def fillClass= {top ->
            def ch= top ? "t" : "b";
            "fg-toolbar ui-toolbar ui-widget-header ui-corner-${ch}l ui-corner-${ch}r ui-helper-clearfix"
        }
        this.category= category
        this.formUrl= formUrl
        this.dataOptions= [bPaginate: true, bProcessing: true, bLengthChange: true, bFilter: false, bSort: true, bInfo: true, bAutoWidth: false,
                bServerSide: true, bJQueryUI: true, iDisplayLength: 25, sAjaxSource: 'datadispatcher.php', sPaginationType: 'full_numbers', 
                aLengthMenu: [[25, 50, 100, 250], [25, 50, 100, 250]], aaSorting: [], 
                sDom: """'<"${fillClass(true)}"l<"player-search">>rt<"${fillClass(false)}"ip><"clear">'"""]

        jsFiles.remove(1)
        jsFiles << 'js/jquery.dataTables.min.js'
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css' << 'css/jquery.dataTables_themeroller.css'

        aoData= ["""{"name": "table", "value": "$category"}"""]
    }

    protected void fillVisualizationJS(def builder) {
        def writer= new StringWriter()
        def formBuilder= new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
        formBuilder.form(action:formUrl, method:'get') {
            mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64 </a>")
            input(type:'text', name:'steamid64')
            input(type:'submit', value:'Search Player')
        }
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped("""
        var table;
        \$(document).ready(function() {
            table= \$('#$category').dataTable({${getDataTableOptions()}});
            \$("div.player-search").html("$writer");
        } );
  """)
        }
    }

    protected void fillContentBoxes(def builder) {
        builder.div(id:"${category}_div", class:'contentbox') {
            table(id:"$category", '')
        }
    }

    protected String getDataTableOptions() {
        def fillAoData= {
            aoData.inject("") {accum, elem ->
                accum+= "                    aoData.push($elem);\n"
            }
        }

        def options= dataOptions.collect {key, value ->
            def str= "                $key: "
            if (value instanceof String) {
                str+= """'$value'"""
            } else {
                str+= "$value"
            }
            str
        }.join(",\n")

        """
$options,
                aoColumnDefs: $aoColumnDefs,
                fnServerData: function (sSource, aoData, fnCallback) {
${fillAoData()}
                    \$.getJSON(sSource, aoData, function(json) {
                        fnCallback(json)
                    })
                },
                sScrollY: document.getElementById('${category}_div').offsetHeight * 0.85
            """
    }
}
