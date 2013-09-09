import com.github.etsai.utils.Time

public class PlayerLevel extends GoogleChartsCreator {
    private final static def columnNames= [["Level", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]]
    private final def reader, steamid64

    public PlayerLevel(Map parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.steamid64= parameters.queries.steamid64
    }
    public def getData() {
        def levels= WebCommon.aggregateCombineMatchHistory(reader.getMatchHistory(steamid64), true)
        def totals= [win: 0, loss: 0, disconnect:0, time:0]
        def data= []

        levels.each {level, stats ->
            data << [c: [[v: level, f:"<a href='javascript:open({\"table\":\"leveldata\",\"level\":\"${level}\",\"steamid64\":\"${steamid64}\"})'>${level}</a>"]].plus(matchHistoryResults(stats))]
            stats.each {stat, value ->
                totals[stat]+= value
            }
        }
        data << [c: [[v: "Totals"], [v: totals.win, p: centerAlign], [v: totals.loss, p: centerAlign], [v: totals.disconnect, p: centerAlign], 
            [v: totals.time, f: totals.time, f: Time.secToStr(totals.time), p: centerAlign]]]
        return data
    }
}
