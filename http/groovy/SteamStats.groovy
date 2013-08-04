import groovy.xml.MarkupBuilder

def damages= [25000, 100000, 500000, 1500000, 3500000, 5500000, 11000000]
def perks= [medic: [damagehealed: [200, 750, 4000, 12000, 25000, 100000, 200000]], 
    sharp: [headshotkills: [30, 100, 700, 2500, 5500, 8500, 17000]], 
    support: [shotgundamage: damages, weldingpoints: [2000, 7000, 35000, 120000, 250000, 370000, 740000]], 
    commando: [bullpupdamage: damages, stalkerkills: [30, 100, 350, 1200, 2400, 3600, 7200]], 
    berserker: [meleedamage: damages], firebug: [flamethrowerdamage: damages], demo: [explosivesdamage: damages]]
def niceNames= [damagehealed: "Healing", headshotkills: "Head Shots", shotgundamage: "Shotgun Damage", weldingpoints: "Welding", 
        bullpupdamage: "Assault Rifle Damage", stalkerkills: "Stalkers Killed", meleedamage: "Melee Damage", flamethrowerdamage: "Fire Damage", 
        explosivesdamage: "Explosives Damage"]
def playerProgress= [:]
def perkLevels= [:]
/*
def url= new URL("http://steamcommunity.com/profiles/${args[0]}/statsfeed/1250")
def content= url.getContent().readLines().join("\n")
*/
def xmlRoot= new XmlSlurper().parse(new File(args[0]))

def getLevel= {requirements ->
    def totalLevel= 6
    requirements.each {apiName, progress ->
        def item= xmlRoot.stats.item.find { it.APIName.text() == apiName }
        def maxLevel= 0, level= 1
        progress.each {amount ->
            playerProgress[apiName]= item.value.text()
            if (amount <= item.value.text().toInteger()) {
                maxLevel= level
            }
            level++
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
        def percent= level > 0 ? [(item.value.text().toFloat() - progress[level - 1]) / (progress[level] - progress[level - 1]), 1.0].min() :
            [item.value.text().toFloat() / progress[level], 1.0].min()
        totalPercent+= percent / requirements.keySet().size()
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
                perkLevels[perk]= getLevel(requirements)
                def percent= (calcProgress(perkLevels[perk], requirements) * 100).toInteger()
                i++
                """\$("#perk_${i}").animate( { width: "${percent}%" }, 500);\n"""
            }.join("")
            mkp.yieldUnescaped("\$(function() { $js });")
        }
    }
    body() {
        table(border: "1") {
            def i= 0
            perks.each {perk, requirements ->
                tr() {
                    td(rowspan: "2") {
                        p() {
                            mkp.yieldUnescaped(perk.capitalize())
                            div(class: "progress-bar") {
                                div(class: "status", id:"perk_${i}", "")
                                i++
                            }
                        }
                    }
                    td() {
                        mkp.yieldUnescaped("Level ${perkLevels[perk]}<br>")
                    }
                }
                tr() {
                    td() {
                        requirements.each {name, progress ->
                            def j= progress[[perkLevels[perk], 5].min()]
                            mkp.yieldUnescaped("${playerProgress[name]}/$j ${niceNames[name]}<br>")
                        }
                    }
                }
            }
        }
    }
}
    
print writer
