public class MatchHistoryHtml extends PagedTable {
    public MatchHistoryHtml() {
        super("matchhistory", "matchhistory.html")
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

    protected void buildXml(def builder) {
        def queryValues= Queries.parseQuery(queries)

        builder.kfstatsx() {
            'profile'(steamid64: queries.steamid64) {
                'stats'(category: "sessions") {
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
}
