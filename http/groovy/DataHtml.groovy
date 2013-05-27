/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.*
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

/**
 * Generates the html data for the page data.html
 * @author etsai
 */
public class DataHtml extends Resource {
    private static def tableAttr= [class: "content-table"]
    
    public String generatePage() {
        def writer= new StringWriter()
        def xml= new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
        def queryValues= Queries.parseQuery(queries)
        
        switch(queryValues[Queries.table]) {
            case "totals":
                xml.center() {
                    table(tableAttr) {
                        tbody() {
                            WebCommon.generateSummary(reader).each {attr ->
                                tr() {
                                    td(attr['name'])
                                    td(attr['value'])
                                }
                            }
                            tr() {
                                td(colspan: "2") {
                                    a(href: "records.html", "View players")
                                }
                            }
                        }
                    }
                }
                break
            case "profile":
                def steamid64= queryValues[Queries.steamid64]
                def row= reader.getRecord(steamid64);

                if (row == null) {
                    xml.center("No records found for SteamID64: " + steamid64)
                } else {
                    def steamIdInfo= reader.getSteamIDInfo(steamid64)

                    xml.center() {
                        table(tableAttr) {
                            tbody() {
                                tr() {
                                    td("Name")
                                    td(colspan: "2", steamIdInfo.name)
                                }
                                tr() {
                                    td("Wins")
                                    td(row.wins)
                                    td(rowspan: "7", align: "center") {
                                        img(src: steamIdInfo.avatar)
                                    }
                                }
                                tr() {
                                    td("Losses")
                                    td(row.losses)
                                }
                                tr() {
                                    td("Disconnects")
                                    td(row.disconnects)
                                }
                                tr() {
                                    td("Finales Played")
                                    td(row.finales_played)
                                }
                                tr() {
                                    td("Finales Survived")
                                    td(row.finales_survived)
                                }
                                tr() {
                                    td("Time Connected")
                                    td(Time.secToStr(row.time_connected))
                                }
                                tr() {
                                    td(colspan: "2") {
                                        a(target: "_blank", href: "http://steamcommunity.com/profiles/" + steamid64, "Steam Community Page")
                                    }
                                }
                                tr() {
                                    td(colspan:"3") {
                                        a(target: "_blank", href: "matchhistory.html?steamid64=" + steamid64, "Match History")
                                    }
                                }
                            }
                        }
                    }
                }
                break
        }
        return writer
    }
}

