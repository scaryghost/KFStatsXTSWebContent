import DataJson.GoogleChartsCreator
import com.github.etsai.utils.Time

public class Default extends GoogleChartsCreator {
    private final def data

    public DefaultData(reader,queries) {
        super()
        columns= [[queries.table.capitalize(), "string"], ["Count", "number"]].collect {
            [label: it[0], type: it[1]]
        }

        def results= (queries.steamid64 == null) ?
            reader.getAggregateData(queries.table) :
            reader.getAggregateData(queries.table, queries.steamid64)
        data= results.collect {row ->
            def fVal= null
            def lower= row.stat.toLowerCase()

            if (queries.table == "perks" || lower.contains('time')) {
                fVal= Time.secToStr(row.value)
            }
            [c: [[v: row.stat], [v: row.value, f: fVal, p: centerAlign]]]
        }
    }

    public def getData() {
        return data
    }
}
