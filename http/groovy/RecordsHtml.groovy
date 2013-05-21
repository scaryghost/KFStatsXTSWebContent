public class RecordsHtml extends PagedTable {
    public RecordsHtml() {
        super("records", "profile.html")
    }

    public String getPageTitle() {
        return "${super.getPageTitle()} - Player Records"
    }

    protected void fillHeader(def builder) {
        builder.h3("Player Records")
    }

}
