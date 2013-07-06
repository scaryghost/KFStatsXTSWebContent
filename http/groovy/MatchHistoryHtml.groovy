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
        builder.kfstatsx() {
            'profile'(steamid64: queries.steamid64) {
                'stats'(category: "sessions") {
                    WebCommon.partialQuery(reader, queries, false).each {match ->
                        'entry'(level: match.level, difficulty: match.difficulty, length: match.length, result: match.result, wave: match.wave, 
                            duration: match.duration, timestamp: match.timestamp)
                    }
                }
            }
        }
    }
}
