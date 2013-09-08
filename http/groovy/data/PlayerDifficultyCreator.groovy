import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class PlayerDifficultyCreator extends GoogleChartsCreator {
    private final def columnInfo= [["Difficulty", "string"], ["Length", "string"], ["Wins", "number"],
            ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]]
    private final def steamid64

    public PlayerDifficultyCreator(reader, queries) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= reader
        this.steamid64= queries.steamid64
    }

    public def getData() {
        def data= []
        def difficulties= WebCommon.aggregateCombineMatchHistory(reader.getMatchHistory(steamid64), false)

        def totals= [win: 0, loss: 0, disconnect:0, time:0]
        difficulties.each {setting, stats ->
            data << [c: [[v: setting[0], f:"<a href='javascript:open({\"table\":\"difficultydata\",\"title\":\"${setting[0]} - ${setting[1]}\",\"difficulty\":\"${setting[0]}\",\"length\":\"${setting[1]}\",\"steamid64\":\"${steamid64}\"})'>${setting[0]}</a>"], [v: setting[1]]].plus(matchHistoryResults(stats))]
            stats.each {stat, value ->
                totals[stat]+= value
            }
        }
        data << [c: [[v: "Totals"], [v: "------"], [v: totals.win, p: centerAlign], [v: totals.loss, p: centerAlign], [v: totals.disconnect, p: centerAlign], 
            [v: totals.time, f: Time.secToStr(totals.time), p: centerAlign]]]

        return data
    }
}
