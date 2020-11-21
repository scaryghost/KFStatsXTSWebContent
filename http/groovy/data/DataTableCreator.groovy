import scaryghost.kfsxtrackingserver.DefaultReader.Order
import groovy.json.JsonBuilder

public abstract class DataTableCreator implements DataCreator {
    protected def pageSize, start, end, order, group, sEcho
    public DataTableCreator(def queries) {
        pageSize= queries.iDisplayLength.toInteger()
        start= queries.iDisplayStart.toInteger()
        end= start + pageSize
        order= queries.containsKey("sSortDir_0") ? Order.valueOf(Order.class, queries.sSortDir_0.toUpperCase()) : 
            Order.NONE
        group= !queries.containsKey("iSortCol_0") ? null : queries.sColumns.tokenize(",")[queries.iSortCol_0.toInteger()]
        sEcho= queries.sEcho.toInteger()
    }

    public String create() {
        def builder= new JsonBuilder()

        builder {
            sEcho(this.sEcho)
            iTotalRecords(getNumTotalRecords())
            iTotalDisplayRecords(getNumFilteredRecords())
            aaData(getData())
        }
        return builder
    }
    public abstract int getNumTotalRecords()
    public abstract int getNumFilteredRecords()
}
