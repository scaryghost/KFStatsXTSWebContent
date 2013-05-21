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
}
