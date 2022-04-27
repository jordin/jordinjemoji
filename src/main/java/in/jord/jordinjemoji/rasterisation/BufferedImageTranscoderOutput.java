package in.jord.jordinjemoji.rasterisation;

import org.apache.batik.transcoder.TranscoderOutput;

import java.awt.image.BufferedImage;

public final class BufferedImageTranscoderOutput extends TranscoderOutput {
    private BufferedImage image;

    public void setImage(final BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }
}
