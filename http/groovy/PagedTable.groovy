import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class PagedTable extends WebPageBase {
    protected final def category, formUrl

    protected PagedTable(def category, def formUrl) {
        super()
        this.category= category
        this.formUrl= formUrl

        jsFiles << 'http/js/jquery.dataTables.min.js'
        stylesheets << 'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css' << 'http/css/jquery.dataTables_themeroller.css'
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

    protected String dataTableOptions() {
        """
                bPaginate: true,
                bProcessing: true,
                bLengthChange: false,
                bFilter: false,
                bSort: true,
                bInfo: true,
                bAutoWidth: true,
                bServerSide: true,
                bJQueryUI: true,
                iDisplayLength: 25,
                sAjaxSource: 'data.json',
                sPaginatationType: 'full_numbers',
                fnServerData: function (sSource, aoData, fnCallback) {
                    ${fillAoData()}
                    \$.getJSON(sSource, aoData, function(json) {
                        fnCallback(json)
                    })
                },
                sScrollY: document.getElementById('${category}_div').offsetHeight * 0.85
"""
    }

    protected String fillAoData() {
        """aoData.push({"name": "table", "value": "$category"})"""
    }
}
