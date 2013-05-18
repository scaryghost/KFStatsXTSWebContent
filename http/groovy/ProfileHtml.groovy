import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class ProfileHtml extends IndexHtml {
    public ProfileHtml() {
        super()
        htmlDiv << "profile"
        navigation= ["profile"]
    }
}
