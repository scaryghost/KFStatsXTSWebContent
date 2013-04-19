import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class ProfileHtml implements Resource {
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def nav= ["profile"].plus(reader.getAggregateCategories()) << "sessions"
        return WebCommon.generateHtml(nav, queries["steamid64"])
    }
}
