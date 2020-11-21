import scaryghost.utils.Time

public class ServerLevelData extends GoogleChartsCreator {
    private static final def columnNames= [["Difficulty", "string"], ["Length", "string"], ["Wins", "number"], ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]]
    private final def reader, level

    public ServerLevelData(Map parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.level= parameters.queries.level
    }

    public def getData() {
        def totals= [wins: 0, losses: 0, time: 0]
        def data= []

        reader.executeQuery("server_level_data", level).each {row ->
            def genHref= {
                "wavedata.html?difficulty=${row.difficulty}&length=${row.length}&level=${level}"
            }
            def avgWave= WebCommon.computeAvgWave(row)
            data << [c: [[v: row.difficulty, f:"<a href='${genHref()}' style='color:#0073BF'>${row.difficulty}</a>"], 
                    [v:row.length], [v: row.wins], [v: row.losses], [v:avgWave, f: String.format("%.2f",avgWave)], 
                    [v:row.time, f: Time.secToStr(row.time)]
            ]]
            totals.wins+= row.wins
            totals.losses+= row.losses
            totals.time+= row.time
        }
        data << [c: [[v: "Totals"], [v: "", f: "---"], [v: totals.wins], [v: totals.losses], [v: 0, f: "---"], 
                [v: totals.time, f: Time.secToStr(totals.time)],
        ]]
        return data
    }
}
