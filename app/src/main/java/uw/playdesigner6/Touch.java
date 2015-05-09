package uw.playdesigner6;

/**
 * Created by lybar_000 on 5/6/2015.
 */
public class Touch {
    float[] distance;
    boolean[] distanceCheck;
    int playerSelected = -1;
    boolean obstacleEncountered;

        public Touch(float[] distanceTemp, boolean[] distanceCheckTemp, int playerSelectedTemp, boolean obstacleEncounteredTemp){
            distance = distanceTemp;
            distanceCheck = distanceCheckTemp;
            playerSelected = playerSelectedTemp;
            obstacleEncountered = obstacleEncounteredTemp;

        }
}
