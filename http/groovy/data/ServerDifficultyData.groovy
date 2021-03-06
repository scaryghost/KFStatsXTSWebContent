import scaryghost.utils.Time

public class ServerDifficultyData extends GoogleChartsCreator {
    private static final def columnNames= [["Level", "string"], ["Wins", "number"], ["Losses", "number"], 
            ["Avg Wave", "number"], ["Time", "number"]]
    private final def reader, difficulty, length

    public ServerDifficultyData(Map parameters) {
        super(columnNames.collect { [label: it[0], type: it[1]] })
        this.reader= parameters.reader
        this.difficulty= parameters.queries.difficulty
        this.length= parameters.queries.length
    }

    public def getData() {
        def totals= [wins: 0, losses: 0, time: 0, wave_sum: 0]
        def data= []

        reader.executeQuery("server_difficulty_data", difficulty, length).each {row ->
            def avgWave= WebCommon.computeAvgWave(row)

            data << [c: [[v: row.level, f:"<a href='wavedata.html?difficulty=${difficulty}&length=${length}&level=${row.level}'>${row.level}</a>"], 
                [v: row.wins, p: centerAlign],
                [v: row.losses, p: centerAlign],
                [v: avgWave, f: String.format("%.2f", avgWave), p: centerAlign],
                [v: row.time, f: Time.secToStr(row.time), p: centerAlign]
            ]]
            totals.wins+= row.wins
            totals.losses+= row.losses
            totals.time+= row.time
            totals.wave_sum+= row.wave_sum
        }
        def totalGames= totals.wins + totals.losses
        def totalAvgWave= (totalGames == 0) ? 0 : totals.wave_sum / (totalGames)
        data << [c: [[v: "Totals"], 
            [v: totals.wins, p: centerAlign],
            [v: totals.losses, p: centerAlign],
            [v: totalAvgWave, f: String.format("%.2f", totalAvgWave), p: centerAlign],
            [v: totals.time, f: Time.secToStr(totals.time), p: centerAlign]
        ]]
        return data
    }
}
