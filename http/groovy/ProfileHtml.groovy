import com.github.etsai.utils.Time

public class ProfileHtml extends IndexHtml {
    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation[0]= "profile"
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name= info == null ? "Player Not Found" : info.name
        return "${super.getPageTitle()} - $name"
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
