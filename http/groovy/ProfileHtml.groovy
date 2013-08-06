import com.github.etsai.utils.Time

public class ProfileHtml extends IndexHtml {
    private def veterancy

    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation[0]= "profile"
    }

    public void setQueries(Map<String, String> queries) {
        super.setQueries(queries)
        veterancy= new Veterancy(queries.steamid64)
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name= info == null ? "Player Not Found" : info.name
        return "${super.getPageTitle()} - $name"
    }

    protected void addDialogBox(def builder) {
        builder.div(id: 'dialog', title:'Perk Progression') {
            table(class: "perk-table") {
                def i= 0
                veterancy.getPerks().each {perk, requirements ->
                    tr() {
                        td(rowspan: "2") {
                            p() {
                                mkp.yieldUnescaped(perk.capitalize())
                                div(class: "progress-bar") {
                                    div(class: "status", id:"perk_${i}", "")
                                    i++
                                }
                            }
                        }
                        td() {
                            mkp.yieldUnescaped("Level ${veterancy.getLevel(perk)}<br>")
                        }
                    }
                    tr() {
                        td() {
                            requirements.each {name, progress ->
                                def j= veterancy.getLevel(perk) >= 6 ? progress.last() : progress[veterancy.getLevel(perk)]
                                mkp.yieldUnescaped("${veterancy.getPlayerProgress(name)}/$j ${Veterancy.niceNames[name]}<br>")
                            }
                        }
                    }
                }
            }
        }
    }

    protected String generateDialogJS() {
        def i= -1
        def js= veterancy.getPerks().collect {perk, requirements ->
            def percent= veterancy.calcProgress(perk)
            i++
            """            \$("#perk_${i}").animate( { width: "${percent}%" }, 500);\n"""
        }.join("")

        return """
        \$(function() {
            \$( "#dialog" ).dialog({
                autoOpen: false,
                position: {my: "left+15%", at: "left top+15%"},
                modal: true,
                width: document.getElementById('levels_div').offsetWidth * 0.985
            });
            \$( "#menu" ).menu();
        });
            
        function showPerkLevel(steamID64) {
            \$("#dialog").dialog("open");
${js}
        }
    """
    }

    protected void buildXml(def builder) {
        def steamid64= queries.steamid64
        def record= reader.getRecord(steamid64)
        def matchHistory= reader.getMatchHistory(steamid64)

        builder.kfstatsx() {
            if (record == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                def steamInfo= reader.getSteamIDInfo(steamid64)
                def profileAttr= [steamid64: steamid64, name: steamInfo.name, avatar: steamInfo.avatar, wins: record.wins, losses: record.losses,
                        disconnects: record.disconnects, finales_played: record.finales_played, finales_survived: record.finales_survived,
                        time: record.time]
                'profile'(steamid64: steamid64) {
                    'stats'(category: 'profile') {
                        profileAttr.each {
                            def attr= [stat: it.getKey(), value: it.getValue()]
                            'entry'(attr) {
                                if (attr.stat.toLowerCase().contains("time")) {
                                    'formatted-time'(Time.secToStr(attr.value))
                                }
                            }
                        }
                    }

                    'stats'(category: 'difficulties') {
                        def aggregateDiffs= WebCommon.aggregateMatchHistory(matchHistory, false)
                        WebCommon.aggregateCombineMatchHistory(matchHistory, false).each {key, stats ->
                            def attr= stats

                            attr.difficulty= key[0]
                            attr.length= key[1]
                            'entry'(attr) {
                                aggregateDiffs[[key[0], key[1]]].each {levelName, levelStats ->
                                    def levelAttrs= levelStats

                                    levelAttrs["name"]= levelName
                                    'level'(levelAttrs) {
                                        'formatted-time'(Time.secToStr(levelStats.time))
                                    }
                                }
                            }
                        }
                    }

                    'stats'(category: 'levels') {
                        def aggregateLevels= WebCommon.aggregateMatchHistory(matchHistory, true)
                        WebCommon.aggregateCombineMatchHistory(matchHistory, true).each {key, stats ->
                            def attr= stats
    
                            attr.level= key
                            'entry'(attr) {
                                aggregateLevels[key].each {diffSetting, diffStats ->
                                    def diffAttrs= diffStats

                                    diffAttrs["name"]= diffSetting[0]
                                    diffAttrs["length"]= diffSetting[1]
                                    'difficulty'(diffAttrs) {
                                        'formatted-time'(Time.secToStr(diffStats.time))
                                    }
                                }
                            }
                        }
                    }
                    reader.getStatCategories().each {category ->
                        'stats'(category: category) {
                            reader.getAggregateData(category, steamid64).each {row ->
                                def attr= [stat: row.stat, value: row.value]
                                builder.'entry'(attr) {
                                    if (category == "perks" || attr.stat.toLowerCase().contains("time")) {
                                        'formatted-time'(Time.secToStr(attr["value"]))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
