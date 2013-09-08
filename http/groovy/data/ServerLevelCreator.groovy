import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class ServerLevelCreator extends GoogleChartsCreator {
    private final def columnNames= [["Level", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]]
    private final def reader
    
    public ServerLevelCreator(reader) {
        super(columnsNames.collect { [label: it[0], type: it[1]] })
        this.reader= reader
    }
    public def getData() {
        def totals= [wins: 0, losses: 0, time: 0]
        def data= []
        reader.getLevels().each {row ->
            data << [c: [[v: row.name, f:"<a href='javascript:open({\"table\":\"leveldata\",\"level\":\"${row.name}\"})'>${row.name}</a>"], 
                [v: row.wins, p: centerAlign],
                [v: row.losses, p: centerAlign],
                [v: row.time, f: Time.secToStr(row.time), p: centerAlign],
            ]]
            totals.wins+= row.wins
            totals.losses+= row.losses
            totals.time+= row.time
        }
        data << [c: [[v: "Totals"], 
            [v: totals.wins, p: centerAlign],
            [v: totals.losses, p: centerAlign],
            [v: totals.time, f: Time.secToStr(totals.time), p: centerAlign],
        ]]

        return data
    }
}
