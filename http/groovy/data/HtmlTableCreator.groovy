import groovy.xml.MarkupBuilder

public abstract class HtmlTableCreator implements DataCreator {
    protected static def tableAttr= [class: "content-table"]
    protected def builder, writer

    public HtmlTableCreator() {
        writer= new StringWriter()
        builder= new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
    }

    public def getData() {
        throw new UnsupportedOperationException("Operation not supported")
    }
}
