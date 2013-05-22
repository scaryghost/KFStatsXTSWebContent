import com.github.etsai.utils.Time

public class ProfileHtml extends IndexHtml {
    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation= ["profile"]
    }

    public String getPageTitle() {
        def info= reader.getSteamIDInfo(queries.steamid64)
        def name
        if (info == null) {
            name= "Invalid SteamID64"
        } else {
            name= info.name
        }
        return "${super.getPageTitle()} - $name"
    }

    protected String toXml(def builder) {
        def steamid64= queries.steamid64
        def profileAttr= reader.getRecord(steamid64)
        
        builder.kfstatsx() {
            if (profileAttr == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                profileAttr.putAll(reader.getSteamIDInfo(steamid64))
                'profile'(profileAttr) {
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
