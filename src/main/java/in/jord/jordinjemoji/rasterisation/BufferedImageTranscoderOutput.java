package in.jord.jordinjemoji.rasterisation;

import org.apache.batik.transcoder.TranscoderOutput;

import java.awt.image.BufferedImage;

public final class BufferedImageTranscoderOutput extends TranscoderOutput {
    private BufferedImage image;

    public final void setImage(final BufferedImage image) {
        this.image = image;
    }

    public final BufferedImage getImage() {
        return this.image;
    }
}
