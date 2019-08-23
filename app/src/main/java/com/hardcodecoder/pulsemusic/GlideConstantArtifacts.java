package com.hardcodecoder.pulsemusic;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class GlideConstantArtifacts {

    private static RoundedCorners rc;

    static {
        rc = new RoundedCorners(12);
    }

    public static RoundedCorners getDefaultRoundingRadius() {
        return rc;
    }


}
