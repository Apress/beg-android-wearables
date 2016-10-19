package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * ActionFragment creates an action button with the icon and label provided in its constructor.
 * Pressing the button triggers actionIntent, which is also a constructor parameter.
 */
public class ActionFragment extends Fragment {
    private static final String ARGS_LABEL = "label";
    private static final String ARGS_ICONID = "iconid";
    private static final String ARGS_ACTION_INTENT = "action_intent";
    private CircledImageView mCircledImageView;
    private int mNormalColor, mPressedColor;
    private String mLabel;
    private int mIconId;
    private Intent mActionIntent;

    public static ActionFragment newInstance(String label, int iconId, Intent actionIntent) {
        Bundle args = new Bundle();
        args.putString(ARGS_LABEL, label);
        args.putInt(ARGS_ICONID, iconId);
        args.putParcelable(ARGS_ACTION_INTENT, actionIntent);

        ActionFragment fragment = new ActionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args == null) {
            throw new RuntimeException("ActionFragment requires arguments.");
        }

        mLabel = args.getString(ARGS_LABEL);
        mIconId = args.getInt(ARGS_ICONID);
        mActionIntent = args.getParcelable(ARGS_ACTION_INTENT);

        Resources res = getResources();
        mNormalColor = res.getColor(R.color.blue);
        mPressedColor = res.getColor(R.color.dark_blue);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /* For round screens, the action button is right in the middle of the screen, but for
         * square screens, the button is slightly above the middle. WatchViewStub lets us
         * provide each shape with a different layout.
         */
        WatchViewStub watchViewStub = (WatchViewStub) getActivity().getLayoutInflater().
                inflate(R.layout.demobutton, null);

        // we can't access any child views of WatchViewStub until the layout is inflated.
        watchViewStub.setOnLayoutInflatedListener(mLayoutInflatedListener);

        return watchViewStub;
    }

    private WatchViewStub.OnLayoutInflatedListener mLayoutInflatedListener =
            new WatchViewStub.OnLayoutInflatedListener() {
        @Override
        public void onLayoutInflated(WatchViewStub watchViewStub) {
            TextView label = (TextView)watchViewStub.findViewById(R.id.label);
            label.setText(mLabel);

            mCircledImageView =
                    (CircledImageView)watchViewStub.findViewById(R.id.circledimageview);
            mCircledImageView.setImageResource(mIconId);

            // Give the button a darker color (mPressedColor) when the user touches any
            // part of the screen. When the user removes her finger from the screen,
            // go back to the original color (mNormalColor) and start the action intent.
            watchViewStub.setOnTouchListener(mTouchListener);
        }
    };

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mCircledImageView.setCircleColor(mPressedColor);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    return true;
                case MotionEvent.ACTION_UP:
                    startActivity(mActionIntent);
                    mCircledImageView.setCircleColor(mNormalColor);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    mCircledImageView.setCircleColor(mNormalColor);
                    return true;
                default:
                    return false;
            }
        }
    };
}
