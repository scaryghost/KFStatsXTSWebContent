import com.github.etsai.utils.Time

public class Records extends DataTableCreator {
    private final def reader, totalNumRecords

    public Records(Map parameters) {
        super(parameters.queries)
        this.reader= parameters.reader
        this.totalNumRecords= parameters.reader.getNumRecords()
    }

    public def getData() {
        def data= []

        reader.getRecords(group, order, start, end).each {row ->
            def steamInfo= reader.getSteamIDInfo(row.steamid64)
            data << ["<a href=profile.html?steamid64=${row.steamid64}>${steamInfo.name}</a>", row.wins, 
                        row.losses, row.disconnects, Time.secToStr(row.time)]
        }
        return data
    }

    public int getNumTotalRecords() {
        return totalNumRecords
    }
    public int getNumFilteredRecords() {
        return totalNumRecords
    }
} 
