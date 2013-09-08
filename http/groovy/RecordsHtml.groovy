import com.github.etsai.utils.Time

public class RecordsHtml extends PagedTable {
    public RecordsHtml() {
        super("records", "profile.html")

        aoColumnDefs= """[
                    { "sTitle": "Name", "sName": "name", "aTargets": [ 0 ] },
                    { "sTitle": "Wins", "sName": "wins", "aTargets": [ 1 ] },
                    { "sTitle": "Losses", "sName": "losses", "aTargets": [ 2 ] },
                    { "sTitle": "Disconnects", "sName": "disconnects", "aTargets": [ 3 ] },
                    { "sTitle": "Time Connected", "sName": "time", "aTargets": [ 4 ] }
                ]"""
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
                    WebCommon.partialQuery(reader, queries, true).each {record ->
                        def steamInfo= reader.getSteamIDInfo(record.steamid64)
                        def recordAttr= [name: steamInfo.name, wins: record.wins, losses: record.losses, disconnects: record.disconnects, time: record.time]
                        
                        builder.'record'(recordAttr) {
                            'formatted-time'(Time.secToStr(record.time))
                        }
                    }
                }
            }
        }
    }
}
