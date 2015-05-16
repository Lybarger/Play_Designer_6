package uw.playdesigner6;

import android.content.Context;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lybar_000 on 5/13/2015.
 */
public class Screens {
    public List<Screen> screens;

    public Screens(){
        screens = new ArrayList<Screen>();

    }


    public void drawScreens(Canvas canvas){
        int screenCount = screens.size();
        if (screenCount > 0) {


            for (int i = 0; i < screenCount; i++) {
                Screen screen = screens.get(i);
                canvas.drawBitmap(screen.icon, screen.getX(), screen.getY(), null);

            }


        }


    }



    public void updateScreenPositions(Players players, Ball ball){
        int screenCount = screens.size();
        if (screenCount > 0) {
            boolean[]removalList = new boolean[screenCount];

            for (int i = 0; i < screenCount; i++) {

                // Update screen location based on player location
                screens.get(i).updateLocation(players.X[screens.get(i).playerIndex],
                        players.Y[screens.get(i).playerIndex]);

                // Determine if player has ball
                removalList[i] =  ball.playerIndex == screens.get(i).playerIndex;

            }

            // Remove screens if player has ball
            for (int i = screenCount-1; i >= 0; i--){
                if (removalList[i]){
                    screens.remove(i);
                }
            }

        }

    }



    public void updateScreenCount(int playerIndex, Context context, Players players){
        int screenCount = screens.size();
        boolean screenPresent = false;
        for (int i = 0; i < screenCount; i++) {
            if (playerIndex== screens.get(i).playerIndex){
                screens.remove(i);
                screenPresent = screenPresent || true;
                break;
            }
        }
        if (!screenPresent) {

            Screen screen = new Screen(players.X[playerIndex], players.Y[playerIndex], playerIndex, false);
            screen.setContext(context);
            screen.setContext(context);
            screen.createIcon();

            screens.add(screen);
        }



    }

    public void clearAll(){
        int screenCount = screens.size();
        if (screenCount > 0) {
            // Remove screens if player has ball
            for (int i = screenCount - 1; i >= 0; i--) {
                screens.remove(i);
            }
        }

    }



}
