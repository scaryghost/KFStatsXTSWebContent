import scaryghost.utils.Time

public class PlayerLevelData extends GoogleChartsCreator {
    private final static def columnNames= [["Difficulty", "string"], ["Length", "string"], ["Wins", "number"],
            ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]]
    private final def reader, level, steamid64

    public PlayerLevelData(Map parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.level= parameters.queries.level
        this.steamid64= parameters.queries.steamid64
    }

    public def getData() {
        def leveldata= WebCommon.aggregateMatchHistory(reader.executeQuery("player_all_histories", steamid64), true)
        def totals= [win: 0, loss: 0, disconnect:0, time:0]
        def data= []

        leveldata[level].each {setting, stats ->
            data << [c: [[v: setting[0]], [v: setting[1]]].plus(matchHistoryResults(stats))]
            stats.each {stat, value ->
                totals[stat]+= value
            }
        }
        data << [c: [[v: "Totals"], [v: "------"], [v: totals.win, p: centerAlign], [v: totals.loss, p: centerAlign], [v: totals.disconnect, p: centerAlign], 
            [v: totals.time, f: Time.secToStr(totals.time), p: centerAlign]]]
        return data
    }
}
