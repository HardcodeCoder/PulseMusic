package com.hardcodecoder.pulsemusic;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class GlideConstantArtifacts {

    private static RoundedCorners rc;
    private static RoundedCorners smallRR;
    private static CircleCrop mCircleCrop;

    static void init(int[] radiusList) {
        rc = new RoundedCorners(radiusList[0]);
        smallRR = new RoundedCorners(radiusList[1]);
        mCircleCrop = new CircleCrop();
    }

    public static RoundedCorners getDefaultRoundingRadius() {
        return rc;
    }

    public static RoundedCorners getRoundingRadiusSmall () { return smallRR; }

    public static CircleCrop getCircleCrop () { return mCircleCrop; }

}
