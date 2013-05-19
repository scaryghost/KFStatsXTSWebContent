/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time
import groovy.json.JsonBuilder

/**
 * Generates the json data for the page data.json
 * @author etsai
 */
public class DataJson implements Resource {
    private static def colStyle= "text-align:center"
    
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def columns
        def data= []
        def builder= new JsonBuilder()
        def queryValues= Queries.parseQuery(queries)

        switch(queryValues[Queries.table]) {
            case "difficulties":
                def totals= [wins: 0, losses: 0, time: 0]
                columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                    ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                reader.getDifficulties().each {row ->
                    data << [c: [[v: row.name, f:"<a href='wavedata.html?name=${row.name}&length=${row.length}'>${row.name}</a>"], 
                        [v: row.length, p:[style: colStyle]],
                        [v: row.wins, p:[style: colStyle]],
                        [v: row.losses, p:[style: colStyle]],
                        [v: String.format("%.2f",row.waveaccum / (row.wins + row.losses)), p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]]
                    ]]
                    totals["wins"]+= row.wins
                    totals["losses"]+= row.losses
                    totals["time"]+= row.time
                }
                data << [c: [[v: "Totals"], 
                    [v: "", f: "---", p:[style: colStyle]],
                    [v: totals["wins"], p:[style: colStyle]],
                    [v: totals["losses"], p:[style: colStyle]],
                    [v: 0, f: "---", p:[style: colStyle]],
                    [v: totals["time"], f: Time.secToStr(totals["time"]), p:[style: colStyle]],
                ]]
               break
            case "levels":
                def totals= [wins: 0, losses: 0, time: 0]
                columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                reader.getLevels().each {row ->
                    data << [c: [[v: row.name, f:"<a href='javascript:open(\"${row.name}\")'>${row.name}</a>"], 
                        [v: row.wins, p:[style: colStyle]],
                        [v: row.losses, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
                    ]]
                    totals["wins"]+= row.wins
                    totals["losses"]+= row.losses
                    totals["time"]+= row.time.toInteger()
                }
                data << [c: [[v: "Totals"], 
                    [v: totals["wins"], p:[style: colStyle]],
                    [v: totals["losses"], p:[style: colStyle]],
                    [v: totals["time"], f: Time.secToStr(totals["time"]), p:[style: colStyle]],
                ]]
                break
            case "difficultydata":
                def totals= [wins: 0, losses: 0, time: 0]
                columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                reader.getDifficultyData(queryValues[Queries.name], queryValues[Queries.length]).each {row ->
                    data << [c: [[v: row.name], 
                        [v: row.wins, p:[style: colStyle]],
                        [v: row.losses, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
                    ]]
                    totals["wins"]+= row.wins
                    totals["losses"]+= row.losses
                    totals["time"]+= row.time.toInteger()
                }
                data << [c: [[v: "Totals"], 
                    [v: totals["wins"], p:[style: colStyle]],
                    [v: totals["losses"], p:[style: colStyle]],
                    [v: totals["time"], f: Time.secToStr(totals["time"]), p:[style: colStyle]],
                ]]
                break
            case "records":
                columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"]].collect {
                    [label: it[0], type: it[1]]
                }

                WebCommon.partialQuery(reader, queryValues, true).each {row -> 
                    data << [c: [[v: row.name, f: "<a href=profile.html?steamid64=${row.steamid64}>${row.name}</a>"], 
                        [v: row.wins, p:[style: colStyle]],
                        [v: row.losses, p:[style: colStyle]],
                        [v: row.disconnects, p:[style: colStyle]]]]
                }
                break
            case "sessions":
                columns= [["Level", "string"], ["Difficulty", "string"], ["Length", "string"],
                        ["Result", "string"], ["Wave", "number"], ["Duration", "number"], ["Timestamp", "string"]].collect {
                    [label: it[0], type: it[1]]
                }
                WebCommon.partialQuery(reader, queryValues, false).each {row ->
                    data << [c: [[v: row.level], 
                        [v: row.difficulty, p:[style: colStyle]],
                        [v: row.length, p:[style: colStyle]],
                        [v: row.result, p:[style: colStyle]],
                        [v: row.wave, p:[style: colStyle]],
                        [v: row.duration, f: Time.secToStr(row.duration), p:[style: colStyle]],
                        [v: row.timestamp, p:[style: colStyle]]
                    ]]
                }
                break
            case "wave":
                def waveSplit= [:], statKeys= new TreeSet()
                def waveData= queryValues[Queries.level] == null ? reader.getWaveData(queryValues[Queries.name], queryValues[Queries.length], queryValues[Queries.group]) : 
                        reader.getWaveData(queryValues[Queries.level], queryValues[Queries.name], queryValues[Queries.length], queryValues[Queries.group])

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
                waveSplit.each {waveNum, stats ->
                    def values= [[v:waveNum.toString()]]
                    statKeys.each {key ->
                        values << [v:stats[key] == null ? 0 : stats[key]]
                    }
                    data << [c: values]
                }
                break;
            default:
                def query, psValues
                def results

                columns= [["Stat", "string"], ["Count", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                if (queryValues[Queries.steamid64] == null) {
                    results= reader.getAggregateData(queryValues[Queries.table])
                } else {
                    results= reader.getAggregateData(queryValues[Queries.table], queryValues[Queries.steamid64])
                }
                results.each {row ->
                    def fVal= null
                    def lower= row.stat.toLowerCase()

                    if (queryValues[Queries.table] == "perks" || lower.contains('time')) {
                        fVal= Time.secToStr(row.value)
                    }
                    data << [c: [[v: row.stat], 
                        [v: row.value, f: fVal, p:[style: colStyle]],
                    ]]
                }
                break
        }
        
        def root= builder {
            cols(columns)
            rows(data)
        }
        return builder
    }
}

