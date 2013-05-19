import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class SessionsHtml extends RecordsHtml {
    public SessionsHtml() {
        super("sessions")
    }
    protected void fillHeader(def builder) {
        builder.h3("Match History")
    }
}
