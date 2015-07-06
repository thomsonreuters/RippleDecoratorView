/* Copyright 2015 Thomson Reuters

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

package com.thomsonreuters.rippledecoratorviewexample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.thomsonreuters.rippledecoratorview.RippleDecoratorView;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = (ListView)findViewById(R.id.test_lsv);
        list.setAdapter(new SampleAdapter());
        RippleDecoratorView rdvNewInterpolator = (RippleDecoratorView)findViewById(R.id.ripple_interp_rdv);
        rdvNewInterpolator.setInterpolator(new DecelerateInterpolator());
        rdvNewInterpolator.setRippleStyle(RippleDecoratorView.Styles.FILL);
        rdvNewInterpolator.setZoomInterpolator(new AccelerateInterpolator());
        RippleDecoratorView rdvHighlight = (RippleDecoratorView)findViewById(R.id.ripple_highlight_rdv);
        rdvHighlight.setHighlighColor(Color.GREEN);
        rdvHighlight.setRippleAnimationDuration(1000);
        rdvHighlight.setRippleAnimationFrames(30);
        /* Ripple Animation Peak <= Ripple Animation Frames */
        rdvHighlight.setRippleAnimationPeakFrame(0);
        rdvHighlight.setHighlightAnimationPeakFrame(20);
        final RippleDecoratorView rdvActivate = (RippleDecoratorView)findViewById(R.id.ripple_activate_rdv);
        Button btnActivate = (Button)findViewById(R.id.ripple_activate_btn);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rdvActivate.doAnimation(rdvActivate.getMeasuredWidth() / 2, rdvActivate.getMeasuredHeight() / 2);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class SampleAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 50;
        }

        @Override
        public Object getItem(int position) {
            return "";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.element_tappable, parent, false);
            }
            /* Reused views need not animate */
            ((RippleDecoratorView)convertView).cancelAnimation();
            return convertView;
        }
    }
}
