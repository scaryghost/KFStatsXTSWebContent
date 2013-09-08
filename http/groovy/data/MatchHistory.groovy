import DataJson.DataTableCreator
import com.github.etsai.utils.Time

public MatchHistory extends DataTableCreator {
    private final def reader, totalNumRecords, steamid64

    public MatchHistoryCreator(parameters) {
        super(parameters.queries)
        this.reader= parameters.reader
        this.steamid64= parameters.queries.steamid64
        this.totalNumRecords= parameters.reader.getMatchHistory(this.steamid64).size()
    }

    public def getData() {
        def data= []

        reader.getMatchHistory(queries.steamid64, group, order, start, end).each {row ->
            data << [row.level, row.difficulty, row.length, row.result, row.wave, 
                    Time.secToStr(row.duration), row.timestamp]
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
