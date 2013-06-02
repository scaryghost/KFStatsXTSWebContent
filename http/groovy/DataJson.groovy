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
public class DataJson extends Resource {
    private static def colStyle= "text-align:center"
    
    public String generatePage() {
        def columns
        def data= []
        def builder= new JsonBuilder()
        def queryValues= Queries.parseQuery(queries)

        def matchHistoryResults= {stats ->
            ["win", "loss", "disconnect", "duration"].collect {
                [v: stats[it] == null ? 0 : stats[it]]
            }
        }

        switch(queryValues[Queries.table]) {
            case "difficulties":
                if (queryValues[Queries.steamid64] == null) {
                    def totals= [wins: 0, losses: 0, time: 0]
                    columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                        ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    reader.getDifficulties().each {row ->
                        data << [c: [[v: row.difficulty, f:"<a href='wavedata.html?difficulty=${row.difficulty}&length=${row.length}'>${row.difficulty}</a>"], 
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
                } else {
                    def difficulties= [:]
                    reader.getMatchHistory(queryValues[Queries.steamid64]).each {row ->
                        def difficulty= [row.difficulty, row.length]
                        if (difficulties[difficulty] == null) {
                            difficulties[difficulty]= [:]
                            difficulties[difficulty]["duration"]= 0
                        }
                        if (difficulties[difficulty][row.result] == null) {
                            difficulties[difficulty][row.result]= 0
                        }
                        difficulties[difficulty][row.result]++
                        difficulties[difficulty]["duration"]+= row.duration
                    }

                    columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                        ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    def totals= [win: 0, loss: 0, disconnect:0, duration:0]
                    difficulties.each {setting, stats ->
                        data << [c: [[v: setting[0], f:"<a href='javascript:open({\"table\":\"difficultydata\",\"difficulty\":\"${setting[0]}\",\"length\":\"${setting[1]}\",\"steamid64\":\"${queryValues[Queries.steamid64]}\"})'>${setting[0]}</a>"], [v: setting[1]]].plus(matchHistoryResults(stats))]
                        stats.each {stat, value ->
                            totals[stat]+= value
                        }
                    }
                    data << [c: [[v: "Totals"], [v: "------"], [v: totals.win], [v: totals.loss], [v: totals.disconnect], [v: Time.secToStr(totals.duration)]]]
                }
               break
            case "levels":
                if (queryValues[Queries.steamid64] == null) {
                    def totals= [wins: 0, losses: 0, time: 0]
                    columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    reader.getLevels().each {row ->
                        data << [c: [[v: row.level, f:"<a href='javascript:open({\"table\":\"leveldata\",\"level\":\"${row.level}\"})'>${row.level}</a>"], 
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
                } else {
                    def levels= [:]
                    reader.getMatchHistory(queryValues[Queries.steamid64]).each {row ->
                        if (levels[row.level] == null) {
                            levels[row.level]= [:]
                            levels[row.level]["duration"]= 0
                        }
                        if (levels[row.level][row.result] == null) {
                            levels[row.level][row.result]= 0
                        }
                        levels[row.level][row.result]++
                        levels[row.level]["duration"]+= row.duration
                    }

                    columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    def totals= [win: 0, loss: 0, disconnect:0, duration:0]
                    levels.each {level, stats ->
                        data << [c: [[v: level, f:"<a href='javascript:open({\"table\":\"leveldata\",\"level\":\"${level}\",\"steamid64\":\"${queryValues[Queries.steamid64]}\"})'>${level}</a>"]].plus(matchHistoryResults(stats))]
                        stats.each {stat, value ->
                            totals[stat]+= value
                        }
                    }
                    data << [c: [[v: "Totals"], [v: totals.win], [v: totals.loss], [v: totals.disconnect], [v: Time.secToStr(totals.duration)]]]
                }
                break
            case "leveldata":
                if (queryValues[Queries.steamid64] == null) {
                    def totals= [wins: 0, losses: 0, time: 0]
                    columns= [["Difficulty", "string"], ["Length", "string"], ["Wins", "number"], ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    reader.getLevelData(queryValues[Queries.level]).each {row ->
                        def avgWave= row.waveaccum / (row.wins + row.losses)
                        data << [c: [[v: row.difficulty, f:"<a href='wavedata.html?difficulty=${row.difficulty}&length=${row.length}&level=${queryValues[Queries.level]}' style='color:#0073BF'>${row.difficulty}</a>"], 
                                [v:row.length], [v: row.wins], [v: row.losses], [v:avgWave, f: String.format("%.2f",avgWave)], 
                                [v:row.time, f: Time.secToStr(row.time)]
                        ]]
                        totals.wins+= row.wins
                        totals.losses+= row.losses
                        totals.time+= row.time
                    }
                    data << [c: [[v: "Totals"], 
                        [v: "", f: "---"],
                        [v: totals["wins"]],
                        [v: totals["losses"]],
                        [v: 0, f: "---"],
                        [v: totals["time"], f: Time.secToStr(totals["time"])],
                    ]]
                } else {
                    def leveldata= [:]
                    reader.getMatchHistory(queryValues[Queries.steamid64]).each {row ->
                        if (row.level == queryValues[Queries.level]) {
                            def difficulty= [row.difficulty, row.length]
                            if (leveldata[difficulty] == null) {
                                leveldata[difficulty]= [:]
                                leveldata[difficulty]["duration"]= 0
                            }
                            if (leveldata[difficulty][row.result] == null) {
                                leveldata[difficulty][row.result]= 0
                            }
                            leveldata[difficulty][row.result]++
                            leveldata[difficulty]["duration"]+= row.duration
                        }
                    }

                    columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                        ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    def totals= [win: 0, loss: 0, disconnect:0, duration:0]
                    leveldata.each {setting, stats ->
                        data << [c: [[v: setting[0]], [v: setting[1]]].plus(matchHistoryResults(stats))]
                        stats.each {stat, value ->
                            totals[stat]+= value
                        }
                    }
                    data << [c: [[v: "Totals"], [v: "------"], [v: totals.win], [v: totals.loss], [v: totals.disconnect], [v: Time.secToStr(totals.duration)]]]
                }
                break
            case "difficultydata":
                if (queryValues[Queries.steamid64] == null) {
                    def totals= [wins: 0, losses: 0, time: 0]
                    columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    reader.getDifficultyData(queryValues[Queries.difficulty], queryValues[Queries.length]).each {row ->
                        data << [c: [[v: row.level], 
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
                } else {
                    def difficultydata= [:]
                    reader.getMatchHistory(queryValues[Queries.steamid64]).each {row ->
                        if (row.difficulty == queryValues[Queries.difficulty] && row.length == queryValues[Queries.length]) {
                            if (difficultydata[row.level] == null) {
                                difficultydata[row.level]= [:]
                                difficultydata[row.level]["duration"]= 0
                            }
                            if (difficultydata[row.level][row.result] == null) {
                                difficultydata[row.level][row.result]= 0
                            }
                            difficultydata[row.level][row.result]++
                            difficultydata[row.level]["duration"]+= row.duration
                        }
                    }

                    columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Disconnects", "number"], ["Time", "number"]].collect {
                        [label: it[0], type: it[1]]
                    }
                    def totals= [win: 0, loss: 0, disconnect:0, duration:0]
                    difficultydata.each {level, stats ->
                        data << [c: [[v: level]].plus(matchHistoryResults(stats))]
                        stats.each {stat, value ->
                            totals[stat]+= value
                        }
                    }
                    data << [c: [[v: "Totals"], [v: totals.win], [v: totals.loss], [v: totals.disconnect], [v: Time.secToStr(totals.duration)]]]
                }
                break
            case "records":
                columns= [["name", "Name", "string"], ["wins", "Wins", "number"], ["losses", "Losses", "number"], ["disconnects", "Disconnects", "number"], 
                ["time_connected", "Time Connected", "numner"]].collect {
                    [id: it[0], label: it[1], type: it[2]]
                }

                WebCommon.partialQuery(reader, queryValues, true).each {row -> 
                    data << [c: [[v: row.name, f: "<a href=profile.html?steamid64=${row.steamid64}>${row.name}</a>"], 
                        [v: row.wins, p:[style: colStyle]],
                        [v: row.losses, p:[style: colStyle]],
                        [v: row.disconnects, p:[style: colStyle]],
                        [v: row.time_connected, f: Time.secToStr(row.time_connected), p:[style: colStyle]]]]
                }
                break
            case "matchhistory":
                columns= [["Level", "string"], ["Difficulty", "string"], ["Length", "string"],
                        ["Result", "string"], ["Wave", "number"], ["Duration", "number"], ["Timestamp", "string"]].collect {
                    [id: it[0], label: it[0], type: it[1]]
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
                def waveData= queryValues[Queries.level] == null ? reader.getWaveData(queryValues[Queries.difficulty], queryValues[Queries.length], queryValues[Queries.group]) : 
                        reader.getWaveData(queryValues[Queries.level], queryValues[Queries.difficulty], queryValues[Queries.length], queryValues[Queries.group])

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

                columns= [[queryValues[Queries.table].capitalize(), "string"], ["Count", "number"]].collect {
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

