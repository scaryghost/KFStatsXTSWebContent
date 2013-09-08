import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class Wave extends GoogleChartsCreator {
    private final def data

    public WaveData(parameters) {
        super()

        def waveSplit= [:], statKeys= new TreeSet()
        def waveData= parameters.queries.level == null ? parameters.reader.getWaveData(parameters.queries.difficulty, parameters.queries.length, parameters.queries.group) : 
                parameters.reader.getWaveData(parameters.queries.level, parameters.queries.difficulty, parameters.queries.length, parameters.queries.group)

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
