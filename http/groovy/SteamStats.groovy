import groovy.xml.MarkupBuilder

def damages= [25000:1 , 100000:2, 500000:3, 1500000:4, 3500000:5, 5500000:6] 
def perks= [medic: [damagehealed: [200:1, 750:2, 4000:3, 12000:4, 25000:5, 100000:6]], 
    sharp: [headshotkills: [30:1, 100:2, 700:3, 2500:4, 5500:5, 8500:6]], 
    support: [shotgundamage: damages, weldingpoints: [2000:1, 7000:2, 35000:3, 120000:4, 250000:5, 370000:6]], 
    commando: [bullpupdamage: damages, stalkerkills: [30:1, 100:2, 350:3, 1200:4, 2400:5, 3600:6]], 
    berserker: [meleedamage: damages], firebug: [flamethrowerdamage: damages], demo: [explosivesdamage: damages]]

def url= new URL("http://steamcommunity.com/profiles/${args[0]}/statsfeed/1250")
def content= url.getContent().readLines().join("\n")
def xmlRoot= new XmlSlurper().parseText(content)

def getLevel= {requirements ->
    def totalLevel= 6
    requirements.each {apiName, progress ->
        def item= xmlRoot.stats.item.find { it.APIName.text() == apiName }
        def maxLevel= 0;
        progress.each {amount, level ->
            if (amount <= item.value.text().toInteger()) {
                maxLevel= level
            }
        }
        if (maxLevel < totalLevel) {
            totalLevel= maxLevel
        }
    }
    return totalLevel
}

def calcProgress= {level, requirements ->
    def totalPercent= 0

    requirements.each {apiName, progress ->
        def item= xmlRoot.stats.item.find { it.APIName.text() == apiName }
        def maxPercent= 0
        progress.keySet().each {amount ->
            def percent= [item.value.text().toFloat() / amount, 1.0].min()
            if (percent > maxPercent) {
                maxPercent= percent
            }
        }
        totalPercent+= maxPercent / requirements.keySet().size()
    }
    return totalPercent
}

def writer= new StringWriter()
def htmlBuilder= new MarkupBuilder(writer)

def css= """
        .progress-bar {border:1px solid #bebebe; background:#ffffff; width:300px; height:14px; -moz-border-radius:10px; -webkit-border-radius:10px; -khtml-border-radius:10px; border-radius:10px;}
        .status {background:#0066cc; width:0%; height:14px; -moz-border-radius:10px; -webkit-border-radius:10px; -khtml-border-radius:10px; border-radius:10px;}
    """

htmlBuilder.html() {
    head() {
        meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
        style(css)
        script(type:'text/javascript', src:"http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js", '')
        script(type:'text/javascript') {
            def i= -1
            def js= perks.collect {perk, requirements ->
                def percent= (calcProgress(getLevel(requirements), requirements) * 100).toInteger()
                i++
                """\$("#perk_${i}").animate( { width: "${percent}%" }, 500);\n"""
            }.join("")
            mkp.yieldUnescaped("\$(function() { $js });")
        }
    }
    body() {
        def i= 0
        perks.keySet().each {perk ->
            div(class: "progress-bar") {
                div(class: "status", id:"perk_${i}", "")
                i++
            }
        }
    }
}
    
println writer
