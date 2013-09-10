import com.github.etsai.utils.Time

public class MatchHistory extends DataTableCreator {
    private final def reader, totalNumRecords, steamid64

    public MatchHistory(Map parameters) {
        super(parameters.queries)
        this.reader= parameters.reader
        this.steamid64= parameters.queries.steamid64
        this.totalNumRecords= parameters.reader.getNumMatches(this.steamid64)
    }

    public def getData() {
        def data= []

        reader.getMatchHistory(steamid64, group, order, start, end).each {row ->
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
