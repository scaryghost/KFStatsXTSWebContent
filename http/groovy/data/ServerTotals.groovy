public class ServerTotals extends HtmlTableCreator {
    private final def reader

    public ServerTotals(Map parameters) {
        super()
        this.reader= parameters.reader
    }

    public String create() {
        def odd= true
        def fillTr= {
            [class: (odd ? "odd-row" : "even-row")]
        }
        builder.table(tableAttr) {
            thead() {
                tr() {
                    th(colspan: "2") {
                        h2("Server Totals")
                    }
                }
            }
            tbody() {
                WebCommon.generateSummary(reader).each {attr ->
                    tr(fillTr()) {
                        td(attr['name'])
                        td(attr['value'])
                    }
                    odd= !odd
                }
                tr(fillTr()) {
                    td(colspan: "2") {
                        a(href: "records.html", "View players")
                    }
                }
            }
        }

        return writer
    }
}
