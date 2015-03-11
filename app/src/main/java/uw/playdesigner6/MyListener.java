package uw.playdesigner6;

/**
 * Created by lybar_000 on 3/10/2015.
 */
public class MyListener {
    public interface Listener {
        public void onStateChange(boolean state);
    }

    private Listener mListener = null;
    public void registerListener (Listener listener) {
        mListener = listener;
    }

    private boolean myBoolean = false;
    public void doYourWork() {
        myBoolean = true;
        if (mListener != null)
            mListener.onStateChange(myBoolean);
    }
}

