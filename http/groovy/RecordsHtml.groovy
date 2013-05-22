public class RecordsHtml extends PagedTable {
    public RecordsHtml() {
        super("records", "profile.html")
    }

    public String getPageTitle() {
        return "${super.getPageTitle()} - Player Records"
    }

    protected void fillHeader(def builder) {
        builder.h3("Player Records")
    }

    protected String toXml(def builder) {
        def queryValues= Queries.parseQuery(queries)
        builder.kfstatsx() {
            def pos= queryValues[Queries.page].toInteger() * queryValues[Queries.rows].toInteger()

            builder.'stats'(category:'records') {
                WebCommon.partialQuery(reader, queryValues, true).each {row ->
                    row.remove("id")
                    row.remove("record_id")
                    row.remove("avatar")
                    
                    row["pos"]= pos + 1

                    builder.'record'(row)
                    pos++
                }
            }
        }
    }
}
