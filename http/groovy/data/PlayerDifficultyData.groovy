import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class PlayerDifficultyData extends GoogleChartsCreator {
    private final def columnNames= [["Level", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]]
    private final def reader, steamid64, difficulty, length

    public PlayerDifficultyData(parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.steamid64= parameters.queries.steamid64
        this.difficulty= parameters.queries.difficulty
        this.length= parameters.queries.length
    }

    public def getData() {
        def difficultydata= WebCommon.aggregateMatchHistory(reader.getMatchHistory(steamid64), false)
        def totals= [win: 0, loss: 0, disconnect:0, time:0]
        def data= []

        difficultydata[[difficulty, length]].each {level, stats ->
            data << [c: [[v: level]].plus(matchHistoryResults(stats))]
            stats.each {stat, value ->
                totals[stat]+= value
            }
        }
        data << [c: [[v: "Totals"], [v: totals.win, p: centerAlign], [v: totals.loss, p: centerAlign], [v: totals.disconnect, p: centerAlign], 
            [v: totals.time, f: Time.secToStr(totals.time), p: centerAlign]]]
        return data
    }
}
