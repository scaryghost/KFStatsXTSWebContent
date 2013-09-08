import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class Wave extends GoogleChartsCreator {
    private final def data

    public WaveData(reader, queries) {
        super()

        def waveSplit= [:], statKeys= new TreeSet()
        def waveData= queries.level == null ? reader.getWaveData(queries.difficulty, queries.length, queries.group) : 
                reader.getWaveData(queries.level, queries.difficulty, queries.length, queries.group)

        waveData.each {row ->
            statKeys << row.stat
            if (waveSplit[row.wave] == null) {
                waveSplit[row.wave]= [:]
            }
            waveSplit[row.wave][row.stat]= row.value
        }
        columns= [["Wave", "string"]]
        statKeys.each {key ->
            columns << [key, "number"]
        }
        columns= columns.collect{[label: it[0], type: it[1]]}

        data= waveSplit.collect {waveNum, stats ->
            [c: statKeys.collect { [v:stats[it] == null ? 0 : stats[it] }, [v:waveNum.toString()]]
        }
    }

    public def getData() {
        return data
    }
}
