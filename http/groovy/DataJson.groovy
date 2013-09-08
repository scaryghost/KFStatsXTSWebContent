/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time
import groovy.json.JsonBuilder

/**
 * Generates the json data for the page data.json
 * @author etsai
 */
public class DataJson extends Resource {
    private static def centerAlign= [style: "text-align:center"], leftAlign= [style: "text-align:left"]

    public interface DataCreator {
        public String create()
        public def getData()
    }

    public abstract class GoogleChartsCreator implements DataCreator {
        private def columns

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

    public abstract class DataTableCreator implements DataCreator {
        protected def pageSize, start, end, order, group, sEcho

        public DataTableCreator(def queries) {
            pageSize= queries.iDisplayLength.toInteger()
            start= queries.iDisplayStart.toInteger()
            end= start + pageSize
            order= Order.valueOf(Order.class, queries.sSortDir_0.toUpperCase())
            group= queries.iSortCol_0 == null ? null : queries.sColumns.tokenize(",")[queries.iSortCol_0.toInteger()]
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
    
    public String generatePage() {
    }
}

