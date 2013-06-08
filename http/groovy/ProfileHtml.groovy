import com.github.etsai.utils.Time

public class ProfileHtml extends IndexHtml {
    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation[0]= "profile"
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name= info == null ? "Invalid SteamID64" : info.name
        return "${super.getPageTitle()} - $name"
    }

    protected void buildXml(def builder) {
        def steamid64= queries.steamid64
        def profileAttr= reader.getRecord(steamid64)
        def matchHistory= reader.getMatchHistory(steamid64)

        builder.kfstatsx() {
            if (profileAttr == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                profileAttr.remove('steamid64')
                profileAttr.putAll(reader.getSteamIDInfo(steamid64))
                'profile'(steamid64: steamid64) {
                    'stats'(category: 'profile') {
                        profileAttr.each {
                            def attr= [stat: it.getKey(), value: it.getValue()]
                            if (attr.stat.toLowerCase().contains("time")) {
                                attr["formatted"]= Time.secToStr(attr.value)
                            }
                            'entry'(attr)
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

                                    levelAttrs["formatted-time"]= Time.secToStr(levelStats.time)
                                    levelAttrs["name"]= levelName
                                    'level'(levelAttrs)
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

                                    diffAttrs["formatted-time"]= Time.secToStr(diffStats.time)
                                    diffAttrs["name"]= diffSetting[0]
                                    diffAttrs["length"]= diffSetting[1]
                                    'difficulty'(diffAttrs)
                                }
                            }
                        }
                    }
                    reader.getAggregateCategories().each {category ->
                        'stats'(category: category) {
                            reader.getAggregateData(category, steamid64).each {row ->
                                def attr= row
                                if (category == "perks" || attr.stat.toLowerCase().contains("time")) {
                                    attr["formatted"]= Time.secToStr(attr["value"])
                                }
                                attr.remove("record_id")
                                attr.remove("category")
                                builder.'entry'(attr)
                            }
                        }
                    }
                }
            }
        }
    }
}
