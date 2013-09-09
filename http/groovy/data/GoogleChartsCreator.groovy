import com.github.etsai.utils.Time
import groovy.json.JsonBuilder

public abstract class GoogleChartsCreator implements DataCreator {
    protected def columns
    protected def centerAlign= [style: "text-align:center"], leftAlign= [style: "text-align:left"]

    public GoogleChartsCreator() {
    }

    public GoogleChartsCreator(columns) {
        this.columns= columns
    }

    public def setColumns(columns) {
        this.columns= columns
    }

    public String create() {
        def builder= new JsonBuilder()

        builder {
            cols(columns)
            rows(getData())
        }
        return builder
    }

    protected def matchHistoryResults(stats) {
        ["win", "loss", "disconnect", "time"].collect {
            def fVal= (it == "time") ? Time.secToStr(stats[it]) : null
            def elem= [v: stats[it] == null ? 0 : stats[it], p: centerAlign]

            if (fVal != null) {
                elem.f= fVal
            }
            elem
        }
    }
}

