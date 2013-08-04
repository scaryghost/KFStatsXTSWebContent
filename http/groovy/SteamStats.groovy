import groovy.xml.MarkupBuilder

def damages= [25000, 100000, 500000, 1500000, 3500000, 5500000]
def perks= [medic: [damagehealed: [200, 750, 4000, 12000, 25000, 100000]], 
    sharp: [headshotkills: [30, 100, 700, 2500, 5500, 8500]], 
    support: [shotgundamage: damages, weldingpoints: [2000, 7000, 35000, 120000, 250000, 370000]], 
    commando: [bullpupdamage: damages, stalkerkills: [30, 100, 350, 1200, 2400, 3600]], 
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

def getProgress= {level, progress ->
    def amount
    if (level >= progress.size()) {
        amount= progress.last() * (level - progress.size() + 2)
    } else {
        amount= progress[level]
    }
    amount
}
def getLevel= {requirements ->
    def totalLevels= []
    requirements.each {apiName, progress ->
        def item= xmlRoot.stats.item.find { it.APIName.text() == apiName }
        def level= 0, amount
        playerProgress[apiName]= item.value.text().toInteger()

        amount= getProgress(level, progress)
        level++
        while(amount <= playerProgress[apiName]) {
            amount= getProgress(level, progress)
            level++
        }
        totalLevels << level - 1
    }
    return totalLevels.min()
}

def calcProgress= {level, requirements ->
    def totalPercent= 0

    requirements.each {apiName, progress ->
        def percent= level > 0 ? [((playerProgress[apiName] - getProgress(level - 1, progress)) * 100) / (getProgress(level, progress) - getProgress(level - 1, progress)), 100].min() :
            [(playerProgress[apiName] * 100) / getProgress(level, progress), 100].min()
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
                def percent= calcProgress(perkLevels[perk], requirements)
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
                            def j= perkLevels[perk] >= 6 ? progress.last() : progress[perkLevels[perk]]
                            mkp.yieldUnescaped("${playerProgress[name]}/$j ${niceNames[name]}<br>")
                        }
                    }
                }
            }
        }
    }
}
    
print writer
