import DataJson.DataTableCreator
import com.github.etsai.utils.Time

public MatchHistoryCreator extends DataTableCreator {
    private final def reader, totalNumRecords, steamid64

    public MatchHistoryCreator(reader, queries) {
        super(queries)
        this.reader= reader
        this.steamid64= queries.steamid64
        this.totalNumRecords= reader.getMatchHistory(this.steamid64).size()
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
