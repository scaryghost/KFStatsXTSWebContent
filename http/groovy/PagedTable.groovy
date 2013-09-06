import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class PagedTable extends WebPageBase {
    protected final def category, formUrl
    protected def dataOptions, aoColumnDefs, aoData

    protected PagedTable(def category, def formUrl) {
        super()
        this.category= category
        this.formUrl= formUrl
        this.dataOptions= [bPaginate: true, bProcessing: true, bLengthChange: false, bFilter: false, bSort: true, bInfo: true, bAutoWidth: false,
                bServerSide: true, bJQueryUI: true, iDisplayLength: 25, sAjaxSource: 'data.json', sPaginationType: 'full_numbers', aaSorting: []]

        jsFiles.remove(1)
        jsFiles << 'http/js/jquery.dataTables.min.js'
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css' << 'http/css/jquery.dataTables_themeroller.css'

        aoData= ["""{"name": "table", "value": "$category"}"""]
    }

    protected void fillVisualizationJS(def builder) {
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped("""
        var table;
        \$(document).ready(function() {
            table= \$('#$category').dataTable({${getDataTableOptions()}});
        } );

        function updatePageSize(pageSize) {
            table.fnSettings()._iDisplayLength= pageSize;
            table.fnDraw();
        }
  """)
        }
    }

    protected void fillContentBoxes(def builder) {
        builder.div(id:"${category}_div", class:'contentbox') {
            table(style:"border-collapse: collapse; width:100%") {
                tr() {
                    td() {
                        form(action:'', 'Number of rows') {
                            select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                                option(selected:"selected", value:'25', '25')
                                option(value:'50', '50')
                                option(value:'100', '100')
                                option(value:'250', '250')
                            }
                        }
                    }
                    td(style: "text-align: right") {
                        form(action:formUrl, method:'get') {
                            mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64 </a>")
                            input(type:'text', name:'steamid64')
                            input(type:'submit', value:'Search Player')
                        }
                    }
                }
            }
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
                sScrollY: document.getElementById('${category}_div').offsetHeight * 0.85,
"""
    }
}
