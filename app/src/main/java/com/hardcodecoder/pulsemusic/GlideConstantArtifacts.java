package com.hardcodecoder.pulsemusic;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class GlideConstantArtifacts {

    private static RoundedCorners rc;
    private static RoundedCorners smallRR;

    static void init(int[] radiusList) {
        rc = new RoundedCorners(radiusList[0]);
        smallRR = new RoundedCorners(radiusList[1]);
    }

    public static RoundedCorners getDefaultRoundingRadius() {
        return rc;
    }

    public static RoundedCorners getRoundingRadiusSmall () { return smallRR; }

}
