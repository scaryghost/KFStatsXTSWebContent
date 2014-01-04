import com.github.etsai.utils.Time

public class ServerDifficulty extends GoogleChartsCreator {
    private final static def columnNames= [["Difficulty", "string"], ["Length", "string"], ["Wins", "number"],
            ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]]
    private final def reader

    public ServerDifficulty(Map parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
    }

    public def getData() {
        def totals= [wins: 0, losses: 0, time: 0]
        def data= []
        reader.executeQuery("server_difficulties").each {row ->
            def avgWave= WebCommon.computeAvgWave(row)

            data << [c: [[v: row.name, f:"<a href='wavedata.html?difficulty=${row.name}&length=${row.length}'>${row.name}</a>"], 
                    [v: row.length, p: centerAlign],
                    [v: row.wins, p: centerAlign],
                    [v: row.losses, p: centerAlign],
                    [v: avgWave, f: String.format("%.2f", avgWave), p: centerAlign],
                    [v: row.time, f: Time.secToStr(row.time), p: centerAlign]
            ]]
            totals.wins+= row.wins
            totals.losses+= row.losses
            totals.time+= row.time
        }
        data << [c: [[v: "Totals"], 
            [v: "", f: "------", p: centerAlign],
            [v: totals["wins"], p: centerAlign],
            [v: totals["losses"], p: centerAlign],
            [v: 0, f: "------", p: centerAlign],
            [v: totals["time"], f: Time.secToStr(totals["time"]), p: centerAlign],
        ]]

        return data
    }
}
