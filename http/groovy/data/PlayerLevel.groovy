import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class PlayerLevel extends GoogleChartsCreator {
    private final def columnsName= [["Level", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]]
    private final def reader, steamid64

    public PlayerLevel(parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.steamid64= parameters.queries.steamid64
    }
    public def getData() {
        def levels= WebCommon.aggregateCombineMatchHistory(reader.getMatchHistory(queries.steamid64), true)
        def totals= [win: 0, loss: 0, disconnect:0, time:0]
        levels.each {level, stats ->
            data << [c: [[v: level, f:"<a href='javascript:open({\"table\":\"leveldata\",\"level\":\"${level}\",\"steamid64\":\"${queries.steamid64}\"})'>${level}</a>"]].plus(matchHistoryResults(stats))]
            stats.each {stat, value ->
                totals[stat]+= value
            }
        }
        data << [c: [[v: "Totals"], [v: totals.win, p: centerAlign], [v: totals.loss, p: centerAlign], [v: totals.disconnect, p: centerAlign], 
            [v: totals.time, f: totals.time, f: Time.secToStr(totals.time), p: centerAlign]]]
        return data
    }
}
