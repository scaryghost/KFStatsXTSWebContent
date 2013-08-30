public class Veterancy {
    private def damages= [25000, 100000, 500000, 1500000, 3500000, 5500000]
    private def requirements= [medic: [damagehealed: [200, 750, 4000, 12000, 25000, 100000]], 
        sharpshooter: [headshotkills: [30, 100, 700, 2500, 5500, 8500]], 
        "support Specialist": [shotgundamage: damages, weldingpoints: [2000, 7000, 35000, 120000, 250000, 370000]], 
        commando: [bullpupdamage: damages, stalkerkills: [30, 100, 350, 1200, 2400, 3600]], 
        berserker: [meleedamage: damages], firebug: [flamethrowerdamage: damages], demo: [explosivesdamage: damages]]
    public static def niceNames= [damagehealed: "Healing", headshotkills: "Head Shots", shotgundamage: "Shotgun Damage", weldingpoints: "Welding", 
        bullpupdamage: "Assault Rifle Damage", stalkerkills: "Stalkers Killed", meleedamage: "Melee Damage", flamethrowerdamage: "Fire Damage", 
        explosivesdamage: "Explosives Damage"]

    private def playerProgress= [:], perkLevels= [:], xmlRoot

    public Veterancy(def steamID64) {
        def url= new URL("http://steamcommunity.com/profiles/${steamID64}/statsfeed/1250")
        def content= url.getContent().readLines().join("\n")
        xmlRoot= new XmlSlurper().parseText(content)
    }

    public def getPerks() {
        return requirements
    }

    public def getPlayerProgress(def stat) {
        if (playerProgress[stat] == null) {
            def item= xmlRoot.stats.item.find { it.APIName.text() == stat }
            playerProgress[stat]= item.value.text().toInteger()
        }
        return playerProgress[stat]
    }

    public def getProgress(level, progress) {
        def amount
        if (level >= progress.size()) {
            amount= progress.last() * (level - progress.size() + 2)
        } else {
            amount= progress[level]
        }
        amount
    }

    public def getLevel(perk) {
        if (perkLevels[perk] == null) {
            def totalLevels= []
            requirements[perk].each {apiName, progress ->
                def level= 0, amount

                amount= getProgress(level, progress)
                level++
                while(amount <= getPlayerProgress(apiName)) {
                    amount= getProgress(level, progress)
                    level++
                }
                totalLevels << level - 1
            }
            perkLevels[perk]= totalLevels.min()
        }
        return perkLevels[perk]
    }
    
    public def calcProgress(perk) {
        def totalPercent= 0
        def level= getLevel(perk)

        requirements[perk].each {apiName, progress ->
            def currProgress= getProgress(level, progress), prevProgress= getProgress(level - 1, progress)
            def percent= level > 0 ? [((getPlayerProgress(apiName) - prevProgress) * 100) / (currProgress - prevProgress), 100].min() :
                [(getPlayerProgress(apiName) * 100) / currProgress, 100].min()
            totalPercent+= percent / requirements[perk].keySet().size()
        }
        return totalPercent
    }
}
