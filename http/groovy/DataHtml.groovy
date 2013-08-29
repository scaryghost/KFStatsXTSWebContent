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
        
        switch(queries.table) {
            case "totals":
                def odd= true
                def fillTr= {
                    [class: (odd ? "odd-row" : "even-row")]
                }
                xml.table(tableAttr) {
                    thead() {
                        tr() {
                            th(colspan: "2") {
                                h2("Server Totals")
                            }
                        }
                    }
                    tbody() {
                        WebCommon.generateSummary(reader).each {attr ->
                            tr(fillTr()) {
                                td(attr['name'])
                                td(attr['value'])
                            }
                            odd= !odd
                        }
                        tr(fillTr()) {
                            td(colspan: "2") {
                                a(href: "records.html", "View players")
                            }
                        }
                    }
                }
                break
            case "profile":
                def steamid64= queries.steamid64
                def row= reader.getRecord(steamid64);
                def keys= [wins: "Wins", losses: "Losses", disconnects: "Disconnects", finales_played: "Finales Played", 
                    finales_survived: "Finales Survived", time: "Time Connected"]
                def stats= [:]
                def steamIdInfo

                if (row == null) {
                    keys.each {key, title ->
                        stats[key]= "---"
                    }
                    steamIdInfo= [name: "No record for $steamid64", 
                        avatar: "http://media.steampowered.com/steamcommunity/public/images/avatars/fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg"]
                } else {
                    keys.each {key, title ->
                        stats[key]= row[key]
                    }
                    steamIdInfo= reader.getSteamIDInfo(steamid64)
                }
                xml.table(tableAttr) {
                    thead() {
                        tr() {
                            th(colspan: "3") {
                                h2(steamIdInfo.name)
                            }
                        }
                    }
                    xml.tbody() {
                        def even= false, first= true
                        stats.each {stat, value ->
                            xml.tr(class: (even ? "even-row" : "odd-row")) {
                                td(keys[stat])
                                td(stat.contains("time") ? (row == null ? "---" : Time.secToStr(value)) : value)
                                if (first) {
                                    td(rowspan: "7", align: "center") {
                                        img(src: steamIdInfo.avatar)
                                    }
                                    first= false
                                }
                            }
                            even= !even
                        }
                        tr(class: (even ? "even-row" : "odd-row")) {
                            td(colspan: "2") {
                                ul(class: "nav-list") {
                                    li(class: "nav-list") {
                                        a(target: "_blank", href: "http://steamcommunity.com/profiles/$steamid64", "Steam Community Page")
                                    }
                                    li(class: "nav-list") {
                                        a(target: "_blank", href: "matchhistory.html?steamid64=$steamid64", "Match History")
                                    }
                                    li(class: "nav-list") {
                                        a(href: "javascript:showPerkLevel('$steamid64')", "Perk Progression")
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

