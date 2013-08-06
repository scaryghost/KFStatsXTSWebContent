import com.github.etsai.utils.Time

public class ProfileHtml extends IndexHtml {
    private static def damages= [25000, 100000, 500000, 1500000, 3500000, 5500000]
    private static def perks= [medic: [damagehealed: [200, 750, 4000, 12000, 25000, 100000]], 
        sharp: [headshotkills: [30, 100, 700, 2500, 5500, 8500]], 
        support: [shotgundamage: damages, weldingpoints: [2000, 7000, 35000, 120000, 250000, 370000]], 
        commando: [bullpupdamage: damages, stalkerkills: [30, 100, 350, 1200, 2400, 3600]], 
        berserker: [meleedamage: damages], firebug: [flamethrowerdamage: damages], demo: [explosivesdamage: damages]]
    private static def niceNames= [damagehealed: "Healing", headshotkills: "Head Shots", shotgundamage: "Shotgun Damage", weldingpoints: "Welding", 
        bullpupdamage: "Assault Rifle Damage", stalkerkills: "Stalkers Killed", meleedamage: "Melee Damage", flamethrowerdamage: "Fire Damage", 
        explosivesdamage: "Explosives Damage"]
    private def playerProgress= [:], perkLevels= [:], xmlRoot

    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation[0]= "profile"
    }

    public void setQueries(Map<String, String> queries) {
        super.setQueries(queries)
        def url= new URL("http://steamcommunity.com/profiles/${queries.steamid64}/statsfeed/1250")
        def content= url.getContent().readLines().join("\n")
        xmlRoot= new XmlSlurper().parseText(content)
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name= info == null ? "Player Not Found" : info.name
        return "${super.getPageTitle()} - $name"
    }

    private def getProgress(level, progress) {
    def amount
    if (level >= progress.size()) {
        amount= progress.last() * (level - progress.size() + 2)
    } else {
        amount= progress[level]
    }
    amount
}
private def getLevel(requirements) {
    def totalLevels= []
    requirements.each {apiName, progress ->
        def item= xmlRoot.stats.item.find { it.APIName.text() == apiName }
        def level= 0, amount
        playerProgress[apiName]= item.value.text().toInteger()

        amount= getProgress(level, progress)
        level++
        while(amount <= playerProgress[apiName]) {
            amount= getProgress(level, progress)
            level++
        }
        totalLevels << level - 1
    }
    return totalLevels.min()
}

private def calcProgress(level, requirements) {
    def totalPercent= 0

    requirements.each {apiName, progress ->
        def percent= level > 0 ? [((playerProgress[apiName] - getProgress(level - 1, progress)) * 100) / (getProgress(level, progress) - getProgress(level - 1, progress)), 100].min() :
            [(playerProgress[apiName] * 100) / getProgress(level, progress), 100].min()
        totalPercent+= percent / requirements.keySet().size()
    }
    return totalPercent
}




    protected void addDialogBox(def builder) {
        builder.div(id: 'dialog_perks', title:'Perk Progress') {
            table(class: "perk-table") {
                def i= 0
                perks.each {perk, requirements ->
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
                            mkp.yieldUnescaped("Level ${perkLevels[perk]}<br>")
                        }
                    }
                    tr() {
                        td() {
                            requirements.each {name, progress ->
                                def j= perkLevels[perk] >= 6 ? progress.last() : progress[perkLevels[perk]]
                                mkp.yieldUnescaped("${playerProgress[name]}/$j ${niceNames[name]}<br>")
                            }
                        }
                    }
                }
            }
        }
    }

    protected void generateDialogJS() {
        def i= -1
        def js= perks.collect {perk, requirements ->
            perkLevels[perk]= getLevel(requirements)
            def percent= calcProgress(perkLevels[perk], requirements)
            i++
            """\$("#perk_${i}").animate( { width: "${percent}%" }, 500);\n"""
        }.join("")

        return """
            \$(function() {
                \$( "#dialog_perks" ).dialog({
                    autoOpen: false,
                    position: {my: "left+15%", at: "left top+15%"},
                    modal: true,
                    width: document.getElementById('levels_div').offsetWidth * 0.985
                });
            });
            
            function showPerkLevel(steamID64) {
                \$("#dialog_perks").dialog("open");
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
