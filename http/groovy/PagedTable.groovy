import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class PagedTable extends WebPageBase {
    protected final def category, formUrl
    protected def stylesheets= ['http/css/jquery.dataTables.css']
    protected def jsFiles= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.js', 'http/js/jquery.dataTables.min.js']

    protected PagedTable(def category, def formUrl) {
        super()
        this.category= category
        this.formUrl= formUrl
    }

    protected void fillVisualizationJS(def builder) {
        builder.script(type: 'text/javascript') {
            mkp.yieldUnescaped("""
        var table;
        \$(document).ready(function() {
            table= \$('#$category').dataTable({${dataTableOptions()}});
        } );

        function updatePageSize(pageSize) {
            table.fnSettings()._iDisplayLength= pageSize;
            table.fnDraw();
        }
  """)
        }
    }

    protected void fillContentBoxes(def builder) {
        builder.div(id:"${category}_div_outer", class:'contentbox') {
            form(action:'', 'Number of rows:') {
                select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                    option(selected:"selected", value:'25', '25')
                    option(value:'50', '50')
                    option(value:'100', '100')
                    option(value:'250', '250')
                }
            }
            form(action:formUrl, method:'get', style:'text-align:left') {
                mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64: </a>")
                input(type:'text', name:'steamid64')
                input(type:'submit', value:'Search Player')
            }
            table(id:"$category", '')
        }
    }

    protected String dataTableOptions() {
        """
                "bPaginate": true,
                "bProcessing": true,
                "bLengthChange": false,
                "bFilter": false,
                "bSort": true,
                "bInfo": true,
                "bAutoWidth": true,
                "bServerSide": true,
                "iDisplayLength": 25,
                "sAjaxSource": 'data.json',
                "sPaginatationType": 'full_numbers',
                "fnServerData": function (sSource, aoData, fnCallback) {
                    aoData.push({"name": "table", "value": "$category"});
                    \$.getJSON(sSource, aoData, function(json) {
                        fnCallback(json)
                    })
                }
"""
    }
}
