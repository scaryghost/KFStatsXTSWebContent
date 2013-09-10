/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time

/**
 * Generates the json data for the page data.json
 * @author etsai
 */
public class DataJson extends Resource {
    private def serverDataClasses, playerDataClasses, baseClasspath, defaultClass="Default.groovy", dataDir= "data"

    public DataJson() {
        serverDataClasses= [difficulties: "ServerDifficulty.groovy", levels: "ServerLevel.groovy", leveldata: "ServerLevelData.groovy",
                difficultyData: "ServerDifficultyData.groovy", records: "Records.groovy", wave: "Wave.groovy", totals: "ServerTotals.groovy"]
        playerDataClasses= [difficulties: "PlayerDifficulty.groovy", levels: "PlayerLevel.groovy", leveldata: "PlayerLevelData.groovy", 
                difficultyData: "PlayerDifficultyData.groovy", matchhistory: "MatchHistory.groovy", profile: "PlayerProfile.groovy"]
        baseClasspath= new File(getClass().protectionDomain.codeSource.location.path).getParent()
    }
    public String generatePage() {
        def dataClasspath= new File(baseClasspath, dataDir)
        def gcl= new GroovyClassLoader();
        gcl.addClasspath(baseClasspath);
        gcl.addClasspath(dataClasspath.toString())

        def getClass= {classes ->
            if (classes.containsKey(queries.table)) {
                return classes[queries.table]
            }
            return defaultClass
        }

        def creatorCtor, clazz
        if (queries.steamid64 == null) {
            clazz= getClass(serverDataClasses)
        } else {
            clazz= getClass(playerDataClasses)
        }

        creatorCtor= gcl.parseClass(new File(dataClasspath,clazz)).getDeclaredConstructor([Map.class] as Class[])
        return creatorCtor.newInstance([reader: reader, queries: queries]).create()
    }
}
