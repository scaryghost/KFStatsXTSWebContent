import DataJson.DataTableCreator
import com.github.etsai.utils.Time

public RecordsCreator extends DataTableCreator {
    private final def reader, totalNumRecords

    public RecordsCreator(reader, queries) {
        super(queries)
        this.reader= reader
        this.totalNumRecords= reader.getNumRecords()
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
