Ripple Decorator View
=====================

RippleDecoratorView is a widget library with a view used to wrap any of your layouts.
It allows you, for any touch, to add animations effects such as: ripple stroke, ripple fill, highlight, and zoom.
These animations come with a configurable fade-in, fade-out timeline.

It does not capture touches, so you can pepper it safely though your XML layout files.

![Example](https://raw.githubusercontent.com/thomsonreuters/RippleDecoratorView/master/RDV.gif)

Usage
=====================

Import the custom namespace (app, rdv, your choice) into your layout, below the android schema:

    xmlns:rdv="http://schemas.android.com/apk/res-auto"


Then wrap any layout with a com.thomsonreuters.rippledecoratorview.RippleDecoratorView element:

    <com.thomsonreuters.rippledecoratorview.RippleDecoratorView
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="center_horizontal"
      android:layout_margin="4dp"
      rdv:rdv_rippleColor="@android:color/holo_blue_dark"
      rdv:rdv_rippleAnimationFrames="60"
      rdv:rdv_rippleAnimationPeakFrame="15"
      rdv:rdv_rippleMaxAlpha="0.8"
      rdv:rdv_rippleAnimationDuration="600"
      rdv:rdv_rippleRadius="50dp">

         <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="4dp" />

    </com.thomsonreuters.rippledecoratorview.RippleDecoratorView>



Configuration
=====================

RippleDecoratorView allows configuration via XML properties or method calls.
For every property described below there is a getProperty and setProperty method.


Ripple
------

    rdv_rippleAnimationTrigger

When the ripple animation will be played: onTap, onTouchDown or onTouchUp.
Default: onTap.


    rdv_rippleColor

Change the color of the ripple.
Default: white.


    rdv_rippleStyle

Change the ripple style: stroke only draws the outline, fill draws the full circle.
Default: stroke.


    rdv_rippleCentered

Whether the ripple originates from the touching point, or the center of the view.
Default: false.


    rdv_rippleRadius

Radius of the ripple. If -1 it takes the size of the view.
Default: -1.


    rdv_ripplePadding

If the radius is the size of the view, the radius size is reduced by this amount.
Default: 0.


    rdv_rippleMaxAlpha

Maximum transparency reached by the ripple during the animation.
Default: 1.


Highlight
------

Highlight can be displayed in addition to the ripple. It covers the entire view in a color.


    rdv_highlightAnimation

Whether the highlight effect is applied.
Default: false.


    rdv_highlightColor

The color of the highlight effect.
Default: same as rdv_rippleColor.


    rdv_highlightMaxAlpha

Maximum transparency reached by the highlight during the animation.
Default: 0,2.


Zoom
------

Zoom animates a little zoom in bump in addition to the ripple and highlight.
Warning: Zoom animation cannot be cancelled once started, use with caution.


    rdv_zoomAnimation

Whether the zoom animation is played.
Default: false.


    rdv_zoomAnimationScale

The scale to which the view zooms.
Default: 1,03.


    rdv_zoomAnimationTrigger

When the zoom animation will be played: onTap, onTouchDown or onTouchUp.
Default: onTap.


Fine tuning animation
=====================

By default, RippleDecoratorView has a fade-in, fade-out animation for the ripple and highlight,
on top of the optional zoom. Library users can configure the timeline for this animation to their needs.

The API is designed such as an animation has a Duration in milliseconds, and a Frames value to represent
each of the steps of the animation.
The animation updates at a rate of Duration / Frames. For example, an animation that lasts 1000 milliseconds
and has 60 frames would update itself once every 16.6 milliseconds. A high Frames values can cause the
update rate to be more frequent than the screen's, causing "dropped" frames. A low Frames value may not look smooth.
Keeping the default values or finding a sweet spot for any given Duration is recommended.


    rdv_rippleAnimationDuration

Time it takes for the ripple/highlight animation to complete, in milliseconds.
Default: 400.


    rdv_rippleAnimationFrames

Number of frames the animation is divided into.
Default: 60.


    rdv_zoomAnimationDuration

Time it takes for the zoom animation to complete, in milliseconds.
Default: same as rdv_rippleAnimationDuration.


Peak frames
------

Peak frames define define key frames in the timeline when the animation changes behaviour. For example,
a 1000 milliseconds animation with 10 frames and a peak on 7 will fade in for 0,7 seconds, then fade out for 0,3 seconds.


    rdv_rippleAnimationPeakFrame

Defines, for the ripple, when the animation goes from fade-in into fade-out.
Default: same as rdv_rippleAnimationFrames.


    rdv_highlightAnimationPeakFrame

Defines, for the highlight, when the animation goes from fade-in into fade-out.
Default: same as rdv_rippleAnimationFrames.


Interpolation
------

Interpolation is not available though XML, but its values can retrieved and modified through code.


    interpolator

Defines the interpolator for the ripple and highlight.
Default: LinearInterpolator.


    zoomInterpolator

Defines the interpolator for the zoom.
Default: LinearInterpolator.


Examples
=====================

There is a module in the repository called RippleDecoratorView-Example that showcases most configurations.


Distribution and installation
=====================

[Download the latest .aar from Maven Central](https://oss.sonatype.org/content/groups/public/com/thomsonreuters/rippledecoratorview/)

**or**

add it as a dependency on your ```build.gradle```

    repositories {
        ...
        mavenCentral()
        ...
    }

    dependencies {
        ...
        compile 'com.thomsonreuters:rippledecoratorview:+'
        ...
    }
    
**or**
    repositories {
        ...
        maven {
	        url "https://jitpack.io"
	    }
        ...
    }

    dependencies {
        ...
        compile 'com.github.thomsonreuters:RippleDecoratorView:v1.0.1'
        ...
    }

Contact
=====================

Francisco Estevez - francisco.estevezgarcia@thomsonreuters.com

License
=====================

The Apache Software License, Version 2.0

See LICENSE.md
