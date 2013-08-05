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

                if (row == null) {
                    xml.center("No records found for SteamID64: " + steamid64)
                } else {
                    def steamIdInfo= reader.getSteamIDInfo(steamid64)
                    xml.table(tableAttr) {
                        thead() {
                            tr() {
                                th(colspan: "3") {
                                    h2(steamIdInfo.name)
                                }
                            }
                        }
                        tbody() {
                            tr(class: "odd-row") {
                                td("Wins")
                                td(row.wins)
                                td(rowspan: "7", align: "center") {
                                    img(src: steamIdInfo.avatar)
                                }
                            }
                            tr(class: "even-row") {
                                td("Losses")
                                td(row.losses)
                            }
                            tr(class: "odd-row") {
                                td("Disconnects")
                                td(row.disconnects)
                            }
                            tr(class: "even-row") {
                                td("Finales Played")
                                td(row.finales_played)
                            }
                            tr(class: "odd-row") {
                                td("Finales Survived")
                                td(row.finales_survived)
                            }
                            tr(class: "even-row") {
                                td("Time Connected")
                                td(Time.secToStr(row.time))
                            }
                            tr(class: "odd-row") {
                                td(colspan: "2") {
                                    ul(class: "nav-list") {
                                        li(class: "nav-list") {
                                            a(target: "_blank", href: "http://steamcommunity.com/profiles/" + steamid64, "Steam Community Page")
                                        }
                                        li(class: "nav-list") {
                                            a(target: "_blank", href: "matchhistory.html?steamid64=" + steamid64, "Match History")
                                        }
                                        li(class: "nav-list") {
                                            a(href: 'javascript:showPerkLevel(' + steamid64 + ')', "Perk Progression")
                                        }
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

