import scaryghost.utils.Time

public class Wave extends GoogleChartsCreator {
    private final def data

    public Wave(Map parameters) {
        super()

        def waveSplit= [:], statKeys= new TreeSet()
        def waveData= parameters.queries.level == null ? parameters.reader.executeQuery("server_wave_data", parameters.queries.difficulty, parameters.queries.length, parameters.queries.group) : 
                parameters.reader.executeQuery("server_level_wave_data", parameters.queries.level, parameters.queries.difficulty, parameters.queries.length, parameters.queries.group)

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
            [c: [[v:waveNum.toString()]].plus(statKeys.collect {key ->
                [v:stats[key] == null ? 0 : stats[key]]
            })]
        }
    }

    public def getData() {
        return data
    }
}
