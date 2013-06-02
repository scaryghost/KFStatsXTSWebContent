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
