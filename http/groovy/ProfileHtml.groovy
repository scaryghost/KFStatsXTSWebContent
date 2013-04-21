import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class ProfileHtml extends IndexHtml {
    public ProfileHtml() {
        super()
        visualizations["sessions"]= visualizations["records"]
        visualizations["profile"]= visualizations["totals"]
        navLeft= ["profile"]
        navRight= ["sessions"]        
    }
}
