public class SessionsHtml extends PagedTable {
    public SessionsHtml() {
        super("sessions", "sessions.html")
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name
        if (info == null) {
            name= "Invalid SteamID64"
        } else {
            name= info.name
        }
        return "${super.getPageTitle()} - Match History - $name"
    }
    protected void fillHeader(def builder) {
        builder.h3("Match History")
    }

    protected String toXml(def builder) {
        def queryValues= Queries.parseQuery(queries)
        def attrs= [category: "sessions", steamid64: queries.steamid64]

        builder.kfstatsx() {
            'stats'(attrs) {
                WebCommon.partialQuery(reader, queryValues, false).each {row ->
                    row.remove("record_id")
                    row.remove("level_id")
                    row.remove("difficulty_id")
                    'entry'(row)
                }
            }
        }
    }
}
