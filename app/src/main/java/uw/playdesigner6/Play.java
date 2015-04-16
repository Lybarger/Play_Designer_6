package uw.playdesigner6;

import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 4/14/2015.
 */
public class Play {

    public Map<String,List<String>> points;
    public int currentStage;
    public int currentPoint;
    public int stageCount;
    public boolean playComplete;


    public Play(Map<String,List<String>> pointsTemp, int stageCountTemp, int currentStageTemp,  int currentPointTemp,
                boolean playCompleteTemp){
        points = pointsTemp;
        stageCount = stageCountTemp;
        currentStage = currentStageTemp;
        currentPoint = currentPointTemp;
        playComplete = playCompleteTemp;
    }

    public String toString(){
        return "Stage index: " + currentStage + ", Point index: " + currentPoint +
                ", Play complete: " + playComplete;

    }
}
