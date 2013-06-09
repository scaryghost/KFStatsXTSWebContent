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

    protected void buildXml(def builder) {
        builder.kfstatsx() {
            'aggregate'() {
                builder.'stats'(category:'records') {
                    WebCommon.partialQuery(reader, queries, true).each {row ->
                        row.remove("id")
                        row.remove("record_id")
                        row.remove("avatar")
                        
                        builder.'record'(row)
                    }
                }
            }
        }
    }
}
